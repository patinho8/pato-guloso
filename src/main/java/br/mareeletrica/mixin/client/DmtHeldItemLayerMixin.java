package br.mareeletrica.mixin.client;
import br.mareeletrica.DmtCombat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandLayer.class)
public abstract class DmtHeldItemLayerMixin {
 @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",at=@At("HEAD"),cancellable=true)
 private void equipment(PoseStack pose,MultiBufferSource buffers,int light,LivingEntity entity,float swing,float amount,float partial,float age,float yaw,float pitch,CallbackInfo ci){if(entity instanceof Player player&&DmtCombat.mode(player)>0)ci.cancel();}
}
