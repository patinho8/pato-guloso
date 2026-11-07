package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Security camera: placed on any face, recorded with a pendrive and viewed from the Power Controller. */
public final class Cameras {
 public static final DeferredBlock<CameraBlock> CAMERA=MareEletrica.BLOCKS.register("security_camera",()->new CameraBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).noOcclusion().sound(SoundType.METAL)));
 public static final DeferredItem<BlockItem> CAMERA_ITEM=MareEletrica.ITEMS.register("security_camera",()->new BlockItem(CAMERA.get(),new Item.Properties()));
 private Cameras(){}
 public static void register(){}

 /** FACING = the direction the lens looks (away from the block it hangs on). */
 public static final class CameraBlock extends DirectionalBlock {
  public static final MapCodec<CameraBlock> CODEC=simpleCodec(CameraBlock::new);
  private static final VoxelShape UP=Block.box(4.5,0,2,11.5,15,14),DOWN=Block.box(4.5,1,2,11.5,16,14),N=Block.box(4.5,6.5,1,11.5,14,16),S=Block.box(4.5,6.5,0,11.5,14,15),E=Block.box(0,6.5,4.5,15,14,11.5),W=Block.box(1,6.5,4.5,16,14,11.5);
  public CameraBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends DirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState().setValue(FACING,c.getClickedFace());return s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
  @Override protected boolean canSurvive(BlockState s,LevelReader level,BlockPos pos){var support=pos.relative(s.getValue(FACING).getOpposite());return level.getBlockState(support).isFaceSturdy(level,support,s.getValue(FACING));}
  @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,net.minecraft.world.level.LevelAccessor level,BlockPos pos,BlockPos np){return d==s.getValue(FACING).getOpposite()&&!s.canSurvive(level,pos)?net.minecraft.world.level.block.Blocks.AIR.defaultBlockState():super.updateShape(s,d,n,level,pos,np);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(FACING)){case UP->UP;case DOWN->DOWN;case NORTH->N;case SOUTH->S;case EAST->E;case WEST->W;};}
 }
}
