package devtitans.irremote.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

import devtitans.irremote.data.dao.DeviceDAO;
import devtitans.irremote.data.database.IrCommandDatabase;
import devtitans.irremote.data.model.Device;

public class DeviceRepository {
    private final DeviceDAO mDeviceDAO;
    private final LiveData<List<Device>> mAllDevices;

    public DeviceRepository(Application application) {
        IrCommandDatabase db = IrCommandDatabase.getDatabase(application);
        mDeviceDAO = db.deviceDAO();
        mAllDevices = mDeviceDAO.getAllDevices();
    }

    public LiveData<List<Device>> getAllDevices() {
        return mAllDevices;
    }

    public void insert(Device device) {
        IrCommandDatabase.databaseWriteExecutor.execute(() -> {
            mDeviceDAO.insert(device);
        });
    }

    public void update(Device device) {
        IrCommandDatabase.databaseWriteExecutor.execute(() -> {
            mDeviceDAO.update(device);
        });
    }

    public void delete(Device device) {
        IrCommandDatabase.databaseWriteExecutor.execute(() -> {
            mDeviceDAO.delete(device);
        });
    }
}