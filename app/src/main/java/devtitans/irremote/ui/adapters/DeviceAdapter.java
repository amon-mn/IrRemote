package devtitans.irremote.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import devtitans.irremote.R;
import devtitans.irremote.data.model.Device;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private final List<Device> deviceList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Device device);
    }

    public DeviceAdapter(List<Device> deviceList, OnItemClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_device, parent, false);
        return new DeviceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device currentDevice = deviceList.get(position);
        holder.bind(currentDevice, listener);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewCreationDate;
        private final ImageView imageViewDeviceIcon;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewDeviceName);
            textViewCreationDate = itemView.findViewById(R.id.textViewCreationDate);
            imageViewDeviceIcon = itemView.findViewById(R.id.imageViewDeviceIcon);
        }

        public void bind(final Device device, final OnItemClickListener listener) {
            textViewName.setText(device.getName());
            imageViewDeviceIcon.setImageResource(device.getIconResId());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            textViewCreationDate.setText(sdf.format(device.getCreationDate()));

            itemView.setOnClickListener(v -> listener.onItemClick(device));
        }
    }
}
