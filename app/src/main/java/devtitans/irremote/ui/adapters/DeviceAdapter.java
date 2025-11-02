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

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> implements Filterable { // <-- IMPLEMENTAR Filterable

    private List<String> deviceListFiltered; // Lista que é mostrada
    private final List<String> deviceListFull;   // Lista original (backup)
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(String deviceName);
    }

    // O construtor agora recebe a lista completa
    public DeviceAdapter(List<String> deviceList, OnDeviceClickListener listener) {
        this.deviceListFull = deviceList;
        // No início, a lista filtrada é igual à lista completa
        this.deviceListFiltered = new ArrayList<>(deviceListFull);
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
        // Usa a lista FILTRADA
        String deviceName = deviceListFiltered.get(position);
        holder.bind(deviceName, listener);
    }

    @Override
    public int getItemCount() {
        // Retorna o tamanho da lista FILTRADA
        return deviceListFiltered.size();
    }

    // --- LÓGICA DO FILTRO ---
    @Override
    public Filter getFilter() {
        return deviceFilter;
    }

    private Filter deviceFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<String> filteredList = new ArrayList<>();

            // Se a pesquisa estiver vazia, mostra a lista completa
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(deviceListFull);
            } else {
                // Converte a pesquisa para minúsculas
                String filterPattern = constraint.toString().toLowerCase().trim();

                // Itera sobre a lista COMPLETA
                for (String deviceName : deviceListFull) {
                    if (deviceName.toLowerCase().contains(filterPattern)) {
                        filteredList.add(deviceName); // Adiciona se corresponder
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
            notifyDataSetChanged(); // Atualiza o RecyclerView
        }
    };
    // --- FIM DA LÓGICA DO FILTRO ---

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceNameTextView;
        TextView creationDateTextView; // IDs do teu XML
        ImageView deviceIconImageView; // IDs do teu XML

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            // Certifica-te que estes IDs correspondem ao list_item_device.xml
            deviceNameTextView = itemView.findViewById(R.id.textViewDeviceName);
            creationDateTextView = itemView.findViewById(R.id.textViewCreationDate);
            deviceIconImageView = itemView.findViewById(R.id.imageViewDeviceIcon);
        }

        public void bind(final String deviceName, final OnDeviceClickListener listener) {
            deviceNameTextView.setText(deviceName);
            // (Aqui podes definir a data e o ícone se tiveres o modelo Device completo)
            // creationDateTextView.setText(...);
            itemView.setOnClickListener(v -> listener.onDeviceClick(deviceName));
        }
    }
}