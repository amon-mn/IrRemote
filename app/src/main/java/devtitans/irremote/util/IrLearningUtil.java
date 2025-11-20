package devtitans.irremote.util;

import android.hardware.ConsumerIrManager;
import java.lang.reflect.Method;

public class IrLearningUtil {

    /**
     * Chama o metodo oculto 'learnIrCode' (ou o nome definido pela equipe) no ConsumerIrManager via Reflection.
     * @param irManager A instância do serviço.
     * @return Um array de inteiros onde index 0 é a frequência e o resto é o padrão.
     * @throws Exception Se houver erro no reflection ou no hardware.
     */
    public static int[] learnIrCode(ConsumerIrManager irManager) throws Exception {
        // 1. Pega a classe do objeto
        Class<?> clazz = irManager.getClass();

        // 2. Procura o metodo oculto.
        // IMPORTANTE: Confirme com sua equipe se o nome é "learnIrCode" mesmo!
        Method method = clazz.getMethod("startLearning");

        // 3. Executa o metodo (Bloqueante)
        Object result = method.invoke(irManager);

        // 4. Cast para o tipo esperado
        return (int[]) result;
    }
}