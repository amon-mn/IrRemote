package devtitans.irremote.util;

import androidx.annotation.NonNull;

public class IrSignal {
    public final int frequency;
    @NonNull public final int[] pattern;
    public final long timestamp;

    public IrSignal(int frequency, @NonNull int[] pattern, long timestamp) {
        this.frequency = frequency;
        this.pattern = pattern;
        this.timestamp = timestamp;
    }

    // Helper para converter para String CSV
    public String getPatternString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pattern.length; i++) {
            sb.append(pattern[i]);
            if (i < pattern.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}