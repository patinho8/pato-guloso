package br.mareeletrica.test;

import br.mareeletrica.Frogs;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.frog): adopt a tiny frog, put it on the head, stash/unstash (logout), let it follow. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualFrog {
 static int tick;static Frogs.TinyFrog frog;
 static boolean on(){return Boolean.getBoolean("mareeletrica.frog");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"frog_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[FROG] "+c.getString()));}
 static void log(String s){System.out.println("[FROG] "+s);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(tick==40)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SURVIVAL);p.teleportTo(l,.5,-60,.5,180,10);
   frog=Frogs.ENTITY.get().create(l);frog.moveTo(.5,-60,-1.5,0,0);frog.setVariant(1);l.addFreshEntity(frog);});
  if(tick==60)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SLIME_BALL,64));
   int n=0;while(!frog.isTame()&&n<40){frog.interact(p,InteractionHand.MAIN_HAND);n++;}log("tame="+frog.isTame()+" after "+n+" balls owner="+(frog.getOwner()==p));});
  if(tick==70)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);
   frog.interact(p,InteractionHand.MAIN_HAND);p.setShiftKeyDown(false);log("on head="+(frog.getVehicle()==p));});
  if(tick==80){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
  if(tick==100){log("client head="+(Frogs.onHead(mc.player)!=null));shot(mc,"cabeca_frente");}
  if(tick==105){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);}
  if(tick==125)shot(mc,"cabeca_costas");
  if(tick==130)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);Frogs.stash(p);log("stashed head="+(Frogs.onHead(p)!=null)+" data="+p.getPersistentData().contains("MareFrogOnHead"));Frogs.unstash(p);
   var f=Frogs.onHead(p);log("restored head="+(f!=null)+(f!=null?" tame="+f.isTame()+" variant="+f.variant():""));frog=f;});
  if(tick==150)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);frog.stopRiding();frog.moveTo(p.getX(),p.getY(),p.getZ()-1);p.teleportTo(p.getX()+8,p.getY(),p.getZ());});
  if(tick==250)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);log("follow dist="+String.format("%.1f",frog.distanceTo(p)));});
  if(tick==255){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setXRot(-40);}
  if(tick==270)shot(mc,"primeira_pessoa");
  if(tick==280){log("DONE");mc.stop();}
 }
}
