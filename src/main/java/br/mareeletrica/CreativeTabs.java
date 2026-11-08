package br.mareeletrica;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** Creative tabs, one per theme, in a fixed order. Anything not placed on purpose ends up in "Materiais e comida" so nothing is ever missing. */
public final class CreativeTabs {
 private CreativeTabs(){}
 /** Collects stacks without duplicates, then hands them to the real tab. */
 static final class Sink implements CreativeModeTab.Output {
  final List<ItemStack> stacks=new ArrayList<>();
  @Override public void accept(ItemStack s,CreativeModeTab.TabVisibility v){if(s.isEmpty())return;for(var o:stacks)if(ItemStack.isSameItemSameComponents(o,s))return;stacks.add(s);}
  void add(Supplier<? extends ItemLike> i){accept(new ItemStack(i.get()));}
 }
 private static void machines(Sink s,java.util.function.Predicate<Kind> which){for(var k:Kind.values())if(which.test(k))s.add(MareEletrica.MACHINES.get(k));}
 static boolean energyKind(Kind k){return (!k.consumer&&k!=Kind.PURIFIER)||k==Kind.POWER_CONTROLLER||k==Kind.AUTO_CRANK;}

 static void energy(Sink s){
  machines(s,CreativeTabs::energyKind);
  s.add(MareEletrica.CABLE_ITEM);s.add(MareEletrica.CYBER_CABLE_ITEM);s.add(MareEletrica.WIND_CABLE_ITEM);MareEletrica.TRANSFORMER_ITEMS.forEach(s::add);
  MareEletrica.WIRELESS_PLATES.forEach(s::add);s.add(MareEletrica.PORTABLE_BATTERY);s.add(CrankBlocks.HAND_CRANK_ITEM);
  s.add(MareEletrica.WATER_SHAFT_ITEM);s.add(MareEletrica.WATER_DUCT_ITEM);s.add(MareEletrica.WATER_INTAKE_ITEM);s.add(MareEletrica.WATER_OUTLET_ITEM);
 }
 static void machinesTab(Sink s){
  machines(s,k->!energyKind(k)&&k!=Kind.MORTAR&&k!=Kind.TURRET&&k!=Kind.ARMOR_STATION&&k!=Kind.HANGAR&&k!=Kind.SHIELD);
  s.add(MareEletrica.ITEM_DUCT_ITEM);s.add(MareEletrica.LED_BED_ITEM);Lamps.ITEMS.forEach(s::add);s.add(MareEletrica.REMOTE);
 }
 static void guns(Sink s){
  for(var gun:Arsenal.guns())if(gun!=MareEletrica.REVOLVER)s.add(gun);
  s.add(MareEletrica.SMG);s.add(MareEletrica.SNIPER);s.add(MareEletrica.REVOLVER);
  s.add(Arsenal.USP_ROUND);s.add(Arsenal.RIFLE_ROUND);s.add(Arsenal.SHOTGUN_SHELL);s.add(MareEletrica.SMG_ROUND);s.add(MareEletrica.SNIPER_ROUND);s.add(MareEletrica.REVOLVER_ROUND);s.add(MareEletrica.ROVER_BULLET);
  MareEletrica.SCOPES.forEach(s::add);ArmoryParts.creative(s,false);s.add(MareEletrica.BIPOD);
  Knives.creative(s);
 }
 static void defense(Sink s){
  Grenades.creative(s);s.add(C4.ITEM);s.add(MareEletrica.MACHINES.get(Kind.MORTAR));
  Barrage.creative(s);/* 0.66.0: batteries + 1000-round ammo crate removed from the tab and recipes (still registered for old worlds) */Wounds.creative(s);s.accept(new ItemStack(Support.RADIO.get()));Missiles.creative(s);Emplacements.creative(s);s.add(MareEletrica.CANNONBALL);s.add(MareEletrica.AIM_REMOTE);
  Flares.creative(s);s.add(WarPlus.MINE_ITEM);s.add(WarPlus.LAUNCHER);s.add(WarPlus.STRIKE);
 }
 static void vehicles(Sink s){Vehicles.creative(s);Jets.creative(s);Drones.creative(s);Parachutes.creative(s);s.add(MareEletrica.MACHINES.get(Kind.HANGAR));
  s.add(Garage.ITEM);s.add(Garage.REMOTE);s.add(Garage.FUEL);s.add(Garage.ARMOR_KIT);s.add(Garage.ENGINE_KIT);s.add(Garage.AMMO_KIT);s.add(BaseBlocks.HELIPAD_ITEM);s.add(BaseBlocks.RUNWAY_ITEM);s.add(BaseBlocks.LIFT_ITEM);s.add(BaseBlocks.LIFT_STOP_ITEM);}
 static void base(Sink s){s.add(PortableBase.ITEM);s.add(BaseControl.PANEL_ITEM);s.add(BaseControl.SIREN_ITEM);s.add(Radar.ITEM);s.add(MareEletrica.MACHINES.get(Kind.SHIELD));s.add(MareEletrica.MACHINES.get(Kind.TURRET));s.add(BunkerDoor.ITEM);s.add(BunkerDoor.CARD);s.add(GarageDoors.ITEM);s.add(AutoGate.ITEM);s.add(Cameras.CAMERA_ITEM);s.add(ControlLinks.PENDRIVE);s.add(Delivery.STATION_ITEM);s.add(Delivery.DRONE_ITEM);s.add(Delivery.ROUTE_TAG);}
 static void gear(Sink s){
  s.add(MareEletrica.DMT);s.add(MareEletrica.DMT_BLADE);s.add(MareEletrica.DMT_BLADES);s.add(MareEletrica.DMT_GUN);s.add(MareEletrica.DMT_CARTRIDGE);s.add(MareEletrica.THUNDER_SPEAR);s.add(MareEletrica.PULL_HOOK);
  s.add(MareEletrica.GAS);s.add(MareEletrica.EMPTY_GAS);
  for(var g:List.of(Gadgets.BINOCULARS,Gadgets.FLASHLIGHT,Gadgets.GPS,Gadgets.WALKIE_TALKIE,Gadgets.THERMAL_GOGGLES))s.add(g);
 }
 static void armor(Sink s){PowerArmor.creative(s);s.add(MareEletrica.MACHINES.get(Kind.ARMOR_STATION));}
 static void camo(Sink s){ArmoryParts.creative(s,true);Camouflage.items().forEach(s::add);}
 static void kit(Sink s,FieldKits.Kind k){s.add(FieldKits.ITEMS.get(k));}
 /** 0.67.2: sports, games, skate/surf and animals each got their own tab (it was all one long mixed list) */
 static void sports(Sink s){
  SportsMode.creative(s::accept);
  Sports.creative(s);s.add(TennisBall.ITEM);TennisBall.COLORED.forEach(s::add);s.add(TennisBall.RACKET);s.add(TennisBall.NET_ITEM);s.add(Scoreboard.ITEM);
  for(var k:List.of(FieldKits.Kind.FUTEBOL,FieldKits.Kind.BASQUETE,FieldKits.Kind.VOLEI,FieldKits.Kind.TENIS))kit(s,k);
 }
 static void games(Sink s){
  Fun.creative(s::accept);Pool.creative(s::accept);s.add(Pato.RACE_ITEM);PingPong.creative(s::accept);Games.creative(s::accept);MiniGolf.creative(s::accept);
 }
 static void radical(Sink s){Skate.creative(s::accept);Surf.creative(s::accept);}
 static void animals(Sink s){
  s.add(FightChickens.ITEM);FightChickens.BREED_ITEMS.forEach(s::add);s.add(FightChickens.WHISTLE);s.add(ChickenBattle.ITEM);kit(s,FieldKits.Kind.GALOS);
  Pato.creative(s::accept);s.add(Frogs.EGG);s.add(Frogs.POCKET);s.add(Robots.ITEM);s.add(Robots.TRASH_ITEM);
 }
 /** 0.65.2: the old rifle/SMG stickers, suppressors and SMG scopes (replaced by the weapon modules) are gone from the tabs and recipes.
  *  They stay registered only so old copies in chests keep loading; the "adapt" recipes still turn them into modules. */
 static boolean legacy(String id){return java.util.Set.of("sentry_gun","flak_battery","auto_grenade_launcher","flak_shell","grenade_round","ammo_crate").contains(id)||id.startsWith("rifle_sticker_")||id.startsWith("rifle_suppressor_")||id.startsWith("smg_sticker_")||id.startsWith("smg_scope_");}
 static void casino(Sink s){s.add(Casino.CHIP);for(var i:Casino.ITEMS.values())s.add(i);}
 static void materials(Sink s){
  s.add(MareEletrica.IRON_GRIT);s.add(MareEletrica.COPPER_GRIT);s.add(MareEletrica.GOLD_GRIT);s.add(MareEletrica.IRON_PLATE);s.add(MareEletrica.COPPER_COIL);s.add(MareEletrica.GOLD_CONTACT);s.add(MareEletrica.CONTROL_MODULE);
  MareEletrica.ADVANCED.values().forEach(s::add);
  s.add(MareEletrica.POTABLE_WATER);s.add(MareEletrica.HONEY_BREAD);s.add(MareEletrica.HONEY_CANDY);s.add(MareEletrica.HONEY_WATER);s.add(Gadgets.DANONINHO);
  // everything else that no tab claimed
  var placed=new HashSet<Item>();
  for(Consumer<Sink> f:List.<Consumer<Sink>>of(CreativeTabs::energy,CreativeTabs::machinesTab,CreativeTabs::guns,CreativeTabs::defense,CreativeTabs::vehicles,CreativeTabs::base,CreativeTabs::gear,CreativeTabs::armor,CreativeTabs::camo,CreativeTabs::sports,CreativeTabs::games,CreativeTabs::radical,CreativeTabs::animals,k->Furniture.creative(k::accept),CreativeTabs::casino)){var x=new Sink();f.accept(x);x.stacks.forEach(st->placed.add(st.getItem()));}
  for(var e:MareEletrica.ITEMS.getEntries()){var i=e.get();if(!placed.contains(i)&&!legacy(e.getId().getPath()))s.accept(new ItemStack(i));}
 }

