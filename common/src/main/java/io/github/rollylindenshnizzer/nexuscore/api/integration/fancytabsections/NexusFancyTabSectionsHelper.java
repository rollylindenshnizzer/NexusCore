package io.github.rollylindenshnizzer.nexuscore.api.integration.fancytabsections;

import io.github.rollylindenshnizzer.nexuscore.api.creative.NexusFancyTabSections;
import net.minecraft.resources.ResourceLocation;

public final class NexusFancyTabSectionsHelper {
    private NexusFancyTabSectionsHelper() {
    }

    public static void addSection(String ownerModId, ResourceLocation tabId, NexusFancyTabSections.ColoredSection section) {
        NexusFancyTabSections.addSection(ownerModId, tabId, section);
    }
}
