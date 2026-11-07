package br.mareeletrica.mixin.client;
import br.mareeletrica.client.ScopeClient;
import br.mareeletrica.client.SniperClient;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Entity.class)
public abstract class ScopeEntityRenderMixin {
 @Inject(method="shouldRenderAtSqrDistance",at=@At("HEAD"),cancellable=true)
 private void mare$distantMob(double distanceSquared,CallbackInfoReturnable<Boolean> ci){var entity=(Entity)(Object)this;if(entity instanceof LivingEntity&&SniperClient.scoped()&&ScopeClient.retains(entity.chunkPosition())&&distanceSquared<576D*576D)ci.setReturnValue(true);}
}
