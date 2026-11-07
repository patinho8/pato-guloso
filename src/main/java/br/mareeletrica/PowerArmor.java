package br.mareeletrica;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Five powered armour suits. The full set gives a power while the chestplate has charge (Shift + right-click the chestplate to charge it with redstone). */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class PowerArmor {
 public enum Suit {
  JUMP("salto","Armadura de Salto","superpulo, sem dano de queda"),
  SPEED("velocidade","Armadura de Velocidade","corrida muito rápida e mineração rápida"),
  FLIGHT("voo","Armadura de Voo","voa (aperte espaço duas vezes)"),
  TITAN("tita","Armadura Titã","força, resistência e não é empurrado"),
  SHADOW("sombra","Armadura Sombra","visão noturna, respira na água, invisível agachado");
  public final String id,title,power;
  Suit(String id,String title,String power){this.id=id;this.title=title;this.power=power;}
 }
 public static final int MAX_CHARGE=24000;   // 20 minutes of use
 private static final DeferredRegister<ArmorMaterial> MATERIALS=DeferredRegister.create(Registries.ARMOR_MATERIAL,MareEletrica.ID);
 private static final Map<Suit,DeferredHolder<ArmorMaterial,ArmorMaterial>> MATERIAL=new java.util.EnumMap<>(Suit.class);
 public static final List<DeferredItem<SuitItem>> ITEMS=new ArrayList<>();
 private static final ArmorItem.Type[] TYPES={ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS};
 static{
  for(var suit:Suit.values()){
   MATERIAL.put(suit,MATERIALS.register("power_"+suit.id,()->new ArmorMaterial(Map.of(ArmorItem.Type.HELMET,3,ArmorItem.Type.CHESTPLATE,7,ArmorItem.Type.LEGGINGS,5,ArmorItem.Type.BOOTS,3,ArmorItem.Type.BODY,7),
    12,SoundEvents.ARMOR_EQUIP_NETHERITE,()->Ingredient.of(Items.IRON_INGOT),List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"power_"+suit.id))),1.5F,.05F)));
   for(var type:TYPES)ITEMS.add(MareEletrica.ITEMS.register("power_"+suit.id+"_"+type.getName(),()->new SuitItem(suit,type)));
  }
 }
 private PowerArmor(){}
 public static void register(IEventBus bus){MATERIALS.register(bus);}
 public static void creative(CreativeModeTab.Output out){for(var i:ITEMS){var s=new ItemStack(i.get());if(i.get().getType()==ArmorItem.Type.CHESTPLATE)setCharge(s,MAX_CHARGE);out.accept(s);}}

 public static int charge(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?0:c.copyTag().getInt("Charge");}
 public static void setCharge(ItemStack s,int v){var c=s.get(DataComponents.CUSTOM_DATA);var t=c==null?new CompoundTag():c.copyTag();t.putInt("Charge",Math.max(0,Math.min(MAX_CHARGE,v)));s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
 /** The suit the player wears completely, or null. */
 public static Suit fullSet(Player p){
  Suit suit=null;
  for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){
   if(!(p.getItemBySlot(slot).getItem() instanceof SuitItem si))return null;
   if(suit==null)suit=si.suit;else if(suit!=si.suit)return null;
  }
  return suit;
 }
 public static boolean powered(Player p){return fullSet(p)!=null&&charge(p.getItemBySlot(EquipmentSlot.CHEST))>0;}

 public static final class SuitItem extends ArmorItem {
  public final Suit suit;
  public SuitItem(Suit suit,Type type){super(MATERIAL.get(suit),type,new Item.Properties().durability(type.getDurability(33)));this.suit=suit;}
  @Override public Component getName(ItemStack s){String part=switch(getType()){case HELMET->"Capacete";case CHESTPLATE->"Peitoral";case LEGGINGS->"Calças";default->"Botas";};return Component.literal(part+" — "+suit.title);}
  @Override public boolean isBarVisible(ItemStack s){return getType()==Type.CHESTPLATE||super.isBarVisible(s);}
  @Override public int getBarWidth(ItemStack s){return getType()==Type.CHESTPLATE?Math.round(13F*charge(s)/MAX_CHARGE):super.getBarWidth(s);}
  @Override public int getBarColor(ItemStack s){return getType()==Type.CHESTPLATE?(charge(s)>MAX_CHARGE/5?0x3FD0FF:0xFF5050):super.getBarColor(s);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> tips,TooltipFlag f){
   tips.add(Component.literal("§bPoder (conjunto completo): §f"+suit.power));
   if(getType()==Type.CHESTPLATE)tips.add(Component.literal("Carga "+charge(s)*100/MAX_CHARGE+"%  •  Shift + clique direito: carregar com redstone"));
   else tips.add(Component.literal("A carga fica no peitoral"));
  }
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);
   if(getType()!=Type.CHESTPLATE||!p.isShiftKeyDown())return super.use(level,p,hand);
   if(!level.isClientSide){
    if(charge(s)>=MAX_CHARGE){p.displayClientMessage(Component.literal("Carga completa."),true);return InteractionResultHolder.fail(s);}
    int slot=-1;boolean block=false;var inv=p.getInventory();
    for(int i=0;i<inv.getContainerSize();i++){var it=inv.getItem(i);if(it.is(Items.REDSTONE_BLOCK)){slot=i;block=true;break;}if(it.is(Items.REDSTONE)&&slot<0)slot=i;}
    if(slot<0&&!p.isCreative()){p.displayClientMessage(Component.literal("Precisa de redstone (pó ou bloco) no inventário."),true);return InteractionResultHolder.fail(s);}
    if(slot>=0&&!p.isCreative())inv.getItem(slot).shrink(1);
    setCharge(s,charge(s)+(block?MAX_CHARGE*3/10:MAX_CHARGE/30));
    level.playSound(null,p.blockPosition(),SoundEvents.BEACON_POWER_SELECT,SoundSource.PLAYERS,.7F,1.6F);
    p.displayClientMessage(Component.literal("Carga "+charge(s)*100/MAX_CHARGE+"%"),true);
   }
   return InteractionResultHolder.sidedSuccess(s,level.isClientSide);
  }
 }

 // ---------------- powers ----------------
 private static MobEffectInstance fx(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> e,int amp){return new MobEffectInstance(e,60,amp,true,false,true);}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%10!=0)return;
  var suit=fullSet(p);var chest=p.getItemBySlot(EquipmentSlot.CHEST);
  boolean on=suit!=null&&charge(chest)>0;
  var data=p.getPersistentData();
  // flight is granted only by the flight suit (and taken back only if we gave it)
  boolean fly=on&&suit==Suit.FLIGHT;
  if(fly&&!p.getAbilities().mayfly){p.getAbilities().mayfly=true;data.putBoolean("MarePowerFly",true);p.onUpdateAbilities();}
  if(!fly&&data.getBoolean("MarePowerFly")){data.remove("MarePowerFly");if(!p.isCreative()&&!p.isSpectator()){p.getAbilities().mayfly=false;p.getAbilities().flying=false;p.onUpdateAbilities();}}
  if(!on)return;
  switch(suit){
   case JUMP->p.addEffect(fx(MobEffects.JUMP,4));
   case SPEED->{p.addEffect(fx(MobEffects.MOVEMENT_SPEED,3));p.addEffect(fx(MobEffects.DIG_SPEED,1));}
   case TITAN->{p.addEffect(fx(MobEffects.DAMAGE_BOOST,1));p.addEffect(fx(MobEffects.DAMAGE_RESISTANCE,1));}
   case SHADOW->{p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,260,0,true,false,true));p.addEffect(fx(MobEffects.WATER_BREATHING,0));if(p.isShiftKeyDown())p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,20,0,true,false,true));}
   default->{}
  }
  int drain=40*(suit==Suit.FLIGHT&&p.getAbilities().flying?2:1);   // 4/tick: a full charge lasts 5 min (flying: 2.5 min)
  int before=charge(chest);setCharge(chest,before-drain);
  if(charge(chest)==0&&before>0)p.displayClientMessage(Component.literal("§cA armadura descarregou! Shift + clique no peitoral com redstone."),true);
  else if(before>MAX_CHARGE/10&&charge(chest)<=MAX_CHARGE/10)p.displayClientMessage(Component.literal("§eArmadura com 10% de carga."),true);
 }
 @SubscribeEvent public static void fall(LivingFallEvent e){
  if(e.getEntity() instanceof Player p&&powered(p)){var s=fullSet(p);if(s==Suit.JUMP||s==Suit.FLIGHT)e.setDistance(0);}
 }
 /** Titan suit: can't be knocked back. */
 @SubscribeEvent public static void knock(net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent e){
  if(e.getEntity() instanceof Player p&&powered(p)&&fullSet(p)==Suit.TITAN)e.setCanceled(true);
 }
}
