package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.ak): the player's imported AK-47 model — hand, third person, aiming, optic + suppressor, skins in the hotbar. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualAk {
 static int tick;
 static ItemStack ak(int skin,boolean optic,int sup){var s=new ItemStack(Arsenal.AK47.get());var t=RifleLoadout.tag(s);t.putInt("Sticker",skin);if(optic){t.putBoolean("HasOptic",true);t.putInt("OpticVariant",2);}if(sup>0)t.putInt("Suppressor",sup);s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,n+".png",mc.getMainRenderTarget(),c->{});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.ak"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);var l=p.serverLevel();
   var at=p.blockPosition().atY(150);for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){l.setBlockAndUpdate(at.offset(x,-1,z),net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<3;y++)l.setBlockAndUpdate(at.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
   p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,0,5);p.getInventory().clearContent();
   for(int i=0;i<9;i++)p.getInventory().setItem(i,ak(new int[]{0,1,2,3,4,7,8,11,12}[i],false,0));
   l.setDayTime(6000);});
  if(tick>=62&&tick%10==0)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);for(var en:p.serverLevel().getEntities(p,p.getBoundingBox().inflate(40),x->!(x instanceof net.minecraft.world.entity.player.Player)))en.discard();});
  if(tick==61)System.out.println("[AK] LEFT "+CreativeTabs.leftovers());
  if(tick==85)shot("ak_fp");
  if(tick==88)mc.options.keyUse.setDown(true);
  if(tick==100)shot("ak_aim");
  if(tick==102)mc.options.keyUse.setDown(false);
  if(tick==106){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.player.setYRot(90);}
  if(tick==118)shot("ak_side");
  if(tick==120)mc.player.setYRot(30);
  if(tick==130)shot("ak_front");
  if(tick==132){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(0);srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.getInventory().setItem(0,ak(0,true,2));});}
  if(tick==145)shot("ak_optic_fp");
  if(tick==147)mc.options.keyUse.setDown(true);
  if(tick==160)shot("ak_optic_aim");
  if(tick==162)mc.options.keyUse.setDown(false);
  if(tick==170)System.out.println("[AK] DONE");
 }
}
