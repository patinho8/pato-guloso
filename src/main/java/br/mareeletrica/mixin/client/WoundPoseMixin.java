package br.mareeletrica.mixin.client;
import br.mareeletrica.client.WoundsClient;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** 0.65: a broken arm hangs; arms move while using bandages / syringes / medical kits. */
@Mixin(PlayerModel.class)
public abstract class WoundPoseMixin {
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
 private void woundPose(LivingEntity entity,float swing,float amount,float age,float yaw,float pitch,CallbackInfo ci){WoundsClient.pose((PlayerModel<?>)(Object)this,entity,age);}
}
