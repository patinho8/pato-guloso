package br.mareeletrica.client;

import br.mareeletrica.WeaponFamily;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Hand-driven reload choreography for guns whose reload cannot be a simple slide: P90 (top magazine) and the Python revolver.
 *  All positions are in gun model space (1 unit = 1 block, 16 units per block). */
public final class ReloadPath {
 /** 0.79.0 (tools/mag_anchors.py): magazine x | bottom y,z | top y,z (model px), one row per WeaponFamily */
 static final float[][] MAG={{8.0F,-2.18F,9.29F,7.62F,11.01F,8.0F,15.32F,16.36F},{8.0F,-7.73F,11.66F,5.71F,11.71F,8.0F,12.29F,14.96F},{8.0F,-3.69F,6.86F,6.15F,4.79F,7.82F,11.55F,15.77F},{8.0F,-1.87F,-1.33F,7.46F,0.07F,0F,0F,0F},{8F,0F,0F,0F,0F,8.0F,7.62F,-0.64F},{8.0F,-3.97F,1.86F,5.64F,1.2F,6.13F,11.76F,-10.43F},{8.0F,-7.79F,4.76F,2.36F,4.94F,6.27F,11.22F,5.48F},{8F,0F,0F,0F,0F,0F,0F,0F},{8.0F,-7.72F,0.62F,4.63F,0.19F,6.03F,10.64F,10.38F},{8.0F,9.27F,10.29F,10.57F,10.29F,8.0F,6.82F,4.22F},{8F,0F,0F,0F,0F,8.0F,12.71F,11.94F},{8.0F,-7.84F,11.66F,5.71F,11.71F,8.0F,12.18F,15.02F},{8.0F,-4.27F,4.85F,3.48F,3.77F,5.56F,10.53F,-1.0F},{8.0F,-3.76F,1.34F,5.85F,0.71F,6.17F,11.72F,-7.41F},{8F,0F,0F,0F,0F,8.0F,8.7F,-1.36F},{8.0F,-7.95F,11.71F,5.6F,11.66F,7.96F,12.01F,14.96F},{8.0F,-5.63F,11.64F,5.53F,11.64F,8.0F,12.37F,15.63F},{8.0F,-4.59F,17.77F,3.48F,17.11F,8.0F,13.16F,13.58F},{6.04F,-1.12F,4.56F,7.39F,0.98F,10.08F,7.9F,3.41F},{8.0F,-2.58F,1.47F,4.19F,0.88F,10.45F,9.5F,-2.29F},{8.0F,-6.5F,11.6F,5.46F,11.6F,8.0F,11.94F,15.52F},{8.0F,-4.58F,5.95F,6.0F,4.04F,7.19F,9.42F,9.08F},{8.0F,-4.4F,20.25F,3.98F,17.97F,6.03F,10.5F,8.4F},{8.0F,-2.68F,8.17F,6.18F,7.01F,10.13F,9.34F,9.08F},{8.0F,-0.1F,7.31F,5.95F,4.26F,9.75F,8.99F,7.44F}};
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
 public static float pySwing(float p){float t=tk(p),D=curD;return t<1?0:t<6?seg(t,1,6):t<D-10?1:t<D-5?1-seg(t,D-10,D-5):0;}
 public static float pyTilt(float p){float t=tk(p);return t<5?seg(t,0,5):t<12?1:t<16?1-seg(t,12,16):0;}
 // ---------------- 0.80.0: round-by-round timing (shared with Magazines.loosePre/Per/Post) ----------------
 static int curN=0,curD=1;static WeaponFamily curF;
 /** call once per frame with the gun in hand */
 public static void cur(net.minecraft.world.item.ItemStack s){var t=br.mareeletrica.RifleLoadout.tag(s);curF=WeaponFamily.of(s);curN=t.getInt("ShellLoad");curD=Math.max(1,t.getInt("ReloadDuration"));}
 static float tk(float p){return p*curD;}
 /** which round (0..n-1) and how far into its cycle (0..1); round<0 = before, round>=n = after */
 static int rnd(float p){if(curF==null)return -1;float t=tk(p)-br.mareeletrica.Magazines.loosePre(curF);return t<0?-1:Math.min(curN,(int)(t/br.mareeletrica.Magazines.loosePer(curF)));}
 static float rndU(float p){if(curF==null)return 0;float t=tk(p)-br.mareeletrica.Magazines.loosePre(curF);int per=br.mareeletrica.Magazines.loosePer(curF);return t<0?0:(t%per)/per;}
 static boolean looseRound(float p){int r=rnd(p);return r>=0&&r<curN&&rndU(p)<.62F;}
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


