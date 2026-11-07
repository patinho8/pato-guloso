package br.mareeletrica.mixin;
import br.mareeletrica.WaterOrigin;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FlowingFluid.class)
public abstract class FlowWaterOriginMixin {
 @Inject(method="spreadTo",at=@At("TAIL"))
 private void spread(LevelAccessor level,BlockPos pos,BlockState old,Direction direction,FluidState fluid,CallbackInfo ci){if(level instanceof ServerLevel server&&fluid.is(FluidTags.WATER)&&!old.getFluidState().isSource()&&level.getFluidState(pos).is(FluidTags.WATER)&&WaterOrigin.artificial(server,pos.relative(direction.getOpposite())))WaterOrigin.mark(server,pos);}
}
