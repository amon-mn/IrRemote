// Em: app/src/main/java/devtitans/irremote/ui/activities/DeviceActivity.java

package devtitans.irremote.ui.activities;

import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import devtitans.irremote.R;
import devtitans.irremote.data.repository.IrCommandRepository;
import devtitans.irremote.data.model.IrCommand;
import devtitans.irremote.ui.adapters.CommandAdapter;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import devtitans.irremote.util.IrLearningUtil;

public class DeviceActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ConsumerIrManager ir;
    private ListView listViewCommands;
    private FloatingActionButton fabAddCommand;
    private String deviceName;
    private IrCommandRepository mRepository;
    private TextView textViewEmpty;
    private List<IrCommand> mCommandList = new ArrayList<>();
    private CommandAdapter commandAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.device_layout_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceName = getIntent().getStringExtra("DEVICE_NAME");
        if (deviceName == null) deviceName = "Dispositivo";

        Toolbar toolbar = findViewById(R.id.toolbar_device);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Referências
        listViewCommands = findViewById(R.id.listViewCommands);
        fabAddCommand = findViewById(R.id.fabAddCommand);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBarLoading);

        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        mRepository = new IrCommandRepository(getApplication()); // Verifique se o Repository está no caminho certo

        // 1. Configurar o Adapter
        setupAdapter();

        // 2. Observar o Banco de Dados
        observeCommands();


        // 3. Configurar o FAB
        // 3. Configurar o FAB com o Fluxo de Aprendizado
        fabAddCommand.setOnClickListener(v -> {
            // Feedback visual
            showAddCommandDialog(null);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu); // Infla o novo menu

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(this); // Define esta classe como o "ouvinte"
            searchView.setQueryHint("Pesquisar comando...");
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Não precisamos de ação no "Submit", apenas na digitação
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // A mágica acontece aqui: O ArrayAdapter (CommandAdapter)
        // usa o filtro nativo para pesquisar o texto do 'toString()' (o commandName).
        if (commandAdapter != null) {
            commandAdapter.getFilter().filter(newText);
        }
        return true;
    }
    /**
     * (ATUALIZADO) Exibe Dialog para Adicionar/Editar.
     * @param commandToEdit Objeto existente para EDIÇÃO. Null se for novo.
     * @param learnedData Dados vindos do driver (index 0 = freq, resto = padrão). Null se não houver.
     */
    /**
     * Exibe Dialog. Se commandToEdit for null, inicia o fluxo de Leitura IR (Loading).
     */
    private void showAddCommandDialog(@Nullable IrCommand commandToEdit) {
        final boolean isEditMode = (commandToEdit != null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_command, null);
        builder.setView(dialogView);
        builder.setTitle(isEditMode ? "Editar Comando" : "Novo Comando IR");

        // Referências de Layout
        View loadingContainer = dialogView.findViewById(R.id.loadingContainer);
        View formContainer = dialogView.findViewById(R.id.formContainer);

        // Referências de Campos
        EditText etName = dialogView.findViewById(R.id.editTextCommandName);
        EditText etFreq = dialogView.findViewById(R.id.editTextFrequency);
        EditText etPattern = dialogView.findViewById(R.id.editTextPattern);

        // --- CONTROLE DE ESTADO INICIAL ---
        if (isEditMode) {
            // MODO EDIÇÃO: Mostra form direto, preenchido
            loadingContainer.setVisibility(View.GONE);
            formContainer.setVisibility(View.VISIBLE);

            etName.setText(commandToEdit.getCommandName());
            etFreq.setText(String.valueOf(commandToEdit.getFrequency()));
            etPattern.setText(commandToEdit.getPattern());
        } else {
            // MODO NOVO: Mostra Loading e esconde Form
            loadingContainer.setVisibility(View.VISIBLE);
            formContainer.setVisibility(View.GONE);

            // Inicia a busca em Background IMEDIATAMENTE
            startIrLearningTask(loadingContainer, formContainer, etFreq, etPattern, etName);
        }
        // Botões do Dialog
        builder.setPositiveButton("Salvar", null); // null aqui para sobrescrever depois (evitar fechar se inválido)
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Sobrescreve o botão Positive para adicionar a lógica de salvar
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Se o loading ainda estiver visível, ignora o clique ou mostra mensagem
            if (loadingContainer.getVisibility() == View.VISIBLE) {
                Toast.makeText(this, "Aguarde a leitura do sinal...", Toast.LENGTH_SHORT).show();
                return;
            }
            // Copie o conteúdo do bloco "builder.setPositiveButton" anterior para cá
            saveCommandLogic(dialog, isEditMode, commandToEdit, etName, etFreq, etPattern);
        });
    }

    // --- Lógica de Background movida para metodo auxiliar ---
    private void startIrLearningTask(View loadingView, View formView, EditText etFreq, EditText etPattern, EditText etName) {
        executor.execute(() -> {
            try {
                // SIMULAÇÃO DE ESPERA (2s) - Remova quando o Framework estiver pronto
                Thread.sleep(2000);

                // CHAMADA REAL (Descomente quando pronto)
                //int[] learnedData = IrLearningUtil.learnIrCode(ir);

                // DADOS MOCKADOS PARA TESTE
                int[] learnedData = new int[]{38000, 9050, 4450, 600, 1600, 600, 600};

                mainHandler.post(() -> {
                    // Esconde Loading, Mostra Form
                    loadingView.setVisibility(View.GONE);
                    formView.setVisibility(View.VISIBLE);

                    if (learnedData != null && learnedData.length > 0) {
                        // Preenche os campos
                        etFreq.setText(String.valueOf(learnedData[0]));

                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < learnedData.length; i++) {
                            sb.append(learnedData[i]);
                            if (i < learnedData.length - 1) sb.append(",");
                        }
                        etPattern.setText(sb.toString());

                        etName.requestFocus(); // Foco no nome para digitar
                    } else {
                        Toast.makeText(this, "Tempo esgotado ou erro na leitura.", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    loadingView.setVisibility(View.GONE);
                    formView.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Método auxiliar só para organizar o código de salvar (copie sua lógica antiga pra cá)
    private void saveCommandLogic(AlertDialog dialog, boolean isEditMode, IrCommand commandToEdit, EditText etName, EditText etFreq, EditText etPattern) {
        String commandName = etName.getText().toString().trim();
        if (commandName.isEmpty()) {
            Toast.makeText(this, "Nome obrigatório", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int freq = Integer.parseInt(etFreq.getText().toString());
            String pattern = etPattern.getText().toString().trim();

            if (isEditMode) {
                commandToEdit.setCommandName(commandName);
                commandToEdit.setFrequency(freq);
                commandToEdit.setPattern(pattern);
                mRepository.update(commandToEdit);
            } else {
                IrCommand newCmd = new IrCommand(deviceName, commandName, freq, pattern);
                mRepository.insert(newCmd);
            }
            dialog.dismiss(); // Fecha o dialog se deu tudo certo
        } catch (Exception e) {
            Toast.makeText(this, "Erro nos dados", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAdapter() {
        commandAdapter = new CommandAdapter(this, mCommandList,
                (command) -> transmitCommand(command),
                (command) -> deleteCommand(command),
                (command) -> {
                    // O botão editar passa null no segundo parâmetro (sem dados aprendidos)
                    showAddCommandDialog(command);
                }
        );
        listViewCommands.setAdapter(commandAdapter);
    }
    private void observeCommands() {
        mRepository.getCommandsByDevice(deviceName).observe(this, commands -> {
            commandAdapter.clear();
            commandAdapter.addAll(commands);
            commandAdapter.notifyDataSetChanged();
            if (commands.isEmpty()) {
                listViewCommands.setVisibility(View.GONE);
                textViewEmpty.setVisibility(View.VISIBLE);
            } else {
                listViewCommands.setVisibility(View.VISIBLE);
                textViewEmpty.setVisibility(View.GONE);
            }
        });
    }

    /**
     * (ATUALIZADO E SIMPLIFICADO)
     * Lógica de Envio (Transmit) - Agora só trata TX
     */
    private void transmitCommand(IrCommand command) {
        if (ir == null || !ir.hasIrEmitter()) {
            Toast.makeText(this, "Emissor IR não disponível", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int freq = command.getFrequency();
            int[] pattern = command.getPatternAsArray();

            if (freq == 0 || pattern.length == 0) {
                Toast.makeText(this, "Erro no formato do padrão", Toast.LENGTH_SHORT).show();
                return;
            }
            // Validação de Padrão PAR (da sua MainActivity original)
            if ((pattern.length % 2) != 0) {
                Toast.makeText(this, "Erro: O padrão deve ter um número PAR de valores (ON/OFF).", Toast.LENGTH_LONG).show();
                return;
            }
            // Chama o metodo padrão
            ir.transmit(freq, pattern);
            Toast.makeText(this, "Enviado TX: " + command.getCommandName(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Lógica de Exclusão (Delete)
    private void deleteCommand(IrCommand command) {
        mRepository.delete(command);
        commandAdapter.notifyDataSetChanged();
        Toast.makeText(this, command.getCommandName() + " excluído", Toast.LENGTH_SHORT).show();
        // O LiveData atualizará a lista automaticamente
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Lida com o clique no botão "Voltar" da AppBar
        return true;
    }
}