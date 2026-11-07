package br.mareeletrica;
import net.minecraft.world.item.ItemStack;
/** Old registry ID and Cylinder ammunition survive upgrades. */
public final class RevolverItem extends GunItem {
 public RevolverItem(Properties p){super(WeaponProfile.DESERT_EAGLE,p);}
 public static int rounds(ItemStack gun){return GunLoadout.rounds(gun);}
}
