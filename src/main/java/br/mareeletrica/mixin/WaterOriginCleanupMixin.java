package br.mareeletrica.mixin;
import br.mareeletrica.WaterOrigin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Level.class)
public abstract class WaterOriginCleanupMixin {
 @Inject(method="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",at=@At("RETURN"))
 private void clear(BlockPos pos,BlockState state,int flags,int depth,CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValue()&&(Object)this instanceof ServerLevel server&&!state.getFluidState().is(FluidTags.WATER)&&WaterOrigin.artificial(server,pos))WaterOrigin.clear(server,pos);}
}
