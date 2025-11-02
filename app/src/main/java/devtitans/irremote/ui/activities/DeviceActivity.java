// Em: app/src/main/java/devtitans/irremote/ui/activities/DeviceActivity.java

package devtitans.irremote.ui.activities;

import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
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

public class DeviceActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ConsumerIrManager ir;
    private ListView listViewCommands;
    private FloatingActionButton fabAddCommand;
    private String deviceName;
    private IrCommandRepository mRepository;
    private TextView textViewEmpty;
    private List<IrCommand> mCommandList = new ArrayList<>();
    private CommandAdapter commandAdapter;

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

        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        mRepository = new IrCommandRepository(getApplication()); // Verifique se o Repository está no caminho certo

        // 1. Configurar o Adapter
        setupAdapter();

        // 2. Observar o Banco de Dados
        observeCommands();

        // 3. Configurar o FAB
        fabAddCommand.setOnClickListener(v -> {
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
     * (ATUALIZADO E SIMPLIFICADO)
     * Cria e exibe um AlertDialog para ADICIONAR ou EDITAR um comando.
     * @param commandToEdit O comando a ser editado, ou 'null' se for para criar um novo.
     */
    private void showAddCommandDialog(@Nullable IrCommand commandToEdit) {
        final boolean isEditMode = (commandToEdit != null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_command, null); // Usa o novo XML (sem RadioButtons)
        builder.setView(dialogView);
        builder.setTitle(isEditMode ? "Editar Comando" : "Adicionar Novo Comando");

        // Referências aos campos (agora apenas 3 campos)
        EditText etName = dialogView.findViewById(R.id.editTextCommandName);
        EditText etFreq = dialogView.findViewById(R.id.editTextFrequency);
        EditText etPattern = dialogView.findViewById(R.id.editTextPattern);

        // Removemos a lógica do RadioGroup

        // --- LÓGICA DE PRÉ-PREENCHIMENTO (Modo Edição) ---
        if (isEditMode) {
            etName.setText(commandToEdit.getCommandName());
            etFreq.setText(String.valueOf(commandToEdit.getFrequency()));
            etPattern.setText(commandToEdit.getPattern());
        }
        // ------------------------------------------------

        // Botão "Salvar" (agora só faz Insert ou Update de TX)
        builder.setPositiveButton(isEditMode ? "Salvar Alterações" : "Salvar", (dialog, which) -> {
            String commandName = etName.getText().toString().trim();
            if (commandName.isEmpty()) {
                Toast.makeText(this, "Nome não pode ser vazio", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // --- Lógica de Salvar (Raw TX) ---
                String freqStr = etFreq.getText().toString();
                String patternStr = etPattern.getText().toString().trim();

                if (freqStr.isEmpty() || patternStr.isEmpty()) {
                    Toast.makeText(this, "Frequência e Padrão são obrigatórios", Toast.LENGTH_SHORT).show();
                    return;
                }

                int freq = Integer.parseInt(freqStr);

                // --- LÓGICA DE SALVAR vs ATUALIZAR ---
                if (isEditMode) {
                    // Atualiza o objeto existente
                    commandToEdit.setCommandName(commandName);
                    commandToEdit.setFrequency(freq);
                    commandToEdit.setPattern(patternStr);
                    mRepository.update(commandToEdit);
                    Toast.makeText(this, "Comando atualizado", Toast.LENGTH_SHORT).show();
                } else {
                    // Cria um novo comando
                    IrCommand newCommand = new IrCommand(deviceName, commandName, freq, patternStr);
                    mRepository.insert(newCommand);
                    Toast.makeText(this, "Comando salvo", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void setupAdapter() {
        commandAdapter = new CommandAdapter(this, mCommandList,
                // Implementação do clique "Send"
                (command) -> {
                    transmitCommand(command);
                },
                // Implementação do clique "Delete"
                (command) -> {
                    deleteCommand(command);
                },
                // Implementação do clique "Edit"
                (command) -> {
                    showAddCommandDialog(command); // Passa o comando a ser editado
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