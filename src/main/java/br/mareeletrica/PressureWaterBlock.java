package br.mareeletrica;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
/** Maps all 15 flowing levels instead of the vanilla seven. */
public final class PressureWaterBlock extends LiquidBlock{
 public PressureWaterBlock(Properties p){super(MareEletrica.PRESSURIZED_WATER.get(),p);}
 @Override protected FluidState getFluidState(BlockState state){int n=state.getValue(LEVEL);return n==0?MareEletrica.PRESSURIZED_WATER.get().getSource(false):MareEletrica.FLOWING_PRESSURIZED_WATER.get().getFlowing(n==15?15:15-n,n==15);}
}
