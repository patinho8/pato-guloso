package br.mareeletrica;
import net.minecraft.world.item.ItemStack;
/** Closed canister loop, paid in clean energy; full output pauses without losing input. */
public final class GasRefiller {
 private GasRefiller(){}
 public static boolean tick(MachineEntity m){
  var in=m.inventory.getStackInSlot(0);var out=m.inventory.getStackInSlot(8);
  if(!in.is(MareEletrica.EMPTY_GAS.get())){m.status=5;return false;}
  if(!out.isEmpty()&&(!out.is(MareEletrica.GAS.get())||out.getCount()>=64)){m.status=7;return false;}
  if(!m.clean.pay(2500)){m.status=2;return false;}m.progress+=10;m.lastRate=5000;m.status=1;
  if(m.progress>=100){m.inventory.extractItem(0,1,false);m.inventory.setStackInSlot(8,new ItemStack(MareEletrica.GAS.get(),out.getCount()+1));m.progress=0;m.syncAim();}
  return true;
 }
}
