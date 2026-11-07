package br.mareeletrica;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
/** Resolve the attachment dynamically: player loading/cloning can replace its handler. */
public final class DmtSlot extends Slot {
 public static final SimpleContainer MARKER=new SimpleContainer(0);
 private final Player player;
 public DmtSlot(Player player){super(MARKER,0,134,62);this.player=player;}
 @Override public boolean mayPlace(ItemStack s){return s.is(MareEletrica.DMT.get());}
 @Override public ItemStack getItem(){return DmtEquipment.get(player);}
 @Override public void set(ItemStack s){DmtEquipment.set(player,s);setChanged();}
 @Override public void setChanged(){player.getInventory().setChanged();}
 @Override public int getMaxStackSize(){return 1;}
 @Override public int getMaxStackSize(ItemStack s){return 1;}
 @Override public ItemStack remove(int amount){return DmtEquipment.inventory(player).extractItem(0,amount,false);}
 @Override public boolean mayPickup(Player p){return true;}
}
