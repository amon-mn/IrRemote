package devtitans.irremote.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import devtitans.irremote.data.model.Device;

@Dao
public interface DeviceDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Device device);

    @Delete
    void delete(Device device);

    @Query("SELECT * FROM devices_table ORDER BY name ASC")
    LiveData<List<Device>> getAllDevices();
}