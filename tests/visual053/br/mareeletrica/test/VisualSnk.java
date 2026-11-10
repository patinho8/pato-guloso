package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.snk): sniper always kills on head/chest and always breaks a limb, through armor. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualSnk {
 static int tick;static Zombie z,mob;static Vec3 base;
 static boolean on(){return Boolean.getBoolean("mareeletrica.snk");}
 static void log(String s){System.out.println("[SNK] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void dmg(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e){if(on()&&tick>60)log(e.getEntity().getType().toShortString()+" took "+e.getNewDamage());}
 @SubscribeEvent public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(on()&&e.getEntity() instanceof net.minecraft.world.entity.player.Player p){log("PLAYER WOULD DIE");e.setCanceled(true);p.setHealth(20);}}
 static void shoot(Vec3 tg){var p=sp();var l=p.serverLevel();z.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(MareEletrica.SNIPER.get()));Vec3 from=tg.add(0,0,-1.2);
  var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),l);b.setOwner(z);b.setPos(from);Vec3 d=tg.subtract(from).normalize();b.shoot(d.x,d.y,d.z,4F,0F);l.addFreshEntity(b);}
 static String st(){var s=Wounds.st(sp());return "LA="+s.limb[0]+" RA="+s.limb[1]+" LL="+s.limb[2]+" RL="+s.limb[3]+" hp="+sp().getHealth();}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(false);var o=p.blockPosition().atY(200);
   for(int x=-6;x<=6;x++)for(int zz=-8;zz<=8;zz++){l.setBlockAndUpdate(o.offset(x,-1,zz),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(o.offset(x,y,zz),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+.5,180,0);base=p.position();
   z=EntityType.ZOMBIE.create(l);z.moveTo(p.getX(),p.getY()-5,p.getZ()-6,0,0);z.setNoAi(true);z.setInvisible(true);z.setInvulnerable(true);l.addFreshEntity(z);
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Wounds.TAC_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Wounds.TAC_CHEST.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Wounds.TAC_LEGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Wounds.TAC_BOOTS.get()));});
  if(tick>60&&tick<200&&tick%3==0)srv.execute(()->{var p=sp();if(base!=null&&p.position().distanceToSqr(base)>.01)p.teleportTo(base.x,base.y,base.z);p.setYRot(180);p.setYBodyRot(180);p.yBodyRot=180;});
  if(tick>60&&tick<200){mc.player.setYRot(180);mc.player.setXRot(5);}
  if(tick==80)srv.execute(()->{sp().invulnerableTime=0;shoot(sp().position().add(0,1.2,0));});
  if(tick==95)srv.execute(()->log("after chest: "+st()));
  if(tick==100)srv.execute(()->{sp().invulnerableTime=0;shoot(sp().position().add(0,1.65,0));});
  if(tick==115)srv.execute(()->log("after head: "+st()));
  if(tick==120)srv.execute(()->{sp().invulnerableTime=0;sp().setHealth(20);shoot(sp().position().add(.12,.45,0));});
  if(tick==135)srv.execute(()->log("after leg: "+st()+" brokenRL="+Wounds.st(sp()).broken(3)));
  if(tick==140)srv.execute(()->{sp().invulnerableTime=0;shoot(sp().position().add(-.27,1.25,0));});
  if(tick==155)srv.execute(()->log("after arm: "+st()+" brokenLA="+Wounds.st(sp()).broken(0)));
  if(tick==160)srv.execute(()->{var l=sp().serverLevel();mob=EntityType.ZOMBIE.create(l);mob.moveTo(base.x+4,base.y,base.z,0,0);mob.setNoAi(true);
   for(var s:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})mob.setItemSlot(s,new ItemStack(s==EquipmentSlot.HEAD?Items.NETHERITE_HELMET:s==EquipmentSlot.CHEST?Items.NETHERITE_CHESTPLATE:s==EquipmentSlot.LEGS?Items.NETHERITE_LEGGINGS:Items.NETHERITE_BOOTS));l.addFreshEntity(mob);});
  if(tick==165)srv.execute(()->{var l=sp().serverLevel();var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),l);b.setOwner(z);b.setPos(mob.position().add(0,1.3,-1.2));b.shoot(0,0,1,4F,0F);l.addFreshEntity(b);});
  if(tick==180)srv.execute(()->log("netherite zombie alive="+mob.isAlive()+" hp="+mob.getHealth()));
  if(tick==185)log("DONE");
 }
}
