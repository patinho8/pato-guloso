package br.mareeletrica;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Fighter jet: free 3D flight (rolls, loops), wing machine guns, up to 3 rocket-assisted bombs and flares. When shot down it loses control and falls burning. */
public final class Jets {
 public static final DeferredItem<JetItem> JET_ITEM=MareEletrica.ITEMS.register("fighter_jet",()->new JetItem(new Item.Properties().stacksTo(1)));
 /** B-2 stealth bomber: same flight model, slower and heavier, no guns, a bomb bay with 12 bombs dropped in sticks of 4. */
 public static final DeferredItem<JetItem> B2_ITEM=MareEletrica.ITEMS.register("b2_bomber",()->new JetItem(new Item.Properties().stacksTo(1),true));
 public static final DeferredItem<Item> BOMB=MareEletrica.ITEMS.register("jet_bomb",()->new Item(new Item.Properties().stacksTo(6)));
 public static final int MAX_BOMBS=3;public static final float MAX_HEALTH=90F;
 public static final Supplier<EntityType<JetEntity>> JET=MareEletrica.ACTORS.register("fighter_jet",()->
  EntityType.Builder.<JetEntity>of(JetEntity::new,MobCategory.MISC).sized(5.2F,2.3F).clientTrackingRange(16).updateInterval(1).fireImmune().build(MareEletrica.ID+":fighter_jet"));
 public static final Supplier<EntityType<JetEntity>> B2=MareEletrica.ACTORS.register("b2_bomber",()->
  EntityType.Builder.<JetEntity>of(JetEntity::new,MobCategory.MISC).sized(9F,2.2F).clientTrackingRange(20).updateInterval(1).fireImmune().build(MareEletrica.ID+":b2_bomber"));
 public static final int B2_BOMBS=12;public static final float B2_HEALTH=220F;
 public static final Supplier<EntityType<JetBombEntity>> BOMB_ENTITY=MareEletrica.ACTORS.register("jet_bomb",()->
  EntityType.Builder.<JetBombEntity>of(JetBombEntity::new,MobCategory.MISC).sized(.9F,.9F).clientTrackingRange(16).updateInterval(1).build(MareEletrica.ID+":jet_bomb"));
 /** Client hook: starts the engine sound loop for a jet entity. */
 public static Consumer<JetEntity> startSound=j->{};
 private Jets(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(JET_ITEM.get());out.accept(B2_ITEM.get());out.accept(BOMB.get());}
 /** Model: 1 model pixel = 1/8 block (drawn at scale 2). Gun muzzles, seat and bomb in jet space (x = left, y = up, z = forward). */
 public static final float SCALE=3F,CENTER_Y=6F*SCALE/16F;
 public static final Vec3 GUN_L=new Vec3(1.26,-.18,1.6),GUN_R=new Vec3(-1.26,-.18,1.6),SEAT=new Vec3(0,.84,1.72),BOMB_AT=new Vec3(0,-1.15,-.15),NOZZLE=new Vec3(0,0,-4.3);

 public static final class JetItem extends Item {
  final boolean bomber;
  public JetItem(Properties p){this(p,false);}
  public JetItem(Properties p,boolean bomber){super(p);this.bomber=bomber;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<Component> t,net.minecraft.world.item.TooltipFlag f){
   if(bomber){t.add(Component.literal("§7Bombardeiro furtivo: lento, resistente, sem metralhadora."));t.add(Component.literal("§7Carrega "+B2_BOMBS+" bombas • [B] ou clique: solta 4 em sequência"));}}
  @Override public InteractionResult useOn(UseOnContext c){
   var level=c.getLevel();
   if(!level.isClientSide){
    var j=new JetEntity(bomber?B2.get():JET.get(),level);var pos=c.getClickedPos().relative(c.getClickedFace());
    j.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);j.setHeading(c.getRotation());
    if(!level.noCollision(j,j.getBoundingBox())){if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal((bomber?"Sem espaço para o B-2 aqui (precisa de 10x3 blocos livres).":"Sem espaço para o caça aqui (precisa de 6x3 blocos livres).")),true);return InteractionResult.FAIL;}
    level.addFreshEntity(j);
    if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
 }

