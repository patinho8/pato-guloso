package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Lightweight proxy. No inventory, energy or independently obtainable item. */
public final class LargeMachinePartEntity extends BlockEntity {
    private MachineEntity previous;
    public LargeMachinePartEntity(BlockPos pos, BlockState state) { super(MareEletrica.LARGE_PART_ENTITY.get(),pos,state); }
    public MachineEntity controller() {
        if (level == null || !(getBlockState().getBlock() instanceof LargeMachinePartBlock)) return null;
        if (LargeMachinePartBlock.part(getBlockState())>=LargeMachineStructure.parts(((LargeMachinePartBlock)getBlockState().getBlock()).kind,getBlockState())) return null;
        BlockPos root = LargeMachineStructure.root(worldPosition,getBlockState());
        if (!level.hasChunkAt(root)) return null;
        BlockState state = level.getBlockState(root);
        if (state.hasProperty(MachineBlock.EXPANDED) && state.getValue(MachineBlock.EXPANDED)!=getBlockState().getValue(MachineBlock.EXPANDED)) return null;
        if (LargeMachineStructure.wide(state)!=LargeMachineStructure.wide(getBlockState())) return null;
        if (!LargeMachineStructure.isRoot(state,((LargeMachinePartBlock)getBlockState().getBlock()).kind) || state.getValue(MachineBlock.FACING) != getBlockState().getValue(MachineBlock.FACING)) return null;
        return level.getBlockEntity(root) instanceof MachineEntity m && !m.isRemoved() ? m : null;
    }
    public void tick() {
        if (level == null || level.isClientSide || isRemoved() || !(getBlockState().getBlock() instanceof LargeMachinePartBlock)
            || (level.getGameTime()+worldPosition.asLong()) % 20 != 0) return;
        MachineEntity current = controller();
        if (current != previous) { level.invalidateCapabilities(worldPosition); previous = current; }
        BlockPos root = LargeMachineStructure.root(worldPosition,getBlockState());
        if (!level.hasChunkAt(root)) return;
        if (current == null) { level.removeBlock(worldPosition,false); return; }
        boolean active = current.getBlockState().getValue(MachineBlock.ACTIVE);
        if (getBlockState().getValue(MachineBlock.ACTIVE) != active) level.setBlock(worldPosition,getBlockState().setValue(MachineBlock.ACTIVE,active),2);
    }
}
