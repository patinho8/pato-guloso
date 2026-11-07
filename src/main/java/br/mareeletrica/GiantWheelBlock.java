package br.mareeletrica;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
public final class GiantWheelBlock extends MachineBlock {
 public GiantWheelBlock(Kind kind,Properties props){super(kind,props);registerDefaultState(defaultBlockState().setValue(WIDE,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(WIDE);}
}
