package br.mareeletrica.mixin.client;
import br.mareeletrica.client.ScopeClient;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Options.class)
public abstract class ScopeOptionsMixin {
 @Inject(method="getEffectiveRenderDistance",at=@At("RETURN"),cancellable=true)
 private void mare$scopeRenderDistance(CallbackInfoReturnable<Integer> ci){if(ScopeClient.radius()>ci.getReturnValue())ci.setReturnValue(ScopeClient.radius());}
}
