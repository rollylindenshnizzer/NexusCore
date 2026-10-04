package io.github.rollylindenshnizzer.nexuscore.api.client.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public final class NexusTextClientTooltip implements ClientTooltipComponent {
    private final NexusTextTooltip tooltip;

    public NexusTextClientTooltip(NexusTextTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return 13;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(tooltip.text()) + tooltip.padding() * 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        NexusTextRenderer.render(graphics, font, tooltip.text(), x + tooltip.padding(), y + 2, tooltip.effectId());
    }
}
