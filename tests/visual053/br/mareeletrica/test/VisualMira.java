package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.mira): guns in first person, at the hip and aiming through their optics. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualMira {
 static int tick;static BlockPos o;
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"mira_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static ItemStack gun(WeaponFamily f,int variant,int sup){var s=new ItemStack(f.item());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putBoolean("ArmoryFormat",true);if(variant>0){t.putBoolean("HasOptic",true);t.putInt("OpticVariant",variant);t.putInt("ArsenalZoom",f.zooms[variant-1]);}t.putInt("Suppressor",sup);t.putInt("Rounds",f.capacity);});return s;}
 static void hold(ItemStack s){srv(()->sp().setItemInHand(InteractionHand.MAIN_HAND,s));}
 static final Object[][] SEQ={{"m4_dot",WeaponFamily.M4A1,1,0},{"mp5_holo",WeaponFamily.MP5,2,1},{"famas_prism",WeaponFamily.FAMAS,2,0}};
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.mira"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);o=p.blockPosition().atY(150);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
   for(int x=-8;x<=8;x++)for(int z=-30;z<=8;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<6;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   for(int y=0;y<4;y++)l.setBlockAndUpdate(o.offset(0,y,-20),Blocks.TARGET.defaultBlockState());
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+.5,180,0);});
  int k=tick-80;if(k<0)return;int i=k/40,t=k%40;
  if(i<SEQ.length){var q=SEQ[i];
   if(t==0){mc.options.keyUse.setDown(false);hold(gun((WeaponFamily)q[1],(int)q[2],(int)q[3]));srv(()->{var p=sp();p.teleportTo(p.serverLevel(),o.getX()+.5,o.getY(),o.getZ()+.5,180,3);});}
   if(t==12)shot(q[0]+"_quadril");
   if(t==14)mc.options.keyUse.setDown(true);
   if(t==34)shot(q[0]+"_mira");
   if(t==36)mc.options.keyUse.setDown(false);}
  else if(i==SEQ.length){
   if(t==0)hold(new ItemStack(MareEletrica.SNIPER.get()));if(t==12)shot("sniper_claude");
   if(t==20)hold(new ItemStack(MareEletrica.HUNTER.get()));if(t==32)shot("sniper_cacador");}
  else if(i==SEQ.length+1){
   if(t==0)srv(()->{var p=sp();var l=p.serverLevel();l.setDayTime(18000);var r=o.offset(0,0,-40);
    for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){l.setBlockAndUpdate(r.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());l.setBlockAndUpdate(r.offset(x,5,z),Blocks.SMOOTH_STONE.defaultBlockState());
     for(int y=0;y<5;y++)l.setBlockAndUpdate(r.offset(x,y,z),(Math.abs(x)==6||Math.abs(z)==6)?Blocks.STONE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState());}
    int[][] at={{-3,4,-3},{-2,4,-3},{-1,4,-3},{0,4,-3},{1,4,-3},{-5,0,-5},{5,0,-5},{0,0,-5},{-5,0,2},{5,1,2}};
    for(int j=0;j<at.length;j++)l.setBlockAndUpdate(r.offset(at[j][0],at[j][1],at[j][2]),Lamps.BLOCKS.get((j*3+1)%Lamps.BLOCKS.size()).get().defaultBlockState());
    p.teleportTo(l,r.getX()+.5,r.getY(),r.getZ()+5.0,180,-5);});
   if(t==30)shot("led");}
  else if(i==SEQ.length+2&&t==0)System.out.println("[MIRA] DONE");
 }
}
