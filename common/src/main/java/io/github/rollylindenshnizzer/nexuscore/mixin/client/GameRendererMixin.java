package io.github.rollylindenshnizzer.nexuscore.mixin.client;

import io.github.rollylindenshnizzer.nexuscore.api.client.glitch.NexusFramebufferGlitches;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void nexuscore$renderFramebufferGlitches(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        NexusFramebufferGlitches.renderFrame();
    }
}
