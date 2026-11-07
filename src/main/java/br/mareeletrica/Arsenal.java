package br.mareeletrica;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
public final class Arsenal {
 public static final DeferredItem<GunItem> USP=gun(WeaponProfile.USP),M4A1=gun(WeaponProfile.M4A1),AK47=gun(WeaponProfile.AK47),M870=gun(WeaponProfile.M870),MP5=gun(WeaponProfile.MP5),MP40=gun(WeaponProfile.MP40),P90=gun(WeaponProfile.P90),PYTHON=gun(WeaponProfile.PYTHON),GLOCK=gun(WeaponProfile.GLOCK),SCAR=gun(WeaponProfile.SCAR),UMP45=gun(WeaponProfile.UMP45),SPAS12=gun(WeaponProfile.SPAS12),BERETTA=gun(WeaponProfile.BERETTA),UZI=gun(WeaponProfile.UZI),FAMAS=gun(WeaponProfile.FAMAS),M249=gun(WeaponProfile.M249),BARRETT=gun(WeaponProfile.BARRETT),MAC10=gun(WeaponProfile.MAC10),GALIL=gun(WeaponProfile.GALIL),AUG=gun(WeaponProfile.AUG),SAIGA12=gun(WeaponProfile.SAIGA12),SVD=gun(WeaponProfile.SVD);
 public static final DeferredItem<Item> USP_ROUND=ammo("usp_round"),RIFLE_ROUND=ammo("rifle_round"),SHOTGUN_SHELL=ammo("shotgun_shell");
 public static final Supplier<MenuType<GunMenu>> MENU=MareEletrica.MENUS.register("arsenal_loadout",()->new MenuType<>(GunMenu::new,FeatureFlags.DEFAULT_FLAGS));
 private static DeferredItem<GunItem> gun(WeaponProfile p){return MareEletrica.ITEMS.register(p.id,()->new GunItem(p,new Item.Properties().stacksTo(1)));}
 private static DeferredItem<Item> ammo(String id){return MareEletrica.ITEMS.register(id,()->new MaterialItem(id,new Item.Properties()));}
 public static List<DeferredItem<? extends GunItem>> guns(){return List.of(MareEletrica.REVOLVER,USP,M4A1,AK47,M870,MP5,MP40,P90,PYTHON,GLOCK,SCAR,UMP45,SPAS12,BERETTA,UZI,FAMAS,M249,BARRETT,MAC10,GALIL,AUG,SAIGA12,SVD);}
 public static void register(IEventBus bus){Camouflage.register(bus);PowerArmor.register(bus);SportsMode.register(bus);CamoEquipment.register(bus);ArmoryParts.register();Grenades.register();C4.register();CrankBlocks.register();Drones.register();ControlLinks.register();Cameras.register();ControlNet.register();Knives.register();Barrage.register();Jets.register();Parachutes.register();Missiles.register();AutoGate.register();Gadgets.register();Emplacements.register();GroundGuns.register();DmtGrab.register();TennisBall.register();Tracers.register();Scoreboard.register();Vehicles.register();Radar.register();BunkerDoor.register();Sports.register();Multiblock.register();Lamps.register();BaseControl.register();GarageDoors.register();BaseBlocks.register();Garage.register();Delivery.register();PortableBase.register();FightChickens.register(bus);Frogs.register(bus);Robots.register(bus);Batteries.register();Wounds.register(bus);Fun.register(bus);RareEgg.register();Thermal.register();Support.register();Games.register();Pool.register(bus);Skate.register(bus);MiniGolf.register();PingPong.register();Surf.register(bus);Pato.register(bus);ChickenBattle.register();FieldKits.register();WarPlus.register();Casino.register();CreativeTabs.register();Flares.register();}
}
