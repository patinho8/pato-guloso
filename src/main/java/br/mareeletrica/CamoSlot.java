package br.mareeletrica;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Right-hand column of the inventory: hood, jacket, pants, boots. Backed by an attachment, not a container. */
public final class CamoSlot extends Slot {
 public static final SimpleContainer[] MARKERS={new SimpleContainer(0),new SimpleContainer(0),new SimpleContainer(0),new SimpleContainer(0)};
 private final Player player;private final int which;
 public CamoSlot(Player player,int which){super(MARKERS[which],0,178,8+which*18);this.player=player;this.which=which;}
 public int index(){return which;}
 @Override public boolean mayPlace(ItemStack s){return s.getItem() instanceof CamoArmorItem c&&CamoEquipment.index(c.getType())==which;}
 @Override public ItemStack getItem(){return CamoEquipment.get(player,which);}
 @Override public void set(ItemStack s){CamoEquipment.set(player,which,s);setChanged();}
 @Override public void setChanged(){player.getInventory().setChanged();}
 @Override public int getMaxStackSize(){return 1;}
 @Override public int getMaxStackSize(ItemStack s){return 1;}
 @Override public ItemStack remove(int amount){return CamoEquipment.inventory(player).extractItem(which,amount,false);}
 @Override public boolean mayPickup(Player p){return true;}
}
