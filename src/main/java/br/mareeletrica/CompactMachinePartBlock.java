package br.mareeletrica;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
public final class CompactMachinePartBlock extends LargeMachinePartBlock {
 private static final IntegerProperty CELL=IntegerProperty.create("part",0,26);
 public CompactMachinePartBlock(Kind kind,Properties p){super(kind,p);}
 @Override public IntegerProperty partProperty(){return CELL;}
}
