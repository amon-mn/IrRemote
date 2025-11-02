package devtitans.irremote.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import devtitans.irremote.data.model.IrCommand;

@Dao
public interface IrCommandDAO {

    @Insert
    void insert(IrCommand command);

    @Delete
    void delete(IrCommand command);

    @Update
    void update(IrCommand command);

    // Query crucial: Busca todos os comandos PARA UM DISPOSITIVO ESPECÍFICO
    @Query("SELECT * FROM ir_commands_table WHERE deviceName = :deviceName ORDER BY commandName ASC")
    LiveData<List<IrCommand>> getCommandsByDevice(String deviceName);
}