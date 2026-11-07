package br.mareeletrica;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
/** A narrow falling stream, renewed by a working outlet; it contains no extra water. */
public final class FaucetStreamBlock extends Block {
 public static final IntegerProperty AGE=IntegerProperty.create("age",0,3);
 public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
 public FaucetStreamBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(AGE,0).setValue(FACING,net.minecraft.core.Direction.DOWN));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(AGE,FACING);}
 protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return net.minecraft.world.phys.shapes.Shapes.empty();}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(!l.isClientSide)l.scheduleTick(p,this,10);}
 protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(s.getValue(AGE)==3)l.removeBlock(p,false);else{l.setBlock(p,s.setValue(AGE,s.getValue(AGE)+1),2);l.scheduleTick(p,this,10);}}
}
