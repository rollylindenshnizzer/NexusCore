package io.github.rollylindenshnizzer.nexuscore.mixin.client;

import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextEffect;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextEffects;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void nexuscore$renderString(Font font, String text, int x, int y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        render(font, text, x, y, cir);
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void nexuscore$renderComponent(Font font, Component text, int x, int y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        render(font, text.getString(), x, y, cir);
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void nexuscore$renderFormatted(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        render(font, plainText(text), x, y, cir);
    }

    private void render(Font font, String text, int x, int y, CallbackInfoReturnable<Integer> cir) {
        if (NexusTextRenderer.isRendering()) {
            return;
        }
        NexusTextEffect effect = NexusTextEffects.effectForText(text).orElse(null);
        if (effect != null) {
            cir.setReturnValue(NexusTextRenderer.render((GuiGraphics) (Object) this, font, text, x, y, effect));
        }
    }

    private static String plainText(FormattedCharSequence text) {
        StringBuilder builder = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }
}
