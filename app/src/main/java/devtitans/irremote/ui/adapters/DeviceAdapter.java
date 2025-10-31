package devtitans.irremote.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import devtitans.irremote.R;
// Importe o seu modelo de dados Device (se estiver a usá-lo)
import devtitans.irremote.data.model.Device;

// Adapte para usar o seu modelo de dados (Device) em vez de String, se já o criou.
// Vou usar String por agora, para corresponder aos seus layouts.
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<String> deviceList; // Mude para List<Device> se estiver a usar o modelo
    private OnDeviceClickListener listener;

    // *** PASSO CRÍTICO 1: A INTERFACE ***
    // Esta interface permite que a Activity (HomeActivity) saiba qual item foi clicado.
    public interface OnDeviceClickListener {
        void onDeviceClick(String deviceName); // Mude para Device device se usar o modelo
    }

    public DeviceAdapter(List<String> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        String deviceName = deviceList.get(position);
        holder.bind(deviceName, listener);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceNameTextView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.textViewDeviceName);
        }

        // *** PASSO CRÍTICO 2: O BIND ***
        // O ViewHolder liga o clique ao listener
        public void bind(final String deviceName, final OnDeviceClickListener listener) {
            deviceNameTextView.setText(deviceName);
            itemView.setOnClickListener(v -> listener.onDeviceClick(deviceName));
        }
    }
}