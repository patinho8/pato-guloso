package br.mareeletrica;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
/** Authoritative sessions. Camera tickets are bounded and never teleport the player's body. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class RemoteSessions {
 public static final int VIEW_DISTANCE=3;
 private static final int LOAD_RADIUS=5;
 private static final TicketType<UUID> VIEW=TicketType.create("mare_rover_view",Comparator.comparing(UUID::toString));
 private static final TicketType<UUID> DOCK=TicketType.create("mare_rover_dock",Comparator.comparing(UUID::toString));
 private static final Map<UUID,Session> SESSIONS=new HashMap<>();
 private static final Map<UUID,Recall> RECALLS=new HashMap<>();
 private RemoteSessions(){}
 private static final class Session {
  final ServerPlayer player;final ServerLevel level;final UUID rover;final BlockPos home;final Vec3 anchor;final float health;final int token;final long started;
  long inputAt,packetTick=-1;ChunkPos center;Vec3 view;ExplorerEntity vehicle;
  Session(ServerPlayer p,UUID r,BlockPos h,BlockPos pos){player=p;level=p.serverLevel();rover=r;home=h.immutable();anchor=p.position();health=p.getHealth();token=java.util.concurrent.ThreadLocalRandom.current().nextInt(1,Integer.MAX_VALUE);started=inputAt=level.getGameTime();view=pos.getCenter();center=new ChunkPos(pos);}
 }
 private static final class Recall {
  final ServerLevel level;final UUID rover;final BlockPos home;final long started;ChunkPos center;
  Recall(Session s){level=s.level;rover=s.rover;home=s.home;center=s.center;started=level.getGameTime();}
 }
 public static boolean active(ServerPlayer p){return SESSIONS.containsKey(p.getUUID())||Drones.viewing(p);}
 public static int viewDistance(ServerPlayer p){return SESSIONS.containsKey(p.getUUID())?VIEW_DISTANCE:Drones.VIEW_DISTANCE;}
 public static void refreshView(ServerPlayer p){refresh(p);}
 public static boolean controls(ServerPlayer p,ExplorerEntity r){Session s=SESSIONS.get(p.getUUID());return s!=null&&s.vehicle==r&&s.level==r.level();}
 public static Vec3 viewPosition(ServerPlayer p){Session s=SESSIONS.get(p.getUUID());return s!=null?s.view:Drones.viewing(p)?Drones.viewPosition(p):p.position();}
 public static ChunkPos viewChunk(ServerPlayer p){Session s=SESSIONS.get(p.getUUID());return s!=null?s.center:Drones.viewChunk(p);}
 private static void message(ServerPlayer p,String key){p.displayClientMessage(Component.translatable(key),true);}
 private static boolean within(Vec3 a,Vec3 b){return VehicleRules.inRange(a.x-b.x,a.y-b.y,a.z-b.z);}
 public static void start(ServerPlayer p,CompoundTag tag){
  if(SESSIONS.containsKey(p.getUUID()))return;
  if(!tag.getBoolean("Bound")){message(p,"remote.mareeletrica.unbound");return;}
  if(!p.level().dimension().location().toString().equals(tag.getString("Dimension"))){message(p,"remote.mareeletrica.dimension");return;}
  BlockPos home=BlockPos.of(tag.getLong("Dock"));
  if(!p.onGround()||p.isPassenger()||p.isSleeping()||p.isSpectator()||p.isInWater()){message(p,"remote.mareeletrica.stand");return;}
  if(!within(p.position(),home.getCenter())||!p.serverLevel().getWorldBorder().isWithinBounds(home)){message(p,"remote.mareeletrica.range");return;}
  ServerLevel level=p.serverLevel();level.getChunk(home.getX()>>4,home.getZ()>>4);
  if(!(level.getBlockEntity(home) instanceof MachineEntity base)||base.kind!=Kind.ROVER_BASE||base.roverId==null){message(p,"remote.mareeletrica.no_rover");return;}
  if(SESSIONS.values().stream().anyMatch(s->s.rover.equals(base.roverId))){message(p,"remote.mareeletrica.busy");return;}
  BlockPos pos=level.getEntity(base.roverId) instanceof ExplorerEntity r?r.blockPosition():base.roverPosition;
  if(pos==null||!within(p.position(),pos.getCenter())){message(p,"remote.mareeletrica.range");return;}
  Recall recall=RECALLS.remove(base.roverId);if(recall!=null)releaseTickets(recall);
  Session s=new Session(p,base.roverId,home,pos);SESSIONS.put(p.getUUID(),s);
  level.getChunkSource().addRegionTicket(VIEW,s.center,LOAD_RADIUS,s.rover);level.getChunkSource().addRegionTicket(DOCK,new ChunkPos(home),3,s.rover);
  p.closeContainer();p.setDeltaMovement(Vec3.ZERO);state(s,-1);refresh(p);message(p,"remote.mareeletrica.loading");
 }
 private static void refresh(ServerPlayer p){((RemoteTracking)p.serverLevel().getChunkSource().chunkMap).mare$refresh(p);p.serverLevel().getChunkSource().move(p);}
 public static void input(ServerPlayer p,RemotePackets.Drive packet){
  Session s=SESSIONS.get(p.getUUID());if(s==null||packet.token()!=s.token)return;
  if((packet.flags()&2)!=0){stop(p,"remote.mareeletrica.ended");return;}
  long now=s.level.getGameTime();if(s.packetTick==now)return;s.packetTick=now;s.inputAt=now;p.resetLastActionTime();
  if((packet.flags()&4)==0||!Float.isFinite(packet.forward())||!Float.isFinite(packet.yaw())||!Float.isFinite(packet.pitch()))return;
  if(s.vehicle!=null){if((packet.flags()&8)!=0)s.vehicle.toggleFlight();s.vehicle.climb=((packet.flags()&16)!=0?1:0)-((packet.flags()&32)!=0?1:0);s.vehicle.driveForward=VehicleRules.axis(packet.forward());s.vehicle.driveYaw=packet.yaw()%360;s.vehicle.drivePitch=VehicleRules.pitch(packet.pitch());s.vehicle.trigger=(packet.flags()&1)!=0;}
 }
 @SubscribeEvent public static void before(PlayerTickEvent.Pre event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;Session s=SESSIONS.get(p.getUUID());if(s==null)return;
  if(p.serverLevel()!=s.level||!p.isAlive()||p.getHealth()<s.health||p.hurtTime>0||p.isPassenger()||p.position().distanceToSqr(s.anchor)>4){stop(p,"remote.mareeletrica.interrupted");return;}
  p.setPos(s.anchor.x,s.anchor.y,s.anchor.z);p.setDeltaMovement(Vec3.ZERO);
 }
 @SubscribeEvent public static void after(PlayerTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;Session s=SESSIONS.get(p.getUUID());if(s==null)return;long now=s.level.getGameTime();
  if(!p.getMainHandItem().is(MareEletrica.REMOTE.get())&&!p.getOffhandItem().is(MareEletrica.REMOTE.get())){stop(p,"remote.mareeletrica.ended");return;}
  if(now-s.inputAt>VehicleRules.TIMEOUT){stop(p,"remote.mareeletrica.signal");return;}
  if(s.vehicle==null){
   if(s.level.getEntity(s.rover) instanceof ExplorerEntity r&&r.isAlive()&&r.home.equals(s.home)){if(r.battery<=0){stop(p,"remote.mareeletrica.empty");return;}s.vehicle=r;r.control(p.getUUID());}
   else{if(now-s.started>100)stop(p,"remote.mareeletrica.no_rover");return;}
  }
  ExplorerEntity r=s.vehicle;
  if(r.isRemoved()||!r.isAlive()||!within(s.anchor,r.position())||r.battery<=0){stop(p,"remote.mareeletrica.signal");return;}
  if(!(s.level.getBlockEntity(s.home) instanceof MachineEntity base)||!r.getUUID().equals(base.roverId)){stop(p,"remote.mareeletrica.no_rover");return;}
  if(now-s.inputAt>10){r.driveForward=0;r.trigger=false;r.climb=0;}s.view=r.position();ChunkPos next=r.chunkPosition();
  if(!next.equals(s.center)){s.level.getChunkSource().addRegionTicket(VIEW,next,LOAD_RADIUS,s.rover);s.level.getChunkSource().removeRegionTicket(VIEW,s.center,LOAD_RADIUS,s.rover);s.center=next;}
  p.setPos(s.anchor.x,s.anchor.y,s.anchor.z);p.setDeltaMovement(Vec3.ZERO);if(now%2==0){refresh(p);state(s,r.getId());}
 }
 private static void state(Session s,int id){ExplorerEntity r=s.vehicle;PacketDistributor.sendToPlayer(s.player,new RemotePackets.State(s.token,id,r==null?0:r.battery,r==null?0:r.ammunition,r==null?0:r.mode(),(int)Math.sqrt(s.anchor.distanceToSqr(s.view))));}
 public static void openCargo(ServerPlayer p,int token){Session s=SESSIONS.get(p.getUUID());if(s!=null&&s.token==token&&s.vehicle!=null){s.vehicle.driveForward=0;s.vehicle.trigger=false;s.vehicle.climb=0;s.vehicle.openCargo(p);}}
 public static void stop(ServerPlayer p,String reason){end(p,true,reason);}
 private static void end(ServerPlayer p,boolean notify,String reason){
  Session s=SESSIONS.remove(p.getUUID());if(s==null)return;
  if(s.vehicle!=null&&!s.vehicle.isRemoved()&&s.vehicle.battery>0){s.vehicle.release();RECALLS.put(s.rover,new Recall(s));}
  else{if(s.vehicle!=null)s.vehicle.release();s.level.getChunkSource().removeRegionTicket(VIEW,s.center,LOAD_RADIUS,s.rover);s.level.getChunkSource().removeRegionTicket(DOCK,new ChunkPos(s.home),3,s.rover);}
  if(notify){PacketDistributor.sendToPlayer(p,new RemotePackets.State(0,0,0,0,0,0));refresh(p);message(p,reason);}
 }
 private static void releaseTickets(Recall r){r.level.getChunkSource().removeRegionTicket(VIEW,r.center,LOAD_RADIUS,r.rover);r.level.getChunkSource().removeRegionTicket(DOCK,new ChunkPos(r.home),3,r.rover);}
 @SubscribeEvent public static void recalls(ServerTickEvent.Post event){
  var it=RECALLS.values().iterator();while(it.hasNext()){
   Recall r=it.next();var e=r.level.getEntity(r.rover);
   if(!(e instanceof ExplorerEntity rover)||rover.isRemoved()||rover.phase==0||rover.phase==3||rover.battery<=0||r.level.getGameTime()-r.started>12000){releaseTickets(r);it.remove();continue;}
   ChunkPos next=rover.chunkPosition();if(!next.equals(r.center)){r.level.getChunkSource().addRegionTicket(VIEW,next,LOAD_RADIUS,r.rover);r.level.getChunkSource().removeRegionTicket(VIEW,r.center,LOAD_RADIUS,r.rover);r.center=next;}
  }
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)end(p,false,"");}
 @SubscribeEvent public static void shutdown(ServerStoppedEvent e){SESSIONS.clear();RECALLS.clear();}
}
