package br.mareeletrica;

import java.util.List;
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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.71.0: the Giant Evil Duck — a boss summoned with the Cursed Egg. Pecks, charges, a sonic quack that throws everyone back
 *  and a rain of exploding eggs; at half health it goes into a rage (red, faster, twice the eggs). Drops the Quack Staff. */
public final class DuckBoss {
 private DuckBoss(){}
 public static final Supplier<EntityType<EvilDuck>> BOSS=MareEletrica.ACTORS.register("evil_duck",()->
  EntityType.Builder.<EvilDuck>of(EvilDuck::new,MobCategory.MONSTER).sized(2.6F,3.9F).eyeHeight(3.4F).fireImmune().clientTrackingRange(16).build(MareEletrica.ID+":evil_duck"));
 public static final Supplier<EntityType<EvilEgg>> EGG=MareEletrica.ACTORS.register("evil_egg",()->
  EntityType.Builder.<EvilEgg>of(EvilEgg::new,MobCategory.MISC).sized(.5F,.5F).clientTrackingRange(10).updateInterval(5).build(MareEletrica.ID+":evil_egg"));
 public static final DeferredItem<Item> CURSED_EGG=MareEletrica.ITEMS.register("cursed_duck_egg",()->new CursedEgg(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC)));
 public static final DeferredItem<Item> STAFF=MareEletrica.ITEMS.register("quack_staff",()->new QuackStaff(new Item.Properties().durability(300).rarity(net.minecraft.world.item.Rarity.EPIC)));
 public static void register(IEventBus bus){bus.addListener(DuckBoss::attributes);}
 static void attributes(EntityAttributeCreationEvent e){e.put(BOSS.get(),EvilDuck.attributes().build());}
 public static void creative(java.util.function.Consumer<ItemStack> out){out.accept(new ItemStack(CURSED_EGG.get()));out.accept(new ItemStack(STAFF.get()));}
 static SoundEvent quack(){return Pato.QUACK.get();}

 public static final class EvilDuck extends Monster {
  static final EntityDataAccessor<Integer> ATTACK=SynchedEntityData.defineId(EvilDuck.class,EntityDataSerializers.INT);
  static final EntityDataAccessor<Boolean> RAGE=SynchedEntityData.defineId(EvilDuck.class,EntityDataSerializers.BOOLEAN);
  public static final int NONE=0,QUACK=1,EGGS=2,CHARGE=3;
  final ServerBossEvent bar=new ServerBossEvent(Component.literal("§5Pato Gigante Malvado"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.NOTCHED_10);
  int cooldown=60,timer;Vec3 dash;
  public float flapO,flap;public int quackAt=-100,peckAt=-100;
  public EvilDuck(EntityType<? extends Monster> t,Level l){super(t,l);xpReward=500;setPersistenceRequired();}
  public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,400).add(Attributes.ATTACK_DAMAGE,12).add(Attributes.MOVEMENT_SPEED,.27)
   .add(Attributes.FOLLOW_RANGE,48).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.ARMOR,8).add(Attributes.ATTACK_KNOCKBACK,1.5).add(Attributes.STEP_HEIGHT,1.6);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ATTACK,0);b.define(RAGE,false);}
  public int attack(){return entityData.get(ATTACK);}
  public boolean rage(){return entityData.get(RAGE);}
  public int attackTime(){return timer;}
  @Override protected void registerGoals(){
   goalSelector.addGoal(0,new FloatGoal(this));
   goalSelector.addGoal(2,new MeleeAttackGoal(this,1.1,true){@Override public boolean canUse(){return attack()==NONE&&super.canUse();}@Override public boolean canContinueToUse(){return attack()==NONE&&super.canContinueToUse();}});
   goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.8));
   goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,24));
   goalSelector.addGoal(7,new RandomLookAroundGoal(this));
   targetSelector.addGoal(1,new HurtByTargetGoal(this));
   targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
  }
  @Override public boolean doHurtTarget(Entity t){peckAt=tickCount;level().broadcastEntityEvent(this,(byte)60);return super.doHurtTarget(t);}
  @Override public void handleEntityEvent(byte id){if(id==60)peckAt=tickCount;else if(id==61)quackAt=tickCount;else super.handleEntityEvent(id);}
  void setAttack(int a){entityData.set(ATTACK,a);timer=0;}
  @Override public void aiStep(){
   super.aiStep();
   flapO=flap;int a=attack();
   boolean flapping=a==QUACK||a==EGGS||!onGround();
   flap=flapping?flap+.6F:flap*.85F;
   if(level().isClientSide){
    if(a==QUACK&&tickCount%2==0)for(int i=0;i<3;i++)level().addParticle(ParticleTypes.PORTAL,getRandomX(1.5),getRandomY(),getRandomZ(1.5),0,.2,0);
    if(rage()&&tickCount%3==0)level().addParticle(ParticleTypes.ANGRY_VILLAGER,getRandomX(1),getY()+3.8,getRandomZ(1),0,0,0);
    if(a==CHARGE&&timer>15)level().addParticle(ParticleTypes.CLOUD,getX(),getY()+.2,getZ(),0,.05,0);
    timer++;return;}
   timer++;
   if(!rage()&&getHealth()<getMaxHealth()/2){entityData.set(RAGE,true);bar.setColor(BossEvent.BossBarColor.RED);bar.setName(Component.literal("§cPato Gigante Malvado §4(FÚRIA)"));
    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.34);sound(2.5F,.4F);level().broadcastEntityEvent(this,(byte)61);
    for(var p:players(48))p.displayClientMessage(Component.literal("§4🦆 O Pato Gigante Malvado entrou em FÚRIA!"),true);}
   var target=getTarget();
   switch(a){
    case NONE->{if(--cooldown<=0&&target!=null&&target.isAlive()&&distanceTo(target)<28){int r=random.nextInt(3);setAttack(r==0?QUACK:r==1?EGGS:CHARGE);
      if(attack()==QUACK){sound(2F,.7F);level().broadcastEntityEvent(this,(byte)61);}}}
    case QUACK->{getNavigation().stop();if(timer==28)sonicQuack();if(timer>=40)finish();}
    case EGGS->{getNavigation().stop();
     if(timer==1){setDeltaMovement(0,1.05,0);sound(2F,.9F);}
     if(timer>12&&timer<70&&timer%(rage()?3:6)==0){var ps=players(30);if(!ps.isEmpty()){var p=ps.get(random.nextInt(ps.size()));dropEgg(p);}}
     if(timer>=80)finish();}
    case CHARGE->{
     if(timer<15){getNavigation().stop();if(target!=null)getLookControl().setLookAt(target);if(timer==1)sound(2F,.55F);}
     else if(timer==15){if(target==null){finish();break;}var d=target.position().subtract(position()).multiply(1,0,1);dash=d.lengthSqr()<1e-4?Vec3.ZERO:d.normalize().scale(rage()?1.15:.9);
      setYRot((float)(Mth.atan2(dash.z,dash.x)*180/Math.PI)-90);yBodyRot=getYRot();}
     else if(timer<40){setDeltaMovement(dash.x,getDeltaMovement().y,dash.z);
      for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(.6),x->x!=this&&!(x instanceof EvilDuck)))
       if(e.hurt(damageSources().mobAttack(this),rage()?18:14))e.push(dash.x*1.8,.7,dash.z*1.8);
      if(horizontalCollision&&timer>18){level().playSound(null,blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.HOSTILE,1F,.7F);setDeltaMovement(Vec3.ZERO);finish();}}
     else finish();}
    default->finish();
   }
  }
  /** tests only */ public void force(int a){setAttack(a);if(a==QUACK){sound(2F,.7F);level().broadcastEntityEvent(this,(byte)61);}}
  void finish(){setAttack(NONE);cooldown=(rage()?50:90)+random.nextInt(40);}
  void sound(float vol,float pitch){level().playSound(null,getX(),getY(),getZ(),quack(),SoundSource.HOSTILE,vol,pitch*(rage()?.85F:1F));}
  List<Player> players(double r){return level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(r),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator());}
  void sonicQuack(){
   sound(4F,.45F);level().broadcastEntityEvent(this,(byte)61);
   if(level() instanceof ServerLevel sl){
    for(int ring=1;ring<=3;ring++)for(int i=0;i<20;i++){double an=i*Math.PI*2/20;sl.sendParticles(ring==2?ParticleTypes.SONIC_BOOM:ParticleTypes.CLOUD,getX()+Math.cos(an)*ring*3.2,getY()+1.5,getZ()+Math.sin(an)*ring*3.2,1,0,0,0,0);}}
   for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(12),x->x!=this&&!(x instanceof EvilDuck))){
    var d=e.position().subtract(position());double dist=Math.max(1,d.length());if(dist>12)continue;var n=d.multiply(1,0,1).normalize();
    e.hurt(damageSources().sonicBoom(this),(rage()?11:8)*(float)(1.2-dist/14));e.push(n.x*2.2,.75,n.z*2.2);e.hurtMarked=true;
    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1));e.addEffect(new MobEffectInstance(MobEffects.CONFUSION,80,0));}
  }
  void dropEgg(Player p){
   var egg=new EvilEgg(EGG.get(),level());
   double x=p.getX()+random.nextGaussian()*3,z=p.getZ()+random.nextGaussian()*3;
   egg.setPos(x,p.getY()+16+random.nextDouble()*4,z);egg.setDeltaMovement(random.nextGaussian()*.03,-.5,random.nextGaussian()*.03);egg.setOwner(this);
   level().addFreshEntity(egg);}
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
   for(var p:players(64))p.displayClientMessage(Component.literal("§a🏆 O Pato Gigante Malvado foi derrotado!"),true);}}
 }

 /** the boss's falling egg: a small explosion that doesn't break blocks */
 public static final class EvilEgg extends ThrowableItemProjectile {
  public EvilEgg(EntityType<? extends ThrowableItemProjectile> t,Level l){super(t,l);}
  @Override protected Item getDefaultItem(){return CURSED_EGG.get();}
  @Override protected double getDefaultGravity(){return .04;}
  @Override public void tick(){super.tick();if(level().isClientSide)level().addParticle(ParticleTypes.WITCH,getX(),getY(),getZ(),0,0,0);if(tickCount>200)discard();}
  @Override protected void onHit(HitResult r){super.onHit(r);if(!level().isClientSide){
   level().explode(getOwner(),getX(),getY(),getZ(),2.2F,Level.ExplosionInteraction.NONE);discard();}}
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
