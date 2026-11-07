package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Three more war toys, built on what the mod already has:
 *  - Mina Terrestre (like the C4): hidden pressure mine that goes off under people, mobs and vehicles; hits tanks hard.
 *  - Lança-Mísseis Portátil (a SAM you carry): hold right-click on an aircraft or tank to lock on, let go to fire a guided missile.
 *  - Sinalizador de Ataque Aéreo (flare + jet bombs): throw the red smoke and a line of bombs falls on it a few seconds later. */
public final class WarPlus {
 // ------------------------------------------------------------ landmine
 public static final BooleanProperty ARMED=BooleanProperty.create("armed"),TRIGGERED=BooleanProperty.create("triggered");
 public static final DeferredBlock<MineBlock> MINE=MareEletrica.BLOCKS.register("landmine",()->new MineBlock(BlockBehaviour.Properties.of().strength(.8F).sound(SoundType.METAL).noOcclusion().noCollission()));
 public static final DeferredItem<BlockItem> MINE_ITEM=MareEletrica.ITEMS.register("landmine",()->new BlockItem(MINE.get(),new Item.Properties().stacksTo(16)){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Arma 3 s depois de colocada. Explode quando alguém ou um veículo pisa."));t.add(Component.literal("§7Muito forte contra tanques. Agache e clique para desarmar."));}
 });
 /** A bigger, prettier explosion on top of the vanilla one: flash, fireball, dirt thrown up, a smoke column and sparks. */
 public static void blast(ServerLevel l,Vec3 c,BlockState ground,float size){
  l.sendParticles(ParticleTypes.FLASH,c.x,c.y+.6,c.z,1,0,0,0,0);
  l.sendParticles(ParticleTypes.EXPLOSION_EMITTER,c.x,c.y+.5,c.z,1,0,0,0,0);
  l.sendParticles(ParticleTypes.FLAME,c.x,c.y+.6,c.z,(int)(60*size),.4*size,.5*size,.4*size,.18*size);
  l.sendParticles(ParticleTypes.LAVA,c.x,c.y+.4,c.z,(int)(14*size),.5*size,.2,.5*size,0);
  l.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,c.x,c.y+.8,c.z,(int)(30*size),.7*size,.6*size,.7*size,.04);
  l.sendParticles(ParticleTypes.LARGE_SMOKE,c.x,c.y+1.5,c.z,(int)(40*size),1.2*size,1.4*size,1.2*size,.06);
  l.sendParticles(ParticleTypes.FIREWORK,c.x,c.y+.6,c.z,(int)(25*size),.3,.3,.3,.35*size);
  if(ground!=null&&!ground.isAir())l.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,ground),c.x,c.y+.4,c.z,(int)(80*size),.6*size,.9*size,.6*size,.4);
  l.playSound(null,BlockPos.containing(c),MareEletrica.FRAG_BLAST.get(),SoundSource.BLOCKS,1.2F*size,.8F);
 }
 // ------------------------------------------------------------ portable missile launcher
 public static final DeferredItem<LauncherItem> LAUNCHER=MareEletrica.ITEMS.register("missile_launcher_portable",()->new LauncherItem(new Item.Properties().stacksTo(1).durability(64)));
 // ------------------------------------------------------------ airstrike
 public static final DeferredItem<StrikeItem> STRIKE=MareEletrica.ITEMS.register("airstrike_flare",()->new StrikeItem(new Item.Properties().stacksTo(4)));
 public static final Supplier<EntityType<Marker>> MARKER=MareEletrica.ACTORS.register("airstrike_marker",()->EntityType.Builder.<Marker>of(Marker::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(16).updateInterval(2).build(MareEletrica.ID+":airstrike_marker"));
 public static final Supplier<EntityType<AirBomb>> BOMB=MareEletrica.ACTORS.register("airstrike_bomb",()->EntityType.Builder.<AirBomb>of(AirBomb::new,MobCategory.MISC).sized(.5F,.5F).clientTrackingRange(16).updateInterval(1).build(MareEletrica.ID+":airstrike_bomb"));
 private WarPlus(){}
 public static void register(){}

 public static final class MineBlock extends Block {
  public static final MapCodec<MineBlock> CODEC=simpleCodec(MineBlock::new);
  static final VoxelShape SHAPE=Block.box(3,0,3,13,1.5,13);
  public MineBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(ARMED,false).setValue(TRIGGERED,false));}
  @Override protected MapCodec<? extends Block> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ARMED,TRIGGERED);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
  @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
  @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){return l.getBlockState(p.below()).isFaceSturdy(l,p.below(),net.minecraft.core.Direction.UP);}
  @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(!s.is(old.getBlock()))l.scheduleTick(p,this,s.getValue(ARMED)?4:60);}
  @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,net.minecraft.util.RandomSource r){
   if(!s.getValue(ARMED)){l.setBlock(p,s.setValue(ARMED,true),3);l.playSound(null,p,SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.BLOCKS,.4F,2F);l.scheduleTick(p,this,4);return;}
   if(s.getValue(TRIGGERED)){boom(l,p);return;}
   // armed: feel for anyone (or any vehicle) standing on it
   if(!l.getEntitiesOfClass(Entity.class,new AABB(p).inflate(.2,.6,.2).move(0,.3,0),WarPlus.MineBlock::victim).isEmpty())trigger(s,l,p);
   else l.scheduleTick(p,this,4);
  }
  static boolean victim(Entity e){return (e instanceof LivingEntity||e instanceof Vehicles.Base)&&!(e instanceof Player pl&&(pl.isCreative()||pl.isSpectator()))&&e.isAlive();}
  static void trigger(BlockState s,Level l,BlockPos p){
   l.setBlock(p,s.setValue(TRIGGERED,true),2);
   l.playSound(null,p,SoundEvents.TRIPWIRE_CLICK_ON,SoundSource.BLOCKS,1.5F,1.6F);
   l.scheduleTick(p,MINE.get(),5);   // "click" ... BOOM
  }
  @Override protected void entityInside(BlockState s,Level l,BlockPos p,Entity e){
   if(l.isClientSide||!s.getValue(ARMED)||s.getValue(TRIGGERED)||!victim(e))return;
   trigger(s,l,p);
  }
  @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player pl,net.minecraft.world.phys.BlockHitResult hit){
   if(!pl.isShiftKeyDown())return net.minecraft.world.InteractionResult.PASS;
   if(!l.isClientSide){l.removeBlock(p,false);Block.popResource(l,p,new ItemStack(MINE_ITEM.get()));pl.displayClientMessage(Component.literal("§aMina desarmada"),true);
    l.playSound(null,p,SoundEvents.TRIPWIRE_DETACH,SoundSource.BLOCKS,1F,1F);}
   return net.minecraft.world.InteractionResult.sidedSuccess(l.isClientSide);
  }
  static void boom(ServerLevel l,BlockPos p){
   var ground=l.getBlockState(p.below());
   l.removeBlock(p,false);Vec3 c=Vec3.atBottomCenterOf(p);
   l.explode(null,c.x,c.y+.2,c.z,3.4F,Level.ExplosionInteraction.TNT);
   // anti-tank charge: a tank on top is destroyed, other vehicles are wrecked
   for(var v:l.getEntitiesOfClass(Vehicles.Base.class,new AABB(p).inflate(3.5,2.5,3.5)))v.hurt(l.damageSources().explosion(null,null),v instanceof Vehicles.Tank?99999F:60F);
   blast(l,c,ground,1F);
  }
 }

 /** RPG / portable missile launcher held in both hands. Right click (hold) aims down the sights; keep a plane, helicopter or
  *  tank in the sights to lock on. Left click fires: a guided missile when locked, a straight rocket otherwise. */
 public static final class LauncherItem extends Item {
  public LauncherItem(Properties p){super(p);}
  public static final int LOCK=24;
  @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.NONE;}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
  @Override public boolean canAttackBlock(BlockState s,Level l,BlockPos p,Player pl){return false;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Botão direito: mirar  •  Clique: disparar"));t.add(Component.literal("§7Mirando num avião, helicóptero ou tanque ele TRAVA e o míssil persegue."));
   t.add(Component.literal("§7Destrói tanques e helicópteros. Munição: míssil antiaéreo"));}
  static boolean target(Entity e){return e instanceof Jets.JetEntity||e instanceof Vehicles.Base||e instanceof Drones.DroneEntity;}
  /** Vehicle closest to the centre of the sights (within 5 degrees and 180 blocks). Works on both sides. */
  public static Entity aimed(Player p){
   Vec3 eye=p.getEyePosition(),look=p.getLookAngle();Entity best=null;double bestAng=5;
   for(var e:p.level().getEntitiesOfClass(Entity.class,p.getBoundingBox().inflate(180),WarPlus.LauncherItem::target)){
    if(e.getControllingPassenger()==p||e.hasPassenger(p))continue;
    Vec3 to=e.position().add(0,e.getBbHeight()/2,0).subtract(eye);double d=to.length();if(d>180||d<4)continue;
    double ang=Math.toDegrees(Math.acos(Mth.clamp(to.normalize().dot(look),-1,1)));if(ang<bestAng){bestAng=ang;best=e;}}
   return best;
  }
  public static boolean aiming(Player p){return p!=null&&p.isUsingItem()&&p.getUseItem().getItem() instanceof LauncherItem;}
  static boolean hasAmmo(Player p){return p.isCreative()||p.getInventory().contains(new ItemStack(Missiles.MISSILE.get()));}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);if(h!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(s);
   p.startUsingItem(h);return InteractionResultHolder.consume(s);
  }
  /** Server, while aiming: lock-on progress kept in the item (the client reads it for the sight). */
  @Override public void onUseTick(Level l,LivingEntity who,ItemStack s,int left){
   if(l.isClientSide||!(who instanceof Player p))return;
   var t=aimed(p);
   var tag=s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
   int id=tag.contains("Target")?tag.getInt("Target"):-1,lock=tag.getInt("Lock");
   if(t==null){lock=0;id=-1;}else if(t.getId()!=id){id=t.getId();lock=0;}else lock=Math.min(LOCK,lock+1);
   if(lock>0&&lock<LOCK&&lock%5==0)l.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.PLAYERS,.5F,1.2F);
   if(lock==LOCK&&p.tickCount%4==0)l.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.PLAYERS,.5F,2F);
   if(id!=tag.getInt("Target")||lock!=tag.getInt("Lock")){tag.putInt("Target",id);tag.putInt("Lock",lock);s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));}
  }
  @Override public void releaseUsing(ItemStack s,Level l,LivingEntity who,int left){
   if(!l.isClientSide){var tag=s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();tag.putInt("Lock",0);tag.putInt("Target",-1);s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));}
  }
  public static int lock(ItemStack s){return s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getInt("Lock");}
  /** Server: the left click from the client. */
  public static void fire(net.minecraft.server.level.ServerPlayer p){
   var s=p.getMainHandItem();if(!(s.getItem() instanceof LauncherItem)||p.getCooldowns().isOnCooldown(s.getItem()))return;
   if(!Wounds.canFire(p))return;   // 0.65: too hurt for a launcher
   var l=p.level();
   if(!hasAmmo(p)){p.displayClientMessage(Component.literal("§cSem míssil antiaéreo no inventário"),true);l.playSound(null,p.blockPosition(),SoundEvents.DISPENSER_FAIL,SoundSource.PLAYERS,1F,1.2F);p.getCooldowns().addCooldown(s.getItem(),10);return;}
   var tag=s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
   Entity t=aiming(p)&&tag.getInt("Lock")>=LOCK?l.getEntity(tag.getInt("Target")):null;
   if(!p.isCreative()){for(var st:p.getInventory().items)if(st.is(Missiles.MISSILE.get())){st.shrink(1);break;}s.hurtAndBreak(1,p,net.minecraft.world.entity.EquipmentSlot.MAINHAND);}
   Vec3 dir=p.getLookAngle();Vec3 start=p.getEyePosition().add(dir.scale(1.3)).add(p.getLookAngle().cross(new Vec3(0,1,0)).normalize().scale(-.25)).add(0,-.15,0);
   var m=new Rocket(ROCKET.get(),l);m.setPos(start.x,start.y,start.z);m.setDeltaMovement(dir.scale(1.6));m.setOwner(p);if(t!=null)m.target=t.getId();
   l.addFreshEntity(m);
   l.playSound(null,p.blockPosition(),MareEletrica.LAUNCHER_FIRE.get(),SoundSource.PLAYERS,1.5F,.9F);
   var sl=(ServerLevel)l;Vec3 back=p.getEyePosition().subtract(dir.scale(1.6));
   sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,back.x,back.y-.2,back.z,25,.4,.3,.4,.05);   // back blast
   sl.sendParticles(ParticleTypes.FLAME,back.x,back.y-.2,back.z,15,.2,.2,.2,.1);
   sl.sendParticles(ParticleTypes.CLOUD,start.x,start.y,start.z,8,.15,.15,.15,.04);
   p.getCooldowns().addCooldown(s.getItem(),45);
   if(p.getVehicle()==null){   // recoil: the back blast shoves the shooter backwards
    Vec3 push=new Vec3(-dir.x,0,-dir.z).normalize().scale(p.isCrouching()?.18:.42);
    p.push(push.x,p.onGround()?.12:.02,push.z);p.hurtMarked=true;}
   p.displayClientMessage(Component.literal(t!=null?"§6Míssil guiado disparado em "+Radar.name(t)+"!":"§7Foguete disparado"),true);
  }
 }
 /** The launcher's missile: big smoke trail, homing when locked, destroys tanks, helicopters and planes it hits. */
 public static final Supplier<EntityType<Rocket>> ROCKET=MareEletrica.ACTORS.register("rpg_missile",()->EntityType.Builder.<Rocket>of(Rocket::new,MobCategory.MISC).sized(.4F,.4F).clientTrackingRange(16).updateInterval(1).build(MareEletrica.ID+":rpg_missile"));
 public static final class Rocket extends net.minecraft.world.entity.projectile.Projectile {
  int target=-1;
  public Rocket(EntityType<? extends Rocket> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<256*256;}
  @Override public void tick(){
   super.tick();
   Vec3 v=getDeltaMovement();
   if(level().isClientSide){
    // thick white smoke trail with the motor's flame
    for(int i=0;i<3;i++){double f=i/3.0;double x=getX()-v.x*f,y=getY()-v.y*f,z=getZ()-v.z*f;
     level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,true,x,y,z,random.nextGaussian()*.01,.012,random.nextGaussian()*.01);
     level().addParticle(ParticleTypes.CLOUD,x,y,z,random.nextGaussian()*.02,random.nextGaussian()*.02,random.nextGaussian()*.02);}
    level().addParticle(ParticleTypes.FLAME,getX()-v.x*.35,getY()-v.y*.35,getZ()-v.z*.35,-v.x*.15,-v.y*.15,-v.z*.15);
    if(tickCount%2==0)level().addParticle(ParticleTypes.FIREWORK,getX()-v.x*.4,getY()-v.y*.4,getZ()-v.z*.4,-v.x*.05+random.nextGaussian()*.03,-v.y*.05,-v.z*.05+random.nextGaussian()*.03);
    return;
   }
   double speed=Math.min(3.4,v.length()+.16);
   var t=target>=0?level().getEntity(target):null;
   if(t!=null&&t.isAlive()){Vec3 to=t.position().add(0,t.getBbHeight()/2,0).subtract(position());Vec3 aim=to.normalize();Vec3 head=v.normalize();
    double ang=Math.toDegrees(Math.acos(Mth.clamp(head.dot(aim),-1,1)));if(ang>1e-3){double f=Math.min(1,5.5/ang);head=head.scale(1-f).add(aim.scale(f)).normalize();}v=head.scale(speed);
    if(to.length()<2.5){hitEntity(t);return;}}
   else v=v.normalize().scale(speed).add(0,-.012,0);
   setDeltaMovement(v);
   var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnMoveVector(this,this::canHitEntity);
   if(hit.getType()!=HitResult.Type.MISS){if(hit instanceof net.minecraft.world.phys.EntityHitResult eh)hitEntity(eh.getEntity());else detonate(null);return;}
   setPos(getX()+v.x,getY()+v.y,getZ()+v.z);
   if(tickCount>140||isInWater())detonate(null);
   if(tickCount%10==0)level().playSound(null,getX(),getY(),getZ(),SoundEvents.FIREWORK_ROCKET_LAUNCH,SoundSource.NEUTRAL,1.4F,.55F);
  }
  @Override protected boolean canHitEntity(Entity e){return e!=getOwner()&&!(getOwner()!=null&&getOwner().getVehicle()==e)&&(e instanceof LivingEntity||LauncherItem.target(e));}
  void hitEntity(Entity e){
   // anti-armour warhead: tanks, helicopters and jets are destroyed outright
   if(e instanceof Vehicles.Tank||e instanceof Vehicles.Helicopter||e instanceof Jets.JetEntity||e instanceof Drones.DroneEntity)e.hurt(damageSources().explosion(this,getOwner()),99999F);
   else e.hurt(damageSources().explosion(this,getOwner()),24F);
   detonate(e);
  }
  void detonate(Entity hit){
   if(!(level() instanceof ServerLevel sl))return;
   level().explode(this,getX(),getY(),getZ(),3F,Level.ExplosionInteraction.TNT);
   blast(sl,position(),level().getBlockState(blockPosition().below()),1.1F);
   discard();
  }
  @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag t){}
  @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag t){}
 }
 public record Fire() implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
  public static final Type<Fire> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"rpg_fire"));
  public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,Fire> CODEC=net.minecraft.network.codec.StreamCodec.unit(new Fire());
  @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 }
 @net.neoforged.fml.common.EventBusSubscriber(modid=MareEletrica.ID,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @net.neoforged.bus.api.SubscribeEvent public static void packets(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){
   e.registrar("20").playToServer(Fire.TYPE,Fire.CODEC,(p,c)->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer sp)LauncherItem.fire(sp);});
  }
 }

 public static final class StrikeItem extends Item {
  public StrikeItem(Properties p){super(p);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Jogue onde quer o bombardeio: um B-2 passa"));t.add(Component.literal("§7e solta 7 bombas em linha na fumaça vermelha. §cFique longe!"));}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);
   if(!l.isClientSide){var m=new Marker(MARKER.get(),l);m.setOwner(p);m.setPos(p.getX(),p.getEyeY()-.1,p.getZ());m.setItem(s.copyWithCount(1));m.shootFromRotation(p,p.getXRot(),p.getYRot(),0,1.1F,1F);m.runYaw=p.getYRot();l.addFreshEntity(m);
    l.playSound(null,p.blockPosition(),SoundEvents.SNOWBALL_THROW,SoundSource.PLAYERS,1F,.6F);p.getCooldowns().addCooldown(this,100);if(!p.isCreative())s.shrink(1);}
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
  }
 }
 /** The smoke canister: lands, smokes red, then calls the bombing run along the direction it was thrown. */
 public static final class Marker extends ThrowableItemProjectile {
  int landed=-1;public float runYaw;
  public Marker(EntityType<? extends Marker> t,Level l){super(t,l);}
  @Override protected Item getDefaultItem(){return STRIKE.get();}
  @Override protected void onHit(HitResult r){if(landed<0){landed=0;setDeltaMovement(Vec3.ZERO);setNoGravity(true);if(r.getType()==HitResult.Type.BLOCK)setPos(r.getLocation().add(0,.1,0));}}
  @Override public void tick(){
   if(landed<0){super.tick();return;}
   landed++;baseTick();
   if(level() instanceof ServerLevel sl){
    // big red smoke: a wide cloud at the bottom and a tall signal column
    sl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(1F,.08F,.06F),4F),getX(),getY()+.6,getZ(),10,1.1,.9,1.1,0);
    sl.sendParticles(new DustParticleOptions(new org.joml.Vector3f(.9F,.15F,.1F),4F),getX(),getY()+2.5+Math.min(8,landed*.06),getZ(),6,.8,1.8,.8,0);
    if(landed%2==0)sl.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,getX(),getY()+.2,getZ(),3,.3,.1,.3,.02);
    if(landed%4==0)sl.sendParticles(ParticleTypes.FLAME,getX(),getY()+.15,getZ(),2,.05,.05,.05,.01);
    if(landed==60)for(var p:sl.getEntitiesOfClass(Player.class,getBoundingBox().inflate(80)))p.displayClientMessage(Component.literal("§c§l✈ ATAQUE AÉREO CHEGANDO!"),true);
    // the bomber comes in from behind the thrower, 45 blocks up, and drops the stick as it crosses the smoke
    if(landed==70){var plane=new Plane(PLANE.get(),sl);Vec3 dir=Vec3.directionFromRotation(0,runYaw);Vec3 st=position().add(dir.scale(-64)).add(0,42,0);
     plane.setPos(st.x,st.y,st.z);plane.setDeltaMovement(dir.scale(1.3));plane.setYRot(runYaw);plane.mark=position();plane.owner=getOwner()==null?null:getOwner().getUUID();sl.addFreshEntity(plane);
     sl.playSound(null,blockPosition(),MareEletrica.JET_ENGINE.get(),SoundSource.HOSTILE,2.5F,.7F);}
    if(landed>260)discard();
   }
  }
 }
 public static final class AirBomb extends ThrowableItemProjectile {
  public AirBomb(EntityType<? extends AirBomb> t,Level l){super(t,l);}
  @Override protected Item getDefaultItem(){return Jets.BOMB.get();}
  @Override protected double getDefaultGravity(){return .06;}
  @Override public void tick(){super.tick();if(level().isClientSide){level().addParticle(ParticleTypes.SMOKE,getX(),getY()+.5,getZ(),0,.05,0);}else if(tickCount%8==0)level().playSound(null,blockPosition(),SoundEvents.FIREWORK_ROCKET_LAUNCH,SoundSource.HOSTILE,1.2F,.35F);}
  @Override protected void onHit(HitResult r){
   if(!(level() instanceof ServerLevel sl))return;
   level().explode(this,getX(),getY(),getZ(),3.8F,Level.ExplosionInteraction.TNT);
   for(var v:level().getEntitiesOfClass(Vehicles.Base.class,getBoundingBox().inflate(4)))v.hurt(damageSources().explosion(this,getOwner()),70F);
   blast(sl,position(),level().getBlockState(blockPosition().below()),1.4F);
   discard();
  }
 }
 /** Bomber that flies over an airstrike mark (no collision, just there to be seen and to drop the bombs). */
 public static final Supplier<EntityType<Plane>> PLANE=MareEletrica.ACTORS.register("strike_plane",()->EntityType.Builder.<Plane>of(Plane::new,MobCategory.MISC).sized(1F,1F).clientTrackingRange(20).updateInterval(1).build(MareEletrica.ID+":strike_plane"));
 public static final class Plane extends Entity {
  Vec3 mark;java.util.UUID owner;int dropped;
  public Plane(EntityType<? extends Plane> t,Level l){super(t,l);noPhysics=true;}
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<400*400;}
  @Override public void tick(){
   super.tick();
   var v=getDeltaMovement();setPos(getX()+v.x,getY()+v.y,getZ()+v.z);
   if(level().isClientSide){if(tickCount%2==0)level().addAlwaysVisibleParticle(ParticleTypes.CLOUD,true,getX()-v.x*3,getY(),getZ()-v.z*3,0,0,0);return;}
   if(tickCount>220){discard();return;}
   if(mark!=null&&dropped<7){
    Vec3 dir=v.normalize();double along=position().subtract(mark).dot(dir);
    // seven bombs, 3.5 blocks apart, centred on the smoke (they fall about 4 blocks forward of where they are released)
    double next=-22+dropped*4;
    if(along>=next){var b=new AirBomb(BOMB.get(),level());b.setPos(getX(),getY()-1.5,getZ());b.setDeltaMovement(dir.scale(.35).add(0,-.6,0));
     var o=owner==null?null:((ServerLevel)level()).getEntity(owner);if(o!=null)b.setOwner(o);level().addFreshEntity(b);dropped++;
     level().playSound(null,blockPosition(),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.HOSTILE,1.5F,.5F);}
   }
  }
  @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag t){}
  @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag t){discard();}
 }
}
