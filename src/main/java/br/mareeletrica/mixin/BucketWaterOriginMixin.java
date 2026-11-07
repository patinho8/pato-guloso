package br.mareeletrica.mixin;
import br.mareeletrica.WaterOrigin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BucketItem.class)
public abstract class BucketWaterOriginMixin {
 @Inject(method="emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",at=@At("RETURN"))
 private void bucket(Player player,Level level,BlockPos pos,BlockHitResult hit,ItemStack stack,CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValue()&&level instanceof ServerLevel server&&level.getFluidState(pos).is(FluidTags.WATER))WaterOrigin.mark(server,pos);}
}
