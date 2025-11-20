package devtitans.irremote.util;

import androidx.annotation.NonNull;

public interface IrLearningListener {

    // Constantes de Erro (Movidas para cá para ficarem junto do contexto)
    int ERROR_UNKNOWN = 0;
    int ERROR_TIMEOUT = 1;
    int ERROR_HARDWARE = 2;

    void onLearned(@NonNull IrSignal signal);

    void onError(int errorCode, @NonNull String message);
}