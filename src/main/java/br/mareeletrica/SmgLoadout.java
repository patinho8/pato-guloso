package br.mareeletrica;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public final class SmgLoadout extends ModularLoadout {
 public SmgLoadout(Player p,ItemStack g){super(p,g,4);}
 public static SmgLoadout of(Player p){return p.containerMenu instanceof SmgMenu m&&m.loadout.gun==p.getMainHandItem()?m.loadout:new SmgLoadout(p,p.getMainHandItem());}
 public static int rounds(ItemStack s){return Magazines.rounds(s);}
 public static int zoom(ItemStack s){return Math.clamp(RifleLoadout.tag(s).getInt("SmgZoom"),1,4);}
 public static int skin(ItemStack s){return Math.clamp(RifleLoadout.tag(s).getInt("SmgSkin"),0,6);}
}
