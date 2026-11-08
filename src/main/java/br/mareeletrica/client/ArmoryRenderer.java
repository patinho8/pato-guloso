package br.mareeletrica.client;
import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent;
/** Family-specific geometry and coherent optical dots, drawn in the same gun transform. */
public final class ArmoryRenderer {
 public static void register(ModelEvent.RegisterAdditional e){for(String part:new String[]{"body","view","moving","magazine"})e.register(ClientEvents.part(name(WeaponFamily.SNIPER,part,13)));for(var f:WeaponFamily.values()){for(int skin=0;skin<=12;skin++)for(String part:new String[]{"body","view","moving","magazine"})e.register(ClientEvents.part(name(f,part,skin)));for(int v=1;v<=3;v++)for(String part:new String[]{"optic","reticle","suppressor"})e.register(ClientEvents.part(name(f,part,v)));}e.register(ClientEvents.part(name(WeaponFamily.PYTHON,"rounds",0)));for(String a:ACC_MODELS){e.register(ClientEvents.part(a));}}
 public static String name(WeaponFamily f,String part,int n){return "armory21_"+f.id+"_"+part+"_"+n;}
 /** 0.72.0 (tools/gunsmith_072.py): muzzle z and bore height of each gun, iron sight line and the centre of each optic */
 static final double[] MUZZLE={-6.87,-1.56,-12.72,-23.20,-17.09,-8.39,-0.17,-49.80,-5.67,0.18,-7.40,-1.00,-13.10,-5.16,-11.92,-1.72,2.88,-3.58,-17.56,-29.78,7.46,-9.81,-6.17,-10.75,-21.56};
 static final float[] BORE={9.60F,8.08F,8.40F,9.60F,7.24F,8.47F,8.89F,8.60F,8.23F,6.54F,9.44F,8.08F,8.14F,8.33F,7.76F,8.00F,8.04F,7.71F,7.57F,8.18F,7.67F,7.62F,8.42F,7.67F,7.71F};
 static final float[] IRON={12.40F,10.12F,10.90F,11.10F,8.98F,10.72F,11.02F,13.50F,9.97F,9.77F,11.24F,10.04F,10.60F,11.05F,9.38F,10.00F,9.87F,11.15F,10.44F,10.70F,9.30F,9.80F,10.44F,9.94F,8.96F};
 static final float[][] SIGHT={{13.02F,13.27F,13.57F},{10.64F,10.89F,11.19F},{11.48F,11.58F,11.87F},{13.60F,13.40F,13.90F},{9.86F,9.66F,9.96F},{12.04F,11.84F,12.14F},{11.79F,11.59F,12.09F},{15.70F,16.25F,16.80F},{10.72F,10.52F,10.82F},{10.85F,10.65F,10.95F},{11.72F,11.97F,12.27F},{10.60F,10.85F,11.15F},{11.27F,11.37F,11.66F},{11.83F,11.63F,11.93F},{10.10F,9.90F,10.20F},{10.60F,10.85F,11.15F},{10.95F,10.75F,11.05F},{12.22F,12.32F,12.61F},{11.17F,10.97F,11.47F},{11.66F,11.84F,12.02F},{10.39F,10.19F,10.49F},{10.56F,10.66F,10.95F},{11.13F,11.23F,11.52F},{10.70F,10.50F,10.80F},{10.64F,10.82F,11.00F}};
 /** 0.74.0 (tools/acc_anchors.py): under y,z | side x,y,z | drum y,z | magazine top */
 static final float[][] ACC={{8.48F,-5.47F,8.6F,9.8F,-6.97F,0.29F,12.45F,7.46F},{4.88F,-0.16F,8.6F,8.28F,-1.66F,-3.63F,9.97F,5.98F},{7.35F,-1.88F,9.02F,8.6F,-3.38F,3.94F,7.35F,5.96F},{8.1F,-11.19F,9.4F,9.8F,-12.69F,7.0F,-0.07F,7.46F},{5.84F,-9.91F,8.43F,7.44F,-11.41F,0F,-5.13F,0F},{6.73F,-1.57F,9.16F,8.67F,-3.07F,5.22F,4.65F,5.62F},{7.95F,3.63F,8.69F,9.09F,2.13F,3.17F,7.14F,3.48F},{0F,0F,0F,0F,0F,0F,0F,0F},{7.39F,-0.23F,8.36F,8.43F,-1.73F,4.57F,4.43F,4.97F},{5.22F,3.01F,8.53F,6.74F,1.51F,1.7F,10.78F,8.84F},{7.55F,-6.0F,8.6F,9.64F,-7.5F,0F,-2.22F,0F},{4.88F,0.4F,8.6F,8.28F,-1.1F,-3.61F,9.56F,6.06F},{6.78F,-2.08F,9.11F,8.34F,-3.58F,3.86F,7.24F,4.21F},{6.63F,0.02F,8.85F,8.53F,-1.48F,5.51F,4.93F,5.76F},{6.35F,-6.91F,8.62F,7.96F,-8.41F,0F,-3.58F,0F},{5.12F,-0.32F,8.6F,8.2F,-1.82F,-3.81F,10.26F,5.85F},{7.38F,5.65F,8.36F,8.24F,4.15F,0.44F,11.14F,5.64F},{5.6F,3.53F,9.03F,7.91F,2.03F,3.96F,15.63F,4.21F},{5.6F,-5.14F,8.41F,7.77F,-6.64F,5.58F,6.01F,6.92F},{6.3F,-10.93F,8.56F,8.38F,-12.43F,4.19F,4.16F,4.57F},{4.45F,8.49F,8.6F,7.87F,6.99F,0.32F,11.25F,5.57F},{6.16F,-1.21F,8.49F,7.82F,-2.71F,5.6F,6.87F,5.86F},{8.08F,2.03F,8.38F,8.62F,0.53F,4.19F,16.03F,4.54F},{6.59F,-1.13F,8.62F,7.87F,-2.63F,5.6F,7.38F,5.98F},{6.54F,-5.58F,8.47F,7.91F,-7.08F,5.6F,6.81F,5.86F}};
 /** 0.75.0 (tools/gunforge.py): where the support hand holds each remodelled gun (y,z px); 0,0 = the default spot */
 static final float[][] SUPPORT={{0F,0F},{0F,0F},{0F,0F},{0F,0F},{5.37F,-2.03F},{0F,0F},{6.3F,5.26F},{0F,0F},{0F,0F},{4.89F,7.02F},{0F,0F},{0F,0F},{0F,0F},{0F,0F},{5.74F,0.56F},{0F,0F},{6.63F,6.67F},{5.83F,5.37F},{5.46F,-0.62F},{7.01F,-7.67F},{5.22F,9.37F},{0F,0F},{6.07F,4.9F},{0F,0F},{0F,0F}};
 public static float[] support(WeaponFamily f){int i=f.ordinal();return i<SUPPORT.length&&(SUPPORT[i][0]!=0||SUPPORT[i][1]!=0)?SUPPORT[i]:null;}
 public static double muzzle(WeaponFamily f){return MUZZLE[f.ordinal()];}
 public static double muzzle(ItemStack s,WeaponFamily f){return s.is(MareEletrica.HUNTER.get())?-21.7:muzzle(f);}
 static final float BIPOD_Y=-.6F/16,BIPOD_Z=-5F/16;
 public static float bore(ItemStack s,WeaponFamily f){return s.is(MareEletrica.HUNTER.get())?10.2F:BORE[f.ordinal()];}
 public static int optic(ItemStack s){return Math.clamp(RifleLoadout.tag(s).getInt("OpticVariant"),1,3);}
 public static float sightY(ItemStack s){var f=WeaponFamily.of(s);if(f==null)return 13.5F;if(!ModularLoadout.hasOptic(s))return IRON[f.ordinal()];return SIGHT[f.ordinal()][optic(s)-1];}
 public static float progress(ItemStack s,float partial){var l=Minecraft.getInstance().level;return l==null?0:Magazines.progress(s,l.getGameTime(),partial);}
 public static float drop(float p){if(p<=.12F)return 0;if(p<.43F)return smooth((p-.12F)/.31F);if(p<=.58F)return 1;if(p<.9F)return 1-smooth((p-.58F)/.32F);return 0;}
 public static float reloadPose(float p){return p<.12F?smooth(p/.12F):p>.9F?1-smooth((p-.9F)/.1F):1;}
 public static float magSlide(float p){if(p<=.12F)return 0;if(p<.5F)return smooth((p-.12F)/.38F);if(p<.9F)return 1-smooth((p-.5F)/.4F);return 0;}
 private static float smooth(float x){return x*x*(3-2*x);}
 public static float magazineZ(WeaponFamily f){return f.pistol()||f==WeaponFamily.UZI||f==WeaponFamily.MAC10?.74F:f==WeaponFamily.SMG?.25F:.37F;}
 public static void parts(ItemStack s,PoseStack pose,MultiBufferSource buffers,int light,boolean supported,boolean first){var f=WeaponFamily.of(s);if(f==null)return;int skin=s.is(MareEletrica.HUNTER.get())?13:Math.clamp(RifleLoadout.tag(s).getInt("Sticker"),0,12);float reload=first?drop(progress(s,0)):0;String base="armory21_"+f.id+"_";
  String bk=first&&SniperClient.aimAmount(1F)>.3F?"view":"body";PaintClient.model(s,f,bk,base+bk+"_"+skin,pose,buffers,light);if(bk.equals("body"))PaintClient.sticker(s,f,pose,buffers,light);
  pose.pushPose();pose.translate(0,0,first?SniperClient.kick*(f.pistol()?.1:.07):0);PaintClient.model(s,f,"moving",base+"moving_"+skin,pose,buffers,light);pose.popPose();
  float prog=first?progress(s,0):0;
  if(f==WeaponFamily.PYTHON){
   // revolver: cylinder swings out to the left, spent rounds fall, the hand brings a speedloader from the belt, the cylinder closes
   float swing=first?ReloadPath.pySwing(prog):0;
   pose.pushPose();pose.translate(6.4/16.0,6.6/16.0,0);pose.mulPose(Axis.ZP.rotationDegrees(swing*70F));pose.translate(-6.4/16.0,-6.6/16.0,0);
   PaintClient.model(s,f,"magazine",base+"magazine_"+skin,pose,buffers,light);pose.popPose();
   float[] loader=first?ReloadPath.pyLoaderOffset(prog):null;
   boolean spent=!first||ReloadPath.pySpentVisible(prog),seated=!first||ReloadPath.pySeatedVisible(prog);
   float fall=first?ReloadPath.pySpentFall(prog):0;
   if((spent&&fall<.97F)||seated||loader!=null){
    pose.pushPose();
    if(loader!=null)pose.translate(loader[0],loader[1],loader[2]);else if(spent&&!seated)pose.translate(0,-fall*.65F,0);
    pose.translate(6.4/16.0,6.6/16.0,0);pose.mulPose(Axis.ZP.rotationDegrees(swing*70F));pose.translate(-6.4/16.0,-6.6/16.0,0);
    DmtGearLayer.model(base+"rounds_0",pose,buffers,light);pose.popPose();
   }
  }else if(f==WeaponFamily.P90){
   // P90: the support hand pulls the top magazine up and back, throws it away, then slides a fresh one in from above
   float[] o=first?ReloadPath.p90Offset(prog):new float[3];
   if(!first||ReloadPath.p90MagVisible(prog)){pose.pushPose();pose.translate(o[0],o[1],o[2]);PaintClient.model(s,f,"magazine",base+"magazine_"+skin,pose,buffers,light);pose.popPose();}
  }else{
  float slide=first?magSlide(prog):0;boolean outgoing=first&&prog<.5F;
  if(slide<.88F){pose.pushPose();pose.translate(-slide*(outgoing?.28F:0F),-slide*(outgoing?.95F:.8F),slide*.04F);pose.translate(.5,0,magazineZ(f));pose.mulPose(Axis.ZP.rotationDegrees(outgoing?-slide*22:0));pose.translate(-.5,0,-magazineZ(f));mag(s,f,base,skin,pose,buffers,light);pose.popPose();}
  }
  accessories(s,f,pose,buffers,light,supported);
  if(ModularLoadout.hasOptic(s)&&f!=WeaponFamily.SNIPER){int v=optic(s);PaintClient.model(s,f,"optic_"+v,base+"optic_"+v,pose,buffers,light);if(first)DmtGearLayer.model(base+"reticle_"+v,pose,buffers,15728880);}
  int suppressor=RifleLoadout.suppressor(s);if(suppressor>0){pose.pushPose();pose.translate(0,(bore(s,f)-10)/16,muzzle(s,f)/16);DmtGearLayer.model(base+"suppressor_"+Math.min(3,suppressor),pose,buffers,light);pose.popPose();}
  // 0.72.0: the Precision sniper has its own folding bipod in the model; the Hunter gets the bipod under its fore-end
  if(f==WeaponFamily.SNIPER&&RifleLoadout.bipod(s)&&s.is(MareEletrica.HUNTER.get())){pose.pushPose();pose.translate(0,BIPOD_Y,BIPOD_Z);DmtGearLayer.model(supported?"rifle_bipod_open":"rifle_bipod_folded",pose,buffers,light);pose.popPose();}
 }

