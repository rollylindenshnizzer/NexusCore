package io.github.rollylindenshnizzer.nexuscore.mixin.worldgen;

import io.github.rollylindenshnizzer.nexuscore.api.worldgen.NexusWorldgen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Inject(method = "createStructures", at = @At("HEAD"), cancellable = true)
    private void nexuscore$createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager templateManager, CallbackInfo ci) {
        if (NexusWorldgen.cancelStructures((ChunkGenerator) (Object) this, registryAccess, structureState, structureManager, chunk, templateManager)) {
            ci.cancel();
        }
    }
}
