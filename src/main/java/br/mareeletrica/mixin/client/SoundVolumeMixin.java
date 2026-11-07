package br.mareeletrica.mixin.client;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** 0.66.0: every sound of the mod plays at about half volume, explosions (all of them) at ~40%. */
@Mixin(SoundEngine.class)
public abstract class SoundVolumeMixin {
 @Inject(method="calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F",at=@At("RETURN"),cancellable=true)
 private void mareQuieter(SoundInstance s,CallbackInfoReturnable<Float> cir){
  var id=s.getLocation();if(id==null)return;
  float k=id.getNamespace().equals("mareeletrica")?.5F:id.getPath().contains("explode")||id.getPath().contains("explosion")?.4F:1F;
  if(k<1F)cir.setReturnValue(cir.getReturnValueF()*k);
 }
}
