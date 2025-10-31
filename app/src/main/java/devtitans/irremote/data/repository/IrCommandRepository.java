// Em: app/src/main/java/devtitans/irremote/data/IrCommandRepository.java

package devtitans.irremote.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

import devtitans.irremote.data.dao.IrCommandDAO;
import devtitans.irremote.data.database.IrCommandDatabase;
import devtitans.irremote.data.model.IrCommand;

public class IrCommandRepository {
    private final IrCommandDAO mIrCommandDAO;

    public IrCommandRepository(Application application) {
        IrCommandDatabase db = IrCommandDatabase.getDatabase(application);
        mIrCommandDAO = db.irCommandDAO();
    }

    // Wrapper para a query. O Room já trata o LiveData em background.
    public LiveData<List<IrCommand>> getCommandsByDevice(String deviceName) {
        return mIrCommandDAO.getCommandsByDevice(deviceName);
    }

    // Wrapper para inserir (usa o Executor do Database)
    public void insert(IrCommand command) {
        IrCommandDatabase.databaseWriteExecutor.execute(() -> {
            mIrCommandDAO.insert(command);
        });
    }

    // Wrapper para deletar (usa o Executor do Database)
    public void delete(IrCommand command) {
        IrCommandDatabase.databaseWriteExecutor.execute(() -> {
            mIrCommandDAO.delete(command);
        });
    }

    public void update(IrCommand command) {
        IrCommandDatabase.databaseWriteExecutor.execute(() -> {
            mIrCommandDAO.update(command);
        });
    }
}