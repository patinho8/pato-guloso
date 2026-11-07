package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HologramBlock extends Block {
    public static final IntegerProperty COLOR=IntegerProperty.create("color",0,16);
    public static final IntegerProperty STYLE=IntegerProperty.create("style",0,127);
    private static final VoxelShape[] SHAPES=new VoxelShape[128];
    static { for(int i=0;i<128;i++) {VoxelShape shape=Shapes.empty();for(double[] b:HologramShapes.boxes(i))shape=Shapes.or(shape,Block.box(b[0],b[1],b[2],b[3],b[4],b[5]));SHAPES[i]=shape.optimize();} }
    public HologramBlock(Properties properties) { super(properties);registerDefaultState(stateDefinition.any().setValue(STYLE,0).setValue(COLOR,16)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(STYLE,COLOR); }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter level,BlockPos pos,CollisionContext c) {return SHAPES[s.getValue(STYLE)];}
    @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moving) {
        if(!level.isClientSide)level.scheduleTick(pos,this,40+(int)Math.floorMod(pos.asLong(),20));
    }
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        HologramIndex index=HologramIndex.get(level);
        if(!index.alive(pos,level.getGameTime())) { level.removeBlock(pos,false);index.remove(pos); }
        else level.scheduleTick(pos,this,40);
    }
    @Override protected void onRemove(BlockState old,Level level,BlockPos pos,BlockState next,boolean moving) {
        if(!old.is(next.getBlock())&&level instanceof ServerLevel server)HologramIndex.get(server).remove(pos);
        super.onRemove(old,level,pos,next,moving);
    }
    @Override protected boolean skipRendering(BlockState a,BlockState b,Direction side) {
        return a.getValue(STYLE)<2 && b.is(this)&&a.getValue(STYLE).equals(b.getValue(STYLE)) || super.skipRendering(a,b,side);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        int style=state.getValue(STYLE);if(style<96||style>=104)return InteractionResult.PASS;
        if(!level.isClientSide) {
            int next=style<100?style+4:style-4;level.setBlock(pos,state.setValue(STYLE,next),3);
            for(BlockPos p:new BlockPos[]{pos.above(),pos.below()}) {
                BlockState adjacent=level.getBlockState(p);
                if(adjacent.is(this)&&adjacent.getValue(STYLE)==style)level.setBlock(p,adjacent.setValue(STYLE,next),3);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    public static int style(BlockState state) {
        String id=BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        int turn=state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)?PotabilizerStructure.turn(state.getValue(BlockStateProperties.HORIZONTAL_FACING)):0;
        if(state.getBlock() instanceof StairBlock) {
            int shape=switch(state.getValue(StairBlock.SHAPE)) {case INNER_LEFT->1;case INNER_RIGHT->2;case OUTER_LEFT->3;case OUTER_RIGHT->4;default->0;};
            return 8+shape*8+(state.getValue(BlockStateProperties.HALF)==net.minecraft.world.level.block.state.properties.Half.TOP?4:0)+turn;
        }
        if(id.endsWith("_slab"))return switch(state.getValue(BlockStateProperties.SLAB_TYPE)){case TOP->3;case BOTTOM->2;default->0;};
        if(id.endsWith("_trapdoor"))return state.getValue(BlockStateProperties.HALF)==net.minecraft.world.level.block.state.properties.Half.TOP?111:110;
        if(id.endsWith("_door"))return 96+turn+(state.getValue(BlockStateProperties.OPEN)?4:0);
        int base=id.endsWith("_fence")?48:id.endsWith("_wall")?64:id.endsWith("_pane")||id.equals("iron_bars")?80:-1;
        if(base>=0) {
            int mask=0;String[] names={"north","east","south","west"};
            for(var e:state.getValues().entrySet())for(int i=0;i<4;i++)if(e.getKey().getName().equals(names[i])) {
                String v=e.getValue().toString().toLowerCase(java.util.Locale.ROOT);if(!v.equals("false")&&!v.equals("none"))mask|=1<<i;
            }
            return base+mask;
        }
        if(id.endsWith("_leaves"))return 1;if(id.endsWith("_carpet"))return 104;
        if(id.contains("torch"))return 105;if(id.endsWith("_bed"))return 106;if(id.equals("farmland")||id.equals("dirt_path"))return 107;
        if(id.equals("lantern")||id.equals("soul_lantern"))return 109;if(state.getBlock() instanceof net.minecraft.world.level.block.BushBlock)return 108;
        return 0;
    }
}
