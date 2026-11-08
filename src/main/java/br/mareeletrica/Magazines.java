package br.mareeletrica;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
/** Pending magazines live on the actual gun. Commit only after the animation; cancel returns escrow. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class Magazines {
 public static int rounds(ItemStack s){var f=WeaponFamily.of(s);if(f==null)return 0;var t=RifleLoadout.tag(s);String key=t.contains("MagRounds")?"MagRounds":f==WeaponFamily.SMG?"Magazine":t.contains("ArsenalMagazine")?"ArsenalMagazine":"Cylinder";return Math.clamp(t.getInt(key),0,Accessory.capacity(s,f));}
 public static void rounds(ItemStack s,int n){var f=WeaponFamily.of(s);if(f==null)return;var t=RifleLoadout.tag(s);t.putInt("MagRounds",Math.clamp(n,0,Accessory.capacity(s,f)));for(String k:new String[]{"Cylinder","Magazine","ArsenalMagazine"})t.remove(k);write(s,t);}
 public static boolean reloading(ItemStack s){return RifleLoadout.tag(s).contains("PendingMagazine");}
 public static float progress(ItemStack s,long time,float partial){var t=RifleLoadout.tag(s);if(!t.contains("PendingMagazine"))return 0;int duration=t.getInt("ReloadDuration");return duration<=0?0:Math.clamp((time-t.getLong("ReloadStart")+partial)/duration,0F,1F);}
 private static void write(ItemStack s,CompoundTag t){s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
 public static void reload(ServerPlayer p){var gun=p.getMainHandItem();var f=WeaponFamily.of(gun);if(f==null||!p.isAlive()||p.isSpectator()||RemoteSessions.active(p)||p.containerMenu!=p.inventoryMenu||reloading(gun)||p.getCooldowns().isOnCooldown(gun.getItem()))return;var kit=ModularLoadout.of(p,gun);if(rounds(gun)>=Accessory.capacity(gun,f))return;ItemStack next=ItemStack.EMPTY;var reserve=kit.getStackInSlot(f.reserve());if(MagazineItem.rounds(reserve)>0)next=kit.extractItem(f.reserve(),1,false);if(next.isEmpty())for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.getItem() instanceof MagazineItem m&&m.family==f&&MagazineItem.rounds(s)>0){next=s.split(1);break;}}if(next.isEmpty()&&p.isCreative())next=MagazineItem.make(f,f.capacity);if(next.isEmpty()){p.displayClientMessage(Component.literal("Falta pente de "+f.title+" abastecido. M2 no pente para colocar cartuchos."),true);return;}p.stopUsingItem();GunItem.trigger(p,false);SmgItem.trigger(p,false);var t=RifleLoadout.tag(gun);t.put("PendingMagazine",next.save(p.registryAccess()));t.putUUID("ReloadOwner",p.getUUID());t.putLong("ReloadStart",p.level().getGameTime());int rt=Accessory.reloadTicks(gun,f);t.putLong("ReloadEnd",p.level().getGameTime()+rt);t.putInt("ReloadDuration",rt);write(gun,t);p.getCooldowns().addCooldown(gun.getItem(),rt);p.level().playSound(null,p.blockPosition(),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.PLAYERS,.65F,1.3F);p.inventoryMenu.broadcastChanges();}
 /** 0.74.0: an extended or drum magazine takes rounds from the other filled magazines until it is full */
 static int topUp(ServerPlayer p,ItemStack gun,WeaponFamily f,int loaded){int cap=Accessory.capacity(gun,f);if(loaded>=cap)return loaded;
  if(p.isCreative())return cap;
  for(int i=0;i<p.getInventory().getContainerSize()&&loaded<cap;i++){var s=p.getInventory().getItem(i);if(!(s.getItem() instanceof MagazineItem m)||m.family!=f||s==gun)continue;int r=MagazineItem.rounds(s);if(r<=0)continue;int take=Math.min(r,cap-loaded);loaded+=take;var t=RifleLoadout.tag(s);t.putInt("Rounds",r-take);s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
  return loaded;}
 public static void finish(ServerPlayer p,ItemStack gun,boolean commit){var t=RifleLoadout.tag(gun);if(!t.contains("PendingMagazine"))return;var f=WeaponFamily.of(gun);if(f==null)return;var pending=ItemStack.parseOptional(p.registryAccess(),t.getCompound("PendingMagazine"));boolean valid=pending.getItem() instanceof MagazineItem m&&m.family==f;int had=rounds(gun);var returned=commit&&valid?MagazineItem.make(f,Math.min(had,f.capacity)):pending;int loaded=commit&&valid?MagazineItem.rounds(pending):had;
  if(commit&&valid){for(int left=had-f.capacity;left>0;left-=f.capacity)ModularLoadout.give(p,MagazineItem.make(f,Math.min(left,f.capacity)));loaded=topUp(p,gun,f,loaded);}for(String k:new String[]{"PendingMagazine","ReloadStart","ReloadEnd","ReloadDuration","ReloadOwner"})t.remove(k);t.putInt("MagRounds",loaded);write(gun,t);var kit=ModularLoadout.of(p,gun);if(!returned.isEmpty()&&kit.getStackInSlot(f.reserve()).isEmpty()&&kit.isItemValid(f.reserve(),returned))kit.setStackInSlot(f.reserve(),returned);else ModularLoadout.give(p,returned);p.getCooldowns().removeCooldown(gun.getItem());p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();if(commit)p.level().playSound(null,p.blockPosition(),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.PLAYERS,.7F,1.1F);}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(!reloading(s))continue;var t=RifleLoadout.tag(s);boolean active=s==p.getMainHandItem()&&p.isAlive()&&!p.isSpectator()&&!RemoteSessions.active(p)&&p.containerMenu==p.inventoryMenu&&t.hasUUID("ReloadOwner")&&t.getUUID("ReloadOwner").equals(p.getUUID());if(!active)finish(p,s,false);else if(p.level().getGameTime()>=t.getLong("ReloadEnd"))finish(p,s,true);}}
 @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)for(int i=0;i<p.getInventory().getContainerSize();i++)finish(p,p.getInventory().getItem(i),false);}
 @SubscribeEvent public static void toss(net.neoforged.neoforge.event.entity.item.ItemTossEvent e){if(e.getPlayer() instanceof ServerPlayer p)finish(p,e.getEntity().getItem(),false);}
}
