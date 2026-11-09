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
 public static boolean reloading(ItemStack s){var t=RifleLoadout.tag(s);return t.contains("PendingMagazine")||t.contains("ShellLoad");}
 // ---------------- 0.80.0: guns fed with loose rounds (revolver, tube shotguns) load them one at a time ----------------
 public static boolean loose(WeaponFamily f){return f==WeaponFamily.PYTHON||f==WeaponFamily.M870||f==WeaponFamily.SPAS12;}
 public static int loosePre(WeaponFamily f){return f==WeaponFamily.PYTHON?16:10;}
 public static int loosePer(WeaponFamily f){return f==WeaponFamily.PYTHON?8:10;}
 public static int loosePost(WeaponFamily f){return f==WeaponFamily.PYTHON?12:14;}
 public static boolean shellLoading(ItemStack s){return RifleLoadout.tag(s).contains("ShellLoad");}
 /** loose rounds the player carries: the ammo item itself, then rounds inside this gun's magazines */
 static int looseAvailable(ServerPlayer p,WeaponFamily f){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.is(f.ammo()))n+=s.getCount();else if(s.getItem() instanceof MagazineItem m&&m.family==f)n+=MagazineItem.rounds(s);}return n;}
 static boolean takeLoose(ServerPlayer p,WeaponFamily f){if(p.isCreative())return true;
  for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.is(f.ammo())){s.shrink(1);return true;}}
  for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.getItem() instanceof MagazineItem m&&m.family==f){int r=MagazineItem.rounds(s);if(r>0){var t=RifleLoadout.tag(s);t.putInt("Rounds",r-1);s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return true;}}}
  return false;}
 static void reloadLoose(ServerPlayer p,ItemStack gun,WeaponFamily f){
  int missing=Accessory.capacity(gun,f)-rounds(gun);if(missing<=0)return;int n=p.isCreative()?missing:Math.min(missing,looseAvailable(p,f));
  if(n<=0){p.displayClientMessage(Component.literal("Sem "+(f==WeaponFamily.PYTHON?"balas .357":"cartuchos de escopeta")+" no inventário."),true);return;}
  p.stopUsingItem();GunItem.trigger(p,false);int had=rounds(gun);var t=RifleLoadout.tag(gun);int d=loosePre(f)+n*loosePer(f)+loosePost(f);long now=p.level().getGameTime();
  t.putInt("MagRounds",had);for(String k:new String[]{"Cylinder","Magazine","ArsenalMagazine"})t.remove(k);
  t.putInt("ShellLoad",n);t.putInt("ShellDone",0);t.putUUID("ReloadOwner",p.getUUID());t.putLong("ReloadStart",now);t.putLong("ReloadEnd",now+d);t.putInt("ReloadDuration",d);write(gun,t);
  p.getCooldowns().addCooldown(gun.getItem(),loosePre(f)+loosePer(f));
  p.level().playSound(null,p.blockPosition(),f==WeaponFamily.PYTHON?SoundEvents.IRON_TRAPDOOR_OPEN:SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.PLAYERS,.6F,1.5F);p.inventoryMenu.broadcastChanges();}
 static void tickLoose(ServerPlayer p,ItemStack gun,boolean active){var f=WeaponFamily.of(gun);var t=RifleLoadout.tag(gun);if(f==null){stopLoose(p,gun);return;}
  if(!active){stopLoose(p,gun);return;}
  long el=p.level().getGameTime()-t.getLong("ReloadStart");int n=t.getInt("ShellLoad"),done=t.getInt("ShellDone"),per=loosePer(f);
  int target=(int)Math.clamp((el-loosePre(f)+per*3/10)/per,0,n);   // each round goes in about 70% through its little cycle
  while(done<target){if(!takeLoose(p,f)||rounds(gun)>=Accessory.capacity(gun,f)){t.putInt("ShellLoad",done);target=done;break;}
   done++;t.putInt("MagRounds",t.getInt("MagRounds")+1);p.level().playSound(null,p.blockPosition(),SoundEvents.ARMOR_EQUIP_CHAIN.value(),SoundSource.PLAYERS,.5F,1.6F+p.getRandom().nextFloat()*.2F);}
  t.putInt("ShellDone",done);write(gun,t);
  if(el>=t.getInt("ReloadDuration")){stopLoose(p,gun);p.level().playSound(null,p.blockPosition(),f==WeaponFamily.PYTHON?SoundEvents.IRON_TRAPDOOR_CLOSE:SoundEvents.PISTON_CONTRACT,SoundSource.PLAYERS,.6F,1.4F);}
  else p.inventoryMenu.broadcastChanges();}
 /** ends a loose-round reload, keeping what is already loaded (also when the player fires to interrupt it) */
 public static void stopLoose(ServerPlayer p,ItemStack gun){var t=RifleLoadout.tag(gun);if(!t.contains("ShellLoad"))return;for(String k:new String[]{"ShellLoad","ShellDone","ReloadStart","ReloadEnd","ReloadDuration","ReloadOwner"})t.remove(k);write(gun,t);p.getCooldowns().removeCooldown(gun.getItem());p.inventoryMenu.broadcastChanges();}
 public static float progress(ItemStack s,long time,float partial){var t=RifleLoadout.tag(s);if(!t.contains("PendingMagazine")&&!t.contains("ShellLoad"))return 0;int duration=t.getInt("ReloadDuration");return duration<=0?0:Math.clamp((time-t.getLong("ReloadStart")+partial)/duration,0F,1F);}
 private static void write(ItemStack s,CompoundTag t){s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
 public static void reload(ServerPlayer p){var gun=p.getMainHandItem();var f=WeaponFamily.of(gun);if(f==null||!p.isAlive()||p.isSpectator()||RemoteSessions.active(p)||p.containerMenu!=p.inventoryMenu||reloading(gun)||p.getCooldowns().isOnCooldown(gun.getItem()))return;if(loose(f)){reloadLoose(p,gun,f);return;}var kit=ModularLoadout.of(p,gun);if(rounds(gun)>=Accessory.capacity(gun,f))return;ItemStack next=ItemStack.EMPTY;var reserve=kit.getStackInSlot(f.reserve());if(MagazineItem.rounds(reserve)>0)next=kit.extractItem(f.reserve(),1,false);if(next.isEmpty())for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.getItem() instanceof MagazineItem m&&m.family==f&&MagazineItem.rounds(s)>0){next=s.split(1);break;}}if(next.isEmpty()&&p.isCreative())next=MagazineItem.make(f,f.capacity);if(next.isEmpty()){p.displayClientMessage(Component.literal("Falta pente de "+f.title+" abastecido. M2 no pente para colocar cartuchos."),true);return;}p.stopUsingItem();GunItem.trigger(p,false);SmgItem.trigger(p,false);var t=RifleLoadout.tag(gun);t.put("PendingMagazine",next.save(p.registryAccess()));t.putUUID("ReloadOwner",p.getUUID());t.putLong("ReloadStart",p.level().getGameTime());int rt=Accessory.reloadTicks(gun,f);t.putLong("ReloadEnd",p.level().getGameTime()+rt);t.putInt("ReloadDuration",rt);write(gun,t);p.getCooldowns().addCooldown(gun.getItem(),rt);p.level().playSound(null,p.blockPosition(),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.PLAYERS,.65F,1.3F);p.inventoryMenu.broadcastChanges();}
 /** 0.74.0: an extended or drum magazine takes rounds from the other filled magazines until it is full */
 static int topUp(ServerPlayer p,ItemStack gun,WeaponFamily f,int loaded){int cap=Accessory.capacity(gun,f);if(loaded>=cap)return loaded;
  if(p.isCreative())return cap;
  for(int i=0;i<p.getInventory().getContainerSize()&&loaded<cap;i++){var s=p.getInventory().getItem(i);if(!(s.getItem() instanceof MagazineItem m)||m.family!=f||s==gun)continue;int r=MagazineItem.rounds(s);if(r<=0)continue;int take=Math.min(r,cap-loaded);loaded+=take;var t=RifleLoadout.tag(s);t.putInt("Rounds",r-take);s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
  return loaded;}
 public static void finish(ServerPlayer p,ItemStack gun,boolean commit){var t=RifleLoadout.tag(gun);if(!t.contains("PendingMagazine"))return;var f=WeaponFamily.of(gun);if(f==null)return;var pending=ItemStack.parseOptional(p.registryAccess(),t.getCompound("PendingMagazine"));boolean valid=pending.getItem() instanceof MagazineItem m&&m.family==f;int had=rounds(gun);var returned=commit&&valid?MagazineItem.make(f,Math.min(had,f.capacity)):pending;int loaded=commit&&valid?MagazineItem.rounds(pending):had;
  if(commit&&valid){for(int left=had-f.capacity;left>0;left-=f.capacity)ModularLoadout.give(p,MagazineItem.make(f,Math.min(left,f.capacity)));loaded=topUp(p,gun,f,loaded);}for(String k:new String[]{"PendingMagazine","ReloadStart","ReloadEnd","ReloadDuration","ReloadOwner"})t.remove(k);t.putInt("MagRounds",loaded);write(gun,t);var kit=ModularLoadout.of(p,gun);if(!returned.isEmpty()&&kit.getStackInSlot(f.reserve()).isEmpty()&&kit.isItemValid(f.reserve(),returned))kit.setStackInSlot(f.reserve(),returned);else ModularLoadout.give(p,returned);p.getCooldowns().removeCooldown(gun.getItem());p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();if(commit)p.level().playSound(null,p.blockPosition(),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.PLAYERS,.7F,1.1F);}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(!reloading(s))continue;var t=RifleLoadout.tag(s);boolean active=s==p.getMainHandItem()&&p.isAlive()&&!p.isSpectator()&&!RemoteSessions.active(p)&&p.containerMenu==p.inventoryMenu&&t.hasUUID("ReloadOwner")&&t.getUUID("ReloadOwner").equals(p.getUUID());if(t.contains("ShellLoad")){tickLoose(p,s,active);continue;}if(!active)finish(p,s,false);else if(p.level().getGameTime()>=t.getLong("ReloadEnd"))finish(p,s,true);}}
 @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)for(int i=0;i<p.getInventory().getContainerSize();i++){stopLoose(p,p.getInventory().getItem(i));finish(p,p.getInventory().getItem(i),false);}}
 @SubscribeEvent public static void toss(net.neoforged.neoforge.event.entity.item.ItemTossEvent e){if(e.getPlayer() instanceof ServerPlayer p){var it=e.getEntity().getItem();var t=RifleLoadout.tag(it);if(t.contains("ShellLoad")){for(String k:new String[]{"ShellLoad","ShellDone","ReloadStart","ReloadEnd","ReloadDuration","ReloadOwner"})t.remove(k);it.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}finish(p,it,false);}}
}
