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

public class LargeMachinePartBlock extends Block implements EntityBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part",0,287);
    public final Kind kind;
    public LargeMachinePartBlock(Kind kind, Properties properties) {
        super(properties); this.kind=kind;
        BlockState state=stateDefinition.any().setValue(partProperty(),1).setValue(MachineBlock.FACING,Direction.NORTH).setValue(MachineBlock.ACTIVE,false).setValue(MachineBlock.EXPANDED,false);
        if(holdsWater())state=state.setValue(MachineBlock.WATERLOGGED,false);
        registerDefaultState(state);
    }
    public IntegerProperty partProperty(){return PART;}
    protected boolean holdsWater(){return false;}
    public static int part(BlockState s){return s.getValue(((LargeMachinePartBlock)s.getBlock()).partProperty());}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(partProperty(),MachineBlock.FACING,MachineBlock.ACTIVE,MachineBlock.EXPANDED);if(holdsWater())builder.add(MachineBlock.WATERLOGGED); }
    @Override protected net.minecraft.world.level.material.FluidState getFluidState(BlockState s){return holdsWater()&&s.getValue(MachineBlock.WATERLOGGED)?net.minecraft.world.level.material.Fluids.WATER.getSource(false):super.getFluidState(s);}
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,net.minecraft.world.level.LevelAccessor l,BlockPos p,BlockPos np){if(holdsWater()&&s.getValue(MachineBlock.WATERLOGGED))l.scheduleTick(p,net.minecraft.world.level.material.Fluids.WATER,5);return super.updateShape(s,d,n,l,p,np);}
    public boolean canPlaceLiquid(Player p,BlockGetter l,BlockPos pos,BlockState s,net.minecraft.world.level.material.Fluid f){return holdsWater()&&!s.getValue(MachineBlock.WATERLOGGED)&&f==net.minecraft.world.level.material.Fluids.WATER;}
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return MachinePanel.add(state,LargeMachineStructure.shape(kind,part(state),state.getValue(MachineBlock.FACING),state.getValue(MachineBlock.EXPANDED),LargeMachineStructure.wide(state)));
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LargeMachinePartEntity(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == MareEletrica.LARGE_PART_ENTITY.get()
            ? (world,pos,blockState,entity) -> ((LargeMachinePartEntity)entity).tick() : null;
    }
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.InteractionHand hand,BlockHitResult hit){if(stack.getItem() instanceof WirelessPlateItem || stack.getItem() instanceof WaterShaftItem || stack.is(MareEletrica.MACHINES.get(Kind.GIANT_WHEEL).get().asItem()))return net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;return super.useItemOn(stack,state,level,pos,player,hand,hit);}
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if(kind==Kind.GIANT_WHEEL)return ControlMachines.turnCrank(level,pos,player)?InteractionResult.sidedSuccess(level.isClientSide):InteractionResult.PASS;
        PotabilizerStructure.interact(level,pos,player); return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(MareEletrica.MACHINES.get(kind).get());
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            MachineEntity m = PotabilizerStructure.machineAt(level,pos);
            if (m != null) level.destroyBlock(m.getBlockPos(),false,player);
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!level.isClientSide && !state.is(next.getBlock())) LargeMachineStructure.partRemoved(level,pos,state);
        super.onRemove(state,level,pos,next,moving);
    }
}
