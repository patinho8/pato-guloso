package br.mareeletrica.test;

import br.mareeletrica.*;
import br.mareeletrica.client.RifleRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import br.mareeletrica.*;

/** Test only (-Dmareeletrica.patobanho): bathtub, ducks bathing with the owner, fetch mode, alarm. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualPato {
 static int tick;static BlockPos o;static java.util.List<Pato.Duck> ducks=new java.util.ArrayList<>();
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"pato_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.patobanho"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(tick==50&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setInvulnerable(true);o=p.blockPosition().atY(200);l.setDayTime(6000);
   l.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
   for(int x=-10;x<=10;x++)for(int z=-14;z<=10;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<6;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(o.offset(x,6,z),Blocks.GLOWSTONE.defaultBlockState());}
   var st=Bath.BATH.get().defaultBlockState().setValue(Bath.BathBlock.FACING,net.minecraft.core.Direction.SOUTH).setValue(Bath.FILLED,true);
   l.setBlockAndUpdate(o.offset(0,0,-3),st);l.setBlockAndUpdate(o.offset(0,0,-4),st.setValue(Bath.PART,net.minecraft.world.level.block.state.properties.BedPart.HEAD));
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+2.5,180,20);
   for(int i=0;i<2;i++){var d=Pato.ENTITY.get().create(l);d.moveTo(o.getX()+2+i,o.getY(),o.getZ()+1,0,0);d.tame(p);d.setMode(Pato.FOLLOW);d.setLook(i,i==0?2:0,0);d.addXp(i==0?240:90);l.addFreshEntity(d);ducks.add(d);}});
  if(tick==90)shot("banheira");
  if(tick==95)srv(()->{var p=sp();var l=p.serverLevel();var pos=o.offset(0,0,-3);var s=l.getBlockState(pos);s.useWithoutItem(l,p,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.core.Direction.UP,pos,false));});
  if(tick==100)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==220){shot("banho_com_patos");System.out.println("[PATO] in bath: "+ducks.stream().filter(d->d.flag(Pato.Duck.BATH)).count());}
  if(tick==225)srv(()->{for(var d:ducks)System.out.println("[PATO] server bath "+d.flag(Pato.Duck.BATH)+" pos "+d.position());});
  if(tick==240){mc.options.setCameraType(CameraType.FIRST_PERSON);srv(()->{var p=sp();p.stopRiding();p.teleportTo(p.serverLevel(),o.getX()+.5,o.getY(),o.getZ()+4.5,180,30);
   for(var d:ducks)d.setMode(Pato.COLLECT);var l=p.serverLevel();
   for(int i=0;i<3;i++){var it=new net.minecraft.world.entity.item.ItemEntity(l,o.getX()-6+i*1.5,o.getY()+.2,o.getZ()+6,new ItemStack(net.minecraft.world.item.Items.DIAMOND,2+i));it.setDeltaMovement(0,0,0);l.addFreshEntity(it);}});}
  if(tick==290)shot("buscando");
  if(tick==330||tick==420)srv(()->{for(var d:ducks)System.out.println("[PATO] t"+tick+" mode "+d.mode()+" lv "+d.duckLevel()+" bath "+d.flag(Pato.Duck.BATH)+" held "+d.getMainHandItem()+" pos "+d.position()+" nav "+d.getNavigation().isInProgress());System.out.println("[PATO] items "+sp().serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,sp().getBoundingBox().inflate(20)).size()+" player "+sp().position());});
  if(tick==520){srv(()->System.out.println("[PATO] diamonds in inventory "+sp().getInventory().countItem(net.minecraft.world.item.Items.DIAMOND)));shot("trouxe");}
  if(tick==530)srv(()->{var p=sp();var l=p.serverLevel();for(var d:ducks)d.setMode(Pato.FOLLOW);var z=net.minecraft.world.entity.EntityType.ZOMBIE.create(l);z.moveTo(o.getX()+.5,o.getY(),o.getZ()+9.5,0,0);l.addFreshEntity(z);});
  if(tick==545||tick==560)srv(()->{for(var d:ducks)System.out.println("[PATO] t"+tick+" lv "+d.duckLevel()+" mode "+d.mode()+" bathT "+d.bathTicks+" cd "+d.alarmCd+" dist "+d.distanceTo(sp()));var z=sp().serverLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.Zombie.class,sp().getBoundingBox().inflate(30));for(var zz:z)System.out.println("[PATO] zombie at "+zz.position()+" d "+zz.distanceTo(sp()));});
  if(tick==575){shot("alarme");srv(()->{var z=sp().serverLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.Zombie.class,sp().getBoundingBox().inflate(30));for(var zz:z)System.out.println("[PATO] zombie glowing "+zz.getId()+" "+zz.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)+" "+zz.isCurrentlyGlowing());});}
  if(tick==580)System.out.println("[PATO] DONE");
 }
}
