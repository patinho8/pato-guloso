package br.mareeletrica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 0.65: gunshot wounds.
 *  - Every bullet that hits a player lands on a body part (head, torso, arms, legs) worked out from where it hit.
 *  - Arms and legs have 24 "limb points". A hit removes points by weapon: sniper 24 (one shot), strong guns 8 (three shots),
 *    SMGs 6 (four shots), USP/Glock/Beretta 3 (eight shots). At 0 the limb stops working:
 *    one leg = forced to crouch, both legs = forced prone; one arm = hangs, guns held with one hand; both arms = can't shoot.
 *    Any broken limb: no heavy weapons (snipers, M249, missile launcher).
 *  - Bullet wounds bleed: 5 minutes to stop it with a bandage or medical kit or the player bleeds to death.
 *    With no working arm you cannot treat yourself — someone else has to use the item on you.
 *  - Bulletproof vest: torso hits lose most of their damage and don't bleed.
 *  - The hit spots show up on the player as blood decals (works with any skin, incl. Quick Skin). */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class Wounds {
 public static final int LA=0,RA=1,LL=2,RL=3,LIMB_HP=60,BLEED_TICKS=6000,MAX_DECALS=24;
 /** 0.65.2: no bleeding any more — wounds heal by themselves. A broken limb is set to BROKEN_AT and loses 1 point every HEAL_EVERY ticks:
  *  it works again after about 1 minute and is fully healed after about 2.5 minutes. */
 public static final int BROKEN_AT=72,HEAL_EVERY=80;
 /** Damage by where the bullet lands: head ×2, torso ×1, arms ×0.6, legs ×0.7. */
 static final float[] ZONE_DAMAGE={2F,1F,.6F,.6F,.7F,.7F};
 static final String[] ZONE_NAME={"na cabeça","no peito","no braço esquerdo","no braço direito","na perna esquerda","na perna direita"};
 public static final String KEY="MareWounds";
 public static final ResourceKey<DamageType> BLEEDING=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"bleeding"));
 /** zone: 0 head, 1 torso, 2 left arm, 3 right arm, 4 left leg, 5 right leg; face: 0 front, 1 back, 2 left, 3 right; u/v 0..1 on that face. */
 public record Decal(int zone,int face,float u,float v){}
 public static final class State {
  public final int[] limb=new int[4];public int bleed=-1;public final List<Decal> decals=new ArrayList<>();
  boolean forcedProne;int dropMsg=-100;
  public boolean broken(int l){return limb[l]>=LIMB_HP;}
  public int armsBroken(){return (broken(LA)?1:0)+(broken(RA)?1:0);}
  public int legsBroken(){return (broken(LL)?1:0)+(broken(RL)?1:0);}
  public boolean anyBroken(){return armsBroken()+legsBroken()>0;}
  public boolean bleeding(){return bleed>=0;}
  public boolean hurt(){return limb[0]+limb[1]+limb[2]+limb[3]>0;}
  public CompoundTag save(){var t=new CompoundTag();t.putIntArray("Limbs",limb);t.putInt("Bleed",bleed);var l=new ListTag();
   for(var d:decals){var c=new CompoundTag();c.putByte("Z",(byte)d.zone);c.putByte("F",(byte)d.face);c.putFloat("U",d.u);c.putFloat("V",d.v);l.add(c);}t.put("Decals",l);return t;}
  public static State load(CompoundTag t){var s=new State();int[] a=t.getIntArray("Limbs");for(int i=0;i<4&&i<a.length;i++)s.limb[i]=a[i];s.bleed=t.contains("Bleed")?t.getInt("Bleed"):-1;
   for(var e:t.getList("Decals",10)){var c=(CompoundTag)e;s.decals.add(new Decal(c.getByte("Z"),c.getByte("F"),c.getFloat("U"),c.getFloat("V")));}return s;}
 }
 // ---------------- items ----------------
 private static final DeferredRegister<ArmorMaterial> MATERIALS=DeferredRegister.create(Registries.ARMOR_MATERIAL,MareEletrica.ID);
 public static final DeferredHolder<ArmorMaterial,ArmorMaterial> VEST_MATERIAL=MATERIALS.register("bulletproof",()->new ArmorMaterial(
  Map.of(ArmorItem.Type.HELMET,2,ArmorItem.Type.CHESTPLATE,7,ArmorItem.Type.LEGGINGS,5,ArmorItem.Type.BOOTS,2,ArmorItem.Type.BODY,7),8,SoundEvents.ARMOR_EQUIP_LEATHER,
  ()->Ingredient.of(Items.IRON_INGOT),
  List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"bulletproof"))),2F,.1F));
 public static final DeferredItem<ArmorItem> VEST=MareEletrica.ITEMS.register("bulletproof_vest",()->new ArmorItem(VEST_MATERIAL,ArmorItem.Type.CHESTPLATE,new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(30))){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Tiros no peito tiram bem menos vida e não sangram."));t.add(Component.literal("§8Não protege braços, pernas nem cabeça. Sniper ainda dói."));}});
 /** 0.84.0: tactical armor — as tough as diamond against mobs and melee, and very good against bullets (see incoming) */
 public static final DeferredHolder<ArmorMaterial,ArmorMaterial> TACTICAL_MATERIAL=MATERIALS.register("tactical",()->new ArmorMaterial(
  Map.of(ArmorItem.Type.HELMET,3,ArmorItem.Type.CHESTPLATE,8,ArmorItem.Type.LEGGINGS,6,ArmorItem.Type.BOOTS,3,ArmorItem.Type.BODY,11),10,SoundEvents.ARMOR_EQUIP_DIAMOND,
  ()->Ingredient.of(Items.DIAMOND),
  List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"tactical"))),2F,0F));
 public static final class TacticalArmor extends ArmorItem {
  public TacticalArmor(ArmorItem.Type t){super(TACTICAL_MATERIAL,t,new Item.Properties().durability(t.getDurability(33)));}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Igual a diamante contra mobs e golpes."));
   t.add(Component.literal("§aSegura tiros: §7"+switch(getType()){case HELMET->"protege a cabeça";case CHESTPLATE->"protege peito e braços";case LEGGINGS->"protege as pernas";default->"ajuda nas pernas (sem a calça)";}));
   t.add(Component.literal("§8Sniper ainda atravessa um pouco."));}}
 public static final DeferredItem<TacticalArmor> TAC_HELMET=MareEletrica.ITEMS.register("tactical_helmet",()->new TacticalArmor(ArmorItem.Type.HELMET)),
  TAC_CHEST=MareEletrica.ITEMS.register("tactical_chestplate",()->new TacticalArmor(ArmorItem.Type.CHESTPLATE)),
  TAC_LEGS=MareEletrica.ITEMS.register("tactical_leggings",()->new TacticalArmor(ArmorItem.Type.LEGGINGS)),
  TAC_BOOTS=MareEletrica.ITEMS.register("tactical_boots",()->new TacticalArmor(ArmorItem.Type.BOOTS));
 /** 0.85.0: tactical (ballistic) shield — stops bullets from the front even without raising it; while holding it only pistols can fire */
 public static final class TacticalShield extends net.minecraft.world.item.ShieldItem {
  public TacticalShield(Item.Properties p){super(p);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Para tiros vindos da frente, mesmo sem levantar o escudo."));
   t.add(Component.literal("§7Cobre cabeça, peito e braços; as pernas ficam expostas."));
   t.add(Component.literal("§cSegurando ele, só dá para atirar com pistolas."));}
  @Override public boolean isValidRepairItem(ItemStack a,ItemStack b){return b.is(Items.IRON_INGOT);}}
 public static final DeferredItem<TacticalShield> TAC_SHIELD=MareEletrica.ITEMS.register("tactical_shield",()->new TacticalShield(new Item.Properties().durability(1200)));
 public static boolean holdsTacShield(LivingEntity p){return p.getMainHandItem().is(TAC_SHIELD.get())||p.getOffhandItem().is(TAC_SHIELD.get());}
 /** bullets go through ordinary shields; only the tactical one stops them */
 @SubscribeEvent public static void shieldBlock(net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent e){
  var d=e.getDamageSource().getDirectEntity();if(d==null||!bullet(d))return;
  if(!e.getEntity().getUseItem().is(TAC_SHIELD.get()))e.setBlocked(false);}
 /** the tactical shield in either hand covers head, chest and arms against bullets coming from the front */
 static boolean shieldStops(LivingEntity p,Entity direct,int zone){
  if(!holdsTacShield(p)||zone>=4)return false;
  Vec3 v=direct.getDeltaMovement();if(v.lengthSqr()<1e-6)v=p.position().subtract(direct.position());
  Vec3 look=Vec3.directionFromRotation(0,p.getYHeadRot());
  return v.normalize().dot(look)<-.35;}
 public enum Med {BANDAGE("bandage",60),SYRINGE("syringe",40),MEDKIT("medkit",140);public final String id;public final int ticks;Med(String id,int t){this.id=id;ticks=t;}}
 public static final DeferredItem<MedItem> BANDAGE=med(Med.BANDAGE),SYRINGE=med(Med.SYRINGE),MEDKIT=med(Med.MEDKIT);
 private static DeferredItem<MedItem> med(Med m){return MareEletrica.ITEMS.register(m.id,()->new MedItem(m,new Item.Properties().stacksTo(m==Med.MEDKIT?4:16)));}
 public static final DeferredHolder<SoundEvent,SoundEvent> BREATH_CALM=sound("breath_calm"),BREATH_HEAVY=sound("breath_heavy"),BREATH_GASP=sound("breath_gasp"),HEARTBEAT=sound("heartbeat"),BANDAGE_WRAP=sound("bandage_wrap"),SYRINGE_INJECT=sound("syringe_inject");
 private static DeferredHolder<SoundEvent,SoundEvent> sound(String n){return MareEletrica.SOUNDS.register(n,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,n)));}
 public static void register(IEventBus bus){MATERIALS.register(bus);bus.addListener(Wounds::packets);}
 public static void creative(net.minecraft.world.item.CreativeModeTab.Output out){out.accept(BANDAGE.get());out.accept(SYRINGE.get());out.accept(MEDKIT.get());out.accept(VEST.get());out.accept(TAC_HELMET.get());out.accept(TAC_CHEST.get());out.accept(TAC_LEGS.get());out.accept(TAC_BOOTS.get());out.accept(TAC_SHIELD.get());}

 // ---------------- server state ----------------
 private static final Map<UUID,State> STATES=new HashMap<>();
 public static State st(Player p){return STATES.computeIfAbsent(p.getUUID(),u->State.load(p.getPersistentData().getCompound(KEY)));}
 /** Client side the state comes from the sync packets; both sides answer through this. */
 public static java.util.function.Function<Player,State> clientState=p->new State();
 public static State of(Player p){return p.level().isClientSide?clientState.apply(p):st(p);}
 public static void save(ServerPlayer p,State s,boolean sync){p.getPersistentData().put(KEY,s.save());if(sync)PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new Sync(p.getId(),s.save()));}
 static void reset(Player p){STATES.remove(p.getUUID());p.getPersistentData().remove(KEY);}

 // ---------------- packets ----------------
 public record Sync(int entity,CompoundTag data) implements CustomPacketPayload {
  public static final Type<Sync> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"wounds_sync"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Sync> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Sync::entity,ByteBufCodecs.COMPOUND_TAG,Sync::data,Sync::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 /** kind 0 = bullet, 1 = explosion; zone is where a bullet landed (-1 none). */
 public record Hit(int kind,float amount,int zone) implements CustomPacketPayload {
  public static final Type<Hit> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"wounds_hit"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Hit> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Hit::kind,ByteBufCodecs.FLOAT,Hit::amount,ByteBufCodecs.VAR_INT,Hit::zone,Hit::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static Consumer<Sync> clientSync=x->{};public static Consumer<Hit> clientHit=x->{};
 static void packets(RegisterPayloadHandlersEvent e){var r=e.registrar("1");
  r.playToClient(Sync.TYPE,Sync.CODEC,(p,c)->clientSync.accept(p));r.playToClient(Hit.TYPE,Hit.CODEC,(p,c)->clientHit.accept(p));}

 // ---------------- bullets → body parts ----------------
 public static boolean bullet(Entity direct){return direct instanceof DmtProjectileEntity d&&!d.isSpear()||direct instanceof Tracers.Round||direct instanceof RoverBulletEntity;}
 /** Limb points a hit removes (limb holds 60): sniper 30 (2 shots), strong 8 (8), SMG 6 (10), light pistols 3 (20). */
 static final int HEAVY=30;
 static int points(Entity direct,DamageSource src){
  if(direct instanceof DmtProjectileEntity d&&d.isPellet())return 3;
  WeaponFamily f=src.getEntity() instanceof LivingEntity le?WeaponFamily.of(le.getMainHandItem()):null;
  if(f!=null)return switch(f){
   case SNIPER,BARRETT,SVD->HEAVY;
   case USP,GLOCK,BERETTA->3;
   case MP5,SMG,MP40,P90,UMP45,UZI,MAC10->6;
   default->8;};
  if(direct instanceof DmtProjectileEntity d&&d.isSniper())return HEAVY;
  return direct instanceof Tracers.Round?8:6;
 }
 static Vec3 hitPoint(LivingEntity p,Entity direct){
  Vec3 pos=direct.position(),v=direct.getDeltaMovement();AABB box=p.getBoundingBox().inflate(.15);
  if(v.lengthSqr()>1e-6){var hit=box.clip(pos.subtract(v.scale(1.5)),pos.add(v.scale(1.5)));if(hit.isPresent())return hit.get();}
  return new Vec3(Mth.clamp(pos.x,box.minX,box.maxX),Mth.clamp(pos.y,box.minY,box.maxY),Mth.clamp(pos.z,box.minZ,box.maxZ));
 }
 /** Which part was hit, and where on it (for the blood decal). */
 static Decal locate(Player p,Entity direct,Vec3 hit){
  var r=p.getRandom();
  double yaw=Math.toRadians(p.yBodyRot);Vec3 fwd=new Vec3(-Math.sin(yaw),0,Math.cos(yaw)),right=new Vec3(-Math.cos(yaw),0,-Math.sin(yaw));
  Vec3 rel=hit.subtract(p.position());double lat=rel.dot(right),h=p.getBbHeight(),y=rel.y/h;
  Vec3 dir=direct.getDeltaMovement().lengthSqr()>1e-6?direct.getDeltaMovement().normalize():fwd.scale(-1);
  double df=dir.dot(fwd);int face=df<-.45?0:df>.45?1:dir.dot(right)<0?3:2;
  int zone;float u,v;
  if(p.getPose()==Pose.SWIMMING||p.getPose()==Pose.FALL_FLYING||p.getPose()==Pose.SLEEPING){   // lying down: no clear height, spread the hits
   float k=r.nextFloat();zone=k<.1?0:k<.55?1:k<.7?(r.nextBoolean()?2:3):(r.nextBoolean()?4:5);u=.2F+r.nextFloat()*.6F;v=.2F+r.nextFloat()*.6F;
  }else if(y<.42){zone=lat>0?5:4;u=(float)Mth.clamp(.5+(Math.abs(lat)-.12)/.25*(lat>0?1:-1)*.5,.15,.85);v=(float)Mth.clamp(1-y/.42,.1,.9);}
  else if(y>.78){zone=0;u=(float)Mth.clamp(.5+lat/.5,.15,.85);v=(float)Mth.clamp((1-y)/.22,.15,.85);}
  else if(Math.abs(lat)>.23){zone=lat>0?3:2;u=.5F;v=(float)Mth.clamp((.78-y)/.36,.1,.9);}
  else{zone=1;u=(float)Mth.clamp(.5+lat/.46,.1,.9);v=(float)Mth.clamp((.78-y)/.36,.1,.9);}
  return new Decal(zone,face,u,v);
 }
 /** which tactical piece covers a zone: helmet → head, chestplate → torso and arms, leggings → legs (boots alone also help the legs) */
 static EquipmentSlot tacticalPiece(Player p,int zone){
  EquipmentSlot sl=zone==0?EquipmentSlot.HEAD:zone<=3?EquipmentSlot.CHEST:p.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof TacticalArmor?EquipmentSlot.LEGS:EquipmentSlot.FEET;
  return p.getItemBySlot(sl).getItem() instanceof TacticalArmor?sl:null;}
 static boolean vest(Player p){return p.getItemBySlot(EquipmentSlot.CHEST).is(VEST.get());}
 static final String[] LIMB_NAME={"braço esquerdo","braço direito","perna esquerda","perna direita"};
 /** 0.84.0: being shot barely pushes you — remember who was just hit by a bullet, the knockback of that same hit is cut down */
 static final Map<Entity,Integer> SHOT=new java.util.WeakHashMap<>();
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void shotMark(LivingIncomingDamageEvent e){
  var d=e.getSource().getDirectEntity();if(d!=null&&bullet(d)&&!e.getEntity().level().isClientSide)SHOT.put(e.getEntity(),e.getEntity().tickCount);}
 @SubscribeEvent public static void shotKnock(net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent e){
  Integer t=SHOT.get(e.getEntity());if(t!=null&&t==e.getEntity().tickCount)e.setStrength(e.getStrength()*.12F);}
 /** 0.84.1: other creatures (bots, mobs; not bosses): a sniper hit on the upper half of the body is a kill at any distance */
 @SubscribeEvent public static void sniperMob(LivingIncomingDamageEvent e){
  var le=e.getEntity();if(le instanceof Player||le.level().isClientSide||le.getType().is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES))return;
  var direct=e.getSource().getDirectEntity();if(direct==null||!bullet(direct)||points(direct,e.getSource())<HEAVY)return;
  Vec3 hit=hitPoint(le,direct);if((hit.y-le.getY())/le.getBbHeight()>=.42)e.setAmount(Math.max(e.getAmount(),10000F));}
 @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){
  if(!(e.getEntity() instanceof ServerPlayer p)||p.isCreative()||p.isSpectator())return;
  var src=e.getSource();var direct=src.getDirectEntity();
  if(direct!=null&&bullet(direct)){
   Vec3 hit=hitPoint(p,direct);Decal d=locate(p,direct,hit);int pts=points(direct,src);
   if(shieldStops(p,direct,d.zone())){   // 0.85.0: the tactical shield takes it
    var hand=p.getMainHandItem().is(TAC_SHIELD.get())?net.minecraft.world.InteractionHand.MAIN_HAND:net.minecraft.world.InteractionHand.OFF_HAND;
    p.getItemInHand(hand).hurtAndBreak(pts>=HEAVY?40:3,p,hand==net.minecraft.world.InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
    p.level().playSound(null,p.blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.PLAYERS,1F,.8F+p.getRandom().nextFloat()*.3F);
    if(p.level() instanceof net.minecraft.server.level.ServerLevel sl){Vec3 at=hit;sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,at.x,at.y,at.z,6,.05,.05,.05,.2);}
    p.displayClientMessage(Component.literal("§7🛡 O escudo tático segurou o tiro"),true);e.setCanceled(true);return;}
   boolean stopped=false,armored=false;
   // 0.84.1: the sniper ignores vests and armor — head or chest is always a kill, an arm or leg always breaks, at any distance
   boolean sniper=pts>=HEAVY;
   if(!sniper&&d.zone()==1&&vest(p)){   // the vest takes most of it and the wound doesn't bleed
    var chest=p.getItemBySlot(EquipmentSlot.CHEST);e.setAmount(e.getAmount()*(pts>=HEAVY?.55F:.3F));chest.hurtAndBreak(pts>=HEAVY?6:2,p,EquipmentSlot.CHEST);stopped=pts<HEAVY;
    p.level().playSound(null,p.blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.PLAYERS,.8F,1.4F);
   }
   // 0.84.0: tactical armor — the piece covering the hit zone soaks most of the bullet and keeps the limb working
   var piece=sniper?null:tacticalPiece(p,d.zone());
   if(piece!=null){var it=p.getItemBySlot(piece);boolean heavy=pts>=HEAVY;e.setAmount(e.getAmount()*(heavy?.55F:.25F));it.hurtAndBreak(heavy?4:1,p,piece);
    pts=Math.max(1,Math.round(pts*(heavy?.5F:.35F)));if(!heavy)stopped=true;armored=true;
    p.level().playSound(null,p.blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.PLAYERS,.7F,1.6F);}
   var s=st(p);
   e.setAmount(e.getAmount()*ZONE_DAMAGE[d.zone()]);
   // ONLY the sniper: an arm or leg hit can't take a healthy player down by itself — only chest or head shots are deadly
   // = arm/leg sniper hits only do a little damage (3.5 hearts at most): a healthy player survives, a badly hurt one can still die
   if(sniper&&d.zone()>=2)e.setAmount(Math.min(e.getAmount(),7F));
   if(sniper&&d.zone()<=1){e.setAmount(10000F);if(!p.getItemBySlot(EquipmentSlot.CHEST).isEmpty()&&d.zone()==1)p.getItemBySlot(EquipmentSlot.CHEST).hurtAndBreak(8,p,EquipmentSlot.CHEST);}
   if(d.zone()>=2){int l=d.zone()-2;boolean was=s.broken(l);s.limb[l]=sniper?BROKEN_AT:Math.min(BROKEN_AT,s.limb[l]+pts);if(s.broken(l))s.limb[l]=BROKEN_AT;
    if(!was&&s.broken(l)){p.displayClientMessage(Component.literal("§c§lSua "+LIMB_NAME[l]+" parou de funcionar! §7(volta sozinha em ~1 min)"),false);
     if(s.armsBroken()==2)p.displayClientMessage(Component.literal("§4Sem os dois braços: você não segura nada. Alguém pode usar uma seringa em você."),false);}}
   // what each hit does right away
   switch(d.zone()){
    case 0->{p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,70,0,false,false));p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,20,0,false,false));}
    case 2,3->p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,80,1,false,false));
    case 4,5->p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,1,false,false));
    default->p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0,false,false));
   }
   p.displayClientMessage(Component.literal((d.zone()==0?"§4§l":"§c")+"Tiro "+ZONE_NAME[d.zone()]+(armored?" §7(a armadura segurou)":stopped?" §7(o colete segurou)":"")),true);
   if(!stopped){s.decals.add(d);while(s.decals.size()>MAX_DECALS)s.decals.remove(0);}
   save(p,s,true);
   PacketDistributor.sendToPlayer(p,new Hit(0,e.getAmount(),d.zone()));
  }else if(src.is(DamageTypeTags.IS_EXPLOSION))PacketDistributor.sendToPlayer(p,new Hit(1,e.getAmount(),-1));
 }
 // ---------------- every tick: bleeding, forced poses ----------------
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive())return;
  var s=STATES.get(p.getUUID());if(s==null){if(!p.getPersistentData().contains(KEY))return;s=st(p);}
  if(p.isCreative()||p.isSpectator())return;
  if(s.bleed>=0)s.bleed=-1;   // old saves: bleeding no longer exists
  // wounds heal by themselves; the blood marks fade one by one once nothing is broken
  if(p.tickCount%HEAL_EVERY==0&&s.hurt()){boolean was=s.anyBroken();for(int i=0;i<4;i++)if(s.limb[i]>0)s.limb[i]--;
   for(int i=0;i<4;i++)if(s.limb[i]==LIMB_HP-1)p.displayClientMessage(Component.literal("§aSua "+LIMB_NAME[i]+" voltou a funcionar."),true);
   save(p,s,was!=s.anyBroken()||p.tickCount%(HEAL_EVERY*4)==0);}
  if(p.tickCount%300==0&&!s.anyBroken()&&!s.decals.isEmpty()){s.decals.remove(0);save(p,s,true);}
  // both arms broken: nothing can be held — whatever is in the hands goes into the backpack (or the floor if it's full)
  if(s.armsBroken()==2)for(var hand:InteractionHand.values()){var held=p.getItemInHand(hand);if(held.isEmpty())continue;
   stash(p,held.copy());p.setItemInHand(hand,ItemStack.EMPTY);
   if(p.tickCount-s.dropMsg>60){s.dropMsg=p.tickCount;p.displayClientMessage(Component.literal("§cSem os dois braços você não consegue segurar nada."),true);}}
  // legs: one broken = crouch (client forces sneaking), no sprint; both = forced prone
  int legs=s.legsBroken();
  if(legs>0)p.setSprinting(false);
  if(legs==2&&!ProneSystem.prone(p)&&p.onGround()&&!p.isInWater()&&!p.isPassenger()){ProneSystem.set(p,true);s.forcedProne=true;}
  else if(legs<2&&s.forcedProne){s.forcedProne=false;if(ProneSystem.prone(p))ProneSystem.set(p,false);}
 }
 @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){reset(p);PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new Sync(p.getId(),new State().save()));}}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p){reset(p);save(p,new State(),true);}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){STATES.remove(p.getUUID());save(p,st(p),true);}}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){STATES.remove(e.getEntity().getUUID());}
 @SubscribeEvent public static void track(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer viewer&&e.getTarget() instanceof ServerPlayer t)PacketDistributor.sendToPlayer(viewer,new Sync(t.getId(),st(t).save()));}

 /** Into the main inventory (not the hotbar, so it isn't held again); leftovers fall on the ground. */
 static void stash(Player p,ItemStack st){
  var inv=p.getInventory();
  for(int i=9;i<36&&!st.isEmpty();i++){var c=inv.getItem(i);if(!c.isEmpty()&&ItemStack.isSameItemSameComponents(c,st)&&c.getCount()<c.getMaxStackSize()){int n=Math.min(st.getCount(),c.getMaxStackSize()-c.getCount());c.grow(n);st.shrink(n);}}
  for(int i=9;i<36&&!st.isEmpty();i++)if(inv.getItem(i).isEmpty()){inv.setItem(i,st.copy());st.setCount(0);}
  if(!st.isEmpty()){var e=p.drop(st,false);if(e!=null)e.setPickUpDelay(60);}
 }
 // ---------------- weapons a hurt body can't handle ----------------
 public static boolean heavy(ItemStack s){var f=WeaponFamily.of(s);return MareEletrica.isSniper(s)||f==WeaponFamily.BARRETT||f==WeaponFamily.SVD||f==WeaponFamily.M249||s.getItem() instanceof WarPlus.LauncherItem;}
 public static boolean gun(ItemStack s){return WeaponFamily.of(s)!=null||s.getItem() instanceof GunItem||s.getItem() instanceof WarPlus.LauncherItem||s.is(MareEletrica.SMG.get())||MareEletrica.isSniper(s);}
 /** Null when the player can fire what they hold; otherwise the reason. */
 public static String blocked(Player p,ItemStack held){
  if(p.isCreative())return null;var s=of(p);
  if(s.armsBroken()==2&&gun(held))return "§cSem os dois braços você não consegue atirar.";
  if(s.anyBroken()&&heavy(held))return "§cMachucado demais para usar uma arma tão pesada.";
  if(holdsTacShield(p)&&gun(held)){var f=WeaponFamily.of(held);if(f==null||!f.pistol())return "§cCom o escudo tático na mão, só dá para atirar com pistola.";}
  return null;
 }
 public static boolean canFire(ServerPlayer p){var why=blocked(p,p.getMainHandItem());if(why==null)return true;p.displayClientMessage(Component.literal(why),true);return false;}

 // ---------------- medical items ----------------
 private static final Map<UUID,UUID> PATIENT=new HashMap<>();
 /** Another player within reach in front of you, if any (you treat them instead of yourself). */
 static Player aimedPlayer(Player user){
  Vec3 eye=user.getEyePosition(),end=eye.add(user.getLookAngle().scale(3.5));
  EntityHitResult hit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(user,eye,end,user.getBoundingBox().expandTowards(user.getLookAngle().scale(3.5)).inflate(1),x->x instanceof Player pl&&pl!=user&&!pl.isSpectator(),12.25);
  return hit!=null&&hit.getEntity() instanceof Player pl?pl:null;
 }
 public static final class MedItem extends Item {
  public final Med kind;
  public MedItem(Med kind,Properties p){super(p);this.kind=kind;}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){int t=kind.ticks;return e instanceof Player p&&of(p).armsBroken()==1?t*17/10:t;}   // one arm: slower
  @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.NONE;}
  boolean needs(State s){return switch(kind){case BANDAGE->s.hurt();case SYRINGE->s.anyBroken();case MEDKIT->s.hurt();};}
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
   var stack=p.getItemInHand(hand);
   if(of(p).armsBroken()==2){if(!level.isClientSide)p.displayClientMessage(Component.literal("§cSem braços! Outra pessoa precisa usar isso em você."),true);return InteractionResultHolder.fail(stack);}
   Player target=aimedPlayer(p);
   State ts=target!=null?of(target):of(p);
   if(!needs(ts)&&!(kind!=Med.SYRINGE&&(target!=null?target:p).getHealth()<(target!=null?target:p).getMaxHealth())){
    if(!level.isClientSide)p.displayClientMessage(Component.literal(target!=null?"§7"+target.getName().getString()+" não precisa disso.":"§7Você não precisa disso agora."),true);return InteractionResultHolder.fail(stack);}
   if(!level.isClientSide){if(target!=null)PATIENT.put(p.getUUID(),target.getUUID());else PATIENT.remove(p.getUUID());
    level.playSound(null,p.blockPosition(),kind==Med.SYRINGE?SYRINGE_INJECT.get():BANDAGE_WRAP.get(),SoundSource.PLAYERS,.9F,1F);
    if(target!=null){p.displayClientMessage(Component.literal("§aTratando "+target.getName().getString()+"..."),true);target.displayClientMessage(Component.literal("§a"+p.getName().getString()+" está cuidando de você."),true);}}
   p.startUsingItem(hand);return InteractionResultHolder.consume(stack);
  }
  @Override public void onUseTick(Level level,LivingEntity e,ItemStack s,int left){
   if(!level.isClientSide&&left%20==0&&kind!=Med.SYRINGE)level.playSound(null,e.blockPosition(),BANDAGE_WRAP.get(),SoundSource.PLAYERS,.6F,.9F+e.getRandom().nextFloat()*.2F);
   if(!level.isClientSide&&e instanceof ServerPlayer p){var id=PATIENT.get(p.getUUID());if(id!=null){var t=level.getPlayerByUUID(id);if(t==null||t.distanceToSqr(p)>5.5*5.5){p.stopUsingItem();p.displayClientMessage(Component.literal("§cMuito longe do paciente."),true);}}}
  }
  @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity e){
   if(!level.isClientSide&&e instanceof ServerPlayer p){
    Player target=p;var id=PATIENT.remove(p.getUUID());if(id!=null&&level.getPlayerByUUID(id) instanceof ServerPlayer t&&t.distanceToSqr(p)<6*6)target=t;
    var ts=st(target);String msg;
    switch(kind){
     case BANDAGE->{int worst=0;for(int i=1;i<4;i++)if(ts.limb[i]>ts.limb[worst])worst=i;ts.limb[worst]=Math.max(0,Math.min(ts.limb[worst],LIMB_HP-1)-30);target.heal(4);msg="Ferimento enfaixado: a "+LIMB_NAME[worst]+" melhorou.";}
     case SYRINGE->{int worst=-1;for(int i=0;i<4;i++)if(ts.broken(i)&&(worst<0||ts.limb[i]>ts.limb[worst]))worst=i;
      if(worst>=0){ts.limb[worst]=0;msg="A "+LIMB_NAME[worst]+" voltou a funcionar.";}else{for(int i=0;i<4;i++)ts.limb[i]=Math.max(0,ts.limb[i]-8);msg="Adrenalina aplicada.";}
      target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,0));target.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,0));}
     default->{for(int i=0;i<4;i++)ts.limb[i]=0;ts.bleed=-1;ts.decals.clear();target.heal(10);msg="Tratado com o kit médico: tudo funcionando.";}
    }
    save((ServerPlayer)target,ts,true);
    level.playSound(null,target.blockPosition(),kind==Med.SYRINGE?SYRINGE_INJECT.get():SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.PLAYERS,1F,1.1F);
    target.displayClientMessage(Component.literal("§a"+msg),true);if(target!=p)p.displayClientMessage(Component.literal("§aVocê tratou "+target.getName().getString()+": "+msg),true);
    if(!p.isCreative())stack.shrink(1);
   }
   return stack;
  }
  @Override public void releaseUsing(ItemStack s,Level l,LivingEntity e,int left){if(!l.isClientSide)PATIENT.remove(e.getUUID());}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal(switch(kind){case BANDAGE->"§7Enfaixa o pior ferimento (volta a funcionar na hora) e cura um pouco.";case SYRINGE->"§7Faz um braço ou perna machucada voltar a funcionar.";case MEDKIT->"§7Trata tudo: membros, sangramento e marcas de tiro.";}));
   t.add(Component.literal("§8Segure o botão direito. Olhando para outro player: usa nele."));
   t.add(Component.literal("§8Com um braço só demora mais. Sem os dois braços, só outra pessoa pode usar em você. Sem item, os ferimentos saram sozinhos."));
  }
 }
}
