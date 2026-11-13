package br.mareeletrica;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
/** 0.90.0: cells of the big (2x2x2 / 3x3x3) versions of ordinary machines; small part ranges keep the state count low. */
public final class ScaledMachinePartBlock {
 private ScaledMachinePartBlock(){}
 private static final IntegerProperty CELL8=IntegerProperty.create("part",0,7),CELL27=IntegerProperty.create("part",0,26);
 public static final class Two extends LargeMachinePartBlock {public Two(Kind k,Properties p){super(k,p);}@Override public IntegerProperty partProperty(){return CELL8;}}
 public static final class Three extends LargeMachinePartBlock {public Three(Kind k,Properties p){super(k,p);}@Override public IntegerProperty partProperty(){return CELL27;}}
}
