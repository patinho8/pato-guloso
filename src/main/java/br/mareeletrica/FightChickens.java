package br.mareeletrica;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** Galinha de Briga: an angry fighting rooster. It follows its owner and, in fight mode, battles other players' roosters
 *  with pecks, flying kicks, wing storms, thunder pecks and egg bombs. The loser is knocked out (not killed) and the winner
 *  levels up. The owner can call moves with the fight whistle. */
public final class FightChickens {
 public static final Supplier<EntityType<FightChicken>> ENTITY=MareEletrica.ACTORS.register("fighting_chicken",()->
  EntityType.Builder.<FightChicken>of(FightChicken::new,MobCategory.CREATURE).sized(.7F,1.3F).clientTrackingRange(10).build(MareEletrica.ID+":fighting_chicken"));
 /** Mystery egg: hatches a rooster of a random breed. */
 public static final DeferredItem<SpawnItem> ITEM=MareEletrica.ITEMS.register("fighting_chicken",()->new SpawnItem(new Item.Properties().stacksTo(16),-1));
 /** One egg per breed (colour and type). */
 public static final java.util.List<DeferredItem<SpawnItem>> BREED_ITEMS=new java.util.ArrayList<>();
 static{for(var b:ChickenTypes.Breed.values())BREED_ITEMS.add(MareEletrica.ITEMS.register("rooster_egg_"+b.id(),()->new SpawnItem(new Item.Properties().stacksTo(16),b.ordinal())));}
 public static final DeferredItem<WhistleItem> WHISTLE=MareEletrica.ITEMS.register("fight_whistle",()->new WhistleItem(new Item.Properties().stacksTo(1)));
 private FightChickens(){}
 public static void register(IEventBus bus){bus.addListener(FightChickens::attributes);}
 static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),FightChicken.attributes().build());}

 // animation ids sent with broadcastEntityEvent
 public static final byte PECK=70,KICK=71,WINGS=72,THUNDER=73,EGG=74,FURY=75;

 public static final class FightChicken extends TamableAnimal {
  static final EntityDataAccessor<Integer> LEVEL=SynchedEntityData.defineId(FightChicken.class,EntityDataSerializers.INT);
  static final EntityDataAccessor<Integer> KO=SynchedEntityData.defineId(FightChicken.class,EntityDataSerializers.INT);
  static final EntityDataAccessor<Boolean> BRAVE=SynchedEntityData.defineId(FightChicken.class,EntityDataSerializers.BOOLEAN);
  static final EntityDataAccessor<Boolean> RAGE=SynchedEntityData.defineId(FightChicken.class,EntityDataSerializers.BOOLEAN);
  static final EntityDataAccessor<Boolean> COMBAT=SynchedEntityData.defineId(FightChicken.class,EntityDataSerializers.BOOLEAN);
  static final EntityDataAccessor<Integer> BREED=SynchedEntityData.defineId(FightChicken.class,EntityDataSerializers.INT);
  /** Ring battle health shown over the head (max 0 = not in a ring battle). */
  static final EntityDataAccessor<Integer> BHP=SynchedEntityData.defineId(FightChicken.class,EntityDataSerializers.INT);
  static final EntityDataAccessor<Integer> BMAX=SynchedEntityData.defineId(FightChicken.class,EntityDataSerializers.INT);
  int rest,xp,wins,peckCd,kickCd,wingCd,thunderCd,eggCd,kickTicks,furyTicks,combatTicks;boolean furyUsed;
  /** Players are never attacked: anything that would make the rooster go for a player (being hit, the owner's fights,
   *  the whistle) only makes it run circles around that player for 5 s, staring, without touching them. */
  public Player circling;public int circleTicks;float circleAngle;
  @Override public void setTarget(LivingEntity t){
   if(t instanceof Player p){if(!level().isClientSide&&!p.isSpectator()){circling=p;circleTicks=100;circleAngle=(float)Math.atan2(getZ()-p.getZ(),getX()-p.getX());}super.setTarget(null);return;}
   super.setTarget(t);
  }
  Vec3 eggAt;int eggTimer;
  // client animation
  public int anim,animStart;public float flap,flapO;

  public FightChicken(EntityType<? extends FightChicken> t,Level l){super(t,l);}
  static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.MOVEMENT_SPEED,.3)
   .add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.3).add(Attributes.STEP_HEIGHT,1.05);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(LEVEL,1);b.define(KO,0);b.define(BRAVE,true);b.define(RAGE,false);b.define(COMBAT,false);b.define(BREED,0);b.define(BHP,0);b.define(BMAX,0);}
  public int lvl(){return entityData.get(LEVEL);}
  public int ko(){return entityData.get(KO);}
  public boolean brave(){return entityData.get(BRAVE);}
  public boolean rage(){return entityData.get(RAGE);}
  public boolean combat(){return entityData.get(COMBAT);}
  public ChickenTypes.Breed breed(){return ChickenTypes.Breed.of(entityData.get(BREED));}
  public void setBreed(ChickenTypes.Breed b){entityData.set(BREED,b.ordinal());}
  public int battleHp(){return entityData.get(BHP);}public int battleMax(){return entityData.get(BMAX);}
  public void setBattleHp(int hp,int max){entityData.set(BHP,hp);entityData.set(BMAX,max);}
  double power(){return (1+(lvl()-1)*.12)*(furyTicks>0?1.6:1);}

  @Override protected void registerGoals(){
   goalSelector.addGoal(1,new FloatGoal(this));
   // sits only when told to (the vanilla goal also freezes it whenever the owner is offline, which would stop fights)
   goalSelector.addGoal(2,new SitWhenOrderedToGoal(this){@Override public boolean canUse(){return isOrderedToSit()&&!isInWaterOrBubble()&&onGround();}});
   goalSelector.addGoal(3,new FightGoal(this));
   goalSelector.addGoal(5,new FollowOwnerGoal(this,1.2,6F,2.5F));
   goalSelector.addGoal(7,new WaterAvoidingRandomStrollGoal(this,.8));
   goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8F));
   goalSelector.addGoal(9,new RandomLookAroundGoal(this));
   targetSelector.addGoal(1,new OwnerHurtByTargetGoal(this));
   targetSelector.addGoal(2,new OwnerHurtTargetGoal(this));
   targetSelector.addGoal(3,new HurtByTargetGoal(this));
   targetSelector.addGoal(4,new NearestAttackableTargetGoal<>(this,FightChicken.class,20,true,false,o->o instanceof FightChicken c&&rivals(this,c)));
  }
  /** Two roosters fight when both are in fight mode, awake, and have different owners. */
  static boolean rivals(FightChicken a,FightChicken b){
   return a!=b&&a.brave()&&b.brave()&&a.ko()==0&&b.ko()==0&&a.rest==0&&b.rest==0&&!a.isOrderedToSit()&&!b.isOrderedToSit()&&a.isTame()&&b.isTame()&&!java.util.Objects.equals(a.getOwnerUUID(),b.getOwnerUUID());
  }
  @Override public boolean isFood(ItemStack s){return s.is(Items.WHEAT_SEEDS)||s.is(Items.MELON_SEEDS)||s.is(Items.PUMPKIN_SEEDS)||s.is(Items.BEETROOT_SEEDS);}
  @Override public AgeableMob getBreedOffspring(ServerLevel l,AgeableMob other){return null;}
  @Override public boolean canMate(net.minecraft.world.entity.animal.Animal other){return false;}
  @Override public boolean causeFallDamage(float d,float m,DamageSource s){return false;}
  @Override public boolean removeWhenFarAway(double d){return false;}
  @Override protected SoundEvent getAmbientSound(){return SoundEvents.CHICKEN_AMBIENT;}
  @Override protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.CHICKEN_HURT;}
  @Override protected SoundEvent getDeathSound(){return SoundEvents.CHICKEN_DEATH;}
  @Override public float getVoicePitch(){return .7F;}
  @Override public boolean wantsToAttack(LivingEntity t,LivingEntity owner){
   if(t instanceof FightChicken c)return rivals(this,c);
   if(t instanceof TamableAnimal ta&&ta.isTame()&&ta.getOwner()==owner)return false;
   return !(t instanceof Player p&&owner instanceof Player o&&!o.canHarmPlayer(p));
  }

  @Override public InteractionResult mobInteract(Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);
   if(level().isClientSide)return isOwnedBy(p)||isFood(s)?InteractionResult.SUCCESS:InteractionResult.PASS;
   if(!isOwnedBy(p))return InteractionResult.PASS;
   if(isFood(s)){if(getHealth()<getMaxHealth()||ko()>0){heal(6);if(ko()>0)entityData.set(KO,Math.max(1,ko()-60));s.consume(1,p);
    ((ServerLevel)level()).sendParticles(ParticleTypes.HEART,getX(),getY()+1.2,getZ(),4,.3,.2,.3,0);playSound(SoundEvents.CHICKEN_EGG,1F,1.2F);}return InteractionResult.SUCCESS;}
   if(s.is(WHISTLE.get()))return InteractionResult.PASS;
   if(p.isShiftKeyDown()){boolean b=!brave();entityData.set(BRAVE,b);if(!b)setTarget(null);
    p.displayClientMessage(Component.literal(b?"§c⚔ Modo briga LIGADO §7— ela vai brigar com galinhas de outros jogadores":"§a☮ Modo briga desligado §7— ela só se defende"),true);return InteractionResult.SUCCESS;}
   setOrderedToSit(!isOrderedToSit());setInSittingPose(isOrderedToSit());jumping=false;getNavigation().stop();setTarget(null);
   p.displayClientMessage(Component.literal(isOrderedToSit()?"§eA galinha ficou esperando aqui":"§eA galinha está te seguindo"),true);
   return InteractionResult.SUCCESS;
  }

  @Override public void tick(){
   super.tick();
   if(level().isClientSide){
    flapO=flap;boolean air=!onGround()&&!isInWater();
    flap+=air||anim==WINGS&&tickCount-animStart<24?1.6F:.08F;
    if(rage()&&tickCount%3==0)level().addParticle(ParticleTypes.FLAME,getRandomX(.5),getY()+random.nextDouble()*1.1,getRandomZ(.5),0,.03,0);
    if(ko()>0&&tickCount%6==0){double a=tickCount*.3;level().addParticle(ParticleTypes.CRIT,getX()+Math.cos(a)*.4,getY()+1.2,getZ()+Math.sin(a)*.4,0,0,0);}
    return;
   }
   if(tickCount%40==0&&getPersistentData().getBoolean("MareBattle")&&!ChickenBattle.ACTIVE.contains(getUUID())){
    getPersistentData().remove("MareBattle");setNoAi(false);setBattleHp(0,0);
    if(getPersistentData().getBoolean("MareCpu")){discard();return;}
   }
   RareEgg.maybeLay(this);
   if(isNoAi())return;
   // slow fall: it flaps its wings
   var v=getDeltaMovement();if(!onGround()&&v.y<-.12&&kickTicks==0)setDeltaMovement(v.x,v.y*.65,v.z);
   if(peckCd>0)peckCd--;if(kickCd>0)kickCd--;if(wingCd>0)wingCd--;if(thunderCd>0)thunderCd--;if(eggCd>0)eggCd--;
   if(furyTicks>0&&--furyTicks==0)entityData.set(RAGE,false);
   int k=ko();
   if(rest>0)rest--;
   if(k>0){entityData.set(KO,k-1);setTarget(null);getNavigation().stop();if(k==1){heal(getMaxHealth()*.3F);rest=600;playSound(SoundEvents.CHICKEN_AMBIENT,1F,.6F);}return;}
   if(circleTicks>0){   // circles the player: no peck, no kick, nothing else
    if(circling==null||!circling.isAlive()||circling.level()!=level()||isOrderedToSit()||getTarget()!=null||distanceToSqr(circling)>24*24){circleTicks=0;circling=null;}
    else{circleTicks--;
     // keep running toward a point a bit further along the ring around the player
     double cur=Math.atan2(getZ()-circling.getZ(),getX()-circling.getX());circleAngle=(float)(cur+.7);
     if(tickCount%4==0)getNavigation().moveTo(circling.getX()+Math.cos(circleAngle)*2.3,circling.getY(),circling.getZ()+Math.sin(circleAngle)*2.3,1.4);
     getLookControl().setLookAt(circling,30F,30F);
     if(circleTicks==0){circling=null;getNavigation().stop();}}
   }
   if(getTarget() instanceof FightChicken f&&(f.ko()>0||!rivals(this,f)&&f.getTarget()!=this))setTarget(null);
   var t=getTarget();
   if(t!=null&&t.isAlive())combatTicks=60;else if(combatTicks>0)combatTicks--;
   entityData.set(COMBAT,combatTicks>0);
   if(combatTicks==0){furyUsed=false;}
   if(kickTicks>0){kickTicks--;
    for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(.45),x->x!=this&&enemy(x))){
     hit(e,7F,1.0,ParticleTypes.EXPLOSION,SoundEvents.PLAYER_ATTACK_KNOCKBACK);kickTicks=0;break;}
   }
   if(eggTimer>0&&--eggTimer==0&&eggAt!=null){
    level().explode(this,null,new net.minecraft.world.level.ExplosionDamageCalculator(){   // the egg bomb spares players
     @Override public boolean shouldDamageEntity(net.minecraft.world.level.Explosion x,net.minecraft.world.entity.Entity en){return !(en instanceof Player)&&super.shouldDamageEntity(x,en);}
     @Override public float getKnockbackMultiplier(net.minecraft.world.entity.Entity en){return en instanceof Player?0F:super.getKnockbackMultiplier(en);}},
    eggAt.x,eggAt.y,eggAt.z,1.6F,false,Level.ExplosionInteraction.NONE);
    ((ServerLevel)level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.EGG)),eggAt.x,eggAt.y+.3,eggAt.z,20,.4,.3,.4,.15);eggAt=null;}
   // fury: once per fight, when almost beaten
   if(t!=null&&!furyUsed&&getHealth()<getMaxHealth()*.3F){furyUsed=true;furyTicks=140;entityData.set(RAGE,true);heal(5);
    playSound(SoundEvents.RAVAGER_ROAR,1F,1.9F);level().broadcastEntityEvent(this,FURY);
    ((ServerLevel)level()).sendParticles(ParticleTypes.ANGRY_VILLAGER,getX(),getY()+1,getZ(),8,.5,.4,.5,0);
    if(getOwner() instanceof Player o)o.displayClientMessage(Component.literal("§4🔥 FÚRIA! §cSua galinha ficou furiosa!"),true);}
  }
  boolean enemy(LivingEntity e){
   if(e==getTarget())return true;
   if(e instanceof FightChicken c)return rivals(this,c);
   return e instanceof Enemy;
  }
  /** Shared hit with damage, knockback, particles and sound. */
  void hit(LivingEntity e,float dmg,double push,ParticleOptions fx,SoundEvent sound){
   if(e instanceof Player)return;   // roosters never hurt players
   var dir=e.position().subtract(position()).multiply(1,0,1);if(dir.lengthSqr()<1e-4)dir=getLookAngle();dir=dir.normalize();
   e.hurt(damageSources().mobAttack(this),(float)(dmg*power()));
   e.push(dir.x*push,.25+push*.18,dir.z*push);e.hurtMarked=true;
   ((ServerLevel)level()).sendParticles(fx,e.getX(),e.getY()+e.getBbHeight()*.6,e.getZ(),fx==ParticleTypes.EXPLOSION?1:6,.2,.2,.2,.1);
   ((ServerLevel)level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.FEATHER)),e.getX(),e.getY()+.6,e.getZ(),5,.25,.25,.25,.08);
   playSound(sound,1F,1.1F+random.nextFloat()*.3F);
  }
  // ---- moves ----
  void peck(LivingEntity t){peckCd=12;swing(InteractionHand.MAIN_HAND);level().broadcastEntityEvent(this,PECK);hit(t,4F,.35,ParticleTypes.CRIT,SoundEvents.PLAYER_ATTACK_STRONG);}
  boolean kick(LivingEntity t,boolean forced){
   if(!onGround()||kickCd>0&&!forced)return false;
   var d=t.position().subtract(position());double h=Math.sqrt(d.x*d.x+d.z*d.z);if(h<1.5||h>12)return false;
   double sp=Math.min(1.25,.45+h*.11);setDeltaMovement(d.x/h*sp,.5+h*.02,d.z/h*sp);hasImpulse=true;
   kickTicks=24;kickCd=90;level().broadcastEntityEvent(this,KICK);playSound(SoundEvents.ENDER_DRAGON_FLAP,.8F,1.7F);
   ((ServerLevel)level()).sendParticles(ParticleTypes.CLOUD,getX(),getY(),getZ(),8,.3,.05,.3,.02);
   if(getOwner() instanceof Player o&&o.distanceToSqr(this)<900)o.displayClientMessage(Component.literal("§6🦶 VOADORA!"),true);
   return true;
  }
  boolean wings(boolean forced){
   if(wingCd>0&&!forced)return false;wingCd=150;level().broadcastEntityEvent(this,WINGS);
   playSound(SoundEvents.ENDER_DRAGON_FLAP,1.2F,1.5F);playSound(SoundEvents.CHICKEN_HURT,1F,.5F);
   var sl=(ServerLevel)level();
   for(int i=0;i<24;i++){double a=i*Math.PI/12;sl.sendParticles(ParticleTypes.CLOUD,getX()+Math.cos(a)*1.5,getY()+.5,getZ()+Math.sin(a)*1.5,1,0,.1,0,.02);}
   sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.FEATHER)),getX(),getY()+.7,getZ(),25,1,.4,1,.12);
   sl.sendParticles(ParticleTypes.SWEEP_ATTACK,getX(),getY()+.6,getZ(),4,1,.2,1,0);
   for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(3.5,1.5,3.5),x->x!=this&&enemy(x)))hit(e,4.5F,.85,ParticleTypes.SWEEP_ATTACK,SoundEvents.PLAYER_ATTACK_SWEEP);
   if(getOwner() instanceof Player o&&o.distanceToSqr(this)<900)o.displayClientMessage(Component.literal("§b🌪 FURACÃO DE ASAS!"),true);
   return true;
  }
  boolean thunder(LivingEntity t){
   if(lvl()<5||thunderCd>0)return false;thunderCd=400;level().broadcastEntityEvent(this,THUNDER);
   var bolt=EntityType.LIGHTNING_BOLT.create(level());if(bolt!=null){bolt.moveTo(t.position());bolt.setVisualOnly(true);level().addFreshEntity(bolt);}
   hit(t,9F,.8,ParticleTypes.ELECTRIC_SPARK,SoundEvents.TRIDENT_THUNDER.value());
   if(getOwner() instanceof Player o&&o.distanceToSqr(this)<900)o.displayClientMessage(Component.literal("§e⚡ BICADA DO TROVÃO!"),true);
   return true;
  }
  boolean egg(LivingEntity t){
   if(lvl()<8||eggCd>0||eggTimer>0)return false;eggCd=300;eggAt=t.position();eggTimer=12;level().broadcastEntityEvent(this,EGG);
   var sl=(ServerLevel)level();var from=position().add(0,.8,0);var to=eggAt.add(0,.4,0);
   for(int i=0;i<=12;i++){double f=i/12.0;var q=from.lerp(to,f).add(0,Math.sin(f*Math.PI)*1.5,0);sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.EGG)),q.x,q.y,q.z,1,0,0,0,0);}
   playSound(SoundEvents.EGG_THROW,1F,.6F);
   if(getOwner() instanceof Player o&&o.distanceToSqr(this)<900)o.displayClientMessage(Component.literal("§f🥚 OVO-BOMBA!"),true);
   return true;
  }

  /** Knocked out instead of killed when beaten by another rooster. */
  @Override public boolean hurt(DamageSource s,float amount){
   if(level().isClientSide)return super.hurt(s,amount);
   if(ko()>0&&s.getEntity() instanceof FightChicken)return false;
   if(s.getEntity() instanceof FightChicken winner&&getHealth()-amount<=1F){
    super.hurt(s,Math.max(0,getHealth()-1F));setHealth(1F);
    entityData.set(KO,200);setTarget(null);getNavigation().stop();furyTicks=0;entityData.set(RAGE,false);
    winner.won(this);return true;
   }
   return super.hurt(s,amount);
  }
  /** Knocked out by the ring battle. */
  public void knockOut(){entityData.set(KO,200);setTarget(null);furyTicks=0;entityData.set(RAGE,false);}
  public void won(FightChicken loser){
   setTarget(null);wins++;xp++;
   var sl=(ServerLevel)level();
   sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,getX(),getY()+1,getZ(),40,.4,.6,.4,.4);
   playSound(SoundEvents.PLAYER_LEVELUP,1F,1.4F);playSound(SoundEvents.CHICKEN_AMBIENT,1.5F,.5F);
   String a=getOwner()!=null?getOwner().getName().getString():"?",b=loser.getOwner()!=null?loser.getOwner().getName().getString():"?";
   for(var p:sl.getEntitiesOfClass(Player.class,new AABB(blockPosition()).inflate(40)))p.sendSystemMessage(Component.literal("§6🏆 A galinha de §e"+a+"§6 nocauteou a galinha de §e"+b+"§6!"));
   int need=lvl()*2;
   if(lvl()<10&&xp>=need){xp-=need;setLevel(lvl()+1);sl.sendParticles(ParticleTypes.FIREWORK,getX(),getY()+1,getZ(),30,.3,.5,.3,.2);
    if(getOwner() instanceof Player o)o.sendSystemMessage(Component.literal("§a⬆ Sua galinha subiu para o §lnível "+lvl()+"§a!"+(lvl()==5?" §eAprendeu: ⚡ Bicada do Trovão!":lvl()==8?" §fAprendeu: 🥚 Ovo-Bomba!":"")));}
  }
  void setLevel(int l){entityData.set(LEVEL,l);var hp=getAttribute(Attributes.MAX_HEALTH);if(hp!=null){hp.setBaseValue(30+(l-1)*4);heal(4);}
   var sp=getAttribute(Attributes.MOVEMENT_SPEED);if(sp!=null)sp.setBaseValue(.3+(l-1)*.006);}

  @Override public void handleEntityEvent(byte id){
   if(id>=PECK&&id<=FURY){anim=id;animStart=tickCount;return;}
   super.handleEntityEvent(id);
  }
  @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Lvl",lvl());t.putInt("Xp",xp);t.putInt("Wins",wins);t.putBoolean("Brave",brave());t.putInt("KO",ko());t.putInt("Rest",rest);t.putInt("Breed",breed().ordinal());}
  @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);if(t.contains("Lvl"))setLevel(Math.max(1,t.getInt("Lvl")));xp=t.getInt("Xp");wins=t.getInt("Wins");if(t.contains("Brave"))entityData.set(BRAVE,t.getBoolean("Brave"));entityData.set(KO,t.getInt("KO"));rest=t.getInt("Rest");entityData.set(BREED,t.getInt("Breed"));}
  public int wins(){return wins;}public int xp(){return xp;}
 }

 /** Chases the target and picks a move: thunder peck, egg bomb, flying kick, wing storm or a plain peck. */
 static final class FightGoal extends Goal {
  final FightChicken c;int repath;
  FightGoal(FightChicken c){this.c=c;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
  @Override public boolean canUse(){var t=c.getTarget();return t!=null&&t.isAlive()&&c.ko()==0&&!c.isOrderedToSit()&&!(t instanceof FightChicken f&&f.ko()>0);}
  @Override public boolean canContinueToUse(){return canUse()&&c.distanceToSqr(c.getTarget())<32*32&&!(c.getTarget() instanceof FightChicken f&&f.ko()>0);}
  @Override public void stop(){c.getNavigation().stop();}
  @Override public boolean requiresUpdateEveryTick(){return true;}
  @Override public void tick(){
   var t=c.getTarget();if(t==null)return;
   c.getLookControl().setLookAt(t,30F,30F);
   double d=c.distanceTo(t);
   if(c.kickTicks>0)return;
   if(--repath<=0){repath=6;c.getNavigation().moveTo(t,c.furyTicks>0?1.6:1.35);}
   var r=c.getRandom();
   if(d<7&&r.nextInt(30)==0&&c.thunder(t))return;
   if(d>3&&d<10&&r.nextInt(25)==0&&c.egg(t))return;
   if(d>3.2&&d<9&&r.nextInt(12)==0&&c.kick(t,false))return;
   if(d<2.6&&r.nextInt(18)==0&&c.wings(false))return;
   if(d<1.6+t.getBbWidth()*.5&&c.peckCd==0&&c.hasLineOfSight(t))c.peck(t);
  }
 }

 /** Hatches a fighting rooster that belongs to you. */
 public static final class SpawnItem extends Item {
  final int breed;
  public SpawnItem(Properties p,int breed){super(p);this.breed=breed;}
  @Override public Component getName(ItemStack s){return breed<0?super.getName(s):Component.literal("Ovo de "+ChickenTypes.Breed.of(breed).label);}
  @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,java.util.List<Component> t,net.minecraft.world.item.TooltipFlag f){
   if(breed<0){t.add(Component.literal("§7Choca uma galinha de raça surpresa"));return;}
   var b=ChickenTypes.Breed.of(breed);t.add(Component.literal("§7Tipo: ").append(Component.literal(b.type.label).withColor(b.type.color)));
   t.add(Component.literal("§7Vida "+b.hp+" · Ataque "+b.atk+" · Defesa "+b.def+" · Velocidade "+b.spd));
  }
  @Override public InteractionResult useOn(UseOnContext c){
   if(!(c.getLevel() instanceof ServerLevel l))return InteractionResult.SUCCESS;
   if(breed==ChickenTypes.Breed.DOURADO.ordinal()){   // the rare egg is hatched, not spawned
    var at=c.getClickLocation();var egg=RareEgg.TYPE.get().create(l);egg.moveTo(at.x,at.y,at.z,0,0);if(c.getPlayer()!=null)egg.owner=c.getPlayer().getUUID();l.addFreshEntity(egg);
    l.playSound(null,egg.blockPosition(),SoundEvents.TURTLE_EGG_CRACK,SoundSource.NEUTRAL,1F,1.4F);
    if(c.getPlayer()!=null){if(!c.getPlayer().isCreative())c.getItemInHand().shrink(1);c.getPlayer().displayClientMessage(Component.literal("§6O ovo raro vai chocar em 15 segundos..."),true);}
    return InteractionResult.CONSUME;}
   var p=c.getClickedPos().relative(c.getClickedFace());var e=ENTITY.get().create(l);if(e==null)return InteractionResult.FAIL;
   e.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,c.getRotation()+180,0);
   var br=breed>=0?ChickenTypes.Breed.of(breed):ChickenTypes.Breed.common(l.random);e.setBreed(br);
   if(c.getPlayer()!=null){e.tame(c.getPlayer());e.setYHeadRot(e.getYRot());}
   l.addFreshEntity(e);l.sendParticles(ParticleTypes.POOF,e.getX(),e.getY()+.5,e.getZ(),12,.3,.3,.3,.02);
   e.playSound(SoundEvents.CHICKEN_EGG,1F,.6F);
   if(c.getPlayer()!=null&&!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("§6🐔 Nasceu um ").append(Component.literal(br.label).withColor(br.type.color)).append(Component.literal(" §7(tipo "+br.type.label+")! Agachar + clique: modo briga · clique: seguir/esperar · sementes: cura · leve ao Ringue para batalhar")),false);
   return InteractionResult.CONSUME;
  }
 }

 /** Fight whistle: click a mob or another rooster to send your roosters at it; click the air for a flying kick,
  *  crouch + click the air for the wing storm. Click your own rooster to see its level. */
 public static final class WhistleItem extends Item {
  public WhistleItem(Properties p){super(p);}
  static List<FightChicken> mine(Player p){return p.level().getEntitiesOfClass(FightChicken.class,p.getBoundingBox().inflate(24),c->c.isOwnedBy(p)&&c.ko()==0);}
  @Override public InteractionResult interactLivingEntity(ItemStack s,Player p,LivingEntity e,InteractionHand h){
   if(p.level().isClientSide)return InteractionResult.SUCCESS;
   if(e instanceof FightChicken c&&c.isOwnedBy(p)){
    var mv=new StringBuilder();for(var m:c.breed().battleMoves(c.lvl()))mv.append(mv.length()>0?", ":"").append(m.label);
    p.sendSystemMessage(Component.literal("§6🐔 ").append(Component.literal(c.breed().label).withColor(c.breed().type.color)).append(Component.literal(" §7(tipo "+c.breed().type.label+") · golpes: §f"+mv)));
    p.displayClientMessage(Component.literal("§6🐔 Nível "+c.lvl()+" §7| §c❤ "+(int)c.getHealth()+"/"+(int)c.getMaxHealth()+" §7| §e🏆 "+c.wins()+" vitórias §7| XP "+c.xp()+"/"+c.lvl()*2+(c.brave()?" §c⚔":" §a☮")),true);
    return InteractionResult.SUCCESS;}
   int n=0;for(var c:mine(p)){if(e instanceof FightChicken f&&!FightChicken.rivals(c,f)&&!(c.brave()&&f.brave()))continue;c.setOrderedToSit(false);c.setInSittingPose(false);c.setTarget(e);n++;}
   p.level().playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_FLUTE.value(),SoundSource.PLAYERS,1F,1.8F);
   p.displayClientMessage(Component.literal(n>0?"§c⚔ Pega! §7("+n+" galinha"+(n>1?"s":"")+" atacando)":"§7Nenhuma galinha sua por perto pode atacar isso"),true);
   return InteractionResult.SUCCESS;
  }
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);if(l.isClientSide)return InteractionResultHolder.success(s);
   boolean any=false;
   for(var c:mine(p)){var t=c.getTarget();if(t==null||!t.isAlive())continue;
    any|=p.isShiftKeyDown()?c.wings(true):c.kick(t,true);}
   l.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_FLUTE.value(),SoundSource.PLAYERS,1F,p.isShiftKeyDown()?1.2F:2F);
   if(!any)p.displayClientMessage(Component.literal("§7Suas galinhas precisam estar brigando (clique no inimigo com o apito)"),true);
   p.getCooldowns().addCooldown(this,20);
   return InteractionResultHolder.success(s);
  }
 }
}
