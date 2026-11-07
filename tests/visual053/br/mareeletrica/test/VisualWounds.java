package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.wounds): shots on given body parts break limbs as designed; vest, gun lock, meds, bleeding. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualWounds {
 static int tick;static Zombie z;static Vec3 base;
 @SubscribeEvent public static void dmg(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e){if(on()&&e.getEntity() instanceof net.minecraft.world.entity.player.Player)log("damage "+e.getAmount()+" from "+e.getSource().getDirectEntity()+" mode "+((net.minecraft.server.level.ServerPlayer)e.getEntity()).gameMode.getGameModeForPlayer());}
 static boolean on(){return Boolean.getBoolean("mareeletrica.wounds");}
 static void log(String s){System.out.println("[WND] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shoot(net.minecraft.world.item.Item gun,double lat,double h){
  var p=sp();var l=p.serverLevel();z.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(gun));
  Vec3 target=p.position().add(lat,h,0);Vec3 from=target.add(0,0,-1.2);   // player faces north (-z): the shot comes from the front
  var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),l);b.setOwner(z);p.invulnerableTime=0;b.setPos(from);Vec3 d=target.subtract(from).normalize();b.shoot(d.x,d.y,d.z,4F,0F);l.addFreshEntity(b);log("shot from "+from+" to "+target+" player at "+p.position()+" bbox "+p.getBoundingBox());
 }
 static String st(){var s=Wounds.st(sp());return "limbs LA="+s.limb[0]+" RA="+s.limb[1]+" LL="+s.limb[2]+" RL="+s.limb[3]+" bleed="+s.bleed+" decals="+s.decals.size()+" hp="+sp().getHealth();}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick>=60&&tick<480){mc.player.setYRot(180);mc.player.setXRot(5);mc.player.yBodyRot=180;}
  if(tick==40&&mc.player.isDeadOrDying()){mc.player.respawn();mc.setScreen(null);}
  if(tick==60)srv.execute(()->{var p=sp();p.setGameMode(GameType.SURVIVAL);p.getAbilities().flying=false;p.onUpdateAbilities();var l=p.serverLevel();
   p.setYRot(180);p.setYBodyRot(180);p.yBodyRot=180;base=p.position();
   z=EntityType.ZOMBIE.create(l);z.moveTo(p.getX(),p.getY()-5,p.getZ()-6,0,0);z.setNoAi(true);z.setInvisible(true);z.setInvulnerable(true);l.addFreshEntity(z);
   p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,99999,4,false,false));log("start "+st()+" mode "+p.gameMode.getGameModeForPlayer()+" pos "+p.position());});
  if(tick>60&&tick<340&&tick%3==0)srv.execute(()->{var p=sp();p.setHealth(p.getMaxHealth());if(base!=null&&p.position().distanceToSqr(base)>.01)p.teleportTo(base.x,base.y,base.z);p.setYBodyRot(180);p.yBodyRot=180;p.setYRot(180);});
  // 3 AK-47 hits on the right arm (player's right = +x when facing north)
  for(int i=0;i<3;i++)if(tick==80+i*12)srv.execute(()->shoot(Arsenal.AK47.get(),.27,1.25));
  if(tick==125)srv.execute(()->log("after 3 AK on right arm: "+st()+" | heavy blocked? "+Wounds.blocked(sp(),new ItemStack(MareEletrica.SNIPER.get()))+" | pistol blocked? "+Wounds.blocked(sp(),new ItemStack(Arsenal.USP.get()))));
  // 7 USP hits on the right leg: not yet; the 8th breaks it
  for(int i=0;i<8;i++)if(tick==135+i*8)srv.execute(()->shoot(Arsenal.USP.get(),.12,.45));
  if(tick==170)srv.execute(()->log("after 5 USP on right leg: "+st()));
  if(tick==210)srv.execute(()->log("after 8 USP on right leg: "+st()+" crouching(client)="+Minecraft.getInstance().player.isCrouching()));
  // vest: torso hit with and without
  if(tick==215)srv.execute(()->{sp().setItemSlot(EquipmentSlot.CHEST,new ItemStack(Wounds.VEST.get()));});
  if(tick==220)srv.execute(()->shoot(Arsenal.AK47.get(),0,1.2));
  if(tick==232)srv.execute(()->log("torso hit with vest: "+st()+" vest dmg "+sp().getItemBySlot(EquipmentSlot.CHEST).getDamageValue()));
  if(tick==86)Screenshot.grab(mc.gameDirectory,"wounds_1hit.png",mc.getMainRenderTarget(),c->{});
  if(tick==236)Screenshot.grab(mc.gameDirectory,"wounds_screen.png",mc.getMainRenderTarget(),c->{});
  // 1 sniper hit on the left leg -> both legs gone -> prone
  if(tick==245)srv.execute(()->shoot(MareEletrica.SNIPER.get(),-.12,.45));
  if(tick==275)srv.execute(()->log("after 1 sniper on left leg: "+st()+" prone="+ProneSystem.prone(sp())));
  if(tick==280){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.options.hideGui=true;}
  if(tick==300)Screenshot.grab(mc.gameDirectory,"wounds_body.png",mc.getMainRenderTarget(),c->{});
  if(tick==305){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.options.hideGui=false;}
  // meds: syringe fixes the worst limb, bandage stops bleeding, medkit clears everything
  if(tick==310)srv.execute(()->{var p=sp();var s=new ItemStack(Wounds.SYRINGE.get());s.getItem().finishUsingItem(s,p.level(),p);log("after syringe: "+st());
   var b=new ItemStack(Wounds.BANDAGE.get());b.getItem().finishUsingItem(b,p.level(),p);log("after bandage: "+st());
   log("use time bandage (one arm broken) = "+Wounds.BANDAGE.get().getUseDuration(b,p)+" ticks");});
  if(tick==330)srv.execute(()->{var p=sp();var k=new ItemStack(Wounds.MEDKIT.get());k.getItem().finishUsingItem(k,p.level(),p);log("after medkit: "+st()+" prone="+ProneSystem.prone(p));});
  if(tick==334)srv.execute(()->{var p=sp();p.getInventory().selected=0;p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
   var s=Wounds.st(p);s.limb[0]=24;s.limb[1]=24;s.limb[3]=10;s.bleed=5400;Wounds.save(p,s,true);});
  if(tick==338)srv.execute(()->{var p=sp();int where=-1;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(net.minecraft.world.item.Items.DIAMOND_SWORD))where=i;log("both arms broken: hand="+p.getMainHandItem()+" sword now in slot "+where);});
  if(tick==342)Screenshot.grab(mc.gameDirectory,"wounds_hud.png",mc.getMainRenderTarget(),c->{});
  // bleeding to death: shoot once, then fast-forward the timer
  if(tick==346)srv.execute(()->{sp().setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);shoot(Arsenal.USP.get(),0,1.2);});
  if(tick==360)srv.execute(()->{var s=Wounds.st(sp());log("shot in torso without vest: bleed="+s.bleed);s.bleed=30;});
  if(tick==420)srv.execute(()->log("after bleed timer ran out: dead="+sp().isDeadOrDying()+" hp="+sp().getHealth()));
  if(tick==440){log("DONE");mc.stop();}
 }
}
