package io.github.rollylindenshnizzer.nexuscore.api.client.text;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class NexusTextEffects {
    private static final String PREFIX = "nexuscore/text_effects";
    private static final Map<ResourceLocation, NexusTextEffect> DEFAULTS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, NexusTextEffect> EFFECTS = new LinkedHashMap<>();
    private static final Map<String, ResourceLocation> EXACT_TEXT = new LinkedHashMap<>();

    private NexusTextEffects() {
    }

    public static synchronized void registerDefault(String ownerModId, NexusTextEffect effect) {
        DEFAULTS.put(effect.id(), effect);
        EFFECTS.putIfAbsent(effect.id(), effect);
        NexusIntegrations.featureUsed(ownerModId, "text_effects");
    }

    public static synchronized void bindText(String ownerModId, String exactText, ResourceLocation effectId) {
        EXACT_TEXT.put(exactText, effectId);
        NexusIntegrations.featureUsed(ownerModId, "global_text_bindings");
    }

    public static synchronized Optional<NexusTextEffect> effect(ResourceLocation id) {
        return Optional.ofNullable(EFFECTS.getOrDefault(id, DEFAULTS.get(id)));
    }

    public static synchronized List<ResourceLocation> ids() {
        LinkedHashSet<ResourceLocation> ids = new LinkedHashSet<>(DEFAULTS.keySet());
        ids.addAll(EFFECTS.keySet());
        return List.copyOf(new ArrayList<>(ids));
    }

    public static synchronized Optional<NexusTextEffect> effectForText(String text) {
        ResourceLocation id = EXACT_TEXT.get(text);
        return id == null ? Optional.empty() : effect(id);
    }

    public static synchronized void reload(ResourceManager manager) {
        EFFECTS.clear();
        EFFECTS.putAll(DEFAULTS);
        Map<ResourceLocation, Resource> resources = manager.listResources(PREFIX, id -> id.getPath().endsWith(".json"));
        resources.forEach((resourceId, resource) -> {
            ResourceLocation effectId = idFromResource(resourceId);
            if (effectId == null) {
                return;
            }
            try (Reader reader = resource.openAsReader()) {
                EFFECTS.put(effectId, parse(effectId, JsonParser.parseReader(reader).getAsJsonObject(), EFFECTS.get(effectId)));
            } catch (Exception exception) {
                NexusCore.LOGGER.error("Failed to load NexusCore text effect {}", resourceId, exception);
            }
        });
        NexusCore.LOGGER.info("Loaded {} NexusCore text effects", EFFECTS.size());
    }

    private static NexusTextEffect parse(ResourceLocation id, JsonObject json, NexusTextEffect fallback) {
        NexusTextEffect base = fallback == null ? NexusTextEffect.builder(id).build() : fallback;
        NexusTextEffect.Builder builder = NexusTextEffect.builder(id);
        JsonObject gradient = object(json, "gradient");
        builder.gradient(color(gradient, "start", base.startColor()), color(gradient, "end", base.endColor()));
        JsonObject wave = object(json, "wave");
        builder.wave(number(wave, "amplitude", base.waveAmplitude()), number(wave, "speed", base.waveSpeed()), number(wave, "phase", base.wavePhase()));
        JsonObject pulse = object(json, "pulse");
        builder.pulse(number(pulse, "strength", base.pulseStrength()), number(pulse, "speed", base.pulseSpeed()));
        JsonObject jitter = object(json, "jitter");
        builder.jitter(integer(jitter, "x", base.jitterX()), integer(jitter, "y", base.jitterY()), longNumber(jitter, "period", base.jitterPeriod()));
        JsonObject ghost = object(json, "ghost");
        builder.ghost(color(ghost, "color", base.ghostColor()), integer(ghost, "x", base.ghostOffsetX()), integer(ghost, "y", base.ghostOffsetY()));
        builder.shadow(json.has("shadow") ? json.get("shadow").getAsBoolean() : base.shadow());
        JsonObject particles = object(json, "particles");
        ResourceLocation style = particles.has("style") ? ResourceLocation.tryParse(particles.get("style").getAsString()) : base.particleStyle();
        builder.particles(style, integer(particles, "count", base.particleCount()), integer(particles, "padding", base.particlePadding()));
        JsonObject glitch = object(json, "glitch");
        builder.glitch(number(glitch, "intensity", base.glitchIntensity()), integer(glitch, "padding", base.glitchPadding()));
        return builder.build();
    }

    private static JsonObject object(JsonObject parent, String key) {
        return parent.has(key) && parent.get(key).isJsonObject() ? parent.getAsJsonObject(key) : new JsonObject();
    }

    private static double number(JsonObject object, String key, double fallback) {
        return object.has(key) ? object.get(key).getAsDouble() : fallback;
    }

    private static int integer(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private static long longNumber(JsonObject object, String key, long fallback) {
        return object.has(key) ? object.get(key).getAsLong() : fallback;
    }

    private static int color(JsonObject object, String key, int fallback) {
        if (!object.has(key)) {
            return fallback;
        }
        String value = object.get(key).getAsString().trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        try {
            return (int) Long.parseLong(value, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static ResourceLocation idFromResource(ResourceLocation resource) {
        String path = resource.getPath();
        String expected = PREFIX + "/";
        if (!path.startsWith(expected) || !path.endsWith(".json")) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), path.substring(expected.length(), path.length() - 5));
    }
}
