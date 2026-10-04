package io.github.rollylindenshnizzer.nexuscore.client;

import dev.architectury.platform.Platform;
import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.NexusCoreConfig;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigs;
import io.github.rollylindenshnizzer.nexuscore.client.config.NexusConfigScreen;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;

public final class NexusClient {
    private static boolean initialized;

    private NexusClient() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        NexusCoreConfig.init();
        NexusConfigs.owners().forEach(owner -> Platform.getOptionalMod(owner).ifPresent(mod -> {
            mod.registerConfigurationScreen(parent -> new NexusConfigScreen(parent, owner));
            NexusIntegrations.featureUsed(owner, "config_editor");
        }));
        NexusCore.LOGGER.info("Initialized NexusCore client features");
    }
}
