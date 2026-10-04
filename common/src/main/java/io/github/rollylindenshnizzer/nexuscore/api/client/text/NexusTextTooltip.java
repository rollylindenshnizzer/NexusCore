package io.github.rollylindenshnizzer.nexuscore.api.client.text;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record NexusTextTooltip(String text, ResourceLocation effectId, int padding) implements TooltipComponent {
    public NexusTextTooltip(String text, ResourceLocation effectId) {
        this(text, effectId, 3);
    }
}
