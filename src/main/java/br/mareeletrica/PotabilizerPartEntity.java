package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Lightweight proxy. No inventory, energy or independently obtainable item. */
public final class PotabilizerPartEntity extends BlockEntity {
    private MachineEntity previous;
    public PotabilizerPartEntity(BlockPos pos, BlockState state) { super(MareEletrica.POTABILIZER_PART_ENTITY.get(),pos,state); }
    public MachineEntity controller() {
        if (level == null || !(getBlockState().getBlock() instanceof PotabilizerPartBlock)) return null;
        BlockPos root = PotabilizerStructure.root(worldPosition,getBlockState());
        if (!level.hasChunkAt(root)) return null;
        BlockState state = level.getBlockState(root);
        if (!PotabilizerStructure.isRoot(state) || state.getValue(MachineBlock.FACING) != getBlockState().getValue(MachineBlock.FACING)) return null;
        return level.getBlockEntity(root) instanceof MachineEntity m && !m.isRemoved() ? m : null;
    }
    public void tick() {
        if (level == null || level.isClientSide || isRemoved() || !(getBlockState().getBlock() instanceof PotabilizerPartBlock)
            || (level.getGameTime()+worldPosition.asLong()) % 20 != 0) return;
        MachineEntity current = controller();
        if (current != previous) { level.invalidateCapabilities(worldPosition); previous = current; }
        BlockPos root = PotabilizerStructure.root(worldPosition,getBlockState());
        if (!level.hasChunkAt(root)) return;
        if (current == null) { level.removeBlock(worldPosition,false); return; }
        boolean active = current.getBlockState().getValue(MachineBlock.ACTIVE);
        if (getBlockState().getValue(MachineBlock.ACTIVE) != active) level.setBlock(worldPosition,getBlockState().setValue(MachineBlock.ACTIVE,active),2);
    }
}
