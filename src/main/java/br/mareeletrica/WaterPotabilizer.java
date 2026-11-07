package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class WaterPotabilizer {
    private WaterPotabilizer() {}
    public static boolean validInput(int slot, ItemStack stack) {
        return slot == 0 ? stack.is(Items.GLASS_BOTTLE) : slot == 1 && (stack.is(Items.COAL) || stack.is(Items.CHARCOAL));
    }
    public static boolean tick(MachineEntity m) {
        if (!(m.getLevel() instanceof ServerLevel level)) return false;
        BlockPos intake = m.getBlockPos().relative(m.getBlockState().getValue(MachineBlock.FACING));
        int profile = 0;
        if (level.hasChunkAt(intake) && level.getBlockState(intake).is(Blocks.WATER) && level.getFluidState(intake).isSource())
            profile = level.getBiome(intake).is(BiomeTags.IS_OCEAN) ? 2 : 1;
        ItemStack result = new ItemStack(MareEletrica.POTABLE_WATER.get()), output = m.inventory.getStackInSlot(8);
        boolean room = (output.isEmpty() || ItemStack.isSameItemSameComponents(output,result)) && output.getCount() < result.getMaxStackSize();
        var step = PotableWaterCycle.step(new PotableWaterCycle.State(m.progress,m.filterLeft,m.waterProfile),profile,m.clean.stored(),
            validInput(0,m.inventory.getStackInSlot(0)),validInput(1,m.inventory.getStackInSlot(1)),room);
        if (step.charcoal() > 0) m.inventory.extractItem(1,step.charcoal(),false);
        if (step.energy() > 0) m.clean.pay(step.energy());
        if (step.bottle()) {
            m.inventory.extractItem(0,1,false);
            m.inventory.setStackInSlot(8,output.isEmpty() ? result : output.copyWithCount(output.getCount()+1));
        }
        // Apply the cycle after inventory callbacks, which can reset progress when a stack empties.
        m.progress=step.state().progress(); m.filterLeft=step.state().filter(); m.waterProfile=step.state().profile();
        m.status=step.status(); m.lastRate=step.energy()*2;
        // Bottles sample the adjacent source like vanilla glass bottles: never erase the river or sea.
        return step.energy() > 0;
    }
}
