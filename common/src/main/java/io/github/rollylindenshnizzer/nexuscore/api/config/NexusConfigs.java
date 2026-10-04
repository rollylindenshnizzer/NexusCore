package io.github.rollylindenshnizzer.nexuscore.api.config;

import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NexusConfigs {
    private static final Map<String, List<NexusJsonConfig>> CONFIGS = new LinkedHashMap<>();

    private NexusConfigs() {
    }

    public static synchronized NexusJsonConfig register(NexusJsonConfig config) {
        List<NexusJsonConfig> configs = CONFIGS.computeIfAbsent(config.ownerModId(), ignored -> new ArrayList<>());
        if (configs.stream().anyMatch(existing -> existing.id().equals(config.id()))) {
            throw new IllegalArgumentException("Duplicate NexusCore config " + config.ownerModId() + ":" + config.id());
        }
        configs.add(config);
        NexusIntegrations.featureUsed(config.ownerModId(), "config");
        config.load();
        return config;
    }

    public static synchronized void loadAll() {
        CONFIGS.values().stream().flatMap(Collection::stream).forEach(NexusJsonConfig::load);
    }

    public static synchronized List<NexusJsonConfig> forMod(String modId) {
        return List.copyOf(CONFIGS.getOrDefault(modId, List.of()));
    }

    public static synchronized Set<String> owners() {
        return new LinkedHashSet<>(CONFIGS.keySet());
    }

    public static synchronized void saveMod(String modId) {
        CONFIGS.getOrDefault(modId, List.of()).forEach(NexusJsonConfig::save);
        NexusCore.LOGGER.info("Saved NexusCore configs for {}", modId);
    }
}
