package br.mareeletrica.test;

import br.mareeletrica.*;
import br.mareeletrica.client.ArcadeScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/** Test only (-Dmareeletrica.casa): furniture room, the duck potion, the new arcade games. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualCasa {
 static int tick;static BlockPos o;
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"casa_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void put(BlockPos p,BlockState s){var l=sp().serverLevel();l.setBlockAndUpdate(p,s);s.getBlock().setPlacedBy(l,p,s,sp(),net.minecraft.world.item.ItemStack.EMPTY);}
 static BlockState f(net.minecraft.world.level.block.Block b,Direction d){return b.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,d);}
 static void key(int k){var s=Minecraft.getInstance().screen;if(s!=null)s.keyPressed(k,0,0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.casa"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20&&tick<200)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);o=p.blockPosition().atY(150);l.setDayTime(13000);l.setWeatherParameters(6000,0,false,false);
   for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.OAK_PLANKS.defaultBlockState());for(int y=0;y<5;y++)l.setBlockAndUpdate(o.offset(x,y,z),(Math.abs(x)==6||Math.abs(z)==6)&&y<4?Blocks.WHITE_TERRACOTTA.defaultBlockState():Blocks.AIR.defaultBlockState());}
   for(int x=-1;x<=1;x++)put(o.offset(x,0,2),f(Furniture.SOFA.get(),Direction.NORTH));
   put(o.offset(-3,0,1),f(Furniture.ARMCHAIR.get(),Direction.EAST));
   put(o.offset(0,0,-3),f(Furniture.TABLE.get(),Direction.NORTH));put(o.offset(0,1,-3),f(Furniture.TV.get(),Direction.SOUTH));
   put(o.offset(3,0,-3),f(Furniture.CHAIR.get(),Direction.WEST));put(o.offset(2,0,-3),f(Furniture.TABLE.get(),Direction.NORTH));
   put(o.offset(-3,0,-4),f(Furniture.LAMP.get(),Direction.NORTH));put(o.offset(2,0,2),f(Furniture.LAMP.get(),Direction.NORTH));
   put(o.offset(-5,0,-5),f(Furniture.FRIDGE.get(),Direction.SOUTH));
   put(o.offset(4,0,3),Furniture.BED.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));
   put(o.offset(-5,0,4),f(Fun.ARCADE.get(),Direction.EAST));
   p.teleportTo(l,o.getX()+4.5,o.getY()+2.2,o.getZ()+5.0,140,30);});
  if(tick>=62&&tick%20==0)srv.execute(()->{var p=sp();for(var en:p.serverLevel().getEntities(p,p.getBoundingBox().inflate(40),x->x instanceof net.minecraft.world.entity.animal.Animal))en.discard();});
  if(tick==95)shot("sala");
  if(tick==97)srv.execute(()->{var p=sp();p.teleportTo(p.serverLevel(),o.getX()-4.5,o.getY()+2.4,o.getZ()+4.5,215,25);});
  if(tick==110)shot("sala2");
  // sit on the sofa and watch TV
  if(tick==112)srv.execute(()->{var p=sp();var s=o.offset(0,0,2);p.serverLevel().getBlockState(s).useWithoutItem(p.serverLevel(),p,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(s),Direction.UP,s,false));});
  if(tick==118){mc.player.setYRot(180);mc.player.setXRot(10);}
  if(tick==126)shot("sofa_tv");
  if(tick==128)mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
  if(tick==138)shot("sentado");
  if(tick==140){mc.options.setCameraType(CameraType.FIRST_PERSON);srv.execute(()->{var p=sp();p.stopRiding();p.teleportTo(p.serverLevel(),o.getX()+.5,o.getY(),o.getZ()-.5,0,0);p.addEffect(new MobEffectInstance(Pato.DUCK_EFFECT,600));});}
  if(tick==150)mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
  if(tick==162)shot("pato_jogador");
  if(tick==164)mc.options.setCameraType(CameraType.FIRST_PERSON);
  if(tick==166)shot("pato_primeira");
  // arcade games
  if(tick==200){ArcadeScreen.open(o.offset(-5,0,4));}
  if(tick==206)shot("menu");
  if(tick==208){key(GLFW.GLFW_KEY_4);key(GLFW.GLFW_KEY_SPACE);}
  if(tick>210&&tick<260&&tick%6==0){key(tick%12==0?GLFW.GLFW_KEY_LEFT:GLFW.GLFW_KEY_UP);if(tick%18==0)key(GLFW.GLFW_KEY_SPACE);}
  if(tick==262)shot("blocos");
  if(tick==264){key(GLFW.GLFW_KEY_BACKSPACE);key(GLFW.GLFW_KEY_5);key(GLFW.GLFW_KEY_SPACE);}
  if(tick>264&&tick<300&&tick%9==0)key(GLFW.GLFW_KEY_SPACE);
  if(tick==296)shot("flappy");
  if(tick==302){key(GLFW.GLFW_KEY_BACKSPACE);key(GLFW.GLFW_KEY_6);key(GLFW.GLFW_KEY_SPACE);}
  if(tick>302&&tick<345&&tick%15==0)key(tick%30==0?GLFW.GLFW_KEY_LEFT:GLFW.GLFW_KEY_RIGHT);
  if(tick==345)shot("estrada");
  if(tick==350){mc.setScreen(null);System.out.println("[CASA] DONE");}
 }
}
