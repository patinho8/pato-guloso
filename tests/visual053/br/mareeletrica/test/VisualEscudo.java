package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
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

/** Test only (-Dmareeletrica.escudo): bullets go through a normal shield; the tactical shield stops them and only allows pistols. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualEscudo {
 static int tick;static Zombie z;static Vec3 base;
 static boolean on(){return Boolean.getBoolean("mareeletrica.escudo");}
 static void log(String s){System.out.println("[ESC] "+s);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"esc_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void dmg(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e){if(on()&&tick>60&&e.getEntity() instanceof net.minecraft.world.entity.player.Player)log("player took "+e.getNewDamage());}
 static void shoot(double lat,double h){var p=sp();var l=p.serverLevel();z.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Arsenal.AK47.get()));Vec3 tg=p.position().add(lat,h,0);Vec3 from=tg.add(0,0,-1.2);
  p.invulnerableTime=0;p.setHealth(20);var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),l);b.setOwner(z);b.setPos(from);Vec3 d=tg.subtract(from).normalize();b.shoot(d.x,d.y,d.z,4F,0F);l.addFreshEntity(b);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(false);var o=p.blockPosition().atY(200);
   for(int x=-6;x<=6;x++)for(int zz=-8;zz<=8;zz++){l.setBlockAndUpdate(o.offset(x,-1,zz),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(o.offset(x,y,zz),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+.5,180,0);base=p.position();for(var s:EquipmentSlot.values())if(s.isArmor())p.setItemSlot(s,ItemStack.EMPTY);Wounds.st(p).limb[0]=Wounds.st(p).limb[1]=Wounds.st(p).limb[2]=Wounds.st(p).limb[3]=0;
   z=EntityType.ZOMBIE.create(l);z.moveTo(p.getX(),p.getY()-5,p.getZ()-6,0,0);z.setNoAi(true);z.setInvisible(true);z.setInvulnerable(true);l.addFreshEntity(z);
   p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));});
  if(tick>60&&tick<186&&tick%3==0)srv.execute(()->{var p=sp();if(base!=null&&p.position().distanceToSqr(base)>.01)p.teleportTo(base.x,base.y,base.z);p.setYRot(180);p.setYBodyRot(180);p.setYHeadRot(180);});
  if(tick>60&&tick<186){mc.player.setYRot(180);mc.player.setXRot(5);}
  if(tick>=70&&tick<92)mc.options.keyUse.setDown(true);if(tick==92)mc.options.keyUse.setDown(false);
  if(tick==84)srv.execute(()->{log("normal shield raised: blocking="+sp().isBlocking());shoot(0,1.2);});
  if(tick==95)srv.execute(()->{var p=sp();p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Wounds.TAC_SHIELD.get()));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);log("--- tactical shield (not raised)");});
  if(tick==100)srv.execute(()->shoot(0,1.2));
  if(tick==110)srv.execute(()->shoot(0,1.65));
  if(tick==120)srv.execute(()->shoot(.12,.45));
  if(tick==130)srv.execute(()->{var p=sp();log("shield dmg "+p.getOffhandItem().getDamageValue()+" | USP blocked? "+Wounds.blocked(p,new ItemStack(Arsenal.USP.get()))+" | AK blocked? "+Wounds.blocked(p,new ItemStack(Arsenal.AK47.get()))+" | Deagle? "+Wounds.blocked(p,new ItemStack(MareEletrica.REVOLVER.get())));});
  if(tick==140)shot("primeira_pessoa");
  if(tick>=145&&tick<168)mc.options.keyUse.setDown(true);if(tick==168)mc.options.keyUse.setDown(false);
  if(tick==155){shot("levantado");srv.execute(()->{log("tactical raised blocking="+sp().isBlocking());shoot(0,1.2);});}
  if(tick==170){srv.execute(()->sp().stopUsingItem());mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
  if(tick==178){mc.options.hideGui=true;}if(tick==185)shot("terceira");if(tick==186)srv.execute(()->{var p=sp();p.setYRot(0);p.setYBodyRot(0);});if(tick>186&&tick<200){mc.player.setYRot(0);}if(tick==196)shot("costas");
  if(tick==200){mc.options.hideGui=false;mc.options.setCameraType(CameraType.FIRST_PERSON);srv.execute(()->{var l=sp().serverLevel();var t=net.minecraft.world.entity.EntityType.TNT.create(l);t.setPos(base.x+3,base.y,base.z-4);t.setFuse(5);l.addFreshEntity(t);});}
  if(tick==232)srv.execute(()->{var p=sp();var l=p.serverLevel();l.setDayTime(6000);var o=p.blockPosition().above(30);
   for(int x=-4;x<=4;x++)for(int zz=-4;zz<=4;zz++)l.setBlockAndUpdate(o.offset(x,-1,zz),net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState());
   for(int i=0;i<2;i++){var a=net.minecraft.world.entity.EntityType.ARMOR_STAND.create(l);a.moveTo(o.getX()+.5+(i==0?-1:1),o.getY(),o.getZ()-2+.5,0,0);a.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(i==0?Wounds.TAC_SHIELD.get():Items.SHIELD));
    var t=new net.minecraft.nbt.CompoundTag();a.saveWithoutId(t);t.putBoolean("ShowArms",true);a.load(t);l.addFreshEntity(a);}
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+1.5,180,15);});
  if(tick>232&&tick<260){mc.player.setYRot(180);mc.player.setXRot(15);}
  if(tick==255){mc.options.hideGui=true;shot("vitrine");}
  if(tick==258)mc.options.hideGui=false;
  if(tick==262){log("DONE");}
 }
}