 // =====================================================================================================
 // 0.79.0: a reload for every gun, built from where ITS magazine really is (MAG table) and how heavy it is.
 // Units: px of the gun model (16 per block) inside, blocks in what is returned.
 // =====================================================================================================
 public enum Style{PISTOL,RIFLE,ROCK,HEAVY,BULLPUP,TUBE,OTHER}
 public static Style style(WeaponFamily f){return switch(f){
  case DESERT_EAGLE,USP,GLOCK,BERETTA->Style.PISTOL;
  case AK47,GALIL,SVD,SAIGA12->Style.ROCK;          // curved / hooked magazines: rocked out and in
  case M249,BARRETT->Style.HEAVY;
  case FAMAS,AUG->Style.BULLPUP;                    // magazine behind the grip
  case M870,SPAS12->Style.TUBE;                     // shell by shell under the receiver, then the pump
  case M4A1,SCAR,MP5,UMP45,SMG,MP40,UZI,MAC10->Style.RIFLE;
  default->Style.OTHER;};}
 public static boolean choreo(WeaponFamily f){return style(f)!=Style.OTHER;}
 /** 0 = feather light, 1 = machine gun */
 public static float weight(WeaponFamily f){return switch(f){case GLOCK,USP,BERETTA->.15F;case DESERT_EAGLE->.4F;case UZI,MAC10->.25F;case MP5,UMP45,SMG,MP40->.35F;
  case M4A1,SCAR,FAMAS,AUG->.5F;case AK47,GALIL->.55F;case SAIGA12,SVD,M870,SPAS12->.65F;case BARRETT->.9F;case M249->1F;default->.5F;};}
 static float[] row(WeaponFamily f){int i=f.ordinal();return i<MAG.length?MAG[i]:new float[8];}
 static float len(float[] m){return (float)Math.sqrt((m[3]-m[1])*(m[3]-m[1])+(m[4]-m[2])*(m[4]-m[2]));}
 /** unit vector from the magazine bottom to its top (y,z) */
 static float[] axis(float[] m){float l=Math.max(.01F,len(m));return new float[]{(m[3]-m[1])/l,(m[4]-m[2])/l};}
 static float rockMax(WeaponFamily f){return style(f)==Style.ROCK?24F:0F;}
 /** rotate (y,z) about the pivot by deg, same sense as Axis.XP */
 static float[] rot(float y,float z,float py,float pz,float deg){double r=Math.toRadians(deg),c=Math.cos(r),sn=Math.sin(r);float dy=y-py,dz=z-pz;return new float[]{(float)(py+dy*c-dz*sn),(float)(pz+dy*sn+dz*c)};}

