package br.mareeletrica;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CableBlock extends Block implements SimpleWaterloggedBlock {
    public static final Map<Direction, BooleanProperty> CONNECTIONS = new EnumMap<>(Direction.class);
    static { for (Direction d : Direction.values()) CONNECTIONS.put(d, BooleanProperty.create(d.getName())); }
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape CENTER = box(6, 6, 6, 10, 10, 10);
    public final boolean cyber;
    /** Energy each cable block carries per ten-tick window. */
    public final int capacity;
    /** Transformers step the voltage down to this level (4 = cables do not change it). */
    public int voltageLimit=4;
    public CableBlock limit(int v){voltageLimit=v;return this;}
    public CableBlock(Properties properties) { this(properties,false); }
    public CableBlock(Properties properties,boolean cyber) { this(properties,cyber,cyber?PowerNetwork.CYBER:PowerNetwork.NORMAL); }
    public CableBlock(Properties properties,boolean cyber,int capacity) {
        super(properties); this.cyber=cyber; this.capacity=capacity;
        BlockState state = stateDefinition.any().setValue(WATERLOGGED, false);
        for (BooleanProperty property : CONNECTIONS.values()) state = state.setValue(property, false);
        registerDefaultState(state);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
        for (BooleanProperty property : CONNECTIONS.values()) builder.add(property);
    }
    private static boolean connects(BlockState state) { return state.getBlock() instanceof CableBlock || state.getBlock() instanceof MachineBlock || state.getBlock() instanceof PotabilizerPartBlock || state.getBlock() instanceof LargeMachinePartBlock; }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        for (Direction direction : Direction.values()) state = state.setValue(CONNECTIONS.get(direction), connects(context.getLevel().getBlockState(context.getClickedPos().relative(direction))));
        return state;
    }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return state.setValue(CONNECTIONS.get(direction), connects(neighbor));
    }
    @Override protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape result = CENTER;
        for (Direction d : Direction.values()) {
            if (!state.getValue(CONNECTIONS.get(d))) continue;
            VoxelShape arm = switch(d) {
                case DOWN -> box(6, 0, 6, 10, 6, 10);
                case UP -> box(6, 10, 6, 10, 16, 10);
                case NORTH -> box(6, 6, 0, 10, 10, 6);
                case SOUTH -> box(6, 6, 10, 10, 10, 16);
                case WEST -> box(0, 6, 6, 6, 10, 10);
                case EAST -> box(10, 6, 6, 16, 10, 10);
            };
            result = Shapes.or(result, arm);
        }
        return result;
    }
}
