// Em: app/src/main/java/devtitans/irremote/ui/activities/DeviceActivity.java

package devtitans.irremote.ui.activities;

import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

// Importe o tipo Nullable correto (AndroidX ou JetBrains)
// Vou usar o do AndroidX androidx.annotation.Nullable
import androidx.annotation.Nullable;

public class DeviceActivity extends AppCompatActivity {

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
        // Define o Título Dinâmico
        deviceName = getIntent().getStringExtra("DEVICE_NAME");
        if (deviceName == null) deviceName = "Dispositivo";

        Toolbar toolbar = findViewById(R.id.toolbar_device);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(deviceName); // <-- Título definido aqui
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
            showAddCommandDialog(null);
        });
    }

    /**
     * Cria e exibe um AlertDialog para ADICIONAR ou EDITAR um comando.
     * @param commandToEdit O comando a ser editado, ou 'null' se for para criar um novo.
     */
    private void showAddCommandDialog(@Nullable IrCommand commandToEdit) {
        final boolean isEditMode = (commandToEdit != null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_command, null);
        builder.setView(dialogView);

        // Define o título dinamicamente
        builder.setTitle(isEditMode ? "Editar Comando" : "Adicionar Novo Comando");

        // Referências aos campos do dialog
        EditText etName = dialogView.findViewById(R.id.editTextCommandName);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupProtocol);
        LinearLayout layoutTX = dialogView.findViewById(R.id.layoutTX);
        LinearLayout layoutNEC = dialogView.findViewById(R.id.layoutNEC);
        EditText etFreq = dialogView.findViewById(R.id.editTextFrequency);
        EditText etPattern = dialogView.findViewById(R.id.editTextPattern);
        EditText etHex = dialogView.findViewById(R.id.editTextHexCode);
        RadioButton radioTX = dialogView.findViewById(R.id.radioTX);
        RadioButton radioNEC = dialogView.findViewById(R.id.radioNEC);

        // --- LÓGICA DE PRÉ-PREENCHIMENTO (Modo Edição) ---
        if (isEditMode) {
            etName.setText(commandToEdit.getCommandName());

            if ("NEC".equals(commandToEdit.getProtocol())) {
                radioNEC.setChecked(true);
                layoutNEC.setVisibility(View.VISIBLE);
                layoutTX.setVisibility(View.GONE);
                etHex.setText(commandToEdit.getPayload()); // Payload é o Hex
            } else { // "TX" ou outro
                radioTX.setChecked(true);
                layoutTX.setVisibility(View.VISIBLE);
                layoutNEC.setVisibility(View.GONE);

                // Extrai a freq e o pattern do payload "38000 9000,..."
                String payload = commandToEdit.getPayload();
                String[] parts = payload.split(" ", 2);
                if (parts.length == 2) {
                    etFreq.setText(parts[0]);
                    etPattern.setText(parts[1]);
                }
            }
        } else {
            // Modo Criação (garante que TX esteja visível por defeito)
            layoutTX.setVisibility(View.VISIBLE);
            layoutNEC.setVisibility(View.GONE);
        }
        // ------------------------------------------------

        // Lógica para mostrar/esconder campos TX vs NEC
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioTX) {
                layoutTX.setVisibility(View.VISIBLE);
                layoutNEC.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioNEC) {
                layoutTX.setVisibility(View.GONE);
                layoutNEC.setVisibility(View.VISIBLE);
            }
        });

        // Botão "Salvar" (agora faz Insert ou Update)
        builder.setPositiveButton(isEditMode ? "Salvar Alterações" : "Salvar", (dialog, which) -> {
            String commandName = etName.getText().toString().trim();
            if (commandName.isEmpty()) {
                Toast.makeText(this, "Nome não pode ser vazio", Toast.LENGTH_SHORT).show();
                return;
            }

            String protocol;
            String payload;

            try {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioTX) {
                    protocol = "TX";
                    String freqStr = etFreq.getText().toString();
                    String patternStr = etPattern.getText().toString().trim();
                    if (freqStr.isEmpty() || patternStr.isEmpty()) {
                        Toast.makeText(this, "Frequência e Padrão são obrigatórios", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    payload = freqStr + " " + patternStr;

                } else { // R.id.radioNEC
                    protocol = "NEC";
                    payload = etHex.getText().toString().trim();
                    if (payload.isEmpty()) {
                        Toast.makeText(this, "Código Hex não pode ser vazio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // --- LÓGICA DE SALVAR vs ATUALIZAR ---
                if (isEditMode) {
                    // Atualiza o objeto existente
                    commandToEdit.setCommandName(commandName);
                    commandToEdit.setProtocol(protocol);
                    commandToEdit.setPayload(payload);
                    // Atualiza a frequência (necessário para o TX)
                    if ("TX".equals(protocol)) {
                        commandToEdit.setFrequency(Integer.parseInt(etFreq.getText().toString()));
                    } else {
                        commandToEdit.setFrequency(38000); // Padrão NEC
                    }
                    mRepository.update(commandToEdit);
                    Toast.makeText(this, "Comando atualizado", Toast.LENGTH_SHORT).show();
                } else {
                    // Cria um novo comando
                    int freq = 38000; // Padrão
                    if ("TX".equals(protocol)) {
                        freq = Integer.parseInt(etFreq.getText().toString());
                    }
                    IrCommand newCommand = new IrCommand(deviceName, commandName, protocol, payload);
                    newCommand.setFrequency(freq); // Define a frequência (necessário para a lógica TX)
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

// ---
// --- OS MÉTODOS EM FALTA ESTAVAM AQUI FORA ---
// --- AGORA ESTÃO DENTRO DA CLASSE DeviceActivity ---
// ---

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
                // (NOVO) Implementação do clique "Edit"
                (command) -> {
                    showAddCommandDialog(command); // Passa o comando a ser editado
                }
        );
        listViewCommands.setAdapter(commandAdapter);
    }

    private void observeCommands() {
        mRepository.getCommandsByDevice(deviceName).observe(this, commands -> {
            mCommandList.clear();
            mCommandList.addAll(commands);
            commandAdapter.notifyDataSetChanged();

            // (META 1) Lógica de Visibilidade do Estado Vazio
            if (commands.isEmpty()) {
                listViewCommands.setVisibility(View.GONE);
                textViewEmpty.setVisibility(View.VISIBLE);
            } else {
                listViewCommands.setVisibility(View.VISIBLE);
                textViewEmpty.setVisibility(View.GONE);
            }
        });
    }

    // Lógica de Envio (Transmit)
    private void transmitCommand(IrCommand command) {
        if (ir == null || !ir.hasIrEmitter()) {
            Toast.makeText(this, "Emissor IR não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String protocol = command.getProtocol();

            if ("TX".equals(protocol)) {
                // --- PROTOCOLO RAW (TX) ---
                int freq = command.getTxFrequency();
                int[] pattern = command.getTxPatternAsArray();

                if (freq == 0 || pattern.length == 0) {
                    Toast.makeText(this, "Erro no formato do padrão TX", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ((pattern.length % 2) != 0) {
                    Toast.makeText(this, "Erro: O padrão deve ter um número PAR de valores (ON/OFF).", Toast.LENGTH_LONG).show();
                    return;
                }
                // Chama o metodo padrão
                ir.transmit(freq, pattern);
                Toast.makeText(this, "Enviado TX: " + command.getCommandName(), Toast.LENGTH_SHORT).show();

            } else if ("NEC".equals(protocol)) {
                // --- PROTOCOLO NEC (CUSTOMIZADO) ---
                String payload = command.getPayload();

                // (Assumindo que a HAL/Driver trata o payload "NEC ...")
                // (Ou que a API foi modificada para aceitar um hex)

                // Vou usar a abordagem de "Payload como ASCII" discutida anteriormente,
                // usando uma frequência "mágica" -1 para a HAL saber que é um NEC.
                int freqMagica = -1;
                int[] patternPayload = command.getPayloadAsAsciiIntArray(); // NECESSÁRIO CRIAR ESTE METODO

                ir.transmit(freqMagica, patternPayload);

                Toast.makeText(this, "Enviado NEC: " + command.getCommandName(), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Lógica de Exclusão (Delete)
    private void deleteCommand(IrCommand command) {
        mRepository.delete(command);
        Toast.makeText(this, command.getCommandName() + " excluído", Toast.LENGTH_SHORT).show();
        // O LiveData atualizará a lista automaticamente
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Lida com o clique no botão "Voltar" da AppBar
        return true;
    }
} // <-- ESTA É A CHAVETA DE FECHO FINAL DA CLASSE