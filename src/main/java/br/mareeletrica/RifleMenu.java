package br.mareeletrica;

import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class RifleMenu extends AbstractContainerMenu {
 public final RifleLoadout loadout;public final int heldSlot;public final ContainerData calibration;
 public RifleMenu(int id,Inventory inv){super(MareEletrica.RIFLE_MENU.get(),id);heldSlot=inv.selected;loadout=new RifleLoadout(inv.player,inv.getSelected());
  for(int i=0;i<5;i++)addSlot(new SlotItemHandler(loadout,i,20+i*48,54){@Override public void setChanged(){loadout.persist();}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++){final int slot=9+row*9+col;addSlot(new Slot(inv,slot,20+col*18,156+row*18));}
  for(int col=0;col<9;col++){final int slot=col;addSlot(new Slot(inv,col,20+col*18,214){@Override public boolean mayPickup(Player p){return slot!=heldSlot;}@Override public boolean mayPlace(ItemStack s){return slot!=heldSlot;}});}
  calibration=new ContainerData(){public int get(int i){return RifleLoadout.calibration(loadout.gun,i);}public void set(int i,int v){loadout.calibrate(i,v);}public int getCount(){return 3;}};addDataSlots(calibration);
 }
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.getMainHandItem().is(MareEletrica.SNIPER.get())&&p.getInventory().selected==heldSlot&&(p.level().isClientSide||p.getMainHandItem()==loadout.gun);}
 @Override public void clicked(int slot,int button,ClickType type,Player p){if(type==ClickType.SWAP&&button==heldSlot)return;super.clicked(slot,button,type,p);}
 @Override public boolean clickMenuButton(Player p,int id){if(!stillValid(p)||p.isSpectator())return false;if(id==6){for(int i=0;i<3;i++)loadout.calibrate(i,0);return true;}if(id<0||id>5)return false;int field=id/2,step=field==0?48:1;loadout.calibrate(field,calibration.get(field)+(id%2==0?-step:step));return true;}
 @Override public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size()||index==32+heldSlot)return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var held=slot.getItem();var copy=held.copy();boolean moved=false;if(index<5)moved=moveItemStackTo(held,5,41,true);else for(int i=0;i<5;i++)if(loadout.isItemValid(i,held))moved|=moveItemStackTo(held,i,i+1,false);if(!moved)return ItemStack.EMPTY;slot.set(held.isEmpty()?ItemStack.EMPTY:held);slot.setChanged();slot.onTake(p,held);return copy;}
}
