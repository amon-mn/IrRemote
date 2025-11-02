package devtitans.irremote.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ir_commands_table")
public class IrCommand {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String deviceName;
    private String commandName;
    private int frequency;
    private String pattern; // O padrão (ex: "9000,4500,...")

    // Construtor
    public IrCommand(String deviceName, String commandName, int frequency, String pattern) {
        this.deviceName = deviceName;
        this.commandName = commandName;
        this.frequency = frequency;
        this.pattern = pattern;
    }

    // --- Getters e Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getCommandName() { return commandName; }
    public void setCommandName(String commandName) { this.commandName = commandName; }

    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    @Override
    public String toString() {
        return this.commandName;
    }

    // --- Metodo de Ajuda ---

    /**
     * Converte o padrão de String ("9000,4500,...") para o int[]
     * que o ConsumerIrManager espera.
     */
    public int[] getPatternAsArray() {
        if (pattern == null || pattern.isEmpty()) {
            return new int[0];
        }
        String[] parts = pattern.split(",");
        int[] patternArray = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                patternArray[i] = Integer.parseInt(parts[i].trim());
            }
        } catch (Exception e) {
            return new int[0]; // Retorna vazio em caso de erro de parsing
        }
        return patternArray;
    }
}