package io.github.rollylindenshnizzer.nexuscore.neoforge.integration.irons;

import io.github.rollylindenshnizzer.nexuscore.api.creative.NexusFancyTabSections;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import io.github.rollylindenshnizzer.nexuscore.internal.integration.NexusIronsCreativeImplementation;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class NexusIronsCreativeNeoForge implements NexusIronsCreativeImplementation {
    public static final NexusIronsCreativeNeoForge INSTANCE = new NexusIronsCreativeNeoForge();

    private NexusIronsCreativeNeoForge() {
    }

    @Override
    public void addAllScrolls(String ownerModId, CreativeModeTab.Output output) {
        addScrolls(ownerModId, output, spellId -> true);
    }

    @Override
    public void addScrolls(String ownerModId, CreativeModeTab.Output output, Predicate<ResourceLocation> filter) {
        if (!NexusIntegrations.isLoaded("irons_spellbooks")) {
            return;
        }
        NexusIntegrations.featureUsed(ownerModId, "irons_scroll_creative_helper");
        createScrolls(filter).forEach(output::accept);
    }

    @Override
    public void addAllScrolls(String ownerModId, NexusFancyTabSections.ColoredSection section) {
        addScrolls(ownerModId, section, spellId -> true);
    }

    @Override
    public void addScrolls(String ownerModId, NexusFancyTabSections.ColoredSection section, Predicate<ResourceLocation> filter) {
        if (!NexusIntegrations.allLoaded(List.of("irons_spellbooks", "fancytabsections"))) {
            return;
        }
        NexusIntegrations.featureUsed(ownerModId, "irons_scroll_section_helper");
        section.addAll(() -> createScrolls(filter));
    }

    @Override
    public ItemStack createScroll(ResourceLocation spellId, int level) {
        AbstractSpell spell = SpellRegistry.REGISTRY.get(spellId);
        if (spell == null) {
            throw new IllegalArgumentException("Unknown Iron's Spells 'n Spellbooks spell: " + spellId);
        }
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, level, stack);
        return stack;
    }

    private static List<ItemStack> createScrolls(Predicate<ResourceLocation> filter) {
        List<ItemStack> stacks = new ArrayList<>();
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            ResourceLocation spellId = SpellRegistry.REGISTRY.getKey(spell);
            if (spellId == null || !filter.test(spellId)) {
                continue;
            }
            for (int level = 1; level <= spell.getMaxLevel(); level++) {
                stacks.add(INSTANCE.createScroll(spellId, level));
            }
        }
        return stacks;
    }
}
