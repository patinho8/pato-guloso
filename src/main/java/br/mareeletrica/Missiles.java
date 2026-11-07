package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Anti-air missile launcher: locks on to fighter jets and drones and fires guided missiles.
 *  The missiles turn at a limited rate, so a hard maneuver at speed can make them overshoot; jet flares can decoy them. */
public final class Missiles {
 public static final int MAX_AMMO=32,RANGE=160;
 public static final double SPEED=2.8;          // only a little faster than the jet
 public static final float TURN=2.3F;            // degrees per tick the missile can turn (the jet pulls up to ~6): easy to out-turn
 public static final DeferredBlock<LauncherBlock> BLOCK=MareEletrica.BLOCKS.register("missile_launcher",()->new LauncherBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(4F,12F).noOcclusion().sound(SoundType.METAL)));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("missile_launcher",()->new BlockItem(BLOCK.get(),new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> MISSILE=MareEletrica.ITEMS.register("sam_missile",()->new Item(new Item.Properties().stacksTo(16)));
 public static final Supplier<BlockEntityType<LauncherEntity>> ENTITY=MareEletrica.ENTITIES.register("missile_launcher",()->BlockEntityType.Builder.of(LauncherEntity::new,BLOCK.get()).build(null));
 public static final Supplier<EntityType<MissileEntity>> MISSILE_ENTITY=MareEletrica.ACTORS.register("sam_missile",()->
  EntityType.Builder.<MissileEntity>of(MissileEntity::new,MobCategory.MISC).sized(.4F,.4F).clientTrackingRange(16).updateInterval(1).build(MareEletrica.ID+":sam_missile"));
 private Missiles(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(ITEM.get());out.accept(MISSILE.get());}
 public static boolean target(Entity e){return e instanceof Vehicles.Helicopter h&&!h.onGround()&&h.rotor>.5F||e instanceof Jets.JetEntity j&&!j.downed()&&!j.onGround()||e instanceof Drones.DroneEntity d&&!d.crashing()&&d.powered();}
 /** The player flying / piloting this target (so a launcher never shoots at its owner). */
 public static UUID pilotOf(Entity e){
  if(e instanceof Jets.JetEntity j&&j.getControllingPassenger()!=null)return j.getControllingPassenger().getUUID();
  if(e instanceof Vehicles.Helicopter h&&h.getControllingPassenger()!=null)return h.getControllingPassenger().getUUID();
  if(e instanceof Drones.DroneEntity d&&!d.level().isClientSide){var p=Drones.pilot(d);return p==null?null:p.getUUID();}
  return null;
 }

 public static final class LauncherBlock extends BaseEntityBlock {
  public static final MapCodec<LauncherBlock> CODEC=simpleCodec(LauncherBlock::new);
  private static final VoxelShape SHAPE=Block.box(1,0,1,15,14,15);
  public LauncherBlock(BlockBehaviour.Properties p){super(p);}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
  @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new LauncherEntity(pos,state);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,ENTITY.get(),LauncherEntity::tick);}
  @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.LivingEntity placer,ItemStack stack){if(placer!=null&&level.getBlockEntity(pos) instanceof LauncherEntity l){l.owner=placer.getUUID();l.setChanged();}}
  @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
   if(!stack.is(MISSILE.get()))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   if(!level.isClientSide&&level.getBlockEntity(pos) instanceof LauncherEntity l){
    int n=Math.min(stack.getCount(),MAX_AMMO-l.ammo);
    if(n<=0)player.displayClientMessage(Component.literal("Lançador cheio ("+MAX_AMMO+" mísseis)."),true);
    else{l.ammo+=n;if(!player.isCreative())stack.shrink(n);l.sync();level.playSound(null,pos,SoundEvents.ARMOR_EQUIP_IRON.value(),SoundSource.BLOCKS,1F,.7F);player.displayClientMessage(Component.literal("Mísseis: "+l.ammo+" / "+MAX_AMMO),true);}
   }
   return ItemInteractionResult.sidedSuccess(level.isClientSide);
  }
  @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
   if(!level.isClientSide&&level.getBlockEntity(pos) instanceof LauncherEntity l){
    l.armed=!l.armed;l.sync();
    player.displayClientMessage(Component.literal((l.armed?"§aDefesa antiaérea LIGADA":"§cDefesa antiaérea DESLIGADA")+"§f — mísseis "+l.ammo+"/"+MAX_AMMO+" • alcance "+RANGE+" blocos • não atira em você"),true);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
 }

 public static final class LauncherEntity extends BlockEntity {
  public int ammo;public boolean armed=true;public UUID owner;private long readyAt;private int lockId=-1,lockTicks;
  public float yaw,pitch=20,yawO,pitchO,yawT,pitchT=20;public long firedAt=-100;
  public LauncherEntity(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
  public static void tick(Level level,BlockPos pos,BlockState state,LauncherEntity l){
   if(level.isClientSide){
    l.yawO=l.yaw;l.pitchO=l.pitch;l.yaw+=Mth.clamp(Mth.wrapDegrees(l.yawT-l.yaw),-9F,9F);l.pitch+=Mth.clamp(l.pitchT-l.pitch,-5F,5F);
    return;
   }
   if(!l.armed||level.getGameTime()%5!=0)return;
   Vec3 c=pos.getCenter().add(0,.6,0);
   Entity best=null;double bd=Double.MAX_VALUE;
   for(var e:level.getEntitiesOfClass(Entity.class,new AABB(pos).inflate(RANGE),Missiles::target)){
    var pilot=pilotOf(e);if(pilot!=null&&pilot.equals(l.owner))continue;
    double d=e.position().distanceToSqr(c);if(d>RANGE*RANGE||d<36)continue;
    if(level.clip(new net.minecraft.world.level.ClipContext(c,e.position(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,e)).getType()!=net.minecraft.world.phys.HitResult.Type.MISS)continue;
    if(d<bd){bd=d;best=e;}
   }
   if(best==null){l.lockId=-1;l.lockTicks=0;return;}
   // turret follows the target; needs ~1 s of lock (the pilot hears the warning) before firing
   var to=best.position().subtract(c);float ty=(float)Math.toDegrees(Math.atan2(-to.x,to.z)),tp=(float)Math.toDegrees(Math.atan2(to.y,to.horizontalDistance()));
   if(Math.abs(Mth.wrapDegrees(ty-l.yawT))>1||Math.abs(tp-l.pitchT)>1){l.yawT=ty;l.pitchT=Mth.clamp(tp,0,85);l.sync();}
   if(best.getId()!=l.lockId){l.lockId=best.getId();l.lockTicks=0;}else l.lockTicks+=5;
   if(l.ammo<=0||l.lockTicks<20||level.getGameTime()<l.readyAt)return;
   l.ammo--;l.readyAt=level.getGameTime()+70;
   double r=Math.toRadians(l.yawT),p=Math.toRadians(Math.max(15,l.pitchT));
   Vec3 dir=new Vec3(-Math.sin(r)*Math.cos(p),Math.sin(p),Math.cos(r)*Math.cos(p));
   var m=new MissileEntity(MISSILE_ENTITY.get(),level);var start=c.add(dir.scale(1.1));m.setPos(start.x,start.y,start.z);m.setDeltaMovement(dir.scale(1.2));m.setTarget(best);m.owner=l.owner;
   level.addFreshEntity(m);l.sync();
   level.blockEvent(pos,state.getBlock(),1,0);
   level.playSound(null,pos,MareEletrica.LAUNCHER_FIRE.get(),SoundSource.BLOCKS,1.2F,1.2F);
   if(level instanceof ServerLevel sl){sl.sendParticles(ParticleTypes.LARGE_SMOKE,start.x,start.y,start.z,14,.3,.3,.3,.05);var b=c.subtract(dir.scale(1));sl.sendParticles(ParticleTypes.CLOUD,b.x,b.y,b.z,14,.3,.3,.3,.08);}
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("Ammo",ammo);t.putBoolean("Armed",armed);if(owner!=null)t.putUUID("Owner",owner);t.putFloat("Yaw",yawT);t.putFloat("Pitch",pitchT);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);ammo=t.getInt("Ammo");armed=!t.contains("Armed")||t.getBoolean("Armed");owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;yawT=t.getFloat("Yaw");pitchT=t.contains("Pitch")?t.getFloat("Pitch"):20;}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
  @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }

 /** Guided missile: pure pursuit with lead, limited turn rate, proximity fuse, can be decoyed by flares. */
 public static final class MissileEntity extends Entity {
  private static final EntityDataAccessor<Integer> TARGET=SynchedEntityData.defineId(MissileEntity.class,EntityDataSerializers.INT);
  public UUID owner;private boolean lost;
  public MissileEntity(EntityType<? extends MissileEntity> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(TARGET,-1);}
  public int targetId(){return entityData.get(TARGET);}
  public void setTarget(Entity e){entityData.set(TARGET,e==null?-1:e.getId());}
  @Override public boolean isNoGravity(){return true;}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<256*256;}
  @Override public void tick(){
   super.tick();
   Vec3 v=getDeltaMovement();
   if(level().isClientSide){
    level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,true,getX()-v.x*.5,getY()-v.y*.5,getZ()-v.z*.5,0,.01,0);
    level().addParticle(ParticleTypes.FLAME,getX()-v.x*.3,getY()-v.y*.3,getZ()-v.z*.3,-v.x*.1,-v.y*.1,-v.z*.1);
    return;
   }
   double speed=Math.min(SPEED,v.length()+.18);   // rocket motor accelerates to full speed
   var target=level().getEntity(targetId());
   if(target!=null&&!lost&&target.isAlive()&&!target.isRemoved()){
    Vec3 to=target.position().add(0,target.getBbHeight()/2,0).subtract(position());
    double dist=to.length();
    Vec3 aim=to.add(target.getDeltaMovement().scale(Math.min(30,dist/speed)*.6)).normalize();   // lead the target
    Vec3 head=v.lengthSqr()<1e-4?aim:v.normalize();
    double ang=Math.toDegrees(Math.acos(Mth.clamp(head.dot(aim),-1,1)));
    if(ang>60&&tickCount>12)lost=true;                    // target got behind the seeker: the lock breaks (that's how you dodge)
    else if(ang>1e-3){double f=Math.min(1,TURN/ang);head=head.scale(1-f).add(aim.scale(f)).normalize();}
    v=head.scale(speed);
    if(dist<3.2){detonate(target);return;}
   }else v=v.normalize().scale(speed).add(0,-.02,0);
   setDeltaMovement(v);move(MoverType.SELF,v);
   if(horizontalCollision||verticalCollision||isInWater()||tickCount>180){detonate(null);return;}
   if(tickCount%12==0)level().playSound(null,getX(),getY(),getZ(),SoundEvents.FIREWORK_ROCKET_LAUNCH,SoundSource.NEUTRAL,1.2F,.6F);
  }
  private void detonate(Entity hit){
   level().explode(this,null,null,getX(),getY(),getZ(),2.6F,false,Level.ExplosionInteraction.NONE,ParticleTypes.EXPLOSION,ParticleTypes.EXPLOSION_EMITTER,MareEletrica.SILENT);
   level().playSound(null,getX(),getY(),getZ(),MareEletrica.FRAG_BLAST.get(),SoundSource.NEUTRAL,1.5F,1F);
   // proximity warhead: shrapnel hurts aircraft nearby (jets only take projectile damage)
   for(var e:level().getEntitiesOfClass(Entity.class,getBoundingBox().inflate(4.5),x->x instanceof Jets.JetEntity||x instanceof Drones.DroneEntity||x instanceof Vehicles.Base)){
    e.hurt(damageSources().mobProjectile(this,null),e==hit?42F:24F);
   }
   discard();
  }
  /** Flares: a missile chasing this aircraft may switch to one of them. */
  public void decoy(Entity flare){if(!lost&&random.nextFloat()<.7F)setTarget(flare);}
  @Override protected void addAdditionalSaveData(CompoundTag t){}
  @Override protected void readAdditionalSaveData(CompoundTag t){}
 }
}
