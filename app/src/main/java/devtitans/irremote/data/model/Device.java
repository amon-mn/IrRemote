package devtitans.irremote.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices_table")
public class Device {

    @PrimaryKey
    @NonNull
    private String name;        // Usado como ID para linkar com IrCommand
    private String macAddress;
    private long creationDate;  // Alterado de Date para long (timestamp) para facilitar Room
    private int iconResId;

    public Device(@NonNull String name, String macAddress, long creationDate, int iconResId) {
        this.name = name;
        this.macAddress = macAddress;
        this.creationDate = creationDate;
        this.iconResId = iconResId;
    }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public long getCreationDate() { return creationDate; }
    public void setCreationDate(long creationDate) { this.creationDate = creationDate; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
}