package br.mareeletrica;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.74.0: accessories that fit every arsenal gun (Oficina, J): under the barrel (vertical / angled grip, bipod — or a light
 *  or laser under a pistol), on the side rail (flashlight, red or green laser, light + laser) and the magazine (extended, drum).
 *  Each gun says where they sit and which ones fit (see {@link #fit}). */
public final class Accessory {
 private Accessory(){}
 public static final int UNDER=4,SIDE=5,MAG=6;
 public enum Kind {
  VERTICAL_GRIP("Empunhadura Vertical",UNDER,"§7Peça redonda para a mão de apoio: §a−25% coice§7, mira mais firme."),
  ANGLED_GRIP("Empunhadura Angulada",UNDER,"§7Apoio inclinado: §a−15% coice§7, mira mais rápida."),
  BIPOD("Bipé",UNDER,"§7Agachado ou deitado: §a−60% coice§7 e tiro muito preciso."),
  FLASHLIGHT("Lanterna Tática",SIDE,"§7Ilumina de verdade para onde a arma aponta."),
  LASER_RED("Laser Vermelho",SIDE,"§7Ponto vermelho no alvo: §a−40% dispersão§7 sem mirar."),
  LASER_GREEN("Laser Verde",SIDE,"§7Ponto verde no alvo (mais visível de dia): §a−40% dispersão§7 sem mirar."),
  COMBO("Lanterna + Laser",SIDE,"§7Lanterna e laser vermelho no mesmo módulo."),
  EXTENDED_MAG("Pente Estendido",MAG,"§7§a+50% munição§7 no carregador. Recarga um pouco mais lenta."),
  DRUM_MAG("Carregador de Tambor",MAG,"§7§a2× munição§7 no carregador. Recarga mais lenta.");
  public final String title,tip;public final int slot;
  Kind(String t,int s,String tip){title=t;slot=s;this.tip=tip;}
  public boolean light(){return this==FLASHLIGHT||this==COMBO;}
  public boolean laser(){return this==LASER_RED||this==LASER_GREEN||this==COMBO;}
 }
 public static final class AccessoryItem extends Item {
  public final Kind kind;
  AccessoryItem(Kind k){super(new Item.Properties().stacksTo(1));kind=k;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){out.add(Component.literal(kind.tip));out.add(Component.literal("§8Serve em todas as armas do arsenal onde couber • J para instalar"));}
 }
 public static final Map<Kind,DeferredItem<Item>> ITEMS=new EnumMap<>(Kind.class);
 static{for(var k:Kind.values())ITEMS.put(k,MareEletrica.ITEMS.register("acc_"+k.name().toLowerCase(java.util.Locale.ROOT),()->new AccessoryItem(k)));}
 public static void register(){}
 public static void creative(Consumer<ItemStack> out){for(var d:ITEMS.values())out.accept(new ItemStack(d.get()));}

 // ---------- which accessories fit each gun ----------
 static final int GRIP=1,BIPOD=2,SIDE_RAIL=4,UNDER_LIGHT=8,EXT=16,DRUM=32;
 /** per family (WeaponFamily order): what fits */
 static final int[] FIT={
  /*desert_eagle*/UNDER_LIGHT|EXT,/*usp*/UNDER_LIGHT|EXT,/*m4a1*/GRIP|BIPOD|SIDE_RAIL|EXT|DRUM,/*ak47*/GRIP|BIPOD|SIDE_RAIL|EXT|DRUM,/*m870*/SIDE_RAIL,/*mp5*/GRIP|SIDE_RAIL|EXT|DRUM,
  /*smg*/0,/*sniper*/0,/*mp40*/GRIP|SIDE_RAIL|EXT,/*p90*/SIDE_RAIL,/*python*/UNDER_LIGHT,/*glock*/UNDER_LIGHT|EXT|DRUM,/*scar*/GRIP|BIPOD|SIDE_RAIL|EXT|DRUM,
  /*ump45*/GRIP|SIDE_RAIL|EXT|DRUM,/*spas12*/SIDE_RAIL,/*beretta*/UNDER_LIGHT|EXT,/*uzi*/GRIP|SIDE_RAIL|EXT,/*famas*/GRIP|BIPOD|SIDE_RAIL|EXT,/*m249*/SIDE_RAIL,
  /*barrett*/SIDE_RAIL|EXT,/*mac10*/GRIP|SIDE_RAIL|EXT,/*galil*/GRIP|BIPOD|SIDE_RAIL|EXT|DRUM,/*aug*/SIDE_RAIL|EXT|DRUM,/*saiga12*/GRIP|SIDE_RAIL|EXT|DRUM,/*svd*/GRIP|BIPOD|SIDE_RAIL|EXT};
 /** the slot this accessory goes into on this gun, or -1 if it doesn't fit */
 public static int fit(WeaponFamily f,Kind k){
  int m=FIT[f.ordinal()];
  return switch(k){
   case VERTICAL_GRIP,ANGLED_GRIP->(m&GRIP)!=0?UNDER:-1;
   case BIPOD->(m&BIPOD)!=0?UNDER:-1;
   case FLASHLIGHT,LASER_RED,LASER_GREEN,COMBO->(m&SIDE_RAIL)!=0?SIDE:(m&UNDER_LIGHT)!=0?UNDER:-1;
   case EXTENDED_MAG->(m&EXT)!=0?MAG:-1;
   case DRUM_MAG->(m&DRUM)!=0?MAG:-1;
  };
 }
 // ---------- reading what a gun has ----------
 static Kind tag(ItemStack gun,String key){int v=RifleLoadout.tag(gun).getInt(key);return v<=0||v>Kind.values().length?null:Kind.values()[v-1];}
 public static Kind under(ItemStack gun){return tag(gun,"AccUnder");}
 public static Kind side(ItemStack gun){return tag(gun,"AccSide");}
 public static Kind mag(ItemStack gun){return tag(gun,"AccMag");}
 /** whatever sits on the side rail, or a light/laser under a pistol */
 public static Kind beam(ItemStack gun){var s=side(gun);if(s!=null)return s;var u=under(gun);return u!=null&&(u.light()||u.laser())?u:null;}
 public static int capacity(ItemStack gun,WeaponFamily f){var m=mag(gun);return m==Kind.DRUM_MAG?f.capacity*2:m==Kind.EXTENDED_MAG?(int)Math.ceil(f.capacity*1.5):f.capacity;}
 public static int reloadTicks(ItemStack gun,WeaponFamily f){var m=mag(gun);return m==Kind.DRUM_MAG?Math.round(f.reloadTicks*1.35F):m==Kind.EXTENDED_MAG?Math.round(f.reloadTicks*1.15F):f.reloadTicks;}
 public static float recoil(ItemStack gun,Player p){var u=under(gun);if(u==Kind.VERTICAL_GRIP)return .75F;if(u==Kind.ANGLED_GRIP)return .85F;if(u==Kind.BIPOD)return p.isCrouching()||p.isVisuallySwimming()||p.getPose()==net.minecraft.world.entity.Pose.SWIMMING?.4F:1F;return 1F;}
 public static float spread(ItemStack gun,Player p,boolean ads){float m=1;var u=under(gun);if(u==Kind.VERTICAL_GRIP)m*=.88F;if(u==Kind.BIPOD&&(p.isCrouching()||p.getPose()==net.minecraft.world.entity.Pose.SWIMMING))m*=.35F;var b=beam(gun);if(!ads&&b!=null&&b.laser())m*=.6F;return m;}
}
