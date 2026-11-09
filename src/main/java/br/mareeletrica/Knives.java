package br.mareeletrica;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.registries.DeferredItem;

/** Melee set: butterfly, karambit, xeno hunter, HRT3 and a (literal) banana. */
@net.neoforged.fml.common.EventBusSubscriber(modid=MareEletrica.ID)
public final class Knives {
 /** Everyone nearby hears the blade (the attacker already heard it on their own game). */
 @net.neoforged.bus.api.SubscribeEvent public static void slash(net.neoforged.neoforge.event.entity.player.AttackEntityEvent e){
  var p=e.getEntity();if(p.level().isClientSide||!is(p.getMainHandItem()))return;
  p.level().playSound(p,p.getX(),p.getY()+1,p.getZ(),MareEletrica.KNIFE_SWING.get(),net.minecraft.sounds.SoundSource.PLAYERS,.9F,.9F+p.getRandom().nextFloat()*.3F);
  if(p.level() instanceof net.minecraft.server.level.ServerLevel sl){
   long now=sl.getGameTime();long[] c=COMBO.computeIfAbsent(p.getUUID(),u->new long[]{-100,-1});
   c[1]=now-c[0]<=30?(c[1]+1)%3:0;c[0]=now;slashTrail(sl,p,id(p.getMainHandItem()),(int)c[1]);}
 }
 /** 0.78.0: per-player combo (last hit time, step) so the 2nd hit's trail goes the other way and the 3rd is wider. */
 private static final java.util.Map<java.util.UUID,long[]> COMBO=new java.util.HashMap<>();
 public static final DeferredItem<Item> BUTTERFLY=knife("butterfly_knife",4,-.4F),KARAMBIT=knife("karambit",4,-.5F),XENO=knife("xeno_hunter",6,-1.0F),HRT3=knife("hrt3_knife",5,-.7F);   // stronger and faster than swords
 public static final DeferredItem<Item> BANANA=MareEletrica.ITEMS.register("banana",()->new Item(new Item.Properties()
  .food(new FoodProperties.Builder().nutrition(4).saturationModifier(.4F).build())
  .attributes(ItemAttributeModifiers.builder()
   .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_damage"),3.0,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
   .add(Attributes.ATTACK_SPEED,new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_speed"),-1.2,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build())));
 private Knives(){}
 private static DeferredItem<Item> knife(String id,int damage,float speed){
  return MareEletrica.ITEMS.register(id,()->new KnifeItem(new Item.Properties().attributes(SwordItem.createAttributes(Tiers.IRON,damage,speed))));
 }
 public static void register(){}
 /** A knife: sword stats, but no vanilla sweep (each knife draws its own slash instead). */
 public static final class KnifeItem extends SwordItem {
  public KnifeItem(Item.Properties p){super(Tiers.IRON,p);}
  @Override public boolean canPerformAction(ItemStack stack,net.neoforged.neoforge.common.ItemAbility ability){return ability!=net.neoforged.neoforge.common.ItemAbilities.SWORD_SWEEP&&super.canPerformAction(stack,ability);}
 }
 /** Slash trail drawn in front of the attacker, different for every knife. */
 private static void slashTrail(net.minecraft.server.level.ServerLevel sl,net.minecraft.world.entity.player.Player p,String id,int step){
  var look=p.getLookAngle();var right=look.cross(new net.minecraft.world.phys.Vec3(0,1,0));
  if(right.lengthSqr()<1e-4)right=new net.minecraft.world.phys.Vec3(1,0,0);right=right.normalize();var up=right.cross(look).normalize();
  var c=p.getEyePosition().add(look.scale(1.5));
  float[] col;double r,a0,a1;double tiltR,tiltU;   // the arc lies in the plane (tiltR*right+tiltU*up, look)
  switch(id){
   case "karambit"->{col=new float[]{.78F,.3F,1F};r=.85;a0=-80;a1=80;tiltR=0;tiltU=1;}       // tall vertical rip
   case "xeno_hunter"->{col=new float[]{1F,.55F,.12F};r=1.1;a0=-70;a1=70;tiltR=.7;tiltU=.7;}   // big diagonal chop
   case "hrt3_knife"->{col=new float[]{.8F,1F,.8F};r=0;a0=0;a1=0;tiltR=0;tiltU=0;}            // straight thrust
   case "banana"->{col=new float[]{1F,.92F,.2F};r=.7;a0=-60;a1=60;tiltR=1;tiltU=.3;}
   default->{col=new float[]{.7F,.95F,1F};r=.55;a0=-70;a1=70;tiltR=1;tiltU=0;}                 // butterfly: quick flat flick
  }
  if(step==1){ // 2nd hit: the cut goes the other way (thrusts become a sideways slash)
   if(r==0){r=.6;a0=-70;a1=70;tiltR=1;tiltU=-.2;}else{double t=a0;a0=a1;a1=t;tiltR=-tiltR;}
   if("karambit".equals(id)){tiltR=1;tiltU=.15;}}
  if(step==2){r=(r==0?.6:r)*1.3;if(a0==0&&a1==0){a0=-80;a1=80;tiltR=1;}a0*=1.15;a1*=1.15;}   // finisher: wider
  var dust=new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(col[0],col[1],col[2]),1.1F);
  if(r==0){for(int i=0;i<12;i++){var q=p.getEyePosition().add(look.scale(.6+i*.13)).subtract(up.scale(.15));sl.sendParticles(dust,q.x,q.y,q.z,1,0,0,0,0);}return;}
  var side=right.scale(tiltR).add(up.scale(tiltU)).normalize();
  for(int i=0;i<=16;i++){double a=Math.toRadians(a0+(a1-a0)*i/16.0);var q=c.add(side.scale(Math.sin(a)*r)).subtract(look.scale((1-Math.cos(a))*r*.6));sl.sendParticles(dust,q.x,q.y,q.z,1,0,0,0,0);if(i%4==0)sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,q.x,q.y,q.z,1,0,0,0,0);}
 }
 public static String id(ItemStack s){var i=s.getItem();return i==BUTTERFLY.get()?"butterfly_knife":i==KARAMBIT.get()?"karambit":i==XENO.get()?"xeno_hunter":i==HRT3.get()?"hrt3_knife":i==BANANA.get()?"banana":null;}
 public static boolean is(ItemStack s){var i=s.getItem();return i==BUTTERFLY.get()||i==KARAMBIT.get()||i==XENO.get()||i==HRT3.get()||i==BANANA.get();}
 public static void creative(CreativeModeTab.Output out){out.accept(BUTTERFLY.get());out.accept(KARAMBIT.get());out.accept(XENO.get());out.accept(HRT3.get());out.accept(BANANA.get());}
}
