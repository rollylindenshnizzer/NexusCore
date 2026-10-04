package io.github.rollylindenshnizzer.nexuscore.api.client.particle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class NexusGuiParticles {
    private static final String PREFIX = "nexuscore/gui_particles";
    private static final Map<ResourceLocation, NexusGuiParticleStyle> DEFAULTS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResolvedStyle> STYLES = new LinkedHashMap<>();

    private NexusGuiParticles() {
    }

    public static synchronized void registerDefault(String ownerModId, NexusGuiParticleStyle style) {
        DEFAULTS.put(style.id(), style);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.getResourceManager() != null) {
            STYLES.put(style.id(), resolve(minecraft.getResourceManager(), style));
        }
        NexusIntegrations.featureUsed(ownerModId, "gui_particles");
    }

    public static synchronized boolean has(ResourceLocation id) {
        return STYLES.containsKey(id) || DEFAULTS.containsKey(id);
    }

    public static void render(GuiGraphics graphics, ResourceLocation styleId, int x, int y, int width, int height, int count, long seed) {
        ResolvedStyle resolved;
        synchronized (NexusGuiParticles.class) {
            resolved = STYLES.get(styleId);
        }
        if (resolved == null || resolved.textures().isEmpty() || count <= 0) {
            return;
        }
        NexusIntegrations.featureUsed(styleId.getNamespace(), "gui_particles");
        long time = Util.getMillis();
        NexusGuiParticleStyle style = resolved.style();
        int size = style.size();
        int areaWidth = Math.max(1, width - size + 1);
        int areaHeight = Math.max(1, height - size + 1);
        float red = (style.tint() >> 16 & 255) / 255.0F;
        float green = (style.tint() >> 8 & 255) / 255.0F;
        float blue = (style.tint() & 255) / 255.0F;
        graphics.setColor(red, green, blue, style.alpha());
        for (int i = 0; i < count; i++) {
            long phase = time / Math.max(1L, style.frameTime()) + seed * 31L + i * 977L;
            int px = x + Math.floorMod(hash(phase, i, 17), areaWidth);
            int py = y + Math.floorMod(hash(phase / 2L, i, 43), areaHeight);
            int textureIndex = Math.floorMod((int) (phase + i), resolved.textures().size());
            TextureFrame frame = resolved.textures().get(textureIndex);
            graphics.blit(frame.location(), px, py, size, size, 0.0F, 0.0F, frame.width(), frame.height(), frame.width(), frame.height());
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static synchronized void reload(ResourceManager manager) {
        STYLES.clear();
        DEFAULTS.values().forEach(style -> STYLES.put(style.id(), resolve(manager, style)));
        Map<ResourceLocation, Resource> resources = manager.listResources(PREFIX, id -> id.getPath().endsWith(".json"));
        resources.forEach((resourceId, resource) -> {
            ResourceLocation styleId = idFromResource(resourceId);
            if (styleId == null) {
                return;
            }
            try (Reader reader = resource.openAsReader()) {
                NexusGuiParticleStyle fallback = Optional.ofNullable(STYLES.get(styleId)).map(ResolvedStyle::style).orElse(DEFAULTS.get(styleId));
                NexusGuiParticleStyle style = parse(styleId, JsonParser.parseReader(reader).getAsJsonObject(), fallback);
                STYLES.put(styleId, resolve(manager, style));
            } catch (Exception exception) {
                NexusCore.LOGGER.error("Failed to load NexusCore GUI particle style {}", resourceId, exception);
            }
        });
        NexusCore.LOGGER.info("Loaded {} NexusCore GUI particle styles", STYLES.size());
    }

    private static ResolvedStyle resolve(ResourceManager manager, NexusGuiParticleStyle style) {
        List<ResourceLocation> textures = new ArrayList<>();
        if (style.texture() != null) {
            textures.add(style.texture());
        } else if (style.particle() != null) {
            ResourceLocation particleJson = ResourceLocation.fromNamespaceAndPath(style.particle().getNamespace(), "particles/" + style.particle().getPath() + ".json");
            try {
                Optional<Resource> resource = manager.getResource(particleJson);
                if (resource.isPresent()) {
                    try (Reader reader = resource.get().openAsReader()) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        JsonArray array = json.getAsJsonArray("textures");
                        if (array != null) {
                            array.forEach(element -> {
                                ResourceLocation texture = ResourceLocation.tryParse(element.getAsString());
                                if (texture != null) {
                                    textures.add(ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "textures/particle/" + texture.getPath() + ".png"));
                                }
                            });
                        }
                    }
                }
            } catch (Exception exception) {
                NexusCore.LOGGER.error("Failed to resolve particle {} for GUI particle style {}", style.particle(), style.id(), exception);
            }
        }
        List<TextureFrame> frames = new ArrayList<>();
        for (ResourceLocation texture : textures) {
            int width = 16;
            int height = 16;
            try {
                Optional<Resource> resource = manager.getResource(texture);
                if (resource.isPresent()) {
                    try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
                        width = image.getWidth();
                        height = image.getHeight();
                    }
                }
            } catch (Exception ignored) {
            }
            frames.add(new TextureFrame(texture, Math.max(1, width), Math.max(1, height)));
        }
        return new ResolvedStyle(style, List.copyOf(frames));
    }

    private static NexusGuiParticleStyle parse(ResourceLocation id, JsonObject json, NexusGuiParticleStyle fallback) {
        NexusGuiParticleStyle base = fallback == null ? NexusGuiParticleStyle.builder(id).build() : fallback;
        NexusGuiParticleStyle.Builder builder = NexusGuiParticleStyle.builder(id);
        ResourceLocation particle = json.has("particle") ? ResourceLocation.tryParse(json.get("particle").getAsString()) : base.particle();
        ResourceLocation texture = json.has("texture") ? ResourceLocation.tryParse(json.get("texture").getAsString()) : base.texture();
        builder.particle(particle).texture(texture);
        builder.size(json.has("size") ? json.get("size").getAsInt() : base.size());
        builder.tint(json.has("tint") ? color(json.get("tint").getAsString(), base.tint()) : base.tint());
        builder.alpha(json.has("alpha") ? json.get("alpha").getAsFloat() : base.alpha());
        builder.frameTime(json.has("frame_time") ? json.get("frame_time").getAsLong() : base.frameTime());
        return builder.build();
    }

    private static ResourceLocation idFromResource(ResourceLocation resource) {
        String path = resource.getPath();
        String expected = PREFIX + "/";
        if (!path.startsWith(expected) || !path.endsWith(".json")) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), path.substring(expected.length(), path.length() - 5));
    }

    private static int color(String value, int fallback) {
        String normalized = value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        try {
            return (int) Long.parseLong(normalized, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int hash(long phase, int index, int salt) {
        return Long.hashCode(phase * 1103515245L + index * 12345L + salt * 2654435761L);
    }

    private record TextureFrame(ResourceLocation location, int width, int height) {
    }

    private record ResolvedStyle(NexusGuiParticleStyle style, List<TextureFrame> textures) {
    }
}
