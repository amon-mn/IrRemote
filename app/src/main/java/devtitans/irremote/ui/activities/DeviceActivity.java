package devtitans.irremote.ui.activities;

import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import devtitans.irremote.R;
import devtitans.irremote.data.model.IrCommand;
import devtitans.irremote.data.repository.IrCommandRepository;
import devtitans.irremote.ui.adapters.CommandAdapter;
import devtitans.irremote.util.IrLearningListener; // <--- Import da Interface
import devtitans.irremote.util.IrReflectionUtil;
import devtitans.irremote.util.IrSignal;           // <--- Import do Modelo

public class DeviceActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ConsumerIrManager ir;
    private ListView listViewCommands;
    private FloatingActionButton fabAddCommand;
    private String deviceName;
    private IrCommandRepository mRepository;
    private TextView textViewEmpty;
    private List<IrCommand> mCommandList = new ArrayList<>();
    private CommandAdapter commandAdapter;

    // Executor para tarefas de background (usado pelo Reflection)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(deviceName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Referências
        listViewCommands = findViewById(R.id.listViewCommands);
        fabAddCommand = findViewById(R.id.fabAddCommand);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        mRepository = new IrCommandRepository(getApplication());

        // 1. Configurar o Adapter
        setupAdapter();

        // 2. Observar o Banco de Dados
        observeCommands();

        // 3. Configurar o FAB
        fabAddCommand.setOnClickListener(v -> {
            // Inicia fluxo de novo comando (Loading -> Hardware -> Form)
            showAddCommandDialog(null);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint("Pesquisar comando...");
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (commandAdapter != null) {
            commandAdapter.getFilter().filter(newText);
        }
        return true;
    }

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

            // Inicia a busca via Reflection + Proxy
            startIrLearningTask(loadingContainer, formContainer, etFreq, etPattern, etName);
        }

        // Botões do Dialog
        builder.setPositiveButton("Salvar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // Se cancelar durante o loading, para o hardware
            if (!isEditMode) IrReflectionUtil.stopLearning(ir);
            dialog.cancel();
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Evita fechar clicando fora durante o loading
        dialog.show();

        // Sobrescreve o botão Positive
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (loadingContainer.getVisibility() == View.VISIBLE) {
                Toast.makeText(this, "Aguarde a leitura do sinal...", Toast.LENGTH_SHORT).show();
                return;
            }
            saveCommandLogic(dialog, isEditMode, commandToEdit, etName, etFreq, etPattern);
        });
    }

    // --- Lógica de Aprendizado (INTEGRADA COM REFLECTION) ---
    private void startIrLearningTask(View loadingView, View formView, EditText etFreq, EditText etPattern, EditText etName) {

        // 1. Chama o utilitário que usa Dynamic Proxy para falar com o Framework
        // OBS: Agora usamos 'IrLearningListener' diretamente (sem o prefixo IrReflectionUtil)
        IrReflectionUtil.startLearning(ir, executor, new IrLearningListener() {

            @Override
            public void onLearned(@NonNull IrSignal signal) {
                // Dados já vêm empacotados e seguros na classe IrSignal
                mainHandler.post(() -> {
                    loadingView.setVisibility(View.GONE);
                    formView.setVisibility(View.VISIBLE);

                    etFreq.setText(String.valueOf(signal.frequency));
                    etPattern.setText(signal.getPatternString()); // Metodo helper da classe IrSignal

                    etName.requestFocus();
                });
            }

            @Override
            public void onError(int errorCode, @NonNull String message) {
                mainHandler.post(() -> {
                    // Usamos a constante da Interface IrLearningListener
                    if (errorCode == IrLearningListener.ERROR_TIMEOUT) {
                        Toast.makeText(DeviceActivity.this, "Tempo esgotado!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DeviceActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show();
                    }

                    loadingView.setVisibility(View.GONE);
                    formView.setVisibility(View.VISIBLE);
                });
            }
        });

        // 2. Timeout de Segurança (5 segundos)
        // Se o hardware não responder em 5s, paramos o processo
        mainHandler.postDelayed(() -> {
            if (loadingView.getVisibility() == View.VISIBLE) {
                IrReflectionUtil.stopLearning(ir);
                Toast.makeText(this, "Tempo esgotado. Tente novamente.", Toast.LENGTH_SHORT).show();

                loadingView.setVisibility(View.GONE);
                formView.setVisibility(View.VISIBLE);
            }
        }, 5000);
    }

    // Metodo auxiliar para salvar
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
            dialog.dismiss();
        } catch (Exception e) {
            Toast.makeText(this, "Erro nos dados", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAdapter() {
        commandAdapter = new CommandAdapter(this, mCommandList,
                (command) -> transmitCommand(command),
                (command) -> deleteCommand(command),
                (command) -> showAddCommandDialog(command)
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
            if ((pattern.length % 2) != 0) {
                Toast.makeText(this, "Erro: O padrão deve ter um número PAR de valores.", Toast.LENGTH_LONG).show();
                return;
            }
            ir.transmit(freq, pattern);
            Toast.makeText(this, "Enviado: " + command.getCommandName(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCommand(IrCommand command) {
        mRepository.delete(command);
        commandAdapter.notifyDataSetChanged();
        Toast.makeText(this, command.getCommandName() + " excluído", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // PARA O APRENDIZADO se o usuário sair da tela
        IrReflectionUtil.stopLearning(ir);
    }
}