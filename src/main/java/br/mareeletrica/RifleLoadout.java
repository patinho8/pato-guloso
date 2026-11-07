package br.mareeletrica;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
public final class RifleLoadout extends ModularLoadout {
 public RifleLoadout(Player p,ItemStack g){super(p,g,5);}
 public static RifleLoadout of(Player p){return p.containerMenu instanceof RifleMenu m&&m.loadout.gun==p.getMainHandItem()?m.loadout:new RifleLoadout(p,p.getMainHandItem());}
 public static CompoundTag tag(ItemStack g){return g.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
 public static int zoom(ItemStack g){return Math.clamp(tag(g).getInt("ScopeZoom"),1,48);}
 public static int suppressor(ItemStack g){return Math.clamp(tag(g).getInt("Suppressor"),0,5);}
 public static int sticker(ItemStack g){return Math.clamp(tag(g).getInt("Sticker"),0,12);}
 public static boolean bipod(ItemStack g){return tag(g).getBoolean("Bipod");}
 public static int calibration(ItemStack g,int field){return tag(g).getInt(field==0?"ZeroRange":field==1?"Windage":"Elevation");}
 public void calibrate(int field,int value){var t=tag(gun);t.putInt(field==0?"ZeroRange":field==1?"Windage":"Elevation",Mth.clamp(value,field==0?0:-30,field==0?1536:30));write(t);}
}
