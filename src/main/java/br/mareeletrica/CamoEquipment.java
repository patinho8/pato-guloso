package br.mareeletrica;

import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Four extra camouflage slots (hood, jacket, pants, boots) worn over any armor; no protection. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class CamoEquipment {
 private static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,MareEletrica.ID);
 public static final Supplier<AttachmentType<Gear>> TYPE=TYPES.register("camo_equipment",()->AttachmentType.serializable(Gear::new).copyOnDeath().build());
 private CamoEquipment(){}
 public static void register(IEventBus bus){TYPES.register(bus);}

 public static final class Gear extends ItemStackHandler {
  private final ItemStack[] last={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};private boolean sent;
  public Gear(){super(4);}
  @Override public boolean isItemValid(int slot,ItemStack stack){return stack.getItem() instanceof CamoArmorItem c&&index(c.getType())==slot;}
  @Override public int getSlotLimit(int slot){return 1;}
 }

 public static int index(ArmorItem.Type type){return switch(type){case HELMET->0;case CHESTPLATE->1;case LEGGINGS->2;case BOOTS->3;default->0;};}
 public static Gear inventory(Player player){return player.getData(TYPE);}
 public static ItemStack get(Player player,int slot){return inventory(player).getStackInSlot(slot);}
 public static void set(Player player,int slot,ItemStack stack){inventory(player).setStackInSlot(slot,stack);}
 public static int slot(AbstractContainerMenu menu,int index){for(var s:menu.slots)if(s instanceof CamoSlot c&&c.index()==index)return s.index;return -1;}

 public static void sync(ServerPlayer player){
  var gear=inventory(player);
  for(int i=0;i<4;i++)gear.last[i]=gear.getStackInSlot(i).copy();
  gear.sent=true;
  PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,packet(player));
 }
 private static CamoNet.Sync packet(Player p){return new CamoNet.Sync(p.getId(),get(p,0).copy(),get(p,1).copy(),get(p,2).copy(),get(p,3).copy());}
 private static boolean changed(Gear g){if(!g.sent)return true;for(int i=0;i<4;i++)if(!ItemStack.matches(g.getStackInSlot(i),g.last[i]))return true;return false;}

 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%5==0&&changed(inventory(p)))sync(p);}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent public static void track(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer viewer&&e.getTarget() instanceof Player target)PacketDistributor.sendToPlayer(viewer,packet(target));}

 public static void drop(Player p){
  if(p.level().isClientSide||p.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))return;
  for(int i=0;i<4;i++){var stack=get(p,i);if(stack.isEmpty())continue;set(p,i,ItemStack.EMPTY);p.drop(stack,true,false);}
 }
}
