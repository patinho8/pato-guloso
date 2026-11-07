package br.mareeletrica.mixin.client;
import br.mareeletrica.client.ThermalClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** 0.65.4 thermal goggles: living things glow through walls. */
@Mixin(Minecraft.class)
public abstract class ThermalGlowMixin {
 @Inject(method="shouldEntityAppearGlowing",at=@At("HEAD"),cancellable=true)
 private void thermalGlow(Entity e,CallbackInfoReturnable<Boolean> cir){if(ThermalClient.glows(e))cir.setReturnValue(true);}
}
