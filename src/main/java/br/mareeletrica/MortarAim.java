package br.mareeletrica;
/** Elevation above the horizon; positive azimuth is right of the machine's front. */
public final class MortarAim {
 public static final double SPEED=3.2,BARREL_LENGTH=1.5;
 private MortarAim(){}
 public static int elevation(int degrees){return Math.max(0,Math.min(90,degrees));}
 public static int azimuth(int degrees){return Math.floorMod(degrees+180,360)-180;}
 public static double[] direction(int elevation,int azimuth,int frontX,int frontZ){double up=Math.toRadians(elevation(elevation)),right=Math.toRadians(azimuth(azimuth));double forward=Math.cos(right)*Math.cos(up),side=Math.sin(right)*Math.cos(up);return new double[]{frontX*forward-frontZ*side,Math.sin(up),frontZ*forward+frontX*side};}
}
