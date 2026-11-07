package br.mareeletrica;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
@EventBusSubscriber(modid=MareEletrica.ID)
public final class LedBedVillage {
 @SubscribeEvent public static void beds(ServerAboutToStartEvent event){var home=event.getServer().registryAccess().registryOrThrow(Registries.POINT_OF_INTEREST_TYPE).getHolderOrThrow(PoiTypes.HOME);var map=net.neoforged.neoforge.registries.GameData.getBlockStatePointOfInterestTypeMap();for(var state:MareEletrica.LED_BED.get().getStateDefinition().getPossibleStates())if(state.getValue(BedBlock.PART)==BedPart.HEAD)map.put(state,home);}
}
