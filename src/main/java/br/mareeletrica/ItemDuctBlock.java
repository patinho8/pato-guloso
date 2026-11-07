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

public final class ItemDuctBlock extends Block implements SimpleWaterloggedBlock, net.minecraft.world.level.block.EntityBlock {
    public static final BooleanProperty INTAKE=BooleanProperty.create("intake");
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty SOURCE=BlockStateProperties.FACING;
    public static final Map<Direction, BooleanProperty> CONNECTIONS = new EnumMap<>(Direction.class);
    static { for (Direction d : Direction.values()) CONNECTIONS.put(d, BooleanProperty.create(d.getName())); }
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape CENTER = box(6, 6, 6, 10, 10, 10);
    public ItemDuctBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any().setValue(WATERLOGGED,false).setValue(INTAKE,false).setValue(SOURCE,Direction.NORTH);
        for (BooleanProperty property : CONNECTIONS.values()) state = state.setValue(property, false);
        registerDefaultState(state);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED,INTAKE,SOURCE);
        for (BooleanProperty property : CONNECTIONS.values()) builder.add(property);
    }
    private static boolean connects(BlockState state) {
        if (state.getBlock() instanceof MachineBlock machine) return machine.kind.storage();
        return state.getBlock() instanceof ItemDuctBlock || state.hasBlockEntity();
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        for (Direction direction : Direction.values()) state = state.setValue(CONNECTIONS.get(direction), connects(context.getLevel().getBlockState(context.getClickedPos().relative(direction))));
        BlockPos source=context.getClickedPos().relative(context.getClickedFace().getOpposite());
        if(context.getLevel().getBlockState(source).hasBlockEntity()&&!(context.getLevel().getBlockState(source).getBlock() instanceof ItemDuctBlock))state=state.setValue(INTAKE,true).setValue(SOURCE,context.getClickedFace().getOpposite());
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
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ItemDuctEntity(p,s);}
    @Override public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level l,BlockState s,net.minecraft.world.level.block.entity.BlockEntityType<T> type){return !l.isClientSide&&type==MareEletrica.ITEM_DUCT_ENTITY.get()?(world,pos,state,e)->((ItemDuctEntity)e).tick():null;}
    @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState s,net.minecraft.world.level.Level l,BlockPos p,net.minecraft.world.entity.player.Player player,net.minecraft.world.phys.BlockHitResult hit){if(!l.isClientSide){boolean input=!s.getValue(INTAKE);l.setBlock(p,s.setValue(INTAKE,input).setValue(SOURCE,hit.getDirection().getOpposite()),3);player.displayClientMessage(net.minecraft.network.chat.Component.literal(input?"Extração: lado "+hit.getDirection().getOpposite().getName():"Duto: transporte"),true);}return net.minecraft.world.InteractionResult.sidedSuccess(l.isClientSide);}
    @Override protected void onRemove(BlockState s,net.minecraft.world.level.Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&l.getBlockEntity(p) instanceof ItemDuctEntity e)net.minecraft.world.Containers.dropItemStack(l,p.getX()+.5,p.getY()+.5,p.getZ()+.5,e.buffer.getStackInSlot(0));super.onRemove(s,l,p,next,moving);}

}
