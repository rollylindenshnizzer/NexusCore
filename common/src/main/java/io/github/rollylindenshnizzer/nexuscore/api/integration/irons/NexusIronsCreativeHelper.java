package io.github.rollylindenshnizzer.nexuscore.api.integration.irons;

import io.github.rollylindenshnizzer.nexuscore.api.creative.NexusFancyTabSections;
import io.github.rollylindenshnizzer.nexuscore.internal.integration.NexusIntegrationImplementations;
import io.github.rollylindenshnizzer.nexuscore.internal.integration.NexusIronsCreativeImplementation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public final class NexusIronsCreativeHelper {
    private NexusIronsCreativeHelper() {
    }

    public static void addAllScrolls(String ownerModId, CreativeModeTab.Output output) {
        NexusIronsCreativeImplementation implementation = NexusIntegrationImplementations.ironsCreative();
        if (implementation != null) {
            implementation.addAllScrolls(ownerModId, output);
        }
    }

    public static void addScrolls(String ownerModId, CreativeModeTab.Output output, Predicate<ResourceLocation> filter) {
        NexusIronsCreativeImplementation implementation = NexusIntegrationImplementations.ironsCreative();
        if (implementation != null) {
            implementation.addScrolls(ownerModId, output, filter);
        }
    }

    public static void addAllScrolls(String ownerModId, NexusFancyTabSections.ColoredSection section) {
        NexusIronsCreativeImplementation implementation = NexusIntegrationImplementations.ironsCreative();
        if (implementation != null) {
            implementation.addAllScrolls(ownerModId, section);
        }
    }

    public static void addScrolls(String ownerModId, NexusFancyTabSections.ColoredSection section, Predicate<ResourceLocation> filter) {
        NexusIronsCreativeImplementation implementation = NexusIntegrationImplementations.ironsCreative();
        if (implementation != null) {
            implementation.addScrolls(ownerModId, section, filter);
        }
    }

    public static ItemStack createScroll(ResourceLocation spellId, int level) {
        NexusIronsCreativeImplementation implementation = NexusIntegrationImplementations.ironsCreative();
        if (implementation == null) {
            throw new IllegalStateException("Iron's Spells 'n Spellbooks integration is not available");
        }
        return implementation.createScroll(spellId, level);
    }
}
