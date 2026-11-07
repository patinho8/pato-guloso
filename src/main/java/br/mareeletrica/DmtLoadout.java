package br.mareeletrica;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.ItemStackHandler;

/** The five modules travel inside the equipped gear, including when it is dropped. */
public final class DmtLoadout extends ItemStackHandler {
 public final Player player;public final ItemStack gear;private boolean loading=true;
 public DmtLoadout(Player player,ItemStack gear){super(5);this.player=player;this.gear=gear;var tag=gear.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var list=tag.getList("DmtLoadout",Tag.TAG_COMPOUND);for(int i=0;i<list.size();i++){var entry=list.getCompound(i);int slot=entry.getInt("Slot");if(slot>=0&&slot<5){var item=ItemStack.parseOptional(player.registryAccess(),entry.getCompound("Item"));if(isItemValid(slot,item))stacks.set(slot,item);}}loading=false;}
 public static DmtLoadout of(Player player){if(player.containerMenu instanceof DmtMenu menu&&menu.loadout.gear==DmtItem.equipped(player))return menu.loadout;return new DmtLoadout(player,DmtItem.equipped(player));}
 @Override public boolean isItemValid(int slot,ItemStack item){return switch(slot){case 0->item.is(MareEletrica.DMT_BLADES.get());case 1->item.is(MareEletrica.DMT_GUN.get());case 2->item.is(MareEletrica.THUNDER_SPEAR.get());case 3->item.is(MareEletrica.GAS.get());case 4->item.is(MareEletrica.PULL_HOOK.get());default->false;};}
 @Override public int getSlotLimit(int slot){return slot==3?64:slot==2?16:1;}
 @Override protected void onContentsChanged(int slot){if(!loading)persist();}
 public void persist(){if(loading||gear.isEmpty())return;var tag=gear.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var list=new ListTag();for(int i=0;i<5;i++)if(!stacks.get(i).isEmpty()){var entry=new CompoundTag();entry.putInt("Slot",i);entry.put("Item",stacks.get(i).save(player.registryAccess()));list.add(entry);}tag.put("DmtLoadout",list);gear.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));player.getInventory().setChanged();}
}
