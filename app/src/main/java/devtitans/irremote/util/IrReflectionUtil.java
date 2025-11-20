package devtitans.irremote.util;

import android.hardware.ConsumerIrManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;

public class IrReflectionUtil {

    /**
     * Inicia o aprendizado via Reflection + Dynamic Proxy.
     */
    public static Object startLearning(ConsumerIrManager irManager, Executor executor, IrLearningListener listener) {
        try {
            Class<?> callbackInterface = Class.forName("android.hardware.ConsumerIrManager$LearnCallback");

            Object proxyInstance = Proxy.newProxyInstance(
                    callbackInterface.getClassLoader(),
                    new Class<?>[]{callbackInterface},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            String methodName = method.getName();
                            Handler uiHandler = new Handler(Looper.getMainLooper());

                            if ("onLearned".equals(methodName)) {
                                try {
                                    int freq = (int) args[0];
                                    int[] pattern = (int[]) args[1];
                                    long timestamp = (long) args[2];

                                    if (pattern == null) pattern = new int[0];

                                    // Usa a classe IrSignal que agora é externa
                                    IrSignal signal = new IrSignal(freq, pattern, timestamp);
                                    uiHandler.post(() -> listener.onLearned(signal));
                                } catch (Exception e) {
                                    uiHandler.post(() -> listener.onError(IrLearningListener.ERROR_UNKNOWN, "Erro ao processar dados: " + e.getMessage()));
                                }
                            }
                            else if ("onError".equals(methodName)) {
                                int code = (int) args[0];
                                String msg = (String) args[1];
                                if (msg == null) msg = "Erro desconhecido";

                                final String finalMsg = msg;
                                uiHandler.post(() -> listener.onError(code, finalMsg));
                            }
                            return null;
                        }
                    }
            );

            Method startMethod = irManager.getClass().getMethod("startLearning", Executor.class, callbackInterface);
            startMethod.invoke(irManager, executor, proxyInstance);

            return proxyInstance;

        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(IrLearningListener.ERROR_UNKNOWN, "Reflection falhou: " + e.getMessage());
            return null;
        }
    }

    public static void stopLearning(ConsumerIrManager irManager) {
        try {
            Method stopMethod = irManager.getClass().getMethod("stopLearning");
            stopMethod.invoke(irManager);
        } catch (Exception e) {
            Log.e("IrReflection", "Erro ao parar: " + e.getMessage());
        }
    }
}