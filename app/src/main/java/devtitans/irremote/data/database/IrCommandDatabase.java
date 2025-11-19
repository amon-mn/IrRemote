package devtitans.irremote.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import devtitans.irremote.data.dao.IrCommandDAO;
import devtitans.irremote.data.dao.DeviceDAO; // Importar novo DAO
import devtitans.irremote.data.model.IrCommand;
import devtitans.irremote.data.model.Device;    // Importar nova Entidade

// Adicione Device.class nas entities e atualize a version se já rodou o app antes (ou desinstale o app para resetar)
@Database(entities = {IrCommand.class, Device.class}, version = 2, exportSchema = false)
public abstract class IrCommandDatabase extends RoomDatabase {

    public abstract IrCommandDAO irCommandDAO();
    public abstract DeviceDAO deviceDAO(); // Novo método abstrato

    private static volatile IrCommandDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static IrCommandDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (IrCommandDatabase.class) {
                if (INSTANCE == null) {
                    // Dica: .fallbackToDestructiveMigration() apaga o banco se mudar a versão sem script de migração
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    IrCommandDatabase.class, "ir_command_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}