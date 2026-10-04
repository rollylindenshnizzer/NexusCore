package io.github.rollylindenshnizzer.nexuscore.api.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NexusProceduralTextures {
    private static final Map<ResourceLocation, Definition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, DynamicTexture> LIVE = new LinkedHashMap<>();

    private NexusProceduralTextures() {
    }

    public static synchronized ResourceLocation register(String ownerModId, ResourceLocation id, int width, int height, PixelGenerator generator) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Procedural texture dimensions must be positive");
        }
        DEFINITIONS.put(id, new Definition(width, height, generator));
        NexusIntegrations.featureUsed(ownerModId, "procedural_textures");
        if (Minecraft.getInstance() != null) {
            generate(id, DEFINITIONS.get(id));
        }
        return id;
    }

    public static synchronized void regenerateAll() {
        DEFINITIONS.forEach(NexusProceduralTextures::generate);
        NexusCore.LOGGER.info("Generated {} NexusCore procedural textures", DEFINITIONS.size());
    }

    public static synchronized void regenerate(ResourceLocation id) {
        Definition definition = DEFINITIONS.get(id);
        if (definition != null) {
            generate(id, definition);
        }
    }

    private static void generate(ResourceLocation id, Definition definition) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        DynamicTexture old = LIVE.remove(id);
        if (old != null) {
            minecraft.getTextureManager().release(id);
        }
        NativeImage image = new NativeImage(definition.width(), definition.height(), true);
        for (int y = 0; y < definition.height(); y++) {
            for (int x = 0; x < definition.width(); x++) {
                image.setPixelRGBA(x, y, argbToAbgr(definition.generator().argb(x, y, definition.width(), definition.height())));
            }
        }
        DynamicTexture texture = new DynamicTexture(image);
        minecraft.getTextureManager().register(id, texture);
        LIVE.put(id, texture);
    }

    private static int argbToAbgr(int argb) {
        int a = argb >>> 24 & 255;
        int r = argb >>> 16 & 255;
        int g = argb >>> 8 & 255;
        int b = argb & 255;
        return a << 24 | b << 16 | g << 8 | r;
    }

    @FunctionalInterface
    public interface PixelGenerator {
        int argb(int x, int y, int width, int height);
    }

    private record Definition(int width, int height, PixelGenerator generator) {
    }
}
