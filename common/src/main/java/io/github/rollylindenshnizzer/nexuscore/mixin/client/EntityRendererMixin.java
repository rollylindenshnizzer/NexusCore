package io.github.rollylindenshnizzer.nexuscore.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.rollylindenshnizzer.nexuscore.NexusCoreConfig;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextEffect;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextEffects;
import io.github.rollylindenshnizzer.nexuscore.client.NexusWorldTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    public abstract Font getFont();

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void nexuscore$renderStyledNameTag(Entity entity, Component name, PoseStack poseStack, MultiBufferSource buffers, int packedLight, float partialTick, CallbackInfo ci) {
        if (!(entity instanceof Player) || !NexusCoreConfig.NAMEPLATE_EFFECTS.get()) {
            return;
        }
        ResourceLocation effectId = ResourceLocation.tryParse(NexusCoreConfig.NAMEPLATE_EFFECT.get());
        NexusTextEffect effect = effectId == null ? null : NexusTextEffects.effect(effectId).orElse(null);
        if (effect == null || entityRenderDispatcher.distanceToSqr(entity) > 4096.0D) {
            return;
        }
        Vec3 attachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
        if (attachment == null) {
            return;
        }
        boolean visible = !entity.isDiscrete();
        int y = "deadmau5".equals(name.getString()) ? -10 : 0;
        poseStack.pushPose();
        poseStack.translate(attachment.x, attachment.y + 0.5D, attachment.z);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        int background = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        Font font = getFont();
        float x = -font.width(name) / 2.0F;
        NexusWorldTextRenderer.render(font, name.getString(), x, y, matrix, buffers, visible ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, background, packedLight, effect);
        if (visible) {
            NexusWorldTextRenderer.render(font, name.getString(), x, y, matrix, buffers, Font.DisplayMode.NORMAL, 0, packedLight, effect);
        }
        poseStack.popPose();
        ci.cancel();
    }
}
