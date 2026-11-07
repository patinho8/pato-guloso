package br.mareeletrica.mixin.client;
import br.mareeletrica.client.RemoteClient;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
 @Redirect(method="turnPlayer",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
 private void mare$turnCamera(LocalPlayer p,double x,double y){if(RemoteClient.active())RemoteClient.turn(x,y);else {double scale=br.mareeletrica.client.SniperClient.mouseScale();if(p.isUsingItem()&&p.getUseItem().is(br.mareeletrica.Gadgets.BINOCULARS.get()))scale*=.12;   // binoculars: slow, steady look
  p.turn(x*scale,y*scale);}}
}
