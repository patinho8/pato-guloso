package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Automatic gate: opens by itself when a player comes close (or with a redstone signal) and closes again after they pass.
 *  Place several side by side for a wide gate. Right-click switches between automatic, always open and locked. */
public final class AutoGate {
 public static final DeferredBlock<GateBlock> BLOCK=MareEletrica.BLOCKS.register("auto_gate",()->new GateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5F,20F).noOcclusion().sound(SoundType.METAL)));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("auto_gate",()->new BlockItem(BLOCK.get(),new Item.Properties()));
 private AutoGate(){}
 public static void register(){}

 public static final class GateBlock extends HorizontalDirectionalBlock {
  public static final MapCodec<GateBlock> CODEC=simpleCodec(GateBlock::new);
  public static final BooleanProperty OPEN=BooleanProperty.create("open");
  /** 0 = automatic, 1 = always open, 2 = locked. */
  public static final net.minecraft.world.level.block.state.properties.IntegerProperty MODE=net.minecraft.world.level.block.state.properties.IntegerProperty.create("mode",0,2);
  private static final VoxelShape NS=Block.box(0,0,6.5,16,16,9.5),EW=Block.box(6.5,0,0,9.5,16,16),TRACK_NS=Block.box(0,0,6,16,1,10),TRACK_EW=Block.box(6,0,0,10,1,16);
  public GateBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OPEN,false).setValue(MODE,0));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,OPEN,MODE);}
  /** One item places the whole gate: 3 blocks wide and 2 high, centred on where you click. */
  private static java.util.List<BlockPos> cells(BlockPos p,Direction facing){var along=facing.getClockWise();var out=new java.util.ArrayList<BlockPos>();for(int dy=0;dy<2;dy++)for(int dx=-1;dx<=1;dx++)out.add(p.relative(along,dx).above(dy));return out;}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){
   var facing=c.getHorizontalDirection();
   for(var q:cells(c.getClickedPos(),facing))if(!q.equals(c.getClickedPos())&&!c.getLevel().getBlockState(q).canBeReplaced(c)||!c.getLevel().isInWorldBounds(q)){if(c.getPlayer()!=null&&!c.getLevel().isClientSide)c.getPlayer().displayClientMessage(Component.literal("Precisa de espaço livre: 3 de largura e 2 de altura"),true);return null;}
   return defaultBlockState().setValue(FACING,facing);
  }
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack stack){
   if(l.isClientSide)return;
   for(var q:cells(p,s.getValue(FACING)))if(!q.equals(p))l.setBlock(q,s,3);
  }
  /** Breaking any piece removes the whole gate; it drops a single item. */
  @Override public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){
   if(!l.isClientSide)for(var q:row(l,p,s))if(!q.equals(p))l.setBlock(q,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),35);
   return super.playerWillDestroy(l,p,s,player);
  }
  private static boolean ns(BlockState s){return s.getValue(FACING).getAxis()==Direction.Axis.Z;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(OPEN)?(ns(s)?TRACK_NS:TRACK_EW):(ns(s)?NS:EW);}   // facing = the way you walk through: the panel spans across it
  @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(OPEN)?Shapes.empty():(ns(s)?Block.box(0,0,6.5,16,24,9.5):Block.box(6.5,0,0,9.5,24,16));}
  @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(!l.isClientSide)l.scheduleTick(p,this,5);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide){
    int mode=(s.getValue(MODE)+1)%3;
    // change the whole row at once
    for(var q:row(l,p,s))l.setBlock(q,l.getBlockState(q).setValue(MODE,mode),3);
    player.displayClientMessage(Component.literal("Portão: "+(mode==0?"automático":mode==1?"sempre aberto":"trancado")),true);
    l.scheduleTick(p,this,1);
   }
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  /** Gate blocks in line with this one (same direction), up to 8 each way, plus the ones stacked on top. */
  private java.util.List<BlockPos> row(Level l,BlockPos p,BlockState s){
   var out=new java.util.ArrayList<BlockPos>();var along=s.getValue(FACING).getClockWise();
   BlockPos base=p;while(l.getBlockState(base.below()).is(this)&&p.getY()-base.getY()<4)base=base.below();
   for(int dir=-1;dir<=1;dir+=2)for(int i=dir<0?0:1;i<=8;i++){var q=base.relative(along,i*dir);if(!l.getBlockState(q).is(this))break;for(int h=0;h<4&&l.getBlockState(q.above(h)).is(this);h++)out.add(q.above(h));}
   return out;
  }
  @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){
   int mode=s.getValue(MODE);
   boolean want=mode==1||mode==0&&(l.hasNeighborSignal(p)||!l.getEntitiesOfClass(Player.class,new AABB(p).inflate(3.5,2,3.5),pl->!pl.isSpectator()).isEmpty());
   if(want!=s.getValue(OPEN)){
    l.setBlock(p,s.setValue(OPEN,want),3);
    l.playSound(null,p,want?SoundEvents.PISTON_EXTEND:SoundEvents.PISTON_CONTRACT,SoundSource.BLOCKS,.5F,want?1.2F:.9F);
   }
   l.scheduleTick(p,this,want?20:5);   // stays open a second after you pass
  }
 }
}
