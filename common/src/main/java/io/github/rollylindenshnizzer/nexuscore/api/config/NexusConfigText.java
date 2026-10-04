package io.github.rollylindenshnizzer.nexuscore.api.config;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record NexusConfigText(Component component, ResourceLocation textEffect) {
    public NexusConfigText {
        Objects.requireNonNull(component, "component");
    }

    public static NexusConfigText empty() {
        return new NexusConfigText(Component.empty(), null);
    }

    public static NexusConfigText literal(String text) {
        return new NexusConfigText(Component.literal(text), null);
    }

    public static NexusConfigText translatable(String translationKey, Object... arguments) {
        return new NexusConfigText(Component.translatable(translationKey, arguments), null);
    }

    public static NexusConfigText of(Component component) {
        return new NexusConfigText(component, null);
    }

    public NexusConfigText withTextEffect(ResourceLocation effectId) {
        return new NexusConfigText(component, effectId);
    }

    public boolean isEmpty() {
        return component.getString().isEmpty();
    }
}
