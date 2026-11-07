package br.mareeletrica.test;

import br.mareeletrica.FieldKits;
import br.mareeletrica.SportsMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.sport): volleyball jump height and the volleyball court kit. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualSport {
 static int tick;static double base,top;
 static boolean on(){return Boolean.getBoolean("mareeletrica.sport");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"sport_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[SPORT] "+c.getString()));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(tick==40)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SURVIVAL);p.teleportTo(l,.5,-60,.5,0,0);
   FieldKits.buildAt(l,new BlockPos(-30,-61,40),Direction.SOUTH,FieldKits.Kind.VOLEI);});
  if(tick==60){base=mc.player.getY();top=base;mc.player.jumpFromGround();}
  if(tick>60&&tick<90)top=Math.max(top,mc.player.getY());
  if(tick==90){System.out.println("[SPORT] jump without shirt: "+String.format("%.2f",top-base));
   srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(SportsMode.ITEMS.get(SportsMode.Mode.VOLLEY.ordinal()*3).get()));});}
  if(tick==100){base=mc.player.getY();top=base;mc.player.jumpFromGround();}
  if(tick>100&&tick<130)top=Math.max(top,mc.player.getY());
  if(tick==130){System.out.println("[SPORT] jump with volleyball shirt: "+String.format("%.2f",top-base)+" hp="+mc.player.getHealth());
   srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SPECTATOR);p.teleportTo(l,-30.5,-30,52,0,90);});}
  if(tick==200)shot(mc,"quadra_volei");
  if(tick==210){System.out.println("[SPORT] DONE");mc.stop();}
 }
}
