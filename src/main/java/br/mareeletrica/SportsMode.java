package br.mareeletrica;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Sports outfits. Wearing the shirt puts you in "game mode" for that sport: the click becomes the sport move
 *  (football: kick with the leg; goalkeeper: catch / punt; basketball: steal, dribble, shoot; volleyball: bump, set, spike;
 *  tennis: longer reach with the racket). The full kit (shirt, shorts, shoes) also makes you a bit faster. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class SportsMode {
 public enum Mode {
  SOCCER("futebol","Futebol",0x1E9E3A,0xFFFFFF), KEEPER("goleiro","Goleiro",0xF2C41A,0x202020), BASKET("basquete","Basquete",0x5A2A92,0xF2C41A),
  VOLLEY("volei","Vôlei",0x1E5AC8,0xF2E64A), TENNIS("tenis","Tênis",0xF4F4F0,0x2E9E5A);
  public final String id,title;public final int main,trim;
  Mode(String id,String title,int main,int trim){this.id=id;this.title=title;this.main=main;this.trim=trim;}
 }
 private static final DeferredRegister<ArmorMaterial> MATERIALS=DeferredRegister.create(Registries.ARMOR_MATERIAL,MareEletrica.ID);
 private static final Map<Mode,DeferredHolder<ArmorMaterial,ArmorMaterial>> MATERIAL=new EnumMap<>(Mode.class);
 public static final List<DeferredItem<OutfitItem>> ITEMS=new ArrayList<>();
 private static final ArmorItem.Type[] PIECES={ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS};
 static{
  for(var m:Mode.values()){
   MATERIAL.put(m,MATERIALS.register("outfit_"+m.id,()->new ArmorMaterial(Map.of(ArmorItem.Type.HELMET,0,ArmorItem.Type.CHESTPLATE,1,ArmorItem.Type.LEGGINGS,1,ArmorItem.Type.BOOTS,0,ArmorItem.Type.BODY,1),
    10,SoundEvents.ARMOR_EQUIP_LEATHER,()->Ingredient.of(net.minecraft.world.item.Items.WHITE_WOOL),List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"outfit_"+m.id))),0F,0F)));
   for(var t:PIECES)ITEMS.add(MareEletrica.ITEMS.register("outfit_"+m.id+"_"+t.getName(),()->new OutfitItem(m,t)));
  }
 }
 private SportsMode(){}
 public static void register(IEventBus bus){MATERIALS.register(bus);}
 public static void creative(java.util.function.Consumer<ItemStack> out){for(var i:ITEMS)out.accept(new ItemStack(i.get()));}

 public static final class OutfitItem extends ArmorItem {
  public final Mode mode;
  public OutfitItem(Mode m,Type t){super(MATERIAL.get(m),t,new Item.Properties().durability(t.getDurability(8)));mode=m;}
  @Override public Component getName(ItemStack s){String part=switch(getType()){case CHESTPLATE->mode==Mode.KEEPER?"Camisa e luvas":"Camisa";case LEGGINGS->"Calção";default->mode==Mode.SOCCER||mode==Mode.KEEPER?"Chuteiras":"Tênis";};return Component.literal(part+" de "+mode.title);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   if(getType()!=Type.CHESTPLATE){t.add(Component.literal("§7Conjunto completo: +velocidade"));return;}
   t.add(Component.literal("§aVestindo: modo "+mode.title));
   t.add(Component.literal("§7"+switch(mode){case SOCCER->"Clique: chute (correndo = forte) • direito: passe";case KEEPER->"Clique: agarra (ou mergulha) • com a bola: clique = tiro de meta, direito = joga com a mão";
    case BASKET->"Clique: pega/rouba e quica • com a bola: clique = arremesso, direito = passe";case VOLLEY->"Clique: toque • pulando: cortada • direito: saque";default->"Raquete alcança mais longe e acerta o ponto doce mais fácil";}));
   if(mode!=Mode.TENNIS){var n=moves(mode);t.add(Component.literal("§eGolpes especiais (Q, R, C, G — mude em Controles):"));for(int i=0;i<n.length;i++)t.add(Component.literal("§7 "+(i+1)+". §f"+n[i]));}
  }
 }
 /** Sport mode from the shirt, or null. */
 public static Mode mode(Player p){return p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof OutfitItem o?o.mode:null;}
 static boolean fullKit(Player p,Mode m){for(var s:new EquipmentSlot[]{EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})if(!(p.getItemBySlot(s).getItem() instanceof OutfitItem o&&o.mode==m))return false;return true;}
 private static final ResourceLocation SPEED=ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"sport_kit_speed");
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  var p=e.getEntity();if(p.level().isClientSide||p.tickCount%10!=0)return;
  var m=mode(p);var attr=p.getAttribute(Attributes.MOVEMENT_SPEED);if(attr==null)return;
  boolean kit=m!=null&&fullKit(p,m);
  if(kit&&!attr.hasModifier(SPEED))attr.addTransientModifier(new AttributeModifier(SPEED,.15,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
  else if(!kit&&attr.hasModifier(SPEED))attr.removeModifier(SPEED);
 }

 /** Volleyball outfit: a much higher jump (about 2 blocks instead of 1.25) to block and spike at the net.
  *  Runs on both sides: the client moves the player, the server keeps the same speed. */
 @SubscribeEvent public static void jump(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent e){
  if(e.getEntity() instanceof Player p&&mode(p)==Mode.VOLLEY&&!p.isShiftKeyDown()){var v=p.getDeltaMovement();p.setDeltaMovement(v.x,Math.max(v.y,.6),v.z);}
 }
 /** No fall damage from your own volleyball jumps. */
 @SubscribeEvent public static void fall(net.neoforged.neoforge.event.entity.living.LivingFallEvent e){
  if(e.getEntity() instanceof Player p&&mode(p)==Mode.VOLLEY&&e.getDistance()<4.5F)e.setDistance(0);
 }
 static Sports.Kind ballFor(Mode m){return switch(m){case BASKET->Sports.Kind.BASKET;case VOLLEY->Sports.Kind.VOLLEY;default->Sports.Kind.SOCCER;};}
 /** Nearest ball of the right sport in front of the player within reach. */
 static Sports.SportBall nearest(Player p,Sports.Kind k,double reach){
  Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();Vec3 body=p.position().add(0,.9,0);
  Sports.SportBall best=null;double bd=1e9;
  for(var b:p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().inflate(reach),x->x.kind()==k)){
   Vec3 to=b.position().add(0,.25,0).subtract(body);double d=to.length();if(d>reach)continue;
   Vec3 tf=new Vec3(to.x,0,to.z);if(tf.lengthSqr()>.25&&tf.normalize().dot(flat)<.2)continue;
   if(d<bd){bd=d;best=b;}
  }
  return best;
 }
 /** Football pass: toward the teammate closest to where you look (or straight ahead). */
 static Vec3 passDir(Player p,Vec3 flat,Sports.SportBall b){
  Player mate=null;double best=.9;
  for(var o:p.level().getEntitiesOfClass(Player.class,p.getBoundingBox().inflate(32),x->x!=p&&!x.isSpectator())){
   Vec3 to=o.position().subtract(b.position()).multiply(1,0,1);double d=to.length();if(d<2||d>32)continue;double c=to.normalize().dot(flat);if(c>best){best=c;mate=o;}}
  if(mate==null)return flat;
  Vec3 to=mate.position().add(mate.getDeltaMovement().scale(6)).subtract(b.position()).multiply(1,0,1);double d=to.length();
  return to.normalize().scale(Math.min(1.35,.55+d*.045)/1.25);
 }
 static Sports.SportBall held(Player p){for(var b:p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().inflate(3)))if(b.holderId()==p.getId())return b;return null;}
 private static void sound(Player p,net.minecraft.sounds.SoundEvent s,float pitch){p.level().playSound(null,p.getX(),p.getY(),p.getZ(),s,SoundSource.PLAYERS,1F,pitch);}
 /** Aim point: where the crosshair hits the ground (or 18 blocks ahead). */
 static Vec3 aimPoint(Player p){Vec3 eye=p.getEyePosition();var hit=p.level().clip(new net.minecraft.world.level.ClipContext(eye,eye.add(p.getLookAngle().scale(48)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
  return hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK?hit.getLocation():eye.add(new Vec3(p.getLookAngle().x,0,p.getLookAngle().z).normalize().scale(18));}
 /** Initial velocity that lands at {@code to} after a flight of {@code ticks} with the given gravity (drag ignored). */
 /** High lob: rises to {@code h} blocks above the higher end, then drops onto the target (volleyball). */
 /** Basketball chest pass: fast and straight to the chest of the teammate you look at (no bounce); they catch it on arrival. */
 static void pass(Player p,Sports.SportBall held,Vec3 look){
  Vec3 eye=p.getEyePosition();Player mate=null;double best=.92;
  for(var o:p.level().getEntitiesOfClass(Player.class,p.getBoundingBox().inflate(40),x->x!=p&&!x.isSpectator())){
   Vec3 to=o.position().add(0,1.2,0).subtract(eye);double d=to.length();if(d<1.5||d>40)continue;double c=to.normalize().dot(look);if(c>best){best=c;mate=o;}}
  Vec3 from=held.position(),v;
  if(mate!=null){Vec3 chest=mate.position().add(mate.getDeltaMovement().scale(3)).add(0,1.2,0);double t=Math.max(2,chest.distanceTo(from)/2.7);v=arc(from,chest,Sports.Kind.BASKET.gravity,t);held.passTo=mate.getUUID();held.passUntil=held.tickCount+(int)t+12;}
  else v=look.scale(2.5).add(0,.12,0);   // nobody there: a hard straight pass where you look
  held.release(v,p);sound(p,SoundEvents.SNOWBALL_THROW,.9F);msg(p,mate!=null?"Passe para "+mate.getName().getString():"Passe");
 }
 public static Vec3 lob(Vec3 from,Vec3 to,double g,double h){
  Vec3 d=to.subtract(from);double top=h+Math.max(0,d.y);
  double vy=Math.sqrt(2*g*top),t=(vy+Math.sqrt(2*g*Math.max(0,top-d.y)))/g;
  t*=1.06;   // a little drag in flight
  return new Vec3(d.x/t,vy*1.03,d.z/t);
 }
 /** 0.66.0: includes the ball's air drag (it used to fall short of the rim on long shots). */
 public static Vec3 arc(Vec3 from,Vec3 to,double g,double ticks){
  Vec3 d=to.subtract(from);double k=.99,n=Math.max(1,ticks),S=k*(1-Math.pow(k,n))/(1-k);
  return new Vec3(d.x/S,(d.y+g*k/(1-k)*(n-S))/S,d.z/S);}
 /** Nearest basketball hoop ring roughly where the player looks. */
 public static Vec3 hoop(Player p){
  Vec3 eye=p.getEyePosition(),look=p.getLookAngle();Vec3 best=null;double bd=1e9;var c=p.blockPosition();
  for(int cx=(c.getX()-40)>>4;cx<=(c.getX()+40)>>4;cx++)for(int cz=(c.getZ()-40)>>4;cz<=(c.getZ()+40)>>4;cz++){
   if(!p.level().hasChunk(cx,cz))continue;
   for(var be:p.level().getChunk(cx,cz).getBlockEntities().values())if(be instanceof Sports.GoalEntity g&&g.getBlockState().getBlock() instanceof Sports.GoalBlock gb&&gb.hoop){
    Vec3 ring=Sports.GoalEntity.ring(g);Vec3 to=ring.subtract(eye);double d=to.length();if(d>40)continue;
    double ang=Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,to.normalize().dot(look)))));if(ang>35)continue;
    if(ang+d*.5<bd){bd=ang+d*.5;best=ring;}
   }
  }
  return best;
 }

 /** The four special moves of each sport, in key order (golpe 1..4). */
 public static String[] moves(Mode m){
  return switch(m){
   case SOCCER->new String[]{"Cavadinha","Chute de curva","Bicicleta / drible","Carrinho"};
   case KEEPER->new String[]{"Soco","Ponte (mergulho)","Tiro de meta / saída","Reposição com a mão / agarrar no alto"};
   case BASKET->new String[]{"Enterrada / bandeja","Passe quicado","Arremesso de 3","Crossover / toco"};
   case VOLLEY->new String[]{"Manchete","Levantamento","Cortada","Bloqueio (correndo + direito: saque viagem)"};
   default->new String[]{"-","-","-","-"};
  };
 }
 static void msg(Player p,String t){p.displayClientMessage(Component.literal(t),true);}
 /** Cooldown (ticks) of each button: 0 click, 1 right click, 2..5 special moves 1..4, 6 dominate the ball. The client uses the same numbers for the HUD. */
 public static int cooldown(Mode m,int button){
  return switch(button){case 0->8;case 1->12;case 6->14;
   case 2->m==Mode.VOLLEY?30:40;   // move 1
   case 3->m==Mode.BASKET?70:60;   // move 2
   case 4->m==Mode.BASKET?80:m==Mode.VOLLEY?50:70;   // move 3
   default->m==Mode.VOLLEY?50:80;};   // move 4 (carrinho, toco, bloqueio...)
 }
 /** Next game tick each player may use each button. */
 public static final Map<java.util.UUID,long[]> READY=new java.util.HashMap<>();
 public static boolean readyForTest(Player p,Mode m,int button){return ready(p,m,button);}
 static boolean ready(Player p,Mode m,int button){
  long now=p.level().getGameTime();long[] r=READY.computeIfAbsent(p.getUUID(),u->new long[7]);
  if(now<r[button]){if(button>=2)msg(p,"§7Golpe recarregando… "+String.format(java.util.Locale.ROOT,"%.1f",(r[button]-now)/20F)+" s");return false;}
  r[button]=now+cooldown(m,button);
  if(button>=2&&button<=5)for(int i=2;i<=5;i++)if(i!=button)r[i]=Math.max(r[i],now+16);   // no chaining specials back to back
  return true;
 }
 /** button 0 = left click, 1 = right click, 2..5 = special moves 1..4 (keys, Q by default for the first). Runs on the server. */
 public static void action(Player p,int button){
  var m=mode(p);if(m==null||m==Mode.TENNIS)return;
  if(!ready(p,m,button))return;
  if(button==6){if(m==Mode.SOCCER)dominate(p);return;}
  if(button>=3&&special(p,m,button-1))return;   // keys 2..4 -> moves 2..4
  var sl=(ServerLevel)p.level();
  Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();
  var held=held(p);
  if(m!=Mode.VOLLEY)PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new Anim(p.getId(),m==Mode.SOCCER||m==Mode.KEEPER&&held!=null&&button==0?0:m==Mode.KEEPER?1:2));
  switch(m){
   case SOCCER->{
    var b=nearest(p,Sports.Kind.SOCCER,2.4);if(b==null)return;
    if(button>2)return;
    var owner=b.holder();
    if(owner!=null&&owner!=p){   // someone is carrying it: try to take it from their feet
     if(p.getRandom().nextFloat()<.45F){b.hold(null);b.kick(flat.scale(.35),p);sound(p,SoundEvents.WOOL_STEP,1.2F);msg(p,"§aDesarmou!");msg((Player)owner,"§cPerdeu a bola!");}
     else{sound(p,SoundEvents.PLAYER_ATTACK_WEAK,1F);msg(p,"§7Não conseguiu desarmar");}
     return;}
    if(owner==p)b.hold(null);
    if(button==2){b.kick(flat.scale(.72).add(0,.95,0),p);sound(p,SoundEvents.WOOL_PLACE,1.4F);msg(p,"Cavadinha!");}             // Q: chip over the keeper
    else if(button==1){b.kick(passDir(p,flat,b).scale(1.25).add(0,.06,0).add(p.getDeltaMovement().multiply(.3,0,.3)),p);sound(p,SoundEvents.WOOL_STEP,1F);msg(p,"Passe");}   // right: low pass
    else{double pow=p.isSprinting()?2.05:1.45;double lift=Math.max(.12,-p.getXRot()/90.0*1.45+.14);
     b.kick(flat.scale(pow).add(0,lift,0).add(p.getDeltaMovement().multiply(.4,0,.4)),p);sound(p,SoundEvents.WOOL_BREAK,.8F);msg(p,p.isSprinting()?"Chute forte!":"Chute");}
    sl.sendParticles(ParticleTypes.CLOUD,b.getX(),b.getY()+.2,b.getZ(),3,.1,.05,.1,.02);
   }
   case KEEPER->{
    if(button>2)return;
    if(held!=null){
     if(button==0||button==2){held.release(flat.scale(2.2).add(0,.9,0),p);sound(p,SoundEvents.WOOL_BREAK,.6F);msg(p,"Tiro de meta");}   // punt
     else{held.release(passDir(p,flat,held).scale(1.5).add(0,.28,0),p);sound(p,SoundEvents.SNOWBALL_THROW,.8F);msg(p,"Reposição com a mão");}
     return;
    }
    if(button==2){var b=nearest(p,Sports.Kind.SOCCER,4.6);if(b!=null){b.kick(look.scale(1.8).add(0,.55,0),p);sound(p,SoundEvents.PLAYER_ATTACK_STRONG,1.2F);msg(p,"Soco!");}return;}   // Q: punch it away
    if(button!=0)return;
    var b=nearest(p,Sports.Kind.SOCCER,4.6);
    if(b==null){var far=nearest(p,Sports.Kind.SOCCER,9);if(far!=null){Vec3 to=far.position().add(far.getDeltaMovement().scale(4)).subtract(p.position());Vec3 dive=to.multiply(1,0,1).normalize().scale(Math.min(1.45,.6+to.horizontalDistance()*.12)).add(0,Mth.clamp(to.y*.12+.3,.25,.7),0);
     p.setDeltaMovement(dive);p.hurtMarked=true;DIVING.put(p.getUUID(),p.tickCount+14);anim(p,1);sound(p,SoundEvents.PLAYER_ATTACK_SWEEP,.7F);msg(p,"Mergulho!");}return;}
    b.hold(p);sound(p,SoundEvents.ARMOR_EQUIP_LEATHER.value(),1.2F);msg(p,"§eDefendeu! §7clique/Q: tiro de meta • direito: joga com a mão");
   }
   case BASKET->{
    if(button>2)return;
    if(held!=null){
     Vec3 ring=hoop(p);
     if(button==2&&ring!=null&&new Vec3(ring.x-p.getX(),0,ring.z-p.getZ()).length()<4.6){   // Q near the hoop: dunk
      p.setDeltaMovement(new Vec3(ring.x-p.getX(),0,ring.z-p.getZ()).scale(.3).add(0,.9,0));p.hurtMarked=true;
      held.hold(null);held.setPos(ring.x,ring.y+.45,ring.z);held.xo=ring.x;held.yo=ring.y+.6;held.zo=ring.z;held.kick(new Vec3(0,-.35,0),p);held.thrownFrom=p.position();
      sound(p,SoundEvents.ANVIL_LAND,1.6F);msg(p,"§6ENTERRADA!");return;
     }
     if(button==0){
      Vec3 from=held.position();Vec3 target=ring!=null?ring.add(0,.05,0):aimPoint(p);
      double dist=target.subtract(from).horizontalDistance();double t=Math.max(7,Math.min(22,6+dist*.85));   // 0.66.0: much faster and longer shots
      Vec3 v=arc(from,target,Sports.Kind.BASKET.gravity,t);
      double err=p.onGround()?.04:.015;   // jump shot is more accurate
      v=v.add(p.getRandom().nextGaussian()*err*.3,p.getRandom().nextGaussian()*err*.2,p.getRandom().nextGaussian()*err*.3);
      held.release(v,p);held.thrownFrom=p.position();sound(p,SoundEvents.SNOWBALL_THROW,.6F);msg(p,"Arremesso");
     }else if(button==2){held.release(flat.scale(1.55).add(0,-.36,0),p);sound(p,SoundEvents.SLIME_BLOCK_FALL,.7F);msg(p,"Passe quicado");}
     else pass(p,held,look);
     return;
    }
    if(button!=0)return;
    var b=nearest(p,Sports.Kind.BASKET,3.0);if(b==null)return;
    var other=b.holder();
    if(other!=null&&other!=p&&p.getRandom().nextFloat()>.35F){sound(p,SoundEvents.PLAYER_ATTACK_WEAK,1F);msg(p,"§7Não conseguiu roubar");return;}
    b.hold(p);sound(p,SoundEvents.SLIME_BLOCK_FALL,.6F);msg(p,other!=null&&other!=p?"§aRoubou a bola!":"Bola dominada — clique: arremesso • Q: enterrada/passe quicado • direito: passe");
   }
   case VOLLEY->{
    if(button>2)return;
    var b=nearest(p,Sports.Kind.VOLLEY,4.3);
    if(b==null){anim(p,button==1?7:3);return;}   // swing anyway: the arms move even when you miss
    double rel=b.getY()-p.getEyeY();Vec3 v;String what;int a;Vec3 tgt=aimPoint(p);
    double dist=tgt.subtract(b.position()).horizontalDistance();
    if(button==2){v=lob(b.position(),tgt,Sports.Kind.VOLLEY.gravity,7);what="Manchete";a=3;}   // Q
    else if(button==1&&p.isSprinting()){jump(p,flat.scale(.35).add(0,.55,0));v=lob(b.position(),tgt,Sports.Kind.VOLLEY.gravity,3.5);what="§6Saque viagem!";a=7;}   // running + right: jump serve
    else if(button==1){v=lob(b.position(),tgt,Sports.Kind.VOLLEY.gravity,5.5);what="Saque";a=7;}   // right: serve
    else if(!p.onGround()&&rel>-.6){v=look.scale(3.1).add(0,-.55,0);what="§c§lCortada!";a=5;}
    else if(rel>-.3){v=flat.scale(.24).add(0,1.3,0);what="Toque (levantamento)";a=4;}   // ball above the head: fingers
    else{v=flat.scale(.32).add(0,1.22,0);what="Manchete";a=3;}                              // low ball: forearms
    anim(p,a);b.kick(v,p);sound(p,SoundEvents.SLIME_BLOCK_HIT,a==5?.7F:a==3?1F:1.3F);msg(p,what);
    if(a==5)sl.sendParticles(ParticleTypes.CRIT,b.getX(),b.getY(),b.getZ(),8,.2,.2,.2,.2);
   }
   default->{}
  }
 }

 /** Goalkeeper: while diving (until that tick) any ball within reach sticks to the gloves. */
 static final Map<java.util.UUID,Integer> DIVING=new java.util.HashMap<>();
 /** 0.66.0 goalkeeper reflexes: a shot that comes at you is caught (or parried) on its own; diving, the reach is much bigger. */
 @SubscribeEvent public static void keeperTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){
  var p=e.getEntity();if(p.level().isClientSide||mode(p)!=Mode.KEEPER||held(p)!=null)return;
  Integer until=DIVING.get(p.getUUID());boolean diving=until!=null&&p.tickCount<=until;if(until!=null&&!diving)DIVING.remove(p.getUUID());
  double r=diving?2.9:1.7;
  for(var b:p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().inflate(r,r*.8,r),x->x.kind()==Sports.Kind.SOCCER&&x.holderId()<0)){
   if(b.getOwner()==p&&b.tickCount-b.kickedAtTick()<20)continue;   // not your own clearance
   Vec3 v=b.getDeltaMovement();double sp=v.length();if(!diving&&sp<.3)continue;
   Vec3 to=p.position().add(0,.9,0).subtract(b.position());if(!diving&&v.dot(to)<=0)continue;   // only balls coming at you
   if(diving||p.getRandom().nextFloat()<(sp>1.6?.55F:.85F)){b.hold(p);anim(p,1);sound(p,SoundEvents.ARMOR_EQUIP_LEATHER.value(),1.2F);msg(p,diving?"§b§lQUE DEFESA!":"§eDefendeu!");DIVING.remove(p.getUUID());}
   else{Vec3 out=new Vec3(-v.x,0,-v.z).normalize().scale(.5).add(new Vec3(-v.z,0,v.x).normalize().scale(p.getRandom().nextBoolean()?.5:-.5)).add(0,.55,0);
    b.kick(out,p);sound(p,SoundEvents.PLAYER_ATTACK_STRONG,1.2F);msg(p,"§eEspalmou!");}
   return;
  }
 }
 /** Players blocking at the net (until that tick). */
 static final Map<java.util.UUID,Integer> BLOCKING=new java.util.HashMap<>();
 @SubscribeEvent public static void blockTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){
  var p=e.getEntity();if(p.level().isClientSide)return;var until=BLOCKING.get(p.getUUID());if(until==null)return;
  if(p.tickCount>until){BLOCKING.remove(p.getUUID());return;}
  Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();
  // the hands: a wide wall above and in front of the head, held up until you land (max ~1 s)
  if(p.tickCount>until-18&&p.onGround()){BLOCKING.remove(p.getUUID());return;}
  var hands=new net.minecraft.world.phys.AABB(p.getX()-1.1,p.getEyeY()-.3,p.getZ()-1.1,p.getX()+1.1,p.getEyeY()+1.5,p.getZ()+1.1).move(flat.scale(.55));
  for(var b:p.level().getEntitiesOfClass(Sports.SportBall.class,hands,x->x.kind()==Sports.Kind.VOLLEY)){
   Vec3 in=b.getDeltaMovement();if(in.dot(flat)>0.05)continue;   // only balls coming toward you
   double force=in.length();Vec3 back=new Vec3(-in.x,0,-in.z);if(back.lengthSqr()<1e-4)back=flat;else back=back.normalize();
   // hard spike: stuffed straight down on their side; soft ball: "toque no bloqueio", pops up on your side to play on
   boolean stuff=force>.55||p.getRandom().nextFloat()<.6F;
   if(stuff){b.kick(flat.scale(.8+force*.35).add(0,-1.05,0),p);sound(p,SoundEvents.SLIME_BLOCK_HIT,.5F);msg(p,"§c§lBLOQUEOU!");
    ((ServerLevel)p.level()).sendParticles(ParticleTypes.CRIT,b.getX(),b.getY(),b.getZ(),16,.25,.25,.25,.25);}
   else{b.kick(back.scale(-.05).add(flat.scale(-.25)).add(0,.75,0),p);sound(p,SoundEvents.SLIME_BLOCK_HIT,1.4F);msg(p,"§eToque no bloqueio — continua!");}
   BLOCKING.remove(p.getUUID());break;
  }
 }
 /** Football (key V): take the ball to your feet and carry it; press again to let it go. */
 static void dominate(Player p){
  var mine=held(p);
  if(mine!=null){Vec3 f=new Vec3(p.getLookAngle().x,0,p.getLookAngle().z).normalize();mine.hold(null);mine.kick(f.scale(.25),p);msg(p,"Soltou a bola");return;}
  var b=nearest(p,Sports.Kind.SOCCER,2.6);if(b==null){msg(p,"§7Chegue perto da bola para dominar");return;}
  var other=b.holder();
  if(other!=null&&other!=p){if(p.getRandom().nextFloat()<.35F){b.hold(p);msg(p,"§aRoubou e dominou!");msg((Player)other,"§cPerdeu a bola!");}else msg(p,"§7Não conseguiu roubar");return;}
  if(b.getY()>p.getY()+1.4){msg(p,"§7A bola está alta demais");return;}
  b.hold(p);sound(p,SoundEvents.WOOL_STEP,.8F);msg(p,"§eBola dominada §7— ande para conduzir • clique: chute • V: soltar");
 }
 static void anim(Player p,int type){PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new Anim(p.getId(),type));}
 static void jump(Player p,Vec3 v){p.setDeltaMovement(v);p.hurtMarked=true;}
 /** Special moves 2..4 (move 1 is the classic Q move handled above). Returns true when handled. */
 static boolean special(Player p,Mode m,int k){
  var sl=(ServerLevel)p.level();Vec3 look=p.getLookAngle();Vec3 flat=new Vec3(look.x,0,look.z).normalize();Vec3 side=new Vec3(-flat.z,0,flat.x);
  var held=held(p);
  switch(m){
   case SOCCER->{
    var b=nearest(p,Sports.Kind.SOCCER,2.6);
    if(k==2){   // curve shot: goes out wide and bends back toward where you aim
     if(b==null)return true;anim(p,0);if(b.holder()==p)b.hold(null);else if(b.holder()!=null)return true;
     double dir=p.getRandom().nextBoolean()?1:-1;
     b.kick(flat.scale(1.7).add(side.scale(dir*.62)).add(0,.36,0),p);b.curve(side.scale(-dir*.066),21);
     sound(p,SoundEvents.WOOL_BREAK,1.1F);msg(p,"§bChute de curva!");sl.sendParticles(ParticleTypes.SWEEP_ATTACK,b.getX(),b.getY()+.3,b.getZ(),1,0,0,0,0);return true;}
    if(k==3){   // bicycle kick when the ball is in the air above you, otherwise a dribble burst
     var air=p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().inflate(2.6,3,2.6),x->x.kind()==Sports.Kind.SOCCER&&x.getY()>p.getY()+.9).stream().findFirst().orElse(null);
     if(air!=null){anim(p,0);jump(p,new Vec3(0,.55,0).subtract(flat.scale(.15)));air.kick(flat.scale(2.3).add(0,-.05,0),p);sound(p,SoundEvents.PLAYER_ATTACK_KNOCKBACK,.8F);msg(p,"§6§lBICICLETA!");
      sl.sendParticles(ParticleTypes.CRIT,air.getX(),air.getY(),air.getZ(),12,.2,.2,.2,.2);return true;}
     // dribble: a quick cut to the side WITH the ball — it stays glued to your feet
     if(b==null)return true;var other=b.holder();if(other!=null&&other!=p)return true;
     anim(p,0);double dir=p.getRandom().nextBoolean()?1:-1;b.hold(p);
     jump(p,side.scale(dir*.55).add(flat.scale(.4)).add(0,.04,0));sound(p,SoundEvents.WOOL_STEP,1.3F);msg(p,"§bDrible!");return true;}
    // carrinho: slide forward low; takes the ball away from anyone in the way
    anim(p,0);jump(p,flat.scale(1.05).add(0,.02,0));sound(p,SoundEvents.GRAVEL_STEP,.8F);msg(p,"Carrinho!");
    sl.sendParticles(ParticleTypes.POOF,p.getX(),p.getY()+.1,p.getZ(),8,.3,.05,.3,.02);
    var tb=p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().expandTowards(flat.scale(3.5)).inflate(.6),x->x.kind()==Sports.Kind.SOCCER).stream().findFirst().orElse(null);
    if(tb!=null){if(tb.holder()!=null&&tb.holder()!=p)msg(tb.holder(),"§cLevou um carrinho!");tb.hold(null);tb.kick(flat.scale(.7).add(side.scale(.3)).add(0,.1,0),p);}
    return true;
   }
   case KEEPER->{
    if(k==2){   // ponte: long dive toward the nearest ball (or to the side you look)
     var far=nearest(p,Sports.Kind.SOCCER,13);anim(p,1);
     Vec3 dir=far!=null?far.position().add(far.getDeltaMovement().scale(5)).subtract(p.position()).multiply(1,0,1).normalize():flat;
     jump(p,dir.scale(1.6).add(0,.5,0));DIVING.put(p.getUUID(),p.tickCount+18);sound(p,SoundEvents.PLAYER_ATTACK_SWEEP,.6F);msg(p,"§bPonte!");
     return true;}
    if(k==3){   // with the ball: goal kick; without: rush out of the goal toward the ball
     if(held!=null){anim(p,0);held.release(flat.scale(2.4).add(0,.95,0),p);sound(p,SoundEvents.WOOL_BREAK,.6F);msg(p,"Tiro de meta");return true;}
     var far=nearest(p,Sports.Kind.SOCCER,18);if(far!=null){Vec3 dir=far.position().subtract(p.position()).multiply(1,0,1).normalize();jump(p,dir.scale(1.25).add(0,.12,0));DIVING.put(p.getUUID(),p.tickCount+12);msg(p,"Saída do gol!");}
     return true;}
    // with the ball: hand throw; without: high catch (jump and grab a ball in the air)
    if(held!=null){anim(p,1);held.release(passDir(p,flat,held).scale(1.55).add(0,.26,0),p);sound(p,SoundEvents.SNOWBALL_THROW,.8F);msg(p,"Reposição com a mão");return true;}
    anim(p,1);jump(p,new Vec3(0,.78,0));DIVING.put(p.getUUID(),p.tickCount+16);
    var high=p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().inflate(3,3.6,3),x->x.kind()==Sports.Kind.SOCCER).stream().findFirst().orElse(null);
    if(high!=null){high.hold(p);msg(p,"§eAgarrou no alto!");}else msg(p,"Pulo do goleiro");
    return true;
   }
   case BASKET->{
    Vec3 ring=hoop(p);
    if(k==2){if(held==null)return true;anim(p,2);held.release(flat.scale(1.55).add(0,-.36,0),p);sound(p,SoundEvents.SLIME_BLOCK_FALL,.7F);msg(p,"Passe quicado");return true;}
    if(k==3){   // three-pointer: jump back and shoot a high arc; very accurate from far
     if(held==null)return true;anim(p,2);jump(p,flat.scale(-.3).add(0,.55,0));
     Vec3 from=held.position();Vec3 target=ring!=null?ring.add(0,.05,0):aimPoint(p);double dist=target.subtract(from).horizontalDistance();
     Vec3 v=arc(from,target,Sports.Kind.BASKET.gravity,Math.max(9,Math.min(24,8+dist*.95)));
     double err=dist>7.5?.006:.03;v=v.add(p.getRandom().nextGaussian()*err*.3,0,p.getRandom().nextGaussian()*err*.3);
     held.release(v,p);held.thrownFrom=p.position();sound(p,SoundEvents.SNOWBALL_THROW,.5F);msg(p,dist>7.5?"§6Arremesso de 3!":"Arremesso com fade");return true;}
    if(held!=null){   // crossover: the ball switches hands with a low bounce and you explode to that side (alternates left/right)
     anim(p,2);held.hand=-held.hand;double dir=held.hand;jump(p,side.scale(dir*1.2).add(flat.scale(.55)).add(0,.06,0));
     sound(p,SoundEvents.SLIME_BLOCK_FALL,1.3F);sound(p,SoundEvents.PLAYER_ATTACK_SWEEP,1.6F);msg(p,"§b§lCROSSOVER!");
     for(var o:p.level().getEntitiesOfClass(Player.class,p.getBoundingBox().inflate(2.2),x->x!=p&&mode(x)!=null)){o.setDeltaMovement(o.getDeltaMovement().add(side.scale(-dir*.35)));o.hurtMarked=true;msg(o,"§cTomou um crossover!");}   // defenders get wrong-footed
     sl.sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.1,p.getZ(),6,.3,.05,.3,.03);return true;}
    // block: jump high and swat any ball in the air near you
    anim(p,2);jump(p,new Vec3(0,.8,0));
    var air=p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().inflate(2.8,3.8,2.8),x->x.kind()==Sports.Kind.BASKET&&x.holderId()<0&&x.getY()>p.getY()+1.2).stream().findFirst().orElse(null);
    if(air!=null){air.kick(flat.scale(.7).add(0,-.35,0),p);sound(p,SoundEvents.PLAYER_ATTACK_STRONG,.9F);msg(p,"§c§lTOCO!");}else msg(p,"Pulo para o toco");
    return true;
   }
   case VOLLEY->{
    var b=nearest(p,Sports.Kind.VOLLEY,4.3);
    if(k==2){   // set: soft and high, lands about 2 blocks in front of you for a teammate's spike
     anim(p,4);if(b==null)return true;Vec3 tgt=p.position().add(flat.scale(2.2));b.kick(lob(b.position(),tgt.add(0,2.6,0),Sports.Kind.VOLLEY.gravity,8),p);
     sound(p,SoundEvents.SLIME_BLOCK_HIT,1.5F);msg(p,"Levantamento");return true;}
    if(k==3){   // spike: jumps on its own if on the ground, then smashes down where you aim
     anim(p,5);if(p.onGround())jump(p,new Vec3(0,.6,0));if(b==null)return true;
     b.kick(look.scale(3.4).add(0,-.65,0),p);sound(p,SoundEvents.PLAYER_ATTACK_STRONG,1F);sound(p,SoundEvents.SLIME_BLOCK_HIT,.6F);msg(p,"§c§lCORTADA!");
     sl.sendParticles(ParticleTypes.CRIT,b.getX(),b.getY(),b.getZ(),10,.2,.2,.2,.2);return true;}
    // block: jump at the net with both hands up; for a moment any ball coming at your hands goes straight back down on their side
    anim(p,6);if(p.onGround())jump(p,new Vec3(0,.62,0));BLOCKING.put(p.getUUID(),p.tickCount+22);
    sound(p,SoundEvents.ARMOR_EQUIP_LEATHER.value(),.7F);msg(p,"§bBloqueio!");return true;
   }
   default->{return false;}
  }
 }

 // ---------------------------------------------------------------- network
 public record Action(int button) implements CustomPacketPayload {
  public static final Type<Action> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"sport_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Action::button,Action::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 /** Animation broadcast: 0 = kick, 1 = keeper catch, 2 = basketball shot, 3 = volleyball bump. */
 public record Anim(int entity,int move) implements CustomPacketPayload {
  public static final Type<Anim> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"sport_anim"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Anim> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Anim::entity,ByteBufCodecs.VAR_INT,Anim::move,Anim::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static java.util.function.Consumer<Anim> clientAnim=a->{};
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
   var r=e.registrar("15");
   r.playToServer(Action.TYPE,Action.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer sp&&sp.isAlive()&&p.button()>=0&&p.button()<=6)action(sp,p.button());});
   r.playToClient(Anim.TYPE,Anim.CODEC,(p,c)->clientAnim.accept(p));
  }
 }
}
