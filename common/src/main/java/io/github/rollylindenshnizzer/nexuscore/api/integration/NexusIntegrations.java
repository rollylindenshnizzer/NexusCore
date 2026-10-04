package io.github.rollylindenshnizzer.nexuscore.api.integration;

import dev.architectury.platform.Platform;
import io.github.rollylindenshnizzer.nexuscore.NexusCore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NexusIntegrations {
    private static final Map<String, Set<String>> REQUESTED_MODS = new LinkedHashMap<>();
    private static final Map<String, Set<String>> INITIALIZED_FEATURES = new LinkedHashMap<>();

    private NexusIntegrations() {
    }

    public static synchronized void initialize() {
        NexusCore.LOGGER.info("Initialized NexusCore integration system");
    }

    public static boolean isLoaded(String modId) {
        return Platform.isModLoaded(modId);
    }

    public static boolean allLoaded(Collection<String> modIds) {
        return modIds.stream().allMatch(Platform::isModLoaded);
    }

    public static synchronized boolean whenLoaded(String ownerModId, String featureId, Collection<String> requiredMods, Runnable initializer) {
        REQUESTED_MODS.computeIfAbsent(ownerModId, ignored -> new LinkedHashSet<>()).addAll(requiredMods);
        List<String> missing = requiredMods.stream().filter(id -> !Platform.isModLoaded(id)).toList();
        if (!missing.isEmpty()) {
            NexusCore.LOGGER.info("Skipped NexusCore integration feature {} for {} because {} are not loaded", featureId, ownerModId, missing);
            return false;
        }
        initializer.run();
        INITIALIZED_FEATURES.computeIfAbsent(ownerModId, ignored -> new LinkedHashSet<>()).add(featureId);
        NexusCore.LOGGER.info("Initialized NexusCore integration feature {} for {} with {}", featureId, ownerModId, requiredMods);
        return true;
    }

    public static boolean whenLoaded(String ownerModId, String featureId, String requiredMod, Runnable initializer) {
        return whenLoaded(ownerModId, featureId, List.of(requiredMod), initializer);
    }

    public static synchronized Set<String> requestedMods(String ownerModId) {
        return Set.copyOf(REQUESTED_MODS.getOrDefault(ownerModId, Set.of()));
    }

    public static synchronized Set<String> initializedFeatures(String ownerModId) {
        return Set.copyOf(INITIALIZED_FEATURES.getOrDefault(ownerModId, Set.of()));
    }

    public static synchronized List<String> installedRequestedMods(String ownerModId) {
        return new ArrayList<>(requestedMods(ownerModId).stream().filter(Platform::isModLoaded).toList());
    }

    public static synchronized void logDependentMods(Collection<String> modIds) {
        if (modIds.isEmpty()) {
            NexusCore.LOGGER.info("No loaded mods declare NexusCore as a dependency");
        } else {
            NexusCore.LOGGER.info("Loaded mods depending on NexusCore: {}", modIds);
        }
    }

    public static synchronized void featureUsed(String ownerModId, String featureId) {
        Set<String> features = INITIALIZED_FEATURES.computeIfAbsent(ownerModId, ignored -> new LinkedHashSet<>());
        if (features.add(featureId)) {
            NexusCore.LOGGER.info("Initialized NexusCore feature {} for {}", featureId, ownerModId);
        }
    }
}
