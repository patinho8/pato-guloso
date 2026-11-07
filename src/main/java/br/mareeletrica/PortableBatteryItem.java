package br.mareeletrica;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** A portable clean-power reserve; charge it in the station, then use it on a machine. */
public final class PortableBatteryItem extends Item {
    public static final int CAPACITY = 16_000;
    public PortableBatteryItem(Properties properties) { super(properties.stacksTo(1)); }
    private static int stored(ItemStack stack) {
        return Math.max(0, Math.min(CAPACITY, stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("CleanEnergy")));
    }
    private static void write(ItemStack stack, int value) {
        var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putInt("CleanEnergy", Math.max(0, Math.min(CAPACITY, value)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
    public static IEnergyStorage energy(ItemStack stack) {
        return new IEnergyStorage() {
            @Override public int receiveEnergy(int max, boolean simulate) {
                int accepted = Math.min(Math.max(0, max), CAPACITY - stored(stack));
                if (!simulate && accepted > 0) write(stack, stored(stack) + accepted);
                return accepted;
            }
            @Override public int extractEnergy(int max, boolean simulate) {
                int extracted = Math.min(Math.max(0, max), stored(stack));
                if (!simulate && extracted > 0) write(stack, stored(stack) - extracted);
                return extracted;
            }
            @Override public int getEnergyStored() { return stored(stack); }
            @Override public int getMaxEnergyStored() { return CAPACITY; }
            @Override public boolean canExtract() { return true; }
            @Override public boolean canReceive() { return true; }
        };
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        MachineEntity machine=PotabilizerStructure.machineAt(level,context.getClickedPos());
        if (machine == null || !machine.energyPort.canReceive()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            int moved = machine.energyPort.receiveEnergy(Math.min(1_000, stored(stack)), false);
            if (moved > 0) write(stack, stored(stack) - moved);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public boolean isBarVisible(ItemStack stack) { return stored(stack) > 0; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13F * stored(stack) / CAPACITY); }
    @Override public int getBarColor(ItemStack stack) { return 0x38DBC5; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("hint.mareeletrica.battery", stored(stack)).withStyle(ChatFormatting.AQUA));
    }
}
