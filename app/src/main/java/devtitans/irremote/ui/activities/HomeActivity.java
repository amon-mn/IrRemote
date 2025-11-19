package devtitans.irremote.ui.activities;

import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import devtitans.irremote.R;
import devtitans.irremote.data.model.Device;
import devtitans.irremote.data.repository.DeviceRepository;
import devtitans.irremote.ui.adapters.DeviceAdapter;

public class HomeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ConsumerIrManager ir;
    private TextView irStatus;
    private RecyclerView recyclerViewDevices;
    private DeviceAdapter deviceAdapter;
    private CardView cardStatus;
    private FloatingActionButton fab; // Botão Flutuante

    private DeviceRepository deviceRepository;

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
        cardStatus = findViewById(R.id.cardStatus);
        fab = findViewById(R.id.fab); // Inicializa o FAB

        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        deviceRepository = new DeviceRepository(getApplication()); // Inicializa Repository

        // Clique do Card de Status
        cardStatus.setOnClickListener(v -> {
            Toast.makeText(this, "Verificando status...", Toast.LENGTH_SHORT).show();
            checkEmitterStatus();
        });

        // Clique do Botão Adicionar (+)
        fab.setOnClickListener(v -> {
            showAddDeviceDialog();
        });

        checkEmitterStatus();
        setupRecyclerView();
    }

    private void checkEmitterStatus() {
        boolean has = (ir != null) && ir.hasIrEmitter();
        irStatus.setText(has ? "Status: Emissor IR Conectado" : "Status: Emissor IR Desconectado");
        if (has) {
            irStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            irStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

    private void setupRecyclerView() {
        deviceAdapter = new DeviceAdapter(device -> {
            Intent intent = new Intent(HomeActivity.this, DeviceActivity.class);
            intent.putExtra("DEVICE_NAME", device.getName());
            startActivity(intent);
        });

        int numberOfColumns = 2;
        recyclerViewDevices.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        recyclerViewDevices.setAdapter(deviceAdapter);

        // Observa as mudanças no Banco de Dados
        deviceRepository.getAllDevices().observe(this, devices -> {
            // Apenas atualiza a lista, mesmo que esteja vazia (0 dispositivos)
            if (devices != null) {
                deviceAdapter.setDevices(devices);
            }
        });
    }
    // Cria dados iniciais para não ficar vazio na primeira vez

    // Abre o diálogo para criar novo dispositivo
    private void showAddDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo Dispositivo");

        // Infla o layout simples (apenas nome)
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_device, null);
        builder.setView(view);

        final EditText input = view.findViewById(R.id.editTextDeviceName);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String deviceName = input.getText().toString().trim();
            if (!deviceName.isEmpty()) {
                saveNewDevice(deviceName);
            } else {
                Toast.makeText(this, "Nome inválido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Salva no Banco de Dados
    private void saveNewDevice(String name) {
        long now = System.currentTimeMillis();
        Device newDevice = new Device(name, "00:00", now, R.drawable.ic_launcher_foreground);
        deviceRepository.insert(newDevice);
        Toast.makeText(this, "Dispositivo criado!", Toast.LENGTH_SHORT).show();
    }

    // --- Menu e Pesquisa ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint("Pesquisar dispositivo...");
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) { return false; }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (deviceAdapter != null) {
            deviceAdapter.getFilter().filter(newText);
        }
        return true;
    }
}