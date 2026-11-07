package br.mareeletrica;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Per observer: sparse sight corridors, bounded tickets and unchanged global view distance. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class ScopeSight {
 public static final int MAX_RADIUS=32, MAX_CHUNKS=512;
 private static final TicketType<UUID> TICKET=TicketType.create("mare_scope",Comparator.comparing(UUID::toString));
 private static final Map<UUID,Session> SESSIONS=new HashMap<>();
 private static final class Session {
  final ServerLevel level;final Set<Long> chunks=new LinkedHashSet<>();int radius;long lastRequest=-100;ChunkPos center;
  Session(ServerPlayer p){level=p.serverLevel();center=p.chunkPosition();}
 }
 public record View(ChunkTrackingView normal,Set<Long> chunks) implements ChunkTrackingView {
  public boolean contains(int x,int z,boolean searchAll){return normal.contains(x,z,searchAll)||chunks.contains(ChunkPos.asLong(x,z));}
  public void forEach(Consumer<ChunkPos> consumer){normal.forEach(consumer);for(long pos:chunks){var c=new ChunkPos(pos);if(!normal.contains(c))consumer.accept(c);}}
 }
 public static boolean active(ServerPlayer p){return SESSIONS.containsKey(p.getUUID());}
 public static int retained(ServerPlayer p){var s=SESSIONS.get(p.getUUID());return s==null?0:s.chunks.size();}
 public static ChunkTrackingView view(ServerPlayer p,int normalRadius){var s=SESSIONS.get(p.getUUID());return s==null?null:new View(ChunkTrackingView.of(p.chunkPosition(),normalRadius),Set.copyOf(s.chunks));}
 public static boolean sees(ServerPlayer p,Entity entity){var s=SESSIONS.get(p.getUUID());return s!=null&&s.level==entity.level()&&!RemoteSessions.active(p)&&(entity instanceof LivingEntity||entity instanceof DmtProjectileEntity)&&s.chunks.contains(entity.chunkPosition().toLong())&&p.distanceToSqr(entity)<(MAX_RADIUS*16+32)*(MAX_RADIUS*16+32);}
 public static void request(ServerPlayer p){
  boolean binoculars=p.isUsingItem()&&p.getUseItem().is(Gadgets.BINOCULARS.get());
  if(!p.isAlive()||p.isSpectator()||RemoteSessions.active(p)||p.containerMenu!=p.inventoryMenu||!binoculars&&(!SniperItem.scoped(p)||!MareEletrica.isSniper(p.getUseItem())))return;
  var s=SESSIONS.computeIfAbsent(p.getUUID(),id->new Session(p));long now=s.level.getGameTime();if(now-s.lastRequest<8)return;s.lastRequest=now;
  int zoom=binoculars?12:RifleLoadout.zoom(p.getUseItem());
  int range=Math.min(MAX_RADIUS,Math.max(12,8+zoom));s.radius=Math.max(s.radius,range);
  var forward=p.getLookAngle();double h=forward.horizontalDistance();if(h<.01)return;double dx=forward.x/h,dz=forward.z/h;
  var candidates=new LinkedHashSet<Long>();int width=Math.min(4,Math.max(1,12/zoom));
  // Centre line first, then adjacent corridors. No synchronous chunk generation on the tick thread.
  for(int lane=0;lane<=width;lane++)for(int step=1;step<range;step++)for(int sign:lane==0?new int[]{0}:new int[]{-1,1}){
   int x=(int)Math.floor((p.getX()+dx*step*16-dz*lane*sign*16)/16),z=(int)Math.floor((p.getZ()+dz*step*16+dx*lane*sign*16)/16);
   var c=new ChunkPos(x,z);if(s.level.getWorldBorder().isWithinBounds(c.getMiddleBlockPosition(64)))candidates.add(c.toLong());
  }
  boolean changed=false;int added=0;
  for(long pos:candidates){if(s.chunks.size()>=MAX_CHUNKS||added>=12)break;if(s.chunks.add(pos)){
   s.level.getChunkSource().addRegionTicket(TICKET,new ChunkPos(pos),1,p.getUUID());changed=true;added++;
  }}
  if(changed){PacketDistributor.sendToPlayer(p,new ScopePackets.State(s.radius,s.chunks.stream().mapToLong(Long::longValue).toArray()));refresh(p);}
 }
 private static void refresh(ServerPlayer p){((RemoteTracking)p.serverLevel().getChunkSource().chunkMap).mare$refresh(p);p.serverLevel().getChunkSource().move(p);}
 public static void clear(ServerPlayer p,boolean notify){var s=SESSIONS.remove(p.getUUID());if(s==null)return;for(long c:s.chunks)s.level.getChunkSource().removeRegionTicket(TICKET,new ChunkPos(c),1,p.getUUID());if(notify){PacketDistributor.sendToPlayer(p,new ScopePackets.State(0,new long[0]));refresh(p);}}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var s=SESSIONS.get(p.getUUID());if(s==null)return;
  if(!p.isAlive()||p.isSpectator()||p.serverLevel()!=s.level||RemoteSessions.active(p)||p.containerMenu!=p.inventoryMenu){clear(p,true);return;}
  if(s.level.getGameTime()%10==0){if(!s.center.equals(p.chunkPosition())){s.center=p.chunkPosition();p.connection.send(new ClientboundSetChunkCacheCenterPacket(s.center.x,s.center.z));}refresh(p);}
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p,false);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p,true);}
 @SubscribeEvent public static void stop(ServerStoppedEvent e){SESSIONS.clear();}
}
