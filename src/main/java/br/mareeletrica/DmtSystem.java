package br.mareeletrica;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server owns attachment, gas and force. Packets never supply positions or velocity. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class DmtSystem {
 public static final double REACH=24,MAX_SPEED=1.8;
 public static final class Anchor {
  public final BlockPos block;public final UUID target;public final Vec3 point;public double length;
  Anchor(BlockPos block,UUID target,Vec3 point,double length){this.block=block;this.target=target;this.point=point;this.length=length;}
  public Vec3 position(ServerPlayer p){if(target!=null){var e=p.serverLevel().getEntity(target);return e instanceof LivingEntity living&&living.isAlive()?e.position().add(point):null;}return p.level().hasChunkAt(block)&&!p.level().getBlockState(block).getCollisionShape(p.level(),block).isEmpty()?point:null;}
 }
 public static final class Session {public Anchor left,right;public boolean boost;public float side;public long lastControl=-100,lastCommand=-100,attackAfter=-100,lastReel=-100;}
 private static final Map<UUID,Session> SESSIONS=new HashMap<>();
 private static final Map<UUID,Long> GRACE=new HashMap<>();
 private DmtSystem(){}
 public static Session ensure(ServerPlayer p){return SESSIONS.computeIfAbsent(p.getUUID(),id->new Session());}
 public static Session session(ServerPlayer p){return SESSIONS.get(p.getUUID());}
 public static boolean active(ServerPlayer p){var s=session(p);return s!=null&&(s.left!=null||s.right!=null);}
 public static boolean canBoost(Session s){return s!=null&&(s.left!=null||s.right!=null)&&(s.left==null||s.left.target==null)&&(s.right==null||s.right.target==null);}
 private static boolean allowed(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&!p.isPassenger()&&!p.isFallFlying()&&!RemoteSessions.active(p)&&DmtItem.enabled(p);}
 private static Anchor trace(ServerPlayer p,int side){
  Vec3 start=p.getEyePosition(),look=p.getLookAngle(),right=new Vec3(-look.z,0,look.x).normalize();Vec3 end=start.add(look.scale(REACH)).add(right.scale(side*.28));
  var block=p.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
  double nearest=block.getType()==HitResult.Type.MISS?REACH*REACH:start.distanceToSqr(block.getLocation());LivingEntity target=null;Vec3 hit=null;
  for(var e:p.level().getEntities(p,new AABB(start,end).inflate(1),e->e instanceof LivingEntity&&e.isAlive()&&!e.isSpectator())){
   var intersection=e.getBoundingBox().inflate(.12).clip(start,end);if(intersection.isPresent()&&start.distanceToSqr(intersection.get())<nearest){nearest=start.distanceToSqr(intersection.get());target=(LivingEntity)e;hit=intersection.get();}
  }
  if(target!=null)return new Anchor(null,target.getUUID(),hit.subtract(target.position()),Math.max(2,hit.distanceTo(p.position().add(0,.8,0))));
  if(block.getType()!=HitResult.Type.BLOCK||nearest>REACH*REACH||!p.level().hasChunkAt(block.getBlockPos()))return null;
  Vec3 point=block.getLocation().add(Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(.04));return new Anchor(block.getBlockPos().immutable(),null,point,Math.max(2,point.distanceTo(p.position().add(0,.8,0))));
 }
 public static void command(ServerPlayer p,int action){
  if(action==11){SniperItem.fire(p);return;}
  if(action==10){if(p.isAlive()&&!p.isSpectator()&&!RemoteSessions.active(p))DmtItem.toggle(p);return;}
  if(action==12){if(allowed(p))DmtGrab.grab(p);return;}
  if(action==13){DmtGrab.release(p);return;}
  if(action<0||action>9||!allowed(p))return;
  if(action==5){if(p.containerMenu==p.inventoryMenu)p.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,owner)->new DmtMenu(id,inv),Component.literal("Equipamento de manobra")));return;}
  if(action==6||action==7){reel(p,action==6?1:-1);return;}
  if(action==8){DmtCombat.attack(p);return;}
  if(action==9){var kit=ensure(p);if(p.level().getGameTime()-kit.lastCommand>=4){DmtCombat.cycle(p);kit.lastCommand=p.level().getGameTime();}return;}
  long now=p.level().getGameTime();var s=SESSIONS.computeIfAbsent(p.getUUID(),id->new Session());
  if(action!=2&&now-s.lastCommand<2)return;s.lastCommand=now;
  if(action==2){s.left=s.right=null;s.boost=false;sync(p,s,false);return;}
  if(action==3){boolean ok=DmtItem.refill(p);p.displayClientMessage(Component.translatable(ok?"dmt.mareeletrica.refilled":"dmt.mareeletrica.refill_fail"),true);sync(p,s,false);return;}
  if(action==0)s.left=s.left==null?trace(p,-1):null;
  else if(action==1)s.right=s.right==null?trace(p,1):null;
  else if(s.left!=null||s.right!=null){s.left=s.right=null;s.boost=false;}else{s.left=trace(p,-1);s.right=trace(p,1);}
  if(s.left!=null||s.right!=null)p.level().playSound(null,p.blockPosition(),SoundEvents.CROSSBOW_SHOOT,SoundSource.PLAYERS,.6F,1.4F);
  else p.displayClientMessage(Component.translatable("dmt.mareeletrica.free"),true);
  sync(p,s,false);
 }
 public static void control(ServerPlayer p,boolean boost,float side){if(!allowed(p))return;var s=session(p);if(s==null)return;s.boost=boost;s.side=Float.isFinite(side)?Math.max(-1,Math.min(1,side)):0;s.lastControl=p.level().getGameTime();}
 public static void clear(ServerPlayer p){var s=SESSIONS.remove(p.getUUID());if(s!=null){s.left=s.right=null;sync(p,s,false);}}
 public static void reel(ServerPlayer p,int direction){var s=session(p);if(s==null||!allowed(p)||p.level().getGameTime()==s.lastReel)return;s.lastReel=p.level().getGameTime();for(var a:new Anchor[]{s.left,s.right})if(a!=null)a.length=Math.max(2,Math.min(48,a.length+(direction>0?.25:-.25)));sync(p,s,false);}
 private static boolean pull(ServerPlayer p,Anchor anchor,Vec3 hip){if(anchor==null||anchor.target==null||!DmtCombat.pullEquipped(p))return false;var e=p.serverLevel().getEntity(anchor.target);if(!(e instanceof LivingEntity mob)||!DmtCombat.pullable(mob))return false;Vec3 toward=hip.subtract(mob.getBoundingBox().getCenter());if(toward.length()>anchor.length+.2){Vec3 v=toward.normalize().scale(Math.min(.8,(toward.length()-anchor.length)*.1)).add(0,.05,0);mob.setDeltaMovement(mob.getDeltaMovement().scale(.5).add(v));mob.hurtMarked=true;mob.fallDistance=0;}return true;}
 private static void sync(ServerPlayer p,Session s,boolean boosting){Vec3 l=s.left==null?null:s.left.position(p),r=s.right==null?null:s.right.position(p);int flags=(l!=null?1:0)|(r!=null?2:0)|(boosting?4:0);PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new DmtPackets.State(p.getId(),flags,DmtItem.fuel(DmtItem.equipped(p)),l==null?0:l.x,l==null?0:l.y,l==null?0:l.z,r==null?0:r.x,r==null?0:r.y,r==null?0:r.z,s.left==null?0:(float)s.left.length,s.right==null?0:(float)s.right.length));}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;var s=session(p);if(s==null)return;if(!allowed(p)){clear(p);return;}
  Vec3 hip=p.position().add(0,.8,0),left=s.left==null?null:s.left.position(p),right=s.right==null?null:s.right.position(p);
  if(left==null||left.distanceToSqr(hip)>64*64){s.left=null;left=null;}if(right==null||right.distanceToSqr(hip)>64*64){s.right=null;right=null;}
  if(left==null&&right==null){if(p.tickCount%4==0)sync(p,s,false);return;}
  long now=p.level().getGameTime();GRACE.put(p.getUUID(),now+30);p.fallDistance=0;
  var gear=DmtItem.equipped(p);boolean wallFlight=canBoost(s);boolean boost=wallFlight&&s.boost&&now-s.lastControl<=10&&(p.isCreative()||DmtItem.fuel(gear)>0);
  Vec3 velocity=p.getDeltaMovement();
  if(boost){velocity=velocity.add(p.getLookAngle().scale(.19)).add(0,.08,0);if(!p.isCreative())DmtItem.fuel(gear,DmtItem.fuel(gear)-1);}
  if(wallFlight&&now-s.lastControl<=10&&Math.abs(s.side)>.01){Vec3 look=p.getLookAngle();velocity=velocity.add(new Vec3(-look.z,0,look.x).normalize().scale(s.side*.035));}
  if(velocity.lengthSqr()>MAX_SPEED*MAX_SPEED)velocity=velocity.normalize().scale(MAX_SPEED);
  boolean pullLeft=pull(p,s.left,hip),pullRight=pull(p,s.right,hip);
  for(int i=0;i<4;i++){
   if(left!=null&&!pullLeft)velocity=DmtRope.constrain(hip,velocity,left,s.left.length);
   if(right!=null&&!pullRight)velocity=DmtRope.constrain(hip,velocity,right,s.right.length);
  }
  if(velocity.lengthSqr()>MAX_SPEED*MAX_SPEED)velocity=velocity.normalize().scale(MAX_SPEED);
  p.setDeltaMovement(velocity);p.hurtMarked=true;p.connection.send(new ClientboundSetEntityMotionPacket(p));
  if(boost&&p.tickCount%4==0)p.serverLevel().sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.7,p.getZ(),2,.18,.05,.18,.02);
  if(p.tickCount%2==0)sync(p,s,boost);
 }
 @SubscribeEvent public static void fall(LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p&&GRACE.getOrDefault(p.getUUID(),-1L)>=p.level().getGameTime()){e.setCanceled(true);p.fallDistance=0;}}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SESSIONS.remove(e.getEntity().getUUID());GRACE.remove(e.getEntity().getUUID());}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){clear(p);GRACE.remove(p.getUUID());}}
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){SESSIONS.remove(e.getEntity().getUUID());GRACE.remove(e.getEntity().getUUID());}
}
