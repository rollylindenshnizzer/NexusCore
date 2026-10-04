package io.github.rollylindenshnizzer.nexuscore.neoforge.creative;

import io.github.rollylindenshnizzer.nexuscore.api.creative.NexusFancyTabSections;
import io.github.rollylindenshnizzer.nexuscore.internal.integration.NexusFancyTabSectionsImplementation;
import io.github.rollylindenshnizzer.nexuscore.internal.integration.NexusIntegrationImplementations;
import net.mcexpanded.fancytabsections.FancyTabSections;
import net.mcexpanded.fancytabsections.Section.SectionColored;
import net.minecraft.resources.ResourceLocation;

public final class NexusFancyTabSectionsNeoForge implements NexusFancyTabSectionsImplementation {
    private static final NexusFancyTabSectionsNeoForge INSTANCE = new NexusFancyTabSectionsNeoForge();

    private NexusFancyTabSectionsNeoForge() {
    }

    public static void register() {
        NexusIntegrationImplementations.setFancyTabSections(INSTANCE);
    }

    @Override
    public void addSection(String ownerModId, ResourceLocation tabId, NexusFancyTabSections.Definition definition) {
        SectionColored section = new SectionColored(definition.id()).setTitle(definition.title()).setRenderTitle(definition.renderTitle()).setTitleOffset(definition.titleOffsetX(), definition.titleOffsetY()).setCentered(definition.centered()).setTextColor(definition.textColor()).setTextOutline(definition.textOutline()).setTextShadow(definition.textShadow()).setCollapsible(definition.collapsible()).setSticky(definition.sticky()).setBannerColor(definition.bannerColor()).setBannerBorderColor(definition.bannerBorderColor()).setVerticalSize(definition.verticalSize()).setHorizontalSize(definition.horizontalSize()).setOffsetX(definition.offsetX()).setOffsetY(definition.offsetY());
        if (definition.displayItem() != null) {
            section.setDisplayItem(definition.displayItem());
        }
        for (NexusFancyTabSections.Entry entry : definition.entries()) {
            if (entry instanceof NexusFancyTabSections.ItemEntry itemEntry) {
                section.add(itemEntry.item());
            } else if (entry instanceof NexusFancyTabSections.StackEntry stackEntry) {
                section.add(stackEntry.stack());
            } else if (entry instanceof NexusFancyTabSections.ItemLikeEntry itemLikeEntry) {
                section.add(itemLikeEntry.item());
            } else if (entry instanceof NexusFancyTabSections.SupplierEntry supplierEntry) {
                section.add(supplierEntry.supplier());
            } else if (entry instanceof NexusFancyTabSections.ListEntry listEntry) {
                section.add(listEntry.stacks());
            } else if (entry instanceof NexusFancyTabSections.ListSupplierEntry listSupplierEntry) {
                section.add(registryAccess -> listSupplierEntry.supplier().get());
            } else if (entry instanceof NexusFancyTabSections.TagEntry tagEntry) {
                section.addItemTag(tagEntry.tag());
            }
        }
        FancyTabSections.addSection(tabId, section);
    }
}