package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.67.0: minigolf. Felt turf (the ball rolls far and true), wooden bumpers it bounces off, a windmill whose turning sails
 *  block the tunnel every few seconds, 2-px-step ramps the ball can climb, a tee that hands out balls, and a putter
 *  (always a ground stroke, with an aiming line while you charge). Plus a ready-made 3-hole course in FieldKits. */
public final class MiniGolf {
 private MiniGolf(){}
 static BlockBehaviour.Properties props(MapColor c,SoundType s){return BlockBehaviour.Properties.of().mapColor(c).strength(1F).sound(s);}
 public static final DeferredBlock<Block> TURF=MareEletrica.BLOCKS.register("minigolf_turf",()->new Block(props(MapColor.COLOR_GREEN,SoundType.WOOL)));
 public static final DeferredBlock<LowBlock> BORDER=MareEletrica.BLOCKS.register("minigolf_border",()->new LowBlock(props(MapColor.WOOD,SoundType.WOOD).noOcclusion()));
 public static final DeferredBlock<TeeBlock> TEE=MareEletrica.BLOCKS.register("minigolf_tee",()->new TeeBlock(props(MapColor.COLOR_GREEN,SoundType.WOOL)));
 public static final DeferredBlock<RampBlock> RAMP=MareEletrica.BLOCKS.register("minigolf_ramp",()->new RampBlock(props(MapColor.COLOR_GREEN,SoundType.WOOL).noOcclusion()));
 public static final DeferredBlock<WindmillBlock> WINDMILL=MareEletrica.BLOCKS.register("minigolf_windmill",()->new WindmillBlock(props(MapColor.COLOR_RED,SoundType.WOOD).noOcclusion()));
 public static final Supplier<BlockEntityType<WindmillEntity>> WINDMILL_ENTITY=MareEletrica.ENTITIES.register("minigolf_windmill",()->BlockEntityType.Builder.of(WindmillEntity::new,WINDMILL.get()).build(null));
 public static final DeferredItem<Item> PUTTER=MareEletrica.ITEMS.register("minigolf_putter",()->new PutterItem(new Item.Properties().stacksTo(1)));
 static DeferredItem<BlockItem> item(DeferredBlock<? extends Block> b,String tip){return MareEletrica.ITEMS.register(b.getId().getPath(),()->new BlockItem(b.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal(tip));}});}
 public static final DeferredItem<BlockItem> TURF_ITEM=item(TURF,"§7Grama de minigolfe: a bola rola longe e reto.");
 public static final DeferredItem<BlockItem> BORDER_ITEM=item(BORDER,"§7Borda de madeira: a bola bate e volta.");
 public static final DeferredItem<BlockItem> TEE_ITEM=item(TEE,"§7Saída: clique com a mão vazia para pegar uma bola de golfe.");
 public static final DeferredItem<BlockItem> RAMP_ITEM=item(RAMP,"§7Rampa de minigolfe: sobe na direção em que você olha ao colocar.");
 public static final DeferredItem<BlockItem> WINDMILL_ITEM=item(WINDMILL,"§7Moinho: as pás giram e tampam o túnel de vez em quando — acerte a hora!");
 public static void register(){}
 public static void creative(java.util.function.Consumer<ItemStack> out){
  for(var i:List.of(PUTTER.get(),TURF_ITEM.get(),BORDER_ITEM.get(),TEE_ITEM.get(),RAMP_ITEM.get(),WINDMILL_ITEM.get()))out.accept(new ItemStack(i));
  out.accept(new ItemStack(FieldKits.ITEMS.get(FieldKits.Kind.MINIGOLFE).get()));}

 /** the ball sits on minigolf surface (turf, tee, ramp, hole) */
 static boolean onTurf(net.minecraft.world.entity.Entity e){
  var s=e.level().getBlockState(BlockPos.containing(e.getX(),e.getY()-.06,e.getZ()));
  return s.is(TURF.get())||s.is(TEE.get())||s.is(RAMP.get())||s.is(Games.GOLF_HOLE.get());}

 // ---------------- windmill ----------------
 /** sail angle in degrees (same on server and clients: it only depends on the world clock) */
 public static float sailAngle(long gameTime,float pt){return (float)(((gameTime%120)+pt)*3.0);}
 /** a sail hangs in front of the tunnel mouth */
 public static boolean blocked(long gameTime){float a=sailAngle(gameTime,0)%90F;return a<22F||a>68F;}
 /** server: a ball reaching the tunnel mouth while a sail is down bounces back */
 static void windmill(Games.GolfBall b){
  Vec3 v=b.getDeltaMovement();if(v.horizontalDistanceSqr()<1e-5)return;
  Vec3 ahead=b.position().add(v.x*1.5,.05,v.z*1.5);var pos=BlockPos.containing(ahead);var s=b.level().getBlockState(pos);
  if(!(s.getBlock() instanceof WindmillBlock))return;
  Direction f=s.getValue(HorizontalDirectionalBlock.FACING);Vec3 n=Vec3.atLowerCornerOf(f.getNormal());
  if(v.dot(n)>=0)return;   // only balls going into the front face
  Vec3 front=Vec3.atCenterOf(pos).add(n.scale(.5));double dist=b.position().subtract(front).dot(n);
  if(dist<-.05||dist>.45||!blocked(b.level().getGameTime()))return;
  double vn=v.dot(n);b.setDeltaMovement(v.subtract(n.scale(2*vn)).scale(.6));b.hurtMarked=true;
  b.level().playSound(null,pos,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.6F,1.4F);
 }
 static VoxelShape rotated(Direction f,double[][] boxes){VoxelShape s=Shapes.empty();for(var b:boxes)s=Shapes.or(s,Multiblock.box(f,b[0],b[1],b[2],b[3],b[4],b[5]));return s;}
 public abstract static class Facing extends HorizontalDirectionalBlock {
  protected Facing(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection());}
 }
 public static final class LowBlock extends Block {
  static final VoxelShape SHAPE=Block.box(0,0,0,16,5,16);
  public LowBlock(Properties p){super(p);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
 }
 public static final class TeeBlock extends Block {
  public TeeBlock(Properties p){super(p);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){
   if(!l.isClientSide){
    boolean has=p.getInventory().contains(new ItemStack(Games.GOLF_BALL_ITEM.get()));
    if(has)p.displayClientMessage(Component.literal("§7Você já tem uma bola de golfe."),true);
    else{p.getInventory().add(new ItemStack(Games.GOLF_BALL_ITEM.get()));l.playSound(null,pos,SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.6F,1.2F);
     p.displayClientMessage(Component.literal("§a⛳ Bola de golfe! §7Ponha no chão e use o taco de minigolfe."),true);}
   }
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }
 /** 8 steps of 2 px rising toward FACING (the golf ball climbs 2 px) */
 public static final class RampBlock extends Facing {
  public RampBlock(Properties p){super(p);}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return MapCodec.unit(this);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
   var f=s.getValue(FACING);VoxelShape out=Shapes.empty();
   for(int i=0;i<8;i++)out=Shapes.or(out,Multiblock.box(f,0,0,14-2*i,16,2*(i+1),16-2*i));
   return out;}
  /** golf balls ignore the steps and roll on a smooth slope instead (see {@link MiniGolf#ramp}) */
  @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
   if(c instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ec&&ec.getEntity() instanceof Games.GolfBall)return Shapes.empty();
   return getShape(s,l,p,c);}
 }
 /** keeps a golf ball on the smooth surface of a minigolf ramp; gravity along the 45° slope slows it uphill, speeds it downhill */
 static void ramp(Games.GolfBall b){
  var l=b.level();var p=BlockPos.containing(b.getX(),b.getY()+.01,b.getZ());var s=l.getBlockState(p);
  if(!(s.getBlock() instanceof RampBlock))return;
  var f=s.getValue(HorizontalDirectionalBlock.FACING);
  double t=(b.getX()-p.getX()-.5)*f.getStepX()+(b.getZ()-p.getZ()-.5)*f.getStepZ()+.5,surf=p.getY()+Math.max(0,Math.min(1,t+.12));   // +ball radius: its front edge rides the slope onto the block above
  if(b.getY()>surf+.03)return;
  var v=b.getDeltaMovement();b.setPos(b.getX(),surf,b.getZ());b.setOnGround(true);
  b.setDeltaMovement(v.x-f.getStepX()*.03,0,v.z-f.getStepZ()*.03);}
 /** windmill: FACING = the side with the sails (where the ball comes from); a tunnel runs straight through the base */
 public static final class WindmillBlock extends BaseEntityBlock {
  static final double[][] BOXES={{0,0,0,4,16,16},{12,0,0,16,16,16},{4,7,0,12,16,16}};
  public WindmillBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HorizontalDirectionalBlock.FACING);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,c.getHorizontalDirection().getOpposite());}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return rotated(s.getValue(HorizontalDirectionalBlock.FACING),BOXES);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new WindmillEntity(p,s);}
 }
 public static final class WindmillEntity extends BlockEntity {
  public WindmillEntity(BlockPos p,BlockState s){super(WINDMILL_ENTITY.get(),p,s);}
 }

 // ---------------- putter ----------------
 public static final int FULL=24;
 public static final class PutterItem extends Item {
  public PutterItem(Properties p){super(p);}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
  @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.NONE;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
  static Games.GolfBall ball(Level l,Player p){
   Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();Games.GolfBall best=null;double bd=2.4*2.4;
   for(var b:l.getEntitiesOfClass(Games.GolfBall.class,p.getBoundingBox().inflate(2.4))){double d=b.distanceToSqr(p.getX(),b.getY(),p.getZ());if(d<bd&&b.position().subtract(p.position()).dot(flat)>-.3){bd=d;best=b;}}
   return best;}
  /** client: dotted aiming line from the ball while charging (longer = stronger) */
  @Override public void onUseTick(Level l,LivingEntity e,ItemStack s,int left){
   if(!l.isClientSide||!(e instanceof Player p)||(72000-left)%2!=0)return;var b=ball(l,p);if(b==null)return;
   float power=Mth.clamp((72000-left)/(float)FULL,.05F,1F);Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();
   int n=3+(int)(power*9);for(int i=1;i<=n;i++){Vec3 q=b.position().add(flat.scale(i*.45));l.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1,1-power*.8F,1-power*.9F),.6F),q.x,q.y+.08,q.z,0,0,0);}
  }
  @Override public void releaseUsing(ItemStack s,Level l,LivingEntity e,int left){
   if(l.isClientSide||!(e instanceof Player p))return;
   var b=ball(l,p);if(b==null){p.displayClientMessage(Component.literal("§7Chegue perto da bola de golfe."),true);return;}
   float power=Mth.clamp((72000-left)/(float)FULL,.05F,1F);Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();
   b.setDeltaMovement(flat.scale(.05+power*.62).add(0,.01,0));b.hurtMarked=true;b.hitter=p.getUUID();b.strokes++;
   l.playSound(null,b.blockPosition(),SoundEvents.WOOD_HIT,SoundSource.PLAYERS,.5F,1.9F);
   ((ServerLevel)l).sendParticles(ParticleTypes.CRIT,b.getX(),b.getY()+.1,b.getZ(),3,.05,.05,.05,.05);
   p.displayClientMessage(Component.literal("§f⛳ Tacada "+b.strokes+" §7(força "+Math.round(power*100)+"%)"),true);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Segure o botão direito perto da bola: a linha mostra para onde e com que força vai."));
   t.add(Component.literal("§7Solte para tacar (sempre rasteira)."));}
 }
}
