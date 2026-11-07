package br.mareeletrica;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** One persistent equipment slot; synchronized separately for other players' waist models. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class DmtEquipment {
 private static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,MareEletrica.ID);
 public static final java.util.function.Supplier<AttachmentType<Gear>> TYPE=TYPES.register("dmt_equipment",()->AttachmentType.serializable(Gear::new).copyOnDeath().build());
 private DmtEquipment(){}
 public static void register(IEventBus bus){TYPES.register(bus);}
 public static final class Gear extends ItemStackHandler {
  private ItemStack lastSent=ItemStack.EMPTY;private boolean sent;
  public Gear(){super(1);}
  @Override public boolean isItemValid(int slot,ItemStack stack){return stack.is(MareEletrica.DMT.get());}
  @Override public int getSlotLimit(int slot){return 1;}
 }
 public static Gear inventory(Player player){return player.getData(TYPE);}
 public static ItemStack get(Player player){return inventory(player).getStackInSlot(0);}
 public static int slot(AbstractContainerMenu menu){for(var s:menu.slots)if(s instanceof DmtSlot)return s.index;return -1;}
 public static void set(Player player,ItemStack stack){inventory(player).setStackInSlot(0,stack);}
 public static void sync(ServerPlayer player){var gear=inventory(player);gear.lastSent=get(player).copy();gear.sent=true;PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,new DmtPackets.Equipment(player.getId(),gear.lastSent.copy()));}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%5==0){var g=inventory(p);if(!g.sent||!ItemStack.matches(get(p),g.lastSent))sync(p);}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent public static void track(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer viewer&&e.getTarget() instanceof Player target)PacketDistributor.sendToPlayer(viewer,new DmtPackets.Equipment(target.getId(),get(target).copy()));}
 public static void drop(Player p){
  if(p.level().isClientSide||p.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))return;
  var stack=get(p);if(stack.isEmpty())return;set(p,ItemStack.EMPTY);
  if(!net.minecraft.world.item.enchantment.EnchantmentHelper.has(stack,net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP))p.drop(stack,true,false);
 }
}