 /** Magazine state relative to seated: {dx,dy,dz (blocks), rock deg about the magazine top, visible 0/1}; null = draw it normally. */
 public static float[] mag(WeaponFamily f,float p){
  var st=style(f);if(st==Style.OTHER||st==Style.TUBE)return null;
  float[] m=row(f);float L=len(m);if(L<.5F)return null;float[] a=axis(m);float R=rockMax(f);
  float out=0,rk=0,tx=0,ty=0,tz=0;boolean vis=true;
  if(st==Style.PISTOL){
   if(p<.08F){}
   else if(p<.26F){float u=(p-.08F)/.18F;out=(L+4F)*u*u;ty=-6F*u*u;}            // released: falls free under its own weight
   else if(p<.44F)vis=false;                                                   // gone; the hand fetches a new one
   else{var h=newMag(f,p,m,a,L,R);out=h[0];rk=h[1];tx=h[2];ty=h[3];tz=h[4];}
  }else{
   float pull=.12F,clear=.30F;
   if(p<pull){}
   else if(p<clear){float u=seg(p,pull,clear);if(R>0){rk=R*seg(p,pull,pull+.07F);out=(L+2F)*seg(p,pull+.06F,clear);}else out=(L+2F)*u;}
   else if(p<.40F){float u=seg(p,clear,.40F);out=L+2F;rk=R;tx=-5F*u;ty=-9F*u;tz=3F*u;}   // thrown away down to the side
   else if(p<.47F)vis=false;
   else{var h=newMag(f,p,m,a,L,R);out=h[0];rk=h[1];tx=h[2];ty=h[3];tz=h[4];}
  }
  return new float[]{tx/16F,(ty-a[0]*out)/16F,(tz-a[1]*out)/16F,rk,vis?1:0};
 }
 /** the new magazine: from the belt to under the well, then in (rocked in for AK-type magazines) */
 static float[] newMag(WeaponFamily f,float p,float[] m,float[] a,float L,float R){
  boolean pistol=style(f)==Style.PISTOL;float t0=pistol?.44F:.47F,t1=pistol?.60F:.64F,t2=pistol?.70F:.74F;
  float pre=L+1.5F;float[] belt=beltOff(m);
  if(p<t0)return new float[]{pre,R,belt[0],belt[1],belt[2]};
  if(p<t1){float u=seg(p,t0,t1);return new float[]{pre,R,belt[0]*(1-u),belt[1]*(1-u),belt[2]*(1-u)};}
  if(p<t2){float u=seg(p,t1,t2);if(R>0)return new float[]{pre*(1-seg(p,t1,t1+.06F)),R*(1-seg(p,t1+.05F,t2)),0,0,0};return new float[]{pre*(1-u),0,0,0,0};}
  return new float[]{0,0,0,0,0};
 }
 /** where the belt pouch is, relative to the pre-insert spot (px) */
 static float[] beltOff(float[] m){return new float[]{-7F,-16F,7F};}

