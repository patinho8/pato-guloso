package br.mareeletrica.mixin.client;
import br.mareeletrica.client.SkatePose;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** 0.65.8: skater pose on the skateboard. */
@Mixin(PlayerModel.class)
public abstract class SkatePoseMixin {
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
 private void skatePose(LivingEntity entity,float swing,float amount,float age,float yaw,float pitch,CallbackInfo ci){SkatePose.pose((PlayerModel<?>)(Object)this,entity);br.mareeletrica.client.Fun067Renderers.pose((PlayerModel<?>)(Object)this,entity);}
}
