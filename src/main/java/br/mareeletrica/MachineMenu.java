package br.mareeletrica;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MachineMenu extends AbstractContainerMenu {
    public final ContainerData data;
    private final MachineEntity machine;
    public MachineMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, null, new ItemStackHandler(9), new SimpleContainerData(MachineEntity.DATA_COUNT));
    }
    public MachineMenu(int id, Inventory playerInventory, MachineEntity machine) {
        this(id, playerInventory, machine, machine.inventory, machine.data);
        if (locked(playerInventory.player, 0)) playerInventory.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cSó o dono desta doca pode mexer na munição."), true);
    }
    private MachineMenu(int id, Inventory playerInventory, MachineEntity machine, IItemHandler items, ContainerData data) {
        super(MareEletrica.MACHINE_MENU.get(), id);
        this.machine = machine; this.data = data;
        checkContainerDataCount(data, MachineEntity.DATA_COUNT);
        for (int i = 0; i < 9; i++) {
            final int index = i;
            addSlot(new SlotItemHandler(items, i, 24 + i * 18, 103) {
                @Override public boolean isActive() { return slotVisible(index); }
                @Override public boolean mayPickup(Player p) { return !locked(p, index) && super.mayPickup(p); }
                @Override public boolean mayPlace(ItemStack stack) {
                    return !locked(playerInventory.player, index) && kind() != Kind.STONE_GENERATOR && !kind().outputInventory() && slotVisible(index)
                        && (kind().gridInventory() || (kind().processor() ? index < kind().inputs() : index == 0)) && super.mayPlace(stack);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(playerInventory, 9 + row * 9 + column, 24 + column * 18, 153 + row * 18));
        }
        for (int i = 0; i < 9; i++) addSlot(new Slot(playerInventory, i, 24 + i * 18, 211));
        addDataSlots(data);
    }
    /** The explorer dock's ammunition belongs to whoever placed the dock. */
    boolean locked(Player p, int index) { return machine != null && machine.kind == Kind.ROVER_BASE && index == 0 && !machine.isOwner(p); }
    public Kind kind() { return Kind.at(data.get(4)); }
    public boolean slotVisible(int index) {
        if (kind() == Kind.STONE_GENERATOR) return index == 8;
        return (kind() == Kind.GREENHOUSE && index == 7) || kind().gridInventory() || ((kind() == Kind.CHARGER || kind() == Kind.MORTAR || kind() == Kind.ROVER_BASE) && index == 0) || (kind().processor() && (index < kind().inputs() || index == 8)) || (kind() == Kind.PURIFIER && index == 0);
    }
    @Override public boolean stillValid(Player player) {
        return machine == null || (!machine.isRemoved() && machine.getLevel() == player.level()
            && player.distanceToSqr(machine.getBlockPos().getX() + 0.5, machine.getBlockPos().getY() + 0.5, machine.getBlockPos().getZ() + 0.5) <= 64);
    }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (machine != null && stillValid(player)) {
            if (id == 0) { machine.toggle(); return true; }
            if (machine.kind == Kind.ROVER_BASE && !machine.isOwner(player)) { player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cSó o dono desta doca pode usar os controles."), true); return false; }
            if (id == 1 && (machine.kind==Kind.MORTAR || machine.kind==Kind.LEVELER || machine.kind==Kind.ROVER_BASE)) return FrontierMachines.action(machine,player);
            if (id == 1 && machine.kind == Kind.QUARRY) { machine.scanIndex=0; machine.setChanged(); return true; }
            if (id == 1) return AdvancedMachines.action(machine, player);
        }
        return false;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.isActive() || index < 9 && locked(player, index)) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 9) {
            if (!moveItemStackTo(stack, 9, 45, true)) return ItemStack.EMPTY;
        } else {
            // Check each destination before merging: result slots are never shift-click inputs.
            boolean inserted = false;
            for (int target = 0; target < 9 && !stack.isEmpty(); target++) {
                if (slots.get(target).isActive() && slots.get(target).mayPlace(stack)
                    && moveItemStackTo(stack, target, target + 1, false)) inserted = true;
            }
            if (!inserted) {
                if (index < 36) { if (!moveItemStackTo(stack, 36, 45, false)) return ItemStack.EMPTY; }
                else if (!moveItemStackTo(stack, 9, 36, false)) return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
