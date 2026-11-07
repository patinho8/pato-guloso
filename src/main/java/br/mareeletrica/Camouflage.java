package br.mareeletrica;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class Camouflage {
 public static final String[] BIOMES={"the_void","plains","sunflower_plains","snowy_plains","ice_spikes","desert","swamp","mangrove_swamp","forest","flower_forest","birch_forest","dark_forest","old_growth_birch_forest","old_growth_pine_taiga","old_growth_spruce_taiga","taiga","snowy_taiga","savanna","savanna_plateau","windswept_hills","windswept_gravelly_hills","windswept_forest","windswept_savanna","jungle","sparse_jungle","bamboo_jungle","badlands","eroded_badlands","wooded_badlands","meadow","cherry_grove","grove","snowy_slopes","frozen_peaks","jagged_peaks","stony_peaks","river","frozen_river","beach","snowy_beach","stony_shore","warm_ocean","lukewarm_ocean","deep_lukewarm_ocean","ocean","deep_ocean","cold_ocean","deep_cold_ocean","frozen_ocean","deep_frozen_ocean","mushroom_fields","dripstone_caves","lush_caves","deep_dark","nether_wastes","warped_forest","crimson_forest","soul_sand_valley","basalt_deltas","the_end","end_highlands","end_midlands","small_end_islands","end_barrens","stone_cave","deepslate_cave"};
 private static final DeferredRegister<ArmorMaterial> MATERIALS=DeferredRegister.create(Registries.ARMOR_MATERIAL,MareEletrica.ID);
 public static final DeferredHolder<ArmorMaterial,ArmorMaterial> MATERIAL=MATERIALS.register("camouflage",()->new ArmorMaterial(Map.of(ArmorItem.Type.HELMET,0,ArmorItem.Type.CHESTPLATE,0,ArmorItem.Type.LEGGINGS,0,ArmorItem.Type.BOOTS,0),14,SoundEvents.ARMOR_EQUIP_LEATHER,()->Ingredient.of(Items.LEATHER),List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"camouflage"))),0,0));
 private static final List<DeferredItem<CamoArmorItem>> CLOTHES=new ArrayList<>();
 static {for(String biome:BIOMES)for(var type:new ArmorItem.Type[]{ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS})CLOTHES.add(MareEletrica.ITEMS.register("camo_"+biome+"_"+type.getName(),()->new CamoArmorItem(biome,type)));}
 public static List<DeferredItem<CamoArmorItem>> items(){return List.copyOf(CLOTHES);}
 public static void register(IEventBus bus){MATERIALS.register(bus);}
}
