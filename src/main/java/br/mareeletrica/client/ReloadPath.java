package br.mareeletrica.client;

import br.mareeletrica.WeaponFamily;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Hand-driven reload choreography for guns whose reload cannot be a simple slide: P90 (top magazine) and the Python revolver.
 *  All positions are in gun model space (1 unit = 1 block, 16 units per block). */
public final class ReloadPath {
 private ReloadPath(){}
 private static float s(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
 private static float seg(float p,float a,float b){return s((p-a)/(b-a));}
 private static float[] mix(float[] a,float[] b,float t){return new float[]{a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t,a[2]+(b[2]-a[2])*t};}
 private static final float[] ZERO={0,0,0};
 public static boolean special(WeaponFamily f){return f==WeaponFamily.PYTHON||f==WeaponFamily.P90;}

 // ---------------- P90: the hand grabs the magazine, pulls it up and back, throws it away, fetches a new one and slides it in ----------------
 private static final float[] LIFT={0,.55F,.45F},DISCARD={.45F,-.10F,.55F},BELT={.10F,-1.10F,.50F};
 /** Offset of the support hand AND of the magazine it carries. */
 public static float[] p90Offset(float p){
  if(p<.10F)return ZERO;
  if(p<.30F)return mix(ZERO,LIFT,seg(p,.10F,.30F));
  if(p<.42F)return mix(LIFT,DISCARD,seg(p,.30F,.42F));
  if(p<.55F)return mix(DISCARD,BELT,seg(p,.42F,.55F));
  if(p<.72F)return mix(BELT,LIFT,seg(p,.55F,.72F));
  if(p<.86F)return mix(LIFT,ZERO,seg(p,.72F,.86F));
  return ZERO;
 }
 /** The old magazine is thrown away at .42; the new one appears in the hand at .55. */
 public static boolean p90MagVisible(float p){return p<.42F||p>=.55F;}
 private static final Vector3f P90_BASE=new Vector3f(.5F,.84F,.62F);

 // ---------------- Python: cylinder swings out, rounds fall, the hand brings a speedloader from the belt, cylinder closes ----------------
 public static float pySwing(float p){return p<.02F?0:p<.14F?seg(p,.02F,.14F):p<.78F?1:p<.88F?1-seg(p,.78F,.88F):0;}
 public static float pyTilt(float p){return p<.10F?seg(p,0,.10F):p<.34F?1:p<.44F?1-seg(p,.34F,.44F):0;}
 public static float pySpentFall(float p){return seg(p,.14F,.30F);}
 /** Spent rounds are visible while seated (before .30, falling out from .14); the new cluster arrives with the loader. */
 public static boolean pySpentVisible(float p){return p<.30F;}
 public static boolean pySeatedVisible(float p){return p>=.72F;}
 /** Offset of the round cluster (speedloader) relative to the swung-out cylinder; null when it is not in the hand. */
 public static float[] pyLoaderOffset(float p){
  if(p<.44F||p>=.72F)return null;
  float[] belt={0F,-.95F,.85F},near={0F,-.22F,.40F};
  return p<.62F?mix(belt,near,seg(p,.44F,.62F)):mix(near,ZERO,seg(p,.62F,.72F));
 }
 private static final Vector3f CYL_OUT=new Vector3f(.31F,.55F,.34F),HC=new Vector3f(.28F,.58F,.16F),HC2=new Vector3f(.24F,.52F,.20F),BELT_PY=new Vector3f(.31F,-.405F,1.19F);

 /** Support-hand target for these families while reloading; other families get the default back. */
 public static Vector3f hand(WeaponFamily f,float p,Vector3f dflt){
  if(f==WeaponFamily.P90){
   if(p<.10F)return new Vector3f(dflt).lerp(P90_BASE,seg(p,0,.10F));
   if(p>.86F)return new Vector3f(P90_BASE).lerp(dflt,seg(p,.86F,1));
   var o=p90Offset(p);return new Vector3f(P90_BASE).add(o[0],o[1],o[2]);
  }
  if(f==WeaponFamily.PYTHON){
   if(p<.14F)return new Vector3f(dflt).lerp(HC,seg(p,0,.14F));
   if(p<.30F)return new Vector3f(HC);
   if(p<.44F)return new Vector3f(HC).lerp(BELT_PY,seg(p,.30F,.44F));
   if(p<.72F){var o=pyLoaderOffset(p);return new Vector3f(CYL_OUT).add(o[0],o[1],o[2]);}
   if(p<.78F)return new Vector3f(CYL_OUT).lerp(HC2,seg(p,.72F,.78F));
   if(p<.88F)return new Vector3f(HC2).lerp(HC,seg(p,.78F,.88F));
   return new Vector3f(HC).lerp(dflt,seg(p,.88F,1));
  }
  return dflt;
 }
}
