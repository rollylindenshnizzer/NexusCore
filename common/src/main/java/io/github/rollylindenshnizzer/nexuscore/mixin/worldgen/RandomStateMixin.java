package io.github.rollylindenshnizzer.nexuscore.mixin.worldgen;

import io.github.rollylindenshnizzer.nexuscore.api.worldgen.NexusWorldgen;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomState.class)
public abstract class RandomStateMixin {
    @Unique
    private static final ThreadLocal<HolderGetter.Provider> nexuscore$registries = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<ResourceKey<NoiseGeneratorSettings>> nexuscore$settingsKey = new ThreadLocal<>();

    @Inject(method = "create(Lnet/minecraft/core/HolderGetter$Provider;Lnet/minecraft/resources/ResourceKey;J)Lnet/minecraft/world/level/levelgen/RandomState;", at = @At("HEAD"))
    private static void nexuscore$captureWorldgenContext(HolderGetter.Provider registries, ResourceKey<NoiseGeneratorSettings> key, long seed, CallbackInfoReturnable<RandomState> cir) {
        nexuscore$registries.set(registries);
        nexuscore$settingsKey.set(key);
    }

    @ModifyArg(method = "create(Lnet/minecraft/core/HolderGetter$Provider;Lnet/minecraft/resources/ResourceKey;J)Lnet/minecraft/world/level/levelgen/RandomState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)Lnet/minecraft/world/level/levelgen/RandomState;"), index = 0)
    private static NoiseGeneratorSettings nexuscore$modifyNoiseGeneratorSettings(NoiseGeneratorSettings original) {
        HolderGetter.Provider registries = nexuscore$registries.get();
        ResourceKey<NoiseGeneratorSettings> key = nexuscore$settingsKey.get();
        if (registries == null || key == null) {
            return original;
        }
        return NexusWorldgen.modifyNoiseGeneratorSettings(registries, key, original);
    }

    @Inject(method = "create(Lnet/minecraft/core/HolderGetter$Provider;Lnet/minecraft/resources/ResourceKey;J)Lnet/minecraft/world/level/levelgen/RandomState;", at = @At("RETURN"))
    private static void nexuscore$clearWorldgenContext(HolderGetter.Provider registries, ResourceKey<NoiseGeneratorSettings> key, long seed, CallbackInfoReturnable<RandomState> cir) {
        nexuscore$registries.remove();
        nexuscore$settingsKey.remove();
    }
}
