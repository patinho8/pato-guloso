package br.mareeletrica.test;

import br.mareeletrica.*;
import br.mareeletrica.client.PaintScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.tinta): painted guns in hand and in the Paint Bench screen. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualTinta {
 static int tick;static BlockPos o;
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"tinta_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static ItemStack gun(WeaponFamily f){for(var it:BuiltInRegistries.ITEM){var s=new ItemStack(it);if(WeaponFamily.of(s)==f&&!s.is(MareEletrica.HUNTER.get()))return s;}return ItemStack.EMPTY;}
 static CompoundTag r(int c,int f,int p,int d){var t=new CompoundTag();t.putInt("C",c);t.putInt("F",f);t.putInt("P",p);t.putInt("D",d);return t;}
 static void hold(ItemStack s,CompoundTag paint){srv(()->{GunPaint.set(s,paint);sp().setItemInHand(InteractionHand.MAIN_HAND,s);});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.tinta"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(mc.screen!=null&&tick>20&&tick<100&&!(mc.screen instanceof PaintScreen))mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);o=p.blockPosition().atY(150);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
   for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   l.setBlockAndUpdate(o.offset(0,0,-2),GunPaint.BENCH.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,net.minecraft.core.Direction.SOUTH));
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+.5,180,20);});
  if(tick==70){var t=new CompoundTag();t.put("r0",r(0xC02020,1,0,0));t.put("r1",r(0x202020,0,0,0));t.put("r2",r(0x5A7A30,0,3,0x3A2A18));t.put("r4",r(0xFFD020,2,0,0));t.putInt("S",2);hold(gun(WeaponFamily.AK47),t);}
  if(tick==90)shot("ak_mao");
  if(tick==92)PaintScreen.open(o.offset(0,0,-2));
  if(tick==100)shot("tela_ak");
  if(tick==102){mc.setScreen(null);var t=new CompoundTag();t.put("r0",r(0x3060E0,0,2,0xFFFFFF));t.put("r1",r(0xFFFFFF,1,0,0));t.put("r3",r(0x30C0FF,2,0,0));t.put("r4",r(0x202020,0,1,0xFF3030));t.putInt("S",4);hold(gun(WeaponFamily.GLOCK),t);}
  if(tick==118)shot("glock_mao");
  if(tick==120)PaintScreen.open(o.offset(0,0,-2));
  if(tick==128)shot("tela_glock");
  if(tick==130){mc.setScreen(null);var t=new CompoundTag();t.put("r0",r(0xFF00FF,3,1,0));t.put("r2",r(0xFF6EC8,0,1,0xFFFFFF));t.put("r5",r(0x101010,1,0,0));t.putInt("S",1);hold(new ItemStack(MareEletrica.HUNTER.get()),t);}
  if(tick==148)PaintScreen.open(o.offset(0,0,-2));
  if(tick==158)shot("tela_hunter");
  if(tick==160){mc.setScreen(null);var t=new CompoundTag();t.put("r0",r(0xE8E8E8,0,3,0x4A5A3A));t.put("r1",r(0x101010,1,0,0));t.put("r2",r(0x7A5230,0,1,0x2A1A10));t.putInt("S",5);hold(gun(WeaponFamily.M4A1),t);}
  if(tick==170)PaintScreen.open(o.offset(0,0,-2));
  if(tick==178)shot("tela_m4");
  if(tick==180){mc.setScreen(null);var t=new CompoundTag();t.put("r0",r(0x20B040,0,0,0));t.put("r5",r(0xFFFFFF,2,0,0));t.put("r2",r(0x20B040,0,2,0x101010));hold(new ItemStack(MareEletrica.SNIPER.get()),t);}
  if(tick==190)PaintScreen.open(o.offset(0,0,-2));
  if(tick==198)shot("tela_sniper");
  if(tick==200){mc.setScreen(null);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
  if(tick==212)shot("terceira");
  if(tick==214){mc.options.setCameraType(CameraType.FIRST_PERSON);var t=new CompoundTag();hold(gun(WeaponFamily.AK47),t);}
  if(tick==222)PaintScreen.open(o.offset(0,0,-2));
  if(tick==230)shot("tela_limpa");
  if(tick==240){mc.setScreen(null);System.out.println("[TINTA] DONE");}
 }
}
