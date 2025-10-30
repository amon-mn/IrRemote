package devtitans.irremote.data.model;

import java.util.Date;

public class Device {
    private String name;
    private String macAddress;
    private Date creationDate;
    private int iconResId;

    public Device(String name, String macAddress, Date creationDate, int iconResId) {
        this.name = name;
        this.macAddress = macAddress;
        this.creationDate = creationDate;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public int getIconResId() {
        return iconResId;
    }
}