 /** Support hand for the choreographed reloads (gun model blocks). */
 public static Vector3f hand2(WeaponFamily f,float p,Vector3f dflt){
  var st=style(f);float[] m=row(f);
  if(st==Style.TUBE)return tubeHand(f,p,m,dflt);
  float[] a=axis(m);float L=len(m);float R=rockMax(f);
  var mg=mag(f,p);
  // the magazine bottom (where the hand holds it), following the magazine
  float[] b=rot(m[1],m[2],m[3],m[4],mg==null?0:mg[3]);
  Vector3f bottom=new Vector3f(m[0]/16F+(mg==null?0:mg[0]),(b[0]-1.3F)/16F+(mg==null?0:mg[1]),b[1]/16F+(mg==null?0:mg[2]));
  float[] bo=beltOff(m);
  Vector3f belt=new Vector3f(m[0]/16F+bo[0]/16F,(m[1]-a[0]*(L+1.5F)+bo[1])/16F,(m[2]-a[1]*(L+1.5F)+bo[2])/16F);
  Vector3f bolt=m[5]!=0?new Vector3f((m[5]-1.2F)/16F,(m[6]+.6F)/16F,(m[7]-.8F)/16F).add(0,0,boltPull(f,p)):null;
  if(st==Style.PISTOL){
   if(p<.14F)return new Vector3f(dflt).lerp(belt,seg(p,0,.14F));           // the thumb drops the magazine, the hand goes for a new one
   if(p<.44F)return belt;
   if(p<.70F)return bottom;                                                // brings the new one in
   if(p<.76F){float k=Mth.sin(seg(p,.70F,.76F)*Mth.PI);return new Vector3f(bottom).add(0,a[0]*k*1.2F/16F,a[1]*k*1.2F/16F);}   // palm slap
   if(bolt!=null){if(p<.82F)return new Vector3f(bottom).lerp(bolt,seg(p,.76F,.82F));if(p<.90F)return bolt;return new Vector3f(bolt).lerp(dflt,seg(p,.90F,1F));}   // racks the slide
   return new Vector3f(bottom).lerp(dflt,seg(p,.76F,1F));
  }
  if(p<.12F)return new Vector3f(dflt).lerp(bottom,seg(p,0,.12F));           // reaches the magazine
  if(p<.36F)return bottom;                                                  // pulls it out
  if(p<.47F)return new Vector3f(bottom).lerp(belt,seg(p,.36F,.47F));      // throws it, goes to the pouch
  if(p<.74F)return bottom;                                                  // new one, in
  if(p<.79F){float k=Mth.sin(seg(p,.74F,.79F)*Mth.PI);return new Vector3f(bottom).add(0,a[0]*k*1.4F/16F,a[1]*k*1.4F/16F);}   // slap it home
  if(bolt!=null){if(p<.84F)return new Vector3f(bottom).lerp(bolt,seg(p,.79F,.84F));if(p<.92F)return bolt;return new Vector3f(bolt).lerp(dflt,seg(p,.92F,1F));}  // charging handle
  return new Vector3f(bottom).lerp(dflt,seg(p,.79F,1F));
 }
 /** how far back the slide / bolt / pump travels right now (blocks, +z = towards the shooter) */
 public static float boltPull(WeaponFamily f,float p){
  var st=style(f);if(st==Style.OTHER)return 0;
  float a=st==Style.PISTOL?.82F:st==Style.TUBE?.84F:.84F,b=st==Style.PISTOL?.88F:st==Style.TUBE?.92F:.89F,c=b+.02F;
  float travel=(st==Style.PISTOL?2.6F:st==Style.TUBE?3.2F:st==Style.HEAVY?3.6F:3F)/16F;
  if(st==Style.TUBE){float t=tk(p),D=curD;float s0=D-10,s1=D-6,s2=D-2;if(t<s0)return 0;return t<s1?travel*seg(t,s0,s1):travel*(1-seg(t,s1,s2));}   // pump at the very end
  if(p<a||p>c)return 0;return p<b?travel*seg(p,a,b):travel*(1-seg(p,b,c));   // eased back, snaps forward
 }

 // ---------------- tube shotguns: shells one by one into the loading port, then pump ----------------
 public static final int SHELLS=4;static final float TS=.06F,TE=.80F;
 /** which shell cycle and where in it (0..1) */
 static float cyc(float p){return Mth.clamp((p-TS)/((TE-TS)/SHELLS),0,SHELLS);}
 public static boolean shellInHand(WeaponFamily f,float p){if(!br.mareeletrica.Magazines.loose(f))return false;int r=rnd(p);if(r<0||r>=curN)return false;float c=rndU(p);return f==WeaponFamily.PYTHON?c<.62F:c>.18F&&c<.8F;}
 static Vector3f port(float[] m){return new Vector3f(8F/16F,4.4F/16F,(m[7]+5.2F)/16F);}
 static Vector3f tubeHand(WeaponFamily f,float p,float[] m,Vector3f dflt){
  Vector3f port=port(m),pouch=new Vector3f(port).add(-7F/16F,-12F/16F,5F/16F),pump=new Vector3f(dflt).add(0,0,boltPull(f,p));
  float t=tk(p),D=curD;int r=rnd(p);
  if(r<0)return new Vector3f(dflt).lerp(pouch,seg(t,0,br.mareeletrica.Magazines.loosePre(f)));
  if(r<curN){float c=rndU(p);
   if(c<.18F)return pouch;                                                     // takes a shell
   if(c<.55F)return new Vector3f(pouch).lerp(new Vector3f(port).add(0,-1.5F/16F,1F/16F),seg(c,.18F,.55F));
   if(c<.8F)return new Vector3f(port).add(0,-1.5F/16F,1F/16F).lerp(new Vector3f(port).add(0,0,-1.8F/16F),seg(c,.55F,.8F));   // thumbs it in
   return new Vector3f(port).lerp(pouch,seg(c,.8F,1F));}
  float end=D-br.mareeletrica.Magazines.loosePost(f);
  if(t<end+4)return new Vector3f(pouch).lerp(dflt,seg(t,end,end+4));
  return pump;                                                                  // back to the fore-end: pump
 }

