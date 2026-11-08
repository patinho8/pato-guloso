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
 public static void register(ModelEvent.RegisterAdditional e){for(String part:new String[]{"body","view","moving","magazine"})e.register(ClientEvents.part(name(WeaponFamily.SNIPER,part,13)));for(var f:WeaponFamily.values()){for(int skin=0;skin<=12;skin++)for(String part:new String[]{"body","view","moving","magazine"})e.register(ClientEvents.part(name(f,part,skin)));for(int v=1;v<=3;v++)for(String part:new String[]{"optic","reticle","suppressor"})e.register(ClientEvents.part(name(f,part,v)));}e.register(ClientEvents.part(name(WeaponFamily.PYTHON,"rounds",0)));}
 public static String name(WeaponFamily f,String part,int n){return "armory21_"+f.id+"_"+part+"_"+n;}
 /** 0.72.0 (tools/gunsmith_072.py): muzzle z and bore height of each gun, iron sight line and the centre of each optic */
 static final double[] MUZZLE={-5.30,-3.30,-12.20,-23.20,-14.00,-8.00,-7.40,-49.80,-6.60,-6.00,-7.00,-3.30,-12.20,-7.40,-13.60,-3.30,-5.00,-12.00,-15.20,-15.20,-5.00,-12.60,-12.20,-13.40,-15.90};
 static final float[] BORE={10.60F,10.25F,9.60F,9.60F,10.00F,9.50F,10.00F,8.60F,9.60F,9.20F,9.90F,10.10F,9.70F,9.60F,10.00F,10.20F,9.20F,9.00F,9.60F,10.00F,9.60F,9.40F,8.80F,9.40F,9.40F};
 static final float[] IRON={13.30F,12.60F,13.50F,11.10F,11.60F,13.50F,13.50F,13.50F,13.50F,14.50F,12.10F,12.30F,13.50F,13.50F,13.50F,12.50F,13.50F,13.50F,13.50F,13.50F,13.50F,13.50F,13.50F,13.50F,13.50F};
 static final float[][] SIGHT={{14.15F,14.40F,14.70F},{13.45F,13.70F,14.00F},{13.85F,13.85F,13.85F},{13.60F,13.40F,13.90F},{13.40F,13.20F,13.50F},{13.85F,13.85F,13.85F},{13.85F,13.85F,13.90F},{15.70F,16.25F,16.80F},{13.85F,13.85F,13.85F},{15.55F,15.35F,15.65F},{12.85F,13.10F,13.40F},{13.15F,13.40F,13.70F},{13.90F,14.00F,14.29F},{13.85F,13.85F,13.85F},{13.85F,13.85F,13.85F},{13.35F,13.60F,13.90F},{13.85F,13.85F,13.85F},{15.55F,15.65F,15.94F},{14.60F,14.40F,14.90F},{14.90F,15.08F,15.26F},{13.85F,13.85F,13.85F},{13.85F,13.80F,14.09F},{13.85F,13.85F,13.89F},{13.85F,13.85F,13.85F},{13.85F,13.85F,13.85F}};
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
  if(slide<.88F){pose.pushPose();pose.translate(-slide*(outgoing?.28F:0F),-slide*(outgoing?.95F:.8F),slide*.04F);pose.translate(.5,0,magazineZ(f));pose.mulPose(Axis.ZP.rotationDegrees(outgoing?-slide*22:0));pose.translate(-.5,0,-magazineZ(f));PaintClient.model(s,f,"magazine",base+"magazine_"+skin,pose,buffers,light);pose.popPose();}
  }
  if(ModularLoadout.hasOptic(s)&&f!=WeaponFamily.SNIPER){int v=optic(s);PaintClient.model(s,f,"optic_"+v,base+"optic_"+v,pose,buffers,light);if(first)DmtGearLayer.model(base+"reticle_"+v,pose,buffers,15728880);}
  int suppressor=RifleLoadout.suppressor(s);if(suppressor>0){pose.pushPose();pose.translate(0,(bore(s,f)-10)/16,muzzle(s,f)/16);DmtGearLayer.model(base+"suppressor_"+Math.min(3,suppressor),pose,buffers,light);pose.popPose();}
  // 0.72.0: the Precision sniper has its own folding bipod in the model; the Hunter gets the bipod under its fore-end
  if(f==WeaponFamily.SNIPER&&RifleLoadout.bipod(s)&&s.is(MareEletrica.HUNTER.get())){pose.pushPose();pose.translate(0,BIPOD_Y,BIPOD_Z);DmtGearLayer.model(supported?"rifle_bipod_open":"rifle_bipod_folded",pose,buffers,light);pose.popPose();}
 }
}