 private static String last;
 private static Supplier<CreativeModeTab> tab(String id,String title,Supplier<ItemStack> icon,Consumer<Sink> fill){
  String before=last;last=id;
  return MareEletrica.TABS.register(id,()->{
   var b=CreativeModeTab.builder().title(Component.literal(title)).icon(icon).displayItems((params,out)->{var s=new Sink();fill.accept(s);for(var st:s.stacks)out.accept(st);});
   if(before!=null)b.withTabsBefore(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,before));
   return b.build();
  });
 }
 public static final Supplier<CreativeModeTab> ENERGY=tab("machines","⚡ Maré Elétrica · Energia",()->new ItemStack(MareEletrica.MACHINES.get(Kind.WIND_GIANT).get()),CreativeTabs::energy);
 public static final Supplier<CreativeModeTab> MACHINES=tab("factory","⚙ Maré Elétrica · Máquinas",()->new ItemStack(MareEletrica.MACHINES.get(Kind.CRUSHER).get()),CreativeTabs::machinesTab);
 public static final Supplier<CreativeModeTab> GUNS=tab("guns","🔫 Maré Elétrica · Armas",()->new ItemStack(MareEletrica.SNIPER.get()),CreativeTabs::guns);
 public static final Supplier<CreativeModeTab> DEFENSE=tab("war","💣 Maré Elétrica · Explosivos e defesa",()->new ItemStack(C4.ITEM.get()),CreativeTabs::defense);
 public static final Supplier<CreativeModeTab> VEHICLES=tab("vehicles","🚁 Maré Elétrica · Veículos",()->new ItemStack(Vehicles.HELI_ITEM.get()),CreativeTabs::vehicles);
 public static final Supplier<CreativeModeTab> BASE=tab("base","🏰 Maré Elétrica · Base e comando",()->new ItemStack(BaseControl.PANEL_ITEM.get()),CreativeTabs::base);
 public static final Supplier<CreativeModeTab> GEAR=tab("gear","🎒 Maré Elétrica · Equipamentos",()->new ItemStack(MareEletrica.DMT.get()),CreativeTabs::gear);
 public static final Supplier<CreativeModeTab> ARMOR=tab("armor","🛡 Maré Elétrica · Armaduras",()->new ItemStack(PowerArmor.ITEMS.get(1).get()),CreativeTabs::armor);
 public static final Supplier<CreativeModeTab> CAMO=tab("camouflage","🌿 Maré Elétrica · Camuflagem",()->new ItemStack(Camouflage.items().get(1).get()),CreativeTabs::camo);
 public static final Supplier<CreativeModeTab> SPORTS=tab("sports","⚽ Maré Elétrica · Esportes",()->new ItemStack(Sports.BALLS.get(Sports.Kind.SOCCER).get()),CreativeTabs::sports);
 public static final Supplier<CreativeModeTab> GAMES=tab("games","🎳 Maré Elétrica · Jogos e diversão",()->new ItemStack(Pool.TABLE_ITEM.get()),CreativeTabs::games);
 public static final Supplier<CreativeModeTab> RADICAL=tab("radical","🛹 Maré Elétrica · Skate e surfe",()->new ItemStack(Skate.ITEM.get()),CreativeTabs::radical);
 public static final Supplier<CreativeModeTab> ANIMALS=tab("animals","🐔 Maré Elétrica · Animais e robôs",()->new ItemStack(FightChickens.WHISTLE.get()),CreativeTabs::animals);
 public static final Supplier<CreativeModeTab> HOME=tab("home","🛋 Maré Elétrica · Casa e móveis",()->new ItemStack(Furniture.TV_ITEM.get()),s->Furniture.creative(s::accept));
 public static final Supplier<CreativeModeTab> CASINO=tab("casino","🎰 Maré Elétrica · Cassino",()->new ItemStack(Casino.CHIP.get()),CreativeTabs::casino);
 public static final Supplier<CreativeModeTab> MATERIALS=tab("materials","🔩 Maré Elétrica · Materiais e comida",()->new ItemStack(MareEletrica.CONTROL_MODULE.get()),CreativeTabs::materials);
 public static void register(){}
 /** test helper: ids that no tab claims on purpose (they fall into "Materiais e comida") */
 public static List<String> leftovers(){var all=new Sink();materials(all);var named=new Sink();named.add(MareEletrica.IRON_GRIT);var out=new ArrayList<String>();for(var st:all.stacks)out.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(st.getItem()).getPath());return out;}
}
