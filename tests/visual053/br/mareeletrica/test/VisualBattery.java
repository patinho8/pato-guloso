package br.mareeletrica.test;

import br.mareeletrica.Barrage;
import br.mareeletrica.Batteries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.battery): the 3 new auto guns + the barrage launcher fire, and the terrain must stay intact. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualBattery {
 static int tick,blocksBefore;
 static boolean on(){return Boolean.getBoolean("mareeletrica.battery");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"battery_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[BAT] "+c.getString()));}
 static void look(Minecraft mc,float y,float x){mc.player.setYRot(y);mc.player.setXRot(x);mc.player.yRotO=y;mc.player.xRotO=x;mc.player.yHeadRot=y;}
 static void log(String s){System.out.println("[BAT] "+s);}
 static int count(net.minecraft.server.level.ServerLevel l){int n=0;for(int x=OX-60;x<=OX+60;x++)for(int z=OZ-80;z<=OZ+30;z++)for(int y=-64;y<=-58;y++)if(!l.getBlockState(new BlockPos(x,y,z)).isAir())n++;return n;}
 static final int OX=3000,OZ=3000;static BlockPos SENTRY=new BlockPos(OX-5,-60,OZ),FLAK=new BlockPos(OX+5,-60,OZ),GREN=new BlockPos(OX-1,-60,OZ+2),BAR=new BlockPos(OX+2,-60,OZ+3);
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;if(mc.screen!=null&&tick>20&&tick<545)mc.setScreen(null);mc.options.hideGui=tick>45&&tick<545;
  if(tick>=50&&tick<400){if(mc.player.distanceToSqr(OX+.5,-57.5,OZ+8)>4)mc.player.setPos(OX+.5,-57.5,OZ+8);look(mc,180,tick<220?30:14);}var srv=mc.getSingleplayerServer();
  if(tick==40)srv.execute(()->{var l=srv.overworld();l.setDayTime(13500);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,srv);var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(l,OX+.5,-57.5,OZ+8,180,30);
   l.setBlockAndUpdate(SENTRY,Batteries.SENTRY.get().defaultBlockState());l.setBlockAndUpdate(FLAK,Batteries.FLAK.get().defaultBlockState());l.setBlockAndUpdate(GREN,Batteries.GRENADE.get().defaultBlockState());l.setBlockAndUpdate(BAR,Barrage.BLOCK.get().defaultBlockState());
   for(var b:new BlockPos[]{SENTRY,FLAK,GREN})if(l.getBlockEntity(b) instanceof Batteries.Gun g){g.ammo=1000;g.owner=p.getUUID();g.sync();}
   if(l.getBlockEntity(BAR) instanceof Barrage.LauncherEntity bl){bl.ammo=1000;bl.sync();}
   blocksBefore=count(l);log("blocks before "+blocksBefore);
   // crate test: 20 stacks of 64 rockets -> caps at 1000; survives save
   var crate=new ItemStack(Batteries.CRATE.get());int in=0;for(int i=0;i<20;i++){var r=new ItemStack(Barrage.ROCKET.get(),64);in+=Batteries.AmmoCrate.insert(crate,r);}
   var tag=crate.save(l.registryAccess());var back=ItemStack.parseOptional(l.registryAccess(),(net.minecraft.nbt.CompoundTag)tag);
   log("crate took "+in+" count "+Batteries.AmmoCrate.count(back)+" type "+Batteries.AmmoCrate.type(back)+" rocketMax "+new ItemStack(Barrage.ROCKET.get()).getMaxStackSize());
   var bad=new ItemStack(Batteries.FLAK_SHELL.get(),10);log("mixed insert (should be 0): "+Batteries.AmmoCrate.insert(back,bad));
  });
  if(tick==230)srv.execute(()->{var l=srv.overworld();
   for(int i=0;i<6;i++){var z=EntityType.ZOMBIE.create(l);z.moveTo(OX-8+i*3,-60,OZ-22-(i%3)*4,0,0);z.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.LEATHER_HELMET));l.addFreshEntity(z);}
   for(int i=0;i<3;i++){var s=EntityType.SKELETON.create(l);s.moveTo(OX-14+i*4,-60,OZ-34,0,0);l.addFreshEntity(s);}
   for(int i=0;i<2;i++){var ph=EntityType.PHANTOM.create(l);ph.moveTo(OX-10+i*20,-30,OZ-30,0,0);l.addFreshEntity(ph);}
   if(l.getBlockEntity(BAR) instanceof Barrage.LauncherEntity bl){bl.aimAt(new Vec3(OX,-60,OZ-55));bl.fire();}
  });
  if(tick==215)shot(mc,"perto");if(tick==262)shot(mc,"fogo1");
  if(tick==300)shot(mc,"fogo2");
  
  if(tick==340)shot(mc,"fogo3");
  if(tick==320)srv.execute(()->{var l=srv.overworld();if(l.getBlockEntity(BAR) instanceof Barrage.LauncherEntity bl){bl.aimAt(new Vec3(OX-20,-60,OZ-40));bl.fire();}});
  if(tick==560)srv.execute(()->{var l=srv.overworld();int after=count(l);
   int alive=l.getEntitiesOfClass(Monster.class,new AABB(OX,-60,OZ,OX,-60,OZ).inflate(120)).size();
   StringBuilder s=new StringBuilder();for(var b:new BlockPos[]{SENTRY,FLAK,GREN})if(l.getBlockEntity(b) instanceof Batteries.Gun g)s.append(g.kind()).append('=').append(g.ammo).append(' ');
   if(l.getBlockEntity(BAR) instanceof Barrage.LauncherEntity bl)s.append("BARRAGE=").append(bl.ammo);
   log("blocks after "+after+" (diff "+(after-blocksBefore)+") monsters alive "+alive+" ammo "+s);
   l.setBlockAndUpdate(SENTRY,Blocks.AIR.defaultBlockState());
   log("crates dropped on break: "+l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(SENTRY).inflate(2)).stream().map(i->i.getItem().getItem()+"x"+Batteries.AmmoCrate.count(i.getItem())).toList());
  });
  if(tick==540)srv.execute(()->srv.overworld().setBlockAndUpdate(new BlockPos(OX,-60,OZ+6),br.mareeletrica.BaseControl.PANEL.get().defaultBlockState()));
  if(tick==548){var d=new net.minecraft.nbt.CompoundTag();d.putLong("Stored",420000);d.putLong("Max",1000000);d.putInt("Gen",1200);d.putInt("Use",300);d.putInt("Machines",7);mc.setScreen(new br.mareeletrica.client.PanelScreen(new BlockPos(OX,-60,OZ+6),d));}
  if(tick==556)shot(mc,"painel");
  if(tick==570){log("DONE");mc.stop();}
 }
}
