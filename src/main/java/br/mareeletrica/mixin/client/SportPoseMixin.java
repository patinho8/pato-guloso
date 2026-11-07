package br.mareeletrica.mixin.client;
import br.mareeletrica.client.SportsClient;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public abstract class SportPoseMixin {
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
 private void sportPose(LivingEntity entity,float swing,float amount,float age,float yaw,float pitch,CallbackInfo ci){SportsClient.pose((PlayerModel<?>)(Object)this,entity);}
}
