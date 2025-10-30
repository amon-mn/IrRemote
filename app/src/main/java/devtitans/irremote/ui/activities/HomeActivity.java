package devtitans.irremote.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import devtitans.irremote.R;
import devtitans.irremote.data.model.Device;
import devtitans.irremote.ui.adapters.DeviceAdapter;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewDevices);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            // Futuramente, abrir tela de criação de dispositivo
        });

        // Mock data
        List<Device> deviceList = new ArrayList<>();
        deviceList.add(new Device("DataShow", "00:11:22:33:44:55", new Date(), R.drawable.ic_datashow));
        deviceList.add(new Device("SmartLamp", "AA:BB:CC:DD:EE:FF", new Date(), R.drawable.ic_smartlamp));
        deviceList.add(new Device("Split", "11:22:33:44:55:66", new Date(), R.drawable.ic_air_conditioner));
        deviceList.add(new Device("Tv IR", "A1:B2:C3:D4:E5:F6", new Date(), R.drawable.ic_tv));

        DeviceAdapter.OnItemClickListener listener = device -> {
            Intent intent = new Intent(HomeActivity.this, DeviceActivity.class);
            // Passar dados do dispositivo para a próxima tela, se necessário
            // intent.putExtra("DEVICE_ID", device.getId());
            startActivity(intent);
        };

        DeviceAdapter adapter = new DeviceAdapter(deviceList, listener);
        recyclerView.setAdapter(adapter);
    }
}
