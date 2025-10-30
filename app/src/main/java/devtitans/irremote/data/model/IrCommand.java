// Conteúdo para data/model/IrCommand.java
package devtitans.irremote.data.model;

public class IrCommand {
    private String name;
    private int frequency;
    private int[] pattern;

    public IrCommand(String name, int frequency, int[] pattern) {
        this.name = name;
        this.frequency = frequency;
        this.pattern = pattern;
    }

    // Getters
    public String getName() { return name; }
    public int getFrequency() { return frequency; }
    public int[] getPattern() { return pattern; }
}
