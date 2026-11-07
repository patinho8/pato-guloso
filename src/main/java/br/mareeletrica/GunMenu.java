package br.mareeletrica;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
public final class GunMenu extends AbstractContainerMenu {
 public final GunLoadout loadout;public final int heldSlot;
 public GunMenu(int id,Inventory inv){super(Arsenal.MENU.get(),id);heldSlot=inv.selected;loadout=new GunLoadout(inv.player,inv.getSelected());for(int i=0;i<4;i++)addSlot(new SlotItemHandler(loadout,i,20+i*54,53){@Override public void setChanged(){loadout.persist();}});for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,20+col*18,122+row*18));for(int col=0;col<9;col++){final int n=col;addSlot(new Slot(inv,col,20+col*18,180){@Override public boolean mayPickup(Player p){return n!=heldSlot;}@Override public boolean mayPlace(ItemStack s){return n!=heldSlot;}});}}
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.getMainHandItem().getItem() instanceof GunItem&&p.getInventory().selected==heldSlot&&(p.level().isClientSide||p.getMainHandItem()==loadout.gun);}
 @Override public void clicked(int slot,int button,ClickType type,Player p){if(type==ClickType.SWAP&&button==heldSlot)return;super.clicked(slot,button,type,p);}
 @Override public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size()||index==31+heldSlot)return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var held=slot.getItem();var copy=held.copy();boolean moved=false;if(index<4)moved=moveItemStackTo(held,4,40,true);else for(int i=0;i<4;i++)if(loadout.isItemValid(i,held))moved|=moveItemStackTo(held,i,i+1,false);if(!moved)return ItemStack.EMPTY;slot.set(held.isEmpty()?ItemStack.EMPTY:held);slot.setChanged();slot.onTake(p,held);return copy;}
}
