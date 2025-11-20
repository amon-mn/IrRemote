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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import devtitans.irremote.R;
import devtitans.irremote.data.model.Device;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> implements Filterable {

    private List<Device> deviceListFiltered = new ArrayList<>();
    private List<Device> deviceListFull = new ArrayList<>();
    private final OnDeviceClickListener listener;

    // Interface para comunicar com a HomeActivity
    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);      // Clique simples (Abrir)
        void onDeviceLongClick(Device device);  // Clique longo (Editar/Excluir)
    }

    public DeviceAdapter(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    // Atualiza a lista quando o LiveData muda
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

    // --- LÓGICA DE FILTRO (SEARCH VIEW) ---
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

    // --- VIEWHOLDER ---
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceNameTextView;
        TextView creationDateTextView;
        ImageView deviceIconImageView;

        // Formatador de data para ficar bonito na tela (Ex: 19/11/2025)
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.textViewDeviceName);
            creationDateTextView = itemView.findViewById(R.id.textViewCreationDate);
            deviceIconImageView = itemView.findViewById(R.id.imageViewDeviceIcon);
        }

        public void bind(final Device device, final OnDeviceClickListener listener) {
            deviceNameTextView.setText(device.getName());

            // Formatação de data segura
            if (device.getCreationDate() > 0) {
                creationDateTextView.setText(dateFormat.format(new Date(device.getCreationDate())));
            } else {
                creationDateTextView.setText("");
            }

            // Ícone
            if (device.getIconResId() != 0) {
                deviceIconImageView.setImageResource(device.getIconResId());
            } else {
                deviceIconImageView.setImageResource(R.drawable.ic_launcher_foreground); // Ícone padrão
            }

            // 1. CLIQUE SIMPLES
            itemView.setOnClickListener(v -> listener.onDeviceClick(device));

            // 2. CLIQUE LONGO (CRÍTICO PARA EDITAR/DELETAR)
            itemView.setOnLongClickListener(v -> {
                listener.onDeviceLongClick(device);
                return true; // Retorna true para indicar que o evento foi consumido
            });
        }
    }
}