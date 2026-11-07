package br.mareeletrica;
import br.mareeletrica.client.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** A real integrated survival world; assertions use production handlers and NBT. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class ArmoryIntegration {
 static int stage,ticks,wait,visual=-1,serverAge;static volatile boolean checked,prepared;static volatile String failure;static ItemStack timerGun;
 record View(WeaponFamily family,int skin,int optic,String pose){}
 static final List<View> VIEWS=new ArrayList<>();
 static {for(var f:WeaponFamily.values()){VIEWS.add(new View(f,0,0,"hip"));VIEWS.add(new View(f,0,0,"iron"));VIEWS.add(new View(f,2,1,"optic"));VIEWS.add(new View(f,1,3,"workshop"));for(int skin=1;skin<=6;skin++)VIEWS.add(new View(f,skin,0,"finish"));}VIEWS.add(new View(WeaponFamily.DESERT_EAGLE,0,0,"reload"));VIEWS.add(new View(WeaponFamily.USP,2,2,"walking"));VIEWS.add(new View(WeaponFamily.MP5,5,1,"walking"));VIEWS.add(new View(WeaponFamily.DESERT_EAGLE,0,0,"recoil"));}
 static {if(Boolean.getBoolean("mareeletrica.quickArmory")){VIEWS.clear();for(var f:WeaponFamily.values()){VIEWS.add(new View(f,2,1,"optic"));VIEWS.add(new View(f,1,3,"workshop"));}VIEWS.add(new View(WeaponFamily.USP,2,2,"walking"));VIEWS.add(new View(WeaponFamily.DESERT_EAGLE,0,0,"reload"));}}
 static void log(String s){System.out.println("ARMORY21: "+s);try{Files.writeString(Path.of("armory21-test.txt"),s+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception e){throw new RuntimeException(e);}}
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static int inventoryRounds(ServerPlayer p,WeaponFamily f){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.is(f.ammo()))n+=s.getCount();if(s.getItem() instanceof MagazineItem m&&m.family==f)n+=MagazineItem.rounds(s);}return n;}
 static void reset(ServerPlayer p){p.closeContainer();p.stopUsingItem();GunItem.trigger(p,false);SmgItem.trigger(p,false);for(var f:WeaponFamily.values())p.getCooldowns().removeCooldown(f.item());p.getInventory().clearContent();p.getInventory().selected=0;}
 static void checks(ServerPlayer p){
  for(var f:WeaponFamily.values()){
   reset(p);var mag=MagazineItem.make(f,0);p.setItemInHand(InteractionHand.MAIN_HAND,mag);p.getInventory().setItem(1,new ItemStack(f.ammo(),f.capacity+3));((MagazineItem)mag.getItem()).use(p.level(),p,InteractionHand.MAIN_HAND);mag=p.getMainHandItem();check(MagazineItem.rounds(mag)==f.capacity,"M2 fill "+f);check(p.getInventory().getItem(1).getCount()==3,"no ammo creation "+f);
   var gun=new ItemStack(f.item());p.setItemInHand(InteractionHand.MAIN_HAND,gun);var kit=ModularLoadout.of(p,gun);kit.setStackInSlot(f.reserve(),mag);var other=WeaponFamily.values()[(f.ordinal()+1)%8];check(!kit.isItemValid(f.reserve(),MagazineItem.make(other,other.capacity)),"wrong magazine rejected "+f);check(!kit.isItemValid(0,new ItemStack(ArmoryParts.module(other,WeaponModuleItem.Type.OPTIC,1))),"wrong optic rejected "+f);check(!kit.isItemValid(1,new ItemStack(ArmoryParts.module(other,WeaponModuleItem.Type.SUPPRESSOR,1))),"wrong suppressor rejected "+f);
   Magazines.reload(p);check(Magazines.reloading(gun)&&Magazines.rounds(gun)==0,"reload is escrow, not immediate "+f);check(ModularLoadout.of(p,gun).getStackInSlot(f.reserve()).isEmpty(),"single magazine extracted "+f);Magazines.finish(p,gun,false);check(Magazines.rounds(gun)==0&&MagazineItem.rounds(ModularLoadout.of(p,gun).getStackInSlot(f.reserve()))==f.capacity,"cancel conservation "+f);
   Magazines.reload(p);Magazines.finish(p,gun,true);check(Magazines.rounds(gun)==f.capacity,"commit "+f);check(ModularLoadout.of(p,gun).getStackInSlot(f.reserve()).getItem() instanceof MagazineItem,"empty returned "+f);
   Magazines.rounds(gun,2);kit=ModularLoadout.of(p,gun);kit.setStackInSlot(f.reserve(),MagazineItem.make(f,f.capacity));Magazines.reload(p);var saved=ItemStack.parseOptional(p.registryAccess(),(CompoundTag)gun.save(p.registryAccess()));check(Magazines.reloading(saved),"pending serialized "+f);Magazines.finish(p,gun,true);check(Magazines.rounds(gun)==f.capacity&&MagazineItem.rounds(ModularLoadout.of(p,gun).getStackInSlot(f.reserve()))==2,"tactical reload keeps leftovers "+f);
   int before=Magazines.rounds(gun);if(f==WeaponFamily.SNIPER)SniperItem.fire(p);else if(f==WeaponFamily.SMG)SmgItem.fire(p);else GunItem.fire(p);check(Magazines.rounds(gun)==before-1,"fire consumes one round "+f);if(f==WeaponFamily.SNIPER)SniperItem.fire(p);else if(f==WeaponFamily.SMG)SmgItem.fire(p);else GunItem.fire(p);check(Magazines.rounds(gun)==before-1,"server cooldown "+f);
   // Legacy in-gun loose ammunition becomes one partial/full magazine, surplus returns once.
   reset(p);gun=new ItemStack(f.item());p.setItemInHand(InteractionHand.MAIN_HAND,gun);var tag=RifleLoadout.tag(gun);var list=new ListTag();var entry=new CompoundTag();entry.putInt("Slot",f.reserve());entry.put("Item",new ItemStack(f.ammo(),f.capacity+3).save(p.registryAccess()));list.add(entry);var accessory=new CompoundTag();accessory.putInt("Slot",1);accessory.put("Item",new ItemStack(MareEletrica.SUPPRESSORS.get(0).get()).save(p.registryAccess()));list.add(accessory);tag.put(f.modules(),list);gun.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));kit=ModularLoadout.of(p,gun);check(MagazineItem.rounds(kit.getStackInSlot(f.reserve()))==f.capacity,"legacy mag "+f);check(inventoryRounds(p,f)==3,"legacy surplus "+f);check(kit.getStackInSlot(1).getItem() instanceof WeaponModuleItem m&&m.family==f,"legacy accessory conversion "+f);ModularLoadout.of(p,gun);check(inventoryRounds(p,f)==3,"migration runs once "+f);
   reset(p);gun=new ItemStack(f.item());p.setItemInHand(InteractionHand.MAIN_HAND,gun);kit=ModularLoadout.of(p,gun);kit.setStackInSlot(f.reserve(),MagazineItem.make(f,f.capacity));Magazines.reload(p);if(f==WeaponFamily.SNIPER)SniperItem.open(p);else if(f==WeaponFamily.SMG)SmgItem.open(p);else GunItem.open(p);check(!Magazines.reloading(gun)&&Magazines.rounds(gun)==0&&MagazineItem.rounds(ModularLoadout.of(p,gun).getStackInSlot(f.reserve()))==f.capacity,"workbench cancellation "+f);p.closeContainer();
   log("PASS family "+f+": fill, slots, escrow, cancel, tactical, serialization, firing, cooldown, migration");
  }
  check(WeaponProfile.DESERT_EAGLE.recoil>=18,"DE recoil");check(WeaponProfile.M870.damage*WeaponProfile.M870.pellets>=36,"shotgun damage");check(WeaponProfile.MP5.aimSpread>=1.25F,"MP5 spread");
  reset(p);timerGun=new ItemStack(MareEletrica.REVOLVER.get());p.setItemInHand(InteractionHand.MAIN_HAND,timerGun);var kit=ModularLoadout.of(p,timerGun);kit.setStackInSlot(3,MagazineItem.make(WeaponFamily.DESERT_EAGLE,7));Magazines.reload(p);serverAge=0;prepared=true;
 }
 @SubscribeEvent public static void serverTick(PlayerTickEvent.Post e){if(!Boolean.getBoolean("mareeletrica.update21Test")||!(e.getEntity() instanceof ServerPlayer p)||!prepared||checked)return;try{serverAge++;if(serverAge<40)check(Magazines.rounds(timerGun)==0,"timer did not commit early");if(serverAge>=45){check(!Magazines.reloading(timerGun)&&Magazines.rounds(timerGun)==7,"actual tick completion");checked=true;log("PASS actual timed reload (42 ticks)");}}catch(Throwable t){failure=t.toString();log("FAIL "+failure);}}
 static void prepareView(ServerPlayer p,int index){reset(p);var v=VIEWS.get(index);var gun=new ItemStack(v.family.item());p.setItemInHand(InteractionHand.MAIN_HAND,gun);var kit=ModularLoadout.of(p,gun);if(v.skin>0)kit.setStackInSlot(v.family==WeaponFamily.SNIPER?3:2,new ItemStack(ArmoryParts.module(v.family,WeaponModuleItem.Type.SKIN,v.skin)));if(v.optic>0)kit.setStackInSlot(0,new ItemStack(ArmoryParts.module(v.family,WeaponModuleItem.Type.OPTIC,v.optic)));if(v.pose.equals("workshop"))kit.setStackInSlot(1,new ItemStack(ArmoryParts.module(v.family,WeaponModuleItem.Type.SUPPRESSOR,3)));kit.rounds(v.pose.equals("reload")?0:v.family.capacity);kit.setStackInSlot(v.family.reserve(),MagazineItem.make(v.family,v.family.capacity));var t=RifleLoadout.tag(gun);t.putInt("VisualIndex",index);gun.set(DataComponents.CUSTOM_DATA,CustomData.of(t));p.connection.teleport(8.5,-60,8.5,180,0);p.getFoodData().setFoodLevel(20);p.inventoryMenu.broadcastChanges();}
 static void screenshot(String name){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),c->{});log("FRAME "+name);}
 @SubscribeEvent public static void clientTick(ClientTickEvent.Post e){if(!Boolean.getBoolean("mareeletrica.update21Test"))return;var mc=Minecraft.getInstance();try{
  if(failure!=null)throw new AssertionError(failure);if(++ticks>6000)throw new AssertionError("timeout stage="+stage);
  if(stage==0&&mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen){mc.setScreen(new TitleScreen());return;}
  if(stage==0&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){stage=1;mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(3);mc.options.framerateLimit().set(40);mc.options.guiScale().set(2);mc.options.languageCode="pt_br";mc.options.bobView().set(false);var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0,null);log("START NeoForge="+net.neoforged.fml.ModList.get().getModContainerById("neoforge").get().getModInfo().getVersion()+" Punchy="+net.neoforged.fml.ModList.get().isLoaded("punchy"));mc.createWorldOpenFlows().createFreshLevel("armory-"+System.currentTimeMillis(),new LevelSettings("Armory test",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,rules,WorldDataConfiguration.DEFAULT),new WorldOptions(1L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),new TitleScreen());return;}
  if(stage==1&&mc.player!=null&&mc.level!=null&&mc.screen==null){stage=2;wait=0;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.connection.teleport(8.5,-60,8.5,180,0);p.serverLevel().setDayTime(6000);});}
  if(stage==2&&++wait>60){stage=3;mc.getSingleplayerServer().execute(()->{try{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());check(p.serverLevel().isPositionEntityTicking(p.blockPosition()),"player entity chunks ready");checks(p);}catch(Throwable t){failure=t.toString();}});}
  if(stage==3&&checked){stage=4;wait=0;log("PASS all server invariants");}
  if(stage==4){if(++visual>=VIEWS.size()){log("PASS visual cycle "+VIEWS.size()+" scenes; completed");stage=9;wait=0;return;}int index=visual;mc.options.keyUse.setDown(false);mc.options.keyUp.setDown(false);if(mc.screen!=null)mc.player.closeContainer();if(mc.player.isUsingItem())mc.gameMode.releaseUsingItem(mc.player);SniperClient.kick=0;mc.getSingleplayerServer().execute(()->prepareView(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()),index));stage=5;wait=0;}
  if(stage==5&&RifleLoadout.tag(mc.player.getMainHandItem()).getInt("VisualIndex")==visual&&WeaponFamily.of(mc.player.getMainHandItem())==VIEWS.get(visual).family){if(++wait<10)return;var v=VIEWS.get(visual);if(v.pose.equals("iron")||v.pose.equals("optic")||v.pose.equals("walking")){mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}if(v.pose.equals("workshop"))mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(v.family==WeaponFamily.SNIPER)SniperItem.open(p);else if(v.family==WeaponFamily.SMG)SmgItem.open(p);else GunItem.open(p);});if(v.pose.equals("reload"))mc.getSingleplayerServer().execute(()->Magazines.reload(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));stage=6;wait=0;}
  if(stage==6){wait++;var v=VIEWS.get(visual);String name=String.format("%02d-%s-%s-%d",visual,v.family.id,v.pose,v.skin);if(v.pose.equals("walking"))mc.options.keyUp.setDown(true);if(v.pose.equals("reload")){if(wait==12||wait==22||wait==34)screenshot(name+"-t"+wait);if(wait>=55){stage=4;wait=0;}return;}if(v.pose.equals("recoil")&&wait==15){mc.getSingleplayerServer().execute(()->GunItem.fire(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}if(wait>=18){screenshot(name);stage=4;wait=0;}}
  if(stage==9&&++wait>25){mc.stop();}
 }catch(Throwable t){log("FAIL "+t);stage=99;mc.stop();throw new RuntimeException(t);}}
}
