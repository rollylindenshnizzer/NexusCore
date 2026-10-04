package io.github.rollylindenshnizzer.nexuscore.api.client.text;

import net.minecraft.resources.ResourceLocation;

public record NexusTextEffect(ResourceLocation id, int startColor, int endColor, double waveAmplitude, double waveSpeed,
                              double wavePhase, double pulseStrength, double pulseSpeed, int jitterX, int jitterY,
                              long jitterPeriod, int ghostColor, int ghostOffsetX, int ghostOffsetY, boolean shadow,
                              ResourceLocation particleStyle, int particleCount, int particlePadding,
                              double glitchIntensity, int glitchPadding) {
    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private int startColor = 0xFFFFFF;
        private int endColor = 0xFFFFFF;
        private double waveAmplitude;
        private double waveSpeed = 300.0D;
        private double wavePhase = 0.7D;
        private double pulseStrength;
        private double pulseSpeed = 500.0D;
        private int jitterX;
        private int jitterY;
        private long jitterPeriod = 100L;
        private int ghostColor;
        private int ghostOffsetX;
        private int ghostOffsetY;
        private boolean shadow = true;
        private ResourceLocation particleStyle;
        private int particleCount;
        private int particlePadding = 2;
        private double glitchIntensity;
        private int glitchPadding = 2;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder gradient(int startColor, int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
            return this;
        }

        public Builder wave(double amplitude, double speed, double phase) {
            this.waveAmplitude = amplitude;
            this.waveSpeed = speed;
            this.wavePhase = phase;
            return this;
        }

        public Builder pulse(double strength, double speed) {
            this.pulseStrength = Math.max(0.0D, Math.min(1.0D, strength));
            this.pulseSpeed = Math.max(1.0D, speed);
            return this;
        }

        public Builder jitter(int x, int y, long period) {
            this.jitterX = Math.max(0, x);
            this.jitterY = Math.max(0, y);
            this.jitterPeriod = Math.max(1L, period);
            return this;
        }

        public Builder ghost(int color, int offsetX, int offsetY) {
            this.ghostColor = color;
            this.ghostOffsetX = offsetX;
            this.ghostOffsetY = offsetY;
            return this;
        }

        public Builder shadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }

        public Builder particles(ResourceLocation style, int count, int padding) {
            this.particleStyle = style;
            this.particleCount = Math.max(0, count);
            this.particlePadding = Math.max(0, padding);
            return this;
        }

        public Builder glitch(double intensity, int padding) {
            this.glitchIntensity = Math.max(0.0D, Math.min(1.0D, intensity));
            this.glitchPadding = Math.max(0, padding);
            return this;
        }

        public NexusTextEffect build() {
            return new NexusTextEffect(id, startColor, endColor, waveAmplitude, waveSpeed, wavePhase, pulseStrength, pulseSpeed, jitterX, jitterY, jitterPeriod, ghostColor, ghostOffsetX, ghostOffsetY, shadow, particleStyle, particleCount, particlePadding, glitchIntensity, glitchPadding);
        }
    }
}
