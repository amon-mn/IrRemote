package devtitans.irremote;

import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private ConsumerIrManager ir;
    private TextView tvStatus, tvLog;
    private EditText etFreq, etPattern;
    private Button btnCheck, btnListRanges, btnTransmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
// Ajuste de insets (mantido do projeto criado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
// ---- UI ----
        tvStatus = findViewById(R.id.tvStatus);
        tvLog = findViewById(R.id.tvLog);
        etFreq = findViewById(R.id.etFreq);
        etPattern = findViewById(R.id.etPattern);
        btnCheck = findViewById(R.id.btnCheck);
        btnListRanges = findViewById(R.id.btnListRanges);
        btnTransmit = findViewById(R.id.btnTransmit);
// ---- IR Manager ----
        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        btnCheck.setOnClickListener(v -> checkEmitter());
        btnListRanges.setOnClickListener(v -> listRanges());
        btnTransmit.setOnClickListener(v -> transmit());
// Valores de teste (NEC curto)
        etFreq.setText("38000");
        etPattern.setText("9000,4500,560,560,560,560,560,1690,560,560");
    checkEmitter();
}
private void checkEmitter() {
        boolean has = (ir != null) && ir.hasIrEmitter();tvStatus.setText(has ? "Status: Emissor IR disponível" : "Status: Sem emissor IR");
        log("hasIrEmitter() = " + has);
        btnTransmit.setEnabled(has);
        btnListRanges.setEnabled(has);
    }
private void listRanges() {
        if (ir == null) {
            log("ConsumerIrManager indisponível.");
            return;
        }
        ConsumerIrManager.CarrierFrequencyRange[] ranges = ir.getCarrierFrequencies();
        if (ranges == null || ranges.length == 0) {
            log("getCarrierFrequencies(): vazio (HAL pode ter publicado fallback).");
            return;
        }
        StringBuilder sb = new StringBuilder("Faixas suportadas:\n");
        for (ConsumerIrManager.CarrierFrequencyRange r : ranges) {
            sb.append(" - ").append(r.getMinFrequency()).append(" Hz .. ")
                    .append(r.getMaxFrequency()).append(" Hz\n");
        }
        log(sb.toString().trim());
    }
private void transmit() {
        if (ir == null) {
            toast("ConsumerIrManager indisponível");
            return;
        }
Integer freq = tryParseInt(etFreq.getText().toString().trim());
        if (freq == null || freq <= 0) {
            toast("Frequência inválida");
            return;
        }
        String patStr = etPattern.getText().toString().trim();
        if (patStr.isEmpty()) {
            toast("Informe o padrão em μs, ex: 9000,4500,560,560,...");
            return;
        }
        String[] parts = patStr.split(",");
        int[] pattern = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                int v = Integer.parseInt(parts[i].trim());
                if (v <= 0) throw new NumberFormatException("valor <= 0");
                pattern[i] = v;
            }
        } catch (Exception e) {
            toast("Padrão contém valores inválidos");
            return;
        }
        if ((pattern.length % 2) != 0) {
            toast("O padrão deve ter quantidade PAR de valores (ON/OFF alternados).");
            return;
        }
        try {
            ir.transmit(freq, pattern);
            log("transmit(freq=" + freq + ", len=" + pattern.length + ") enviado.");
        } catch (Throwable t) {
            log("Falha no transmit(): " + t.getMessage());
        }
    }
    private Integer tryParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }private void log(String msg) {
        String prev = tvLog.getText() == null ? "" : tvLog.getText().toString();
        tvLog.setText(msg + "\n----------------------\n" + prev);
    }
    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}