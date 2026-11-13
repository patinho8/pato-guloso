package br.mareeletrica.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.invdmt): the DMT slot under the camouflage column. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualInvDmt {
 static int tick;
 static boolean on(){return Boolean.getBoolean("mareeletrica.invdmt");}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"inv_"+n+".png",mc.getMainRenderTarget(),c->{});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==50)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SURVIVAL);p.getInventory().clearContent();});
  if(tick==70)mc.setScreen(new InventoryScreen(mc.player));
  if(tick==80)org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),5,5);
  if(tick==85)shot("survival");
  if(tick==90){mc.setScreen(null);srv.execute(()->srv.getPlayerList().getPlayers().get(0).setGameMode(GameType.CREATIVE));}
  if(tick==110)mc.setScreen(new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player,mc.player.connection.enabledFeatures(),true));
  if(tick==118&&mc.screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen cs){}
  if(tick==130)shot("creative");
  if(tick==132){mc.setScreen(null);System.out.println("[INV] DONE");}
 }
}
