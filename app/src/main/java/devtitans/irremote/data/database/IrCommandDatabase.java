// Em: app/src/main/java/devtitans/irremote/data/IrCommandDatabase.java

package devtitans.irremote.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import devtitans.irremote.data.dao.IrCommandDAO;
import devtitans.irremote.data.model.IrCommand;

@Database(entities = {IrCommand.class}, version = 1, exportSchema = false)
public abstract class IrCommandDatabase extends RoomDatabase {

    public abstract IrCommandDAO irCommandDAO();

    private static volatile IrCommandDatabase INSTANCE;

    // Executor para correr as queries fora da Main Thread
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static IrCommandDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (IrCommandDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    IrCommandDatabase.class, "ir_command_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}