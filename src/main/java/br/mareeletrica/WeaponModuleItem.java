package br.mareeletrica;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
public final class WeaponModuleItem extends Item {
 public enum Type { OPTIC,SUPPRESSOR,SKIN }
 public final WeaponFamily family;public final Type type;public final int variant,value;
 public WeaponModuleItem(WeaponFamily f,Type t,int v){super(new Properties().stacksTo(1));family=f;type=t;variant=v;value=t==Type.OPTIC?f.zooms[v-1]:v;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag flags){out.add(Component.literal("Exclusivo: "+family.title+" • J para instalar"));if(type==Type.OPTIC)out.add(Component.literal("Ampliação óptica: "+value+"×"));}
}
