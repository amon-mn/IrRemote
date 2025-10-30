package devtitans.irremote.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import devtitans.irremote.R;
import devtitans.irremote.data.model.IrCommand;
import devtitans.irremote.ui.adapters.CommandAdapter;

public class DeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_device);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listViewCommands = findViewById(R.id.listViewCommands);
        FloatingActionButton fabAddCommand = findViewById(R.id.fabAddCommand);

        // Sample data
        List<IrCommand> commandList = new ArrayList<>();
        commandList.add(new IrCommand("Ligar/Desligar", 38000, new int[]{9000, 4500, 560, 560}));
        commandList.add(new IrCommand("Aumentar Volume", 38000, new int[]{9000, 4500, 560, 1690}));
        commandList.add(new IrCommand("Diminuir Volume", 38000, new int[]{9000, 4500, 560, 2250}));

        CommandAdapter.CommandClickListener listener = new CommandAdapter.CommandClickListener() {
            @Override
            public void onSendClick(IrCommand command) {
                Toast.makeText(DeviceActivity.this, "Enviando: " + command.getName(), Toast.LENGTH_SHORT).show();
                // Lógica para enviar o comando IR
            }

            @Override
            public void onDeleteClick(IrCommand command) {
                Toast.makeText(DeviceActivity.this, "Excluindo: " + command.getName(), Toast.LENGTH_SHORT).show();
                // Lógica para excluir o comando
            }
        };

        CommandAdapter adapter = new CommandAdapter(this, commandList, listener);
        listViewCommands.setAdapter(adapter);

        fabAddCommand.setOnClickListener(view -> {
            // Lógica para adicionar um novo comando
            Toast.makeText(DeviceActivity.this, "Adicionar novo comando", Toast.LENGTH_SHORT).show();
        });
    }
}
