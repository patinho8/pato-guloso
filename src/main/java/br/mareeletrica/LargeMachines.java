package br.mareeletrica;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class LargeMachines {
    private LargeMachines() {}
    public static boolean validInput(int slot,ItemStack stack) {
        return slot==0 && !crop(stack).isEmpty() || slot==1 && stack.is(MareEletrica.POTABLE_WATER.get()) || slot==2 && stack.is(Items.BONE_MEAL);
    }
    private static ItemStack crop(ItemStack seed) {
        if(seed.is(Items.WHEAT_SEEDS))return new ItemStack(Items.WHEAT,8);
        if(seed.is(Items.BEETROOT_SEEDS))return new ItemStack(Items.BEETROOT,8);
        if(seed.is(Items.CARROT))return new ItemStack(Items.CARROT,8);
        if(seed.is(Items.POTATO))return new ItemStack(Items.POTATO,8);
        if(seed.is(Items.MELON_SEEDS))return new ItemStack(Items.MELON_SLICE,8);
        if(seed.is(Items.PUMPKIN_SEEDS))return new ItemStack(Items.PUMPKIN,4);
        if(seed.is(Items.SUGAR_CANE))return new ItemStack(Items.SUGAR_CANE,8);
        if(seed.is(Items.SWEET_BERRIES))return new ItemStack(Items.SWEET_BERRIES,8);
        if(seed.is(Items.COCOA_BEANS))return new ItemStack(Items.COCOA_BEANS,8);
        if(seed.is(Items.NETHER_WART))return new ItemStack(Items.NETHER_WART,6);
        return ItemStack.EMPTY;
    }
    public static boolean tick(MachineEntity m) { return m.kind==Kind.GREENHOUSE?greenhouse(m):QuarryBatches.tick(m); }
    private static boolean room(ItemStack existing,ItemStack output) {
        return existing.isEmpty() || ItemStack.isSameItemSameComponents(existing,output)
            && existing.getCount()+output.getCount()<=existing.getMaxStackSize();
    }
    private static void append(MachineEntity m,int slot,ItemStack output) {
        ItemStack existing=m.inventory.getStackInSlot(slot);
        m.inventory.setStackInSlot(slot,existing.isEmpty()?output:existing.copyWithCount(existing.getCount()+output.getCount()));
    }
    private static boolean greenhouse(MachineEntity m) {
        ItemStack produce=crop(m.inventory.getStackInSlot(0));
        if(produce.isEmpty() || !m.inventory.getStackInSlot(1).is(MareEletrica.POTABLE_WATER.get())) { m.status=33;return false; }
        boolean fertilized=m.inventory.getStackInSlot(2).is(Items.BONE_MEAL);
        if(fertilized)produce.setCount(Math.min(64,produce.getCount()*2));
        ItemStack bottle=new ItemStack(Items.GLASS_BOTTLE);
        if(!room(m.inventory.getStackInSlot(8),produce) || !room(m.inventory.getStackInSlot(7),bottle)) { m.status=7;return false; }
        if(!m.clean.pay(fertilized?800:400)) { m.status=2;return false; }
        m.progress+=10;m.lastRate=fertilized?1600:800;m.status=34;
        if(m.progress>=100) {
            m.inventory.extractItem(1,1,false);
            if(fertilized)m.inventory.extractItem(2,1,false);
            append(m,7,bottle);append(m,8,produce);m.progress=0;
        }
        return true;
    }
}
