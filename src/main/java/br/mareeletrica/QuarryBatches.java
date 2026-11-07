package br.mareeletrica;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.items.ItemHandlerHelper;
/** A saved pending result avoids rerolls and item loss when the output fills. */
public final class QuarryBatches {
 public static boolean tick(MachineEntity m){
  if(m.clean.stored()<2000){m.status=2;return false;}
  if(m.batchRemaining==0){int gas=0;for(int i=0;i<10;i++)if(m.gasInventory.getStackInSlot(i).is(MareEletrica.GAS.get()))gas++;
   if(java.util.stream.IntStream.range(0,9).allMatch(i->m.inventory.getStackInSlot(i).getCount()>=64)){m.status=7;return false;}
   m.batchTotal=m.batchRemaining=OreBatchRules.amount(gas);for(int i=0;i<10;i++)if(m.gasInventory.getStackInSlot(i).is(MareEletrica.GAS.get()))m.gasInventory.setStackInSlot(i,new ItemStack(MareEletrica.EMPTY_GAS.get()));m.setChanged();
  }
  m.progress+=10;m.status=35;m.projectedLength=m.batchTotal;if(m.progress<40)return true;
  for(int i=0;i<10&&m.batchRemaining>0&&m.clean.stored()>=2000;i++){
   if(m.pendingOre<0)m.pendingOre=OreBatchRules.select(m.getLevel().random.nextInt(OreBatchRules.TOTAL));
   ItemStack out=new ItemStack(switch(m.pendingOre){case 0->Items.COAL;case 1->Items.RAW_IRON;case 2->Items.RAW_GOLD;case 3->Items.RAW_COPPER;case 4->Items.DIAMOND;case 6->Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE;default->Items.ANCIENT_DEBRIS;});
   if(!ItemHandlerHelper.insertItemStacked(m.inventory,out,true).isEmpty()){m.status=7;m.setChanged();return false;}
   ItemHandlerHelper.insertItemStacked(m.inventory,out,false);m.clean.pay(2000);m.batchRemaining--;m.pendingOre=-1;m.lastRate+=4000;
  }m.progress=0;if(m.batchRemaining==0)m.batchCount++;m.setChanged();return true;
 }
}
