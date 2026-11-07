package br.mareeletrica.client;

import java.util.*;
import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class ScopeClient {
 private static int radius,clock;private static ClientLevel world;
 private static Set<Long> retained=Set.of();private static final Map<Long,LevelChunk> cached=new HashMap<>();
 public static int radius(){return radius;}
 public static boolean retains(ChunkPos pos){return retained.contains(pos.toLong());}
 public static LevelChunk cached(int x,int z){return cached.get(ChunkPos.asLong(x,z));}
 public static void remember(LevelChunk c){if(c!=null&&retains(c.getPos()))cached.put(c.getPos().toLong(),c);}
 public static void state(ScopePackets.State state){var mc=Minecraft.getInstance();if(mc.level==null)return;radius=state.radius();world=mc.level;var next=new HashSet<Long>();for(long c:state.chunks())next.add(c);retained=Set.copyOf(next);cached.keySet().retainAll(retained);mc.level.getChunkSource().updateViewRadius(Math.max(radius,mc.options.getEffectiveRenderDistance()));}
 private static void reset(boolean notify){var mc=Minecraft.getInstance();boolean had=radius>0;radius=0;retained=Set.of();cached.clear();if(notify&&mc.getConnection()!=null)PacketDistributor.sendToServer(new GunPackets.Command(8));if(had&&mc.level!=null){mc.level.getChunkSource().updateViewRadius(mc.options.getEffectiveRenderDistance());}}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();ScopePackets.client=ScopeClient::state;if(world!=mc.level){reset(false);world=mc.level;}if(mc.player==null)return;if((!mc.player.isAlive()||RemoteClient.active())&&radius>0)reset(true);boolean bino=mc.player.isUsingItem()&&mc.player.getUseItem().is(Gadgets.BINOCULARS.get());if(++clock%8==0&&(bino||SniperClient.aiming()&&MareEletrica.isSniper(mc.player.getMainHandItem())))PacketDistributor.sendToServer(new GunPackets.Command(7));}
 @SubscribeEvent public static void screen(ScreenEvent.Opening e){if(e.getNewScreen() instanceof AbstractContainerScreen<?>)reset(true);}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){reset(false);world=null;}
}
