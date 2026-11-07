package br.mareeletrica;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** DMT block grab: hold right-click to tear a block out with the cable, swing it at mobs where you aim, release to throw it. */
public final class DmtGrab {
 public static final double HOLD=5.5,REACH=24;
 public static final Supplier<EntityType<Grabbed>> ENTITY=MareEletrica.ACTORS.register("dmt_grabbed_block",()->
  EntityType.Builder.<Grabbed>of(Grabbed::new,MobCategory.MISC).sized(.98F,.98F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":dmt_grabbed_block"));
 private static final Map<UUID,UUID> HELD=new HashMap<>();
 private DmtGrab(){}
 public static void register(){}

 /** Called when the player has held right-click long enough. */
 public static void grab(ServerPlayer p){
  if(holding(p)!=null)return;
  var level=p.serverLevel();BlockPos pos=null;
  var s=DmtSystem.session(p);
  if(s!=null&&s.right!=null&&s.right.block!=null)pos=s.right.block;
  else if(s!=null&&s.left!=null&&s.left.block!=null)pos=s.left.block;
  if(pos==null){
   Vec3 a=p.getEyePosition(),b=a.add(p.getLookAngle().scale(REACH));
   var hit=level.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
   if(hit.getType()!=HitResult.Type.BLOCK)return;pos=hit.getBlockPos();
  }
  BlockState state=level.getBlockState(pos);
  if(!grabbable(level,pos,state)||!p.mayInteract(level,pos)||p.blockActionRestricted(level,pos,p.gameMode.getGameModeForPlayer())){p.displayClientMessage(Component.literal("§cEsse bloco não dá pra puxar"),true);return;}
  if(NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,pos,state,p)).isCanceled())return;
  var gear=DmtItem.equipped(p);
  if(!p.isCreative()){if(DmtItem.fuel(gear)<30){p.displayClientMessage(Component.translatable("dmt.mareeletrica.refill_fail"),true);return;}DmtItem.fuel(gear,DmtItem.fuel(gear)-30);}
  level.removeBlock(pos,false);
  var e=new Grabbed(ENTITY.get(),level);e.init(state,p);e.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
  level.addFreshEntity(e);HELD.put(p.getUUID(),e.getUUID());
  level.levelEvent(2001,pos,net.minecraft.world.level.block.Block.getId(state));
  level.playSound(null,pos,SoundEvents.CHAIN_BREAK,SoundSource.PLAYERS,1F,.7F);
  // the cables now carry the block instead of the player
  if(s!=null){s.left=s.right=null;s.boost=false;}
 }
 /** Called when right-click is released: throw the block where you look. */
 public static void release(ServerPlayer p){
  var e=holding(p);HELD.remove(p.getUUID());if(e==null)return;
  e.throwFrom(p.getLookAngle().scale(2.3).add(e.getDeltaMovement().scale(.35)));
  p.level().playSound(null,p.blockPosition(),SoundEvents.CROSSBOW_SHOOT,SoundSource.PLAYERS,.9F,.6F);
 }
 public static Grabbed holding(ServerPlayer p){var id=HELD.get(p.getUUID());if(id==null)return null;var e=p.serverLevel().getEntity(id);if(e instanceof Grabbed g&&g.isAlive()&&g.held())return g;HELD.remove(p.getUUID());return null;}
 public static void drop(ServerPlayer p){var e=holding(p);HELD.remove(p.getUUID());if(e!=null)e.throwFrom(e.getDeltaMovement().scale(.5));}
 static boolean grabbable(Level l,BlockPos pos,BlockState s){
  if(s.isAir()||!s.getFluidState().isEmpty()&&s.getCollisionShape(l,pos).isEmpty())return false;
  float hard=s.getDestroySpeed(l,pos);
  return hard>=0&&hard<=50&&!s.hasBlockEntity()&&!s.is(Blocks.BEDROCK)&&!s.getCollisionShape(l,pos).isEmpty();
 }

 public static final class Grabbed extends Entity {
  static final EntityDataAccessor<BlockState> BLOCK=SynchedEntityData.defineId(Grabbed.class,EntityDataSerializers.BLOCK_STATE);
  static final EntityDataAccessor<Integer> OWNER=SynchedEntityData.defineId(Grabbed.class,EntityDataSerializers.INT);
  static final EntityDataAccessor<Boolean> HELD_FLAG=SynchedEntityData.defineId(Grabbed.class,EntityDataSerializers.BOOLEAN);
  UUID owner;private final Map<UUID,Integer> hitCooldown=new HashMap<>();private int flying;
  public Grabbed(EntityType<? extends Grabbed> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(BLOCK,Blocks.STONE.defaultBlockState());b.define(OWNER,-1);b.define(HELD_FLAG,false);}
  public BlockState block(){return entityData.get(BLOCK);}
  void setBlock(BlockState s){entityData.set(BLOCK,s);}
  void init(BlockState s,ServerPlayer p){setBlock(s);owner=p.getUUID();entityData.set(OWNER,p.getId());entityData.set(HELD_FLAG,true);noPhysics=true;}
  public boolean held(){return entityData.get(HELD_FLAG);}
  public int ownerId(){return entityData.get(OWNER);}
  /** Cable length (blocks). */
  double rope=HOLD;
  Vec3 center(){return position().add(0,.49,0);}
  /** Right hand of the player: the cable comes out of the right DMT launcher. */
  public static Vec3 hand(Player p){double yaw=Math.toRadians(p.getYRot());Vec3 right=new Vec3(-Math.cos(yaw),0,-Math.sin(yaw));return p.position().add(0,1.1,0).add(right.scale(.38));}
  void throwFrom(Vec3 v){entityData.set(HELD_FLAG,false);noPhysics=false;setDeltaMovement(v);hurtMarked=true;flying=0;}
  @Override public boolean isPickable(){return false;}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<128*128;}
  @Override public void tick(){
   super.tick();
   if(level().isClientSide){noPhysics=held();return;}
   var server=(ServerLevel)level();
   hitCooldown.replaceAll((k,v)->v-1);hitCooldown.values().removeIf(v->v<=0);
   if(held()){
    noPhysics=true;
    Player p=owner==null?null:server.getPlayerByUUID(owner);
    if(!(p instanceof ServerPlayer sp)||!sp.isAlive()||sp.level()!=level()||sp.distanceToSqr(this)>32*32||!DmtItem.enabled(sp)){throwFrom(getDeltaMovement().scale(.5));HELD.remove(owner);}
    else{
     // pendulum on ONE cable from the right hand: gravity, the cable length as a hard limit, a light pull toward where you aim,
     // real collisions with blocks (it bounces off walls), air drag
     Vec3 hand=hand(sp);
     Vec3 aim=sp.getEyePosition().add(sp.getLookAngle().scale(rope));
     Vec3 v=getDeltaMovement().add(0,-.06,0);
     v=v.add(aim.subtract(center()).scale(.045));                 // you steer it by looking
     v=v.scale(.97);
     Vec3 next=center().add(v);Vec3 d=next.subtract(hand);double len=d.length();
     if(len>rope){Vec3 fixed=hand.add(d.scale(rope/len));v=fixed.subtract(center());}   // the cable is taut: it swings around the hand
     if(v.lengthSqr()>2.4*2.4)v=v.normalize().scale(2.4);
     setDeltaMovement(v);
     noPhysics=false;Vec3 before=position();move(MoverType.SELF,v);
     if(horizontalCollision){Vec3 nv=getDeltaMovement();setDeltaMovement(new Vec3(-v.x*.45,nv.y,-v.z*.45));if(v.horizontalDistanceSqr()>.25)server.playSound(null,blockPosition(),block().getSoundType().getHitSound(),SoundSource.BLOCKS,1F,.7F);}
     if(verticalCollision){setDeltaMovement(new Vec3(getDeltaMovement().x*.8,-v.y*.3,getDeltaMovement().z*.8));}
     Vec3 real=position().subtract(before);
     smash(real,sp,1F,2.5F);
     if(tickCount%3==0&&real.lengthSqr()>.6)server.playSound(null,blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,.35F,.6F);
    }
    return;
   }
   noPhysics=false;flying++;
   Vec3 v=getDeltaMovement().add(0,-.06,0).scale(.985);setDeltaMovement(v);
   Entity thrower=owner==null?null:server.getEntity(owner);
   smash(v,thrower,2F,3F);
   move(MoverType.SELF,getDeltaMovement());
   boolean stopped=onGround()||horizontalCollision||verticalCollision||flying>200;
   if(stopped&&flying>1)land(server,v);
  }
  /** Hurt mobs the block passes through; damage grows with speed. */
  private void smash(Vec3 v,Entity source,float base,float perSpeed){
   double speed=v.length();if(speed<.35)return;
   for(var e:level().getEntities(this,getBoundingBox().expandTowards(v).inflate(.25),x->x instanceof LivingEntity&&x.isAlive()&&!x.getUUID().equals(owner))){
    if(hitCooldown.containsKey(e.getUUID()))continue;hitCooldown.put(e.getUUID(),10);
    var src=source instanceof LivingEntity le?level().damageSources().mobProjectile(this,le):level().damageSources().fallingBlock(this);
    e.hurt(src,base+(float)speed*perSpeed);
    Vec3 push=v.normalize().scale(.6+speed*.4);e.push(push.x,.25+speed*.1,push.z);e.hurtMarked=true;
    level().playSound(null,e.blockPosition(),block().getSoundType().getBreakSound(),SoundSource.PLAYERS,1F,.8F);
   }
  }
  /** Thrown blocks shatter where they land: particles and sound, no block and no drop. */
  private void land(ServerLevel l,Vec3 v){
   BlockState s=block();
   l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,s),getX(),getY()+.5,getZ(),40,.35,.35,.35,.15);
   l.playSound(null,blockPosition(),s.getSoundType().getBreakSound(),SoundSource.BLOCKS,1.2F,.7F);
   discard();
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){t.put("Block",NbtUtils.writeBlockState(block()));}
  @Override protected void readAdditionalSaveData(CompoundTag t){if(t.contains("Block"))setBlock(NbtUtils.readBlockState(level().holderLookup(net.minecraft.core.registries.Registries.BLOCK),t.getCompound("Block")));}
 }
}
