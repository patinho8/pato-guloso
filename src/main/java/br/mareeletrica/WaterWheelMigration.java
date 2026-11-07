package br.mareeletrica;
import net.minecraft.world.level.block.Blocks;
/** Converts old 5-cube wheels without dropping their controller or stored energy. */
public final class WaterWheelMigration {
 private WaterWheelMigration(){}
 public static boolean upgrade(MachineEntity m){
  var level=m.getLevel();var state=m.getBlockState();if(state.getValue(MachineBlock.EXPANDED))return true;
  var root=m.getBlockPos();var facing=state.getValue(MachineBlock.FACING);
  for(int part=1;part<125;part++)if(!level.hasChunkAt(LargeMachineStructure.position(root,facing,part,Kind.GIANT_WHEEL,false)))return false;
  var next=state.setValue(MachineBlock.EXPANDED,true);level.setBlock(root,next,3);
  for(int part=1;part<125;part++){
   var pos=LargeMachineStructure.position(root,facing,part,Kind.GIANT_WHEEL,false);var old=level.getBlockState(pos);
   if(old.getBlock() instanceof LargeMachinePartBlock b&&b.kind==Kind.GIANT_WHEEL&&!old.getValue(MachineBlock.EXPANDED)
    &&old.getValue(MachineBlock.FACING)==facing&&LargeMachinePartBlock.part(old)==part)
    level.setBlock(pos,old.getValue(MachineBlock.WATERLOGGED)?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
  }
  LargeMachineStructure.form(level,root,next);m.setChanged();return true;
 }
}
