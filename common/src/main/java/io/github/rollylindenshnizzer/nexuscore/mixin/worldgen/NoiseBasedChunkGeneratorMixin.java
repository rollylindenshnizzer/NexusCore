package io.github.rollylindenshnizzer.nexuscore.mixin.worldgen;

import io.github.rollylindenshnizzer.nexuscore.api.worldgen.NexusWorldgen;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    @Unique
    private static final ThreadLocal<RandomState> nexuscore$randomState = new ThreadLocal<>();

    @Inject(method = "doCreateBiomes", at = @At("HEAD"))
    private void nexuscore$captureRandomState(net.minecraft.world.level.levelgen.blending.Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk, CallbackInfo ci) {
        nexuscore$randomState.set(randomState);
    }

    @ModifyArg(method = "doCreateBiomes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/blending/Blender;getBiomeResolver(Lnet/minecraft/world/level/biome/BiomeResolver;)Lnet/minecraft/world/level/biome/BiomeResolver;"), index = 0)
    private BiomeResolver nexuscore$modifyBiomeResolver(BiomeResolver original) {
        return NexusWorldgen.modifyBiomeResolver((ChunkGenerator) (Object) this, original, nexuscore$randomState.get());
    }

    @Inject(method = "doCreateBiomes", at = @At("RETURN"))
    private void nexuscore$clearRandomState(net.minecraft.world.level.levelgen.blending.Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk, CallbackInfo ci) {
        nexuscore$randomState.remove();
    }

    @Inject(method = "applyCarvers", at = @At("HEAD"), cancellable = true)
    private void nexuscore$applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step, CallbackInfo ci) {
        if (NexusWorldgen.cancelCarvers((NoiseBasedChunkGenerator) (Object) this, region, seed, randomState, structureManager, chunk, step)) {
            ci.cancel();
        }
    }
}
