package br.mareeletrica.test;

import br.mareeletrica.*;
import br.mareeletrica.client.RifleRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.acc): the 0.74.0 M4A1 and the gun accessories. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualAcc {
 static int tick;static BlockPos o;
 static ItemStack gun(WeaponFamily f,int skin,int under,int side,int mag){var s=new ItemStack(f.item());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putBoolean("ArmoryFormat",true);t.putInt("Sticker",skin);t.putInt("AccUnder",under);t.putInt("AccSide",side);t.putInt("AccMag",mag);t.putInt("Rounds",30);});return s;}
 static int id(Accessory.Kind k){return k==null?0:k.ordinal()+1;}
 /** everything that fits, for the gallery */
 static ItemStack full(WeaponFamily f,int v){var K=Accessory.Kind.values();int u=0,sd=0,m=0;
  Accessory.Kind[] unders={Accessory.Kind.VERTICAL_GRIP,Accessory.Kind.ANGLED_GRIP,Accessory.Kind.BIPOD},sides={Accessory.Kind.COMBO,Accessory.Kind.LASER_GREEN,Accessory.Kind.FLASHLIGHT};
  if(Accessory.fit(f,unders[v%3])==Accessory.UNDER)u=id(unders[v%3]);else if(Accessory.fit(f,Accessory.Kind.VERTICAL_GRIP)==Accessory.UNDER)u=id(Accessory.Kind.VERTICAL_GRIP);
  if(Accessory.fit(f,sides[v%3])==Accessory.SIDE)sd=id(sides[v%3]);else if(Accessory.fit(f,Accessory.Kind.COMBO)==Accessory.UNDER&&u==0)u=id(Accessory.Kind.COMBO);
  if(Accessory.fit(f,Accessory.Kind.DRUM_MAG)==Accessory.MAG&&v%2==0)m=id(Accessory.Kind.DRUM_MAG);else if(Accessory.fit(f,Accessory.Kind.EXTENDED_MAG)==Accessory.MAG)m=id(Accessory.Kind.EXTENDED_MAG);
  return gun(f,0,u,sd,m);}
 public static final class G extends Screen {
  final int mode;G(int m){super(Component.literal("a"));mode=m;}
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){g.fill(0,0,width,height,0xFF2A2F38);}
  void draw(GuiGraphics g,ItemStack s,float cx,float cy,float sc,float yaw,float pitch,boolean sup){
   var pose=g.pose();pose.pushPose();pose.translate(cx,cy,200);pose.scale(sc,-sc,sc);pose.mulPose(Axis.XP.rotationDegrees(pitch));pose.mulPose(Axis.YP.rotationDegrees(yaw));pose.translate(-.5,-.42,-.25);
   Lighting.setupForEntityInInventory();RifleRenderer.parts(s,pose,g.bufferSource(),15728880,sup);g.flush();Lighting.setupFor3DItems();pose.popPose();}
  @Override public void render(GuiGraphics g,int mx,int my,float pt){
   renderBackground(g,mx,my,pt);var M=WeaponFamily.M4A1;
   if(mode==0){draw(g,gun(M,0,0,0,0),width*.25F,height*.3F,95,90,0,false);draw(g,gun(M,0,id(Accessory.Kind.VERTICAL_GRIP),id(Accessory.Kind.COMBO),id(Accessory.Kind.DRUM_MAG)),width*.75F,height*.3F,95,-90,0,false);draw(g,gun(M,0,id(Accessory.Kind.VERTICAL_GRIP),id(Accessory.Kind.COMBO),id(Accessory.Kind.DRUM_MAG)),width*.5F,height*.75F,110,-130,18,false);}
   else if(mode==1){draw(g,gun(M,0,id(Accessory.Kind.ANGLED_GRIP),id(Accessory.Kind.LASER_GREEN),id(Accessory.Kind.EXTENDED_MAG)),width*.25F,height*.3F,95,-90,0,false);draw(g,gun(M,0,id(Accessory.Kind.BIPOD),id(Accessory.Kind.FLASHLIGHT),0),width*.75F,height*.3F,95,-90,0,false);draw(g,gun(M,0,id(Accessory.Kind.BIPOD),0,0),width*.25F,height*.75F,95,-90,0,true);draw(g,gun(M,0,id(Accessory.Kind.ANGLED_GRIP),id(Accessory.Kind.LASER_GREEN),id(Accessory.Kind.EXTENDED_MAG)),width*.75F,height*.75F,100,-150,25,false);}
   else if(mode==2){for(int sk=0;sk<=12;sk++){int c=sk%5,r=sk/5;draw(g,gun(M,sk,0,0,0),width*(.1F+c*.2F),height*(.2F+r*.31F),42,90,0,false);g.drawString(font,""+sk,(int)(width*(.02F+c*.2F)),(int)(height*(.05F+r*.31F)),0xFFFFFF80);}}
   else if(mode>=10){var fs=WeaponFamily.values();for(int c=0;c<4;c++){int n=(mode-10)*4+c;if(n>=fs.length)break;var f=fs[n];float sc=f.pistol()?110:70;
     draw(g,gun(f,3,0,0,0),width*(.25F+(c%2)*.5F),height*(.27F+(c/2)*.5F),sc,c%2==0?-60:-120,-12,false);g.drawString(font,f.id,(int)(width*(.02F+(c%2)*.5F)),(int)(height*(.03F+(c/2)*.5F)),0xFFFFFF80);}}
   else{int i=0;var fs=WeaponFamily.values();int from=(mode-3)*12;for(int n=from;n<Math.min(fs.length,from+12);n++){var f=fs[n];if(f==WeaponFamily.SNIPER)continue;int c=i%4,r=i/4;float sc=f.pistol()?62:50;draw(g,full(f,n),width*(.13F+c*.25F),height*(.2F+r*.32F),sc,-90,0,false);g.drawString(font,f.id,(int)(width*(.02F+c*.25F)),(int)(height*(.04F+r*.32F)),0xFFFFFF80);i++;}}
  }
  @Override public boolean isPauseScreen(){return false;}
 }
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"acc_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(Boolean.getBoolean("mareeletrica.acc")&&e.getEntity() instanceof net.minecraft.world.entity.player.Player)System.out.println("[ACC] death "+e.getSource().getMsgId()+" "+e.getSource().getEntity()+" tick "+tick);}
 static void hold(ItemStack s){srv(()->sp().setItemInHand(InteractionHand.MAIN_HAND,s));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.acc"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  var M=WeaponFamily.M4A1;
  if(tick==50&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.setInvulnerable(true);o=p.blockPosition().atY(200);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
   for(int x=-8;x<=8;x++)for(int z=-30;z<=8;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.STONE_BRICKS.defaultBlockState());for(int y=0;y<6;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(o.offset(x,6,z),Blocks.STONE_BRICKS.defaultBlockState());}
   for(int x=-8;x<=8;x++)for(int y=0;y<6;y++)l.setBlockAndUpdate(o.offset(x,y,-16),Blocks.STONE_BRICKS.defaultBlockState());
   for(int y=0;y<3;y++)l.setBlockAndUpdate(o.offset(0,y,-12),Blocks.TARGET.defaultBlockState());
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+.5,180,3);});
  if(Boolean.getBoolean("mareeletrica.accmag")&&tick>=70){int i=(tick-70)/12,k=(tick-70)%12;if(i>=7){if(k==0){mc.setScreen(null);System.out.println("[ACC] DONE");}return;}if(k==0)mc.setScreen(new G(10+i));if(k==10)shot("mag"+i);return;}
  if(Boolean.getBoolean("mareeletrica.accguns")&&tick>=70){var fs=WeaponFamily.values();int i=(tick-70)/44,k=(tick-70)%44;
   if(i>=fs.length){if(k==0){mc.options.keyUse.setDown(false);System.out.println("[ACC] DONE");}return;}
   var f=fs[i];if(k==0){mc.options.keyUse.setDown(false);hold(gun(f,3,0,0,0));}if(k==18)shot("fp_"+f.id);if(k==20)mc.options.keyUse.setDown(true);if(k==40)shot("ads_"+f.id);return;}
  int t0=Boolean.getBoolean("mareeletrica.accfast")?-1000:70;String[] names={"m4","m4b","m4skins","todas1","todas2","todas3"};
  for(int i=0;i<6;i++){if(tick==t0+i*12)mc.setScreen(new G(i));if(tick==t0+i*12+10)shot(names[i]);}
  int t=t0<0?70:t0+6*12;
  if(tick==t){mc.setScreen(null);hold(gun(M,0,id(Accessory.Kind.VERTICAL_GRIP),id(Accessory.Kind.LASER_RED),id(Accessory.Kind.DRUM_MAG)));}
  if(tick==t+16)shot("mao");
  if(tick==t+18)mc.options.keyUse.setDown(true);
  if(tick==t+36)shot("mira");
  if(tick==t+38){mc.options.keyUse.setDown(false);srv(()->sp().serverLevel().setDayTime(18000));hold(gun(M,3,id(Accessory.Kind.ANGLED_GRIP),id(Accessory.Kind.COMBO),id(Accessory.Kind.EXTENDED_MAG)));}
  if(tick==t+60)shot("noite_lanterna");
  if(tick==t+62)hold(gun(WeaponFamily.GLOCK,0,id(Accessory.Kind.COMBO),0,id(Accessory.Kind.DRUM_MAG)));
  if(tick==t+80)shot("noite_glock");
  if(tick==t+82){hold(gun(M,0,id(Accessory.Kind.VERTICAL_GRIP),id(Accessory.Kind.LASER_GREEN),0));mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);srv(()->{var l=sp().serverLevel();l.setDayTime(6000);for(var mob:l.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,sp().getBoundingBox().inflate(60)))mob.discard();});}
  if(tick==t+100)shot("terceira");
  if(tick==t+102){mc.options.setCameraType(CameraType.FIRST_PERSON);srv(()->{var p=sp();var l=p.serverLevel();l.setDayTime(6000);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
    l.setBlockAndUpdate(o.offset(3,2,-15),Shower.SHOWER.get().defaultBlockState().setValue(Shower.ShowerBlock.FACING,net.minecraft.core.Direction.SOUTH).setValue(Shower.ShowerBlock.ON,true));
    l.setBlockAndUpdate(o.offset(-2,2,-15),Shower.SHOWER.get().defaultBlockState().setValue(Shower.ShowerBlock.FACING,net.minecraft.core.Direction.SOUTH));
    p.teleportTo(l,o.getX()-1.5,o.getY()+.5,o.getZ()-13.2,180,-25);});}
  if(tick==t+130)shot("chuveiro");
  if(tick==t+132)srv(()->{var p=sp();p.teleportTo(p.serverLevel(),o.getX()+3.5,o.getY(),o.getZ()-14.4,0,0);p.setRemainingFireTicks(200);});
  if(tick==t+134)mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
  if(tick==t+156){shot("banho");System.out.println("[ACC] fire after shower "+sp().getRemainingFireTicks());}
  if(tick==t+158){mc.options.setCameraType(CameraType.FIRST_PERSON);System.out.println("[ACC] DONE");}
 }
}
