package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Scoreboard: a big LED board for two sides. Right-click the left half for HOME +1, the right half for AWAY +1,
 *  Shift + right-click a half for -1, right-click the middle to switch normal/tennis counting, Shift + middle resets. */
public final class Scoreboard {
 public static final DeferredBlock<BoardBlock> BLOCK=MareEletrica.BLOCKS.register("scoreboard",()->new BoardBlock(BlockBehaviour.Properties.of().strength(2F).sound(SoundType.METAL).noOcclusion().lightLevel(s->6)));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("scoreboard",()->new BlockItem(BLOCK.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Board>> ENTITY=MareEletrica.ENTITIES.register("scoreboard",()->BlockEntityType.Builder.of(Board::new,BLOCK.get()).build(null));
 private Scoreboard(){}
 public static void register(){}
 /** Nearest scoreboard within the radius (loaded chunks only), or null. */
 public static Board nearest(Level l,BlockPos c,int r){
  Board best=null;double bd=Double.MAX_VALUE;
  for(int cx=(c.getX()-r)>>4;cx<=(c.getX()+r)>>4;cx++)for(int cz=(c.getZ()-r)>>4;cz<=(c.getZ()+r)>>4;cz++){
   if(!l.hasChunk(cx,cz))continue;
   for(var be:l.getChunk(cx,cz).getBlockEntities().values())if(be instanceof Board b){double d=b.getBlockPos().distSqr(c);if(d<=r*r&&d<bd){bd=d;best=b;}}
  }
  return best;
 }

 public static final class BoardBlock extends HorizontalDirectionalBlock implements EntityBlock,Multiblock.Master {
  static final double[][] BOXES={{-16,2,12.5,32,23,15.5},{-12,0,13,-10,2,15},{26,0,13,28,2,15},{7,0,13,9,2,15}};
  @Override public java.util.List<BlockPos> cells(BlockState s){return Multiblock.cellsOf(s.getValue(FACING),BOXES);}
  @Override public VoxelShape cellShape(BlockState s,BlockPos o){return Multiblock.slice(s.getValue(FACING),BOXES,o);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock()))Multiblock.clear(l,p,s);super.onRemove(s,l,p,n,moving);}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack st){Multiblock.place(l,p,s);}

  public static final MapCodec<BoardBlock> CODEC=simpleCodec(BoardBlock::new);
  private static final VoxelShape N=Block.box(0,0,12,16,16,16),S=Block.box(0,0,0,16,16,4),E=Block.box(0,0,0,4,16,16),W=Block.box(12,0,0,16,16,16);
  public BoardBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  /** The screen faces the player who places it. */
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var st=defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());return Multiblock.fits(c,st)?st:null;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return cellShape(s,BlockPos.ZERO);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Board(p,s);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(l.isClientSide||!(l.getBlockEntity(p) instanceof Board b))return InteractionResult.sidedSuccess(l.isClientSide);
   Direction f=s.getValue(FACING);
   // horizontal position across the screen, 0 = left edge seen from the front, 1 = right edge (the board is 3 blocks wide)
   var local=hit.getLocation().subtract(p.getX()+.5,0,p.getZ()+.5);
   var right=f.getCounterClockWise();
   double across=(local.x*right.getStepX()+local.z*right.getStepZ()+1.5)/3.0;
   boolean shift=player.isShiftKeyDown();
   if(across<.4)b.point(0,shift?-1:1);
   else if(across>.6)b.point(1,shift?-1:1);
   else if(shift)b.reset();
   else b.toggleMode();
   l.playSound(null,p,SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,.8F,shift?.8F:1.4F);
   player.displayClientMessage(Component.literal(b.summary()),true);
   return InteractionResult.CONSUME;
  }
 }

 public static final class Board extends BlockEntity {
  /** Normal mode: points. Tennis mode: points 0..4 (0/15/30/40/AD) plus games and sets. */
  public int[] points=new int[2],games=new int[2],sets=new int[2];public boolean tennis;
  public Board(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  public static final String[] TENNIS={"0","15","30","40","AD"};
  public String pointText(int side){
   if(!tennis)return Integer.toString(points[side]);
   if(points[0]>=3&&points[1]>=3){if(points[side]==points[1-side])return "40";return points[side]>points[1-side]?"AD":"40";}
   return TENNIS[Math.min(4,points[side])];
  }
  public void point(int side,int delta){
   if(!tennis){points[side]=Math.max(0,Math.min(999,points[side]+delta));changed();return;}
   if(delta<0){points[side]=Math.max(0,points[side]-1);changed();return;}
   points[side]++;
   int a=points[side],o=points[1-side];
   if(a>=4&&a-o>=2){points[0]=points[1]=0;games[side]++;
    int g=games[side],og=games[1-side];
    if(g>=6&&g-og>=2||g==7){games[0]=games[1]=0;sets[side]++;}
   }
   changed();
  }
  void reset(){points=new int[2];games=new int[2];sets=new int[2];changed();}
  void toggleMode(){tennis=!tennis;reset();}
  String summary(){return tennis?"Tênis  •  sets "+sets[0]+"-"+sets[1]+"  games "+games[0]+"-"+games[1]+"  pontos "+pointText(0)+"-"+pointText(1):"Placar "+points[0]+" x "+points[1];}
  private void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putIntArray("Points",points);t.putIntArray("Games",games);t.putIntArray("Sets",sets);t.putBoolean("Tennis",tennis);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);points=fix(t.getIntArray("Points"));games=fix(t.getIntArray("Games"));sets=fix(t.getIntArray("Sets"));tennis=t.getBoolean("Tennis");}
  private static int[] fix(int[] a){return a.length==2?a:new int[2];}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
  @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }
}
