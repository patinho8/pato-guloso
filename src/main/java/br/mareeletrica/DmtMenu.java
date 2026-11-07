package br.mareeletrica;

import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class DmtMenu extends AbstractContainerMenu {
 public final DmtLoadout loadout;
 public DmtMenu(int id,Inventory inventory){super(MareEletrica.DMT_MENU.get(),id);loadout=DmtLoadout.of(inventory.player);
  for(int i=0;i<5;i++)addSlot(new SlotItemHandler(loadout,i,24+i*36,48){@Override public void setChanged(){loadout.persist();}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,24+col*18,137+row*18));
  for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,24+col*18,195));
 }
 @Override public boolean stillValid(Player player){return !DmtItem.equipped(player).isEmpty()&&(player.level().isClientSide||DmtItem.equipped(player)==loadout.gear)&&player.isAlive();}
 @Override public boolean clickMenuButton(Player player,int id){if(!stillValid(player)||player.isSpectator())return false;if(id>=0&&id<=3){DmtCombat.select(player,id);return true;}if(id==4){DmtItem.refill(player);return true;}return false;}
 @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var held=slot.getItem();var before=held.copy();boolean moved=false;if(index<5)moved=moveItemStackTo(held,5,41,true);else for(int i=0;i<5&&!held.isEmpty();i++)if(loadout.isItemValid(i,held))moved|=moveItemStackTo(held,i,i+1,false);if(!moved)return ItemStack.EMPTY;slot.set(held.isEmpty()?ItemStack.EMPTY:held);slot.setChanged();slot.onTake(player,held);return before;}
}
