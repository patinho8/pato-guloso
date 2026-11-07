package br.mareeletrica;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
public final class MortarMenu extends AbstractContainerMenu {
 public final ContainerData data;private final MachineEntity machine;
 public MortarMenu(int id,Inventory inv){this(id,inv,null);}
 public MortarMenu(int id,Inventory inv,MachineEntity machine){
  super(MareEletrica.MORTAR_MENU.get(),id);this.machine=machine;
  data=machine==null?new SimpleContainerData(16):new ContainerData(){
   @Override public int get(int i){if(i==15)return 1;if(i==0)return machine.clean.stored();if(i==1)return (int)Math.max(0,machine.mortarAt-machine.getLevel().getGameTime());if(i==2)return machine.enabled?1:0;return i>=3&&i<15?(i%2==1?machine.mortarElevation[(i-3)/2]:machine.mortarAzimuth[(i-3)/2]):0;}
   @Override public void set(int i,int value){}@Override public int getCount(){return 16;}
  };
  addSlot(new SlotItemHandler(machine==null?new ItemStackHandler(1):machine.inventory,0,18,136));
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,69+col*18,164+row*18));
  for(int col=0;col<9;col++)addSlot(new Slot(inv,col,69+col*18,222));addDataSlots(data);
 }
 public int elevation(int i){return data.get(3+i*2);}public int azimuth(int i){return data.get(4+i*2);}
 @Override public boolean stillValid(Player p){return machine==null||(!machine.isRemoved()&&p.level()==machine.getLevel()&&p.distanceToSqr(machine.getBlockPos().getCenter())<=64);}
 public void aim(Player p,int barrel,int elevation,int azimuth){if(machine==null||!stillValid(p)||barrel<0||barrel>=6||machine.mortarAt>0)return;machine.mortarElevation[barrel]=MortarAim.elevation(elevation);machine.mortarAzimuth[barrel]=MortarAim.azimuth(azimuth);machine.syncAim();}
 @Override public boolean clickMenuButton(Player p,int b){if(machine==null||!stillValid(p))return false;if(b==0){machine.toggle();return true;}return b==1&&FrontierMachines.action(machine,p);}
 @Override public ItemStack quickMoveStack(Player p,int i){
  if(i<0||i>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();
  if(i==0){if(!moveItemStackTo(stack,1,37,true))return ItemStack.EMPTY;}else if(stack.is(MareEletrica.CANNONBALL.get())){if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}else if(i<28){if(!moveItemStackTo(stack,28,37,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,1,28,false))return ItemStack.EMPTY;
  if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();if(copy.getCount()==stack.getCount())return ItemStack.EMPTY;slot.onTake(p,stack);return copy;
 }
}
