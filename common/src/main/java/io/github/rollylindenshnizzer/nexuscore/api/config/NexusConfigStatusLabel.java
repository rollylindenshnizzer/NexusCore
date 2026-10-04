package io.github.rollylindenshnizzer.nexuscore.api.config;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record NexusConfigStatusLabel(String id, NexusConfigText text, int color) {
    public static final NexusConfigStatusLabel ALPHA = new NexusConfigStatusLabel("alpha", NexusConfigText.translatable("config.nexuscore.label.alpha"), 0xFFD9822B);
    public static final NexusConfigStatusLabel BETA = new NexusConfigStatusLabel("beta", NexusConfigText.translatable("config.nexuscore.label.beta"), 0xFF3C8DDE);
    public static final NexusConfigStatusLabel MARKED_FOR_REMOVAL = new NexusConfigStatusLabel("marked_for_removal", NexusConfigText.translatable("config.nexuscore.label.marked_for_removal"), 0xFFC95A45);
    public static final NexusConfigStatusLabel DEPRECATED = new NexusConfigStatusLabel("deprecated", NexusConfigText.translatable("config.nexuscore.label.deprecated"), 0xFF8A6A9E);

    public NexusConfigStatusLabel {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(text, "text");
    }

    public static NexusConfigStatusLabel of(String id, NexusConfigText text, int color) {
        return new NexusConfigStatusLabel(id, text, color);
    }

    public NexusConfigStatusLabel withTextEffect(ResourceLocation effectId) {
        return new NexusConfigStatusLabel(id, text.withTextEffect(effectId), color);
    }
}
