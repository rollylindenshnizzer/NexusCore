package io.github.rollylindenshnizzer.nexuscore.api.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public final class NexusWorldgen {
    private static final List<BiomeResolverModifier> BIOME_RESOLVER_MODIFIERS = new CopyOnWriteArrayList<>();
    private static final List<CarverRule> CARVER_RULES = new CopyOnWriteArrayList<>();
    private static final List<NoiseGeneratorSettingsModifier> NOISE_GENERATOR_SETTINGS_MODIFIERS = new CopyOnWriteArrayList<>();
    private static final List<StructureRule> STRUCTURE_RULES = new CopyOnWriteArrayList<>();
    private static final List<Supplier<ResourceKey<WorldPreset>>> DEFAULT_WORLD_PRESET_PROVIDERS = new CopyOnWriteArrayList<>();

    private NexusWorldgen() {
    }

    public static void registerBiomeResolverModifier(BiomeResolverModifier modifier) {
        BIOME_RESOLVER_MODIFIERS.add(Objects.requireNonNull(modifier, "modifier"));
    }

    public static void registerCarverRule(CarverRule rule) {
        CARVER_RULES.add(Objects.requireNonNull(rule, "rule"));
    }

    public static void registerNoiseGeneratorSettingsModifier(NoiseGeneratorSettingsModifier modifier) {
        NOISE_GENERATOR_SETTINGS_MODIFIERS.add(Objects.requireNonNull(modifier, "modifier"));
    }

    public static void registerStructureRule(StructureRule rule) {
        STRUCTURE_RULES.add(Objects.requireNonNull(rule, "rule"));
    }

    public static void registerDefaultWorldPresetProvider(Supplier<ResourceKey<WorldPreset>> provider) {
        DEFAULT_WORLD_PRESET_PROVIDERS.add(Objects.requireNonNull(provider, "provider"));
    }

    public static BiomeResolver modifyBiomeResolver(ChunkGenerator generator, BiomeResolver original, RandomState randomState) {
        BiomeResolver result = original;
        for (BiomeResolverModifier modifier : BIOME_RESOLVER_MODIFIERS) {
            BiomeResolver modified = modifier.modify(generator, result, randomState);
            if (modified != null) {
                result = modified;
            }
        }
        return result;
    }

    public static boolean cancelCarvers(NoiseBasedChunkGenerator generator, WorldGenRegion region, long seed, RandomState randomState, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        for (CarverRule rule : CARVER_RULES) {
            if (rule.cancel(generator, region, seed, randomState, structureManager, chunk, step)) {
                return true;
            }
        }
        return false;
    }

    public static boolean cancelStructures(ChunkGenerator generator, RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager) {
        for (StructureRule rule : STRUCTURE_RULES) {
            if (rule.cancel(generator, registryAccess, structureState, structureManager, chunk, templateManager)) {
                return true;
            }
        }
        return false;
    }

    public static NoiseGeneratorSettings modifyNoiseGeneratorSettings(HolderGetter.Provider registries, ResourceKey<NoiseGeneratorSettings> key, NoiseGeneratorSettings original) {
        NoiseGeneratorSettings result = original;
        for (NoiseGeneratorSettingsModifier modifier : NOISE_GENERATOR_SETTINGS_MODIFIERS) {
            NoiseGeneratorSettings modified = modifier.modify(registries, key, result);
            if (modified != null) {
                result = modified;
            }
        }
        return result;
    }

    public static ResourceKey<WorldPreset> defaultWorldPreset() {
        for (int i = DEFAULT_WORLD_PRESET_PROVIDERS.size() - 1; i >= 0; i--) {
            ResourceKey<WorldPreset> key = DEFAULT_WORLD_PRESET_PROVIDERS.get(i).get();
            if (key != null) {
                return key;
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface BiomeResolverModifier {
        BiomeResolver modify(ChunkGenerator generator, BiomeResolver original, RandomState randomState);
    }

    @FunctionalInterface
    public interface CarverRule {
        boolean cancel(NoiseBasedChunkGenerator generator, WorldGenRegion region, long seed, RandomState randomState, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step);
    }

    @FunctionalInterface
    public interface StructureRule {
        boolean cancel(ChunkGenerator generator, RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager);
    }

    @FunctionalInterface
    public interface NoiseGeneratorSettingsModifier {
        NoiseGeneratorSettings modify(HolderGetter.Provider registries, ResourceKey<NoiseGeneratorSettings> key, NoiseGeneratorSettings original);
    }
}
