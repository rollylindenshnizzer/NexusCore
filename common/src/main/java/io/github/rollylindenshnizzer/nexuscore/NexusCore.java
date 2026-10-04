package io.github.rollylindenshnizzer.nexuscore;

import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NexusCore {
    public static final String MOD_ID = "nexuscore";
    public static final Logger LOGGER = LoggerFactory.getLogger("NexusCore");
    private static boolean initialized;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        NexusIntegrations.initialize();
        LOGGER.info("NexusCore loaded");
    }
}