 /** gun pose while reloading (times the reload envelope): {dx,dy,dz, roll, pitch}, plus a dip when the magazine seats */
 public static float[] gun(WeaponFamily f,float p){
  if(f==WeaponFamily.PYTHON)return new float[]{-.04F,.20F,-.26F,16F,2F,20F};   // 0.80.0: on its side, cylinder towards the camera
  float w=weight(f);float[] g=switch(style(f)){
   // dx,dy,dz, roll, pitch, yaw (muzzle swings to the support side so the magazine well faces the camera)
   case PISTOL->new float[]{-.02F,.36F,-.10F,20F,20F,26F};
   case RIFLE->new float[]{-.10F,.27F,.06F,18F,10F,42F};
   case ROCK->new float[]{-.10F,.27F,.06F,22F,8F,42F};
   case HEAVY->new float[]{-.06F,.33F,.02F,12F,6F,38F};
   case BULLPUP->new float[]{.02F,.26F,-.20F,22F,12F,46F};
   case TUBE->new float[]{-.08F,.24F,.06F,40F,4F,36F};
   default->new float[]{-.14F,.32F,-.12F,28F,6F,0F};};
  // the magazine seats (or the pump is racked): a small jolt, bigger on heavy guns
  float seat=style(f)==Style.PISTOL?.70F:style(f)==Style.TUBE?Math.max(0,1F-8F/curD):.74F;
  float j=p>seat&&p<seat+.08F?Mth.sin((p-seat)/.08F*Mth.PI):0;
  g[1]-=j*(.012F+.03F*w);g[4]+=j*(2F+6F*w);
  // heavy guns sag while one hand is off the gun
  float off=style(f)!=Style.TUBE&&p>.12F&&p<.74F?1:0;g[1]-=off*.05F*w;g[3]-=off*4F*w;
  return g;
 }
 /** Support-hand target for these families while reloading; other families get the default back. */
 public static Vector3f hand(WeaponFamily f,float p,Vector3f dflt){
  if(f==WeaponFamily.P90){
   if(p<.10F)return new Vector3f(dflt).lerp(P90_BASE,seg(p,0,.10F));
   if(p>.86F)return new Vector3f(P90_BASE).lerp(dflt,seg(p,.86F,1));
   var o=p90Offset(p);return new Vector3f(P90_BASE).add(o[0],o[1],o[2]);
  }
  if(f==WeaponFamily.PYTHON){   // 0.80.0: opens, ejects, then one round at a time from the belt into the cylinder, closes
   float t=tk(p),D=curD;int r=rnd(p);float u=rndU(p);
   Vector3f chamber=new Vector3f(CYL_OUT).add(0,.02F,.07F),near=new Vector3f(CYL_OUT).add(0,-.10F,.30F);
   if(t<6)return new Vector3f(dflt).lerp(HC,seg(t,0,6));
   if(t<13){float k=Mth.sin(seg(t,6,13)*Mth.PI);return new Vector3f(HC).add(0,0,-.05F*k);}        // pushes the ejector rod
   if(r<0)return new Vector3f(HC).lerp(BELT_PY,seg(t,13,16));
   if(r<curN){
    if(u<.35F)return new Vector3f(BELT_PY).lerp(near,seg(u,0,.35F));
    if(u<.62F)return new Vector3f(near).lerp(chamber,seg(u,.35F,.62F));                         // thumbs the round in
    return new Vector3f(chamber).lerp(r==curN-1?HC2:BELT_PY,seg(u,.62F,1F));}
   if(t<D-5)return new Vector3f(HC2).lerp(HC,seg(t,D-10,D-5));                                  // closes the cylinder
   return new Vector3f(HC).lerp(dflt,seg(t,D-5,D));
  }
  return dflt;
 }
}
