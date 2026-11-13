package br.mareeletrica;

import net.minecraft.core.*;
import net.minecraft.tags.*;
import net.minecraft.world.level.material.FluidState;

/** Four independent water conditions, sampled every ten ticks without loading chunks. */
public final class HydroGenerators {
 private HydroGenerators(){}
 public static float speed(Kind kind){return kind==Kind.GIANT_WHEEL?2.5F:kind==Kind.CURRENT_TURBINE?12F:kind==Kind.FALL_TURBINE?8F:6F;}
 public static BlockPos local(MachineEntity m,int x,int y,int z){Direction f=m.getBlockState().getValue(MachineBlock.FACING);return m.getBlockPos().relative(f.getClockWise(),x).relative(f.getOpposite(),z).above(y);}
 private static FluidState fluid(MachineEntity m,int x,int y,int z){BlockPos p=local(m,x,y,z);return WaterOrigin.eligible(m.getLevel(),p)?m.getLevel().getFluidState(p):net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState();}
 private static boolean water(MachineEntity m,int x,int y,int z){return fluid(m,x,y,z).is(FluidTags.WATER);}
 public static int potential(MachineEntity m){
  int wet=0;
  if(m.kind==Kind.GIANT_WHEEL){
   // 0.90.0: water touching the bottom of the wheel keeps it turning again (crank and cranker still work on dry land)
   int width=LargeMachineStructure.width(m.kind,m.getBlockState());
   for(int x=0;x<width;x++)for(int y=0;y<2;y++)for(int z=-1;z<=width;z++)if(water(m,x,y,z))wet++;
   if(wet>0)m.drive=Math.max(m.drive,120);
   if(m.drive<=0){m.status=56;return 0;}
   return 14000+Math.min(8,wet)*1500;}
  if(m.kind==Kind.GIANT_WHEEL||m.kind==Kind.CURRENT_TURBINE){
   int width=LargeMachineStructure.width(m.kind,m.getBlockState()),height=m.kind==Kind.GIANT_WHEEL?2:3;
   for(int x=0;x<width;x++)for(int y=0;y<height;y++)for(int z=-1;z<=width;z++)if(water(m,x,y,z))wet++;
   if(wet==0){m.status=52;return 0;}
   return m.kind==Kind.GIANT_WHEEL?16000+Math.min(8,wet)*2000:8000+Math.min(9,wet)*2000;
  }
  int column=0;while(column<16&&water(m,1,3+column,1))column++;
  if(m.kind==Kind.FALL_TURBINE){
   var f=fluid(m,1,3,1);if(column<2||!f.is(FluidTags.WATER)||!f.hasProperty(net.minecraft.world.level.material.FlowingFluid.FALLING)||!f.getValue(net.minecraft.world.level.material.FlowingFluid.FALLING)){m.status=54;return 0;}
   return 6000*Math.min(8,column);
  }
  for(int y=0;y<3;y++)for(int x=0;x<3;x++){if(water(m,x,y,-1))wet++;if(water(m,x,y,3))wet++;if(water(m,-1,y,x))wet++;if(water(m,3,y,x))wet++;}
  if(column<6||wet<24){m.status=55;return 0;}
  return 8000*Math.min(10,column);
 }
 /** A turning giant wheel turns the wheels coupled in front of and behind it (same axle), which pass it on down the line.
  *  Each link loses a little drive, so a chain can never keep itself going without a crank or cranker at one end. */
 private static void chain(MachineEntity m){
  if(m.drive<=40)return;
  for(boolean front:new boolean[]{true,false}){
   if(!WheelConnections.connected(m,front))continue;
   var next=WheelConnections.wheel(m.getLevel(),WheelConnections.port(m,front));
   if(next==null||next==m)continue;
   int give=m.drive-30;
   if(next.drive<give){next.drive=give;next.setChanged();}
  }
 }
 /** splashes where the paddles hit the water and the creak of the big wooden wheel */
 private static void effects(MachineEntity m){
  if(!(m.getLevel() instanceof net.minecraft.server.level.ServerLevel sl))return;
  int w=LargeMachineStructure.width(m.kind,m.getBlockState());var r=sl.random;
  for(int i=0;i<2;i++){BlockPos p=local(m,r.nextInt(w),0,r.nextInt(w));if(sl.getFluidState(p).is(FluidTags.WATER)||sl.getFluidState(p.below()).is(FluidTags.WATER))
   sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,p.getX()+.5,p.getY()+.9,p.getZ()+.5,6,.4,.1,.4,.1);}
  if(r.nextInt(4)==0){BlockPos c=local(m,w/2,w/2,w/2);sl.playSound(null,c,net.minecraft.sounds.SoundEvents.WOOD_STEP,net.minecraft.sounds.SoundSource.BLOCKS,.9F,.45F+r.nextFloat()*.15F);}
 }
 public static boolean tick(MachineEntity m){
  PowerNetwork.send(m,false,128000,Kind::core);PowerNetwork.send(m,true,128000,k->k==Kind.PURIFIER);
  int rate=potential(m);if(rate==0){m.lastRate=0;return false;}
  if(m.kind==Kind.GIANT_WHEEL){m.drive=Math.max(0,m.drive-10);chain(m);effects(m);}
  // 0.90.0 fix: the giant wheel made 'dirty' (sea) energy everywhere; now only in the ocean, like the others
  boolean ocean=m.getLevel().getBiome(local(m,m.kind.structureWidth()/2,1,m.kind.structureWidth()/2)).is(BiomeTags.IS_OCEAN);
  int generated=(ocean?m.dirty:m.clean).add(rate);m.lastRate=generated*2;m.status=generated>0?1:10;m.waterProfile=ocean?2:1;
  return generated>0;
 }
}
