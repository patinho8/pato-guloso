package br.mareeletrica.mixin.client;
import br.mareeletrica.client.ThermalClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** 0.65.4 thermal goggles: the glow outline takes a heat colour. */
@Mixin(Entity.class)
public abstract class ThermalColorMixin {
 @Inject(method="getTeamColor",at=@At("HEAD"),cancellable=true)
 private void thermalColor(CallbackInfoReturnable<Integer> cir){var self=(Entity)(Object)this;if(self.level().isClientSide()&&ThermalClient.glows(self))cir.setReturnValue(ThermalClient.color(self));}
}
