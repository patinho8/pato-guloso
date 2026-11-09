package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.68.0 — o Pato Guloso: a pet duck that eats (and gets chubbier), grows from duckling to adult to Duck King,
 *  wears hats, guards you (with a terrifying QUACK — or a tiny gun when it wears the army helmet), fishes, sniffs out ores,
 *  sleeps in a nest and lays golden eggs, rides on your head, skates and surfs with you; plus duck races at the casino. */
public final class Pato {
 private Pato(){}
 public static final String[] COLORS={"branco","marrom","preto","dourado","arco-íris"};
 public static final String[] HATS={"","boné","cartola","coroa","óculos escuros","capacete militar","mini-luneta"};
 public static final String[] MODES={"Seguir","Sentar","Guarda-costas","Pescar","Farejar","Coletar"};
 public static final int FOLLOW=0,SIT=1,GUARD=2,FISH=3,SNIFF=4,COLLECT=5;
 /** 0.76.0: total XP needed for each level (1..10) */
 public static final int[] LEVEL_XP={0,30,80,150,240,350,480,630,800,1000};
 public static final String[] PERKS={"","","§b🔔 Alarme§7: avisa quando um monstro chega perto de você","§e🧺 Modo Coletar§7: busca itens no chão e traz pra você","","§d✨ Marca os monstros§7 (eles brilham através das paredes)","","§a❤ Cura§7: quando você está quase morrendo, ele te dá regeneração","","","§6⭐ PATO LENDÁRIO§7: brilho dourado e mais ovos dourados"};
 public static final Supplier<EntityType<Duck>> ENTITY=MareEletrica.ACTORS.register("duck",()->
  EntityType.Builder.<Duck>of(Duck::new,MobCategory.CREATURE).sized(.5F,.62F).eyeHeight(.52F).clientTrackingRange(10).build(MareEletrica.ID+":duck"));
 public static final Supplier<SoundEvent> QUACK=MareEletrica.SOUNDS.register("duck_quack",()->SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"duck_quack")));
 public static final DeferredItem<DeferredSpawnEggItem> EGG=MareEletrica.ITEMS.register("duck_spawn_egg",()->new DeferredSpawnEggItem(ENTITY,0xF4F1E6,0xF0A030,new Item.Properties()));
 public static final DeferredItem<Item> GOLDEN_EGG=MareEletrica.ITEMS.register("golden_duck_egg",()->new GoldenEgg(new Item.Properties().stacksTo(16)));
 public static final DeferredItem<Item> WHISTLE=MareEletrica.ITEMS.register("duck_whistle",()->new Whistle(new Item.Properties().stacksTo(1)));
 public static final List<DeferredItem<Item>> HAT_ITEMS=new ArrayList<>();
 static{for(int i=1;i<HATS.length;i++){int h=i;HAT_ITEMS.add(MareEletrica.ITEMS.register("duck_hat_"+new String[]{"","cap","tophat","crown","sunglasses","helmet","scope"}[i],()->new HatItem(h,new Item.Properties().stacksTo(1))));}}
 public static final DeferredBlock<NestBlock> NEST=MareEletrica.BLOCKS.register("duck_nest",()->new NestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(.5F).sound(SoundType.GRASS).noOcclusion()));
 public static final DeferredItem<BlockItem> NEST_ITEM=MareEletrica.ITEMS.register("duck_nest",()->new BlockItem(NEST.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7À noite seus patos dormem aqui. Pato feliz e alimentado bota §6ovo dourado§7."));}});
 public static final DeferredBlock<RaceBlock> RACE=MareEletrica.BLOCKS.register("duck_race",()->new RaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2F).sound(SoundType.WOOD)));
 public static final DeferredItem<BlockItem> RACE_ITEM=MareEletrica.ITEMS.register("duck_race",()->new BlockItem(RACE.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Os patos correm 14 blocos para a frente (deixe o chão livre e plano)."));
   t.add(Component.literal("§7Mão vazia: escolhe a raia • com §efichas§7 na mão: aposta (paga 3×)"));}});
 public static final Supplier<BlockEntityType<RaceEntity>> RACE_ENTITY=MareEletrica.ENTITIES.register("duck_race",()->BlockEntityType.Builder.of(RaceEntity::new,RACE.get()).build(null));

 public static void register(IEventBus bus){bus.addListener(Pato::attributes);bus.addListener(Pato::placements);EFFECTS.register(bus);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(Pato::duckPlayer);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(Duck::shoulderOff);}
 // ======================================================================= 0.69.0: Poção do Pato — turns the drinker into a duck
 public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.effect.MobEffect> EFFECTS=net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT,MareEletrica.ID);
 public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.effect.MobEffect,DuckEffect> DUCK_EFFECT=EFFECTS.register("duck",DuckEffect::new);
 public static final DeferredItem<Item> POTION=MareEletrica.ITEMS.register("duck_potion",()->new DuckPotion(new Item.Properties().stacksTo(16)));
 public static final class DuckEffect extends net.minecraft.world.effect.MobEffect {
  public DuckEffect(){super(net.minecraft.world.effect.MobEffectCategory.NEUTRAL,0xF4C430);
   addAttributeModifier(Attributes.SCALE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"duck_size"),-.65,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);}
 }
 public static boolean isDuck(Entity e){return e instanceof LivingEntity l&&l.hasEffect(DUCK_EFFECT);}
 public static final class DuckPotion extends Item {
  public DuckPotion(Properties p){super(p);}
  @Override public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack s){return net.minecraft.world.item.UseAnim.DRINK;}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 32;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){return net.minecraft.world.item.ItemUtils.startUsingInstantly(l,p,h);}
  @Override public ItemStack finishUsingItem(ItemStack s,Level l,LivingEntity e){
   if(!l.isClientSide){e.addEffect(new MobEffectInstance(DUCK_EFFECT,3600,0,false,false,true));quack(e,1F,1.2F);if(e instanceof Player p)msg(p,"§e🦆 QUACK! Você virou um pato por 3 minutos! §7Shift: quack • pulo no ar: bate as asas • leite: volta ao normal");
    if(l instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.CLOUD,e.getX(),e.getY()+.5,e.getZ(),25,.3,.4,.3,.05);}
   if(e instanceof Player p&&!p.getAbilities().instabuild){s.shrink(1);if(s.isEmpty())return new ItemStack(Items.GLASS_BOTTLE);p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));}
   return s;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Vira pato por 3 minutos: pequeno, boia, plana, bate as asas e faz QUACK (Shift)."));t.add(Component.literal("§8Leite desfaz."));}
 }
 static final Map<UUID,Boolean> SNEAK=new HashMap<>();
 /** both sides: ducks float, glide, never take fall damage; on the server, pressing Shift quacks */
 static void duckPlayer(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){
  var p=e.getEntity();if(!p.hasEffect(DUCK_EFFECT)){if(!p.level().isClientSide)SNEAK.remove(p.getUUID());return;}
  var v=p.getDeltaMovement();p.fallDistance=0;
  if(p.isInWater()){if(!p.isShiftKeyDown()&&p.getFluidHeight(net.minecraft.tags.FluidTags.WATER)>.12)p.setDeltaMovement(v.x*1.04,Math.max(v.y,.035),v.z*1.04);}
  else if(!p.onGround()&&v.y<-.16&&!p.getAbilities().flying)p.setDeltaMovement(v.x,-.16,v.z);
  if(!p.level().isClientSide){boolean sh=p.isShiftKeyDown();Boolean was=SNEAK.put(p.getUUID(),sh);
   if(sh&&(was==null||!was)){quack(p,1F,1.05F+p.getRandom().nextFloat()*.2F);if(p.level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.NOTE,p.getX(),p.getY()+.8,p.getZ(),1,0,0,0,0);}}
 }
 static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),Duck.attributes().build());}
 static void placements(RegisterSpawnPlacementsEvent e){
  e.register(ENTITY.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,l,r,p,rnd)->l.getRawBrightness(p,0)>8&&l.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.ANIMALS_SPAWNABLE_ON),RegisterSpawnPlacementsEvent.Operation.REPLACE);}
 public static void creative(java.util.function.Consumer<ItemStack> out){
  out.accept(new ItemStack(EGG.get()));out.accept(new ItemStack(WHISTLE.get()));out.accept(new ItemStack(NEST_ITEM.get()));out.accept(new ItemStack(GOLDEN_EGG.get()));out.accept(new ItemStack(POTION.get()));
  for(var h:HAT_ITEMS)out.accept(new ItemStack(h.get()));}
 static void quack(Entity e,float vol,float pitch){e.level().playSound(null,e.getX(),e.getY(),e.getZ(),QUACK.get(),SoundSource.NEUTRAL,vol,pitch);}
 static int food(ItemStack s){return s.is(Items.BREAD)?30:s.is(Items.COD)||s.is(Items.SALMON)||s.is(Items.COOKED_COD)||s.is(Items.COOKED_SALMON)?25:s.is(Items.WHEAT_SEEDS)||s.is(Items.MELON_SEEDS)||s.is(Items.PUMPKIN_SEEDS)||s.is(Items.BEETROOT_SEEDS)?10:s.is(Items.WHEAT)?14:0;}
 static void msg(Player p,String t){if(p!=null)p.displayClientMessage(Component.literal(t),true);}

 // ======================================================================= the duck
 public static final class Duck extends TamableAnimal {
  static final EntityDataAccessor<Integer> COLOR=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT),HAT=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT),
   FED=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT),MODE=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT),
   FLAGS=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT),HUNGER=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT),
   HAPPY=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT),LANE=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT),
   XP=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.INT);
  static final EntityDataAccessor<Boolean> GLIDE=SynchedEntityData.defineId(Duck.class,EntityDataSerializers.BOOLEAN);
  public static final int SLEEP=1,DIVE=2,KING=4,BOARD=8,RACER=16,BATH=32;
  /** client animation clocks */
  public int quackAt=-100,peckAt=-100;public float flap,flapO;
  /** used when a duck is drawn for a player who drank the Duck Potion */
  public float fakeWalkPos=-1,fakeWalkSpd;public boolean fakeSwim;
  int fishTimer=400,sniffTimer=60,eggTimer=2400,shotCd,diveTicks,nestCheck,boardCd;
  // 0.76.0 companion: bath, alarm, heal, collect
  BlockPos bathPos;public int bathTicks,bathCheck=200,alarmCd,healCd,petCd,companionTicks;boolean bathEntered;float bathPhase;
  /** 0.76.0: shared by every duck, so one monster is announced once even with several ducks around */
  static final java.util.Map<java.util.UUID,Long> WARNED=new java.util.HashMap<>();ItemEntity collectTarget;
  public Duck(EntityType<? extends Duck> t,Level l){super(t,l);}
  public static AttributeSupplier.Builder attributes(){return Animal.createMobAttributes().add(Attributes.MAX_HEALTH,12).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.FOLLOW_RANGE,20);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(COLOR,0);b.define(HAT,0);b.define(FED,0);b.define(MODE,0);b.define(FLAGS,0);b.define(HUNGER,70);b.define(HAPPY,60);b.define(LANE,0);b.define(XP,0);b.define(GLIDE,false);}
  public int color(){return entityData.get(COLOR);}public int hat(){return entityData.get(HAT);}public int fed(){return entityData.get(FED);}public int mode(){return entityData.get(MODE);}
  public int xp(){return entityData.get(XP);}
  public int duckLevel(){int x=xp(),l=1;for(int i=1;i<LEVEL_XP.length;i++)if(x>=LEVEL_XP[i])l=i+1;return l;}
  /** XP towards the next level, as text */
  String xpText(){int l=duckLevel();return l>=10?"§6MÁX":xp()+"/"+LEVEL_XP[l];}
  public void addXp(int n){if(!isTame()||racer()||n<=0)return;int before=duckLevel();entityData.set(XP,Math.min(xp()+n,LEVEL_XP[9]+0));int now=duckLevel();
   if(now>before){resize();heal(getMaxHealth());var o=getOwner();level().playSound(null,blockPosition(),SoundEvents.PLAYER_LEVELUP,SoundSource.NEUTRAL,.9F,1.4F);
    if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,getX(),getY()+.5,getZ(),25,.3,.4,.3,.25);
    if(o instanceof Player p){p.sendSystemMessage(Component.literal("§6🦆 "+getName().getString()+" subiu para o §lnível "+now+"§6! §7(mais vida, força e velocidade)"));
     for(int k=before+1;k<=now&&k<PERKS.length;k++)if(!PERKS[k].isEmpty())p.sendSystemMessage(Component.literal("§7   Novo: "+PERKS[k]));}}}
  public int hunger(){return entityData.get(HUNGER);}public int happy(){return entityData.get(HAPPY);}public int lane(){return entityData.get(LANE);}
  public boolean flag(int f){return (entityData.get(FLAGS)&f)!=0;}
  void flag(int f,boolean on){int v=entityData.get(FLAGS);entityData.set(FLAGS,on?v|f:v&~f);}
  public boolean king(){return flag(KING);}
  void setHunger(int v){entityData.set(HUNGER,Mth.clamp(v,0,100));}void setHappy(int v){entityData.set(HAPPY,Mth.clamp(v,0,100));}
  public void setMode(int m){entityData.set(MODE,m);setOrderedToSit(m==SIT);setTarget(null);getNavigation().stop();}
  /** chubby: every meal makes it a little bigger; the King is bigger still */
  void resize(){int lv=duckLevel()-1;var a=getAttribute(Attributes.SCALE);if(a!=null)a.setBaseValue((1+Math.min(fed(),60)*.008+lv*.01)*(king()?1.15:1));
   var h=getAttribute(Attributes.MAX_HEALTH);if(h!=null)h.setBaseValue((king()?24:12)+lv*2);var d=getAttribute(Attributes.ATTACK_DAMAGE);if(d!=null)d.setBaseValue((king()?6:3)+lv*.5);
   var sp=getAttribute(Attributes.MOVEMENT_SPEED);if(sp!=null)sp.setBaseValue(.27+lv*.006);}
  public boolean racer(){return flag(RACER);}
  /** set appearance directly (creative/testing) */
  public void setLook(int color,int hat,int fed){entityData.set(COLOR,color);entityData.set(HAT,hat);entityData.set(FED,fed);flag(KING,fed>=40);resize();}
  public void setSleeping(boolean b){flag(SLEEP,b);}
  void racer(int i){setNoAi(true);setInvulnerable(true);flag(RACER,true);entityData.set(COLOR,i);entityData.set(LANE,i+1);entityData.set(HAT,new int[]{1,2,4,5}[i]);}
  // ---------------- goals
  @Override protected void registerGoals(){
   goalSelector.addGoal(0,new FloatGoal(this){@Override public boolean canUse(){return !flag(DIVE)&&super.canUse();}});
   goalSelector.addGoal(1,new SitWhenOrderedToGoal(this));
   goalSelector.addGoal(2,new PanicGoal(this,1.6){@Override public boolean canUse(){return !isTame()&&super.canUse();}});
   goalSelector.addGoal(3,new MeleeAttackGoal(this,1.4,true));
   goalSelector.addGoal(4,new BreedGoal(this,1.0));
   goalSelector.addGoal(5,new TemptGoal(this,1.2,Ingredient.of(Items.BREAD,Items.WHEAT_SEEDS,Items.WHEAT),false));
   goalSelector.addGoal(6,new FollowOwnerGoal(this,1.3,5F,2F){@Override public boolean canUse(){return (mode()==FOLLOW||mode()==GUARD||collectIdle()||ownerFar())&&!flag(SLEEP)&&bathTicks<=0&&super.canUse();}
    @Override public boolean canContinueToUse(){return (mode()==FOLLOW||mode()==GUARD||collectIdle()||ownerFar())&&bathTicks<=0&&super.canContinueToUse();}});
   goalSelector.addGoal(7,new FollowParentGoal(this,1.1));
   goalSelector.addGoal(8,new WaterAvoidingRandomStrollGoal(this,1.0){@Override public boolean canUse(){return !flag(SLEEP)&&mode()!=SNIFF&&mode()!=COLLECT&&bathTicks<=0&&super.canUse();}});
   goalSelector.addGoal(9,new LookAtPlayerGoal(this,Player.class,6F));
   goalSelector.addGoal(10,new RandomLookAroundGoal(this));
   targetSelector.addGoal(1,new OwnerHurtByTargetGoal(this){@Override public boolean canUse(){return mode()==GUARD&&super.canUse();}});
   targetSelector.addGoal(2,new OwnerHurtTargetGoal(this){@Override public boolean canUse(){return mode()==GUARD&&super.canUse();}});
   targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,Mob.class,10,true,false,t->t instanceof Enemy&&mode()==GUARD&&isTame()&&getOwner()!=null&&t.distanceTo(getOwner())<14));
  }
  boolean collectIdle(){return mode()==COLLECT&&(duckLevel()<3||collectTarget==null&&getMainHandItem().isEmpty());}
  boolean ownerFar(){var o=getOwner();return o!=null&&distanceToSqr(o)>22*22;}
  @Override public boolean isFood(ItemStack s){return food(s)>0;}
  @Override public AgeableMob getBreedOffspring(ServerLevel l,AgeableMob other){var d=ENTITY.get().create(l);if(d!=null){d.entityData.set(COLOR,random.nextBoolean()?color():other instanceof Duck o?o.color():color());
   if(getOwnerUUID()!=null){d.setOwnerUUID(getOwnerUUID());d.setTame(true,true);}}return d;}
  @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor l,DifficultyInstance d,MobSpawnType why,SpawnGroupData data){
   int r=random.nextInt(100);entityData.set(COLOR,r<40?0:r<70?1:r<85?2:r<96?3:4);return super.finalizeSpawn(l,d,why,data);}
  // ---------------- sounds
  @Override protected SoundEvent getAmbientSound(){return QUACK.get();}
  @Override protected SoundEvent getHurtSound(DamageSource s){return QUACK.get();}
  @Override protected SoundEvent getDeathSound(){return QUACK.get();}
  @Override public float getVoicePitch(){return (isBaby()?1.5F:king()?.8F:1F)+(random.nextFloat()-.5F)*.15F;}
  @Override public int getAmbientSoundInterval(){return 200;}
  @Override public void playAmbientSound(){if(racer()||flag(SLEEP))return;super.playAmbientSound();level().broadcastEntityEvent(this,(byte)70);}
  @Override public void handleEntityEvent(byte b){if(b==70)quackAt=tickCount;else if(b==71)peckAt=tickCount;else super.handleEntityEvent(b);}
  // ---------------- interaction
  @Override public InteractionResult mobInteract(Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);boolean client=level().isClientSide;
   if(racer())return InteractionResult.PASS;
   int f=food(s);
   if(!isTame()&&f>0&&!isBaby()){
    if(!client){if(!p.getAbilities().instabuild)s.shrink(1);quack(this,.7F,1.2F);
     if(random.nextInt(3)==0&&!net.neoforged.neoforge.event.EventHooks.onAnimalTame(this,p)){tame(p);setMode(FOLLOW);setCustomName(Component.literal("Pato de "+p.getGameProfile().getName()));setCustomNameVisible(true);
      level().broadcastEntityEvent(this,(byte)7);msg(p,"§a🦆 Você adotou um pato! §7Pegue o §eApito de Pato§7 para dar ordens • clique: carinho • Shift+clique: no ombro");}
     else level().broadcastEntityEvent(this,(byte)6);}
    return InteractionResult.sidedSuccess(client);}
   if(!isTame()||!isOwnedBy(p))return super.mobInteract(p,hand);
   if(f>0){   // feeding
    if(!client){
     if(hunger()>=100&&!isBaby()){msg(p,"§e🦆 Ele tá cheio! (fome 100/100)");return InteractionResult.CONSUME;}
     if(!p.getAbilities().instabuild)s.shrink(1);setHunger(hunger()+f);setHappy(happy()+8);heal(3);addXp(3);quack(this,.8F,1.3F);level().broadcastEntityEvent(this,(byte)71);
     if(isBaby())ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(-getAge()),true);
     else{int before=fed();entityData.set(FED,fed()+(f>=25?2:1));
      if(!king()&&fed()>=40){flag(KING,true);msg(p,"§6§l👑 O SEU PATO VIROU O PATO REI! §7(coroa, capa e mais forte)");level().playSound(null,blockPosition(),SoundEvents.PLAYER_LEVELUP,SoundSource.NEUTRAL,1F,1.2F);
       if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,getX(),getY()+.5,getZ(),40,.4,.5,.4,.3);}
      else if(before/10!=fed()/10)msg(p,"§e🦆 Seu pato tá ficando gordinho! §7("+fed()+" refeições"+(fed()<40?" • Rei com 40":"")+")");
      resize();}
     if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.HEART,getX(),getY()+.7,getZ(),2,.2,.1,.2,0);}
    return InteractionResult.sidedSuccess(client);}
   if(s.getItem() instanceof HatItem h){
    if(!client){int old=hat();entityData.set(HAT,h.hat);if(!p.getAbilities().instabuild)s.shrink(1);if(old>0)p.getInventory().placeItemBackInInventory(new ItemStack(HAT_ITEMS.get(old-1).get()));
     msg(p,"§d🎩 Seu pato agora usa "+HATS[h.hat]+(h.hat==5?" §7(no modo Guarda-costas ele atira!)":h.hat==6?" §7(fareja minérios mais longe)":""));quack(this,.7F,1.4F);}
    return InteractionResult.sidedSuccess(client);}
   if(s.is(Items.SHEARS)&&hat()>0){if(!client){p.getInventory().placeItemBackInInventory(new ItemStack(HAT_ITEMS.get(hat()-1).get()));entityData.set(HAT,0);}return InteractionResult.sidedSuccess(client);}
   if(s.isEmpty()&&hand==InteractionHand.MAIN_HAND){
    if(p.isShiftKeyDown()){   // onto your head / back down
     if(!client){if(getVehicle()==p)hopOff(p);
      else if(shoulderDucks(p).size()>=2)msg(p,"§e🦆 Os dois ombros já têm pato!");
      else{setOrderedToSit(false);getNavigation().stop();startRiding(p,true);quack(this,.7F,1.5F);
       msg(p,"§a🦆 "+getName().getString()+" subiu no seu "+(shoulderDucks(p).indexOf(this)==0?"ombro direito":"ombro esquerdo")+"! §7Ele vigia monstros e plana com você nas quedas • Shift+clique num bloco com a mão vazia: descer");}}
     return InteractionResult.sidedSuccess(client);}
    if(!client){setHappy(happy()+15);level().broadcastEntityEvent(this,(byte)7);quack(this,.6F,1.5F);if(petCd<=0){addXp(2);petCd=600;}
     msg(p,"§a🦆 "+getName().getString()+" §6Nv "+duckLevel()+" §7("+xpText()+" XP) • fome "+bar(hunger())+" §7• felicidade "+bar(happy())+" §7• "+(isBaby()?"filhote":king()?"§6PATO REI":"adulto")+" §7• modo §e"+MODES[mode()]);}
    return InteractionResult.sidedSuccess(client);}
   return super.mobInteract(p,hand);
  }
  @Override public boolean killedEntity(ServerLevel l,LivingEntity victim){if(victim instanceof net.minecraft.world.entity.monster.Enemy)addXp(10);return super.killedEntity(l,victim);}
  static String bar(int v){return (v<25?"§c":v<60?"§e":"§a")+v+"%";}
  @Override public boolean doHurtTarget(Entity t){
   boolean ok=super.doHurtTarget(t);level().broadcastEntityEvent(this,(byte)71);
   if(ok&&t instanceof LivingEntity le){le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,1));le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,1));quack(this,1.6F,.7F);}   // the terrifying QUACK
   return ok;}
  // ---------------- 0.77.0: on your shoulder (right first, then left)
  public static java.util.List<Duck> shoulderDucks(Player p){var l=new java.util.ArrayList<Duck>();for(var e:p.getPassengers())if(e instanceof Duck d)l.add(d);return l;}
  @Override public Vec3 getVehicleAttachmentPoint(Entity v){
   if(!(v instanceof Player p))return super.getVehicleAttachmentPoint(v);
   int i=shoulderDucks(p).indexOf(this);double side=i==1?-1:1;float yaw=(float)Math.toRadians(p.yBodyRot);
   double rx=-Math.cos(yaw),rz=-Math.sin(yaw);   // the player's right
   double up=p.isCrouching()?1.16:p.isSwimming()||p.isFallFlying()||p.isVisuallyCrawling()?.4:1.46;
   Vec3 want=p.position().add(rx*side*.36+Math.sin(yaw)*.04,up,rz*side*.36-Math.cos(yaw)*.04);
   return p.getPassengerRidingPosition(this).subtract(want);}
  public void hopOff(Player p){stopRiding();double yaw=Math.toRadians(p.yBodyRot);moveTo(p.getX()-Math.cos(yaw)*.8,p.getY(),p.getZ()-Math.sin(yaw)*.8,p.getYRot(),0);setDeltaMovement(0,.25,0);quack(this,.7F,1.3F);}
  static void shoulderOff(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){
   var p=e.getEntity();if(p.level().isClientSide||!p.isShiftKeyDown()||e.getHand()!=InteractionHand.MAIN_HAND||!p.getMainHandItem().isEmpty())return;
   var ds=shoulderDucks(p);if(ds.isEmpty())return;for(var d:ds)d.hopOff(p);msg(p,"§7🦆 "+(ds.size()>1?"Os patos desceram":ds.get(0).getName().getString()+" desceu")+" do ombro.");e.setCanceled(true);}
  /** on the shoulder: spreads its wings when the owner falls (slow falling) */
  void shoulderTick(Player p){
   if(level().isClientSide)return;
   if(p.fallDistance>2.5F&&!p.getAbilities().flying&&!p.isFallFlying()&&!p.isInWater()){
    p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,15,0,false,false,true));p.fallDistance=0;
    if(tickCount%12==0)quack(this,.5F,1.6F);if(level() instanceof ServerLevel sl&&tickCount%3==0)sl.sendParticles(ParticleTypes.CLOUD,getX(),getY()+.3,getZ(),1,.2,.05,.2,0);
    entityData.set(GLIDE,true);}
   else if(entityData.get(GLIDE)&&p.onGround())entityData.set(GLIDE,false);}
  // ---------------- riding on your head (like the tiny frog)
  private int headSync;
  @Override public boolean startRiding(Entity v,boolean force){boolean ok=super.startRiding(v,force);if(ok&&v instanceof Player)headSync=3;return ok;}
  @Override public void removeVehicle(){var v=getVehicle();super.removeVehicle();if(v instanceof net.minecraft.server.level.ServerPlayer sp)sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(sp));}
  @Override public void rideTick(){super.rideTick();
   if(headSync>0&&--headSync==0&&getVehicle() instanceof net.minecraft.server.level.ServerPlayer sp)sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(sp));
   if(getVehicle() instanceof Player p){setYRot(p.yBodyRot);yBodyRot=getYRot();yHeadRot=p.getYHeadRot();setXRot(0);fallDistance=0;shoulderTick(p);}}
  @Override public boolean isInvulnerableTo(DamageSource s){return (getVehicle() instanceof Player||racer())&&!s.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)||super.isInvulnerableTo(s);}
  @Override public boolean removeWhenFarAway(double d){return false;}
  // ---------------- skate & surf: rides on the front of your board
  Entity board(){var o=getOwner();if(o==null||mode()!=FOLLOW||isPassenger())return null;var v=o.getVehicle();return v instanceof Skate.Board||v instanceof Surf.Board?v:null;}
  void stickToBoard(Entity b){double r=Math.toRadians(b.getYRot());double ox=-Math.sin(r)*.62,oz=Math.cos(r)*.62;
   setPos(b.getX()+ox,b.getY()+(b instanceof Surf.Board?.17:.28),b.getZ()+oz);setDeltaMovement(Vec3.ZERO);setYRot(b.getYRot());yBodyRot=b.getYRot();yHeadRot=b.getYRot();fallDistance=0;}
  @Override public void tick(){
   super.tick();
   if(racer())return;
   Entity b=null;if(isTame()){var o=getOwner();if(o!=null&&mode()==FOLLOW&&!isPassenger()){var v=o.getVehicle();if((v instanceof Skate.Board||v instanceof Surf.Board)&&distanceToSqr(v)<16*16)b=v;}}
   if(b!=null){noPhysics=true;setNoGravity(true);stickToBoard(b);if(!level().isClientSide&&!flag(BOARD)){flag(BOARD,true);quack(this,.9F,1.4F);}}
   else if(flag(BOARD)||noPhysics&&!isPassenger()){noPhysics=false;setNoGravity(false);if(!level().isClientSide){flag(BOARD,false);var o=getOwner();if(o!=null)moveTo(o.getX()+1,o.getY(),o.getZ());}}
  }
  @Override public void aiStep(){
   super.aiStep();
   flapO=flap;boolean flying=!onGround()&&!isInWater()&&!isPassenger()&&!flag(BOARD);flap=flying||isPassenger()&&entityData.get(GLIDE)||flag(BOARD)&&level().isClientSide&&random.nextInt(30)==0?flap+.5F:flap*.8F;
   // floats on water like a real duck (unless diving)
   if(isInWater()&&!flag(DIVE)){double up=getFluidHeight(net.minecraft.tags.FluidTags.WATER);if(up>.25)setDeltaMovement(getDeltaMovement().multiply(.9,.5,.9).add(0,.06,0));}
   if(level().isClientSide){
    if(flag(SLEEP)&&tickCount%30==0)level().addParticle(ParticleTypes.NOTE,getX(),getY()+.8*getScale(),getZ(),.0,0,0);
    if(isTame()&&hunger()<20&&tickCount%40==0)level().addParticle(ParticleTypes.ANGRY_VILLAGER,getX(),getY()+.8*getScale(),getZ(),0,0,0);
    if(color()==4&&tickCount%6==0)level().addParticle(ParticleTypes.END_ROD,getRandomX(.4),getY()+.4,getRandomZ(.4),0,.01,0);
    if(isTame()&&duckLevel()>=10&&tickCount%5==0)level().addParticle(ParticleTypes.WAX_OFF,getRandomX(.5),getY()+random.nextDouble()*.6,getRandomZ(.5),0,.02,0);
    if(flag(BATH)&&tickCount%8==0)level().addParticle(random.nextBoolean()?ParticleTypes.SPLASH:ParticleTypes.BUBBLE_POP,getRandomX(.4),getY()+.15,getRandomZ(.4),0,.05,0);
    return;}
   if(racer())return;
   if(!isTame())return;
   var owner=getOwner();
   if(companion(owner))return;
   // hunger & happiness
   if(tickCount%1200==0){setHunger(hunger()-4);setHappy(happy()-(hunger()<25?3:1));}
   if(hunger()<20&&owner!=null&&distanceToSqr(owner)<9&&random.nextInt(500)==0&&!isPassenger()){owner.hurt(damageSources().mobAttack(this),1);level().broadcastEntityEvent(this,(byte)71);quack(this,1.2F,.8F);
    if(owner instanceof Player p)msg(p,"§c🦆 O pato tá com fome e te bicou! §7Dê pão, sementes ou peixe.");}
   if(isPassenger()||flag(BOARD))return;
   int m=mode();
   // soldier: the army helmet comes with a tiny gun
   if(m==GUARD&&hat()==5&&getTarget()!=null&&getTarget().isAlive()&&--shotCd<=0&&distanceToSqr(getTarget())<14*14&&hasLineOfSight(getTarget())){
    shotCd=22;var t=getTarget();t.hurt(damageSources().mobAttack(this),3);
    if(level() instanceof ServerLevel sl){Vec3 a=position().add(0,.45*getScale(),0),d=t.getEyePosition().subtract(a);for(int i=0;i<8;i++){Vec3 q=a.add(d.scale(i/8.0));sl.sendParticles(ParticleTypes.CRIT,q.x,q.y,q.z,1,0,0,0,0);}}
    level().playSound(null,blockPosition(),SoundEvents.CROSSBOW_SHOOT,SoundSource.NEUTRAL,.7F,1.9F);}
   // night: sleep on a nest and maybe lay a golden egg
   if((m==FOLLOW||m==SIT)&&!level().isDay()){
    if(--nestCheck<=0){nestCheck=60;var nest=findNest(14);
     if(nest!=null){Vec3 c=Vec3.atBottomCenterOf(nest).add(0,.25,0);if(position().distanceToSqr(c)>.6)getNavigation().moveTo(c.x,c.y,c.z,1.0);else{getNavigation().stop();moveTo(c.x,getY(),c.z);if(!flag(SLEEP))flag(SLEEP,true);}}}
    if(flag(SLEEP)&&--eggTimer<=0){eggTimer=(duckLevel()>=10?1200:2400)+random.nextInt(2400);
     if(hunger()>40&&happy()>50&&random.nextInt(3)>0){spawnAtLocation(new ItemStack(GOLDEN_EGG.get()));quack(this,1F,1.2F);if(owner instanceof Player p&&distanceToSqr(p)<32*32)msg(p,"§6🥚 Seu pato botou um OVO DOURADO no ninho!");}}
   }else if(flag(SLEEP))flag(SLEEP,false);
   if(flag(SLEEP)&&getNavigation().isInProgress())flag(SLEEP,false);
   if(m==FISH)fish(owner);
   if(m==SNIFF)sniff(owner);
  }
  /** 0.76.0: bath, alarm, healing and fetching; true while splashing in the tub (nothing else to do then) */
  boolean companion(LivingEntity owner){
   if(petCd>0)petCd--;if(alarmCd>0)alarmCd--;if(healCd>0)healCd--;
   // 0.76.0: the alarm also works while the duck is in the tub
   if(owner instanceof Player ap&&(!isPassenger()||getVehicle()==owner)&&alarmCd<=0&&tickCount%10==0&&duckLevel()>=2&&distanceToSqr(owner)<24*24)alarm(ap,duckLevel());
   if(bathTicks>0)return bathTick(owner);
   if(owner==null||isPassenger()||flag(BOARD))return false;
   double od=distanceToSqr(owner);
   if(od<12*12&&++companionTicks>=1200){companionTicks=0;addXp(1);}
   int lv=duckLevel();
   if(lv>=7&&healCd<=0&&od<12*12&&owner.getHealth()<owner.getMaxHealth()*.3F&&happy()>40){healCd=2400;owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION,120,1));
    quack(this,1F,1.5F);if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.HEART,owner.getX(),owner.getY()+1.2,owner.getZ(),8,.4,.4,.4,0);
    if(owner instanceof Player p)msg(p,"§a❤ "+getName().getString()+" cuidou de você! §7(regeneração)");addXp(2);}
   if(mode()==COLLECT&&lv>=3&&owner instanceof Player p)collect(p);
   if(--bathCheck<=0){bathCheck=200;
    if((mode()==FOLLOW||mode()==COLLECT&&getMainHandItem().isEmpty())&&getTarget()==null&&od<16*16&&random.nextInt(6)==0&&!flag(SLEEP)){var tub=findTub(10);if(tub!=null)goBath(tub,false);}}
   return false;}
  void alarm(Player p,int lv){
   double r=8+lv;Mob best=null;double bd=1e9;
   for(var m:level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(r),m->m instanceof Enemy&&m.isAlive()&&!(m instanceof Duck))){
    Long w=WARNED.get(m.getUUID());if(w!=null&&level().getGameTime()-w<600)continue;double d=m.distanceToSqr(p);if(d<bd){bd=d;best=m;}}
   long now=level().getGameTime();if(WARNED.size()>256)WARNED.entrySet().removeIf(en->now-en.getValue()>600||now<en.getValue());
   if(best==null)return;WARNED.put(best.getUUID(),now);alarmCd=60;
   Vec3 to=best.position().subtract(p.position());double ang=Mth.wrapDegrees(Math.toDegrees(Math.atan2(-to.x,to.z))-p.getYRot());
   String dir=Math.abs(ang)<45?"na sua frente":Math.abs(ang)>135?"atrás de você":ang>0?"à sua direita":"à sua esquerda";
   quack(this,1.6F,1.6F);level().broadcastEntityEvent(this,(byte)70);
   p.displayClientMessage(Component.literal("§c⚠ 🦆 QUACK! §f"+best.getName().getString()+" §c"+dir+" §7("+(int)Math.sqrt(bd)+" blocos)"),true);
   level().playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.NEUTRAL,.6F,1.8F);
   if(lv>=5)best.addEffect(new MobEffectInstance(MobEffects.GLOWING,140,0));
   addXp(1);}
  void collect(Player p){
   var held=getMainHandItem();
   if(!held.isEmpty()){
    if(distanceToSqr(p)<2.4*2.4){var give=held.copy();setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,ItemStack.EMPTY);if(!p.getInventory().add(give))spawnAtLocation(give);
     quack(this,.8F,1.4F);level().broadcastEntityEvent(this,(byte)71);addXp(1);level().playSound(null,p.blockPosition(),SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.4F,1.4F);}
    else if(tickCount%10==0)getNavigation().moveTo(p,1.35);
    return;}
   if(collectTarget!=null&&(!collectTarget.isAlive()||collectTarget.distanceToSqr(p)>18*18))collectTarget=null;
   if(collectTarget==null&&tickCount%20==0){double bd=1e9;
    for(var it:level().getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(12),i->i.isAlive()&&!i.hasPickUpDelay()&&i.distanceToSqr(p)<18*18&&i.getOwner()!=p)){double d=it.distanceToSqr(this);if(d<bd){bd=d;collectTarget=it;}}}
   if(collectTarget==null)return;
   if(distanceToSqr(collectTarget)<1.4*1.4){var st=collectTarget.getItem().copy();collectTarget.discard();collectTarget=null;
    setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,st);setGuaranteedDrop(net.minecraft.world.entity.EquipmentSlot.MAINHAND);quack(this,.6F,1.6F);level().broadcastEntityEvent(this,(byte)71);}
   else if(tickCount%10==0)getNavigation().moveTo(collectTarget,1.3);}
  // ---------------- bathtub
  int bathWalk;boolean bathWithOwner;
  BlockPos findTub(int r){var at=blockPosition();BlockPos best=null;double bd=1e9;
   for(var q:BlockPos.betweenClosed(at.offset(-r,-3,-r),at.offset(r,3,r))){var st=level().getBlockState(q);if(st.getBlock() instanceof Bath.BathBlock&&st.getValue(Bath.FILLED)){double d=q.distSqr(at);if(d<bd){bd=d;best=q.immutable();}}}return best;}
  /** off to the tub (with the owner when they get in) */
  public boolean goBath(BlockPos tub,boolean withOwner){
   if(bathTicks>0||isPassenger()||racer())return false;var st=level().getBlockState(tub);if(!(st.getBlock() instanceof Bath.BathBlock)||!st.getValue(Bath.FILLED))return false;
   bathPos=tub;bathTicks=withOwner?6000:500+random.nextInt(500);bathWithOwner=withOwner;bathEntered=false;bathWalk=0;flag(SLEEP,false);setTarget(null);return true;}
  boolean bathTick(LivingEntity owner){
   var st=level().getBlockState(bathPos);
   boolean ok=st.getBlock() instanceof Bath.BathBlock&&st.getValue(Bath.FILLED)&&getTarget()==null&&!isPassenger()&&(owner==null||distanceToSqr(owner)<24*24);
   if(bathWithOwner&&owner!=null&&!(owner.getVehicle() instanceof Furniture.Seat)&&bathTicks>60)bathTicks=60;
   if(!ok||--bathTicks<=0||!bathEntered&&++bathWalk>400){leaveBath();return false;}
   if(!bathEntered){
    if(position().distanceToSqr(Vec3.atBottomCenterOf(bathPos))<2.4*2.4){bathEntered=true;flag(BATH,true);setNoGravity(true);getNavigation().stop();quack(this,1F,1.5F);bathPhase=random.nextFloat()*6;
     if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.SPLASH,bathPos.getX()+.5,bathPos.getY()+.6,bathPos.getZ()+.5,20,.3,.1,.3,.1);}
    else{if(tickCount%10==0)getNavigation().moveTo(bathPos.getX()+.5,bathPos.getY()+1,bathPos.getZ()+.5,1.25);return false;}}
   // paddle up and down the tub
   BlockPos o=Bath.BathBlock.other(bathPos,st);Vec3 a=Vec3.atBottomCenterOf(bathPos),b=Vec3.atBottomCenterOf(o),mid=a.add(b).scale(.5),ax=b.subtract(a).normalize(),side=new Vec3(-ax.z,0,ax.x);
   bathPhase+=.012F;Vec3 p=mid.add(ax.scale(Mth.sin(bathPhase)*.5)).add(side.scale(Mth.sin(bathPhase*1.7F)*.14));Vec3 v=ax.scale(Mth.cos(bathPhase)*.5).add(side.scale(Mth.cos(bathPhase*1.7F)*.24));
   setPos(p.x,bathPos.getY()+.3,p.z);setDeltaMovement(Vec3.ZERO);getNavigation().stop();fallDistance=0;
   if(v.lengthSqr()>1e-4){float yaw=(float)Math.toDegrees(Math.atan2(-v.x,v.z));setYRot(yaw);yBodyRot=yaw;yHeadRot=yaw;}
   if(tickCount%40==0)setHappy(happy()+1);
   if(random.nextInt(300)==0){quack(this,.7F,1.4F);level().broadcastEntityEvent(this,(byte)70);}
   return true;}
  void leaveBath(){
   if(bathEntered){flag(BATH,false);setNoGravity(false);addXp(5);setHappy(happy()+10);
    var st=level().getBlockState(bathPos);BlockPos out=null;
    if(st.getBlock() instanceof Bath.BathBlock)for(BlockPos h:new BlockPos[]{bathPos,Bath.BathBlock.other(bathPos,st)})for(var d:Direction.Plane.HORIZONTAL){var q=h.relative(d);if(out==null&&level().getBlockState(q).isAir()&&level().getBlockState(q.above()).isAir())out=q;}
    if(out!=null)moveTo(out.getX()+.5,out.getY(),out.getZ()+.5);else moveTo(getX(),bathPos.getY()+1,getZ());
    if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.SPLASH,getX(),getY()+.4,getZ(),25,.25,.2,.25,.15);}
   bathTicks=0;bathPos=null;bathEntered=false;}
  BlockPos findNest(int r){BlockPos best=null;double bd=1e9;var at=blockPosition();
   for(var p:BlockPos.betweenClosed(at.offset(-r,-3,-r),at.offset(r,3,r)))if(level().getBlockState(p).is(NEST.get())){double d=p.distSqr(at);if(d<bd){bd=d;best=p.immutable();}}return best;}
  void fish(LivingEntity owner){
   if(!isInWater()){if(tickCount%40==0){var at=blockPosition();BlockPos best=null;double bd=1e9;
     for(var p:BlockPos.betweenClosed(at.offset(-8,-2,-8),at.offset(8,2,8)))if(level().getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&level().getBlockState(p.above()).isAir()){double d=p.distSqr(at);if(d<bd){bd=d;best=p.immutable();}}
     if(best!=null)getNavigation().moveTo(best.getX()+.5,best.getY()+1,best.getZ()+.5,1.1);
     else if(tickCount%200==0&&owner instanceof Player p&&distanceToSqr(p)<16*16)msg(p,"§7🦆 Não tem água perto pra pescar...");}
    flag(DIVE,false);return;}
   if(flag(DIVE)){setDeltaMovement(getDeltaMovement().add(0,-.03,0));if(--diveTicks<=0){flag(DIVE,false);setDeltaMovement(0,.35,0);
     int r=random.nextInt(100);var fish=new ItemStack(r<55?Items.COD:r<85?Items.SALMON:r<95?Items.TROPICAL_FISH:Items.PUFFERFISH);
     var it=new ItemEntity(level(),getX(),getY()+.6,getZ(),fish);if(owner!=null){Vec3 d=owner.position().subtract(position()).normalize();it.setDeltaMovement(d.x*.35,.35,d.z*.35);}level().addFreshEntity(it);
     quack(this,1F,1.3F);setHappy(happy()+3);addXp(5);if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.SPLASH,getX(),getY()+.3,getZ(),20,.3,.1,.3,.1);}}
   else if(--fishTimer<=0){fishTimer=300+random.nextInt(400);flag(DIVE,true);diveTicks=40;if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.SPLASH,getX(),getY()+.3,getZ(),12,.2,.1,.2,.1);}
  }
  void sniff(LivingEntity owner){
   if(--sniffTimer>0)return;sniffTimer=80;int r=hat()==6?16:10;var at=blockPosition();BlockPos best=null;int bv=-1;double bd=1e9;
   for(var p:BlockPos.betweenClosed(at.offset(-r,-r,-r),at.offset(r,r,r))){var st=level().getBlockState(p);if(!st.is(net.neoforged.neoforge.common.Tags.Blocks.ORES))continue;
    String id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath();int v=id.contains("ancient")?6:id.contains("diamond")?5:id.contains("emerald")?4:id.contains("gold")?3:id.contains("redstone")||id.contains("lapis")?2:id.contains("iron")||id.contains("copper")?1:0;
    double d=p.distSqr(at);if(v>bv||v==bv&&d<bd){bv=v;bd=d;best=p.immutable();}}
   if(best==null){if(owner instanceof Player p&&distanceToSqr(p)<20*20&&random.nextInt(4)==0)msg(p,"§7🦆 *fareja* ... nada por aqui.");return;}
   getNavigation().moveTo(best.getX()+.5,best.getY()+1,best.getZ()+.5,1.0);addXp(bv>=5?6:3);
   String name=Component.translatable(level().getBlockState(best).getBlock().getDescriptionId()).getString();
   quack(this,bv>=5?1.6F:.9F,bv>=5?1.6F:1.1F);level().broadcastEntityEvent(this,(byte)70);
   if(level() instanceof ServerLevel sl){Vec3 a=position().add(0,.6,0),d=Vec3.atCenterOf(best).subtract(a);for(int i=1;i<10;i++){Vec3 q=a.add(d.scale(i/10.0));sl.sendParticles(bv>=5?ParticleTypes.HAPPY_VILLAGER:ParticleTypes.WAX_ON,q.x,q.y,q.z,1,0,0,0,0);}}
   if(owner instanceof Player p&&distanceToSqr(p)<24*24)msg(p,(bv>=5?"§b§l🦆 QUACK! ":"§e🦆 Quack! ")+"§7Cheiro de §f"+name+"§7 a §f"+(int)Math.sqrt(bd)+" blocos"+(best.getY()<getY()-1?" ↓":best.getY()>getY()+1?" ↑":""));
  }
  @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);
   t.putInt("DuckColor",color());t.putInt("DuckHat",hat());t.putInt("DuckFed",fed());t.putInt("DuckMode",mode());t.putInt("DuckHunger",hunger());t.putInt("DuckHappy",happy());t.putBoolean("DuckKing",king());t.putBoolean("DuckRacer",racer());t.putInt("DuckXp",xp());}
  @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);
   entityData.set(COLOR,t.getInt("DuckColor"));entityData.set(HAT,t.getInt("DuckHat"));entityData.set(FED,t.getInt("DuckFed"));entityData.set(MODE,t.getInt("DuckMode"));
   if(t.contains("DuckHunger")){setHunger(t.getInt("DuckHunger"));setHappy(t.getInt("DuckHappy"));}flag(KING,t.getBoolean("DuckKing"));flag(RACER,t.getBoolean("DuckRacer"));entityData.set(XP,t.getInt("DuckXp"));setNoGravity(false);resize();}
  @Override public void checkDespawn(){if(racer()&&tickCount>2400)discard();else super.checkDespawn();}
 }

 // ======================================================================= items
 public static final class HatItem extends Item {
  public final int hat;public HatItem(int h,Properties p){super(p);hat=h;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Clique no seu pato para ele usar. Tesoura tira."));
   if(hat==5)t.add(Component.literal("§7No modo §eGuarda-costas§7 o pato atira com uma mini-arma."));if(hat==6)t.add(Component.literal("§7No modo §eFarejar§7 acha minérios mais longe."));}
 }
 public static final class GoldenEgg extends Item {
  public GoldenEgg(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){var s=p.getItemInHand(h);
   if(!l.isClientSide){int n=3+l.random.nextInt(6);p.getInventory().placeItemBackInInventory(new ItemStack(Items.GOLD_NUGGET,n));boolean dia=l.random.nextInt(20)==0;if(dia)p.getInventory().placeItemBackInInventory(new ItemStack(Items.DIAMOND));
    l.addFreshEntity(new ExperienceOrb(l,p.getX(),p.getY(),p.getZ(),10));l.playSound(null,p.blockPosition(),SoundEvents.TURTLE_EGG_CRACK,SoundSource.PLAYERS,1F,1.2F);
    msg(p,"§6🥚 Ovo dourado: §e"+n+" pepitas de ouro"+(dia?" §b+ 1 DIAMANTE!":""));if(!p.getAbilities().instabuild)s.shrink(1);}
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Botão direito: quebra (ouro, XP e às vezes diamante)."));}
 }
 public static final class Whistle extends Item {
  public Whistle(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){var s=p.getItemInHand(h);
   if(!l.isClientSide){var ducks=l.getEntitiesOfClass(Duck.class,p.getBoundingBox().inflate(48),d->d.isTame()&&d.isOwnedBy(p)&&!d.racer());
    if(ducks.isEmpty()){msg(p,"§7🦆 Nenhum pato seu por perto (48 blocos).");return InteractionResultHolder.fail(s);}
    l.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_FLUTE.value(),SoundSource.PLAYERS,1F,1.8F);
    if(p.isShiftKeyDown()){for(var d:ducks){if(d.isPassenger())continue;d.setMode(FOLLOW);d.moveTo(p.getX()+(l.random.nextDouble()-.5)*2,p.getY(),p.getZ()+(l.random.nextDouble()-.5)*2);quack(d,.8F,1.3F);}
     msg(p,"§a🦆 "+ducks.size()+" pato(s) vieram correndo! §7(modo Seguir)");}
    else{var t=s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();int m=(t.getInt("Mode")+1)%MODES.length;t.putInt("Mode",m);
     s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(t));
     for(var d:ducks){d.setMode(m);quack(d,.7F,1.2F);}
     msg(p,"§e🦆 Modo: §f"+MODES[m]+" §7("+ducks.size()+" pato"+(ducks.size()>1?"s":"")+") • "+new String[]{"seguem você","ficam sentados","atacam monstros perto de você","pescam e trazem peixe","farejam minérios","buscam itens do chão e trazem pra você (nível 3+)"}[m]);}}
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Botão direito: troca o modo dos seus patos"));t.add(Component.literal("§8Seguir → Sentar → Guarda-costas → Pescar → Farejar → Coletar"));t.add(Component.literal("§7Shift + botão direito: chama todos os patos"));}
 }
 // ======================================================================= nest
 public static final class NestBlock extends Block {
  static final VoxelShape SHAPE=Block.box(1,0,1,15,4,15);
  public NestBlock(Properties p){super(p);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
 }
 // ======================================================================= duck races (casino)
 public static final String[] RACERS={"Bolinha","Trovão","Biscoito","Foguete"};
 public static final class RaceBlock extends BaseEntityBlock {
  public RaceBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HorizontalDirectionalBlock.FACING);}
  @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,c.getHorizontalDirection());}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new RaceEntity(p,s);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,RACE_ENTITY.get(),(lv,ps,st,e)->e.tick());}
  @Override protected ItemInteractionResult useItemOn(ItemStack s,BlockState st,Level l,BlockPos pos,Player p,InteractionHand h,BlockHitResult hit){
   if(!(l.getBlockEntity(pos) instanceof RaceEntity r))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   if(!l.isClientSide){if(s.is(Casino.CHIP.get()))r.bet(p,s);else if(s.isEmpty())r.choose(p);else return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
   return ItemInteractionResult.sidedSuccess(l.isClientSide);}
  @Override protected InteractionResult useWithoutItem(BlockState st,Level l,BlockPos pos,Player p,BlockHitResult hit){
   if(!l.isClientSide&&l.getBlockEntity(pos) instanceof RaceEntity r)r.choose(p);return InteractionResult.sidedSuccess(l.isClientSide);}
 }
 public static final class RaceEntity extends BlockEntity {
  public static final double LENGTH=14;
  int state,timer,winner=-1;final Map<UUID,Integer> lane=new HashMap<>();final Map<UUID,int[]> bets=new HashMap<>();final int[] ids={-1,-1,-1,-1};final double[] prog=new double[4];
  public RaceEntity(BlockPos p,BlockState s){super(RACE_ENTITY.get(),p,s);}
  Direction dir(){return getBlockState().getValue(HorizontalDirectionalBlock.FACING);}
  void choose(Player p){if(state==2){msg(p,"§7A corrida já começou!");return;}int c=(lane.getOrDefault(p.getUUID(),-1)+1)%4;lane.put(p.getUUID(),c);
   msg(p,"§e🦆 Raia "+(c+1)+" — "+RACERS[c]+" §7• clique com §efichas§7 na mão para apostar (paga 3×)");}
  void bet(Player p,ItemStack chips){
   if(state>=2){msg(p,"§7Espere a próxima corrida.");return;}
   int c=lane.getOrDefault(p.getUUID(),0);int n=Math.min(16,chips.getCount());if(!p.getAbilities().instabuild)chips.shrink(n);
   var old=bets.get(p.getUUID());if(old!=null&&old[0]==c)old[1]+=n;else{if(old!=null)Casino.give(p,old[1]);bets.put(p.getUUID(),new int[]{c,n});}
   msg(p,"§a🎰 Aposta: "+bets.get(p.getUUID())[1]+" fichas na raia "+(c+1)+" ("+RACERS[c]+")");
   if(state==0){state=1;timer=100;announce("§6🦆 Corrida de patos em 5 segundos! §7Apostem!");}}
  void announce(String t){if(level==null)return;for(var p:level.players())if(p.blockPosition().distSqr(worldPosition)<40*40)p.displayClientMessage(Component.literal(t),true);}
  Vec3 start(int i){var f=dir();var r=f.getClockWise();return Vec3.atBottomCenterOf(worldPosition).add(Vec3.atLowerCornerOf(f.getNormal()).scale(1.2)).add(Vec3.atLowerCornerOf(r.getNormal()).scale(i-1.5));}
  void tick(){
   if(!(level instanceof ServerLevel sl))return;
   if(state==1&&--timer<=0){   // the ducks line up
    for(int i=0;i<4;i++){var d=ENTITY.get().create(sl);if(d==null)continue;var at=start(i);d.moveTo(at.x,at.y,at.z,dir().toYRot(),0);d.racer(i);d.setCustomName(Component.literal("§l"+(i+1)+" "+RACERS[i]));d.setCustomNameVisible(true);
     sl.addFreshEntity(d);ids[i]=d.getId();prog[i]=0;}
    state=2;timer=0;winner=-1;level.playSound(null,worldPosition,SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,1.2F,1F);announce("§a§l🏁 VALENDO!");}
   else if(state==2){timer++;int lead=0;
    for(int i=0;i<4;i++){if(!(sl.getEntity(ids[i]) instanceof Duck d))continue;
     double sp=.11+sl.random.nextDouble()*.09;if(sl.random.nextInt(40)==0)sp+=.35;if(sl.random.nextInt(60)==0)sp=.01;   // bursts and stumbles
     prog[i]=Math.min(LENGTH,prog[i]+sp);var at=start(i).add(Vec3.atLowerCornerOf(dir().getNormal()).scale(prog[i]));
     d.moveTo(at.x,at.y+(Math.sin((timer+i*3)*.9)>.6?.12:0),at.z,dir().toYRot(),0);d.setYHeadRot(dir().toYRot());if(sl.random.nextInt(50)==0)quack(d,.8F,1.1F+i*.1F);
     if(prog[i]>prog[lead])lead=i;
     if(prog[i]>=LENGTH&&winner<0)winner=i;}
    if(timer%10==0&&winner<0)announce("§e🦆 Na frente: raia "+(lead+1)+" — "+RACERS[lead]);
    if(winner>=0){state=3;timer=60;finish(sl);}}
   else if(state==3&&--timer<=0){for(int id:ids)if(sl.getEntity(id) instanceof Duck d)d.discard();Arrays.fill(ids,-1);state=0;}
  }
  void finish(ServerLevel sl){
   announce("§6§l🏆 VENCEU A RAIA "+(winner+1)+" — "+RACERS[winner]+"!");sl.playSound(null,worldPosition,SoundEvents.PLAYER_LEVELUP,SoundSource.BLOCKS,1F,1.4F);
   if(sl.getEntity(ids[winner]) instanceof Duck d)sl.sendParticles(ParticleTypes.FIREWORK,d.getX(),d.getY()+.6,d.getZ(),30,.3,.4,.3,.1);
   for(var e:bets.entrySet()){var p=sl.getPlayerByUUID(e.getKey());if(p==null)continue;int[] b=e.getValue();
    if(b[0]==winner){Casino.give(p,b[1]*3);p.sendSystemMessage(Component.literal("§a🦆 Você ganhou §e"+b[1]*3+" fichas§a na corrida de patos!"));}
    else p.sendSystemMessage(Component.literal("§7🦆 Seu pato ("+RACERS[b[0]]+") perdeu. Venceu "+RACERS[winner]+"."));}
   bets.clear();}
  @Override public void setRemoved(){super.setRemoved();if(level instanceof ServerLevel sl)for(int id:ids)if(sl.getEntity(id) instanceof Duck d)d.discard();}
 }
}
