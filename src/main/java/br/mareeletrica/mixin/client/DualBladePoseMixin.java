package br.mareeletrica.mixin.client;
import br.mareeletrica.DmtCombat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HumanoidModel.class)
public abstract class DualBladePoseMixin {
 @Shadow @Final public ModelPart rightArm; @Shadow @Final public ModelPart leftArm;
 @Inject(method="setupAttackAnimation",at=@At("TAIL")) private void bothArms(LivingEntity entity,float age,CallbackInfo ci){if(entity instanceof Player p&&DmtCombat.mode(p)==1&&p.getAttackAnim(0)>0){leftArm.xRot=rightArm.xRot;leftArm.yRot=-rightArm.yRot;leftArm.zRot=-rightArm.zRot;}}
}
