package br.mareeletrica;
import java.util.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
public final class ArmoryParts {
 private static final Map<String,DeferredItem<? extends Item>> ITEMS=new LinkedHashMap<>();
 static {for(var f:WeaponFamily.values()){for(var t:WeaponModuleItem.Type.values())for(int v=1;v<=(t==WeaponModuleItem.Type.SKIN?12:3);v++){final int n=v;String id=f.id+"_"+t.name().toLowerCase(Locale.ROOT)+"_"+v;ITEMS.put(id,MareEletrica.ITEMS.register(id,()->new WeaponModuleItem(f,t,n)));}for(boolean full:new boolean[]{false,true}){String id=f.id+(full?"_magazine":"_empty_magazine");ITEMS.put(id,MareEletrica.ITEMS.register(id,()->new MagazineItem(f,full)));}}}
 public static void register(){}
 public static Item module(WeaponFamily f,WeaponModuleItem.Type t,int v){return ITEMS.get(f.id+"_"+t.name().toLowerCase(Locale.ROOT)+"_"+v).get();}
 public static Item magazine(WeaponFamily f,boolean full){return ITEMS.get(f.id+(full?"_magazine":"_empty_magazine")).get();}
 public static void creative(CreativeModeTab.Output out,boolean skins){for(var d:ITEMS.values())if((d.get() instanceof WeaponModuleItem m&&m.type==WeaponModuleItem.Type.SKIN)==skins)out.accept(d.get());}
}
