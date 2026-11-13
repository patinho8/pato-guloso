package br.mareeletrica;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** Power controller (hands out energy with a limit and an on/off cycle) and the automatic cranker (spends that energy to turn a wheel). */
public final class ControlMachines {
 private ControlMachines(){}
 /** Most energy the controller can push per 10-tick window at 100 %. */
 public static final int WINDOW_MAX=40_000;
 /** Energy an automatic cranker spends per 10-tick window; it adds 12 drive ticks, so the wheel returns ~84 % of it at best. */
 public static final int CRANK_COST=16_000,CRANK_DRIVE=12,DRIVE_CAP=1200;

 public static boolean poweredNow(MachineEntity m){
  if(m.ctrlOn<=0||m.ctrlOff<=0)return true;   // 0 = always on
  long period=(m.ctrlOn+m.ctrlOff)*20L;
  return m.getLevel().getGameTime()%period<m.ctrlOn*20L;
 }
 public static boolean controller(MachineEntity m){
  if(!poweredNow(m)){m.status=57;m.lastRate=0;return false;}
  int budget=Math.max(0,WINDOW_MAX*Math.max(0,Math.min(100,m.ctrlLimit))/100);
  int sent=PowerNetwork.send(m,false,budget,k->k==Kind.AUTO_CRANK);
  m.lastRate=sent*2;m.status=sent>0?1:(m.clean.stored()>0?10:2);
  return sent>0;
 }
 public static boolean crank(MachineEntity m){
  MachineEntity wheel=wheelNear(m.getLevel(),m);
  if(wheel==null){m.status=58;return false;}
  if(wheel.drive>=DRIVE_CAP/2){m.status=1;return false;}   // the wheel already has plenty of motion
  if(m.clean.stored()<CRANK_COST){m.status=2;return false;}
  m.clean.pay(CRANK_COST);wheel.drive=Math.min(DRIVE_CAP,wheel.drive+CRANK_DRIVE);wheel.setChanged();
  m.lastRate=CRANK_COST*2;m.status=1;return true;
 }
 /** Right-click on a giant wheel that has a crank: turn it by hand. */
 public static boolean turnCrank(Level level,net.minecraft.core.BlockPos pos,net.minecraft.world.entity.player.Player player){
  var wheel=PotabilizerStructure.machineAt(level,pos);
  if(wheel==null||wheel.kind!=Kind.GIANT_WHEEL||!wheel.crank)return false;
  if(!level.isClientSide){
   wheel.drive=Math.min(DRIVE_CAP,wheel.drive+100);wheel.setChanged();
   player.causeFoodExhaustion(.35F);
   level.playSound(null,pos,net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_OPEN,net.minecraft.sounds.SoundSource.BLOCKS,.6F,.6F+level.random.nextFloat()*.2F);
   player.displayClientMessage(net.minecraft.network.chat.Component.literal("Roda girando por mais "+(wheel.drive/20)+" s."),true);
  }
  return true;
 }
 public static MachineEntity wheelNear(Level level,MachineEntity from){
  // 0.90.0: the big crank motor touches the wheel with any of its blocks
  int n=from.kind.large()?LargeMachineStructure.parts(from.kind,from.getBlockState()):1;
  for(int i=0;i<n;i++){var w=wheelAt(level,i==0?from.getBlockPos():LargeMachineStructure.position(from,i));if(w!=null)return w;}
  return null;}
 public static MachineEntity wheelAt(Level level,net.minecraft.core.BlockPos pos){
  for(Direction d:Direction.values()){
   var e=PotabilizerStructure.machineAt(level,pos.relative(d));
   if(e!=null&&e.kind==Kind.GIANT_WHEEL)return e;
  }
  return null;
 }
}
