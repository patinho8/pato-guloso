package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PotabilizerPartBlock extends Block implements EntityBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part",0,11);
    public PotabilizerPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART,1).setValue(MachineBlock.FACING,Direction.NORTH).setValue(MachineBlock.ACTIVE,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(PART,MachineBlock.FACING,MachineBlock.ACTIVE); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return MachinePanel.add(state,PotabilizerStructure.shape(state.getValue(PART),state.getValue(MachineBlock.FACING)));
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PotabilizerPartEntity(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == MareEletrica.POTABILIZER_PART_ENTITY.get()
            ? (world,pos,blockState,entity) -> ((PotabilizerPartEntity)entity).tick() : null;
    }
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.InteractionHand hand,BlockHitResult hit){if(stack.getItem() instanceof WirelessPlateItem || stack.getItem() instanceof WaterShaftItem || stack.is(MareEletrica.MACHINES.get(Kind.GIANT_WHEEL).get().asItem()))return net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;return super.useItemOn(stack,state,level,pos,player,hand,hit);}
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        PotabilizerStructure.interact(level,pos,player); return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(MareEletrica.MACHINES.get(Kind.POTABILIZER).get());
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            MachineEntity m = PotabilizerStructure.machineAt(level,pos);
            if (m != null) level.destroyBlock(m.getBlockPos(),false,player);
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!level.isClientSide && !state.is(next.getBlock())) PotabilizerStructure.partRemoved(level,pos,state);
        super.onRemove(state,level,pos,next,moving);
    }
}
