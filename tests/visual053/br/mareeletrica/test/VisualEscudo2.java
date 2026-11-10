package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.escudo2): looks at the tactical shield from the front, back and in hand. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualEscudo2 {
 static int tick;static net.minecraft.core.BlockPos o;
 static boolean on(){return Boolean.getBoolean("mareeletrica.escudo2");}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"esc2_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void cmd(String c){var p=sp();p.server.getCommands().performPrefixedCommand(p.createCommandSourceStack().withPermission(4).withSuppressedOutput(),c);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.CREATIVE);o=p.blockPosition().atY(230);l.setDayTime(6000);l.setWeatherParameters(99999,0,false,false);
   for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){l.setBlockAndUpdate(o.offset(x,-1,z),net.minecraft.world.level.block.Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());for(int y=0;y<5;y++)l.setBlockAndUpdate(o.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+3.5,180,8);
   String it="{item:{id:\"mareeletrica:tactical_shield\",count:1},item_display:\"fixed\",brightness:{sky:15,block:15},transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[2.2f,2.2f,2.2f]},Rotation:[%sf,0f]}";
   cmd("summon minecraft:item_display "+(o.getX()-.9)+" "+(o.getY()+1.6)+" "+(o.getZ()+.5)+" "+String.format(it,"0"));
   cmd("summon minecraft:item_display "+(o.getX()+1.9)+" "+(o.getY()+1.6)+" "+(o.getZ()+.5)+" "+String.format(it,"180"));
   cmd("summon minecraft:item_display "+(o.getX()+.5)+" "+(o.getY()+1.6)+" "+(o.getZ()-1.5)+" "+String.format(it,"35"));
   p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Wounds.TAC_SHIELD.get()));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Arsenal.USP.get()));});
  if(tick>60&&tick<130){mc.player.setYRot(180);mc.player.setXRot(8);}
  if(tick==90){mc.options.hideGui=true;}
  if(tick==100)shot("vitrine");
  if(tick==104)mc.options.hideGui=false;
  if(tick==110)shot("mao");
  if(tick>=112&&tick<128)mc.options.keyUse.setDown(true);
  if(tick==126)shot("mao_levantado");
  if(tick==128)mc.options.keyUse.setDown(false);
  if(tick==130){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.options.hideGui=true;}
  if(tick==140)shot("terceira_frente");
  if(tick==142)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==150)shot("terceira_costas");
  if(tick==152){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.options.hideGui=false;System.out.println("[ESC2] DONE");}
 }
}
