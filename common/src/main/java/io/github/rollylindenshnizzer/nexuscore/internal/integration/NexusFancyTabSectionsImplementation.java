package io.github.rollylindenshnizzer.nexuscore.internal.integration;

import io.github.rollylindenshnizzer.nexuscore.api.creative.NexusFancyTabSections;
import net.minecraft.resources.ResourceLocation;

public interface NexusFancyTabSectionsImplementation {
    void addSection(String ownerModId, ResourceLocation tabId, NexusFancyTabSections.Definition section);
}
