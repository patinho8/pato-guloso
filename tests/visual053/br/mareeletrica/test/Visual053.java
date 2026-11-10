package br.mareeletrica.test;

import br.mareeletrica.Multiblock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Visual check (test only): builds a scene with the 0.53 blocks and vehicles and takes screenshots. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class Visual053 {
 static int tick=-1,shot=0;static int baseY;
 // x,y,z,yaw,pitch,time,name
 static final double[][] SHOTS={{6,1.6,13.5,0,25,6000},{6,1.6,13.5,0,25,6000},{6,1.6,13.5,0,25,6000},{6,1.6,13.5,0,25,6000},{3,-.2,21.8,0,8,6000}};
 static final String[] NAMES={"battle1","battle2","battle3","battle4","breeds"};
 static ServerLevel level(){var s=Minecraft.getInstance().getSingleplayerServer();return s==null?null:s.overworld();}
 static BlockState st(String id){return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("mareeletrica",id)).defaultBlockState();}
 static void put(ServerLevel l,int x,int z,String id){
  var p=new BlockPos(x,baseY,z);var s=st(id);
  if(s.hasProperty(BlockStateProperties.HORIZONTAL_FACING))s=s.setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH);
  l.setBlock(p,s,3);Multiblock.place(l,p,s);
 }
 static void spawn(ServerLevel l,String id,double x,double z,float yaw){
  var t=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("mareeletrica",id));Entity e=t.create(l);
  if(e==null){System.out.println("[V053] no entity "+id);return;}e.moveTo(x,baseY,z,yaw,0);l.addFreshEntity(e);
 }
 static void chickens(ServerLevel l){
  var pl=l.getServer().getPlayerList().getPlayers().get(0);
  var a=br.mareeletrica.FightChickens.ENTITY.get().create(l);a.moveTo(6.5,baseY,15.5,180,0);a.tame(pl);a.setNoAi(true);a.setYHeadRot(180);a.setYBodyRot(180);l.addFreshEntity(a);
  a.setBreed(br.mareeletrica.ChickenTypes.Breed.FOGO);
  // rooster ring with a training battle
  var rp=new BlockPos(6,baseY,19);var rs=br.mareeletrica.ChickenBattle.BLOCK.get().defaultBlockState();l.setBlock(rp,rs,3);br.mareeletrica.Multiblock.place(l,rp,rs);
  if(l.getBlockEntity(rp) instanceof br.mareeletrica.ChickenBattle.Ring r)r.train(pl,a);
  // one of each breed in a row for a look at the colours
  var all=br.mareeletrica.ChickenTypes.Breed.values();
  for(int i=0;i<all.length;i++){var e=br.mareeletrica.FightChickens.ENTITY.get().create(l);e.setBreed(all[i]);e.moveTo(-4+i*1.6,baseY,24.5,180,0);e.setNoAi(true);e.setYBodyRot(180);e.setYHeadRot(180);l.addFreshEntity(e);}
 }
 static void build(ServerLevel l){
  baseY=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,0,0);
  for(int x=-12;x<=34;x++)for(int z=-24;z<=20;z++)for(int y=baseY;y<baseY+12;y++)l.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
  put(l,0,0,"radar");put(l,6,0,"command_panel");put(l,13,0,"soccer_goal");put(l,20,0,"basketball_hoop");put(l,27,0,"scoreboard");
  String[] c={"white","red","orange","yellow","lime","green","cyan","blue","purple","pink"};
  for(int i=0;i<c.length;i++)put(l,i*2,11,"led_lamp_"+c[i]);// stone wall in front of the lamps
  for(int x=-2;x<=20;x++)for(int y=0;y<4;y++)l.setBlock(new BlockPos(x,baseY+y,9),Blocks.STONE_BRICKS.defaultBlockState(),2);
  put(l,26,10,"vehicle_lift");put(l,-6,10,"helipad");put(l,-6,4,"alarm_siren");
  spawn(l,"helicopter",0,-12,0);spawn(l,"battle_tank",12,-12,0);spawn(l,"sport_ball",13.5,2.5,0);{var a=net.minecraft.world.entity.EntityType.ARMOR_STAND.create(l);a.moveTo(15,baseY,1.5,180,0);l.addFreshEntity(a);var b=net.minecraft.world.entity.EntityType.ARMOR_STAND.create(l);b.moveTo(21.5,baseY,2.5,180,0);l.addFreshEntity(b);}spawn(l,"delivery_drone",4,4,0);
  var it=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mareeletrica","delivery_drone"));
  chickens(l);System.out.println("[V053] scene built at y="+baseY);
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(Boolean.getBoolean("mareeletrica.escudo2")||Boolean.getBoolean("mareeletrica.escudo")||Boolean.getBoolean("mareeletrica.snk")||Boolean.getBoolean("mareeletrica.banho084")||Boolean.getBoolean("mareeletrica.club")||Boolean.getBoolean("mareeletrica.acc")||Boolean.getBoolean("mareeletrica.patobanho")||Boolean.getBoolean("mareeletrica.ombro")||Boolean.getBoolean("mareeletrica.faca")||Boolean.getBoolean("mareeletrica.recarga")||Boolean.getBoolean("mareeletrica.c4")||Boolean.getBoolean("mareeletrica.bomba")||Boolean.getBoolean("mareeletrica.deagle")||Boolean.getBoolean("mareeletrica.mira")||Boolean.getBoolean("mareeletrica.galeria")||Boolean.getBoolean("mareeletrica.chefe")||Boolean.getBoolean("mareeletrica.tinta")||Boolean.getBoolean("mareeletrica.casa")||Boolean.getBoolean("mareeletrica.pato")||Boolean.getBoolean("mareeletrica.sniper")||Boolean.getBoolean("mareeletrica.ak")||Boolean.getBoolean("mareeletrica.fun067")||Boolean.getBoolean("mareeletrica.big066")||Boolean.getBoolean("mareeletrica.skate")||Boolean.getBoolean("mareeletrica.pool")||Boolean.getBoolean("mareeletrica.newstuff")||Boolean.getBoolean("mareeletrica.cockpit")||Boolean.getBoolean("mareeletrica.casino")||Boolean.getBoolean("mareeletrica.war")||Boolean.getBoolean("mareeletrica.audit")||Boolean.getBoolean("mareeletrica.pilot")||Boolean.getBoolean("mareeletrica.fly")||Boolean.getBoolean("mareeletrica.frog")||Boolean.getBoolean("mareeletrica.sport")||Boolean.getBoolean("mareeletrica.robot"))return;
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;var l=level();if(l==null)return;
  tick++;
  if(tick==40){mc.options.hideGui=false;mc.getSingleplayerServer().execute(()->{build(l);var sp=l.getServer().getPlayerList().getPlayers().get(0);sp.setGameMode(GameType.SPECTATOR);});}
  int t0=160+shot*80;
  if(shot<SHOTS.length&&tick==t0){var s=SHOTS[shot];mc.getSingleplayerServer().execute(()->{l.setDayTime((long)s[5]);var sp=l.getServer().getPlayerList().getPlayers().get(0);sp.teleportTo(l,s[0],baseY+s[1],s[2],(float)s[3],(float)s[4]);});}
  if(tick%70==0&&mc.screen instanceof br.mareeletrica.client.BattleScreen bs)net.neoforged.neoforge.network.PacketDistributor.sendToServer(new br.mareeletrica.ChickenBattle.Choose(bs.pos(),(tick/70)%4));
  if(shot<SHOTS.length&&tick==t0+60){mc.getSingleplayerServer().execute(()->{for(var ch:l.getEntitiesOfClass(br.mareeletrica.FightChickens.FightChicken.class,new net.minecraft.world.phys.AABB(-20,-70,-20,40,0,40)))System.out.println("[V053] chicken "+ch.position()+" tgt="+ch.getTarget()+" hp="+ch.getHealth()+" ko="+ch.ko()+" sit="+ch.isOrderedToSit()+" nav="+ch.getNavigation().isDone()+" goals="+ch.goalSelector.getAvailableGoals().stream().filter(g->g.isRunning()).map(g->g.getGoal().getClass().getSimpleName()).toList());});Screenshot.grab(mc.gameDirectory,"v053_"+NAMES[shot]+".png",mc.getMainRenderTarget(),c->System.out.println("[V053] "+c.getString()));shot++;}
  if(shot>=SHOTS.length&&tick==t0+20){System.out.println("[V053] DONE");mc.stop();}
 }
}
