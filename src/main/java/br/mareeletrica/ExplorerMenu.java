package br.mareeletrica;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
public final class ExplorerMenu extends AbstractContainerMenu{
 public final ContainerData data;private final ExplorerEntity rover;
 public ExplorerMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf){this(id,inv,null,new ItemStackHandler(5),new SimpleContainerData(4));buf.readVarInt();}
 public ExplorerMenu(int id,Inventory inv,ExplorerEntity r){this(id,inv,r,r.cargo,r.menuData);if(!r.isOwner(inv.player))inv.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cSó o dono da doca pode mexer na munição e no gás do explorador."),true);}
 /** Ammunition (0) and gas (1) belong to the dock owner. */
 boolean locked(Player p,int s){return s<2&&rover!=null&&!rover.isOwner(p);}
 private ExplorerMenu(int id,Inventory inv,ExplorerEntity r,IItemHandler items,ContainerData data){super(MareEletrica.EXPLORER_MENU.get(),id);rover=r;this.data=data;addDataSlots(data);for(int i=0;i<5;i++){final int s=i;final Player who=inv.player;addSlot(new SlotItemHandler(items,i,22+i*29,54){public boolean mayPickup(Player p){return !locked(p,s)&&super.mayPickup(p);}public boolean mayPlace(ItemStack stack){if(locked(who,s))return false;return s>=2||stack.is(s==0?MareEletrica.ROVER_BULLET.get():MareEletrica.GAS.get());}});}for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,8+col*18,99+row*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,157));}
 public boolean stillValid(Player p){return rover==null||rover.isAlive()&&rover.level()==p.level()&&(p.distanceToSqr(rover)<=64||p instanceof net.minecraft.server.level.ServerPlayer s&&RemoteSessions.controls(s,rover));}
 public boolean clickMenuButton(Player p,int id){if(rover==null||!stillValid(p)||id!=0||p.isSpectator()||!rover.isOwner(p))return false;int n=Math.min(rover.ammunition,64);var rest=rover.cargo.insertItem(0,new ItemStack(MareEletrica.ROVER_BULLET.get(),n),false);rover.ammunition-=n-rest.getCount();rover.reloadDelay=100;return true;}
 public ItemStack quickMoveStack(Player p,int i){if(i<0||i>=slots.size())return ItemStack.EMPTY;var slot=slots.get(i);if(!slot.hasItem()||locked(p,i))return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();boolean own=!locked(p,0);boolean moved=i<5?moveItemStackTo(stack,5,41,true):stack.is(MareEletrica.ROVER_BULLET.get())?own&&moveItemStackTo(stack,0,1,false):stack.is(MareEletrica.GAS.get())?own&&moveItemStackTo(stack,1,2,false):moveItemStackTo(stack,2,5,false);if(!moved)return ItemStack.EMPTY;if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;}
}
