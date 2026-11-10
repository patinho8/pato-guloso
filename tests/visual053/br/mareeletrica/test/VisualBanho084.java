package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.banho084): big bathtub, ducks put into a tub, tactical armor vs bullets, bullet knockback. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualBanho084 {
 static int tick;static BlockPos o;static Zombie z,target;static Vec3 base,before;static java.util.List<Pato.Duck> ducks=new java.util.ArrayList<>();
 static boolean on(){return Boolean.getBoolean("mareeletrica.banho084");}
 static void log(String s){System.out.println("[B84] "+s);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"b84_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shoot(net.minecraft.world.item.Item gun,double lat,double h){
  var p=sp();var l=p.serverLevel();z.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(gun));
  Vec3 tg=p.position().add(lat,h,0);Vec3 from=tg.add(0,0,-1.2);
  var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),l);b.setOwner(z);p.invulnerableTime=0;b.setPos(from);Vec3 d=tg.subtract(from).normalize();b.shoot(d.x,d.y,d.z,4F,0F);l.addFreshEntity(b);}
 static String st(){var s=Wounds.st(sp());return "limbs LA="+s.limb[0]+" RA="+s.limb[1]+" LL="+s.limb[2]+" RL="+s.limb[3]+" broken RA="+s.broken(1)+" LL="+s.broken(2);}
 @SubscribeEvent public static void dmg(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e){if(on()&&e.getEntity() instanceof net.minecraft.world.entity.player.Player&&tick>150)log("player took "+e.getNewDamage());}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(false);o=p.blockPosition().atY(200);l.setDayTime(6000);
   for(int x=-10;x<=10;x++)for(int zz=-14;zz<=10;zz++){l.setBlockAndUpdate(o.offset(x,-1,zz),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<6;y++)l.setBlockAndUpdate(o.offset(x,y,zz),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(o.offset(x,7,zz),Blocks.GLOWSTONE.defaultBlockState());}
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+.5,180,30);
   // place the big tub like a player would: holding it, clicking the floor 2 blocks north
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Bath.BIG_ITEM.get()));
   var floor=o.offset(0,-1,-2);var r=new ItemStack(Bath.BIG_ITEM.get()).useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false)));
   p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
   int n=0;for(var q:BlockPos.betweenClosed(o.offset(-3,0,-6),o.offset(3,0,0)))if(l.getBlockState(q).is(Bath.BIG.get()))n++;
   log("placed "+r+" cells "+n+" front "+l.getBlockState(o.offset(0,0,-2))+" center "+l.getBlockState(o.offset(0,0,-3)));
   for(int i=0;i<4;i++){var d=Pato.ENTITY.get().create(l);d.moveTo(o.getX()+2+i*.6,o.getY(),o.getZ()+2,0,0);d.tame(p);d.setMode(Pato.FOLLOW);d.setLook(i%3,i==0?2:0,0);l.addFreshEntity(d);ducks.add(d);}});
  if(tick>=60&&tick<100){mc.player.setYRot(180);mc.player.setXRot(35);}
  if(tick==80){mc.options.hideGui=true;shot("vazia");}
  if(tick==85)srv(()->{var p=sp();var l=p.serverLevel();var pos=o.offset(0,0,-3);l.getBlockState(pos).useWithoutItem(l,p,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));});
  // two ducks on the shoulders, Shift+click the tub: they go into the water
  if(tick==90)srv(()->{var p=sp();ducks.get(0).startRiding(p,true);ducks.get(1).startRiding(p,true);});
  if(tick==95)srv(()->{var p=sp();p.setShiftKeyDown(true);var pos=o.offset(1,0,-4);
   var ev=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
   net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(ev);p.setShiftKeyDown(false);
   log("shoulder->tub canceled "+ev.isCanceled()+" bath flags "+ducks.stream().map(d->d.flag(Pato.Duck.BATH)+"@"+d.position()).toList());});
  if(tick==120){shot("patos_na_banheira");}
  if(tick==125)srv(()->{var p=sp();var l=p.serverLevel();var pos=o.offset(-1,0,-2);l.getBlockState(pos).useWithoutItem(l,p,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));log("player seat "+p.getVehicle()+" at "+p.position());});
  if(tick==128)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==200){shot("ofuro");srv(()->log("ducks in bath "+ducks.stream().filter(d->d.flag(Pato.Duck.BATH)).count()+" regen "+sp().hasEffect(MobEffects.REGENERATION)));}
  // ---- tactical armor vs bullets
  if(tick==205)srv(()->{var p=sp();p.stopRiding();var l=p.serverLevel();p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+6.5,180,5);base=p.position();
   z=EntityType.ZOMBIE.create(l);z.moveTo(p.getX(),p.getY()-5,p.getZ()-6,0,0);z.setNoAi(true);z.setInvisible(true);z.setInvulnerable(true);l.addFreshEntity(z);
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Wounds.TAC_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Wounds.TAC_CHEST.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Wounds.TAC_LEGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Wounds.TAC_BOOTS.get()));
   p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,99999,4,false,false));log("armor value "+p.getArmorValue()+" toughness "+p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS));});
  if(tick>205&&tick<400&&tick%3==0)srv(()->{var p=sp();p.setHealth(p.getMaxHealth());if(base!=null&&p.position().distanceToSqr(base)>.01)p.teleportTo(base.x,base.y,base.z);p.setYRot(180);p.setYBodyRot(180);});
  if(tick>205&&tick<400){mc.player.setYRot(180);mc.player.setXRot(5);}
  for(int i=0;i<8;i++)if(tick==215+i*8)srv(()->shoot(Arsenal.AK47.get(),.27,1.25));
  if(tick==285)srv(()->log("ARMORED 8 AK right arm: "+st()+" chest dmg "+sp().getItemBySlot(EquipmentSlot.CHEST).getDamageValue()));
  if(tick==288)srv(()->shoot(MareEletrica.SNIPER.get(),-.12,.45));
  if(tick==300)srv(()->{log("ARMORED sniper left leg: "+st());var p=sp();for(var s:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})p.setItemSlot(s,ItemStack.EMPTY);Wounds.st(p).limb[1]=0;Wounds.st(p).limb[2]=0;});
  for(int i=0;i<8;i++)if(tick==305+i*8)srv(()->shoot(Arsenal.AK47.get(),.27,1.25));
  if(tick==375)srv(()->log("BARE 8 AK right arm: "+st()));
  if(tick==378)srv(()->shoot(MareEletrica.SNIPER.get(),-.12,.45));
  if(tick==390)srv(()->log("BARE 1 sniper left leg: "+st()));
  // ---- knockback: an AK hit on a zombie
  if(tick==400)srv(()->{var p=sp();var l=p.serverLevel();p.removeAllEffects();
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Wounds.TAC_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Wounds.TAC_CHEST.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Wounds.TAC_LEGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Wounds.TAC_BOOTS.get()));
   target=EntityType.ZOMBIE.create(l);target.moveTo(o.getX()+5.5,o.getY(),o.getZ()+6.5,0,0);target.setNoAi(true);l.addFreshEntity(target);});
  if(tick==405)srv(()->{var l=sp().serverLevel();before=target.position();for(int i=0;i<3;i++){var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),l);b.setOwner(z);z.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Arsenal.AK47.get()));b.setPos(target.position().add(0,1,-1.5));b.shoot(0,0,1,4F,0F);l.addFreshEntity(b);}});
  if(tick==425)srv(()->log("zombie knocked "+String.format("%.3f",target.position().distanceTo(before))+" blocks, hp "+target.getHealth()));
  if(tick==430){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.player.setXRot(10);}
  if(tick==445)shot("armadura");
  if(tick==450)log("DONE");
 }
}
