package br.mareeletrica;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** Giant Evil Duck boss (0.71.0; 0.72.0 rework). It keeps its distance and circles you, hops around flapping, and fights at range:
 *  volleys of feather darts, eggs lobbed in arcs (cluster eggs in rage), a sonic quack that sends a ring-shaped shockwave along the
 *  ground (jump over it), a dive bomb from the sky onto a marked spot, a dash, and in rage it calls kamikaze ducklings. */
public final class DuckBoss {
 private DuckBoss(){}
 public static final Supplier<EntityType<EvilDuck>> BOSS=MareEletrica.ACTORS.register("evil_duck",()->
  EntityType.Builder.<EvilDuck>of(EvilDuck::new,MobCategory.MONSTER).sized(2.6F,3.9F).eyeHeight(3.4F).fireImmune().clientTrackingRange(16).build(MareEletrica.ID+":evil_duck"));
 public static final Supplier<EntityType<EvilEgg>> EGG=MareEletrica.ACTORS.register("evil_egg",()->
  EntityType.Builder.<EvilEgg>of(EvilEgg::new,MobCategory.MISC).sized(.6F,.6F).clientTrackingRange(10).updateInterval(2).build(MareEletrica.ID+":evil_egg"));
 public static final Supplier<EntityType<Feather>> FEATHER=MareEletrica.ACTORS.register("evil_feather",()->
  EntityType.Builder.<Feather>of(Feather::new,MobCategory.MISC).sized(.35F,.35F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":evil_feather"));
 public static final Supplier<EntityType<Shockwave>> WAVE=MareEletrica.ACTORS.register("quack_wave",()->
  EntityType.Builder.<Shockwave>of(Shockwave::new,MobCategory.MISC).sized(1F,1F).clientTrackingRange(10).updateInterval(20).build(MareEletrica.ID+":quack_wave"));
 public static final Supplier<EntityType<Duckling>> DUCKLING=MareEletrica.ACTORS.register("evil_duckling",()->
  EntityType.Builder.<Duckling>of(Duckling::new,MobCategory.MONSTER).sized(.6F,.7F).clientTrackingRange(10).build(MareEletrica.ID+":evil_duckling"));
 public static final DeferredItem<Item> CURSED_EGG=MareEletrica.ITEMS.register("cursed_duck_egg",()->new CursedEgg(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC)));
 public static final DeferredItem<Item> STAFF=MareEletrica.ITEMS.register("quack_staff",()->new QuackStaff(new Item.Properties().durability(300).rarity(net.minecraft.world.item.Rarity.EPIC)));
 public static void register(IEventBus bus){bus.addListener(DuckBoss::attributes);}
 static void attributes(EntityAttributeCreationEvent e){e.put(BOSS.get(),EvilDuck.attributes().build());
  e.put(DUCKLING.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,6).add(Attributes.MOVEMENT_SPEED,.36).add(Attributes.FOLLOW_RANGE,32).add(Attributes.ATTACK_DAMAGE,2).build());}
 public static void creative(java.util.function.Consumer<ItemStack> out){out.accept(new ItemStack(CURSED_EGG.get()));out.accept(new ItemStack(STAFF.get()));}
 static SoundEvent quack(){return Pato.QUACK.get();}
 static boolean foe(Entity e,Entity boss){return e!=boss&&e instanceof LivingEntity l&&l.isAlive()&&!(e instanceof EvilDuck)&&!(e instanceof Duckling)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()));}

 public static final class EvilDuck extends Monster {
  static final EntityDataAccessor<Integer> ATTACK=SynchedEntityData.defineId(EvilDuck.class,EntityDataSerializers.INT);
  static final EntityDataAccessor<Boolean> RAGE=SynchedEntityData.defineId(EvilDuck.class,EntityDataSerializers.BOOLEAN);
  public static final int NONE=0,QUACK=1,EGGS=2,CHARGE=3,FEATHERS=4,DIVE=5,SUMMON=6,HOP=7;
  final ServerBossEvent bar=new ServerBossEvent(Component.literal("§5Pato Gigante Malvado"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.NOTCHED_10);
  int cooldown=50,timer,orbitDir=1,lastAttack=-1;Vec3 dash,diveAt;
  public float flapO,flap;public int quackAt=-100,peckAt=-100;
  public EvilDuck(EntityType<? extends Monster> t,Level l){super(t,l);xpReward=500;setPersistenceRequired();}
  public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,400).add(Attributes.ATTACK_DAMAGE,12).add(Attributes.MOVEMENT_SPEED,.3)
   .add(Attributes.FOLLOW_RANGE,64).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.ARMOR,8).add(Attributes.ATTACK_KNOCKBACK,1.5).add(Attributes.STEP_HEIGHT,1.6);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ATTACK,0);b.define(RAGE,false);}
  public int attack(){return entityData.get(ATTACK);}
  public boolean rage(){return entityData.get(RAGE);}
  public int attackTime(){return timer;}
  @Override protected void registerGoals(){
   goalSelector.addGoal(0,new FloatGoal(this));
   goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,32));
   targetSelector.addGoal(1,new HurtByTargetGoal(this));
   targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,false));
  }
  @Override public boolean doHurtTarget(Entity t){peckAt=tickCount;level().broadcastEntityEvent(this,(byte)60);return super.doHurtTarget(t);}
  @Override public void handleEntityEvent(byte id){if(id==60)peckAt=tickCount;else if(id==61)quackAt=tickCount;else super.handleEntityEvent(id);}
  void setAttack(int a){entityData.set(ATTACK,a);timer=0;}
  /** tests only */ public void force(int a){setAttack(a);if(a==QUACK){sound(2F,.7F);level().broadcastEntityEvent(this,(byte)61);}}
  @Override public void aiStep(){
   super.aiStep();
   flapO=flap;int a=attack();
   boolean flapping=a==QUACK||a==EGGS||a==HOP||a==DIVE||a==SUMMON||!onGround();
   flap=flapping?flap+.6F:flap*.85F;
   if(level().isClientSide){
    if(a==QUACK&&tickCount%2==0)for(int i=0;i<3;i++)level().addParticle(ParticleTypes.PORTAL,getRandomX(1.5),getRandomY(),getRandomZ(1.5),0,.2,0);
    if(rage()&&tickCount%3==0)level().addParticle(ParticleTypes.ANGRY_VILLAGER,getRandomX(1),getY()+3.8,getRandomZ(1),0,0,0);
    if(a==CHARGE&&timer>15)level().addParticle(ParticleTypes.CLOUD,getX(),getY()+.2,getZ(),0,.05,0);
    if(a==DIVE&&!onGround())level().addParticle(ParticleTypes.CLOUD,getRandomX(1),getY(),getRandomZ(1),0,-.1,0);
    timer++;return;}
   timer++;
   if(!rage()&&getHealth()<getMaxHealth()/2){entityData.set(RAGE,true);bar.setColor(BossEvent.BossBarColor.RED);bar.setName(Component.literal("§cPato Gigante Malvado §4(FÚRIA)"));
    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.38);sound(2.5F,.4F);level().broadcastEntityEvent(this,(byte)61);
    for(var p:players(48))p.displayClientMessage(Component.literal("§4🦆 O Pato Gigante Malvado entrou em FÚRIA!"),true);
    setAttack(SUMMON);}
   var target=getTarget();if(target!=null&&!target.isAlive())target=null;
   switch(a){
    case NONE->{
     if(target!=null)keepDistance(target);
     if(--cooldown<=0&&target!=null)choose(target);}
    case QUACK->{getNavigation().stop();if(timer==26)sonicQuack();if(timer>=38)finish();}
    case FEATHERS->{getNavigation().stop();if(target!=null)getLookControl().setLookAt(target);
     if(timer==8||timer==16||(rage()&&timer==24))if(target!=null)featherVolley(target);
     if(timer>=(rage()?34:26))finish();}
    case EGGS->{getNavigation().stop();
     if(timer==1){setDeltaMovement(getDeltaMovement().x,.9,getDeltaMovement().z);sound(2F,.9F);}
     if(timer>8&&timer<44&&timer%(rage()?4:7)==0&&target!=null){var ps=players(36);if(!ps.isEmpty())lobEgg(ps.get(random.nextInt(ps.size())),rage());}
     if(timer>=56)finish();}
    case HOP->{if(timer==1&&target!=null){var side=target.position().subtract(position()).multiply(1,0,1).normalize().yRot((float)(Math.PI/2*orbitDir));setDeltaMovement(side.x*1.1,1.0,side.z*1.1);}
     if(timer>4&&onGround()||timer>40)finish();}
    case CHARGE->{
     if(timer<15){getNavigation().stop();if(target!=null)getLookControl().setLookAt(target);if(timer==1)sound(2F,.55F);}
     else if(timer==15){if(target==null){finish();break;}var d=target.position().subtract(position()).multiply(1,0,1);dash=d.lengthSqr()<1e-4?Vec3.ZERO:d.normalize().scale(rage()?1.2:.95);
      setYRot((float)(Mth.atan2(dash.z,dash.x)*180/Math.PI)-90);yBodyRot=getYRot();}
     else if(timer<40){setDeltaMovement(dash.x,getDeltaMovement().y,dash.z);
      for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(.6),x->foe(x,this)))
       if(e.hurt(damageSources().mobAttack(this),rage()?16:12))e.push(dash.x*1.8,.7,dash.z*1.8);
      if(horizontalCollision&&timer>18){level().playSound(null,blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.HOSTILE,1F,.7F);setDeltaMovement(Vec3.ZERO);finish();}}
     else finish();}
    case DIVE->dive(target);
    case SUMMON->{getNavigation().stop();
     if(timer==10){sound(3F,1.4F);int n=rage()?4:3;for(int i=0;i<n;i++){var d=DUCKLING.get().create(level());if(d==null)continue;double an=i*Math.PI*2/n;
      d.moveTo(getX()+Math.cos(an)*2.4,getY()+1.5,getZ()+Math.sin(an)*2.4,random.nextFloat()*360,0);d.setDeltaMovement(Math.cos(an)*.3,.5,Math.sin(an)*.3);if(target!=null)d.setTarget(target);level().addFreshEntity(d);
      ((ServerLevel)level()).sendParticles(ParticleTypes.POOF,d.getX(),d.getY(),d.getZ(),8,.3,.3,.3,.02);}}
     if(timer>=30)finish();}
    default->finish();
   }
  }
  /** circle the target at 9–15 blocks, backing off when it gets close */
  void keepDistance(LivingEntity t){
   var to=t.position().subtract(position());double d=Math.sqrt(to.x*to.x+to.z*to.z);if(d<1e-3)return;
   var dir=new Vec3(to.x/d,0,to.z/d);var side=new Vec3(-dir.z*orbitDir,0,dir.x*orbitDir);
   double want=rage()?9:12;Vec3 move=side.scale(1).add(dir.scale(Mth.clamp((d-want)*.25,-1.2,1.2)));
   var goal=position().add(move.normalize().scale(4));
   getMoveControl().setWantedPosition(goal.x,getY(),goal.z,rage()?1.25:1.0);
   getLookControl().setLookAt(t,30,30);
   if(horizontalCollision&&onGround()){orbitDir=-orbitDir;setDeltaMovement(getDeltaMovement().add(0,.6,0));}
   if(random.nextInt(160)==0)orbitDir=-orbitDir;
   if(d<4.2&&tickCount%14==0&&getBoundingBox().inflate(1.2).intersects(t.getBoundingBox()))doHurtTarget(t);
  }
  void choose(LivingEntity t){
   double d=distanceTo(t);int pick;
   if(d<6)pick=random.nextInt(3)==0?CHARGE:QUACK;
   else{int[] opts=rage()?new int[]{FEATHERS,EGGS,DIVE,CHARGE,HOP,FEATHERS,QUACK,EGGS,DIVE}:new int[]{FEATHERS,EGGS,HOP,CHARGE,FEATHERS,QUACK,EGGS};
    pick=opts[random.nextInt(opts.length)];if(pick==lastAttack)pick=opts[random.nextInt(opts.length)];}
   if(rage()&&random.nextInt(9)==0&&level().getEntitiesOfClass(Duckling.class,getBoundingBox().inflate(30)).size()<3)pick=SUMMON;
   lastAttack=pick;setAttack(pick);
   if(pick==QUACK){sound(2F,.7F);level().broadcastEntityEvent(this,(byte)61);}
  }
  void dive(LivingEntity t){
   if(timer==1){setDeltaMovement(0,1.7,0);sound(2.5F,.8F);}
   if(timer>=10&&timer<34){setNoGravity(true);
    if(t!=null){diveAt=t.position();var to=diveAt.add(0,9,0).subtract(position());setDeltaMovement(to.scale(.12));}
    if(timer==12&&diveAt!=null){var m=WAVE.get().create(level());if(m!=null){m.moveTo(diveAt.x,diveAt.y+.05,diveAt.z);m.marker=true;m.setOwner(this);level().addFreshEntity(m);}}}
   if(timer==34){setNoGravity(false);setDeltaMovement(0,-2.6,0);}
   if(timer>34&&onGround()){
    if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.EXPLOSION,getX(),getY()+.5,getZ(),6,1.5,.3,1.5,0);
    level().playSound(null,blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.HOSTILE,2F,.6F);
    for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(4),x->foe(x,this))){var n=e.position().subtract(position()).multiply(1,0,1).normalize();e.hurt(damageSources().mobAttack(this),rage()?16:12);e.push(n.x*1.6,.8,n.z*1.6);e.hurtMarked=true;}
    var w=WAVE.get().create(level());if(w!=null){w.moveTo(getX(),getY()+.05,getZ());w.setOwner(this);w.damage=rage()?8:6;level().addFreshEntity(w);}
    finish();}
   if(timer>80){setNoGravity(false);finish();}
  }
  void finish(){setNoGravity(false);setAttack(NONE);cooldown=(rage()?18:34)+random.nextInt(rage()?18:30);}
  void sound(float vol,float pitch){level().playSound(null,getX(),getY(),getZ(),quack(),SoundSource.HOSTILE,vol,pitch*(rage()?.85F:1F));}
  List<Player> players(double r){return level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(r),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator());}
  void sonicQuack(){
   sound(4F,.45F);level().broadcastEntityEvent(this,(byte)61);
   var w=WAVE.get().create(level());if(w!=null){w.moveTo(getX(),getY()+.05,getZ());w.setOwner(this);w.damage=rage()?10:7;level().addFreshEntity(w);}
   // anyone hugging the duck gets blown away at once
   for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2.5),x->foe(x,this))){var n=e.position().subtract(position()).multiply(1,0,1).normalize();e.hurt(damageSources().sonicBoom(this),6);e.push(n.x*2.4,.8,n.z*2.4);e.hurtMarked=true;}
  }
  void featherVolley(LivingEntity t){
   level().playSound(null,getX(),getY(),getZ(),SoundEvents.PHANTOM_FLAP,SoundSource.HOSTILE,2F,1.4F);
   int n=rage()?7:5;var eye=new Vec3(getX(),getY()+2.6,getZ());
   for(int i=0;i<n;i++){var f=new Feather(FEATHER.get(),level());f.setOwner(this);f.setPos(eye.x,eye.y,eye.z);
    var to=t.position().add(0,t.getBbHeight()*.6,0).subtract(eye);double spread=(i-(n-1)/2.0)*.09;var dir=to.normalize().yRot((float)spread);
    f.shoot(dir.x,dir.y+to.length()*.006,dir.z,1.7F,1.5F);level().addFreshEntity(f);}
  }
  void lobEgg(Player p,boolean cluster){
   var egg=new EvilEgg(EGG.get(),level());egg.setOwner(this);var from=new Vec3(getX(),getY()+3.2,getZ());egg.setPos(from.x,from.y,from.z);
   var aim=p.position().add(p.getDeltaMovement().scale(12)).add(random.nextGaussian()*1.5,0,random.nextGaussian()*1.5);
   var d=aim.subtract(from);double hd=Math.sqrt(d.x*d.x+d.z*d.z);double t=Mth.clamp(hd*1.4,14,42);double g=.04;
   egg.setDeltaMovement(d.x/t,(d.y+.5*g*t*t)/t,d.z/t);egg.setCluster(cluster);level().addFreshEntity(egg);}
  @Override public boolean ignoreExplosion(Explosion e){return true;}
  @Override public boolean causeFallDamage(float d,float m,DamageSource s){return false;}
  @Override public boolean removeWhenFarAway(double d){return false;}
  @Override public boolean canChangeDimensions(Level a,Level b){return false;}
  @Override protected SoundEvent getAmbientSound(){return quack();}
  @Override public float getVoicePitch(){return rage()?.45F:.55F;}
  @Override protected float getSoundVolume(){return 2.5F;}
  @Override protected SoundEvent getHurtSound(DamageSource s){return quack();}
  @Override protected SoundEvent getDeathSound(){return quack();}
  @Override public int getAmbientSoundInterval(){return 140;}
  @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
  @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
  @Override protected void customServerAiStep(){super.customServerAiStep();bar.setProgress(getHealth()/getMaxHealth());}
  @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putBoolean("Rage",rage());}
  @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);if(t.getBoolean("Rage"))entityData.set(RAGE,true);if(hasCustomName())bar.setName(getDisplayName());}
  @Override protected void dropCustomDeathLoot(ServerLevel l,DamageSource s,boolean hit){
   super.dropCustomDeathLoot(l,s,hit);
   spawnAtLocation(new ItemStack(STAFF.get()));spawnAtLocation(new ItemStack(Pato.GOLDEN_EGG.get(),3));spawnAtLocation(new ItemStack(Items.FEATHER,16+random.nextInt(9)));
   spawnAtLocation(new ItemStack(Pato.HAT_ITEMS.get(2).get()));}
  @Override public void die(DamageSource s){super.die(s);if(level() instanceof ServerLevel sl){sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,getX(),getY()+2,getZ(),2,1,1,1,0);
   for(var d:sl.getEntitiesOfClass(Duckling.class,getBoundingBox().inflate(40)))d.discard();
   for(var p:players(64))p.displayClientMessage(Component.literal("§a🏆 O Pato Gigante Malvado foi derrotado!"),true);}}
 }

 /** an egg lobbed in an arc: explodes on impact (no block damage); in rage it splits into three small eggs */
 public static final class EvilEgg extends ThrowableProjectile {
  static final EntityDataAccessor<Boolean> SMALL=SynchedEntityData.defineId(EvilEgg.class,EntityDataSerializers.BOOLEAN);
  boolean cluster;public float spin=(float)(Math.random()*360);
  public EvilEgg(EntityType<? extends ThrowableProjectile> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(SMALL,false);}
  public boolean small(){return entityData.get(SMALL);}
  void setCluster(boolean c){cluster=c;}
  @Override protected double getDefaultGravity(){return .04;}
  @Override public void tick(){super.tick();if(level().isClientSide)level().addParticle(small()?ParticleTypes.SMOKE:ParticleTypes.WITCH,getX(),getY(),getZ(),0,0,0);if(tickCount>200)discard();}
  @Override protected boolean canHitEntity(Entity e){return super.canHitEntity(e)&&!(e instanceof EvilDuck)&&!(e instanceof Duckling)&&!(e instanceof EvilEgg);}
  @Override protected void onHit(HitResult r){super.onHit(r);if(level().isClientSide)return;
   level().explode(getOwner(),getX(),getY(),getZ(),small()?1.4F:2.3F,Level.ExplosionInteraction.NONE);
   if(cluster&&!small())for(int i=0;i<3;i++){var e=new EvilEgg(EGG.get(),level());e.setOwner(getOwner());e.entityData.set(SMALL,true);e.setPos(getX(),getY()+.6,getZ());
    double an=i*Math.PI*2/3+random.nextDouble();e.setDeltaMovement(Math.cos(an)*.32,.55,Math.sin(an)*.32);level().addFreshEntity(e);}
   discard();}
 }
 /** a feather dart */
 public static final class Feather extends ThrowableProjectile {
  public Feather(EntityType<? extends ThrowableProjectile> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override protected double getDefaultGravity(){return .008;}
  @Override public void tick(){super.tick();if(tickCount>80)discard();if(level().isClientSide&&tickCount%2==0)level().addParticle(ParticleTypes.CLOUD,getX(),getY(),getZ(),0,0,0);}
  @Override protected boolean canHitEntity(Entity e){return super.canHitEntity(e)&&!(e instanceof EvilDuck)&&!(e instanceof Duckling)&&!(e instanceof Feather);}
  @Override protected void onHitEntity(EntityHitResult r){super.onHitEntity(r);if(!level().isClientSide){var o=getOwner();r.getEntity().hurt(damageSources().mobProjectile(this,o instanceof LivingEntity l?l:null),5);}}
  @Override protected void onHit(HitResult r){super.onHit(r);if(!level().isClientSide){level().playSound(null,getX(),getY(),getZ(),SoundEvents.ARROW_HIT,SoundSource.HOSTILE,.6F,1.6F);discard();}}
 }
 /** the sonic quack's shockwave: a ring that grows along the ground; jump to let it pass under you. As a marker it is a red target circle. */
 public static final class Shockwave extends Entity {
  static final EntityDataAccessor<Boolean> MARK=SynchedEntityData.defineId(Shockwave.class,EntityDataSerializers.BOOLEAN);
  public boolean marker;public float damage=7;UUID owner;final Set<UUID> hit=new HashSet<>();
  public static final float SPEED=.5F,MAX=18F;
  public Shockwave(EntityType<?> t,Level l){super(t,l);noPhysics=true;}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(MARK,false);}
  public boolean isMarker(){return entityData.get(MARK);}
  void setOwner(Entity e){owner=e.getUUID();}
  public float radius(float pt){return isMarker()?3.2F:(tickCount+pt)*SPEED+1F;}
  @Override public void tick(){
   super.tick();
   if(level().isClientSide)return;
   if(marker&&!isMarker())entityData.set(MARK,true);
   if(isMarker()){if(tickCount>26)discard();return;}
   float r=radius(0);if(r>MAX){discard();return;}
   var boss=owner==null||!(level() instanceof ServerLevel sl)?null:sl.getEntity(owner);
   for(var e:level().getEntitiesOfClass(LivingEntity.class,new AABB(position(),position()).inflate(r+1,3,r+1),x->foe(x,boss)&&!hit.contains(x.getUUID()))){
    double dx=e.getX()-getX(),dz=e.getZ()-getZ(),d=Math.sqrt(dx*dx+dz*dz);
    if(d<r-1.2||d>r+.4)continue;
    if(e.getY()-getY()>1.1)continue;   // jumped over it
    hit.add(e.getUUID());
    e.hurt(boss instanceof LivingEntity l?damageSources().sonicBoom(l):damageSources().magic(),damage);
    var n=d<1e-3?new Vec3(1,0,0):new Vec3(dx/d,0,dz/d);e.push(n.x*1.5,.55,n.z*1.5);e.hurtMarked=true;
    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,1));
   }
  }
  @Override protected void readAdditionalSaveData(CompoundTag t){}
  @Override protected void addAdditionalSaveData(CompoundTag t){}
  @Override public boolean shouldBeSaved(){return false;}
 }
 /** a kamikaze duckling: runs at you, puffs up and pops */
 public static final class Duckling extends Monster {
  static final EntityDataAccessor<Integer> FUSE=SynchedEntityData.defineId(Duckling.class,EntityDataSerializers.INT);
  public Duckling(EntityType<? extends Monster> t,Level l){super(t,l);xpReward=2;}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(FUSE,0);}
  public int fuse(){return entityData.get(FUSE);}
  @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1.2,true));targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true));}
  @Override public boolean doHurtTarget(Entity t){if(fuse()==0)entityData.set(FUSE,1);return true;}
  @Override public void tick(){super.tick();
   if(!level().isClientSide){int f=fuse();if(f>0){entityData.set(FUSE,f+1);if(f==1)playSound(SoundEvents.CREEPER_PRIMED,1F,1.8F);if(f>=18){level().explode(this,getX(),getY()+.3,getZ(),1.6F,Level.ExplosionInteraction.NONE);discard();}}
    else if(getTarget()!=null&&distanceToSqr(getTarget())<2.6)entityData.set(FUSE,1);
    if(tickCount>600)discard();}}
  @Override protected SoundEvent getAmbientSound(){return quack();}
  @Override public float getVoicePitch(){return 1.9F;}
  @Override protected SoundEvent getHurtSound(DamageSource s){return quack();}
  @Override public boolean removeWhenFarAway(double d){return true;}
  @Override public boolean causeFallDamage(float d,float m,DamageSource s){return false;}
 }

 public static final class CursedEgg extends Item {
  CursedEgg(Properties p){super(p);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§5Use no chão para chamar o Pato Gigante Malvado."));t.add(Component.literal("§8Chefão: 400 de vida, quack sônico, chuva de ovos."));}
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();if(!(l instanceof ServerLevel sl))return InteractionResult.SUCCESS;
   BlockPos at=c.getClickedPos().relative(c.getClickedFace());
   if(!sl.getEntitiesOfClass(EvilDuck.class,new net.minecraft.world.phys.AABB(at).inflate(96)).isEmpty()){if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("§cJá tem um Pato Gigante Malvado por perto!"),true);return InteractionResult.FAIL;}
   var bolt=EntityType.LIGHTNING_BOLT.create(sl);if(bolt!=null){bolt.moveTo(Vec3.atBottomCenterOf(at));bolt.setVisualOnly(true);sl.addFreshEntity(bolt);}
   var boss=BOSS.get().create(sl);if(boss==null)return InteractionResult.FAIL;
   boss.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,c.getPlayer()==null?0:c.getPlayer().getYRot()+180,0);
   boss.finalizeSpawn(sl,sl.getCurrentDifficultyAt(at),MobSpawnType.MOB_SUMMONED,null);sl.addFreshEntity(boss);
   sl.playSound(null,at,quack(),SoundSource.HOSTILE,4F,.4F);
   for(var p:sl.getEntitiesOfClass(Player.class,new net.minecraft.world.phys.AABB(at).inflate(48)))p.displayClientMessage(Component.literal("§5🦆 O Pato Gigante Malvado despertou!"),true);
   if(c.getPlayer()==null||!c.getPlayer().getAbilities().instabuild)c.getItemInHand().shrink(1);
   return InteractionResult.CONSUME;}
 }

 /** the boss's drop: a sonic quack in front of you */
 public static final class QuackStaff extends Item {
  QuackStaff(Properties p){super(p);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Clique direito: quack sônico que empurra e machuca"));t.add(Component.literal("§7tudo na frente (recarga de 2 segundos)."));}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);
   if(!l.isClientSide){
    l.playSound(null,p.getX(),p.getY(),p.getZ(),quack(),SoundSource.PLAYERS,2F,.7F);
    var look=p.getLookAngle();
    if(l instanceof ServerLevel sl)for(int i=1;i<=4;i++){var c=p.getEyePosition().add(look.scale(i*1.8));sl.sendParticles(i==2?ParticleTypes.SONIC_BOOM:ParticleTypes.CLOUD,c.x,c.y,c.z,i==2?1:6,.2,.2,.2,0);}
    for(var e:l.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(9),x->x!=p)){
     var d=e.position().add(0,e.getBbHeight()/2,0).subtract(p.getEyePosition());double dist=d.length();if(dist>9||dist<.01||d.normalize().dot(look)<.55)continue;
     e.hurt(p.damageSources().indirectMagic(p,p),8);var n=d.multiply(1,0,1).normalize();e.push(n.x*1.8,.55,n.z*1.8);e.hurtMarked=true;}
    s.hurtAndBreak(1,p,net.minecraft.world.entity.LivingEntity.getSlotForHand(h));}
   p.getCooldowns().addCooldown(this,40);
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
 }
}
