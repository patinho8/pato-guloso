package br.mareeletrica.mixin.client;

import br.mareeletrica.client.RemoteClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Vanilla centers its render sections on the body even when the camera is remote. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
 // Optional targets: renderer replacements may implement their own camera-centered sections.
 @ModifyExpressionValue(method="setupRender",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getX()D"),require=0)
 private double mare$cameraX(double original){var p=RemoteClient.renderPosition();return p==null?original:p.x;}
 @ModifyExpressionValue(method="setupRender",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getY()D"),require=0)
 private double mare$cameraY(double original){var p=RemoteClient.renderPosition();return p==null?original:p.y;}
 @ModifyExpressionValue(method="setupRender",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getZ()D"),require=0)
 private double mare$cameraZ(double original){var p=RemoteClient.renderPosition();return p==null?original:p.z;}
}
