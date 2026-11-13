package br.mareeletrica.mixin.client;
import br.mareeletrica.DmtSlot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryMixin {
 @Inject(method="selectTab",at=@At("TAIL"))
 private void mare$equipmentPosition(CreativeModeTab tab,CallbackInfo ci){
  if(tab.getType()!=CreativeModeTab.Type.INVENTORY)return;
  for(var slot:((CreativeModeInventoryScreen)(Object)this).getMenu().slots)if(slot.container==DmtSlot.MARKER){var a=(SlotPositionAccessor)(Object)slot;a.mare$setX(199);a.mare$setY(80);}
  for(var slot:((CreativeModeInventoryScreen)(Object)this).getMenu().slots)for(int i=0;i<4;i++)if(slot.container==br.mareeletrica.CamoSlot.MARKERS[i]){var a=(SlotPositionAccessor)(Object)slot;a.mare$setX(199);a.mare$setY(8+i*18);}
 }
}
