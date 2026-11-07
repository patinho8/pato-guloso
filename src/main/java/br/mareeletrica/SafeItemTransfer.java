package br.mareeletrica;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/** Transfers only through inventory contracts. In-flight items live in a saved local buffer. */
public final class SafeItemTransfer {
    private SafeItemTransfer() {}
    public static boolean empty(IItemHandler buffer) {
        for (int i = 0; i < buffer.getSlots(); i++) if (!buffer.getStackInSlot(i).isEmpty()) return false;
        return true;
    }
    public static ItemStack insert(IItemHandler destination, ItemStack offered, boolean simulate) {
        ItemStack rest = offered.copy();
        for (int i = 0; i < destination.getSlots() && !rest.isEmpty(); i++) rest = destination.insertItem(i, rest, simulate);
        return rest;
    }
    public static boolean stage(IItemHandler source, IItemHandlerModifiable buffer, int limit, Predicate<ItemStack> acceptable) {
        if (source == buffer || limit <= 0 || !empty(buffer) || buffer.getSlots() == 0) return false;
        int wanted = Math.min(limit, buffer.getSlotLimit(0));
        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack preview = source.extractItem(i, wanted, true);
            if (preview.isEmpty() || !acceptable.test(preview.copy())) continue;
            ItemStack extracted = source.extractItem(i, Math.min(wanted, preview.getCount()), false);
            if (extracted.isEmpty()) continue;
            // The buffer is empty and owned by this machine. Store before attempting delivery.
            buffer.setStackInSlot(0, extracted);
            return true;
        }
        return false;
    }
    public static int deliver(IItemHandlerModifiable buffer, IItemHandler destination, int limit) {
        if (buffer == destination || limit <= 0) return 0;
        int moved = 0;
        for (int slot = 0; slot < buffer.getSlots() && moved < limit; slot++) {
            ItemStack held = buffer.getStackInSlot(slot);
            if (held.isEmpty()) continue;
            int offered = Math.min(held.getCount(), limit - moved);
            ItemStack rest = insert(destination, held.copyWithCount(offered), false);
            int accepted = offered - rest.getCount();
            if (accepted <= 0) continue;
            // Keep every rejected item in the original buffer, including on a partial delivery.
            int remaining = held.getCount() - accepted;
            buffer.setStackInSlot(slot, remaining == 0 ? ItemStack.EMPTY : held.copyWithCount(remaining));
            moved += accepted;
        }
        return moved;
    }
}
