package io.github.rollylindenshnizzer.nexuscore.internal.integration;

import io.github.rollylindenshnizzer.nexuscore.api.creative.NexusFancyTabSections;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public interface NexusIronsCreativeImplementation {
    void addAllScrolls(String ownerModId, CreativeModeTab.Output output);

    void addScrolls(String ownerModId, CreativeModeTab.Output output, Predicate<ResourceLocation> filter);

    void addAllScrolls(String ownerModId, NexusFancyTabSections.ColoredSection section);

    void addScrolls(String ownerModId, NexusFancyTabSections.ColoredSection section, Predicate<ResourceLocation> filter);

    ItemStack createScroll(ResourceLocation spellId, int level);
}
