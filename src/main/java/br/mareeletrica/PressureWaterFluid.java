package br.mareeletrica;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.fluids.FluidType;
/** Finite gradient: source + fourteen horizontal steps, with water physics. */
public abstract class PressureWaterFluid extends WaterFluid {
 public static final IntegerProperty AMOUNT=IntegerProperty.create("pressure",1,15);
 public Fluid getFlowing(){return MareEletrica.FLOWING_PRESSURIZED_WATER.get();}
 public Fluid getSource(){return MareEletrica.PRESSURIZED_WATER.get();}
 public FluidType getFluidType(){return net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value();}
 public boolean isSame(Fluid f){return f==getSource()||f==getFlowing();}
 public FluidState getFlowing(int amount,boolean falling){return getFlowing().defaultFluidState().setValue(AMOUNT,Math.max(1,Math.min(15,amount))).setValue(FALLING,falling);}
 protected boolean canConvertToSource(Level l){return false;}
 public boolean canConvertToSource(FluidState s,Level l,BlockPos p){return false;}
 public float getOwnHeight(FluidState s){return Math.min(1,s.getAmount()/16F);}
 public BlockState createLegacyBlock(FluidState s){return MareEletrica.PRESSURIZED_WATER_BLOCK.get().defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL,s.isSource()?0:s.getValue(FALLING)?15:15-s.getAmount());}
 private boolean open(Level l,BlockPos from,BlockState a,Direction dir,BlockPos to,BlockState b){return !Shapes.mergedFaceOccludes(a.getCollisionShape(l,from),b.getCollisionShape(l,to),dir);}
 protected FluidState getNewLiquid(Level l,BlockPos p,BlockState state){
  int highest=0;for(Direction d:Direction.Plane.HORIZONTAL){BlockPos n=p.relative(d);if(!l.hasChunkAt(n))continue;BlockState s=l.getBlockState(n);FluidState f=s.getFluidState();if(isSame(f.getType())&&open(l,p,state,d,n,s))highest=Math.max(highest,f.getAmount());}
  BlockPos above=p.above();if(l.hasChunkAt(above)){BlockState s=l.getBlockState(above);if(isSame(s.getFluidState().getType())&&open(l,p,state,Direction.UP,above,s))return getFlowing(15,true);}
  int amount=highest-1;return amount<=0?Fluids.EMPTY.defaultFluidState():getFlowing(amount,false);
 }
 public static final class Source extends PressureWaterFluid{public int getAmount(FluidState s){return 15;}public boolean isSource(FluidState s){return true;}}
 public static final class Flowing extends PressureWaterFluid{
  protected void createFluidStateDefinition(StateDefinition.Builder<Fluid,FluidState> b){super.createFluidStateDefinition(b);b.add(AMOUNT);}
  public int getAmount(FluidState s){return s.getValue(AMOUNT);}public boolean isSource(FluidState s){return false;}
 }
}
