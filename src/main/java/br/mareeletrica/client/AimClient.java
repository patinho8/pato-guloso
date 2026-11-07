package br.mareeletrica.client;
import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class AimClient {
 private static boolean active(){var p=Minecraft.getInstance().player;return p!=null&&AimRemoteItem.active(p.getMainHandItem());}
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered e){if(active()&&e.isAttack()){e.setCanceled(true);e.setSwingHand(false);PacketDistributor.sendToServer(new MachinePackets.AimControl(0,0));}}
 /** Holding attack keeps the Torre Sentinela firing. */
 @SubscribeEvent public static void hold(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.screen==null&&active()&&mc.options.keyAttack.isDown()&&mc.player.tickCount%2==0)PacketDistributor.sendToServer(new MachinePackets.AimControl(2,0));}
 @SubscribeEvent public static void scroll(InputEvent.MouseScrollingEvent e){if(active()){e.setCanceled(true);int n=AimRemoteItem.data(Minecraft.getInstance().player.getMainHandItem()).getInt("Barrel");PacketDistributor.sendToServer(new MachinePackets.AimControl(1,Math.floorMod(n+(e.getScrollDeltaY()>0?1:-1),7)));}}
 public static void hud(GuiGraphics g,DeltaTracker dt){if(!active())return;var mc=Minecraft.getInstance();int n=AimRemoteItem.data(mc.player.getMainHandItem()).getInt("Barrel");g.fill(8,8,g.guiWidth()-8,44,0xCC102633);int links=AimRemoteItem.links(AimRemoteItem.data(mc.player.getMainHandItem())).size();g.drawString(mc.font,"Mira 360° · "+links+" arma(s) · "+(n==0?"Todos os canhões":"Canhão "+n),14,14,0xA6F4FF,false);g.drawString(mc.font,"Olhe para mirar · Ataque: disparar (segure: metralhadora) · Roda: canhão · Direito: sair",14,28,0xDDDDDD,false);}
}
