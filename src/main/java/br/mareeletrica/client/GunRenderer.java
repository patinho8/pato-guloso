package br.mareeletrica.client;
import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
public final class GunRenderer {
 public static float muzzleZ(WeaponProfile p){return switch(p){case DESERT_EAGLE->3.4F;case USP->4.2F;case M4A1->-9F;case AK47->-9.5F;case M870->-13F;case MP5->-4.5F;case MP40->-5F;case P90->-5F;case PYTHON->3.4F;case GLOCK->4.2F;case SCAR->-9F;case UMP45->-4.5F;case SPAS12->-13F;case BERETTA->4.2F;case UZI->-4.5F;case FAMAS->-9F;case M249->-11F;case BARRETT->-13F;case MAC10->-2.5F;case GALIL->-9.5F;case AUG->-7F;case SAIGA12->-9F;case SVD->-14F;};}
 public static void parts(ItemStack stack,PoseStack pose,MultiBufferSource buffers,int light,boolean firstPerson){var p=WeaponProfile.of(stack);if(p==null)return;int skin=GunLoadout.skin(stack);String name="arsenal_"+p.id+"_";float kick=firstPerson?SniperClient.kick:0,reload=firstPerson?SniperClient.reloadAmount(stack):0;
 DmtGearLayer.model(RifleRenderer.part(name+"body",skin),pose,buffers,light);pose.pushPose();pose.translate(0,0,kick*(p==WeaponProfile.M870?.19:.11));DmtGearLayer.model(RifleRenderer.part(name+"moving",skin),pose,buffers,light);pose.popPose();pose.pushPose();pose.translate(reload*.08,-reload*.24,reload*.10);DmtGearLayer.model(RifleRenderer.part(name+"magazine",skin),pose,buffers,light);pose.popPose();
 if(GunLoadout.hasOptic(stack)){pose.pushPose();pose.translate(0,p.pistol?-.10:0,p.pistol?.10:0);DmtGearLayer.model("arsenal_optic_"+GunLoadout.zoom(stack),pose,buffers,light);pose.popPose();}int silencer=RifleLoadout.suppressor(stack);if(silencer>0){pose.pushPose();pose.translate(.5,p.pistol?.52:.56,muzzleZ(p)/16);float scale=p.pistol?.65F:.85F;pose.scale(scale,scale,scale);pose.translate(-.5,-.56,10.5/16.0);DmtGearLayer.model("rifle_silencer_"+silencer,pose,buffers,light);pose.popPose();}}
}
