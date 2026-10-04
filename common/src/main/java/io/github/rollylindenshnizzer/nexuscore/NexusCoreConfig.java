package io.github.rollylindenshnizzer.nexuscore;

import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextEffects;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigValue;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigs;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusJsonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class NexusCoreConfig {
    public static final NexusJsonConfig CLIENT;
    public static final NexusConfigValue<Boolean> NAMEPLATE_EFFECTS;
    public static final NexusConfigValue<String> NAMEPLATE_EFFECT;

    static {
        NexusJsonConfig.Builder builder = NexusJsonConfig.builder(NexusCore.MOD_ID, "client");
        builder.page("client", "config.nexuscore.page.client");
        builder.heading("nameplates", "config.nexuscore.heading.nameplates", "config.nexuscore.heading.nameplates.description");
        NAMEPLATE_EFFECTS = builder.booleanValue("nameplates.enabled", false).translation("config.nexuscore.option.nameplates.enabled", "config.nexuscore.option.nameplates.enabled.description").currentEffect(enabled -> Component.translatable(enabled ? "config.nexuscore.option.nameplates.enabled.effect.on" : "config.nexuscore.option.nameplates.enabled.effect.off"));
        NAMEPLATE_EFFECT = builder.stringValue("nameplates.effect", "nexuscore:arcane").translation("config.nexuscore.option.nameplates.effect", "config.nexuscore.option.nameplates.effect.description").dropdown(() -> NexusTextEffects.ids().stream().map(ResourceLocation::toString).toList()).optionTextEffect(ResourceLocation::tryParse).valueTextEffect(ResourceLocation::tryParse).currentEffect(effect -> Component.translatable("config.nexuscore.option.nameplates.effect.current_effect", effect));
        CLIENT = NexusConfigs.register(builder.build());
    }

    private NexusCoreConfig() {
    }

    public static void init() {
    }
}
