package io.github.rollylindenshnizzer.nexuscore.neoforge.integration;

import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import io.github.rollylindenshnizzer.nexuscore.internal.integration.NexusIntegrationImplementations;
import io.github.rollylindenshnizzer.nexuscore.neoforge.integration.fancytabsections.NexusFancyTabSectionsNeoForge;
import io.github.rollylindenshnizzer.nexuscore.neoforge.integration.irons.NexusIronsCreativeNeoForge;

public final class NexusNeoForgeIntegrations {
    private NexusNeoForgeIntegrations() {
    }

    public static void register() {
        NexusIntegrationImplementations.setFancyTabSections(NexusFancyTabSectionsNeoForge.INSTANCE);
        if (NexusIntegrations.isLoaded("irons_spellbooks")) {
            NexusIntegrationImplementations.setIronsCreative(NexusIronsCreativeNeoForge.INSTANCE);
        }
    }
}
