package br.mareeletrica;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
public final class ServiceMenu extends AbstractContainerMenu{
 public final ContainerData data;private final MachineEntity machine;
 public ServiceMenu(int id,Inventory inv){this(id,inv,null,new ItemStackHandler(9),new ItemStackHandler(10),new SimpleContainerData(MachineEntity.DATA_COUNT));}
 public ServiceMenu(int id,Inventory inv,MachineEntity m){this(id,inv,m,m.inventory,m.gasInventory,m.data);}
 private ServiceMenu(int id,Inventory inv,MachineEntity m,IItemHandler out,IItemHandler gas,ContainerData data){super(MareEletrica.SERVICE_MENU.get(),id);machine=m;this.data=data;addDataSlots(data);
  for(int i=0;i<10;i++){final int s=i;addSlot(new SlotItemHandler(gas,i,44+i%5*18,52+i/5*18){public boolean isActive(){return kind()==Kind.QUARRY||s==0;}public boolean mayPlace(ItemStack stack){return isActive()&&stack.is(MareEletrica.GAS.get());}public int getMaxStackSize(){return kind()==Kind.QUARRY?1:64;}});}
  for(int i=0;i<9;i++){final int output=i;addSlot(new SlotItemHandler(out,i,8+i*18,111){public boolean isActive(){return kind()==Kind.QUARRY||output==0;}public boolean mayPlace(ItemStack s){return false;}});}
  for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,9+r*9+c,8+c*18,150+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,208));
 }
 public Kind kind(){return Kind.at(data.get(4));}
 public boolean stillValid(Player p){return machine==null||!machine.isRemoved()&&machine.getLevel()==p.level()&&p.distanceToSqr(machine.getBlockPos().getCenter())<=64;}
 public boolean clickMenuButton(Player p,int id){if(machine==null||!stillValid(p)||p.isSpectator())return false;if(id==0){machine.toggle();return true;}return id==1&&machine.kind==Kind.EXTERMINATOR&&ExterminatorEntity.enlarge(machine,p);}
 public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.isActive()||!slot.hasItem())return ItemStack.EMPTY;var held=slot.getItem();var copy=held.copy();boolean moved=false;if(i<19)moved=moveItemStackTo(held,19,55,true);else if(held.is(MareEletrica.GAS.get()))for(int n=0;n<10&&!held.isEmpty();n++)if(slots.get(n).isActive())moved|=moveItemStackTo(held,n,n+1,false);if(!moved)return ItemStack.EMPTY;if(held.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,held);return copy;}
}