 // ---------- 0.74.0 accessories ----------
 static final String[] ACC_MODELS={"acc_vertical_grip","acc_angled_grip","acc_bipod","acc_flashlight","acc_flashlight_lens","acc_laser_red","acc_laser_red_lens","acc_laser_green","acc_laser_green_lens","acc_combo","acc_combo_lens",
  "acc_flashlight_under","acc_flashlight_under_lens","acc_laser_red_under","acc_laser_red_under_lens","acc_laser_green_under","acc_laser_green_under_lens","acc_combo_under","acc_combo_under_lens","acc_drum"};
 static final int FULL=15728880;
 /** the magazine, a drum in its place, or the same magazine stretched downwards (extended) */
 static void mag(ItemStack s,WeaponFamily f,String base,int skin,PoseStack pose,MultiBufferSource buffers,int light){
  var k=Accessory.mag(s);float[] a=ACC[f.ordinal()];
  if(k==Accessory.Kind.DRUM_MAG){pose.pushPose();pose.translate(0,a[5]/16,a[6]/16);DmtGearLayer.model("acc_drum",pose,buffers,light);pose.popPose();return;}
  if(k==Accessory.Kind.EXTENDED_MAG){pose.pushPose();float top=a[7]/16;pose.translate(0,top,0);pose.scale(1,f.pistol()?1.5F:1.4F,1);pose.translate(0,-top,0);PaintClient.model(s,f,"magazine",base+"magazine_"+skin,pose,buffers,light);pose.popPose();return;}
  PaintClient.model(s,f,"magazine",base+"magazine_"+skin,pose,buffers,light);
 }
 /** where the beam of a light/laser leaves the gun, in the gun's model space (pixels) — for the laser line */
 public static float[] beamOrigin(ItemStack s,WeaponFamily f){
  float[] a=ACC[f.ordinal()];var side=Accessory.side(s);
  if(side!=null)return new float[]{a[2]+.3F+(side==Accessory.Kind.COMBO?1.2F:.95F),a[3]+.1F,a[4]-2.1F};
  return new float[]{8,a[0]-1F,a[1]-1.9F};
 }
 static void accessories(ItemStack s,WeaponFamily f,PoseStack pose,MultiBufferSource buffers,int light,boolean supported){
  if(f==WeaponFamily.SNIPER)return;float[] a=ACC[f.ordinal()];
  var u=Accessory.under(s);
  if(u!=null){pose.pushPose();pose.translate(0,a[0]/16,a[1]/16);
   String n="acc_"+u.name().toLowerCase(java.util.Locale.ROOT);
   if(u.light()||u.laser()){DmtGearLayer.model(n+"_under",pose,buffers,light);DmtGearLayer.model(n+"_under_lens",pose,buffers,FULL);}
   else if(u==Accessory.Kind.BIPOD&&supported){pose.translate(.5,-.06,0);pose.mulPose(Axis.XP.rotationDegrees(-80));pose.translate(-.5,.06,0);DmtGearLayer.model(n,pose,buffers,light);}
   else DmtGearLayer.model(n,pose,buffers,light);
   pose.popPose();}
  var sd=Accessory.side(s);
  if(sd!=null){pose.pushPose();pose.translate((a[2]+.3F-8)/16,a[3]/16,a[4]/16);String n="acc_"+sd.name().toLowerCase(java.util.Locale.ROOT);DmtGearLayer.model(n,pose,buffers,light);DmtGearLayer.model(n+"_lens",pose,buffers,FULL);pose.popPose();}
 }
}
