package io.github.rollylindenshnizzer.nexuscore.api.client.glitch;

public record NexusGlitchBounds(int x, int y, int width, int height) {
    public NexusGlitchBounds {
        width = Math.max(0, width);
        height = Math.max(0, height);
    }
}
