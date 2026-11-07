package br.mareeletrica;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
public final class LedBedBlock extends BedBlock {
 public static final BooleanProperty LIT=BooleanProperty.create("lit");
 public static final net.minecraft.world.level.block.state.properties.IntegerProperty COLOR=net.minecraft.world.level.block.state.properties.IntegerProperty.create("color",0,6);
 public static final int[] COLORS={0xFF4040,0xFF9B32,0xFFE34A,0x4BFF68,0x3EEBFF,0x5268FF,0xE34FFF};
 public LedBedBlock(Properties props){super(DyeColor.PURPLE,props);registerDefaultState(defaultBlockState().setValue(LIT,true).setValue(COLOR,0));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b){super.createBlockStateDefinition(b);b.add(LIT,COLOR);}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new LedBedEntity(p,s);}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
 @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){super.onPlace(s,l,p,old,moving);if(!l.isClientSide&&s.getValue(PART)==BedPart.HEAD&&!old.is(this))l.scheduleTick(p,this,10);}
 @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource random){
  if(s.getValue(PART)!=BedPart.HEAD)return;
  if(!s.getValue(LIT)){l.setBlock(p,s.setValue(LIT,true),2);BlockPos foot=p.relative(s.getValue(FACING).getOpposite());BlockState f=l.getBlockState(foot);if(f.is(this))l.setBlock(foot,f.setValue(LIT,true),2);}
  l.scheduleTick(p,this,200);
 }
}
