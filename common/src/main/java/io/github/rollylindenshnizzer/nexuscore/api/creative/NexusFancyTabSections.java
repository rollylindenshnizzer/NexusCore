package io.github.rollylindenshnizzer.nexuscore.api.creative;

import io.github.rollylindenshnizzer.nexuscore.internal.integration.NexusFancyTabSectionsImplementation;
import io.github.rollylindenshnizzer.nexuscore.internal.integration.NexusIntegrationImplementations;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NexusFancyTabSections {
    private NexusFancyTabSections() {
    }

    public static ColoredSection colored(ResourceLocation id) {
        return new ColoredSection(id);
    }

    public static void addSection(String ownerModId, ResourceLocation tabId, ColoredSection section) {
        NexusFancyTabSectionsImplementation implementation = NexusIntegrationImplementations.fancyTabSections();
        if (implementation != null) {
            implementation.addSection(ownerModId, tabId, section.definition());
        }
    }

    public static final class ColoredSection {
        private final ResourceLocation id;
        private Component title;
        private boolean renderTitle = true;
        private int titleOffsetX = 5;
        private int titleOffsetY = 5;
        private int textColor = -1;
        private int textOutline;
        private boolean textShadow = true;
        private boolean centered;
        private boolean collapsible = true;
        private boolean sticky = true;
        private Function<RegistryAccess, ItemStack> displayItem;
        private int bannerColor = 0xFF45BB77;
        private int bannerBorderColor = brighten(bannerColor, 0.2F);
        private int verticalSize = 162;
        private int horizontalSize = 18;
        private int offsetX;
        private int offsetY;
        private final List<Entry> entries = new ArrayList<>();

        private ColoredSection(ResourceLocation id) {
            this.id = id;
            this.title = Component.translatable("section." + id.getNamespace() + "." + id.getPath());
        }

        public ColoredSection setTitle(Component title) {
            this.title = title;
            return this;
        }

        public ColoredSection setTitle(String title) {
            this.title = Component.literal(title);
            return this;
        }

        public ColoredSection setRenderTitle(boolean renderTitle) {
            this.renderTitle = renderTitle;
            return this;
        }

        public ColoredSection setDisplayItem(Function<RegistryAccess, ItemStack> displayItem) {
            this.displayItem = displayItem;
            return this;
        }

        public ColoredSection setTitleOffset(int x, int y) {
            this.titleOffsetX = x;
            this.titleOffsetY = y;
            return this;
        }

        public ColoredSection setCentered(boolean centered) {
            this.centered = centered;
            return this;
        }

        public ColoredSection setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public ColoredSection setTextOutline(int textOutline) {
            this.textOutline = textOutline;
            return this;
        }

        public ColoredSection setTextShadow(boolean textShadow) {
            this.textShadow = textShadow;
            return this;
        }

        public ColoredSection setCollapsible(boolean collapsible) {
            this.collapsible = collapsible;
            return this;
        }

        public ColoredSection setSticky(boolean sticky) {
            this.sticky = sticky;
            return this;
        }

        public ColoredSection setBannerColor(int bannerColor) {
            this.bannerColor = bannerColor;
            this.bannerBorderColor = brighten(bannerColor, 0.2F);
            return this;
        }

        public ColoredSection setBannerBorderColor(int bannerBorderColor) {
            this.bannerBorderColor = bannerBorderColor;
            return this;
        }

        public ColoredSection setVerticalSize(int verticalSize) {
            this.verticalSize = verticalSize;
            return this;
        }

        public ColoredSection setHorizontalSize(int horizontalSize) {
            this.horizontalSize = horizontalSize;
            return this;
        }

        public ColoredSection setOffsetX(int offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        public ColoredSection setOffsetY(int offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        public ColoredSection setTextureInsideRow() {
            this.horizontalSize = 160;
            this.verticalSize = 16;
            this.offsetX = 1;
            this.offsetY = 1;
            return this;
        }

        public ColoredSection add(Item item) {
            entries.add(new ItemEntry(item));
            return this;
        }

        public ColoredSection add(ItemStack stack) {
            entries.add(new StackEntry(stack));
            return this;
        }

        public ColoredSection add(ItemLike item) {
            entries.add(new ItemLikeEntry(item));
            return this;
        }

        public ColoredSection add(Supplier<ItemStack> supplier) {
            entries.add(new SupplierEntry(supplier));
            return this;
        }

        public ColoredSection add(List<ItemStack> stacks) {
            entries.add(new ListEntry(List.copyOf(stacks)));
            return this;
        }

        public ColoredSection addAll(Supplier<List<ItemStack>> supplier) {
            entries.add(new ListSupplierEntry(supplier));
            return this;
        }

        public ColoredSection addItemTag(TagKey<Item> tag) {
            entries.add(new TagEntry(tag));
            return this;
        }

        public ResourceLocation id() {
            return id;
        }

        private Definition definition() {
            return new Definition(id, title, renderTitle, titleOffsetX, titleOffsetY, textColor, textOutline, textShadow, centered, collapsible, sticky, displayItem, bannerColor, bannerBorderColor, verticalSize, horizontalSize, offsetX, offsetY, List.copyOf(entries));
        }
    }

    public record Definition(ResourceLocation id, Component title, boolean renderTitle, int titleOffsetX,
                             int titleOffsetY, int textColor, int textOutline, boolean textShadow, boolean centered,
                             boolean collapsible, boolean sticky, Function<RegistryAccess, ItemStack> displayItem,
                             int bannerColor, int bannerBorderColor, int verticalSize, int horizontalSize, int offsetX,
                             int offsetY, List<Entry> entries) {
    }

    private static int brighten(int color, float amount) {
        int alpha = color >>> 24 & 255;
        int red = color >>> 16 & 255;
        int green = color >>> 8 & 255;
        int blue = color & 255;
        red = (int) (red + (255 - red) * amount);
        green = (int) (green + (255 - green) * amount);
        blue = (int) (blue + (255 - blue) * amount);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public sealed interface Entry permits ItemEntry, StackEntry, ItemLikeEntry, SupplierEntry, ListEntry, ListSupplierEntry, TagEntry {
    }

    public record ItemEntry(Item item) implements Entry {
    }

    public record StackEntry(ItemStack stack) implements Entry {
    }

    public record ItemLikeEntry(ItemLike item) implements Entry {
    }

    public record SupplierEntry(Supplier<ItemStack> supplier) implements Entry {
    }

    public record ListEntry(List<ItemStack> stacks) implements Entry {
    }

    public record ListSupplierEntry(Supplier<List<ItemStack>> supplier) implements Entry {
    }

    public record TagEntry(TagKey<Item> tag) implements Entry {
    }
}
