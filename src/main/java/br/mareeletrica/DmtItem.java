package br.mareeletrica;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class DmtItem extends Item {
 public static final int CAPACITY=2400,CANISTER=1200;
 public DmtItem(Properties p){super(p);}
 public static ItemStack equipped(Player p){return DmtEquipment.get(p);}
 public static boolean enabled(Player p){var s=equipped(p);return !s.isEmpty()&&!s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean("DmtDisabled");}
 public static void toggle(ServerPlayer p){var s=equipped(p);if(s.isEmpty())return;boolean was=enabled(p);var t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();t.putBoolean("DmtDisabled",was);t.putInt("DmtWeapon",0);s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));DmtSystem.clear(p);DmtEquipment.sync(p);p.displayClientMessage(Component.literal(was?"DMT desativado — comandos normais liberados":"DMT ativado"),true);}
 public static int fuel(ItemStack s){return Math.max(0,Math.min(CAPACITY,s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("DmtGas")));}
 public static void fuel(ItemStack s,int amount){CompoundTag t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();t.putInt("DmtGas",Math.max(0,Math.min(CAPACITY,amount)));s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
 public static boolean refill(Player p){
  ItemStack s=equipped(p);if(s.isEmpty()||fuel(s)>CAPACITY-CANISTER)return false;
  var kit=DmtLoadout.of(p);if(kit.getStackInSlot(3).is(MareEletrica.GAS.get())){kit.extractItem(3,1,false);fuel(s,fuel(s)+CANISTER);var empty=new ItemStack(MareEletrica.EMPTY_GAS.get());if(!p.getInventory().add(empty))p.drop(empty,false);return true;}
  for(int i=0;i<p.getInventory().getContainerSize();i++){var gas=p.getInventory().getItem(i);if(gas.is(MareEletrica.GAS.get())){gas.shrink(1);fuel(s,fuel(s)+CANISTER);var empty=new ItemStack(MareEletrica.EMPTY_GAS.get());if(!p.getInventory().add(empty))p.drop(empty,false);p.getInventory().setChanged();return true;}}
  return false;
 }
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){var held=p.getItemInHand(hand);if(!equipped(p).isEmpty())return InteractionResultHolder.fail(held);if(!l.isClientSide){DmtEquipment.set(p,held.split(1));if(p instanceof ServerPlayer sp)DmtEquipment.sync(sp);}return InteractionResultHolder.sidedSuccess(held,l.isClientSide);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("hint.mareeletrica.dmt"));lines.add(Component.translatable("dmt.mareeletrica.fuel",fuel(s)/20,CAPACITY/20));}
 @Override public boolean isBarVisible(ItemStack s){return true;}
 @Override public int getBarWidth(ItemStack s){return Math.round(13F*fuel(s)/CAPACITY);}
 @Override public int getBarColor(ItemStack s){return 0x43DDD0;}
}
