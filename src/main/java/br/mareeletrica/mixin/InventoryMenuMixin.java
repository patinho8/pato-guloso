package br.mareeletrica.mixin;
import br.mareeletrica.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {
 protected InventoryMenuMixin(MenuType<?> type,int id){super(type,id);}
 @Inject(method="<init>",at=@At("TAIL"))
 private void mare$equipment(Inventory inv,boolean active,Player player,CallbackInfo ci){addSlot(new DmtSlot(player));for(int i=0;i<4;i++)addSlot(new CamoSlot(player,i));}
 @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
 private void mare$quickCamo(Player player,int index,CallbackInfoReturnable<ItemStack> ci){
  if(index<9||index>=46)return;
  var source=slots.get(index);var stack=source.getItem();if(!(stack.getItem() instanceof CamoArmorItem c))return;
  int target=CamoEquipment.slot(this,CamoEquipment.index(c.getType()));if(target<0||slots.get(target).hasItem())return;
  var copy=stack.copy();if(moveItemStackTo(stack,target,target+1,false)){if(stack.isEmpty())source.setByPlayer(ItemStack.EMPTY);else source.setChanged();source.onTake(player,stack);ci.setReturnValue(copy);}
 }
 @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
 private void mare$quickEquip(Player player,int index,CallbackInfoReturnable<ItemStack> ci){
  int target=DmtEquipment.slot(this);if(target<0||index==target||index<9||index>=46)return;
  var source=slots.get(index);var stack=source.getItem();if(!stack.is(MareEletrica.DMT.get())||slots.get(target).hasItem())return;
  var copy=stack.copy();if(moveItemStackTo(stack,target,target+1,false)){if(stack.isEmpty())source.setByPlayer(ItemStack.EMPTY);else source.setChanged();source.onTake(player,stack);ci.setReturnValue(copy);}
 }
}
