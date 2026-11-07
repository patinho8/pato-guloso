package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.fun): entertainment blocks, sniper limb rule, self-healing, jets vs explosions. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualFun {
 static int tick;static BlockPos A,P,S;static Jets.JetEntity jet;static net.minecraft.world.entity.monster.Zombie z;
 static boolean on(){return Boolean.getBoolean("mareeletrica.fun");}
 static void log(String s){System.out.println("[FUN] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shoot(net.minecraft.world.item.Item gun,double lat,double h){var p=sp();var l=p.serverLevel();z.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(gun));p.invulnerableTime=0;
  Vec3 target=p.position().add(lat,h,0);Vec3 from=target.add(0,0,-1.2);var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),l);b.setOwner(z);b.setPos(from);Vec3 d=target.subtract(from).normalize();b.shoot(d.x,d.y,d.z,4F,0F);l.addFreshEntity(b);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20&&!(mc.screen instanceof br.mareeletrica.client.ArcadeScreen))mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick>=60&&tick<200){mc.player.setYRot(180);mc.player.setXRot(20);}
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);var b=p.blockPosition();
   A=b.offset(-2,0,-3);P=b.offset(0,0,-3);S=b.offset(2,0,-3);
   l.setBlockAndUpdate(A,Fun.ARCADE.get().defaultBlockState());l.setBlockAndUpdate(P,Fun.POPCORN_MACHINE.get().defaultBlockState());l.setBlockAndUpdate(S,Fun.FIREWORKS.get().defaultBlockState());
   if(l.getBlockEntity(P) instanceof Fun.PopcornEntity pe){pe.cooking=100;}
   if(l.getBlockEntity(S) instanceof Fun.ShowEntity se)log("show started="+(se.getClass()!=null));});
  if(tick==62)srv.execute(()->{var l=sp().serverLevel();l.getBlockState(S).useWithoutItem(l,sp(),new net.minecraft.world.phys.BlockHitResult(S.getCenter(),net.minecraft.core.Direction.UP,S,false));});
  if(tick==100)srv.execute(()->{var inv=sp().getInventory();var items=new net.minecraft.world.item.Item[]{Wounds.BANDAGE.get(),Wounds.SYRINGE.get(),Wounds.MEDKIT.get(),Fun.ARCADE_ITEM.get(),Fun.POPCORN_MACHINE_ITEM.get(),Fun.FIREWORKS_ITEM.get(),Fun.POPCORN.get(),TennisBall.COLORED.get(0).get(),TennisBall.COLORED.get(2).get()};
   for(int i=0;i<9;i++)inv.setItem(i,new ItemStack(items[i]));});
  if(tick==120)Screenshot.grab(mc.gameDirectory,"fun_blocks.png",mc.getMainRenderTarget(),c->{});
  if(tick==170)srv.execute(()->{var l=sp().serverLevel();
   int inv=0;for(int i=0;i<36;i++)if(sp().getInventory().getItem(i).is(Fun.POPCORN.get()))inv+=sp().getInventory().getItem(i).getCount();log("popcorn picked up by the player: "+inv);
   var pop=l.getEntitiesOfClass(ItemEntity.class,new AABB(P).inflate(3),x->x.getItem().is(Fun.POPCORN.get())).stream().mapToInt(x->x.getItem().getCount()).sum();
   log("popcorn out: "+pop+" | rockets in the air: "+l.getEntitiesOfClass(FireworkRocketEntity.class,new AABB(S).inflate(80)).size());});
  if(tick==175)br.mareeletrica.client.ArcadeScreen.open(A);
  if(tick>176&&tick<200&&mc.screen instanceof br.mareeletrica.client.ArcadeScreen a){if(tick==177)a.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE,0,0);}
  if(tick==198)Screenshot.grab(mc.gameDirectory,"fun_arcade.png",mc.getMainRenderTarget(),c->{});
  if(tick==200)mc.setScreen(null);
  // sniper rule: a leg shot never kills, a chest shot does
  if(tick==210)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setYRot(180);p.yBodyRot=180;
   z=EntityType.ZOMBIE.create(l);z.moveTo(p.getX(),p.getY()-6,p.getZ()-6,0,0);z.setNoAi(true);z.setInvisible(true);z.setInvulnerable(true);l.addFreshEntity(z);p.setHealth(20);});
  if(tick>=205&&tick<345){mc.player.setYRot(180);mc.player.yBodyRot=180;mc.player.setXRot(5);}
  if(tick>=205&&tick<345&&tick%2==0)srv.execute(()->{var p=sp();p.setYRot(180);p.setYBodyRot(180);p.yBodyRot=180;});
  if(tick==215)srv.execute(()->shoot(MareEletrica.SNIPER.get(),.12,.45));
  if(tick==222)srv.execute(()->{var s=Wounds.st(sp());log("sniper leg shot at FULL hp: alive="+sp().isAlive()+" (bodyYaw "+sp().yBodyRot+")"+" alive2="+sp().isAlive()+" hp="+sp().getHealth()+" RL="+s.limb[3]+" broken="+s.broken(3));});
  if(tick==229&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==223)srv.execute(()->sp().setHealth(1));
  if(tick==224)srv.execute(()->shoot(MareEletrica.SNIPER.get(),-.12,.45));
  if(tick==227)srv.execute(()->log("sniper leg shot at half a heart: dead="+sp().isDeadOrDying()));
  if(tick==226)srv.execute(()->{var s=Wounds.st(sp());s.limb[3]=Wounds.LIMB_HP;Wounds.save(sp(),s,true);});
  if(tick>226&&tick<330&&tick%10==0)srv.execute(()->{var s=Wounds.st(sp());log("t"+tick+" RL="+s.limb[3]+" ptick="+sp().tickCount);});
  if(tick==330)srv.execute(()->{var s=Wounds.st(sp());log("self-heal: RL after ~100 ticks = "+s.limb[3]+" broken="+s.broken(3));});
  if(tick==335)srv.execute(()->{sp().setHealth(20);shoot(MareEletrica.SNIPER.get(),0,1.2);});
  if(tick==342)srv.execute(()->log("sniper chest shot at 20 hp: dead="+sp().isDeadOrDying()));
  // jets: explosions now bring them down
  if(tick==350)srv.execute(()->{var l=sp().serverLevel();jet=Jets.JET.get().create(l);jet.moveTo(sp().getX()+30,sp().getY()+60,sp().getZ(),0,0);l.addFreshEntity(jet);
   for(int i=0;i<4&&!jet.downed();i++){jet.invulnerableTime=0;jet.hurt(l.damageSources().explosion(null,null),30F);}
   log("jet after explosions: downed="+jet.downed()+" alive="+jet.isAlive());});
  if(tick==360){log("DONE");mc.stop();}
 }
}
