package br.mareeletrica;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public final class GunLoadout extends ModularLoadout {
 public GunLoadout(Player p,ItemStack g){super(p,g,7);}
 public static GunLoadout of(Player p){return p.containerMenu instanceof GunMenu m&&m.loadout.gun==p.getMainHandItem()?m.loadout:new GunLoadout(p,p.getMainHandItem());}
 public static int rounds(ItemStack s){return Magazines.rounds(s);}
 public static int zoom(ItemStack s){return Math.clamp(RifleLoadout.tag(s).getInt("ArsenalZoom"),1,6);}
 public static int skin(ItemStack s){return Math.clamp(RifleLoadout.tag(s).getInt("ArsenalSkin"),0,6);}
}
