package devtitans.irremote.ui.activities;

import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import devtitans.irremote.R;
import devtitans.irremote.ui.adapters.DeviceAdapter;

// A classe implementa o listener da SearchView
public class HomeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ConsumerIrManager ir;
    private TextView irStatus;
    private RecyclerView recyclerViewDevices;
    private DeviceAdapter deviceAdapter;
    private List<String> deviceList;
    private CardView cardStatus;

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

        // Referências da UI (Corrigido para R.id.textViewStatus)
        irStatus = findViewById(R.id.textViewStatus);
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices);
        cardStatus = findViewById(R.id.cardStatus);

        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);

        cardStatus.setOnClickListener(v -> {
            Toast.makeText(this, "A verificar status do emissor...", Toast.LENGTH_SHORT).show();
            checkEmitterStatus();
        });

        checkEmitterStatus();
        setupRecyclerView();
    }

    private void checkEmitterStatus() {
        boolean has = (ir != null) && ir.hasIrEmitter();
        irStatus.setText(has ? "Status: Emissor IR Conectado" : "Status: Emissor IR Desconectado");

        // CORREÇÃO: Usando ContextCompat.getColor para evitar depreciação
        if (has) {
            irStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            irStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

    private void setupRecyclerView() {
        // Dados de exemplo
        deviceList = new ArrayList<>(Arrays.asList("DataShow", "Ar-Condicionado Split", "SmartLamp", "Tv IR"));

        deviceAdapter = new DeviceAdapter(deviceList, deviceName -> {
            Intent intent = new Intent(HomeActivity.this, DeviceActivity.class);
            intent.putExtra("DEVICE_NAME", deviceName);
            startActivity(intent);
        });

        int numberOfColumns = 2;
        recyclerViewDevices.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        recyclerViewDevices.setAdapter(deviceAdapter);
    }

    // --- Lógica do Menu da AppBar (CORRIGIDA) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search); // Usa o ID do menu.xml
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(this); // Define esta classe como o "ouvinte"
            searchView.setQueryHint("Pesquisar dispositivo...");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) { // Assumindo que R.id.action_menu é o ícone de 3 pontos
            Toast.makeText(this, "Menu Clicado", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Métodos do SearchView.OnQueryTextListener ---

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Opcional: Força o filtro quando o utilizador prime Enter
        if (deviceAdapter != null) {
            deviceAdapter.getFilter().filter(query);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Chamado CADA VEZ que o utilizador digita uma letra
        if (deviceAdapter != null) {
            deviceAdapter.getFilter().filter(newText);
        }
        return true;
    }
}