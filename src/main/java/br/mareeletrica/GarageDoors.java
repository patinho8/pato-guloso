package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Big roll-up garage door: one item places a 5 wide x 4 high door. Right-click opens/closes it; it also opens by itself
 *  when a vehicle with a driver comes close, and the command panel can open or close all of them. */
public final class GarageDoors {
 public static final BooleanProperty OPEN=BooleanProperty.create("open");
 public static final IntegerProperty ROW=IntegerProperty.create("row",0,3);
 public static final DeferredBlock<GarageDoorBlock> BLOCK=MareEletrica.BLOCKS.register("garage_door",()->new GarageDoorBlock(BlockBehaviour.Properties.of().strength(6F,30F).sound(SoundType.METAL).noOcclusion()));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("garage_door",()->new BlockItem(BLOCK.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Anchor>> ANCHOR=MareEletrica.ENTITIES.register("garage_door",()->BlockEntityType.Builder.of(Anchor::new,BLOCK.get()).build(null));
 private GarageDoors(){}
 public static void register(){}
 static List<BlockPos> cells(BlockPos base,Direction f){var along=f.getClockWise();var out=new ArrayList<BlockPos>();for(int row=0;row<4;row++)for(int i=-2;i<=2;i++)out.add(base.relative(along,i).above(row));return out;}
 /** All door blocks connected to this one (same facing). */
 static Set<BlockPos> connected(Level l,BlockPos start){
  var out=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(start);
  var f=l.getBlockState(start).getValue(HorizontalDirectionalBlock.FACING);
  while(!queue.isEmpty()&&out.size()<40){var p=queue.poll();if(!out.add(p))continue;
   for(var d:new Direction[]{Direction.UP,Direction.DOWN,f.getClockWise(),f.getCounterClockWise()}){var q=p.relative(d);var s=l.getBlockState(q);if(s.is(BLOCK.get())&&s.getValue(HorizontalDirectionalBlock.FACING)==f&&!out.contains(q))queue.add(q);}}
  return out;
 }
 static void setAll(Level l,BlockPos any,boolean open){
  var cells=connected(l,any);boolean changed=false;
  for(var p:cells){var s=l.getBlockState(p);if(s.getValue(OPEN)!=open){l.setBlock(p,s.setValue(OPEN,open),2|16);changed=true;}}
  if(changed)l.playSound(null,any,open?SoundEvents.IRON_DOOR_OPEN:SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,1.5F,.5F);
 }

 public static final class GarageDoorBlock extends HorizontalDirectionalBlock implements EntityBlock {
  public static final MapCodec<GarageDoorBlock> CODEC=simpleCodec(GarageDoorBlock::new);
  private static final VoxelShape NS=Block.box(0,0,6,16,16,10),EW=Block.box(6,0,0,10,16,16),DRUM_NS=Block.box(0,10,4,16,16,12),DRUM_EW=Block.box(4,10,0,12,16,16);
  public GarageDoorBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OPEN,false).setValue(ROW,0));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,OPEN,ROW);}
  private static boolean ns(BlockState s){return s.getValue(FACING).getAxis()==Direction.Axis.Z;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){if(!s.getValue(OPEN))return ns(s)?NS:EW;return s.getValue(ROW)==3?(ns(s)?DRUM_NS:DRUM_EW):Shapes.empty();}
  @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(OPEN)?Shapes.empty():(ns(s)?NS:EW);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){
   var f=c.getHorizontalDirection();
   for(var q:cells(c.getClickedPos(),f))if(!q.equals(c.getClickedPos())&&!c.getLevel().getBlockState(q).canBeReplaced(c)){if(c.getPlayer()!=null&&!c.getLevel().isClientSide)c.getPlayer().displayClientMessage(Component.literal("Precisa de espaço livre: 5 de largura e 4 de altura"),true);return null;}
   return defaultBlockState().setValue(FACING,f);
  }
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,ItemStack stack){
   if(l.isClientSide)return;
   var f=s.getValue(FACING);var along=f.getClockWise();
   for(int row=0;row<4;row++)for(int i=-2;i<=2;i++){var q=p.relative(along,i).above(row);if(!q.equals(p))l.setBlock(q,s.setValue(ROW,row),3);}
  }
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(ROW)==0?new Anchor(p,s):null;}
  @SuppressWarnings("unchecked")
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=ANCHOR.get()?null:(BlockEntityTicker<T>)(BlockEntityTicker<Anchor>)Anchor::tick;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide){
    if(BaseControl.lockdown(l,p)&&!BaseControl.member(BaseControl.panelFor(l,p),player)){player.displayClientMessage(Component.literal("§cTrancado pelo alarme da base"),true);return InteractionResult.CONSUME;}
    boolean open=!s.getValue(OPEN);setAll(l,p,open);
    for(var a:anchors(l,p))a.manual=open;
   }
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  static List<Anchor> anchors(Level l,BlockPos p){var out=new ArrayList<Anchor>();for(var q:connected(l,p))if(l.getBlockEntity(q) instanceof Anchor a)out.add(a);return out;}
  /** Breaking any piece removes the whole door and drops one item. */
  @Override public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){
   if(!l.isClientSide){var cells=connected(l,p);int n=Math.max(1,cells.size()/20);for(var q:cells)if(!q.equals(p))l.setBlock(q,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2|16);
    if(!player.isCreative())for(int i=0;i<n;i++)Block.popResource(l,p,new ItemStack(ITEM.get()));}
   return super.playerWillDestroy(l,p,s,player);
  }
 }
 /** Bottom piece of each column: auto-open when a driven vehicle comes close. */
 public static final class Anchor extends BlockEntity {
  boolean manual,auto;private int idle;
  public Anchor(BlockPos p,BlockState s){super(ANCHOR.get(),p,s);}
  public void set(Level l,boolean open){manual=open;setAll(l,worldPosition,open);}
  static void tick(Level l,BlockPos p,BlockState s,Anchor a){
   if(l.getGameTime()%10!=0)return;
   // only the middle column checks, the others follow it
   var f=s.getValue(HorizontalDirectionalBlock.FACING);
   BlockState left=l.getBlockState(p.relative(f.getCounterClockWise(),1)),right=l.getBlockState(p.relative(f.getClockWise(),1));
   if(!(left.is(BLOCK.get())&&right.is(BLOCK.get())&&l.getBlockState(p.relative(f.getCounterClockWise(),2)).is(BLOCK.get())&&l.getBlockState(p.relative(f.getClockWise(),2)).is(BLOCK.get())))return;
   boolean near=false;var panel=BaseControl.panelFor(l,p);
   for(var v:l.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,new AABB(p).inflate(8,4,8),e->e instanceof Vehicles.Base||e instanceof Jets.JetEntity))
    if(v.getControllingPassenger() instanceof Player pl&&BaseControl.member(panel,pl)||v instanceof Vehicles.Base b&&b.autoTarget!=null){near=true;break;}
   if(near){a.idle=0;if(!s.getValue(OPEN)){a.auto=true;setAll(l,p,true);}}
   else if(a.auto&&!a.manual&&++a.idle>=6){a.auto=false;setAll(l,p,false);}
  }
 }
}