 public static final class JetEntity extends Entity implements Vehicles.Serviceable {
  private static final EntityDataAccessor<Quaternionf> ROT=SynchedEntityData.defineId(JetEntity.class,EntityDataSerializers.QUATERNION);
  private static final EntityDataAccessor<Float> THROTTLE=SynchedEntityData.defineId(JetEntity.class,EntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Integer> BOMBS=SynchedEntityData.defineId(JetEntity.class,EntityDataSerializers.INT);
  private static final EntityDataAccessor<Boolean> DOWNED=SynchedEntityData.defineId(JetEntity.class,EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Integer> FIRING=SynchedEntityData.defineId(JetEntity.class,EntityDataSerializers.INT);
  /** Orientation (jet space -> world). q is the live value; qO the previous tick for smooth rendering. */
  public final Quaternionf q=new Quaternionf(),qO=new Quaternionf();
  public float throttle;private float health=-1;private java.util.UUID lastPilot;private int flareCooldown;private int lerpSteps;private double lx,ly,lz;
  /** Pilot input for this tick (set by the pilot's client before the tick). */
  public float inPitch,inRoll,inYaw,inThrottle;public boolean inFire;
  private boolean fireHeld;private int gunCooldown;private boolean leftGun;private float lastSpeed;private Vec3 lastVel=Vec3.ZERO;
  public Object engineSound;
  public JetEntity(EntityType<? extends JetEntity> type,Level level){super(type,level);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(ROT,new Quaternionf());b.define(THROTTLE,0F);b.define(BOMBS,0);b.define(DOWNED,false);b.define(FIRING,0);}
  public void setHeading(float yaw){q.identity().rotateY((float)Math.toRadians(-yaw));qO.set(q);setYRot(yaw);entityData.set(ROT,new Quaternionf(q));}
  public boolean hasBomb(){return bombs()>0;}
  public boolean bomber(){return getType()==B2.get();}
  public int maxBombs(){return bomber()?B2_BOMBS:MAX_BOMBS;}
  public float maxHealth(){return bomber()?B2_HEALTH:MAX_HEALTH;}
  private int salvo;
  public int bombs(){return entityData.get(BOMBS);}
  /** Shot down: engine dead, controls gone, it spins down burning. */
  public boolean downed(){return entityData.get(DOWNED);}
  int downedTicks;
  public boolean firing(){return entityData.get(FIRING)>0;}
  public float syncedThrottle(){return entityData.get(THROTTLE);}
  public Vec3 forward(){var v=q.transform(new Vector3f(0,0,1));return new Vec3(v.x,v.y,v.z);}
  public Vec3 up(){var v=q.transform(new Vector3f(0,1,0));return new Vec3(v.x,v.y,v.z);}
  public Vec3 local(Vec3 p){var v=q.transform(new Vector3f((float)p.x,(float)p.y,(float)p.z));return position().add(v.x,v.y+CENTER_Y,v.z);}
  /** Bank angle in degrees, positive = right wing down. */
  public static float roll(Quaternionf q){
   var f=q.transform(new Vector3f(0,0,1));var u=q.transform(new Vector3f(0,1,0));
   var right=new Vector3f(f).cross(0,1,0);if(right.lengthSquared()<1e-6)return 0;right.normalize();var upRef=new Vector3f(right).cross(f).normalize();
   return (float)Math.toDegrees(Math.atan2(u.dot(right),u.dot(upRef)));
  }
  public static float yaw(Quaternionf q){var f=q.transform(new Vector3f(0,0,1));return (float)Math.toDegrees(Math.atan2(-f.x,f.z));}
  public static float pitch(Quaternionf q){var f=q.transform(new Vector3f(0,0,1));return (float)-Math.toDegrees(Math.asin(Mth.clamp(f.y,-1,1)));}

  @Override public LivingEntity getControllingPassenger(){return getFirstPassenger() instanceof Player p?p:null;}
  @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty();}
  @Override public boolean isPickable(){return !isRemoved();}
  @Override public boolean canBeCollidedWith(){return false;}
  @Override public boolean isPushable(){return false;}
  @Override public boolean isNoGravity(){return true;}   // our own gravity; also keeps servers from kicking the pilot for "floating"
  @Override public ItemStack getPickResult(){return new ItemStack(JET_ITEM.get());}
  @Override public net.minecraft.world.phys.AABB getBoundingBoxForCulling(){return getBoundingBox().inflate(3.4,2,3.4);}
  @Override protected void positionRider(Entity rider,Entity.MoveFunction move){
   if(!hasPassenger(rider))return;
   var seat=local(SEAT);move.accept(rider,seat.x,seat.y-rider.getEyeHeight(),seat.z);   // eye at the canopy, whatever the attitude
  }
  @Override public Vec3 getDismountLocationForPassenger(LivingEntity e){var s=super.getDismountLocationForPassenger(e);return s.y<getY()?new Vec3(s.x,getY()+getBbHeight(),s.z):s;}
  @Override public InteractionResult interact(Player player,InteractionHand hand){
   var held=player.getItemInHand(hand);
   if(level().isClientSide)return InteractionResult.SUCCESS;
   if(held.is(BOMB.get())){
    if(bombs()>=maxBombs())player.displayClientMessage(Component.literal((bomber()?"O B-2":"O caça")+" já tem "+maxBombs()+" bombas."),true);
    else{entityData.set(BOMBS,bombs()+1);if(!player.isCreative())held.shrink(1);level().playSound(null,blockPosition(),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.NEUTRAL,1F,.6F);player.displayClientMessage(Component.literal("Bombas: "+bombs()+"/"+maxBombs()+". [B] em voo para disparar."),true);}
    return InteractionResult.CONSUME;
   }
   if(player.isShiftKeyDown()&&held.isEmpty()&&getPassengers().isEmpty()&&onGround()){
    var s=new ItemStack(JET_ITEM.get());if(!player.getInventory().add(s))player.drop(s,false);
    if(hasBomb()){var b=new ItemStack(BOMB.get(),bombs());if(!player.getInventory().add(b))player.drop(b,false);}
    discard();return InteractionResult.CONSUME;
   }
   if(!player.isShiftKeyDown()&&getPassengers().isEmpty()&&!downed()){
    player.startRiding(this);
    player.displayClientMessage(Component.literal("W/S motor • mouse: arfagem e rolagem • A/D leme • clique/Espaço: metralhadoras • B: bomba • Shift: sair"),true);
   }
   return InteractionResult.CONSUME;
  }
  @Override public boolean hurt(DamageSource source,float amount){
   if(source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE))return false;   // 0.81.1: jets do not catch fire
   if(level().isClientSide||isRemoved()||isInvulnerableTo(source))return false;
   if(source.getEntity()!=null&&hasPassenger(source.getEntity()))return false;
   if(!source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)&&!source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)&&!(source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile)&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return false;   // bullets AND explosions (rockets, flak, tank shells, grenades) bring it down
   health-=amount;level().playSound(null,blockPosition(),SoundEvents.ANVIL_LAND,SoundSource.NEUTRAL,.4F,1.6F);
   if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.CRIT,getX(),getY()+CENTER_Y,getZ(),6,1,.4,1,.2);
   if(health<=0&&!downed()){
    entityData.set(DOWNED,true);
    level().playSound(null,blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.NEUTRAL,1.5F,1.3F);
    if(getControllingPassenger() instanceof Player p)p.displayClientMessage(Component.literal("§cABATIDO! Shift para sair."),true);
   }   // 0.65.2: never blown up at once in the air (a missile used to do that) — it always goes down burning and explodes when it hits the ground
   return true;
  }
  /** The crash: a huge fireball, fire on the ground, a shock ring of dust, metal scattered everywhere. */
  private void crash(){
   if(level().isClientSide||isRemoved())return;
   ejectPassengers();
   double x=getX(),y=getY()+1,z=getZ();
   level().explode(this,null,null,x,y,z,bomber()?9F:7F,true,Level.ExplosionInteraction.TNT,ParticleTypes.EXPLOSION,ParticleTypes.EXPLOSION_EMITTER,SoundEvents.GENERIC_EXPLODE);
   if(level() instanceof ServerLevel sl){
    sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,x,y,z,6,3,1,3,0);
    sl.sendParticles(ParticleTypes.FLAME,x,y+1,z,160,3,2,3,.25);
    sl.sendParticles(ParticleTypes.LAVA,x,y+1,z,40,2,1,2,0);
    sl.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,x,y+1,z,40,2.5,1,2.5,.05);
    sl.sendParticles(ParticleTypes.LARGE_SMOKE,x,y+1,z,140,3.5,2,3.5,.1);
    sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState()),x,y+1,z,120,2.5,1.5,2.5,.6);
    for(int i=0;i<48;i++){double a=i*Math.PI/24;sl.sendParticles(ParticleTypes.CLOUD,x+Math.cos(a)*2,y-.5,z+Math.sin(a)*2,0,Math.cos(a)*1.2,.05,Math.sin(a)*1.2,1);}   // shock ring
    level().playSound(null,blockPosition(),MareEletrica.C4_BLAST.get(),SoundSource.NEUTRAL,2F,.7F);
    level().playSound(null,blockPosition(),SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.NEUTRAL,1.5F,.6F);
   }
   discard();
  }
  private void explode(float power){
   if(level().isClientSide||isRemoved())return;
   ejectPassengers();
   level().explode(this,getX(),getY()+1,getZ(),power,Level.ExplosionInteraction.TNT);
   if(level() instanceof ServerLevel sl){sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,getX(),getY()+1,getZ(),3,1.5,.5,1.5,0);sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+1,getZ(),80,2,1,2,.05);}
   discard();
  }

  // ---------------- flight model ----------------
  /** Runs where the jet is simulated: the pilot's own game, or the server when nobody flies it. */
  private void fly(){
   if(downed()){spinDown();return;}
   float speed=(float)getDeltaMovement().length();
   float authority=Mth.clamp(speed/1.1F,.12F,1F);
   if(onGround())authority=Math.min(authority,.6F);
   // control surfaces: pitch around the wing axis, roll around the nose, rudder around the vertical
   float agile=bomber()?.45F:1F;
   q.rotateX((float)Math.toRadians(Mth.clamp(inPitch,-6F,6F)*authority*agile));
   q.rotateZ((float)Math.toRadians(Mth.clamp(inRoll,-9F,9F)*authority*agile));
   q.rotateY((float)Math.toRadians(Mth.clamp(inYaw,-1.8F,1.8F)*authority));
   float bank=roll(q);
   if(!onGround())q.rotateLocalY((float)Math.toRadians(-Math.sin(Math.toRadians(bank))*1.6F*authority));   // banked wings pull the nose round
   Vec3 fwd=forward(),up=up();
   boolean low=onGround()||!level().noCollision(this,getBoundingBox().move(0,-1.5,0));
   if(!low&&speed<.5F&&fwd.y>-.6)q.rotateX((float)Math.toRadians(1.2F));   // stall in the air: the nose drops
   if(low&&onGround()){float p=pitch(q),rl=roll(q);if(Math.abs(rl)>.5F)q.rotateZ((float)Math.toRadians(-rl*.25F));if(p>2F)q.rotateX((float)Math.toRadians(-p*.25F));}   // on the wheels: settle level, nose never digs in
   q.normalize();
   throttle=Mth.clamp(throttle+(inThrottle>0?.02F:inThrottle<0?-.06F:0F),0F,1F);
   boolean braking=inThrottle<0&&throttle<=0;
   Vec3 v=getDeltaMovement();
   double along=v.dot(fwd);
   v=v.scale(onGround()?(braking?.85:.95):(braking?.94:.975)).add(fwd.scale(throttle*(bomber()?.044:.055)));   // drag (S with the engine off = air brake / wheel brake) + thrust
   double lift=Math.min(.07,.045*(along/1.1)*(along/1.1))*(downed()?.35:1);if(along<0)lift=0;
   v=v.add(up.scale(lift)).add(0,-.045,0);                                                // lift along the wings' up, gravity down
   Vec3 aligned=fwd.scale(v.dot(fwd));v=v.add(aligned.subtract(v).scale(Mth.clamp(speed*.12,0,.2)));   // wings turn the flight path toward the nose
   if(onGround()&&v.y<0)v=new Vec3(v.x,0,v.z);
   setDeltaMovement(v);
   move(MoverType.SELF,v);
   setYRot(yaw(q));setXRot(pitch(q));
  }
  /** shot down: a flat spin around the nose while the nose swings down until it points at the ground, falling faster and faster */
  private void spinDown(){
   inThrottle=-1;inFire=false;throttle=Math.max(0,throttle-.05F);
   Vec3 fwd=forward();
   if(fwd.y>-.88){   // swing the nose toward the ground (world-space rotation around fwd × down)
    var axis=new org.joml.Vector3f((float)fwd.z,0,(float)-fwd.x);   // fwd × (0,-1,0)
    if(axis.lengthSquared()<1e-4F)axis.set(1,0,0);axis.normalize();
    q.premul(new Quaternionf().rotationAxis((float)Math.toRadians(2.6F+Math.min(3F,downedTicks*.05F)),axis.x,axis.y,axis.z));
   }
   q.rotateZ((float)Math.toRadians(Math.min(16F,6F+downedTicks*.15F)));   // the spin gets faster
   q.normalize();fwd=forward();
   Vec3 v=getDeltaMovement();double sp=Math.min(2.2,Math.max(v.length(),.6)+.025);
   v=v.scale(.9).add(fwd.scale(sp*.1)).add(0,-.06,0);
   if(v.length()>2.4)v=v.normalize().scale(2.4);
   setDeltaMovement(v);move(MoverType.SELF,v);
   setYRot(yaw(q));setXRot(pitch(q));
  }
  @Override public void tick(){clearFire();
   qO.set(q);
   super.tick();
   if(level().isClientSide){
    if(engineSound==null)startSound.accept(this);
    if(isControlledByLocalInstance())fly();
    else{
     q.set(entityData.get(ROT));throttle=entityData.get(THROTTLE);
     if(lerpSteps>0){setPos(getX()+(lx-getX())/lerpSteps,getY()+(ly-getY())/lerpSteps,getZ()+(lz-getZ())/lerpSteps);lerpSteps--;}
    }
    if(downed()){downedTicks++;   // burning engine and wing: a thick black trail that hangs in the air
     var n=local(new Vec3(0,.3,-1));var wing=local(new Vec3(random.nextBoolean()?2.2:-2.2,.2,.3));
     for(int i=0;i<3;i++)level().addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,n.x+random.nextGaussian()*.3,n.y,n.z+random.nextGaussian()*.3,0,.02,0);
     level().addParticle(ParticleTypes.LARGE_SMOKE,wing.x,wing.y,wing.z,0,.03,0);
     for(int i=0;i<3;i++)level().addParticle(ParticleTypes.FLAME,n.x+random.nextGaussian()*.5,n.y+random.nextGaussian()*.3,n.z+random.nextGaussian()*.5,0,.02,0);
     level().addParticle(ParticleTypes.LAVA,wing.x,wing.y,wing.z,0,0,0);}
    if(throttle>.75F&&tickCount%2==0){var n=local(NOZZLE);var v=forward().scale(-.25);level().addParticle(ParticleTypes.FLAME,n.x,n.y,n.z,v.x,v.y,v.z);level().addParticle(ParticleTypes.SMOKE,n.x,n.y,n.z,v.x,v.y,v.z);}
    return;
   }
   // ---------------- server ----------------
   if(health<0)health=maxHealth();
   if(salvo>0&&tickCount%4==0){salvo--;dropOne();}
   var pilot=getControllingPassenger();
   if(pilot==null){inPitch=inRoll=inYaw=0;inThrottle=-1;inFire=false;fly();entityData.set(ROT,new Quaternionf(q));}
   entityData.set(THROTTLE,throttle);
   float speed=(float)lastVel.length();
   // crashes: a healthy jet survives rough landings; only a really hard hit (or going down shot) destroys it
   if(downed()){downedTicks++;
    if(level() instanceof ServerLevel sl&&downedTicks%14==0){   // bits blow off on the way down
     sl.sendParticles(ParticleTypes.EXPLOSION,getX(),getY()+CENTER_Y,getZ(),2,1.2,.4,1.2,0);
     sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState()),getX(),getY()+CENTER_Y,getZ(),25,1,.5,1,.3);
     level().playSound(null,blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.NEUTRAL,1.2F,1.4F+random.nextFloat()*.3F);}
    if(downedTicks%30==1)level().playSound(null,blockPosition(),MareEletrica.SHELL_WHISTLE.get(),SoundSource.NEUTRAL,1.2F,.7F);
    if(horizontalCollision||verticalCollision||isInWater()||onGround()||downedTicks>600){crash();return;}}
   else if((horizontalCollision&&speed>1.7F)||(verticalCollision&&lastVel.y<-1.35)){explode(5F);return;}
   lastVel=pilot==null?getDeltaMovement():lastVel;
   // wing guns
   if(gunCooldown>0)gunCooldown--;
   entityData.set(FIRING,Math.max(0,entityData.get(FIRING)-1));
   if(inFire&&pilot!=null&&!downed()&&gunCooldown==0){if(bomber()){if(salvo==0)dropBomb();gunCooldown=20;}else{shoot(pilot);gunCooldown=1;entityData.set(FIRING,3);}}
   if(flareCooldown>0)flareCooldown--;
   if(pilot!=null&&tickCount%40==0&&level().getGameTime()-inputAt>40)inFire=false;
  }
  private long inputAt;private int serviceCount;
  /** Hangar: repairs the hull (and lifts the "shot down" state once fully repaired on the ground), then loads bombs one by one. */
  @Override public boolean service(){
   if(health<maxHealth()){health=Math.min(maxHealth(),health+6);if(health>=maxHealth()&&downed()&&onGround())entityData.set(DOWNED,false);return true;}
   if(downed()&&onGround()){entityData.set(DOWNED,false);return true;}
   if(bombs()<maxBombs()&&++serviceCount%6==0){entityData.set(BOMBS,bombs()+1);return true;}
   return false;
  }
  /** Server: the pilot's game reports attitude, throttle and velocity every tick (its position arrives with the vanilla vehicle packet). */
  public void input(ServerPlayer p,float qx,float qy,float qz,float qw,float thr,Vec3 vel,boolean fire,boolean bomb,boolean flare){
   if(getControllingPassenger()!=p)return;
   var nq=new Quaternionf(qx,qy,qz,qw);if(!Float.isFinite(nq.lengthSquared())||nq.lengthSquared()<.5F)return;nq.normalize();
   q.set(nq);entityData.set(ROT,new Quaternionf(q));throttle=Mth.clamp(thr,0,1);
   if(vel.lengthSqr()<64)lastVel=vel;setDeltaMovement(lastVel);
   inFire=fire;inputAt=level().getGameTime();
   if(bomb)dropBomb();
   if(flare)flares();
   setYRot(yaw(q));setXRot(pitch(q));
  }
  private void shoot(LivingEntity pilot){
   leftGun=!leftGun;var from=local(leftGun?GUN_L:GUN_R);var dir=forward();
   dir=dir.add(random.nextGaussian()*.012,random.nextGaussian()*.012,random.nextGaussian()*.012).normalize();
   Tracers.fire(level(),this,pilot,from,dir,6.5F,8F,getDeltaMovement());
   level().playSound(null,from.x,from.y,from.z,MareEletrica.JET_GUN.get(),SoundSource.NEUTRAL,1.5F,.9F+random.nextFloat()*.2F);
   if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.FLAME,from.x,from.y,from.z,1,0,0,0,0);
  }
  public void dropBomb(){
   if(bomber()){if(!level().isClientSide&&salvo==0&&hasBomb()&&!downed()){salvo=Math.min(4,bombs());level().playSound(null,blockPosition(),SoundEvents.IRON_DOOR_OPEN,SoundSource.NEUTRAL,1.5F,.5F);}return;}
   dropOne();
  }
  private void dropOne(){
   if(level().isClientSide||!hasBomb()||downed())return;
   int n=bombs();entityData.set(BOMBS,n-1);
   // fired from the pylon it hangs on (centre first, then the wings), with a rocket kick forward
   Vec3 slot=bomber()?new Vec3((n%2==0?.6:-.6),-.5,0):n==3?BOMB_AT:n==2?BOMB_AT.add(1.8,.25,0):BOMB_AT.add(-1.8,.25,0);
   var at=local(slot);var b=new JetBombEntity(BOMB_ENTITY.get(),level());b.setItem(new ItemStack(BOMB.get()));b.setOwner(getControllingPassenger());
   b.setPos(at.x,at.y,at.z);var f=forward();b.launch(bomber()?lastVel.scale(.95).add(0,-.15,0):lastVel.scale(.98).add(f.scale(.9)).add(0,-.05,0),f);level().addFreshEntity(b);
   level().playSound(null,blockPosition(),MareEletrica.LAUNCHER_FIRE.get(),SoundSource.NEUTRAL,1.2F,.7F);
  }
  /** Ejection seat (J): the pilot is blasted up out of the cockpit and a parachute opens by itself a moment later
   *  (no parachute item needed). In the air the empty jet goes down burning; on the ground you just hop out. */
  public void eject(ServerPlayer p){
   if(!hasPassenger(p))return;
   boolean air=!onGround()&&level().clip(new net.minecraft.world.level.ClipContext(position(),position().add(0,-4,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.ANY,this)).getType()==net.minecraft.world.phys.HitResult.Type.MISS;
   var up=new org.joml.Vector3f(0,1,0).rotate(q);Vec3 dir=new Vec3(up.x,Math.max(.35,up.y),up.z).normalize();
   p.stopRiding();
   if(!air)return;
   p.teleportTo(p.getX()+dir.x*1.5,p.getY()+dir.y*2.2,p.getZ()+dir.z*1.5);
   p.setDeltaMovement(getDeltaMovement().scale(.4).add(dir.scale(1.6)));p.hurtMarked=true;p.fallDistance=0;
   EjectSeat.arm(p);
   if(!downed()){entityData.set(DOWNED,true);}
   var sl=(ServerLevel)level();
   sl.sendParticles(ParticleTypes.EXPLOSION,p.getX(),p.getY(),p.getZ(),2,.3,.3,.3,0);sl.sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY(),p.getZ(),20,.3,.3,.3,.15);
   level().playSound(null,p.blockPosition(),SoundEvents.FIREWORK_ROCKET_LAUNCH,SoundSource.PLAYERS,1.5F,.6F);
   level().playSound(null,p.blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.PLAYERS,1.5F,1.6F);
   p.displayClientMessage(net.minecraft.network.chat.Component.literal("§e§lEJETADO! §7O paraquedas abre sozinho."),true);
  }
  /** Countermeasure flares thrown out to both sides. */
  public void flares(){
   if(level().isClientSide||flareCooldown>0||downed())return;flareCooldown=40;
   var f=forward();var side=new Vec3(f.z,0,-f.x).normalize();
   Flares.FlareEntity last=null;
   for(int i=-1;i<=1;i+=2){var at=local(new Vec3(0,0,-2));var fl=new Flares.FlareEntity(Flares.FLARE_ENTITY.get(),level());fl.setColor(4);fl.decoy=true;fl.setPos(at.x,at.y,at.z);fl.setDeltaMovement(lastVel.scale(.6).add(side.scale(i*.45)).add(0,-.1,0));level().addFreshEntity(fl);last=fl;}
   // missiles chasing this jet may go after the flares instead (like real countermeasures)
   for(var m:level().getEntitiesOfClass(Missiles.MissileEntity.class,getBoundingBox().inflate(120),m->m.targetId()==getId()))m.decoy(last);
   level().playSound(null,blockPosition(),SoundEvents.FIREWORK_ROCKET_LAUNCH,SoundSource.NEUTRAL,1.2F,.8F);
  }
  @Override public void lerpTo(double x,double y,double z,float yr,float xr,int steps){lx=x;ly=y;lz=z;lerpSteps=Math.max(steps,3);}
  @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key==ROT&&level().isClientSide&&!isControlledByLocalInstance()&&tickCount<2){q.set(entityData.get(ROT));qO.set(q);}}
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putFloat("Health",health);t.putInt("Bombs",bombs());t.putBoolean("Downed",downed());t.putFloat("QX",q.x);t.putFloat("QY",q.y);t.putFloat("QZ",q.z);t.putFloat("QW",q.w);}
  @Override protected void readAdditionalSaveData(CompoundTag t){health=t.contains("Health")?t.getFloat("Health"):maxHealth();entityData.set(BOMBS,t.contains("Bombs")?t.getInt("Bombs"):(t.getBoolean("Bomb")?1:0));entityData.set(DOWNED,t.getBoolean("Downed"));if(t.contains("QW")){q.set(t.getFloat("QX"),t.getFloat("QY"),t.getFloat("QZ"),t.getFloat("QW")).normalize();qO.set(q);entityData.set(ROT,new Quaternionf(q));}}
 }

 /** The big bomb: free fall with the jet's speed, whistles, and a huge blast. */
 public static final class JetBombEntity extends ThrowableItemProjectile {
  private boolean whistled;
  private Vec3 thrust=Vec3.ZERO;
  public JetBombEntity(EntityType<? extends JetBombEntity> type,Level level){super(type,level);}
  public void launch(Vec3 v,Vec3 dir){setDeltaMovement(v);thrust=new Vec3(dir.x,Math.min(dir.y,0),dir.z).normalize().scale(.07);}
  @Override protected Item getDefaultItem(){return BOMB.get();}
  @Override protected double getDefaultGravity(){return .05;}
  @Override public void tick(){
   super.tick();
   if(!level().isClientSide){var v=getDeltaMovement();setDeltaMovement(v.x*.985,v.y,v.z*.985);}   // air drag bleeds the forward speed: the path (and the nose, which follows it) curves down like a real bomb
   if(tickCount<12){if(!level().isClientSide)setDeltaMovement(getDeltaMovement().add(thrust.scale(.6)));else{var v=getDeltaMovement();level().addParticle(ParticleTypes.FLAME,getX()-v.x*.5,getY(),getZ()-v.z*.5,0,0,0);level().addParticle(ParticleTypes.LARGE_SMOKE,getX()-v.x,getY(),getZ()-v.z,0,.02,0);}}
   if(!level().isClientSide&&!whistled&&tickCount>20){whistled=true;level().playSound(null,getX(),getY(),getZ(),MareEletrica.SHELL_WHISTLE.get(),SoundSource.NEUTRAL,1.2F,.6F);}
   if(!level().isClientSide&&(tickCount>800||getY()<level().getMinBuildHeight()-16))discard();
  }
  @Override protected boolean canHitEntity(Entity e){return !(e instanceof JetEntity)&&super.canHitEntity(e);}
  @Override protected void onHit(HitResult result){
   super.onHit(result);
   if(level().isClientSide)return;
   level().explode(this,null,null,getX(),getY(),getZ(),18F,true,Level.ExplosionInteraction.TNT,ParticleTypes.EXPLOSION,ParticleTypes.EXPLOSION_EMITTER,MareEletrica.SILENT);
   level().playSound(null,getX(),getY(),getZ(),MareEletrica.C4_BLAST.get(),SoundSource.NEUTRAL,3F,.7F);
   if(level() instanceof ServerLevel sl){sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,getX(),getY()+1,getZ(),8,3,1.5,3,0);sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+2,getZ(),200,4,3,4,.1);}
   discard();
  }
 }
}
