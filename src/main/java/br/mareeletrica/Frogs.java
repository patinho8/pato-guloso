package br.mareeletrica;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** Sapo pequenino: a tiny frog (a fifth of a block) that hops around swamps, rivers and jungles, croaks with a puffed throat,
 *  catches flies with its tongue, and can be picked up (sneak + empty hand) and carried as a pet item. Three colours. */
public final class Frogs {
 public static final String[] VARIANTS={"green","gold","blue"};
 public static final String[] NAMES={"verde","dourado","azul"};
 public static final Supplier<EntityType<TinyFrog>> ENTITY=MareEletrica.ACTORS.register("tiny_frog",()->
  EntityType.Builder.<TinyFrog>of(TinyFrog::new,MobCategory.CREATURE).sized(.32F,.24F).eyeHeight(.18F).clientTrackingRange(8).build(MareEletrica.ID+":tiny_frog"));
 public static final DeferredItem<DeferredSpawnEggItem> EGG=MareEletrica.ITEMS.register("tiny_frog_spawn_egg",()->new DeferredSpawnEggItem(ENTITY,0x4E9A2E,0xF0C64A,new Item.Properties()));
 /** The frog in your pocket: right click on a block to let it go. */
 public static final DeferredItem<PocketFrog> POCKET=MareEletrica.ITEMS.register("tiny_frog",()->new PocketFrog(new Item.Properties().stacksTo(1)));
 private Frogs(){}
 public static void register(IEventBus bus){bus.addListener(Frogs::attributes);bus.addListener(Frogs::placements);
  var g=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;g.addListener(Frogs::logout);g.addListener(Frogs::login);g.addListener(Frogs::travel);g.addListener(Frogs::changed);g.addListener(Frogs::rightClickBlock);}
 /** The frog riding on this player's head, or null. */
 public static TinyFrog onHead(Player p){for(var e:p.getPassengers())if(e instanceof TinyFrog f)return f;return null;}
 private static final String HEAD="MareFrogOnHead";
 /** Leaving the world or changing dimension: the frog on your head goes with you (saved on the player, put back after). */
 public static void stash(Player p){var f=onHead(p);if(f==null||p.level().isClientSide)return;var t=new CompoundTag();f.stopRiding();f.save(t);p.getPersistentData().put(HEAD,t);f.discard();}
 public static void unstash(Player p){
  if(p.level().isClientSide||!p.getPersistentData().contains(HEAD))return;var t=p.getPersistentData().getCompound(HEAD);p.getPersistentData().remove(HEAD);
  var e=EntityType.create(t,p.level()).orElse(null);if(!(e instanceof TinyFrog f))return;
  f.moveTo(p.getX(),p.getY()+p.getBbHeight(),p.getZ(),p.getYRot(),0);p.level().addFreshEntity(f);f.startRiding(p,true);
 }
 static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){stash(e.getEntity());}
 static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){unstash(e.getEntity());}
 static void travel(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent e){if(e.getEntity() instanceof Player p)stash(p);}
 static void changed(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){unstash(e.getEntity());}
 /** Sneak + right click on a block with the empty hand while a frog is on your head: it hops down there. */
 static void rightClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){
  var p=e.getEntity();if(!p.isShiftKeyDown()||!p.getMainHandItem().isEmpty()||e.getHand()!=InteractionHand.MAIN_HAND)return;var f=onHead(p);if(f==null)return;
  if(!p.level().isClientSide){var at=e.getPos().relative(e.getFace()==null?net.minecraft.core.Direction.UP:e.getFace());f.stopRiding();f.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,p.getYRot(),0);
   f.setDeltaMovement(0,.25,0);p.level().playSound(null,at,SoundEvents.FROG_LONG_JUMP,SoundSource.NEUTRAL,.6F,1.8F);}
  e.setCanceled(true);e.setCancellationResult(InteractionResult.sidedSuccess(p.level().isClientSide));
 }
 static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),TinyFrog.attributes().build());}
 static void placements(RegisterSpawnPlacementsEvent e){
  e.register(ENTITY.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,l,r,p,rnd)->l.getRawBrightness(p,0)>8&&(l.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.ANIMALS_SPAWNABLE_ON)||l.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.MUD)||l.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.SAND)),RegisterSpawnPlacementsEvent.Operation.REPLACE);
 }

 public static final class TinyFrog extends net.minecraft.world.entity.TamableAnimal {
  private static final EntityDataAccessor<Integer> VARIANT=SynchedEntityData.defineId(TinyFrog.class,EntityDataSerializers.INT);
  /** Client animation clocks (ticks): last croak, last tongue flick. */
  public int croakAt=-100,tongueAt=-100;private int hopDelay;
  public TinyFrog(EntityType<? extends TinyFrog> t,Level l){super(t,l);}
  public boolean onHead(){return getVehicle() instanceof Player;}
  public static AttributeSupplier.Builder attributes(){return Animal.createMobAttributes().add(Attributes.MAX_HEALTH,1).add(Attributes.MOVEMENT_SPEED,.07).add(Attributes.JUMP_STRENGTH,.5);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(VARIANT,0);}
  public int variant(){return Mth.clamp(entityData.get(VARIANT),0,VARIANTS.length-1);}
  public void setVariant(int v){entityData.set(VARIANT,Mth.clamp(v,0,VARIANTS.length-1));}
  @Override protected void registerGoals(){
   goalSelector.addGoal(0,new FloatGoal(this));
   goalSelector.addGoal(1,new Stay(this));
   goalSelector.addGoal(1,new PanicGoal(this,1.7));
   goalSelector.addGoal(2,new BreedGoal(this,1.0));
   goalSelector.addGoal(3,new TemptGoal(this,1.2,Ingredient.of(Items.SPIDER_EYE,Items.SLIME_BALL),false));
   goalSelector.addGoal(4,new AvoidEntityGoal<>(this,Player.class,4F,1.2,1.6,p->!isTame()&&!p.isShiftKeyDown()&&!p.isSpectator()));   // wild ones: sneak to get close
   goalSelector.addGoal(4,new FollowOwnerGoal(this,1.25,5F,2F));   // adopted ones hop after you
   goalSelector.addGoal(5,new FollowParentGoal(this,1.1));
   var stroll=new WaterAvoidingRandomStrollGoal(this,1.0,.02F);stroll.setInterval(40);goalSelector.addGoal(6,stroll);
   goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,4F));
   goalSelector.addGoal(8,new RandomLookAroundGoal(this));
  }
  @Override public boolean isFood(ItemStack s){return s.is(Items.SPIDER_EYE)||s.is(Items.SLIME_BALL);}
  @Override public AgeableMob getBreedOffspring(ServerLevel l,AgeableMob other){var f=ENTITY.get().create(l);if(f!=null)f.setVariant(random.nextBoolean()?variant():other instanceof TinyFrog o?o.variant():variant());return f;}
  @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor l,DifficultyInstance d,MobSpawnType why,SpawnGroupData data){
   var biome=l.getBiome(blockPosition());
   int v=biome.is(BiomeTags.IS_JUNGLE)?(random.nextInt(3)==0?0:1+random.nextInt(2)):random.nextInt(6)==0?1+random.nextInt(2):0;
   setVariant(v);return super.finalizeSpawn(l,d,why,data);
  }
  /** Adopted frog told to stay: it stops where it is (works even when the owner is offline). */
  static final class Stay extends Goal {
   final TinyFrog f;Stay(TinyFrog f){this.f=f;setFlags(java.util.EnumSet.of(Flag.JUMP,Flag.MOVE));}
   @Override public boolean canUse(){return f.isTame()&&f.isOrderedToSit()&&!f.isInWater()&&!f.isPassenger();}
   @Override public void start(){f.getNavigation().stop();f.setInSittingPose(true);}
   @Override public void stop(){f.setInSittingPose(false);}
  }
  /** On a head it faces where the player looks and does nothing else. */
  /** The server tells other players about passengers but not the player carrying them: send it to the carrier too (a few ticks
   *  after mounting, so the frog itself is already known on that client). */
  private int headSync;
  @Override public boolean startRiding(Entity v,boolean force){boolean ok=super.startRiding(v,force);if(ok&&v instanceof Player)headSync=3;return ok;}
  @Override public void removeVehicle(){var v=getVehicle();super.removeVehicle();if(v instanceof net.minecraft.server.level.ServerPlayer sp)sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(sp));}
  @Override public void rideTick(){super.rideTick();
   if(headSync>0&&--headSync==0&&getVehicle() instanceof net.minecraft.server.level.ServerPlayer sp)sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(sp));if(getVehicle() instanceof Player p){setYRot(p.getYHeadRot());yBodyRot=getYRot();yHeadRot=getYRot();setXRot(0);fallDistance=0;}}
  @Override public boolean isInvulnerableTo(DamageSource s){return onHead()&&!s.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)||super.isInvulnerableTo(s);}
  /** Moves in little hops instead of walking. */
  @Override public void aiStep(){
   super.aiStep();
   if(level().isClientSide||isPassenger()||isInSittingPose())return;
   if(hopDelay>0)hopDelay--;
   if(onGround()&&hopDelay==0&&!getNavigation().isDone()){
    var next=getNavigation().getPath()==null?null:getNavigation().getPath().getNextEntityPos(this);
    Vec3 dir=next==null?getLookAngle():next.subtract(position());dir=new Vec3(dir.x,0,dir.z);
    if(dir.lengthSqr()>1e-3){dir=dir.normalize();setYRot((float)(Mth.atan2(dir.z,dir.x)*180/Math.PI)-90);yBodyRot=getYRot();
     boolean panic=getLastHurtByMob()!=null&&tickCount-getLastHurtByMobTimestamp()<100;
     setDeltaMovement(dir.scale(panic?.34:.24).add(0,panic?.42:.34,0));hasImpulse=true;
     hopDelay=panic?4:8+random.nextInt(12);}
   }
   // idle: a little hop in a random direction now and then
   if(onGround()&&hopDelay==0&&getNavigation().isDone()&&random.nextInt(70)==0){
    float yaw=getYRot()+(random.nextFloat()-.5F)*120F;setYRot(yaw);yBodyRot=yaw;double r=Math.toRadians(yaw);
    setDeltaMovement(-Math.sin(r)*.2,.3,Math.cos(r)*.2);hasImpulse=true;hopDelay=10;}
   // catch a fly now and then: the tongue flicks out (client sees it through an entity event)
   if(random.nextInt(260)==0&&onGround()){level().broadcastEntityEvent(this,(byte)71);if(random.nextInt(3)==0)heal(1);}
  }
  @Override public void playAmbientSound(){super.playAmbientSound();level().broadcastEntityEvent(this,(byte)70);}
  @Override public int getAmbientSoundInterval(){return 140;}
  @Override protected SoundEvent getAmbientSound(){return SoundEvents.FROG_AMBIENT;}
  @Override protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.FROG_HURT;}
  @Override protected SoundEvent getDeathSound(){return SoundEvents.FROG_DEATH;}
  @Override protected void playStepSound(BlockPos p,net.minecraft.world.level.block.state.BlockState s){playSound(SoundEvents.FROG_STEP,.08F,1.8F);}
  @Override public float getVoicePitch(){return 1.7F+random.nextFloat()*.3F;}
  @Override protected float getSoundVolume(){return .45F;}
  @Override public boolean causeFallDamage(float d,float m,DamageSource s){return false;}
  @Override public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType t){return false;}
  @Override public int getMaxFallDistance(){return 12;}
  /** Wild: a slime ball or spider eye may adopt it (1 in 3). Adopted: click = stay / follow, sneak + empty hand = onto your head.
   *  Sneak + empty hand on a wild frog (or one that isn't yours) picks it up as an item. */
  @Override public InteractionResult mobInteract(Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);boolean client=level().isClientSide;
   if(!isTame()&&isFood(s)&&!isBaby()){
    if(!client){if(!p.getAbilities().instabuild)s.shrink(1);
     if(random.nextInt(3)==0&&!net.neoforged.neoforge.event.EventHooks.onAnimalTame(this,p)){tame(p);getNavigation().stop();setOrderedToSit(false);level().broadcastEntityEvent(this,(byte)7);
      p.displayClientMessage(Component.literal("§a🐸 Você adotou o sapinho! §7Shift + clique (mão vazia): na cabeça • clique: fica/segue"),true);}
     else level().broadcastEntityEvent(this,(byte)6);}
    return InteractionResult.sidedSuccess(client);
   }
   if(isTame()&&isOwnedBy(p)&&s.isEmpty()&&hand==InteractionHand.MAIN_HAND){
    if(p.isShiftKeyDown()){   // onto your head
     if(!client){if(Frogs.onHead(p)!=null){p.displayClientMessage(Component.literal("§7Já tem um sapinho na sua cabeça."),true);return InteractionResult.CONSUME;}
      setOrderedToSit(false);setInSittingPose(false);getNavigation().stop();startRiding(p,true);
      level().playSound(null,blockPosition(),SoundEvents.FROG_LONG_JUMP,SoundSource.NEUTRAL,.6F,1.9F);
      p.displayClientMessage(Component.literal("§a🐸 Sapinho na cabeça! §7Shift + clique direito num bloco (mão vazia) para descer"),true);}
     return InteractionResult.sidedSuccess(client);
    }
    if(!client){setOrderedToSit(!isOrderedToSit());jumping=false;getNavigation().stop();p.displayClientMessage(Component.literal(isOrderedToSit()?"§7Sapinho: fica aqui":"§7Sapinho: me segue"),true);}
    return InteractionResult.sidedSuccess(client);
   }
   if(s.isEmpty()&&p.isShiftKeyDown()&&!isBaby()&&(!isTame()||isOwnedBy(p))){
    if(!client){var item=new ItemStack(POCKET.get());var tag=new CompoundTag();tag.putInt("Variant",variant());if(getOwnerUUID()!=null)tag.putUUID("Owner",getOwnerUUID());if(hasCustomName())tag.putString("Name",getCustomName().getString());
     item.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));if(hasCustomName())item.set(DataComponents.CUSTOM_NAME,getCustomName());
     p.setItemInHand(hand,item);level().playSound(null,blockPosition(),SoundEvents.FROG_AMBIENT,SoundSource.NEUTRAL,.6F,2F);discard();}
    return InteractionResult.sidedSuccess(level().isClientSide);
   }
   return super.mobInteract(p,hand);
  }
  @Override public void handleEntityEvent(byte b){if(b==70)croakAt=tickCount;else if(b==71)tongueAt=tickCount;else super.handleEntityEvent(b);}
  @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Variant",variant());}
  @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);setVariant(t.getInt("Variant"));
   var mh=getAttribute(Attributes.MAX_HEALTH);if(mh!=null&&mh.getBaseValue()>1){mh.setBaseValue(1);setHealth(Math.min(getHealth(),1));}}   // 0.65.4: half a heart
  @Override public ItemStack getPickResult(){return new ItemStack(EGG.get());}
 }

 public static final class PocketFrog extends Item {
  public PocketFrog(Properties p){super(p);}
  static int variant(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("Variant");}
  @Override public Component getName(ItemStack s){return Component.literal("Sapo pequenino "+NAMES[Mth.clamp(variant(s),0,2)]);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<Component> t,TooltipFlag f){if(s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().hasUUID("Owner"))t.add(Component.literal("§a♥ Adotado"));t.add(Component.literal("§7Clique num bloco para soltar"));}
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();if(l.isClientSide)return InteractionResult.SUCCESS;
   var f=ENTITY.get().create(l);if(f==null)return InteractionResult.FAIL;
   var pos=c.getClickedPos().relative(c.getClickedFace());f.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,c.getRotation()+180,0);f.setVariant(variant(c.getItemInHand()));
   var pt=c.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(pt.hasUUID("Owner")){f.setTame(true,false);f.setOwnerUUID(pt.getUUID("Owner"));}
   if(c.getItemInHand().has(DataComponents.CUSTOM_NAME))f.setCustomName(c.getItemInHand().get(DataComponents.CUSTOM_NAME));
   f.setPersistenceRequired();l.addFreshEntity(f);l.playSound(null,pos,SoundEvents.FROG_AMBIENT,SoundSource.NEUTRAL,.6F,1.8F);
   c.getItemInHand().shrink(1);return InteractionResult.CONSUME;
  }
 }
}
