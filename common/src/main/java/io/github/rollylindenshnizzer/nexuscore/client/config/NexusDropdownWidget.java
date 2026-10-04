package io.github.rollylindenshnizzer.nexuscore.client.config;

import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigText;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

final class NexusDropdownWidget extends AbstractWidget {
    private static final int OPTION_HEIGHT = 20;
    private static final int MAX_VISIBLE_OPTIONS = 8;
    private final Font font;
    private final NexusConfigValue<?> value;
    private boolean open;
    private int firstVisible;

    NexusDropdownWidget(Font font, int x, int y, int width, NexusConfigValue<?> value) {
        super(x, y, width, OPTION_HEIGHT, value.label());
        this.font = font;
        this.value = value;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!visible) {
            return false;
        }
        int height = open ? OPTION_HEIGHT * (visibleOptionCount() + 1) : OPTION_HEIGHT;
        return mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + height;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int relativeY = (int) mouseY - getY();
        if (relativeY < OPTION_HEIGHT) {
            open = !open;
            if (open) {
                revealCurrentValue();
            }
            return;
        }
        if (!open) {
            return;
        }
        List<String> options = value.dropdownValues();
        int index = firstVisible + relativeY / OPTION_HEIGHT - 1;
        if (index >= 0 && index < options.size()) {
            value.setFromString(options.get(index));
            open = false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!open || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        List<String> options = value.dropdownValues();
        int maximum = Math.max(0, options.size() - MAX_VISIBLE_OPTIONS);
        firstVisible = Math.max(0, Math.min(maximum, firstVisible - (int) Math.signum(verticalAmount)));
        return true;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int background = isHoveredOrFocused() ? 0xFF6A6A6A : 0xFF4A4A4A;
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + OPTION_HEIGHT, background);
        graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + OPTION_HEIGHT - 1, 0xFF202020);
        NexusConfigText selected = value.currentValueText();
        NexusConfigTextRenderer.draw(graphics, font, selected, getX() + 5, getY() + 6, 0xE0E0E0, false);
        graphics.drawString(font, Component.literal(open ? "▲" : "▼"), getX() + getWidth() - 13, getY() + 6, 0xA0A0A0, false);
        if (!open) {
            return;
        }
        List<String> options = value.dropdownValues();
        int visible = Math.min(MAX_VISIBLE_OPTIONS, Math.max(0, options.size() - firstVisible));
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        for (int i = 0; i < visible; i++) {
            int optionIndex = firstVisible + i;
            int optionY = getY() + OPTION_HEIGHT * (i + 1);
            boolean hovered = mouseX >= getX() && mouseX < getX() + getWidth() && mouseY >= optionY && mouseY < optionY + OPTION_HEIGHT;
            graphics.fill(getX(), optionY, getX() + getWidth(), optionY + OPTION_HEIGHT, hovered ? 0xFF5A5A5A : 0xFF303030);
            NexusConfigText option = value.optionLabelText(options.get(optionIndex));
            NexusConfigTextRenderer.draw(graphics, font, option, getX() + 5, optionY + 6, 0xE0E0E0, false);
        }
        if (options.size() > MAX_VISIBLE_OPTIONS) {
            int trackX = getX() + getWidth() - 3;
            int trackTop = getY() + OPTION_HEIGHT;
            int trackHeight = OPTION_HEIGHT * visible;
            graphics.fill(trackX, trackTop, trackX + 2, trackTop + trackHeight, 0xFF202020);
            int thumbHeight = Math.max(8, trackHeight * MAX_VISIBLE_OPTIONS / options.size());
            int maximum = Math.max(1, options.size() - MAX_VISIBLE_OPTIONS);
            int thumbY = trackTop + (trackHeight - thumbHeight) * firstVisible / maximum;
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, 0xFF9A9A9A);
        }
        graphics.pose().popPose();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
        output.add(NarratedElementType.USAGE, value.currentValueText().component());
    }

    private int visibleOptionCount() {
        return Math.min(MAX_VISIBLE_OPTIONS, Math.max(0, value.dropdownValues().size() - firstVisible));
    }

    private void revealCurrentValue() {
        List<String> options = value.dropdownValues();
        int selected = options.indexOf(value.displayValue());
        if (selected < 0) {
            firstVisible = 0;
            return;
        }
        if (selected < firstVisible) {
            firstVisible = selected;
        } else if (selected >= firstVisible + MAX_VISIBLE_OPTIONS) {
            firstVisible = selected - MAX_VISIBLE_OPTIONS + 1;
        }
    }
}
