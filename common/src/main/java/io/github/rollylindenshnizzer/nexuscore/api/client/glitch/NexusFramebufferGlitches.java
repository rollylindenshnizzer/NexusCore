package io.github.rollylindenshnizzer.nexuscore.api.client.glitch;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class NexusFramebufferGlitches {
    private static final Map<ResourceLocation, RegisteredEffect> REGISTERED = new LinkedHashMap<>();
    private static final List<GlitchRequest> FRAME_REQUESTS = new ArrayList<>();
    private static TextureTarget scratch;

    private NexusFramebufferGlitches() {
    }

    // register the effect
    public static synchronized void register(String ownerModId, ResourceLocation id, BooleanSupplier condition, Supplier<NexusGlitchBounds> bounds, DoubleSupplier intensity, Supplier<String> reason) {
        REGISTERED.put(id, new RegisteredEffect(condition, bounds, intensity, reason));
        NexusIntegrations.featureUsed(ownerModId, "framebuffer_glitch");
        NexusCore.LOGGER.info("Registered NexusCore framebuffer glitch {} for {}", id, ownerModId);
    }

    public static synchronized void unregister(ResourceLocation id) {
        REGISTERED.remove(id);
    }

    public static synchronized void request(String ownerModId, ResourceLocation id, NexusGlitchBounds bounds, double intensity, String reason) {
        if (bounds.width() <= 0 || bounds.height() <= 0 || intensity <= 0.0D) {
            return;
        }
        FRAME_REQUESTS.add(new GlitchRequest(id, bounds, clampIntensity(intensity), reason == null ? "" : reason));
        NexusIntegrations.featureUsed(ownerModId, "framebuffer_glitch");
    }

    public static void renderFrame() {
        List<GlitchRequest> requests = new ArrayList<>();
        synchronized (NexusFramebufferGlitches.class) {
            REGISTERED.forEach((id, effect) -> {
                try {
                    if (effect.condition().getAsBoolean()) {
                        NexusGlitchBounds bounds = effect.bounds().get();
                        double intensity = clampIntensity(effect.intensity().getAsDouble());
                        if (bounds != null && bounds.width() > 0 && bounds.height() > 0 && intensity > 0.0D) {
                            requests.add(new GlitchRequest(id, bounds, intensity, effect.reason().get()));
                        }
                    }
                } catch (RuntimeException exception) {
                    NexusCore.LOGGER.error("Failed to evaluate NexusCore framebuffer glitch {}", id, exception);
                }
            });
            requests.addAll(FRAME_REQUESTS);
            FRAME_REQUESTS.clear();
        }
        if (requests.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.getMainRenderTarget();
        if (main == null || main.width <= 0 || main.height <= 0) {
            return;
        }
        ensureScratch(main.width, main.height);
        copy(main, scratch);
        main.bindWrite(true);
        int guiWidth = Math.max(1, minecraft.getWindow().getGuiScaledWidth());
        int guiHeight = Math.max(1, minecraft.getWindow().getGuiScaledHeight());
        for (GlitchRequest request : requests) {
            renderRequest(main, scratch, request, guiWidth, guiHeight);
        }
        GlStateManager._disableScissorTest();
        main.bindWrite(true);
    }

    private static void renderRequest(RenderTarget main, RenderTarget source, GlitchRequest request, int guiWidth, int guiHeight) {
        double sx = main.width / (double) guiWidth;
        double sy = main.height / (double) guiHeight;
        NexusGlitchBounds bounds = request.bounds();
        int left = clamp((int) Math.floor(bounds.x() * sx), 0, main.width);
        int right = clamp((int) Math.ceil((bounds.x() + bounds.width()) * sx), 0, main.width);
        int bottom = clamp(main.height - (int) Math.ceil((bounds.y() + bounds.height()) * sy), 0, main.height);
        int top = clamp(main.height - (int) Math.floor(bounds.y() * sy), 0, main.height);
        if (right <= left || top <= bottom) {
            return;
        }
        int regionWidth = right - left;
        int regionHeight = top - bottom;
        int strips = Math.max(2, 2 + (int) Math.round(request.intensity() * 10.0D));
        int maxShift = Math.max(1, (int) Math.round(2.0D + request.intensity() * Math.min(16.0D, regionWidth * 0.2D)));
        long time = Util.getMillis() / 45L;
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(left, bottom, regionWidth, regionHeight);
        GlStateManager._glBindFramebuffer(36008, source.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, main.frameBufferId);
        for (int i = 0; i < strips; i++) {
            int y0 = bottom + i * regionHeight / strips;
            int y1 = bottom + (i + 1) * regionHeight / strips;
            int shift = Math.floorMod(Long.hashCode(time * 31L + request.id().hashCode() * 17L + i * 101L), maxShift * 2 + 1) - maxShift;
            GlStateManager._glBlitFrameBuffer(left, y0, right, y1, left + shift, y0, right + shift, y1, 16384, 9728);
        }
        if (request.intensity() > 0.55D) {
            int split = bottom + Math.floorMod(Long.hashCode(time + request.id().hashCode()), Math.max(1, regionHeight));
            int height = Math.max(1, Math.min(regionHeight / 6, 2 + (int) (request.intensity() * 6)));
            int y1 = Math.min(top, split + height);
            GlStateManager._glBlitFrameBuffer(left, split, right, y1, left - maxShift, split, right - maxShift, y1, 16384, 9728);
        }
        GlStateManager._disableScissorTest();
    }

    private static void copy(RenderTarget source, RenderTarget target) {
        GlStateManager._glBindFramebuffer(36008, source.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, target.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, source.width, source.height, 0, 0, target.width, target.height, 16384, 9728);
    }

    private static void ensureScratch(int width, int height) {
        if (scratch == null) {
            scratch = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        } else if (scratch.width != width || scratch.height != height) {
            scratch.resize(width, height, Minecraft.ON_OSX);
        }
    }

    private static double clampIntensity(double intensity) {
        return Math.max(0.0D, Math.min(1.0D, intensity));
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private record RegisteredEffect(BooleanSupplier condition, Supplier<NexusGlitchBounds> bounds,
                                    DoubleSupplier intensity, Supplier<String> reason) {
    }

    private record GlitchRequest(ResourceLocation id, NexusGlitchBounds bounds, double intensity, String reason) {
    }
}
