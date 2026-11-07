package br.mareeletrica;
/** Authoritative control limits, shared with offline tests. */
public final class VehicleRules {
 public static final int RANGE=1024,MAGAZINE=256,TIMEOUT=100,SHOT_DELAY=8,SHOT_ENERGY=200;
 public static final int LAND=0,BOAT=1,GLIDER=2,PLANE=3;
 private VehicleRules(){}
 public static float axis(float v){return Float.isFinite(v)?Math.max(-1,Math.min(1,v)):0;}
 public static float pitch(float v){return Float.isFinite(v)?Math.max(-70,Math.min(70,v)):0;}
 public static float turn(float current,float requested){if(!Float.isFinite(requested))return current;float delta=((requested-current)%360+540)%360-180;return current+Math.max(-18,Math.min(18,delta));}
 public static int mode(boolean water,boolean ground,double downward,float fall,int previous){if(water)return BOAT;if(ground)return LAND;return downward<0&&(fall>=2.5F||previous==GLIDER)?GLIDER:LAND;}
 public static boolean inRange(double x,double y,double z){return x*x+y*y+z*z<=RANGE*(double)RANGE;}
 public static boolean canShoot(boolean manual,int ammo,int energy,int cooldown){return manual&&ammo>0&&energy>=SHOT_ENERGY&&cooldown<=0;}
}
