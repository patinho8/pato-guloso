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
 static final double[] MUZZLE={-14.25,-6.82,-25.66,-23.20,-32.36,-19.02,-6.44,-49.80,-14.86,-5.90,-15.00,-6.04,-26.24,-14.07,-24.44,-7.05,-1.76,-11.66,-28.11,-44.75,5.26,-21.20,-15.62,-22.64,-33.55};
 static final float[] BORE={11.20F,9.07F,9.88F,9.60F,8.12F,9.99F,10.64F,8.60F,9.63F,7.04F,10.98F,9.07F,9.49F,9.78F,8.91F,8.96F,9.34F,8.84F,8.29F,9.12F,8.77F,8.70F,9.92F,8.77F,8.48F};
 static final float[] IRON={15.12F,11.93F,13.72F,11.10F,10.78F,13.45F,13.91F,13.50F,12.30F,11.99F,13.50F,11.82F,13.26F,13.95F,11.39F,11.76F,12.13F,14.10F,12.19F,12.54F,11.27F,12.03F,13.01F,12.25F,10.18F};
 static final float[][] SIGHT={{15.58F,15.83F,16.13F},{12.26F,12.51F,12.81F},{14.07F,14.07F,14.23F},{13.60F,13.40F,13.90F},{11.35F,11.15F,11.45F},{14.70F,14.50F,14.80F},{14.30F,14.26F,14.60F},{15.70F,16.25F,16.80F},{12.67F,12.65F,12.77F},{12.86F,12.66F,12.96F},{13.85F,14.02F,14.32F},{12.20F,12.45F,12.75F},{13.61F,13.61F,13.90F},{14.38F,14.30F,14.48F},{11.73F,11.74F,11.83F},{12.20F,12.45F,12.75F},{13.03F,12.83F,13.13F},{14.97F,15.07F,15.36F},{12.67F,12.54F,12.97F},{13.22F,13.40F,13.58F},{12.16F,11.96F,12.26F},{12.43F,12.53F,12.82F},{13.36F,13.40F,13.69F},{12.65F,12.60F,12.75F},{11.83F,12.01F,12.19F}};
 /** 0.74.0 (tools/acc_anchors.py): under y,z | side x,y,z | drum y,z | magazine top */
 static final float[][] ACC={{9.63F,-12.85F,8.6F,11.4F,-14.35F,-1.23F,10.65F,7.62F},{4.59F,-5.42F,8.6F,9.27F,-6.92F,-7.06F,11.66F,5.71F},{8.28F,-9.06F,9.8F,10.08F,-10.56F,3.05F,5.09F,6.15F},{8.1F,-11.19F,9.4F,9.8F,-12.69F,7.0F,-0.07F,7.46F},{5.97F,-18.77F,8.79F,8.32F,-20.27F,0F,-9.71F,0F},{7.33F,-8.57F,10.1F,10.19F,-10.07F,5.02F,0.96F,5.64F},{9.2F,-0.61F,9.24F,10.84F,-2.11F,1.87F,4.76F,2.36F},{0F,0F,0F,0F,0F,0F,0F,0F},{8.34F,-6.52F,8.66F,9.83F,-8.02F,4.02F,0.62F,4.63F},{5.02F,-1.56F,8.96F,7.24F,-3.06F,-0.38F,10.34F,10.57F},{8.33F,-13.6F,8.6F,11.18F,-15.1F,0F,-4.5F,0F},{4.59F,-4.64F,8.6F,9.27F,-6.14F,-7.17F,11.66F,5.71F},{7.4F,-9.35F,9.73F,9.69F,-10.85F,2.94F,4.91F,3.48F},{7.18F,-6.14F,10.11F,9.98F,-7.64F,5.46F,1.38F,5.85F},{6.74F,-14.18F,9.12F,9.11F,-15.68F,0F,-7.33F,0F},{4.93F,-5.65F,8.6F,9.16F,-7.15F,-7.28F,11.71F,5.6F},{7.33F,2.95F,9.32F,9.54F,1.45F,-2.18F,11.64F,5.53F},{5.6F,-0.77F,9.87F,9.04F,-2.27F,3.08F,17.77F,3.48F},{6.05F,-11.2F,8.65F,8.49F,-12.7F,5.57F,3.98F,7.39F},{6.56F,-19.07F,8.97F,9.32F,-20.57F,3.68F,1.47F,4.19F},{3.84F,7.18F,8.81F,8.97F,5.68F,-2.39F,11.6F,5.46F},{6.46F,-8.02F,9.59F,8.9F,-9.52F,5.6F,4.35F,6.0F},{9.39F,-3.05F,8.68F,10.12F,-4.55F,3.44F,18.39F,3.98F},{7.11F,-7.9F,9.12F,8.97F,-9.4F,5.6F,5.14F,6.18F},{6.88F,-11.79F,9.16F,8.68F,-13.29F,5.6F,5.08F,5.95F}};
 /** 0.75.0 (tools/gunforge.py): where the support hand holds each remodelled gun (y,z px); 0,0 = the default spot */
 static final float[][] SUPPORT={{0F,0F},{0F,0F},{0F,0F},{0F,0F},{5.24F,-9.28F},{0F,0F},{6.68F,1.88F},{0F,0F},{0F,0F},{4.52F,4.58F},{0F,0F},{0F,0F},{0F,0F},{0F,0F},{5.82F,-5.32F},{0F,0F},{7.18F,4.04F},{5.96F,2.06F},{5.41F,-5.04F},{7.52F,-14.64F},{5.02F,8.18F},{0F,0F},{6.32F,1.34F},{0F,0F},{0F,0F}};
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
