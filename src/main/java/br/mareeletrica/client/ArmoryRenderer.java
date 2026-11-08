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
 public static double muzzle(WeaponFamily f){return new double[]{-5,-3,-12,-23,-14,-8,-6,-15,-6,-5,-7,-3,-12,-6,-14,-3,-5,-12,-15,-15,-5,-12,-9,-11,-16}[f.ordinal()];}
 public static int optic(ItemStack s){return Math.clamp(RifleLoadout.tag(s).getInt("OpticVariant"),1,3);}
 public static float sightY(ItemStack s){var f=WeaponFamily.of(s);if(f==null)return 13.5F;if(!ModularLoadout.hasOptic(s))return f==WeaponFamily.AK47?11.1F:f==WeaponFamily.DESERT_EAGLE?13.3F:f==WeaponFamily.USP?12.6F:f==WeaponFamily.PYTHON?12.1F:f==WeaponFamily.GLOCK?12.3F:f==WeaponFamily.BERETTA?12.5F:13.5F;return new float[]{14.4F,13.7F,14.8F,14.5F,14.2F,14.6F,14.4F,15.7F,14.4F,14.9F,13.9F,13.4F,14.8F,14.6F,14.2F,13.6F,14.4F,14.8F,14.6F,16.0F,14.4F,14.5F,14.8F,14.5F,15.7F}[f.ordinal()]+(optic(s)-1)*.55F;}
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
  if(ModularLoadout.hasOptic(s)){int v=optic(s);PaintClient.model(s,f,"optic_"+v,base+"optic_"+v,pose,buffers,light);if(first)DmtGearLayer.model(base+"reticle_"+v,pose,buffers,15728880);}
  int suppressor=RifleLoadout.suppressor(s);if(suppressor>0){pose.pushPose();pose.translate(0,0,muzzle(f)/16);DmtGearLayer.model(base+"suppressor_"+Math.min(3,suppressor),pose,buffers,light);pose.popPose();}
  if(f==WeaponFamily.SNIPER&&RifleLoadout.bipod(s))DmtGearLayer.model(supported?"rifle_bipod_open":"rifle_bipod_folded",pose,buffers,light);
 }
}
