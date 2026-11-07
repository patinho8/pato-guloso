package br.mareeletrica.client;

import br.mareeletrica.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/** "Inspect" (V): every weapon and every knife has its own choreography. Keyframes: t, dx, dy, dz (blocks), rx, ry, rz (degrees). */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class InspectClient {
 public static final KeyMapping KEY=new KeyMapping("key.mareeletrica.inspect",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,"key.categories.mareeletrica");
 private static float start=-1;private static int slot=-1,ticks=70;private static boolean knife;

 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static final class Keys{@SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(KEY);}}

 private record Choreo(int ticks,float[][] keys){}
 private static float[] k(float t,float dx,float dy,float dz,float rx,float ry,float rz){return new float[]{t,dx,dy,dz,rx,ry,rz};}
 private static final float[] Z={0,0,0,0,0,0,0};
 private static final Map<String,Choreo> GUNS=Map.ofEntries(
  // ---- pistols: allowed to show off ----
  // Desert Eagle: tilts the slide to the light, tosses it up and flips it end over end, catches it
  Map.entry("DESERT_EAGLE",new Choreo(92,new float[][]{Z,k(.14F,-.04F,.03F,.05F,-8,18,0),k(.30F,-.05F,.04F,.06F,-6,38,24),k(.46F,-.04F,.05F,.06F,-4,10,10),k(.62F,-.03F,.12F,.04F,-180,0,0),k(.80F,-.02F,.08F,.04F,-360,0,0),k(.92F,-.01F,.02F,.03F,-360,0,0),k(1,0,0,0,-360,0,0)})),
  // USP: looks down the slide, then twirls the pistol around the barrel
  Map.entry("USP",new Choreo(80,new float[][]{Z,k(.14F,-.03F,.04F,.05F,-26,0,0),k(.32F,-.04F,.05F,.06F,-26,-22,0),k(.46F,-.03F,.05F,.05F,-6,0,0),k(.74F,-.02F,.07F,.04F,0,0,-360),k(.90F,-.01F,.02F,.02F,0,0,-360),k(1,0,0,0,0,0,-360)})),
  // Python: gunslinger spin, two full turns, then a quick look at the cylinder
  Map.entry("PYTHON",new Choreo(100,new float[][]{Z,k(.12F,-.03F,.04F,.05F,-10,0,0),k(.26F,-.03F,.10F,.05F,-10,0,0),k(.62F,-.03F,.14F,.05F,-730,0,0),k(.78F,-.03F,.06F,.05F,-720,24,12),k(.92F,-.01F,.02F,.03F,-720,0,0),k(1,0,0,0,-720,0,0)})),
  // Glock: taps the slide, then a quick flip around the barrel and back
  Map.entry("GLOCK",new Choreo(74,new float[][]{Z,k(.14F,-.03F,.04F,.05F,-18,14,0),k(.30F,-.03F,.05F,.05F,-18,-14,0),k(.62F,-.02F,.06F,.04F,0,0,360),k(.84F,-.01F,.02F,.02F,0,0,360),k(1,0,0,0,0,0,360)})),
  // Beretta: rolls to show the open slide, then a half spin and back
  Map.entry("BERETTA",new Choreo(76,new float[][]{Z,k(.16F,-.03F,.04F,.05F,-10,0,-40),k(.34F,-.03F,.05F,.05F,-10,0,-40),k(.62F,-.02F,.07F,.04F,-180,0,0),k(.82F,-.01F,.03F,.02F,-360,0,0),k(1,0,0,0,-360,0,0)})),
  // ---- rifles and SMGs: calm, small movements, no sideways drift ----
  Map.entry("UZI",new Choreo(62,new float[][]{Z,k(.24F,-.01F,.02F,.03F,6,12,6),k(.56F,-.01F,.02F,.03F,-4,-14,-6),k(.82F,0,.01F,.01F,1,2,0),k(1,0,0,0,0,0,0)})),
  Map.entry("FAMAS",new Choreo(72,new float[][]{Z,k(.24F,-.01F,.02F,.03F,-8,10,3),k(.54F,-.01F,.02F,.03F,-5,22,-5),k(.82F,0,.01F,.01F,2,-4,1),k(1,0,0,0,0,0,0)})),
  Map.entry("M249",new Choreo(84,new float[][]{Z,k(.26F,-.01F,.02F,.03F,-6,-12,-6),k(.56F,-.01F,.02F,.04F,-8,-22,4),k(.84F,0,.01F,.01F,2,4,0),k(1,0,0,0,0,0,0)})),
  Map.entry("BARRETT",new Choreo(90,new float[][]{Z,k(.24F,-.01F,.03F,.04F,-14,6,0),k(.56F,-.01F,.03F,.04F,-18,12,-4),k(.84F,0,.01F,.01F,3,-3,0),k(1,0,0,0,0,0,0)})),
  Map.entry("MAC10",new Choreo(62,new float[][]{Z,k(.24F,-.01F,.02F,.03F,6,12,6),k(.56F,-.01F,.02F,.03F,-4,-14,-6),k(.82F,0,.01F,.01F,1,2,0),k(1,0,0,0,0,0,0)})),
  Map.entry("GALIL",new Choreo(72,new float[][]{Z,k(.22F,-.01F,.02F,.03F,-5,-10,-4),k(.52F,-.01F,.02F,.03F,-4,-20,8),k(.80F,0,.01F,.01F,3,6,-2),k(1,0,0,0,0,0,0)})),
  Map.entry("AUG",new Choreo(72,new float[][]{Z,k(.24F,-.01F,.02F,.03F,-8,10,3),k(.54F,-.01F,.02F,.03F,-5,22,-5),k(.82F,0,.01F,.01F,2,-4,1),k(1,0,0,0,0,0,0)})),
  Map.entry("SAIGA12",new Choreo(78,new float[][]{Z,k(.24F,-.01F,.03F,.03F,-16,6,0),k(.54F,-.01F,.03F,.04F,-18,18,-5),k(.82F,0,.01F,.01F,3,-4,0),k(1,0,0,0,0,0,0)})),
  Map.entry("SVD",new Choreo(90,new float[][]{Z,k(.24F,-.01F,.03F,.04F,-14,6,0),k(.56F,-.01F,.03F,.04F,-18,12,-4),k(.84F,0,.01F,.01F,3,-3,0),k(1,0,0,0,0,0,0)})),
  Map.entry("SCAR",new Choreo(74,new float[][]{Z,k(.24F,-.01F,.02F,.03F,-6,12,4),k(.54F,-.01F,.02F,.03F,-4,24,-6),k(.82F,0,.01F,.01F,2,-5,2),k(1,0,0,0,0,0,0)})),
  Map.entry("UMP45",new Choreo(66,new float[][]{Z,k(.24F,-.01F,.02F,.03F,6,-10,4),k(.56F,-.01F,.02F,.03F,-4,16,-6),k(.82F,0,.01F,.01F,1,2,0),k(1,0,0,0,0,0,0)})),
  Map.entry("SPAS12",new Choreo(78,new float[][]{Z,k(.24F,-.01F,.03F,.03F,-16,6,0),k(.54F,-.01F,.03F,.04F,-18,18,-5),k(.82F,0,.01F,.01F,3,-4,0),k(1,0,0,0,0,0,0)})),
  Map.entry("M4A1",new Choreo(70,new float[][]{Z,k(.22F,-.01F,.02F,.03F,-7,9,3),k(.52F,-.01F,.02F,.03F,-3,20,-5),k(.80F,0,.01F,.01F,2,-7,2),k(1,0,0,0,0,0,0)})),
  Map.entry("AK47",new Choreo(72,new float[][]{Z,k(.22F,-.01F,.02F,.03F,-5,-10,-4),k(.52F,-.01F,.02F,.03F,-4,-20,8),k(.80F,0,.01F,.01F,3,6,-2),k(1,0,0,0,0,0,0)})),
  Map.entry("M870",new Choreo(76,new float[][]{Z,k(.24F,-.01F,.03F,.03F,-14,8,0),k(.54F,-.01F,.03F,.04F,-20,14,-6),k(.82F,0,.01F,.01F,4,-4,0),k(1,0,0,0,0,0,0)})),
  Map.entry("MP5",new Choreo(66,new float[][]{Z,k(.24F,-.01F,.02F,.03F,-5,12,5),k(.56F,-.01F,.02F,.03F,-3,-14,-7),k(.82F,0,.01F,.01F,1,3,0),k(1,0,0,0,0,0,0)})),
  Map.entry("SMG",new Choreo(66,new float[][]{Z,k(.24F,-.01F,.02F,.03F,-8,10,6),k(.56F,-.01F,.02F,.03F,-4,-12,-6),k(.82F,0,.01F,.01F,2,2,0),k(1,0,0,0,0,0,0)})),
  Map.entry("SNIPER",new Choreo(84,new float[][]{Z,k(.24F,-.01F,.03F,.04F,-18,6,0),k(.56F,-.01F,.03F,.04F,-22,12,-5),k(.84F,0,.01F,.01F,3,-3,0),k(1,0,0,0,0,0,0)})),
  Map.entry("MP40",new Choreo(68,new float[][]{Z,k(.24F,-.01F,.02F,.03F,8,10,0),k(.56F,-.01F,.02F,.03F,5,-14,6),k(.82F,0,.01F,.01F,-2,3,0),k(1,0,0,0,0,0,0)})),
  Map.entry("P90",new Choreo(68,new float[][]{Z,k(.24F,-.01F,.02F,.03F,-9,12,-4),k(.56F,-.01F,.03F,.04F,-5,-12,7),k(.82F,0,.01F,.01F,2,2,0),k(1,0,0,0,0,0,0)})));
 private static final Map<String,Integer> KNIFE_TICKS=Map.of("butterfly_knife",110,"karambit",1000000,"xeno_hunter",110,"hrt3_knife",120,"banana",1000000);
 private static boolean butterflyOpen=true,nextOpen=true;private static String knifeId=null;
 public static boolean butterflyOpen(){return butterflyOpen;}
 private static Choreo current;

 private static float[] sample(float[][] keys,float t){
  for(int i=0;i<keys.length-1;i++){
   var a=keys[i];var b=keys[i+1];
   if(t>=a[0]&&t<=b[0]){float u=(t-a[0])/(b[0]-a[0]);u=u*u*(3-2*u);float[] out=new float[6];for(int n=0;n<6;n++)out[n]=Mth.lerp(u,a[n+1],b[n+1]);return out;}
  }
  return new float[6];
 }
 private static float progress(float partial){
  var mc=Minecraft.getInstance();if(start<0||mc.player==null||current==null)return -1;
  float t=(mc.player.tickCount+partial-start)/current.ticks;
  return t>=1?-1:Math.max(t,0);
 }
 public static void cancel(){start=-1;}
 /** {dx,dy,dz, rx,ry,rz} with rotations in radians for the gun matrix; zeros when idle or busy. */
 public static float[] gunPose(WeaponFamily family,float partial,boolean busy){
  if(busy)start=-1;
  float t=progress(partial);if(t<0||knife)return new float[6];
  var p=sample(current.keys,t);p[3]=(float)Math.toRadians(p[3]);p[4]=(float)Math.toRadians(p[4]);p[5]=(float)Math.toRadians(p[5]);return p;
 }
 /** Elapsed ticks of the running knife inspection, or -1 when none is running (loops never end). */
 public static float knifeTicks(float partial){var mc=Minecraft.getInstance();if(!knife||start<0||mc.player==null||progress(partial)<0)return -1;return Math.max(0,mc.player.tickCount+partial-start);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null){start=-1;return;}
  var held=p.getMainHandItem();
  var family=WeaponFamily.of(held);
  boolean gun=SniperItem.firearm(held);String knifeId=Knives.id(held);
  if(start>=0&&(!(gun||knifeId!=null)||p.getInventory().selected!=slot||mc.screen!=null||mc.options.keyAttack.isDown()||mc.options.keyUse.isDown()||SniperClient.RELOAD.isDown()))start=-1;
  while(KEY.consumeClick()){
   if(mc.screen!=null||!(gun||knifeId!=null))continue;
   if(progress(0)>=0){start=-1;continue;}   // pressing V again stops an inspection (needed for the endless knife loops)
   knife=knifeId!=null;
   if(knife){InspectClient.knifeId=knifeId;current=new Choreo(KNIFE_TICKS.getOrDefault(knifeId,80),new float[0][]);if(knifeId.equals("butterfly_knife")){butterflyOpen=nextOpen;nextOpen=!nextOpen;}}
   else{Choreo c=GUNS.get(family!=null?family.name():(held.is(MareEletrica.SMG.get())?"SMG":"SNIPER"));if(c==null)c=GUNS.get("M4A1");current=c;}
   start=p.tickCount;slot=p.getInventory().selected;
  }
 }
}
