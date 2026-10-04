package io.github.rollylindenshnizzer.nexuscore.api.client.particle;

import net.minecraft.resources.ResourceLocation;

public record NexusGuiParticleStyle(ResourceLocation id, ResourceLocation particle, ResourceLocation texture, int size,
                                    int tint, float alpha, long frameTime) {
    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private ResourceLocation particle;
        private ResourceLocation texture;
        private int size = 3;
        private int tint = 0xFFFFFF;
        private float alpha = 1.0F;
        private long frameTime = 90L;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder particle(ResourceLocation particle) {
            this.particle = particle;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder size(int size) {
            this.size = Math.max(1, size);
            return this;
        }

        public Builder tint(int tint) {
            this.tint = tint & 0xFFFFFF;
            return this;
        }

        public Builder alpha(float alpha) {
            this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
            return this;
        }

        public Builder frameTime(long frameTime) {
            this.frameTime = Math.max(1L, frameTime);
            return this;
        }

        public NexusGuiParticleStyle build() {
            return new NexusGuiParticleStyle(id, particle, texture, size, tint, alpha, frameTime);
        }
    }
}
