package br.mareeletrica;
import net.minecraft.core.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public final class WaterShaftBlock extends CableBlock {
 public WaterShaftBlock(Properties p){super(p,true);}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
  VoxelShape shape=box(4,4,4,12,12,12);
  for(var d:Direction.values())if(s.getValue(CONNECTIONS.get(d)))shape=Shapes.or(shape,switch(d){
   case DOWN->box(4,0,4,12,8,12);case UP->box(4,8,4,12,16,12);case NORTH->box(4,4,0,12,12,8);
   case SOUTH->box(4,4,8,12,12,16);case WEST->box(0,4,4,8,12,12);case EAST->box(8,4,4,16,12,12);
  });return shape;
 }
}
