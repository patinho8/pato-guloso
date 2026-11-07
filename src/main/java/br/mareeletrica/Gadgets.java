package br.mareeletrica;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** Hand-held electronics: binoculars (with range finder), flashlight, ore detector, GPS, walkie-talkie,
 *  thermal goggles (head slot) — and a strawberry Danoninho. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class Gadgets {
 public static final DeferredItem<Item> BINOCULARS=MareEletrica.ITEMS.register("binoculars",()->new BinocularsItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> FLASHLIGHT=MareEletrica.ITEMS.register("flashlight",()->new FlashlightItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> GPS=MareEletrica.ITEMS.register("gps",()->new GpsItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> WALKIE_TALKIE=MareEletrica.ITEMS.register("walkie_talkie",()->new WalkieItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> THERMAL_GOGGLES=MareEletrica.ITEMS.register("thermal_goggles",()->new GogglesItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> DANONINHO=MareEletrica.ITEMS.register("danoninho",()->new Item(new Item.Properties().stacksTo(16)
  .food(new FoodProperties.Builder().nutrition(4).saturationModifier(.6F).alwaysEdible().fast().build())));
 private Gadgets(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){for(var i:List.of(BINOCULARS,FLASHLIGHT,GPS,WALKIE_TALKIE,THERMAL_GOGGLES,DANONINHO))out.accept(i.get());}
 static CompoundTag data(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?new CompoundTag():c.copyTag();}
 static void save(ItemStack s,CompoundTag t){s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
 public static boolean wearsGoggles(Player p){return p.getItemBySlot(EquipmentSlot.HEAD).is(THERMAL_GOGGLES.get());}

 /** Hold right-click: 12x zoom; the screen shows the distance to what you aim at. Far terrain and mobs load like the sniper scope. */
 public static final class BinocularsItem extends Item {
  public BinocularsItem(Properties p){super(p);}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
  @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.SPYGLASS;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){p.playSound(SoundEvents.SPYGLASS_USE,1F,1F);p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
  @Override public void releaseUsing(ItemStack s,Level l,LivingEntity e,int left){e.playSound(SoundEvents.SPYGLASS_STOP_USING,1F,1F);if(e instanceof ServerPlayer sp)ScopeSight.clear(sp,true);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("Segure o botão direito: zoom 12x e distância até o alvo"));}
 }

 /** Right-click switches it on: a real light lands where you point it (up to 32 blocks). */
 public static final class FlashlightItem extends Item {
  public FlashlightItem(Properties p){super(p);}
  public static boolean on(ItemStack s){return data(s).getBoolean("On");}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);if(!l.isClientSide){var t=data(s);t.putBoolean("On",!t.getBoolean("On"));save(s,t);l.playSound(null,p.blockPosition(),SoundEvents.LEVER_CLICK,SoundSource.PLAYERS,.6F,t.getBoolean("On")?1.6F:1.2F);}
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
  }
  @Override public boolean isFoil(ItemStack s){return on(s);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal(on(s)?"§eLigada":"§7Desligada")); t.add(Component.literal("Clique direito: liga/desliga • ilumina onde você aponta"));}
 }
 private static final Map<UUID,BlockPos[]> BEAMS=new HashMap<>();
 private static void clearBeam(Level l,UUID id){var old=BEAMS.remove(id);if(old!=null)for(var b:old)if(b!=null&&l.getBlockState(b).is(Blocks.LIGHT))l.removeBlock(b,false);}
 private static void putLight(Level l,BlockPos p,int level,BlockPos[] slot,int i){
  if(p.equals(slot[i]))return;
  if(slot[i]!=null&&l.getBlockState(slot[i]).is(Blocks.LIGHT))l.removeBlock(slot[i],false);slot[i]=null;
  if(l.getBlockState(p).isAir()){l.setBlock(p,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,level),3);slot[i]=p;}
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;
  var main=p.getMainHandItem();var off=p.getOffhandItem();
  boolean light=main.is(FLASHLIGHT.get())&&FlashlightItem.on(main)||off.is(FLASHLIGHT.get())&&FlashlightItem.on(off);
  if(!light){if(BEAMS.containsKey(p.getUUID()))clearBeam(p.level(),p.getUUID());return;}
  if(p.tickCount%2!=0)return;
  var slot=BEAMS.computeIfAbsent(p.getUUID(),k->new BlockPos[2]);
  Vec3 eye=p.getEyePosition(),end=eye.add(p.getLookAngle().scale(32));
  var hit=p.level().clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
  Vec3 spot=hit.getType()==HitResult.Type.MISS?end:hit.getLocation().subtract(p.getLookAngle().scale(.5));
  putLight(p.level(),BlockPos.containing(spot),14,slot,0);
  putLight(p.level(),BlockPos.containing(eye.add(p.getLookAngle().scale(1.5))),9,slot,1);   // a little spill around you
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){clearBeam(e.getEntity().level(),e.getEntity().getUUID());}

 /** Right-click: scans 16 blocks around for ores and reports the most valuable close one. */
 public static final class DetectorItem extends Item {
  public DetectorItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);
   if(!l.isClientSide){
    BlockPos best=null;double bd=Double.MAX_VALUE;int bv=0;var c=p.blockPosition();
    for(var q:BlockPos.betweenClosed(c.offset(-16,-16,-16),c.offset(16,16,16))){
     var st=l.getBlockState(q);if(!st.is(net.neoforged.neoforge.common.Tags.Blocks.ORES))continue;
     int v=value(st);double d=q.distSqr(c);if(v>bv||v==bv&&d<bd){bv=v;bd=d;best=q.immutable();}
    }
    if(best==null){p.displayClientMessage(Component.literal("§7Detector: nada num raio de 16 blocos."),true);l.playSound(null,c,SoundEvents.NOTE_BLOCK_BASS.value(),SoundSource.PLAYERS,.6F,.6F);}
    else{
     int dist=(int)Math.round(Math.sqrt(bd));var name=l.getBlockState(best).getBlock().getName().getString();
     int dy=best.getY()-c.getY();
     p.displayClientMessage(Component.literal("§bDetector: "+name+" a "+dist+" m "+(dy<-1?"(abaixo "+(-dy)+")":dy>1?"(acima "+dy+")":"(no seu nível)")+" "+direction(p,best)),true);
     l.playSound(null,c,SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.PLAYERS,.7F,2F-Math.min(1.4F,dist/12F));
    }
    p.getCooldowns().addCooldown(this,30);
   }
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
  }
  private static int value(net.minecraft.world.level.block.state.BlockState s){
   if(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_NETHERITE_SCRAP))return 9;if(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_DIAMOND))return 8;if(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_EMERALD))return 7;
   if(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_GOLD))return 6;if(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_LAPIS))return 5;if(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_REDSTONE))return 4;
   if(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_IRON))return 3;if(s.is(net.neoforged.neoforge.common.Tags.Blocks.ORES_COPPER))return 2;return 1;
  }
 }
 /** "à frente", "à direita"... relative to where the player looks. */
 public static String direction(Player p,BlockPos to){
  double dx=to.getX()+.5-p.getX(),dz=to.getZ()+.5-p.getZ();if(dx*dx+dz*dz<4)return "";
  float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));float rel=net.minecraft.util.Mth.wrapDegrees(yaw-p.getYRot());
  return rel>-30&&rel<30?"à frente":rel>=30&&rel<150?"à direita":rel<=-30&&rel>-150?"à esquerda":"atrás";
 }

 /** Shows your position on screen; Shift + right-click marks a waypoint, then it points the way and the distance. */
 public static final class GpsItem extends Item {
  public GpsItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);
   if(!l.isClientSide){
    var t=data(s);
    if(p.isShiftKeyDown()){t.putLong("Mark",p.blockPosition().asLong());t.putString("Dim",l.dimension().location().toString());save(s,t);p.displayClientMessage(Component.literal("§aGPS: ponto marcado em "+p.blockPosition().toShortString()),true);}
    else p.displayClientMessage(Component.literal("§bGPS: "+p.blockPosition().toShortString()),true);
    l.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.PLAYERS,.5F,1.8F);
   }
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
  }
  public static BlockPos mark(ItemStack s){var t=data(s);return t.contains("Mark")?BlockPos.of(t.getLong("Mark")):null;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){var m=mark(s);t.add(Component.literal(m==null?"Shift + clique: marcar ponto":"Ponto: "+m.toShortString()));}
 }

 /** Right-click: every player carrying a walkie-talkie hears the call and gets your position. */
 public static final class WalkieItem extends Item {
  public WalkieItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);
   if(!l.isClientSide&&p instanceof ServerPlayer sp){
    for(var other:sp.server.getPlayerList().getPlayers()){
     if(!other.getInventory().contains(new ItemStack(WALKIE_TALKIE.get())))continue;
     String where=other.level()==l?(int)other.distanceTo(p)+" m "+(other==p?"":direction(other,p.blockPosition())):"outra dimensão";
     other.displayClientMessage(Component.literal("§6📻 "+p.getName().getString()+": §fCâmbio! Estou em "+p.blockPosition().toShortString()+" §7("+where+")"),false);
     other.playNotifySound(SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.PLAYERS,.8F,1.4F);
    }
    p.getCooldowns().addCooldown(this,40);
   }
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("Clique direito: chama todos com rádio e manda sua posição"));}
 }

 /** Worn on the head (helmet slot): thermal vision — the world goes dark, warm bodies glow white-hot. */
 public static final class GogglesItem extends Item implements Equipable {
  public GogglesItem(Properties p){super(p);}
  @Override public EquipmentSlot getEquipmentSlot(){return EquipmentSlot.HEAD;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){return swapWithEquipmentSlot(this,l,p,h);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("Vista no espaço do capacete: visão térmica"));}
 }
}
