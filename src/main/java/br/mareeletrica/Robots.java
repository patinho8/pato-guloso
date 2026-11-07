package br.mareeletrica;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Robô Catador: a little three-wheeled cleaning robot of your own. It follows you, picks up the items lying around into its
 *  27-slot belly and presses common junk (dirt, cobblestone, gravel, rotten flesh...) into compacted trash cubes (a furnace fuel). */
public final class Robots {
 public static final Supplier<EntityType<Catador>> ENTITY=MareEletrica.ACTORS.register("catador_robot",()->
  EntityType.Builder.<Catador>of(Catador::new,MobCategory.MISC).sized(.8F,.9F).eyeHeight(.7F).clientTrackingRange(10).build(MareEletrica.ID+":catador_robot"));
 public static final DeferredItem<RobotItem> ITEM=MareEletrica.ITEMS.register("catador_robot",()->new RobotItem(new Item.Properties().stacksTo(1)));
 public static final DeferredBlock<Block> TRASH=MareEletrica.BLOCKS.register("trash_cube",()->new Block(BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.WOOL)));
 public static final DeferredItem<BlockItem> TRASH_ITEM=MareEletrica.ITEMS.register("trash_cube",()->new BlockItem(TRASH.get(),new Item.Properties()));
 /** What the robot presses into cubes, 9 at a time. */
 public static final Set<Item> JUNK=Set.of(Items.DIRT,Items.COARSE_DIRT,Items.COBBLESTONE,Items.COBBLED_DEEPSLATE,Items.GRAVEL,Items.NETHERRACK,Items.ROTTEN_FLESH,
  Items.TUFF,Items.DIORITE,Items.ANDESITE,Items.GRANITE,Items.WHEAT_SEEDS,Items.POISONOUS_POTATO,Items.DEAD_BUSH);
 private Robots(){}
 public static void register(IEventBus bus){bus.addListener((EntityAttributeCreationEvent e)->e.put(ENTITY.get(),Catador.attributes().build()));}

 public static final class Catador extends TamableAnimal {
  private static final EntityDataAccessor<Integer> SCOOP=SynchedEntityData.defineId(Catador.class,EntityDataSerializers.INT);
  private static final EntityDataAccessor<Byte> STATE=SynchedEntityData.defineId(Catador.class,EntityDataSerializers.BYTE);   // 1 = lamp on, 2 = belly full, 4 = pressing
  public final SimpleContainer belly=new SimpleContainer(27){@Override public boolean stillValid(Player p){return isAlive()&&p.distanceToSqr(Catador.this)<64;}};
  /** Client: wheel angle (degrees) driven by how far it rolled. */
  public float wheel,wheelO;private int fullWarned,pressAt=-100;
  public Catador(EntityType<? extends Catador> t,Level l){super(t,l);setPersistenceRequired();}
  public static AttributeSupplier.Builder attributes(){return net.minecraft.world.entity.animal.Animal.createMobAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.ARMOR,4).add(Attributes.STEP_HEIGHT,1.0);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(SCOOP,-100);b.define(STATE,(byte)0);}
  public int scoopAt(){return entityData.get(SCOOP);}
  public boolean lampOn(){return (entityData.get(STATE)&1)!=0;}
  public boolean full(){return (entityData.get(STATE)&2)!=0;}
  public boolean pressing(){return (entityData.get(STATE)&4)!=0;}
  @Override protected void registerGoals(){
   goalSelector.addGoal(0,new FloatGoal(this));
   goalSelector.addGoal(1,new Stay(this));
   goalSelector.addGoal(2,new Collect(this));
   goalSelector.addGoal(3,new FollowOwnerGoal(this,1.2,6F,2.5F));
   goalSelector.addGoal(4,new WaterAvoidingRandomStrollGoal(this,.7,.002F));
   goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,6F));
   goalSelector.addGoal(6,new RandomLookAroundGoal(this));
  }
  @Override public boolean isFood(ItemStack s){return false;}
  @Override public boolean canMate(net.minecraft.world.entity.animal.Animal a){return false;}
  @Override public AgeableMob getBreedOffspring(ServerLevel l,AgeableMob other){return null;}
  @Override public boolean removeWhenFarAway(double d){return false;}
  @Override public boolean causeFallDamage(float d,float m,DamageSource s){return false;}
  @Override protected SoundEvent getAmbientSound(){return null;}
  @Override protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.IRON_GOLEM_HURT;}
  @Override protected SoundEvent getDeathSound(){return SoundEvents.IRON_GOLEM_DEATH;}
  @Override protected void playStepSound(net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){playSound(SoundEvents.MINECART_INSIDE,.02F,2F);}
  @Override public float getVoicePitch(){return 1.6F;}
  void beep(float pitch){level().playSound(null,blockPosition(),SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.NEUTRAL,.5F,pitch);}
  boolean hasRoom(ItemStack s){return belly.canAddItem(s);}

  /** Told to stay: stops where it is (also works while the owner is offline). */
  static final class Stay extends Goal {
   final Catador r;Stay(Catador r){this.r=r;setFlags(EnumSet.of(Flag.JUMP,Flag.MOVE));}
   @Override public boolean canUse(){return r.isTame()&&r.isOrderedToSit();}
   @Override public void start(){r.getNavigation().stop();r.setInSittingPose(true);}
   @Override public void stop(){r.setInSittingPose(false);}
  }
  /** Rolls to items lying nearby (not too far from the owner) and scoops them up. */
  static final class Collect extends Goal {
   final Catador r;ItemEntity target;int look,stuck;
   Collect(Catador r){this.r=r;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
   ItemEntity find(){
    if(r.isOrderedToSit())return null;var owner=r.getOwner();
    ItemEntity best=null;double bd=1e9;
    for(var it:r.level().getEntitiesOfClass(ItemEntity.class,r.getBoundingBox().inflate(10,4,10),i->i.isAlive()&&!i.hasPickUpDelay()&&r.hasRoom(i.getItem()))){
     if(owner!=null&&it.distanceToSqr(owner)>18*18)continue;
     double d=it.distanceToSqr(r);if(d<bd){bd=d;best=it;}
    }
    return best;
   }
   @Override public boolean canUse(){if(--look>0)return false;look=10;target=find();return target!=null;}
   @Override public boolean canContinueToUse(){return target!=null&&target.isAlive()&&stuck<120&&!r.isOrderedToSit()&&r.hasRoom(target.getItem());}
   @Override public void start(){stuck=0;r.getNavigation().moveTo(target,1.25);}
   @Override public void stop(){target=null;r.getNavigation().stop();}
   @Override public void tick(){
    stuck++;r.getLookControl().setLookAt(target,30,30);
    if(stuck%10==0)r.getNavigation().moveTo(target,1.25);
    if(r.distanceToSqr(target)<1.7*1.7){
     var s=target.getItem().copy();var rest=r.belly.addItem(s);
     if(rest.isEmpty())target.discard();else target.setItem(rest);
     r.entityData.set(SCOOP,r.tickCount);r.beep(1.6F+r.random.nextFloat()*.4F);
     r.level().playSound(null,r.blockPosition(),SoundEvents.ITEM_PICKUP,SoundSource.NEUTRAL,.4F,1.4F);
     target=null;
    }
   }
  }

  @Override public void aiStep(){
   super.aiStep();
   if(level().isClientSide){wheelO=wheel;double dx=getX()-xo,dz=getZ()-zo;double d=Math.sqrt(dx*dx+dz*dz);
    var look=net.minecraft.world.phys.Vec3.directionFromRotation(0,yBodyRot);wheel+=(float)((dx*look.x+dz*look.z)>=0?d:-d)*360F/(float)(Math.PI*4/16*.8);
    if(pressing()&&random.nextInt(2)==0)level().addParticle(ParticleTypes.SMOKE,getX(),getY()+.4,getZ(),0,.03,0);return;}
   if(tickCount%20==0){
    byte st=0;if(level().isNight()||level().getRawBrightness(blockPosition(),0)<7)st|=1;
    boolean isFull=true;for(int i=0;i<belly.getContainerSize();i++)if(belly.getItem(i).isEmpty()){isFull=false;break;}
    if(isFull)st|=2;if(tickCount-pressAt<30)st|=4;entityData.set(STATE,st);
    if(isFull&&getOwner() instanceof Player o&&tickCount-fullWarned>1200){fullWarned=tickCount;o.displayClientMessage(Component.literal("§e🤖 O Robô Catador está cheio! Clique nele para esvaziar."),true);beep(.7F);}
   }
   if(tickCount%40==0)press();
   if(getHealth()<getMaxHealth()&&tickCount%200==0)heal(1);
  }
  /** Presses 9 of the same junk into one compacted cube. */
  void press(){
   for(var junk:JUNK){
    int n=belly.countItem(junk);if(n<9)continue;
    var cube=new ItemStack(TRASH_ITEM.get());if(!belly.canAddItem(cube)&&n>9)continue;
    int left=9;for(int i=0;i<belly.getContainerSize()&&left>0;i++){var s=belly.getItem(i);if(s.is(junk)){int k=Math.min(left,s.getCount());s.shrink(k);left-=k;}}
    var rest=belly.addItem(cube);if(!rest.isEmpty())spawnAtLocation(rest);
    pressAt=tickCount;entityData.set(STATE,(byte)(entityData.get(STATE)|4));
    level().playSound(null,blockPosition(),SoundEvents.PISTON_EXTEND,SoundSource.NEUTRAL,.6F,.8F);
    level().playSound(null,blockPosition(),SoundEvents.ANVIL_LAND,SoundSource.NEUTRAL,.25F,1.6F);
    ((ServerLevel)level()).sendParticles(ParticleTypes.POOF,getX(),getY()+.5,getZ(),6,.2,.1,.2,.02);
    return;
   }
  }
  /** Owner: click = open its belly • sneak + click = stay / follow • iron ingot = repair. */
  @Override public InteractionResult mobInteract(Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);boolean client=level().isClientSide;
   if(!isOwnedBy(p)){if(!client)p.displayClientMessage(Component.literal("§7Este robô tem dono."),true);return InteractionResult.sidedSuccess(client);}
   if(s.is(Items.IRON_INGOT)&&getHealth()<getMaxHealth()){if(!client){heal(10);if(!p.getAbilities().instabuild)s.shrink(1);level().playSound(null,blockPosition(),SoundEvents.IRON_GOLEM_REPAIR,SoundSource.NEUTRAL,1F,1.3F);}return InteractionResult.sidedSuccess(client);}
   if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;
   if(p.isShiftKeyDown()){
    if(!client){setOrderedToSit(!isOrderedToSit());getNavigation().stop();beep(isOrderedToSit()?.8F:1.4F);
     p.displayClientMessage(Component.literal(isOrderedToSit()?"§7🤖 Robô Catador: aguardando aqui":"§7🤖 Robô Catador: seguindo e catando"),true);}
    return InteractionResult.sidedSuccess(client);
   }
   if(!client){beep(1.2F);p.openMenu(new SimpleMenuProvider((id,inv,pl)->ChestMenu.threeRows(id,inv,belly),getDisplayName()));}
   return InteractionResult.sidedSuccess(client);
  }
  /** The owner punching it while sneaking packs it back into an item (it keeps what it carries). */
  @Override public boolean hurt(DamageSource src,float amount){
   if(!level().isClientSide&&src.getEntity() instanceof Player p&&isOwnedBy(p)){
    if(p.isShiftKeyDown()){var it=RobotItem.pack(this);if(!p.getInventory().add(it))spawnAtLocation(it);level().playSound(null,blockPosition(),SoundEvents.ARMOR_EQUIP_IRON.value(),SoundSource.NEUTRAL,1F,1.2F);discard();return true;}
    return false;   // the owner can't break it by accident
   }
   return super.hurt(src,amount);
  }
  @Override protected void dropCustomDeathLoot(ServerLevel l,DamageSource s,boolean hit){super.dropCustomDeathLoot(l,s,hit);
   for(int i=0;i<belly.getContainerSize();i++){var it=belly.removeItemNoUpdate(i);if(!it.isEmpty())spawnAtLocation(it);}spawnAtLocation(new ItemStack(Items.IRON_INGOT,3));}
  @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.put("Belly",belly.createTag(registryAccess()));}
  @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);if(t.contains("Belly"))belly.fromTag(t.getList("Belly",10),registryAccess());}
  @Override public ItemStack getPickResult(){return new ItemStack(ITEM.get());}
 }

 /** The robot as an item: right-click a block to put it down (it becomes yours). Keeps its belly and health when packed. */
 public static final class RobotItem extends Item {
  public RobotItem(Properties p){super(p);}
  static ItemStack pack(Catador r){
   var s=new ItemStack(ITEM.get());var t=new CompoundTag();t.put("Belly",r.belly.createTag(r.registryAccess()));t.putFloat("Health",r.getHealth());
   s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));if(r.hasCustomName())s.set(DataComponents.CUSTOM_NAME,r.getCustomName());return s;
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Te segue e cata os itens do chão (27 espaços)"));
   t.add(Component.literal("§7Prensa terra, pedregulho, cascalho, carne podre... em cubos de lixo"));
   t.add(Component.literal("§8Clique: abrir • Shift+clique: espera/segue • barra de ferro: conserta"));
   t.add(Component.literal("§8Shift + soco: guarda o robô (com tudo dentro)"));
   int n=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getList("Belly",10).size();if(n>0)t.add(Component.literal("§eCarregando "+n+" pilhas de itens"));
  }
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();if(l.isClientSide)return InteractionResult.SUCCESS;
   var r=ENTITY.get().create(l);if(r==null)return InteractionResult.FAIL;
   var pos=c.getClickedPos().relative(c.getClickedFace());r.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,c.getRotation()+180,0);
   r.yBodyRot=r.getYRot();r.yHeadRot=r.getYRot();
   var t=c.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
   if(t.contains("Belly"))r.belly.fromTag(t.getList("Belly",10),l.registryAccess());
   if(c.getPlayer()!=null)r.tame(c.getPlayer());
   if(t.contains("Health"))r.setHealth(Math.max(1,t.getFloat("Health")));
   if(c.getItemInHand().has(DataComponents.CUSTOM_NAME))r.setCustomName(c.getItemInHand().get(DataComponents.CUSTOM_NAME));
   l.addFreshEntity(r);r.beep(1.0F);r.beep(1.5F);
   if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("§a🤖 Robô Catador ligado! §7Ele te segue e cata itens do chão."),true);
   c.getItemInHand().shrink(1);return InteractionResult.CONSUME;
  }
 }
}
