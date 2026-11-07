package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.65.4: golf and bowling.
 *  GOLF — put a golf ball on the ground (right-click), hold right-click with the club to charge and let go to hit it
 *  (standing = drive in the air, crouching = putt along the ground). Get it into the hole (flag) — strokes are counted.
 *  BOWLING — the pinsetter sets up 10 pins in front of it (right-click again resets). Hold right-click with the bowling ball,
 *  let go to roll it. The pinsetter announces the result (STRIKE! when all 10 fall). */
public final class Games {
 private Games(){}
 public static void register(){}
 static BlockBehaviour.Properties props(MapColor c){return BlockBehaviour.Properties.of().mapColor(c).strength(1.5F,4F).noOcclusion().sound(SoundType.WOOD);}
 public static final Supplier<EntityType<GolfBall>> GOLF_BALL=MareEletrica.ACTORS.register("golf_ball",()->EntityType.Builder.<GolfBall>of(GolfBall::new,MobCategory.MISC).sized(.2F,.2F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":golf_ball"));
 public static final Supplier<EntityType<BowlingBall>> BOWLING_BALL=MareEletrica.ACTORS.register("bowling_ball",()->EntityType.Builder.<BowlingBall>of(BowlingBall::new,MobCategory.MISC).sized(.45F,.45F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":bowling_ball"));
 public static final Supplier<EntityType<Pin>> PIN=MareEletrica.ACTORS.register("bowling_pin",()->EntityType.Builder.<Pin>of(Pin::new,MobCategory.MISC).sized(.3F,.75F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":bowling_pin"));
 public static final DeferredItem<Item> GOLF_BALL_ITEM=MareEletrica.ITEMS.register("golf_ball",()->new GolfBallItem(new Item.Properties().stacksTo(16)));
 public static final DeferredItem<Item> GOLF_CLUB=MareEletrica.ITEMS.register("golf_club",()->new ClubItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> BOWLING_BALL_ITEM=MareEletrica.ITEMS.register("bowling_ball",()->new BowlingBallItem(new Item.Properties().stacksTo(1)));
 public static final DeferredBlock<HoleBlock> GOLF_HOLE=MareEletrica.BLOCKS.register("golf_hole",()->new HoleBlock(props(MapColor.COLOR_GREEN)));
 public static final DeferredBlock<PinsetterBlock> PINSETTER=MareEletrica.BLOCKS.register("bowling_pinsetter",()->new PinsetterBlock(props(MapColor.WOOD)));
 public static final DeferredItem<BlockItem> GOLF_HOLE_ITEM=blockItem(GOLF_HOLE,"§7Buraco com bandeira. Ponha no chão do campo.");
 public static final DeferredItem<BlockItem> PINSETTER_ITEM=blockItem(PINSETTER,"§7Monta 10 pinos na frente dele. Botão direito: arruma os pinos de novo.");
 /** 0.66.0: a real bowling lane: oiled maple boards (the ball rolls far and straight, a hook bites late), aiming arrows and gutters. */
 public static final DeferredBlock<LaneBlock> LANE=MareEletrica.BLOCKS.register("bowling_lane",()->new LaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F).sound(net.minecraft.world.level.block.SoundType.WOOD),16));
 public static final DeferredBlock<LaneBlock> LANE_ARROW=MareEletrica.BLOCKS.register("bowling_lane_arrow",()->new LaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F).sound(net.minecraft.world.level.block.SoundType.WOOD),16));
 public static final DeferredBlock<LaneBlock> GUTTER=MareEletrica.BLOCKS.register("bowling_gutter",()->new LaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion(),10));
 public static final DeferredItem<BlockItem> LANE_ITEM=blockItem(LANE,"§7Pista de boliche: a bola rola longe e reto (e o efeito pega no fim).");
 public static final DeferredItem<BlockItem> LANE_ARROW_ITEM=blockItem(LANE_ARROW,"§7Pista com as setinhas de mira.");
 public static final DeferredItem<BlockItem> GUTTER_ITEM=blockItem(GUTTER,"§7Canaleta: mais baixa que a pista — bola que cai aqui passa reto pelos pinos.");
 public static final Supplier<BlockEntityType<PinsetterEntity>> PINSETTER_ENTITY=MareEletrica.ENTITIES.register("bowling_pinsetter",()->BlockEntityType.Builder.of(PinsetterEntity::new,PINSETTER.get()).build(null));
 private static DeferredItem<BlockItem> blockItem(DeferredBlock<? extends Block> b,String tip){return MareEletrica.ITEMS.register(b.getId().getPath(),()->new BlockItem(b.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal(tip));}});}
 public static void creative(java.util.function.Consumer<ItemStack> out){for(var i:List.of(GOLF_CLUB.get(),GOLF_BALL_ITEM.get(),GOLF_HOLE_ITEM.get(),BOWLING_BALL_ITEM.get(),PINSETTER_ITEM.get(),LANE_ITEM.get(),LANE_ARROW_ITEM.get(),GUTTER_ITEM.get()))out.accept(new ItemStack(i));
  out.accept(new ItemStack(FieldKits.ITEMS.get(FieldKits.Kind.BOLICHE).get()));out.accept(new ItemStack(FieldKits.ITEMS.get(FieldKits.Kind.GOLFE).get()));}
 public static final class LaneBlock extends HorizontalDirectionalBlock {
  final VoxelShape shape;
  public LaneBlock(Properties p,int h){super(p);shape=Block.box(0,0,0,16,h,16);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection());}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return MapCodec.unit(this);}
  @Override protected VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return shape;}
 }

 // ---------------- shared rolling physics (real block collision, bounce, rolling friction) ----------------
 abstract static class Rolling extends Entity {
  Rolling(EntityType<?> t,Level l){super(t,l);}
  abstract double gravity();abstract double bounce();abstract double roll();
  double drag(){return .99;}
  /** how much speed survives hitting a wall */
  double wall(){return .5;}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override public boolean isPickable(){return true;}
  @Override public boolean isPushable(){return false;}
  /** returns the impact speed of a floor hit this tick (0 if none). */
  double physics(){
   Vec3 v=getDeltaMovement();boolean ground=onGround();
   if(ground&&v.y<=0&&v.y>-.08)v=new Vec3(v.x,-.02,v.z);else v=v.add(0,-gravity(),0);   // a tiny push down keeps it in contact (0.66.0: it used to hop and lose 8% every other tick)
   v=v.scale(isInWater()?.8:drag());
   Vec3 from=position();move(MoverType.SELF,v);Vec3 moved=position().subtract(from);
   double nx=v.x,ny=v.y,nz=v.z,impact=0;
   if(verticalCollision&&v.y<0){impact=-v.y;if(impact>.1){ny=impact*bounce();if(ny<.08)ny=0;nx*=.92;nz*=.92;}else{ny=0;impact=0;}}else if(verticalCollision)ny=-v.y*.4;
   if(horizontalCollision){if(Math.abs(moved.x-v.x)>1e-5)nx=-v.x*wall();if(Math.abs(moved.z-v.z)>1e-5)nz=-v.z*wall();}
   if(ny==0&&onGround()){nx*=roll();nz*=roll();if(nx*nx+nz*nz<1e-5){nx=0;nz=0;}}
   setDeltaMovement(nx,ny,nz);return impact;
  }
 }

 // ---------------- golf ----------------
 public static final class GolfBall extends Rolling {
  public UUID hitter;public int strokes;public float spin,spinO;
  public GolfBall(EntityType<? extends GolfBall> t,Level l){super(t,l);}
  @Override double gravity(){return .045;}@Override double bounce(){return .38;}
  /** 0.67.0: rolls much further on minigolf turf, bounces off the minigolf bumpers */
  @Override double roll(){return MiniGolf.onTurf(this)?.978:.955;}
  @Override double wall(){return MiniGolf.onTurf(this)?.78:.5;}
  @Override public float maxUpStep(){return .14F;}   // climbs the 2-px steps of a minigolf ramp
  @Override public void tick(){
   super.tick();spinO=spin;double sp=getDeltaMovement().horizontalDistance();spin+=sp*60;
   double y0=getY();
   if(!level().isClientSide)MiniGolf.windmill(this);
   double impact=physics();
   MiniGolf.ramp(this);   // smooth slope on minigolf ramps (gravity along it)
   if(level().isClientSide){if(sp>.6&&tickCount%2==0)level().addParticle(ParticleTypes.CLOUD,getX(),getY()+.1,getZ(),0,0,0);return;}
   if(impact>.12)level().playSound(null,blockPosition(),SoundEvents.WOOD_HIT,SoundSource.NEUTRAL,.4F,1.8F);
   // into the hole: slow enough, over the cup
   BlockPos below=BlockPos.containing(getX(),getY()-.05,getZ());BlockPos here=blockPosition();
   for(BlockPos h:new BlockPos[]{here,below})if(level().getBlockState(h).is(GOLF_HOLE.get())){
    double dx=getX()-(h.getX()+.5),dz=getZ()-(h.getZ()+.5);
    if(dx*dx+dz*dz<.22*.22&&getDeltaMovement().horizontalDistance()<.42){sink(h);return;}
   }
   if(getY()<level().getMinBuildHeight()-5)discard();
  }
  void sink(BlockPos hole){
   var l=(ServerLevel)level();l.sendParticles(ParticleTypes.HAPPY_VILLAGER,hole.getX()+.5,hole.getY()+1,hole.getZ()+.5,20,.3,.3,.3,0);
   l.playSound(null,hole,SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,1F,1.5F);l.playSound(null,hole,SoundEvents.PLAYER_LEVELUP,SoundSource.BLOCKS,.7F,1.3F);
   Player p=hitter==null?null:l.getPlayerByUUID(hitter);
   String how=strokes==1?"§6§lHOLE IN ONE! §r§6(1 tacada)":"§aNa caçapa em §f"+strokes+"§a tacadas!";
   for(var o:l.players())if(o.distanceToSqr(this)<48*48)o.displayClientMessage(Component.literal("⛳ "+(p!=null?p.getName().getString()+": ":"")+how),false);
   if(p!=null&&!p.getInventory().add(new ItemStack(GOLF_BALL_ITEM.get())))p.drop(new ItemStack(GOLF_BALL_ITEM.get()),false);
   discard();
  }
  /** Punch the ball to pick it back up. */
  @Override public boolean hurt(DamageSource s,float a){if(!level().isClientSide&&!isRemoved()&&s.getEntity() instanceof Player p){if(!p.isCreative())p.getInventory().add(new ItemStack(GOLF_BALL_ITEM.get()));discard();}return true;}
  @Override protected void readAdditionalSaveData(CompoundTag t){strokes=t.getInt("Strokes");hitter=t.hasUUID("Hitter")?t.getUUID("Hitter"):null;}
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putInt("Strokes",strokes);if(hitter!=null)t.putUUID("Hitter",hitter);}
 }
 public static final class GolfBallItem extends Item {
  public GolfBallItem(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext c){
   if(c.getClickedFace()!=Direction.UP)return InteractionResult.PASS;
   if(c.getLevel() instanceof ServerLevel l){var at=c.getClickLocation();var b=GOLF_BALL.get().create(l);b.setPos(at.x,at.y+.01,at.z);l.addFreshEntity(b);
    l.playSound(null,c.getClickedPos(),SoundEvents.WOOD_PLACE,SoundSource.PLAYERS,.6F,1.8F);if(c.getPlayer()!=null&&!c.getPlayer().isCreative())c.getItemInHand().shrink(1);}
   return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Botão direito no chão: põe a bola. Soco na bola: pega de volta."));}
 }
 /** Charge with right-click; power grows over 1.5 s. */
 public static final class ClubItem extends Item {
  public ClubItem(Properties p){super(p);}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
  @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.SPEAR;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
  @Override public void releaseUsing(ItemStack s,Level l,LivingEntity e,int left){
   if(l.isClientSide||!(e instanceof Player p))return;
   float power=Mth.clamp((72000-left)/30F,.08F,1F);
   Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();
   GolfBall ball=null;double best=2.6*2.6;
   for(var b:l.getEntitiesOfClass(GolfBall.class,p.getBoundingBox().inflate(2.6))){double d=b.distanceToSqr(p.getX(),b.getY(),p.getZ());if(d<best&&b.position().subtract(p.position()).dot(flat)>-.3){best=d;ball=b;}}
   if(ball==null){p.displayClientMessage(Component.literal("§7Chegue perto da bola de golfe."),true);return;}
   Vec3 v;String what;
   if(p.isShiftKeyDown()){v=flat.scale(.15+power*.75).add(0,.02,0);what="Tacada curta";}                    // putt: rolls along the ground
   else{double sp=.5+power*2.5;double a=Math.toRadians(18+power*14);v=flat.scale(Math.cos(a)*sp).add(0,Math.sin(a)*sp,0);what="Tacada";}  // drive
   ball.setDeltaMovement(v);ball.hurtMarked=true;ball.hitter=p.getUUID();ball.strokes++;
   l.playSound(null,ball.blockPosition(),SoundEvents.PLAYER_ATTACK_STRONG,SoundSource.PLAYERS,.8F,1.9F);
   ((ServerLevel)l).sendParticles(ParticleTypes.CRIT,ball.getX(),ball.getY()+.1,ball.getZ(),6,.1,.1,.1,.1);
   p.displayClientMessage(Component.literal("§f⛳ "+what+" "+ball.strokes+" §7(força "+Math.round(power*100)+"%)"),true);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Segure o botão direito perto da bola para carregar a força e solte."));t.add(Component.literal("§8Em pé: tacada alta • Agachado: tacada curta (rola no chão)"));}
 }
 public static final class HoleBlock extends Block {
  static final VoxelShape SHAPE=Block.box(0,0,0,16,16,16);
  public HoleBlock(Properties p){super(p);}
 }

 // ---------------- bowling ----------------
 public static final class BowlingBall extends Rolling {
  public UUID thrower;public float spin,spinO;int still;
  static final EntityDataAccessor<Integer> COLOR=SynchedEntityData.defineId(BowlingBall.class,EntityDataSerializers.INT);
  public static final int[] COLORS={0x1E4FD8,0xD8261E,0x7A2AB8,0x16A04A,0xF07A16,0xE8338A,0x141414,0x12B5C9,0xE8C311,0x8A5A2A};
  /** hook: sideways direction and strength (the curve "bites" after a few blocks, like oil running out on a real lane) */
  public Vec3 hookDir=Vec3.ZERO;public float hook;double travelled;
  public BowlingBall(EntityType<? extends BowlingBall> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(COLOR,COLORS[0]);}
  public int color(){return entityData.get(COLOR);}
  void setColor(int c){entityData.set(COLOR,c);}
  @Override double gravity(){return .08;}@Override double bounce(){return .1;}@Override double drag(){return .999;}
  @Override double roll(){var b=level().getBlockState(BlockPos.containing(getX(),getY()-.1,getZ()));return b.is(LANE.get())||b.is(LANE_ARROW.get())?.9975:b.is(GUTTER.get())?.996:.994;}
  @Override public void tick(){
   super.tick();spinO=spin;double sp=getDeltaMovement().horizontalDistance();spin+=sp*45;
   if(!level().isClientSide&&hook!=0&&onGround()){
    boolean gutter=level().getBlockState(BlockPos.containing(getX(),getY()-.1,getZ())).is(GUTTER.get());
    if(gutter)hook=0;
    else{travelled+=sp;double bite=Mth.clamp((travelled-6)/10,0,1);setDeltaMovement(getDeltaMovement().add(hookDir.scale(hook*bite*sp)));}
   }
   Vec3 before=position();
   double impact=physics();
   if(level().isClientSide)return;
   if(impact>.15)level().playSound(null,blockPosition(),SoundEvents.STONE_HIT,SoundSource.NEUTRAL,.8F,.6F);
   if(sp>.05&&tickCount%6==0)level().playSound(null,blockPosition(),SoundEvents.MINECART_RIDING,SoundSource.NEUTRAL,.15F,.6F);
   var sweep=getBoundingBox().inflate(.18).expandTowards(before.subtract(position()));   // fast ball: check the whole path of this tick
   for(var pin:level().getEntitiesOfClass(Pin.class,sweep,x->!x.down())){pin.knock(getDeltaMovement(),position());setDeltaMovement(getDeltaMovement().scale(.93));}
   still=sp<.02?still+1:0;
   if(still>40||tickCount>400){   // the ball comes back to whoever rolled it
    Player p=thrower==null?null:level().getPlayerByUUID(thrower);
    if(p!=null&&!p.isCreative()&&!p.getInventory().add(new ItemStack(BOWLING_BALL_ITEM.get())))p.drop(new ItemStack(BOWLING_BALL_ITEM.get()),false);
    discard();
   }
  }
  @Override protected void readAdditionalSaveData(CompoundTag t){thrower=t.hasUUID("Thrower")?t.getUUID("Thrower"):null;if(t.contains("Color"))entityData.set(COLOR,t.getInt("Color"));}
  @Override protected void addAdditionalSaveData(CompoundTag t){if(thrower!=null)t.putUUID("Thrower",thrower);t.putInt("Color",color());}
 }
 public static final class BowlingBallItem extends Item {
  public BowlingBallItem(Properties p){super(p);}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
  @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.SPEAR;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
  @Override public void releaseUsing(ItemStack s,Level l,LivingEntity e,int left){
   if(l.isClientSide||!(e instanceof Player p))return;
   float power=Mth.clamp((72000-left)/20F,.15F,1F);Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();
   var b=BOWLING_BALL.get().create(l);Vec3 at=p.position().add(flat.scale(.8));b.setPos(at.x,p.getY()+.05,at.z);
   b.setColor(BowlingBall.COLORS[p.getRandom().nextInt(BowlingBall.COLORS.length)]);
   Vec3 v=flat.scale(.5+power*.8);
   if(p.isShiftKeyDown()){   // crouched: thrown with spin — it starts out wide and hooks back toward the pins late on the lane
    Vec3 side=new Vec3(-flat.z,0,flat.x);double toward=1;
    var ps=nearestPinsetter(l,p.position());
    if(ps!=null){Vec3 head=ps.headPin();Vec3 rel=head.subtract(p.position());toward=Math.signum(rel.x*side.x+rel.z*side.z);if(toward==0)toward=1;}
    v=v.add(side.scale(-toward*.03));b.hookDir=side.scale(toward);b.hook=.018F;
    p.displayClientMessage(Component.literal("§b🎳 Com efeito! §7(a bola abre e volta no fim da pista)"),true);
   }
   b.setDeltaMovement(v);b.thrower=p.getUUID();l.addFreshEntity(b);
   l.playSound(null,p.blockPosition(),SoundEvents.STONE_PLACE,SoundSource.PLAYERS,1F,.6F);
   if(!p.isCreative())s.shrink(1);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Segure o botão direito para dar força e solte para rolar a bola."));
   t.add(Component.literal("§7Agachado: joga com efeito (curva no fim da pista). Em pé: reta."));t.add(Component.literal("§8Cada jogada sai com uma cor diferente."));}
 }
 static PinsetterEntity nearestPinsetter(Level l,Vec3 at){
  PinsetterEntity best=null;double bd=40*40;var c=BlockPos.containing(at);
  for(int cx=(c.getX()-40)>>4;cx<=(c.getX()+40)>>4;cx++)for(int cz=(c.getZ()-40)>>4;cz<=(c.getZ()+40)>>4;cz++){if(!l.hasChunk(cx,cz))continue;
   for(var be:l.getChunk(cx,cz).getBlockEntities().values())if(be instanceof PinsetterEntity pe){double d=pe.getBlockPos().getCenter().distanceToSqr(at);if(d<bd){bd=d;best=pe;}}}
  return best;}
 public static final class Pin extends Entity {
  static final EntityDataAccessor<Float> FALL=SynchedEntityData.defineId(Pin.class,EntityDataSerializers.FLOAT);
  static final EntityDataAccessor<Float> DIR=SynchedEntityData.defineId(Pin.class,EntityDataSerializers.FLOAT);
  public float tip,tipO;
  public Pin(EntityType<? extends Pin> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(FALL,0F);b.define(DIR,0F);}
  public boolean down(){return entityData.get(FALL)>0;}
  public float fallDir(){return entityData.get(DIR);}
  @Override public boolean isPickable(){return true;}
  /** Knocked by the ball or another pin: tips over away from the hit and slides a bit. */
  void knock(Vec3 push,Vec3 from){
   if(down())return;Vec3 away=position().subtract(from).multiply(1,0,1);if(away.lengthSqr()<1e-4)away=push;away=away.normalize();
   Vec3 dir=push.multiply(1,0,1).normalize().scale(.6).add(away.scale(.4)).normalize();
   entityData.set(FALL,1F);entityData.set(DIR,(float)Math.toDegrees(Math.atan2(-dir.x,dir.z)));
   setDeltaMovement(dir.scale(.18+push.horizontalDistance()*.5).add(0,.17,0));
   level().playSound(null,blockPosition(),SoundEvents.WOOD_HIT,SoundSource.NEUTRAL,1F,1.1F+random.nextFloat()*.3F);
  }
  @Override public void tick(){
   super.tick();tipO=tip;if(down())tip=Math.min(90,tip+12);
   Vec3 v=getDeltaMovement();if(!onGround())v=v.add(0,-.06,0);v=v.multiply(.9,.98,.9);if(onGround()&&v.y<0)v=new Vec3(v.x*.8,0,v.z*.8);
   setDeltaMovement(v);move(MoverType.SELF,v);
   if(!level().isClientSide&&down()&&v.horizontalDistance()>.05)for(var o:level().getEntitiesOfClass(Pin.class,getBoundingBox().inflate(.35),x->x!=this&&!x.down()))o.knock(v,position());   // chain reaction
   if(!level().isClientSide&&tickCount>20*60*10)discard();
  }
  @Override public boolean hurt(DamageSource s,float a){if(!level().isClientSide&&s.getEntity() instanceof Player p&&!down())knock(p.getLookAngle().scale(.6),p.position());return true;}
  @Override protected void readAdditionalSaveData(CompoundTag t){entityData.set(FALL,t.getFloat("Fall"));entityData.set(DIR,t.getFloat("Dir"));}
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putFloat("Fall",entityData.get(FALL));t.putFloat("Dir",entityData.get(DIR));}
 }
 public static final class PinsetterBlock extends BaseEntityBlock {
  public PinsetterBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HorizontalDirectionalBlock.FACING);}
  /** It faces the lane: the pins go on the side the player was standing. */
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,c.getHorizontalDirection().getOpposite());}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new PinsetterEntity(p,s);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,PINSETTER_ENTITY.get(),PinsetterEntity::tick);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){
   if(!l.isClientSide&&l.getBlockEntity(pos) instanceof PinsetterEntity e){e.setPins();p.displayClientMessage(Component.literal("§f🎳 Pinos arrumados! Role a bola."),true);}
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  @Override public void setPlacedBy(Level l,BlockPos pos,BlockState s,LivingEntity e,ItemStack st){if(!l.isClientSide&&l.getBlockEntity(pos) instanceof PinsetterEntity pe)pe.setPins();}
 }
 public static final class PinsetterEntity extends BlockEntity {
  final List<UUID> pins=new ArrayList<>();int lastDown=-1,quiet;boolean thrown;long resetAt=-1;
  public Vec3 headPin(){Direction f=getBlockState().getValue(HorizontalDirectionalBlock.FACING);return Vec3.atBottomCenterOf(worldPosition).add(Vec3.atLowerCornerOf(f.getNormal()).scale(4.0));}
  public PinsetterEntity(BlockPos p,BlockState s){super(PINSETTER_ENTITY.get(),p,s);}
  public void setPins(){
   var l=(ServerLevel)level;for(var id:pins){var e=l.getEntity(id);if(e!=null)e.discard();}pins.clear();
   Direction f=getBlockState().getValue(HorizontalDirectionalBlock.FACING);Vec3 fwd=Vec3.atLowerCornerOf(f.getNormal()),side=new Vec3(-fwd.z,0,fwd.x);
   Vec3 base=Vec3.atBottomCenterOf(worldPosition);
   for(int row=0;row<4;row++)for(int k=0;k<=row;k++){   // head pin 4 blocks out (toward the bowler), each row 0.55 closer to the machine
    Vec3 at=base.add(fwd.scale(4.0-row*.55)).add(side.scale((k-row/2.0)*.6));
    var p=PIN.get().create(l);p.setPos(at.x,worldPosition.getY(),at.z);l.addFreshEntity(p);pins.add(p.getUUID());
   }
   lastDown=-1;quiet=0;thrown=false;setChanged();
   l.playSound(null,worldPosition,SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,1F,.8F);
  }
  int down(){int n=0;for(var id:pins)if(((ServerLevel)level).getEntity(id) instanceof Pin p&&p.down())n++;else if(((ServerLevel)level).getEntity(id)==null)n++;return n;}
  static void tick(Level l,BlockPos pos,BlockState s,PinsetterEntity e){
   if(e.resetAt>0&&l.getGameTime()>=e.resetAt){e.resetAt=-1;e.setPins();for(var p:l.players())if(p.distanceToSqr(pos.getCenter())<32*32)p.displayClientMessage(Component.literal("§f🎳 Pinos de volta — próxima jogada!"),true);return;}
   if(e.pins.isEmpty()||l.getGameTime()%5!=0)return;
   int d=e.down();
   if(d!=e.lastDown){if(d>0)e.thrown=true;e.lastDown=d;e.quiet=0;return;}
   if(!e.thrown)return;
   if(++e.quiet==12){   // nothing moved for 3 s: call it
    String msg=d==10?"§6§l🎳 STRIKE!!! §r§6Todos os 10 pinos!":d==0?"§7🎳 Nenhum pino... bola na canaleta!":"§f🎳 "+d+" pino"+(d>1?"s":"")+" derrubado"+(d>1?"s":"")+"!";
    for(var p:l.players())if(p.distanceToSqr(pos.getCenter())<32*32)p.displayClientMessage(Component.literal(msg),false);
    l.playSound(null,pos,d==10?SoundEvents.PLAYER_LEVELUP:SoundEvents.NOTE_BLOCK_CHIME.value(),SoundSource.BLOCKS,1F,d==10?1.2F:1F);
    if(d==10)((ServerLevel)l).sendParticles(ParticleTypes.TOTEM_OF_UNDYING,pos.getX()+.5,pos.getY()+1.5,pos.getZ()+.5,40,.6,.6,.6,.4);
    e.thrown=false;e.resetAt=l.getGameTime()+60;   // the machine sets the pins again 3 s later
   }
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);var l=new net.minecraft.nbt.ListTag();for(var id:pins)l.add(net.minecraft.nbt.NbtUtils.createUUID(id));t.put("Pins",l);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);pins.clear();for(var x:t.getList("Pins",11))pins.add(net.minecraft.nbt.NbtUtils.loadUUID(x));}
 }
}
