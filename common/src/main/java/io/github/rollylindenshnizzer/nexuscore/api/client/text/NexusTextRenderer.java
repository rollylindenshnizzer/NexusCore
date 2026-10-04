package io.github.rollylindenshnizzer.nexuscore.api.client.text;

import io.github.rollylindenshnizzer.nexuscore.api.client.glitch.NexusFramebufferGlitches;
import io.github.rollylindenshnizzer.nexuscore.api.client.glitch.NexusGlitchBounds;
import io.github.rollylindenshnizzer.nexuscore.api.client.particle.NexusGuiParticles;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class NexusTextRenderer {
    private static final ThreadLocal<Integer> RENDER_DEPTH = ThreadLocal.withInitial(() -> 0);

    private NexusTextRenderer() {
    }

    public static boolean isRendering() {
        return RENDER_DEPTH.get() > 0;
    }

    public static int render(GuiGraphics graphics, Font font, Component text, int x, int y, ResourceLocation effectId) {
        return render(graphics, font, text.getString(), x, y, effectId);
    }

    public static int render(GuiGraphics graphics, Font font, String text, int x, int y, ResourceLocation effectId) {
        NexusTextEffect effect = NexusTextEffects.effect(effectId).orElse(null);
        if (effect == null) {
            return graphics.drawString(font, text, x, y, 0xFFFFFF, true);
        }
        NexusIntegrations.featureUsed(effectId.getNamespace(), "text_effects");
        return render(graphics, font, text, x, y, effect);
    }

    public static int render(GuiGraphics graphics, Font font, String text, int x, int y, NexusTextEffect effect) {
        RENDER_DEPTH.set(RENDER_DEPTH.get() + 1);
        try {
            long time = Util.getMillis();
            int cursor = x;
            int denominator = Math.max(1, text.length() - 1);
            for (int i = 0; i < text.length(); i++) {
                String character = String.valueOf(text.charAt(i));
                int width = font.width(character);
                double wave = effect.waveAmplitude() == 0.0D ? 0.0D : Math.sin(time / Math.max(1.0D, effect.waveSpeed()) + i * effect.wavePhase()) * effect.waveAmplitude();
                int jitterX = jitter(time, i, effect.jitterPeriod(), effect.jitterX(), 17);
                int jitterY = jitter(time, i, effect.jitterPeriod(), effect.jitterY(), 31);
                int drawX = cursor + jitterX;
                int drawY = y + (int) Math.round(wave) + jitterY;
                float blend = i / (float) denominator;
                int rgb = lerp(effect.startColor(), effect.endColor(), blend);
                if (effect.pulseStrength() > 0.0D) {
                    float pulse = (float) (((Math.sin(time / effect.pulseSpeed()) + 1.0D) * 0.5D) * effect.pulseStrength());
                    rgb = lerp(rgb, 0xFFFFFF, pulse);
                }
                int color = 0xFF000000 | rgb;
                if ((effect.ghostColor() & 0xFFFFFF) != 0) {
                    graphics.drawString(font, character, drawX + effect.ghostOffsetX(), drawY + effect.ghostOffsetY(), 0xFF000000 | effect.ghostColor(), false);
                }
                graphics.drawString(font, character, drawX, drawY, color, effect.shadow());
                cursor += width;
            }
            if (effect.particleStyle() != null && effect.particleCount() > 0) {
                int padding = effect.particlePadding();
                NexusGuiParticles.render(graphics, effect.particleStyle(), x - padding, y - padding, Math.max(1, font.width(text) + padding * 2), font.lineHeight + padding * 2, effect.particleCount(), text.hashCode());
            }
            if (effect.glitchIntensity() > 0.0D) {
                int padding = effect.glitchPadding();
                NexusFramebufferGlitches.request(effect.id().getNamespace(), effect.id(), new NexusGlitchBounds(x - padding, y - padding, Math.max(1, font.width(text) + padding * 2), font.lineHeight + padding * 2), effect.glitchIntensity(), "text_effect:" + effect.id());
            }
            return cursor;
        } finally {
            int depth = RENDER_DEPTH.get() - 1;
            if (depth <= 0) {
                RENDER_DEPTH.remove();
            } else {
                RENDER_DEPTH.set(depth);
            }
        }
    }

    public static int lerp(int start, int end, float amount) {
        int sr = start >> 16 & 255;
        int sg = start >> 8 & 255;
        int sb = start & 255;
        int er = end >> 16 & 255;
        int eg = end >> 8 & 255;
        int eb = end & 255;
        int r = Math.round(sr + (er - sr) * amount);
        int g = Math.round(sg + (eg - sg) * amount);
        int b = Math.round(sb + (eb - sb) * amount);
        return r << 16 | g << 8 | b;
    }

    private static int jitter(long time, int index, long period, int amplitude, int salt) {
        if (amplitude <= 0) {
            return 0;
        }
        long step = time / Math.max(1L, period);
        int range = amplitude * 2 + 1;
        int hash = Long.hashCode(step * 73428767L + index * 912931L + salt);
        return Math.floorMod(hash, range) - amplitude;
    }
}
