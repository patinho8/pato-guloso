package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/** Hydro interiors let naturally flowing water enter without replacing their parts. */
final class HydroWater {
    private HydroWater() {}

    static boolean canFill(BlockState state, Fluid fluid) {
        return !state.getValue(MachineBlock.WATERLOGGED)
            && (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER);
    }

    static boolean fill(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        if (!canFill(state, fluid.getType())) return false;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(MachineBlock.WATERLOGGED, true), 3);
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return true;
    }
}
