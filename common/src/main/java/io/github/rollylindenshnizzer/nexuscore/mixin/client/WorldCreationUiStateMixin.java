package io.github.rollylindenshnizzer.nexuscore.mixin.client;

import io.github.rollylindenshnizzer.nexuscore.api.worldgen.NexusWorldgen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;

@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void nexuscore$selectDefaultWorldPreset(Path savesFolder, WorldCreationContext settings, Optional<ResourceKey<WorldPreset>> selectedPreset, OptionalLong seed, CallbackInfo ci) {
        if (seed.isPresent() || selectedPreset.filter(key -> !WorldPresets.NORMAL.equals(key)).isPresent()) {
            return;
        }
        ResourceKey<WorldPreset> preferred = NexusWorldgen.defaultWorldPreset();
        if (preferred == null) {
            return;
        }
        WorldCreationUiState self = (WorldCreationUiState) (Object) this;
        self.getNormalPresetList().stream().filter(entry -> entry.preset().unwrapKey().filter(preferred::equals).isPresent()).findFirst().ifPresent(self::setWorldType);
    }
}
