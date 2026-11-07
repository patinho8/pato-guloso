package br.mareeletrica;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Big blocks with real hitboxes: the master block fills the rest of its footprint with invisible filler blocks that
 *  take their shape from the master, forward clicks to it and break it when mined. */
public final class Multiblock {
 public static final IntegerProperty OX=IntegerProperty.create("ox",0,6),OY=IntegerProperty.create("oy",0,5),OZ=IntegerProperty.create("oz",0,6);
 public static final DeferredBlock<Filler> FILLER=MareEletrica.BLOCKS.register("multiblock_filler",()->new Filler(BlockBehaviour.Properties.of().strength(3F,8F).sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK).dynamicShape()));
 private Multiblock(){}
 public static void register(){}

 /** A block that owns filler cells. Offsets are world offsets from the master (already turned to its facing). */
 public interface Master {
  List<BlockPos> cells(BlockState master);
  /** Shape of a filler cell, in that cell's own coordinates. */
  VoxelShape cellShape(BlockState master,BlockPos offset);
  default VoxelShape cellCollision(BlockState master,BlockPos offset){return cellShape(master,offset);}
 }

 /** A box given in the "facing north" frame (pixels) turned to the facing. */
 public static VoxelShape box(Direction f,double x0,double y0,double z0,double x1,double y1,double z1){
  return switch(f){
   case SOUTH->Block.box(16-x1,y0,16-z1,16-x0,y1,16-z0);
   case EAST->Block.box(16-z1,y0,x0,16-z0,y1,x1);
   case WEST->Block.box(z0,y0,16-x1,z1,y1,16-x0);
   default->Block.box(x0,y0,z0,x1,y1,z1);
  };
 }
 /** Offset (right, up, back) in the "facing north" frame turned to the facing. */
 public static BlockPos offset(Direction f,int right,int up,int back){
  return switch(f){case SOUTH->new BlockPos(-right,up,-back);case EAST->new BlockPos(-back,up,right);case WEST->new BlockPos(back,up,-right);default->new BlockPos(right,up,back);};
 }
 /** Inverse of {@link #offset}: (right, up, back) of a world offset. */
 public static int[] local(Direction f,BlockPos o){
  return switch(f){case SOUTH->new int[]{-o.getX(),o.getY(),-o.getZ()};case EAST->new int[]{o.getZ(),o.getY(),-o.getX()};case WEST->new int[]{-o.getZ(),o.getY(),o.getX()};default->new int[]{o.getX(),o.getY(),o.getZ()};};
 }
 /** The part of a big shape (boxes in pixels, "facing north" frame, relative to the master cell) that lies in one cell, turned to the facing. */
 public static VoxelShape slice(Direction f,double[][] boxes,BlockPos worldOffset){
  int[] o=local(f,worldOffset);double cx=o[0]*16,cy=o[1]*16,cz=o[2]*16;VoxelShape out=Shapes.empty();
  for(var b:boxes){
   double x0=Math.max(b[0],cx),y0=Math.max(b[1],cy),z0=Math.max(b[2],cz),x1=Math.min(b[3],cx+16),y1=Math.min(b[4],cy+16),z1=Math.min(b[5],cz+16);
   if(x1-x0<.01||y1-y0<.01||z1-z0<.01)continue;
   out=Shapes.or(out,box(f,x0-cx,y0-cy,z0-cz,x1-cx,y1-cy,z1-cz));
  }
  return out;
 }
 /** Cells covered by a big shape (boxes in pixels, north frame), turned to the facing; the master cell itself is left out. */
 public static List<BlockPos> cellsOf(Direction f,double[][] boxes){
  var out=new java.util.ArrayList<BlockPos>();
  for(int r=-3;r<=3;r++)for(int u=0;u<=5;u++)for(int b=-3;b<=3;b++){if(r==0&&u==0&&b==0)continue;
   double cx=r*16,cy=u*16,cz=b*16;boolean hit=false;
   for(var x:boxes)if(Math.min(x[3],cx+16)-Math.max(x[0],cx)>.01&&Math.min(x[4],cy+16)-Math.max(x[1],cy)>.01&&Math.min(x[5],cz+16)-Math.max(x[2],cz)>.01){hit=true;break;}
   if(hit)out.add(offset(f,r,u,b));}
  return out;
 }
 /** Placement check: every cell must be replaceable. Tells the player when it is not. */
 public static boolean fits(BlockPlaceContext c,BlockState master){
  if(!(master.getBlock() instanceof Master m))return true;
  for(var o:m.cells(master)){var q=c.getClickedPos().offset(o);if(!c.getLevel().isInWorldBounds(q)||!c.getLevel().getBlockState(q).canBeReplaced(c)){
   if(c.getPlayer()!=null&&!c.getLevel().isClientSide)c.getPlayer().displayClientMessage(Component.literal("§eSem espaço: este bloco ocupa mais de um bloco."),true);return false;}}
  return true;
 }
 public static void place(Level l,BlockPos p,BlockState master){
  if(l.isClientSide||!(master.getBlock() instanceof Master m))return;
  for(var o:m.cells(master)){if(o.getX()<-3||o.getX()>3||o.getZ()<-3||o.getZ()>3||o.getY()<0||o.getY()>5)continue;
   l.setBlock(p.offset(o),FILLER.get().defaultBlockState().setValue(OX,o.getX()+3).setValue(OY,o.getY()).setValue(OZ,o.getZ()+3),3);}
 }
 /** Removes the fillers of a master that is going away. */
 public static void clear(Level l,BlockPos p,BlockState master){
  if(l.isClientSide||!(master.getBlock() instanceof Master m))return;
  for(var o:m.cells(master)){var q=p.offset(o);var s=l.getBlockState(q);if(s.is(FILLER.get())&&masterOffset(s).equals(o))l.setBlock(q,Blocks.AIR.defaultBlockState(),3|32);}
 }
 static BlockPos masterOffset(BlockState s){return new BlockPos(s.getValue(OX)-3,s.getValue(OY),s.getValue(OZ)-3);}

 public static final class Filler extends Block {
  public Filler(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(OX,3).setValue(OY,0).setValue(OZ,3));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(OX,OY,OZ);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
  static BlockPos master(BlockState s,BlockPos p){return p.subtract(masterOffset(s));}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var m=l.getBlockState(master(s,p));return m.getBlock() instanceof Master mb?mb.cellShape(m,masterOffset(s)):Shapes.empty();}
  @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var m=l.getBlockState(master(s,p));return m.getBlock() instanceof Master mb?mb.cellCollision(m,masterOffset(s)):Shapes.empty();}
  @Override protected float getShadeBrightness(BlockState s,BlockGetter l,BlockPos p){return 1F;}
  @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   var mp=master(s,p);var m=l.getBlockState(mp);if(!(m.getBlock() instanceof Master))return InteractionResult.PASS;
   return m.useWithoutItem(l,player,hit.withPosition(mp));
  }
  @Override public ItemStack getCloneItemStack(LevelReader l,BlockPos p,BlockState s){var m=l.getBlockState(master(s,p));return m.getBlock() instanceof Master?new ItemStack(m.getBlock()):ItemStack.EMPTY;}
  @Override public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){
   var mp=master(s,p);if(!l.isClientSide&&l.getBlockState(mp).getBlock() instanceof Master)l.destroyBlock(mp,!player.isCreative(),player);
   return super.playerWillDestroy(l,p,s,player);
  }
  /** Lonely fillers (master gone) clean themselves up. */
  @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,net.minecraft.world.level.LevelAccessor l,BlockPos p,BlockPos np){
   return l.getBlockState(master(s,p)).getBlock() instanceof Master?s:Blocks.AIR.defaultBlockState();
  }
 }
}
