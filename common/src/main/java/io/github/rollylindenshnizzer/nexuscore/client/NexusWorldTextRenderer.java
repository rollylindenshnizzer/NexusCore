package io.github.rollylindenshnizzer.nexuscore.client;

import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextEffect;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextRenderer;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public final class NexusWorldTextRenderer {
    private NexusWorldTextRenderer() {
    }

    public static void render(Font font, String text, float x, float y, Matrix4f matrix, MultiBufferSource buffers, Font.DisplayMode mode, int background, int packedLight, NexusTextEffect effect) {
        long time = Util.getMillis();
        float cursor = x;
        int denominator = Math.max(1, text.length() - 1);
        for (int i = 0; i < text.length(); i++) {
            String character = String.valueOf(text.charAt(i));
            double wave = effect.waveAmplitude() == 0.0D ? 0.0D : Math.sin(time / Math.max(1.0D, effect.waveSpeed()) + i * effect.wavePhase()) * effect.waveAmplitude();
            int jitterX = jitter(time, i, effect.jitterPeriod(), effect.jitterX(), 17);
            int jitterY = jitter(time, i, effect.jitterPeriod(), effect.jitterY(), 31);
            int rgb = NexusTextRenderer.lerp(effect.startColor(), effect.endColor(), i / (float) denominator);
            if (effect.pulseStrength() > 0.0D) {
                float pulse = (float) (((Math.sin(time / effect.pulseSpeed()) + 1.0D) * 0.5D) * effect.pulseStrength());
                rgb = NexusTextRenderer.lerp(rgb, 0xFFFFFF, pulse);
            }
            int color = 0xFF000000 | rgb;
            if ((effect.ghostColor() & 0xFFFFFF) != 0) {
                font.drawInBatch(character, cursor + jitterX + effect.ghostOffsetX(), y + (float) wave + jitterY + effect.ghostOffsetY(), 0xFF000000 | effect.ghostColor(), false, matrix, buffers, mode, 0, packedLight);
            }
            font.drawInBatch(character, cursor + jitterX, y + (float) wave + jitterY, color, effect.shadow(), matrix, buffers, mode, background, packedLight);
            cursor += font.width(character);
        }
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
