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
 static final double[] MUZZLE={-6.87,-3.30,-12.72,-23.20,-14.00,-8.00,-7.40,-49.80,-6.60,-6.00,-7.00,-3.30,-12.20,-7.40,-13.60,-3.30,-5.00,-12.00,-15.20,-15.20,-5.00,-12.60,-12.20,-13.40,-15.90};
 static final float[] BORE={9.60F,10.25F,8.40F,9.60F,10.00F,9.50F,10.00F,8.60F,9.60F,9.20F,9.90F,10.10F,9.70F,9.60F,10.00F,10.20F,9.20F,9.00F,9.60F,10.00F,9.60F,9.40F,8.80F,9.40F,9.40F};
 static final float[] IRON={12.40F,12.60F,10.90F,11.10F,11.60F,13.50F,13.50F,13.50F,13.50F,14.50F,12.10F,12.30F,13.50F,13.50F,13.50F,12.50F,13.50F,13.50F,13.50F,13.50F,13.50F,13.50F,13.50F,13.50F,13.50F};
 static final float[][] SIGHT={{13.02F,13.27F,13.57F},{13.45F,13.70F,14.00F},{11.48F,11.58F,11.87F},{13.60F,13.40F,13.90F},{13.40F,13.20F,13.50F},{13.85F,13.85F,13.85F},{13.85F,13.85F,13.90F},{15.70F,16.25F,16.80F},{13.85F,13.85F,13.85F},{15.55F,15.35F,15.65F},{12.85F,13.10F,13.40F},{13.15F,13.40F,13.70F},{13.90F,14.00F,14.29F},{13.85F,13.85F,13.85F},{13.85F,13.85F,13.85F},{13.35F,13.60F,13.90F},{13.85F,13.85F,13.85F},{15.55F,15.65F,15.94F},{14.60F,14.40F,14.90F},{14.90F,15.08F,15.26F},{13.85F,13.85F,13.85F},{13.85F,13.80F,14.09F},{13.85F,13.85F,13.89F},{13.85F,13.85F,13.85F},{13.85F,13.85F,13.85F}};
 /** 0.74.0 (tools/acc_anchors.py): under y,z | side x,y,z | drum y,z | magazine top */
 static final float[][] ACC={{8.48F,-5.47F,8.6F,9.8F,-6.97F,0.29F,12.45F,7.46F},{7.0F,-1.9F,8.6F,10.45F,-3.4F,1.2F,11.15F,6.1F},{7.35F,-1.88F,9.02F,8.6F,-3.38F,3.94F,7.35F,5.96F},{8.1F,-11.19F,9.4F,9.8F,-12.69F,7.0F,-0.07F,7.46F},{7.0F,-8.12F,8.55F,10.2F,-9.62F,0F,-4.2F,0F},{6.8F,-2.38F,9.55F,9.7F,-3.88F,5.8F,3.78F,7.0F},{8.2F,-2.27F,9.8F,10.2F,-3.77F,1.6F,2.8F,1.8F},{0F,0F,0F,0F,0F,0F,0F,0F},{8.35F,0.27F,9.3F,9.8F,-1.23F,4.8F,5.9F,7.0F},{6.6F,-3.48F,8.5F,9.4F,-4.98F,2.0F,4.0F,12.9F},{9.1F,-5.6F,8.6F,10.1F,-7.1F,0F,-2.1F,0F},{7.0F,-1.9F,8.6F,10.3F,-3.4F,1.2F,11.15F,6.1F},{7.45F,-2.93F,10.21F,9.9F,-4.43F,4.6F,5.4F,7.6F},{6.5F,-1.13F,9.55F,9.8F,-2.63F,5.6F,4.8F,6.8F},{6.6F,-7.89F,9.12F,10.2F,-9.39F,0F,-4.08F,0F},{7.0F,-1.9F,8.6F,10.4F,-3.4F,1.2F,11.15F,6.1F},{7.6F,1.12F,9.4F,9.4F,-0.38F,1.0F,11.3F,6.4F},{5.2F,-1.98F,9.6F,9.2F,-3.48F,5.6F,14.6F,6.4F},{7.25F,-5.0F,8.55F,9.8F,-6.5F,5.8F,4.6F,8.0F},{7.65F,-4.16F,9.8F,10.2F,-5.66F,5.2F,5.5F,6.8F},{4.8F,1.12F,9.3F,9.8F,-0.38F,0.6F,11.3F,6.4F},{7.1F,-5.59F,9.6F,9.6F,-7.09F,7.4F,2.74F,7.4F},{7.95F,-2.1F,8.5F,9.0F,-3.6F,4.6F,14.6F,6.4F},{7.2F,-3.92F,9.93F,9.6F,-5.42F,6.4F,4.78F,7.4F},{7.6F,-4.9F,9.54F,9.6F,-6.4F,6.6F,4.47F,7.6F}};
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
 public static float magazineZ(WeaponFamily f){return f.pistol()?.75F:f==WeaponFamily.SMG?.25F:.37F;}
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
