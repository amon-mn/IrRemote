package devtitans.irremote.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import devtitans.irremote.R;
import devtitans.irremote.data.model.Device; // Importar Modelo

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> implements Filterable {

    private List<Device> deviceListFiltered = new ArrayList<>(); // Alterado para Device
    private List<Device> deviceListFull = new ArrayList<>();     // Alterado para Device
    private final OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device); // Passa o objeto completo agora
    }

    public DeviceAdapter(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    // Método auxiliar para atualizar a lista via LiveData
    public void setDevices(List<Device> devices) {
        this.deviceListFull = devices;
        this.deviceListFiltered = new ArrayList<>(devices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceListFiltered.get(position);
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return deviceListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return deviceFilter;
    }

    private final Filter deviceFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Device> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(deviceListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Device device : deviceListFull) {
                    if (device.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(device);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            deviceListFiltered.clear();
            deviceListFiltered.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceNameTextView;
        TextView creationDateTextView;
        ImageView deviceIconImageView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.textViewDeviceName);
            creationDateTextView = itemView.findViewById(R.id.textViewCreationDate);
            deviceIconImageView = itemView.findViewById(R.id.imageViewDeviceIcon);
        }

        public void bind(final Device device, final OnDeviceClickListener listener) {
            deviceNameTextView.setText(device.getName());
            // Exemplo simples de data:
            creationDateTextView.setText(new java.util.Date(device.getCreationDate()).toString());

            // Define ícone se válido, senão usa padrão
            if (device.getIconResId() != 0) {
                deviceIconImageView.setImageResource(device.getIconResId());
            } else {
                deviceIconImageView.setImageResource(R.drawable.ic_launcher_foreground);
            }

            itemView.setOnClickListener(v -> listener.onDeviceClick(device));
        }
    }
}