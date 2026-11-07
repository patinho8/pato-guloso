package br.mareeletrica;

import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.registries.DeferredItem;

/** A bouncy tennis ball: throw it, it bounces off walls and the floor, then stops and can be picked up again. Tamed wolves fetch it. */
@net.neoforged.fml.common.EventBusSubscriber(modid=MareEletrica.ID)
public final class TennisBall {
 /** wolf -> ball item on the ground it is running to (or null once it carries the ball back). */
 private static final java.util.Map<java.util.UUID,java.util.UUID> FETCH=new java.util.HashMap<>();
 private static final java.util.Set<java.util.UUID> CARRYING=new java.util.HashSet<>();
 public static final DeferredItem<BallItem> ITEM=MareEletrica.ITEMS.register("tennis_ball",()->new BallItem(new Item.Properties().stacksTo(16)));
 /** 0.65.2: balls in many colours (the classic one is the yellow-green). */
 public static final String[] COLORS={"pink","orange","blue","red","white","magenta","yellow","green"};
 public static final java.util.List<DeferredItem<BallItem>> COLORED=new java.util.ArrayList<>();
 static {for(String c:COLORS)COLORED.add(MareEletrica.ITEMS.register("tennis_ball_"+c,()->new BallItem(new Item.Properties().stacksTo(16))));}
 public static boolean isBall(ItemStack st){return st.getItem() instanceof BallItem;}
 public static final Supplier<EntityType<Ball>> ENTITY=MareEletrica.ACTORS.register("tennis_ball",()->
  EntityType.Builder.<Ball>of(Ball::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(8).updateInterval(1).build(MareEletrica.ID+":tennis_ball"));
 public static final DeferredItem<RacketItem> RACKET=MareEletrica.ITEMS.register("tennis_racket",()->new RacketItem(new Item.Properties().stacksTo(1).durability(400)));
 public static final net.neoforged.neoforge.registries.DeferredBlock<NetBlock> NET=MareEletrica.BLOCKS.register("tennis_net",()->new NetBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(.8F).sound(net.minecraft.world.level.block.SoundType.WOOL).noOcclusion()));
 public static final DeferredItem<net.minecraft.world.item.BlockItem> NET_ITEM=MareEletrica.ITEMS.register("tennis_net",()->new net.minecraft.world.item.BlockItem(NET.get(),new Item.Properties()));
 private TennisBall(){}
 /** Tamed wolves run to the ball, pick it up and bring it back to their owner. */
 @net.neoforged.bus.api.SubscribeEvent public static void wolfTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post e){
  if(!(e.getEntity() instanceof Wolf w)||w.level().isClientSide)return;var id=w.getUUID();
  if(CARRYING.contains(id)){
   var owner=w.getOwner();
   if(owner==null||!w.isAlive()||w.isOrderedToSit()){CARRYING.remove(id);w.spawnAtLocation(new ItemStack(ITEM.get()));return;}
   if(w.tickCount%10==0)w.getNavigation().moveTo(owner,1.4);
   if(w.distanceTo(owner)<2.2){CARRYING.remove(id);var ball=new ItemStack(ITEM.get());if(!(owner instanceof Player p&&p.getInventory().add(ball)))w.spawnAtLocation(ball);w.level().playSound(null,w.blockPosition(),SoundEvents.WOLF_AMBIENT,SoundSource.NEUTRAL,1F,1.3F);}
   else if(w.tickCount%20==0)((net.minecraft.server.level.ServerLevel)w.level()).sendParticles(new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM,new ItemStack(ITEM.get())),w.getX(),w.getY()+.6,w.getZ(),1,.1,.1,.1,0);
   return;
  }
  var target=FETCH.get(id);if(target==null)return;
  var ball=((net.minecraft.server.level.ServerLevel)w.level()).getEntity(target);
  if(!(ball instanceof net.minecraft.world.entity.item.ItemEntity it)||!it.isAlive()||w.isOrderedToSit()||w.distanceTo(ball)>40){FETCH.remove(id);return;}
  if(w.tickCount%10==0)w.getNavigation().moveTo(ball,1.4);
  if(w.distanceTo(ball)<1.4){int n=it.getItem().getCount();it.discard();FETCH.remove(id);CARRYING.add(id);if(n>1)w.spawnAtLocation(new ItemStack(ITEM.get(),n-1));w.level().playSound(null,w.blockPosition(),SoundEvents.ITEM_PICKUP,SoundSource.NEUTRAL,.5F,1.2F);}
 }
 public static void register(){}
 public record HitBall(int ball) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
  public static final Type<HitBall> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"tennis_hit"));
  public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,HitBall> CODEC=net.minecraft.network.codec.StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.VAR_INT,HitBall::ball,HitBall::new);
  @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 }
 @net.neoforged.fml.common.EventBusSubscriber(modid=MareEletrica.ID,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @net.neoforged.bus.api.SubscribeEvent public static void packets(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){
   e.registrar("12").playToServer(HitBall.TYPE,HitBall.CODEC,(p,c)->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer sp&&sp.isAlive()&&sp.level().getEntity(p.ball()) instanceof Ball b)b.struck(sp);});
  }
 }


 /** Tennis racket: right-click tosses a ball up in front of you (from your inventory); swing (left-click) near a ball to hit it where you look. */
 public static final class RacketItem extends Item {
  public RacketItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);
   int slot=-1;for(int i=0;i<p.getInventory().getContainerSize();i++)if(isBall(p.getInventory().getItem(i))){slot=i;break;}
   if(slot<0&&!p.isCreative()){if(!l.isClientSide)p.displayClientMessage(net.minecraft.network.chat.Component.literal("Sem bola de tênis no inventário"),true);return InteractionResultHolder.fail(s);}
   if(!l.isClientSide){
    var b=new Ball(ENTITY.get(),l);b.setOwner(p);Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();
    b.setPos(p.getX()+flat.x*.9,p.getEyeY()-.2,p.getZ()+flat.z*.9);b.setDeltaMovement(0,.55,0);   // the serve toss
    if(slot>=0)b.setItem(p.getInventory().getItem(slot).copyWithCount(1));
    else b.setItem(new ItemStack(COLORED.get(l.random.nextInt(COLORED.size())).get()));   // creative with no ball: a random colour each toss
    if(slot>=0&&!p.isCreative())p.getInventory().getItem(slot).shrink(1);
    l.addFreshEntity(b);
    l.playSound(null,p.blockPosition(),SoundEvents.WOOL_PLACE,SoundSource.PLAYERS,.4F,1.6F);
   }
   p.getCooldowns().addCooldown(this,10);
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
  }
 }

 /** Tennis net: a thin net with a white top tape; the ball bounces off it. Place several side by side for a full court. */
 public static final class NetBlock extends net.minecraft.world.level.block.HorizontalDirectionalBlock {
  public static final com.mojang.serialization.MapCodec<NetBlock> CODEC=simpleCodec(NetBlock::new);
  private static final net.minecraft.world.phys.shapes.VoxelShape NS=net.minecraft.world.level.block.Block.box(0,0,7.3,16,14,8.7),EW=net.minecraft.world.level.block.Block.box(7.3,0,0,8.7,14,16);
  public NetBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,net.minecraft.world.level.block.state.BlockState> b){b.add(FACING);}
  @Override public net.minecraft.world.level.block.state.BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getClockWise());}
  @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return s.getValue(FACING).getAxis()==Direction.Axis.Z?NS:EW;}
 }

 public static final class BallItem extends Item {
  public BallItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);
   l.playSound(null,p.getX(),p.getY(),p.getZ(),SoundEvents.SNOWBALL_THROW,SoundSource.PLAYERS,.5F,1.3F);
   if(!l.isClientSide){
    var b=new Ball(ENTITY.get(),l);b.setOwner(p);b.setPos(p.getX(),p.getEyeY()-.1,p.getZ());b.setItem(s.copyWithCount(1));
    // crouch for a soft toss, normal for a strong throw
    b.shootFromRotation(p,p.getXRot(),p.getYRot(),0F,p.isShiftKeyDown()?.7F:1.7F,.5F);
    l.addFreshEntity(b);
   }
   p.awardStat(Stats.ITEM_USED.get(this));s.consume(1,p);
   p.getCooldowns().addCooldown(this,6);
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
  }
 }

 public static final class Ball extends ThrowableItemProjectile {
  private int bounces;private int lastHit=-100;
  /** 1 = topspin (dips fast, kicks forward on the bounce), -1 = slice (floats, stays low), 0 = flat. */
  private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> SPIN=net.minecraft.network.syncher.SynchedEntityData.defineId(Ball.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(SPIN,0);}
  public int spin(){return entityData.get(SPIN);}
  private static double gravity(int spin){return spin>0?.062:spin<0?.032:.045;}
  /** A real shot: the ball flies to where the player looks, with power and spin from the kind of stroke and accuracy from the timing. */
  public void struck(net.minecraft.server.level.ServerPlayer p){
   if(tickCount-lastHit<5||!p.getMainHandItem().is(RACKET.get()))return;
   Vec3 body=p.position().add(0,1.0,0);double d=position().distanceTo(body);
   Vec3 to=position().subtract(p.getEyePosition());
   boolean pro=SportsMode.mode(p)==SportsMode.Mode.TENNIS;
   if(d>(pro?7.0:5.8)||to.normalize().dot(p.getLookAngle())<-.45)return;
   // contact quality: best with the ball about 2 blocks away, between the knees and the shoulders
   double rel=getY()-p.getY();
   double q=(1-Math.min(1,Math.abs(d-(pro?2.6:2.2))/(pro?5.0:4.0)))*(rel<0?.75:rel>2.6?.8:1);q=Math.max(pro?.55:.4,Math.min(1,q+(pro?.2:.1)));   // 0.65.2: much more forgiving
   Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z);if(flat.lengthSqr()<1e-4)flat=new Vec3(0,0,1);flat=flat.normalize();
   int spin;double vh;String shot;
   boolean lob=p.isShiftKeyDown()&&p.getXRot()<-15;
   if(p.isSprinting()){spin=1;vh=1.8;shot="Pancada!";}
   else if(lob){spin=0;vh=.75;shot="Balão";}
   else if(p.isShiftKeyDown()){spin=-1;vh=.9;shot="Slice";}
   else{spin=1;vh=1.3;shot="Topspin";}
   // where the player aims: the ground under the crosshair (or far ahead at the player's height)
   Vec3 eye=p.getEyePosition();var clip=level().clip(new net.minecraft.world.level.ClipContext(eye,eye.add(look.scale(64)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
   Vec3 target=clip.getType()==HitResult.Type.BLOCK&&clip.getLocation().distanceTo(eye)>4?clip.getLocation():position().add(flat.scale(22)).with(Direction.Axis.Y,p.getY());
   Vec3 hor=new Vec3(target.x-getX(),0,target.z-getZ());double dist=Math.max(2,hor.length());Vec3 dir=hor.lengthSqr()<1e-4?flat:hor.normalize();
   dir=dir.scale(.85).add(flat.scale(.15)).normalize();
   double power=.7+.3*q;double h=vh*power;
   double t=Math.max(6,dist/h);double g=gravity(spin);
   double vy=((target.y-getY())+.5*g*t*t)/t;
   if(lob)vy=Math.max(vy,1.05);
   vy=Math.max(-1.1,Math.min(1.6,vy));
   // timing errors spray the shot; a very late or early swing hits the frame
   double err=1-q;double yawErr=random.nextGaussian()*err*.08;
   dir=new Vec3(dir.x*Math.cos(yawErr)-dir.z*Math.sin(yawErr),0,dir.x*Math.sin(yawErr)+dir.z*Math.cos(yawErr));
   vy+=random.nextGaussian()*err*.08;
   Vec3 v;
   if(q<.18){v=dir.scale(.45).add(random.nextGaussian()*.25,.7+random.nextDouble()*.4,random.nextGaussian()*.25);spin=0;shot="Pegou no aro!";}
   else v=dir.scale(h).add(0,vy,0).add(p.getDeltaMovement().multiply(.3,0,.3));
   setDeltaMovement(v);hasImpulse=true;setOwner(p);bounces=0;lastHit=tickCount;entityData.set(SPIN,spin);
   var sl=(net.minecraft.server.level.ServerLevel)level();
   sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,getX(),getY(),getZ(),q>.75?10:4,.1,.1,.1,.2);
   level().playSound(null,getX(),getY(),getZ(),SoundEvents.SLIME_BLOCK_HIT,SoundSource.PLAYERS,1F,q>.5?2F:1.6F);
   level().playSound(null,getX(),getY(),getZ(),q>.75?SoundEvents.PLAYER_ATTACK_STRONG:SoundEvents.PLAYER_ATTACK_WEAK,SoundSource.PLAYERS,.6F,1.8F);
   p.displayClientMessage(net.minecraft.network.chat.Component.literal(shot+(q>=.18?"  •  "+(q>.8?"no ponto doce!":q>.5?"boa":"atrasado/adiantado"):"")),true);
   p.getMainHandItem().hurtAndBreak(1,p,net.minecraft.world.entity.EquipmentSlot.MAINHAND);
  }
  public Ball(EntityType<? extends Ball> t,Level l){super(t,l);}
  @Override protected Item getDefaultItem(){return ITEM.get();}
  @Override protected double getDefaultGravity(){return gravity(spin());}
  @Override protected void onHitBlock(BlockHitResult hit){
   if(level().isClientSide)return;
   Vec3 v=getDeltaMovement();Direction d=hit.getDirection();
   Vec3 n=Vec3.atLowerCornerOf(d.getNormal());
   // reflect: v - 2(v·n)n, losing energy on the bounce and a little grip on the surface
   Vec3 r=v.subtract(n.scale(2*v.dot(n)));
   Vec3 normalPart=n.scale(r.dot(n)),tangent=r.subtract(normalPart);
   if(level().getBlockState(hit.getBlockPos()).is(NET.get())){r=v.scale(-.12).add(0,-.05,0);}   // into the net: it dies and drops
   else{
    // tennis-ball bounce: topspin kicks up and forward, slice skids low and slows, flat is in between
    int sp=d==Direction.UP?spin():0;
    r=normalPart.scale(sp>0?.76:sp<0?.6:.7).add(tangent.scale(sp>0?.95:sp<0?.62:.84));
    if(d==Direction.UP&&sp!=0)entityData.set(SPIN,0);
   }
   bounces++;
   float speed=(float)v.length();
   if(speed>.12)level().playSound(null,getX(),getY(),getZ(),SoundEvents.SLIME_BLOCK_HIT,SoundSource.NEUTRAL,Math.min(1F,.25F+speed*.6F),1.7F+random.nextFloat()*.2F);
   if(r.length()<.11||bounces>40){settle(hit.getLocation().add(n.scale(.15)));return;}
   setPos(hit.getLocation().add(n.scale(.13)));
   setDeltaMovement(r);hasImpulse=true;
  }
  @Override protected void onHitEntity(EntityHitResult hit){
   if(level().isClientSide)return;
   var e=hit.getEntity();
   if(e instanceof Player pl&&pl.getMainHandItem().is(RACKET.get())&&tickCount-lastHit>5)return;   // flies past a player holding a racket (they swing to hit it)
   if(e instanceof Wolf w&&w.isTame()){fetch(w);return;}
   e.hurt(damageSources().thrown(this,getOwner()),getDeltaMovement().length()>1.2?1F:0F);
   level().playSound(null,getX(),getY(),getZ(),SoundEvents.SLIME_BLOCK_HIT,SoundSource.NEUTRAL,.7F,1.9F);
   setDeltaMovement(getDeltaMovement().scale(-.35).add(0,.15,0));hasImpulse=true;
  }
  @Override protected void onHit(HitResult r){super.onHit(r);} // no discard: the ball keeps bouncing
  /** Stops bouncing and becomes an item on the ground that can be picked up. */
  private void settle(Vec3 at){
   var drop=new net.minecraft.world.entity.item.ItemEntity(level(),at.x,at.y,at.z,getItem().isEmpty()?new ItemStack(ITEM.get()):getItem().copy());
   drop.setDeltaMovement(Vec3.ZERO);drop.setDefaultPickUpDelay();level().addFreshEntity(drop);
   // the nearest tamed wolf runs to bring it back
   for(var w:level().getEntitiesOfClass(Wolf.class,getBoundingBox().inflate(24),x->x.isTame()&&!x.isOrderedToSit()&&x.getOwner()!=null&&!FETCH.containsKey(x.getUUID())&&!CARRYING.contains(x.getUUID()))){FETCH.put(w.getUUID(),drop.getUUID());w.getNavigation().moveTo(drop,1.4);break;}
   discard();
  }
  /** Caught in the air by a tamed wolf: it carries the ball back. */
  private void fetch(Wolf w){
   if(CARRYING.contains(w.getUUID())){w.spawnAtLocation(getItem().isEmpty()?new ItemStack(ITEM.get()):getItem().copy());discard();return;}
   CARRYING.add(w.getUUID());
   level().playSound(null,w.blockPosition(),SoundEvents.WOLF_AMBIENT,SoundSource.NEUTRAL,1F,1.4F);
   discard();
  }
  @Override public void tick(){super.tick();if(!level().isClientSide&&(tickCount>1200||getY()<level().getMinBuildHeight()-16)){if(getY()>level().getMinBuildHeight())settle(position());else discard();}}
 }
}
