package io.github.rollylindenshnizzer.nexuscore.client.config;

import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextRenderer;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

final class NexusConfigTextRenderer {
    private NexusConfigTextRenderer() {
    }

    static int draw(GuiGraphics graphics, Font font, NexusConfigText text, int x, int y, int color, boolean shadow) {
        if (text.textEffect() != null) {
            return NexusTextRenderer.render(graphics, font, text.component(), x, y, text.textEffect());
        }
        return graphics.drawString(font, text.component(), x, y, color, shadow);
    }

    static int drawCentered(GuiGraphics graphics, Font font, NexusConfigText text, int centerX, int y, int color, boolean shadow) {
        int x = centerX - font.width(text.component()) / 2;
        return draw(graphics, font, text, x, y, color, shadow);
    }

    static List<String> wrap(Font font, NexusConfigText text, int width) {
        return wrap(font, text.component().getString(), width);
    }

    static List<String> wrap(Font font, Component text, int width) {
        return wrap(font, text.getString(), width);
    }

    private static List<String> wrap(Font font, String text, int width) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (!line.isEmpty() && font.width(candidate) > width) {
                    lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (!line.isEmpty()) {
                        line.append(' ');
                    }
                    line.append(word);
                }
            }
            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    static int drawWrapped(GuiGraphics graphics, Font font, NexusConfigText text, int x, int y, int width, int color, int lineHeight) {
        int drawY = y;
        for (String line : wrap(font, text, width)) {
            NexusConfigText lineText = new NexusConfigText(Component.literal(line), text.textEffect());
            draw(graphics, font, lineText, x, drawY, color, false);
            drawY += lineHeight;
        }
        return drawY;
    }
}
