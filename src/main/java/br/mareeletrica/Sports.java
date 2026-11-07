package br.mareeletrica;

import net.minecraft.util.Mth;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Football, basketball and volleyball. Balls stay in the world: left-click kicks/hits them, right-click picks them up, right-click with one in hand throws.
 *  The football goal and the basketball hoop add points to the nearest scoreboard by themselves. */
public final class Sports {
 public enum Kind { SOCCER("soccer_ball",.045,.5,.985), BASKET("basketball",.05,.74,.99), VOLLEY("volleyball",.04,.52,.99);
  public final String id;public final double gravity,bounce,drag;
  Kind(String id,double g,double b,double d){this.id=id;gravity=g;bounce=b;drag=d;} }
 public static final Map<Kind,DeferredItem<BallItem>> BALLS=new EnumMap<>(Kind.class);
 static{for(var k:Kind.values())BALLS.put(k,MareEletrica.ITEMS.register(k.id,()->new BallItem(new Item.Properties().stacksTo(4),k)));}
 public static final Supplier<EntityType<SportBall>> BALL=MareEletrica.ACTORS.register("sport_ball",()->
  EntityType.Builder.<SportBall>of(SportBall::new,MobCategory.MISC).sized(.7F,.7F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":sport_ball"));
 public static final DeferredBlock<GoalBlock> GOAL=MareEletrica.BLOCKS.register("soccer_goal",()->new GoalBlock(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).noOcclusion(),false));
 public static final DeferredBlock<GoalBlock> HOOP=MareEletrica.BLOCKS.register("basketball_hoop",()->new GoalBlock(BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).noOcclusion(),true));
 public static final DeferredItem<BlockItem> GOAL_ITEM=MareEletrica.ITEMS.register("soccer_goal",()->new BlockItem(GOAL.get(),new Item.Properties()));
 public static final DeferredItem<BlockItem> HOOP_ITEM=MareEletrica.ITEMS.register("basketball_hoop",()->new BlockItem(HOOP.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<GoalEntity>> GOAL_ENTITY=MareEletrica.ENTITIES.register("sport_goal",()->BlockEntityType.Builder.of(GoalEntity::new,GOAL.get(),HOOP.get()).build(null));
 private Sports(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){for(var b:BALLS.values())out.accept(b.get());out.accept(GOAL_ITEM.get());out.accept(HOOP_ITEM.get());}
 static Kind kindOf(ItemStack s){for(var e:BALLS.entrySet())if(s.is(e.getValue().get()))return e.getKey();return Kind.SOCCER;}

 public static final class BallItem extends Item {
  final Kind kind;
  public BallItem(Properties p,Kind k){super(p);kind=k;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);
   if(!l.isClientSide){
    var b=new SportBall(BALL.get(),l);b.setItem(s.copyWithCount(1));b.setOwner(p);b.thrownFrom=p.position();
    b.setPos(p.getX(),p.getEyeY()-.3,p.getZ());
    float power=kind==Kind.SOCCER?.75F:p.isShiftKeyDown()?.9F:1.7F;
    b.shootFromRotation(p,p.getXRot()-(kind==Kind.BASKET?12:0),p.getYRot(),0,power,.4F);
    l.addFreshEntity(b);
    l.playSound(null,p.blockPosition(),SoundEvents.SNOWBALL_THROW,SoundSource.PLAYERS,.5F,.8F);
   }
   s.consume(1,p);
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
  }
 }

 public static final class SportBall extends ThrowableItemProjectile {
  public Vec3 thrownFrom;private boolean resting;public int scoredAt=-100;
  /** Client: rolling angle (radians) and axis, for the renderer. */
  public float roll,rollO;public Vec3 rollAxis=new Vec3(1,0,0);
  /** Player holding the ball (goalkeeper catch, basketball dribble), -1 = free. */
  private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> HOLDER=net.minecraft.network.syncher.SynchedEntityData.defineId(SportBall.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(HOLDER,-1);}
  public int holderId(){return entityData.get(HOLDER);}
  public Player holder(){return holderId()<0?null:level().getEntity(holderId()) instanceof Player p?p:null;}
  private Vec3 carrierLast;
  /** Basketball pass in flight: who it is meant for, and until when the catch counts. */
  public java.util.UUID passTo;public int passUntil;
  public void hold(Player p){carrierLast=null;entityData.set(HOLDER,p==null?-1:p.getId());resting=false;if(p!=null){setOwner(p);setDeltaMovement(Vec3.ZERO);}}
  public void release(Vec3 v,Player by){entityData.set(HOLDER,-1);kick(v,by);}
  public SportBall(EntityType<? extends SportBall> t,Level l){super(t,l);}
  public Kind kind(){return kindOf(getItem());}
  @Override protected Item getDefaultItem(){return BALLS.get(Kind.SOCCER).get();}
  @Override protected double getDefaultGravity(){return resting?0:kind().gravity;}
  @Override public boolean isPickable(){return true;}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<96*96;}
  @Override protected boolean canHitEntity(Entity e){return !(e instanceof Player)&&super.canHitEntity(e);}
  /** Sideways push applied every tick for a while: curve shots ("chute de curva"). */
  public Vec3 spin=Vec3.ZERO;public int spinTicks;
  public void curve(Vec3 perTick,int ticks){spin=perTick;spinTicks=ticks;}
  /** Tick of the last kick: a ball you just kicked is not trapped again by your own feet. */
  int kickedAt=-100;public int kickedAtTick(){return kickedAt;}
  /** basketball: the hand the ball is dribbled with (1 right, -1 left) — the crossover switches it */
  public int hand=1;
  public void kick(Vec3 v,Player by){kickedAt=tickCount;spinTicks=0;resting=false;setDeltaMovement(v);hasImpulse=true;hurtMarked=true;if(by!=null){setOwner(by);thrownFrom=by.position();}}
  /** Left-click: kick (football), pass (basketball), bump or spike (volleyball). */
  @Override public boolean skipAttackInteraction(Entity attacker){
   if(!(attacker instanceof Player p)||level().isClientSide)return true;
   if(SportsMode.mode(p)!=null){SportsMode.action(p,0);return true;}
   Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();
   Vec3 v;
   switch(kind()){
    case SOCCER->{double pow=p.isSprinting()?1.95:1.3;double lift=Math.max(.13,-p.getXRot()/90.0*1.4+.16);v=flat.scale(pow).add(0,lift,0);}
    case BASKET->{Vec3 ring=SportsMode.hoop(p);if(ring!=null){double d=ring.subtract(position()).horizontalDistance();v=SportsMode.arc(position(),ring.add(0,.05,0),Kind.BASKET.gravity,Math.max(8,Math.min(24,7+d*1.05)));thrownFrom=p.position();}else v=look.scale(1.2).add(0,.4,0);}
    default->{boolean spike=getY()>p.getEyeY()-.2&&p.getXRot()>10&&!p.onGround();v=spike?look.scale(1.5).add(0,-.3,0):flat.scale(.45).add(0,.85,0);}
   }
   kick(v,p);
   level().playSound(null,getX(),getY(),getZ(),kind()==Kind.SOCCER?SoundEvents.WOOL_BREAK:SoundEvents.SLIME_BLOCK_HIT,SoundSource.PLAYERS,1F,kind()==Kind.BASKET?.7F:1.3F);
   return true;
  }
  @Override public InteractionResult interact(Player p,InteractionHand hand){
   if(level().isClientSide)return InteractionResult.SUCCESS;
   if(holderId()>=0)return InteractionResult.PASS;
   var s=getItem().isEmpty()?new ItemStack(BALLS.get(Kind.SOCCER).get()):getItem().copy();
   if(!p.getInventory().add(s))return InteractionResult.FAIL;
   level().playSound(null,blockPosition(),SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.5F,1.2F);discard();
   return InteractionResult.CONSUME;
  }
  @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float a){if(s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile pr&&!level().isClientSide){kick(getDeltaMovement().add(pr.getDeltaMovement().scale(.4)),null);}return false;}
  @Override protected void onHitBlock(BlockHitResult hit){
   if(level().isClientSide)return;
   Vec3 v=getDeltaMovement();Vec3 n=Vec3.atLowerCornerOf(hit.getDirection().getNormal());
   Vec3 r=v.subtract(n.scale(2*v.dot(n)));Vec3 np=n.scale(r.dot(n)),tan=r.subtract(np);
   // restitution drops for soft hits (a real ball "dies" on the grass instead of hopping forever); grass and sand absorb more
   double impact=Math.abs(v.dot(n));var under=level().getBlockState(hit.getBlockPos());
   double soft=hit.getDirection()==Direction.UP&&(under.is(net.minecraft.tags.BlockTags.SAND)||under.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)||under.is(net.minecraft.tags.BlockTags.WOOL))?.8:1;
   double e=kind().bounce*soft*Mth.clamp(impact/.45,.45,1);
   r=np.scale(e).add(tan.scale(hit.getDirection()==Direction.UP?(kind()==Kind.SOCCER?.86:.9):.75));
   float speed=(float)impact;
   if(speed>.12)level().playSound(null,getX(),getY(),getZ(),kind()==Kind.BASKET?SoundEvents.SLIME_BLOCK_FALL:SoundEvents.WOOL_STEP,SoundSource.NEUTRAL,Math.min(1F,.25F+speed*.8F),kind()==Kind.BASKET?.6F:1F);
   // put the ball back just outside the surface it touched (its position is the bottom centre of a .7 box)
   Vec3 at=hit.getLocation();
   setPos(switch(hit.getDirection()){case UP->at.add(0,.002,0);case DOWN->at.add(0,-.71,0);default->at.add(n.scale(.36));});
   if(hit.getDirection()==Direction.UP&&Math.abs(r.y)<.16){r=new Vec3(r.x,0,r.z);if(r.horizontalDistance()<.012){r=Vec3.ZERO;resting=true;}}
   setDeltaMovement(r);hasImpulse=true;
  }
  @Override protected void onHit(HitResult r){super.onHit(r);}
  @Override public void tick(){
   var h=holder();
   if(h!=null){
    // carried: in the keeper's hands, or bounced in front of a basketball player (dribble)
    Vec3 flat=new Vec3(h.getLookAngle().x,0,h.getLookAngle().z);if(flat.lengthSqr()<1e-4)flat=new Vec3(0,0,1);flat=flat.normalize();
    boolean feet=kind()==Kind.SOCCER&&SportsMode.mode(h)==SportsMode.Mode.SOCCER;
    if(feet){   // football: the ball rolls glued to your feet, a little ahead when you run, touched forward every few steps
     // carrier speed from its own positions (a server player's motion vector is not kept up to date)
     Vec3 hp=h.position();Vec3 mv=carrierLast==null?Vec3.ZERO:hp.subtract(carrierLast);carrierLast=hp;
     double sp=Math.sqrt(mv.x*mv.x+mv.z*mv.z);if(sp>1.5)sp=0;Vec3 dir=sp>.03?new Vec3(mv.x,0,mv.z).normalize():flat;
     double ahead=.7+Math.min(.45,sp*1.8)+Math.abs(Math.sin(tickCount*.45))*Math.min(.3,sp*1.2);
     xo=getX();yo=getY();zo=getZ();
     Vec3 target=new Vec3(h.getX()+dir.x*ahead+mv.x,h.getY(),h.getZ()+dir.z*ahead+mv.z);
     Vec3 now=position();Vec3 next=now.add(target.subtract(now).scale(.7));
     setPos(next.x,level().noCollision(this,getBoundingBox().move(next.x-getX(),h.getY()-getY(),next.z-getZ()))?h.getY():Math.max(h.getY(),getY()),next.z);
     setDeltaMovement(next.subtract(now));
     if(!level().isClientSide){
      if(!h.isAlive()||SportsMode.mode(h)!=SportsMode.Mode.SOCCER||h.distanceToSqr(this)>9){entityData.set(HOLDER,-1);carrierLast=null;}
     }
     return;
    }
    double y=kind()==Kind.BASKET?h.getY()+.15+Math.abs(Math.sin(tickCount*.32))*.75:h.getY()+1.05;
    Vec3 side=new Vec3(-flat.z,0,flat.x).scale(kind()==Kind.BASKET?.38*hand:0);
    xo=getX();yo=getY();zo=getZ();setPos(h.getX()+flat.x*.65+side.x,y,h.getZ()+flat.z*.65+side.z);setDeltaMovement(Vec3.ZERO);
    if(!level().isClientSide){
     if(kind()==Kind.BASKET&&tickCount%9==0&&Math.abs(Math.sin(tickCount*.32))<.35)level().playSound(null,getX(),getY(),getZ(),SoundEvents.SLIME_BLOCK_FALL,SoundSource.PLAYERS,.4F,.6F);
     if(!h.isAlive()||SportsMode.mode(h)==null||h.distanceToSqr(this)>16)entityData.set(HOLDER,-1);
    }
    return;
   }
   if(resting&&!level().isClientSide&&tickCount%10==0&&level().noCollision(this,getBoundingBox().move(0,-.1,0)))resting=false;
   if(resting&&!level().isClientSide){setDeltaMovement(Vec3.ZERO);}
   baseTick();
   if(!resting)physics();   // real collision on both sides: the client no longer sinks the ball into the floor waiting for the server
   if(level().isClientSide){   // visual roll: the ball turns as it moves (axis across the motion)
    rollO=roll;double dx=getX()-xo,dz=getZ()-zo,d=Math.sqrt(dx*dx+dz*dz);
    if(d>1e-4){roll+=(float)(d/.25);rollAxis=new Vec3(dz/d,0,-dx/d);}
    return;
   }
   if(spinTicks>0&&!onGroundish()){spinTicks--;setDeltaMovement(getDeltaMovement().add(spin));hurtMarked=true;}
   // rolling friction on the ground (grass/sand slow it more), air drag in flight
   if(!resting&&onGroundish()){Vec3 v=getDeltaMovement();var under=level().getBlockState(blockPosition().below());double f=under.is(net.minecraft.tags.BlockTags.SAND)||under.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)?.93:.965;setDeltaMovement(v.x*kind().drag*f,v.y,v.z*kind().drag*f);}
   // bodies: the ball bounces off players and mobs (with a bit of their own speed); heads send it up
   if(passTo!=null&&!level().isClientSide&&holderId()<0){   // the pass reaches the teammate: they catch it
    if(tickCount>passUntil)passTo=null;
    else if(level().getPlayerByUUID(passTo) instanceof Player m&&m.getBoundingBox().inflate(.9).intersects(getBoundingBox())){hold(m);passTo=null;
     level().playSound(null,m.blockPosition(),SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.PLAYERS,1F,1.3F);m.displayClientMessage(net.minecraft.network.chat.Component.literal("§aPegou o passe!"),true);}}
   for(var e:level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,getBoundingBox().inflate(.05),x->x.isAlive()&&!x.isSpectator()&&x.getId()!=holderId())){
    if(kind()==Kind.SOCCER&&e instanceof Player&&getY()<e.getY()+.6)continue;   // feet: dribbling handles it
    Vec3 v=getDeltaMovement();Vec3 away=new Vec3(getX()-e.getX(),0,getZ()-e.getZ());if(away.lengthSqr()<1e-4)away=v.multiply(-1,0,-1);if(away.lengthSqr()<1e-4)continue;away=away.normalize();
    double into=-v.dot(away);if(into<=0&&getY()<e.getEyeY())continue;
    boolean head=getY()>e.getEyeY()-.25;
    Vec3 nv=head?new Vec3(v.x*.4,Math.abs(v.y)*.45+.18,v.z*.4).add(e.getLookAngle().multiply(.3,0,.3)):v.add(away.scale(into*1.5)).scale(.6).add(e.getDeltaMovement().multiply(.5,0,.5));
    setDeltaMovement(nv);hasImpulse=true;hurtMarked=true;resting=false;
    level().playSound(null,getX(),getY(),getZ(),SoundEvents.WOOL_STEP,SoundSource.NEUTRAL,.6F,head?1.3F:1F);
    break;
   }
   // football: dribbling — walking into the ball pushes it ahead of you
   if(kind()==Kind.SOCCER)for(var p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(.35),x->!x.isSpectator())){
    // in football mode a slow ball that reaches your feet is trapped (domínio): it stays with you instead of running away
    if(SportsMode.mode(p)==SportsMode.Mode.SOCCER&&holderId()<0&&getDeltaMovement().horizontalDistance()<.32&&Math.abs(getDeltaMovement().y)<.2&&tickCount-kickedAt>14&&SportsMode.held(p)==null){
     hold(p);level().playSound(null,getX(),getY(),getZ(),SoundEvents.WOOL_STEP,SoundSource.PLAYERS,.5F,1.1F);break;}
    Vec3 mv=p.getDeltaMovement();Vec3 away=new Vec3(getX()-p.getX(),0,getZ()-p.getZ());if(away.lengthSqr()<1e-4)continue;
    double sp=Math.max(.12,Math.min(.3,new Vec3(mv.x,0,mv.z).length()*1.4));kick(away.normalize().scale(sp),p);break;
   }
   if(getY()<level().getMinBuildHeight()-16)discard();
  }
  /** Moves with real block collision (no tunnelling, never inside the floor) and bounces: the vertical speed is lost
   *  a little more on every bounce; once the hop is tiny the ball stops bouncing and just rolls (no endless micro-hops). */
  private void physics(){
   Vec3 v=getDeltaMovement();
   boolean ground=onGroundish();
   if(ground&&v.y<=0&&v.y>-.09)v=new Vec3(v.x,0,v.z);          // sitting on the floor: no gravity, no hop
   else v=v.add(0,-kind().gravity,0);
   double drag=isInWater()?.8:.99;v=v.scale(drag);
   Vec3 from=position();
   move(net.minecraft.world.entity.MoverType.SELF,v);
   Vec3 moved=position().subtract(from);
   double nx=v.x,ny=v.y,nz=v.z;
   if(verticalCollision){
    if(v.y<0){
     double impact=-v.y;var under=level().getBlockState(getOnPos());
     double soft=under.is(net.minecraft.tags.BlockTags.SAND)||under.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)||under.is(net.minecraft.tags.BlockTags.WOOL)?.8:1;
     double e=kind().bounce*soft*Mth.clamp(impact/.45,.45,1);
     ny=impact*e;if(ny<.12)ny=0;                                  // small hop: it just settles
     double t=kind()==Kind.SOCCER?.86:.9;if(impact>.12){nx*=t;nz*=t;}
     if(impact>.14&&!level().isClientSide)level().playSound(null,getX(),getY(),getZ(),kind()==Kind.BASKET?SoundEvents.SLIME_BLOCK_FALL:SoundEvents.WOOL_STEP,SoundSource.NEUTRAL,Math.min(1F,.25F+(float)impact*.8F),kind()==Kind.BASKET?.6F:1F);
    }else ny=-v.y*.5;                                              // ceiling / crossbar
   }
   if(horizontalCollision){
    boolean hitX=Math.abs(moved.x-v.x)>1e-5,hitZ=Math.abs(moved.z-v.z)>1e-5;
    if(hitX)nx=-v.x*.6;if(hitZ)nz=-v.z*.6;
    if(!level().isClientSide&&Math.max(Math.abs(hitX?v.x:0),Math.abs(hitZ?v.z:0))>.2)level().playSound(null,getX(),getY(),getZ(),SoundEvents.WOOL_STEP,SoundSource.NEUTRAL,.6F,1F);
   }
   if(ny==0&&onGroundish()&&nx*nx+nz*nz<1.5e-4){nx=0;nz=0;if(!level().isClientSide)resting=true;}
   setDeltaMovement(nx,ny,nz);
  }
  private boolean onGroundish(){return !level().noCollision(this,getBoundingBox().move(0,-.08,0));}
  @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putBoolean("Resting",resting);}
  @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);resting=t.getBoolean("Resting");}
 }

 /** Football goal (3 wide x 2 high, net behind) or basketball hoop (on a pole). Right-click switches which team scores here. */
 public static final class GoalBlock extends HorizontalDirectionalBlock implements EntityBlock,Multiblock.Master {
  static final double[][] GOAL_BOXES={{-64.8,0,1,80.8,0.75,5.33},{-64.8,0,1,-60.25,72.8,5.33},{76.25,0,1,80.8,72.8,5.33},{-64.8,68.25,1,80.8,72.8,5.33},{-64.8,0,51.27,80.8,60.67,53},{-62.98,0,5.33,-62.62,68.25,53},{78.62,0,5.33,78.98,68.25,53},{-64.8,67.95,5.33,80.8,68.25,53}},HOOP_BOXES={{4.24,0,12.5,11.76,66,16.66},{-22,48.4,8.76,38,70.4,11.24},{1.34,0,10,14.66,2.2,16.66},{-8.26,57.2,-18.76,24.24,58.66,7.5}},HOOP_SOLID={{4.24,0,12.5,11.76,66,16.66},{-22,48.4,8.76,38,70.4,11.24},{1.34,0,10,14.66,2.2,16.66}};
  @Override public java.util.List<BlockPos> cells(BlockState s){return Multiblock.cellsOf(s.getValue(FACING),hoop?HOOP_BOXES:GOAL_BOXES);}
  @Override public VoxelShape cellShape(BlockState s,BlockPos o){return Multiblock.slice(s.getValue(FACING),hoop?HOOP_BOXES:GOAL_BOXES,o);}
  @Override public VoxelShape cellCollision(BlockState s,BlockPos o){return Multiblock.slice(s.getValue(FACING),hoop?HOOP_SOLID:GOAL_BOXES,o);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock()))Multiblock.clear(l,p,s);super.onRemove(s,l,p,n,moving);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack st){Multiblock.place(l,p,s);}

  public final boolean hoop;
  public static final MapCodec<GoalBlock> CODEC=simpleCodec(p->new GoalBlock(p,false));
  public GoalBlock(BlockBehaviour.Properties p,boolean hoop){super(p);this.hoop=hoop;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  /** The open side faces the player who places it. */
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var st=defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());return Multiblock.fits(c,st)?st:null;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return cellShape(s,BlockPos.ZERO);}
  @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return cellCollision(s,BlockPos.ZERO);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GoalEntity(p,s);}
  @SuppressWarnings("unchecked")
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){
   if(t!=GOAL_ENTITY.get())return null;
   return l.isClientSide?(BlockEntityTicker<T>)(BlockEntityTicker<GoalEntity>)(lv,ps,st,g)->{if(g.swish<99)g.swish++;}:(BlockEntityTicker<T>)(BlockEntityTicker<GoalEntity>)GoalEntity::tick;}
  @Override protected boolean triggerEvent(BlockState s,Level l,BlockPos p,int id,int param){var be=l.getBlockEntity(p);return be!=null&&be.triggerEvent(id,param);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide&&l.getBlockEntity(p) instanceof GoalEntity g){g.team=1-g.team;g.setChanged();player.displayClientMessage(Component.literal((hoop?"Cesta":"Gol")+" aqui conta ponto para: "+(g.team==0?"CASA":"VISITANTE")),true);}
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }

 public static final class GoalEntity extends BlockEntity {
  /** 0.66.0: the football goal is drawn and detected 30% bigger */
  public static final double G=1.3;
  /** client: ticks since the last basket (net swish animation) */
  public int swish=99;
  public int team;
  public GoalEntity(BlockPos p,BlockState s){super(GOAL_ENTITY.get(),p,s);}
  /** Local frame (facing north = open side toward -z): x across, z depth into the goal. */
  private static Vec3 world(BlockPos p,Direction f,double x,double y,double z){
   double rx,rz;switch(f){case SOUTH->{rx=-x;rz=-z;}case EAST->{rx=-z;rz=x;}case WEST->{rx=z;rz=-x;}default->{rx=x;rz=z;}}
   return new Vec3(p.getX()+.5+rx,p.getY()+y,p.getZ()+.5+rz);
  }
  /** Centre of the hoop ring in world space. */
  public static Vec3 ring(GoalEntity g){return world(g.getBlockPos(),g.getBlockState().getValue(HorizontalDirectionalBlock.FACING),0,3.617,-0.852);}
  static void tick(Level l,BlockPos p,BlockState s,GoalEntity g){
   var block=(GoalBlock)s.getBlock();var f=s.getValue(HorizontalDirectionalBlock.FACING);
   if(block.hoop){
    // ring: in front of the pole, about 3 blocks up (real height)
    Vec3 ring=world(p,f,0,3.617,-0.852);
    for(var b:l.getEntitiesOfClass(SportBall.class,new AABB(ring,ring).inflate(1.4),x->x.kind()==Kind.BASKET)){
     if(b.tickCount-b.scoredAt<30)continue;
     double dx=b.getX()-ring.x,dz=b.getZ()-ring.z;
     if(dx*dx+dz*dz<.72*.72&&b.yo+.35>=ring.y&&b.getY()+.35<ring.y&&b.getDeltaMovement().y<0){
      b.scoredAt=b.tickCount;
      int pts=b.thrownFrom!=null&&b.thrownFrom.distanceTo(ring)>7.5?3:2;
      g.score(l,p,ring,pts);l.blockEvent(p,s.getBlock(),1,0);   // swish
      b.setDeltaMovement(b.getDeltaMovement().multiply(.1,.6,.1));
     }
    }
   }else{
    // goal mouth (0.66.0: 30% bigger — about 9 wide, 4.5 high, 3.3 deep)
    var a=world(p,f,-3.22*G,0,(-0.321+.4375)*G-.4375);var c=world(p,f,3.22*G,3.2*G,(1.946+.4375)*G-.4375);
    var box=new AABB(a,c);
    for(var b:l.getEntitiesOfClass(SportBall.class,box,x->x.kind()==Kind.SOCCER)){
     if(b.tickCount-b.scoredAt<40)continue;
     b.scoredAt=b.tickCount;g.score(l,p,b.position(),1);
     // the ball goes straight into the inventory of the nearest player (to restart the game)
     var near=l.getNearestPlayer(b,48);
     if(near!=null&&near.getInventory().add(b.getItem().isEmpty()?new ItemStack(BALLS.get(Kind.SOCCER).get()):b.getItem().copy())){
      b.discard();near.displayClientMessage(Component.literal("§7⚽ A bola foi para o seu inventário"),false);l.playSound(null,near.blockPosition(),SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.6F,1F);}
     else b.setDeltaMovement(b.getDeltaMovement().scale(.1));
    }
   }
  }
  void score(Level l,BlockPos p,Vec3 at,int pts){
   var board=Scoreboard.nearest(l,p,64);
   if(board!=null){if(board.tennis){board.tennis=false;board.points=new int[2];}board.point(team,pts);}
   l.playSound(null,p,SoundEvents.FIREWORK_ROCKET_BLAST,SoundSource.BLOCKS,1.2F,1F);
   l.playSound(null,p,SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,1.5F,1.2F);
   if(l instanceof ServerLevel sl){sl.sendParticles(ParticleTypes.FIREWORK,at.x,at.y+.5,at.z,40,.6,.6,.6,.15);sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,at.x,at.y+.5,at.z,15,.8,.5,.8,.1);}
   String who=team==0?"CASA":"VISITANTE";
   for(var pl:l.getEntitiesOfClass(Player.class,new AABB(p).inflate(48)))pl.displayClientMessage(Component.literal("§6"+(((GoalBlock)getBlockState().getBlock()).hoop?(pts==3?"CESTA DE 3!":"CESTA!"):"GOOOL!")+" §f+"+pts+" "+who+(board!=null?"  ("+board.points[0]+" x "+board.points[1]+")":"")),true);
  }
  @Override public boolean triggerEvent(int id,int param){if(id==1){swish=0;return true;}return super.triggerEvent(id,param);}
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("Team",team);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);team=t.getInt("Team");}
 }
}
