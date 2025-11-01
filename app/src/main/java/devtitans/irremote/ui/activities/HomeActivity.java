// Em: ui/activities/HomeActivity.java

package devtitans.irremote.ui.activities;

import android.content.Intent; // Certifique-se que o Intent está importado
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import devtitans.irremote.R; // Importar R
import devtitans.irremote.ui.adapters.DeviceAdapter; // Importar o Adapter

public class HomeActivity extends AppCompatActivity {

    private ConsumerIrManager ir;
    private TextView irStatus;
    private RecyclerView recyclerViewDevices;
    private DeviceAdapter deviceAdapter;
    private List<String> deviceList; // Use List<Device> se tiver o modelo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_layout_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        irStatus = findViewById(R.id.textViewStatus);
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices);

        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        checkEmitterStatus();
        setupRecyclerView();
    }

    private void checkEmitterStatus() {
        boolean has = (ir != null) && ir.hasIrEmitter();
        irStatus.setText(has ? "Status: Emissor IR Conectado" : "Status: Emissor IR Desconectado");
    }

    private void setupRecyclerView() {
        // Dados de exemplo
        deviceList = new ArrayList<>(Arrays.asList("DataShow", "ArCondicionado Split", "SmartLamp", "Tv IR"));
        deviceAdapter = new DeviceAdapter(deviceList, deviceName -> {
            Intent intent = new Intent(HomeActivity.this, DeviceActivity.class);
            intent.putExtra("DEVICE_NAME", deviceName);
            startActivity(intent);
        });

        int numberOfColumns = 2;
        recyclerViewDevices.setLayoutManager(new GridLayoutManager(this, numberOfColumns));;
        recyclerViewDevices.setAdapter(deviceAdapter);
    }

    // --- Lógica do Menu da AppBar ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Verifica se o ID do item clicado é o do 'action_menu'
        if (item.getItemId() == R.id.action_scan) {
            Toast.makeText(this, "Menu Clicado", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}