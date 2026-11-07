package br.mareeletrica;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;

/** Public FE is always clean. Dirty power is never exposed as usable FE. */
public final class MachinePorts {
    private MachinePorts() {}
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,MareEletrica.POTABILIZER_PART_ENTITY.get(),(part,side) -> {
            MachineEntity controller=part.controller(); return controller == null ? null : controller.energyPort;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,MareEletrica.POTABILIZER_PART_ENTITY.get(),(part,side) -> {
            MachineEntity controller=part.controller(); return controller == null ? null : controller.itemPort;
        });
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,MareEletrica.LARGE_PART_ENTITY.get(),(part,side) -> {
            MachineEntity controller=part.controller(); return controller == null ? null : controller.energyPort;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,MareEletrica.LARGE_PART_ENTITY.get(),(part,side) -> {
            MachineEntity controller=part.controller(); return controller == null ? null : controller.itemPort;
        });
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> PortableBatteryItem.energy(stack), MareEletrica.PORTABLE_BATTERY.get());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MareEletrica.MACHINE_ENTITY.get(),
            (machine, side) -> machine.kind.core() || machine.kind.consumer ? machine.energyPort : null);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MareEletrica.MACHINE_ENTITY.get(),
            (machine, side) -> machine.kind.storage() ? machine.itemPort : null);
    }
    public static IEnergyStorage energy(MachineEntity machine) {
        return new IEnergyStorage() {
            @Override public int receiveEnergy(int requested, boolean simulate) {
                if (!canReceive()) return 0;
                int accepted = Math.min(Math.max(0, requested), machine.clean.room());
                if (!simulate && accepted > 0) { machine.clean.add(accepted); machine.setChanged(); }
                return accepted;
            }
            @Override public int extractEnergy(int requested, boolean simulate) {
                if (!canExtract()) return 0;
                int extracted = Math.min(Math.max(0, requested), machine.clean.stored());
                if (!simulate && extracted > 0) { machine.clean.take(extracted); machine.setChanged(); }
                return extracted;
            }
            @Override public int getEnergyStored() { return machine.clean.stored(); }
            @Override public int getMaxEnergyStored() { return machine.clean.capacity(); }
            @Override public boolean canExtract() { return !machine.isRemoved() && machine.enabled && machine.kind.core(); }
            @Override public boolean canReceive() { return !machine.isRemoved() && machine.enabled && (machine.kind.consumer || machine.kind.core()); }
        };
    }
    public static IItemHandler items(MachineEntity machine) {
        return new IItemHandler() {
            @Override public int getSlots() { return 9; }
            @Override public ItemStack getStackInSlot(int slot) { return machine.inventory.getStackInSlot(slot); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (machine.isRemoved() || machine.kind.outputInventory()) return stack;
                return machine.inventory.insertItem(slot, stack, simulate);
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (machine.isRemoved() || !(machine.kind.outputInventory() || machine.kind == Kind.BUILDER || (machine.kind == Kind.GREENHOUSE && slot == 7) || machine.kind == Kind.PLANTER || machine.kind == Kind.ANIMAL_FEEDER || machine.kind == Kind.FOOD_STATION || machine.kind == Kind.CHARGER || (machine.kind.processor() && slot == 8))) return ItemStack.EMPTY;
                return machine.inventory.extractItem(slot, amount, simulate);
            }
            @Override public int getSlotLimit(int slot) { return machine.inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return !machine.kind.outputInventory() && machine.inventory.isItemValid(slot, stack); }
        };
    }
}
