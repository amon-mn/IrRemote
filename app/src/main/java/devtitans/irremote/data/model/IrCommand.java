// Em: app/src/main/java/devtitans/irremote/data/model/IrCommand.java
package devtitans.irremote.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ir_commands_table")
public class IrCommand {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String deviceName;
    private String commandName;
    private String protocol;
    private String payload;

    // (NOVO) Campo de frequência, necessário para a lógica de edição TX
    private int frequency;

    public IrCommand(String deviceName, String commandName, String protocol, String payload) {
        this.deviceName = deviceName;
        this.commandName = commandName;
        this.protocol = protocol;
        this.payload = payload;
    }

    // --- Getters (Já os tem) ---
    public int getId() { return id; }
    public String getDeviceName() { return deviceName; }
    public String getCommandName() { return commandName; }
    public String getProtocol() { return protocol; }
    public String getPayload() { return payload; }
    public int getFrequency() { return frequency; } // Getter para o novo campo

    // --- (ADICIONE ESTES SETTERS) ---
    public void setId(int id) { this.id = id; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public void setCommandName(String commandName) { this.commandName = commandName; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public void setPayload(String payload) { this.payload = payload; }
    public void setFrequency(int frequency) { this.frequency = frequency; } // <-- O setter em falta

    // --- Métodos de Ajuda (Helpers) ---

    /**
     * Helper para o protocolo TX: Extrai a frequência do payload
     */
    public int getTxFrequency() {
        if (!"TX".equals(protocol) || payload == null) return 0;
        try {
            String[] parts = payload.split(" ", 2);
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Helper para o protocolo TX: Extrai o padrão de pulsos (int[]) do payload
     */
    public int[] getTxPatternAsArray() {
        if (!"TX".equals(protocol) || payload == null) return new int[0];
        try {
            String[] parts = payload.split(" ", 2);
            if (parts.length < 2) return new int[0];
            String patternStr = parts[1];

            String[] pulseParts = patternStr.split(",");
            int[] patternArray = new int[pulseParts.length];
            for (int i = 0; i < pulseParts.length; i++) {
                patternArray[i] = Integer.parseInt(pulseParts[i].trim());
            }
            return patternArray;
        } catch (Exception e) {
            return new int[0];
        }
    }

    /**
     * Helper para o protocolo NEC: Converte o payload (ex: "NEC 20DF10EF") para ASCII int[]
     */
    public int[] getPayloadAsAsciiIntArray() {
        if (payload == null) return new int[0];
        byte[] bytes = payload.getBytes();
        int[] patternArray = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            patternArray[i] = bytes[i];
        }
        return patternArray;
    }
}