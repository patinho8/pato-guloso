package br.mareeletrica.client;
import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;
/** Both skin arms track the actual grip points while raising the rifle and recoiling. */
public final class RifleHands {
 /** 0.76.0: guns ~30% bigger in first person, grown around the grip so the hand stays put */
 /** 0.80.2 (tools/hand_size.py): per-gun size, one per WeaponFamily (pistols were ~1.5x too big next to the rifles) */
 static final float[] SIZE={0.713F,0.686F,1.0F,1.0F,1.0F,0.977F,1.0F,1.0F,1.0F,1.0F,0.758F,0.735F,1.0F,1.0F,1.0F,0.771F,0.942F,1.0F,1.0F,1.0F,0.78F,1.0F,1.0F,1.0F,1.0F};
 static final float BIG=.85F,GY=.23F-.5F,GZ=.76F-.9F;
 public static void render(RenderHandEvent e){var mc=Minecraft.getInstance();var player=mc.player;if(player==null)return;var gun=player.getMainHandItem();ReloadPath.cur(gun);var f=WeaponFamily.of(gun);if(f==null)return;boolean pistol=f.pistol(),right=player.getMainArm()==HumanoidArm.RIGHT;float side=right?1:-1;float progress=ArmoryRenderer.progress(gun,e.getPartialTick()),reload=ArmoryRenderer.drop(progress);boolean reloading=Magazines.reloading(gun);float reloadPose=reloading?ArmoryRenderer.reloadPose(progress):0;float aim=SniperClient.aimAmount(e.getPartialTick()),smooth=aim*aim*(3-2*aim),kick=SniperClient.kick;
  float scale=pistol?.62F:.66F,sight=ArmoryRenderer.sightY(gun),big=BIG*(f.ordinal()<SIZE.length?SIZE[f.ordinal()]:1F),aimY=-(GY+big*(sight/16F-.5F-GY))*scale;
  float walk=(float)Math.min(1,player.getDeltaMovement().horizontalDistance()*(ProneSystem.prone(player)?26:8)),phase=(player.tickCount+e.getPartialTick())*.72F;
  float swayX=Mth.sin(phase)*walk*.012F,swayY=Mth.cos(phase*2)*walk*.008F;
  float[] ins=InspectClient.gunPose(f,e.getPartialTick(),smooth>.05F||reloadPose>.01F||kick>.01F);
  float[] rg=reloading?ReloadPath.gun(f,progress):new float[]{-.14F,.32F,-.12F,28F,6F,0F};   // 0.79.0: each gun type has its own reload pose (and weight)
  var rifle=new Matrix4f().translation(side*(Mth.lerp(smooth,.30F,0)+reloadPose*rg[0])+swayX+LeanClient.local()*.05F,Mth.lerp(smooth,-.37F,aimY)+swayY+reloadPose*rg[1],Mth.lerp(smooth,pistol?-.68F:-.80F,pistol?-.83F:-1.06F)+reloadPose*rg[2]+kick*(f==WeaponFamily.DESERT_EAGLE?.31F:.17F))
   .translate(side*ins[0],ins[1],ins[2]).rotateY(side*ins[4]).rotateX(ins[3]).rotateZ(side*ins[5])
   .rotateY((float)Math.toRadians(side*reloadPose*rg[5]))
   .rotateZ((float)Math.toRadians(-side*reloadPose*rg[3]-LeanClient.local()*5F))
   .rotateX((float)Math.toRadians(kick*(f==WeaponFamily.DESERT_EAGLE?42F:f==WeaponFamily.PYTHON?34F:f==WeaponFamily.SPAS12?20F:f==WeaponFamily.BARRETT?26F:pistol?15F:11F)+reloadPose*rg[4]+(f==WeaponFamily.PYTHON&&reloading?ReloadPath.pyTilt(progress)*12F:0F)))
   .scale(scale).translate(0,GY,GZ).scale(big).translate(0,-GY,-GZ).translate(-.5F,-.5F,-.9F);
  var pose=e.getPoseStack();var buffers=e.getMultiBufferSource();int light=e.getPackedLight();
  if(!player.isInvisible()&&mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer){var model=renderer.getModel();var grip=rifle.transformPosition(new Vector3f(.5F,.23F,.76F));var support=new Vector3f(.5F,pistol?.24F:.42F,pistol?.69F:f==WeaponFamily.SMG?.1F:-.12F);{float[] sp=ArmoryRenderer.support(f);if(sp!=null)support.set(.5F,sp[0]/16,sp[1]/16);}
   if(reloading&&ReloadPath.special(f)){support.set(ReloadPath.hand(f,progress,support));}
   else if(reloading&&ReloadPath.choreo(f)){support.set(ReloadPath.hand2(f,progress,support));}
   else if(reloading){float reach=Math.min(1,progress/.12F);support.lerp(new Vector3f(.5F-reload*.28F,.12F-reload*.36F,ArmoryRenderer.magazineZ(f)+reload*.04F),reach);}
   rifle.transformPosition(support);arm(pose,buffers,light,right?model.rightArm:model.leftArm,right?model.rightSleeve:model.leftSleeve,right,player.isModelPartShown(right?PlayerModelPart.RIGHT_SLEEVE:PlayerModelPart.LEFT_SLEEVE),new Vector3f(side*.42F,-.59F,.12F),grip);
   if(f!=WeaponFamily.DESERT_EAGLE||reloading)arm(pose,buffers,light,right?model.leftArm:model.rightArm,right?model.leftSleeve:model.rightSleeve,!right,player.isModelPartShown(right?PlayerModelPart.LEFT_SLEEVE:PlayerModelPart.RIGHT_SLEEVE),new Vector3f(-side*.35F,-.64F,.04F),support);
  }
  pose.pushPose();pose.mulPose(rifle);RifleRenderer.parts(gun,pose,buffers,light,ProneSystem.supported(player),true);
  if(reloading&&ReloadPath.shellInHand(f,progress)){var sp=ArmoryRenderer.support(f);var h=f==WeaponFamily.PYTHON?ReloadPath.hand(f,progress,new Vector3f(.5F,sp!=null?sp[0]/16:.42F,sp!=null?sp[1]/16:-.12F)):ReloadPath.hand2(f,progress,new Vector3f(.5F,sp!=null?sp[0]/16:.42F,sp!=null?sp[1]/16:-.12F));
   // 0.80.0: a small 3D round / shell between the fingers (model centred on 8,8,8 px), pointing forward
   pose.pushPose();pose.translate(h.x,h.y+.06F,h.z-.02F);if(f==WeaponFamily.PYTHON)pose.scale(1.4F,1.4F,1.4F);pose.translate(-.5F,-.5F,-.5F);
   DmtGearLayer.model(f==WeaponFamily.PYTHON?"loose_round":"loose_shell",pose,buffers,light);pose.popPose();}
  if(SniperClient.flashTicks>0&&RifleLoadout.suppressor(gun)==0){pose.pushPose();pose.translate(0,(ArmoryRenderer.bore(gun,f)-8.9)/16,(ArmoryRenderer.muzzle(gun,f)+10.5)/16);DmtGearLayer.model("gun_flash",pose,buffers,15728880);pose.popPose();}
  if(kick>.08F){pose.pushPose();pose.translate((1-kick)*.6,.1+kick*.16,.05);pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(kick*180));DmtGearLayer.model("gun_case",pose,buffers,light);pose.popPose();}pose.popPose();
 }
 static void arm(PoseStack pose,MultiBufferSource buffers,int light,ModelPart arm,ModelPart sleeve,boolean right,boolean showSleeve,Vector3f shoulder,Vector3f hand){arm(pose,buffers,light,arm,sleeve,right,showSleeve,shoulder,hand,.68F);}
 static void arm(PoseStack pose,MultiBufferSource buffers,int light,ModelPart arm,ModelPart sleeve,boolean right,boolean showSleeve,Vector3f shoulder,Vector3f hand,float thick){var direction=new Vector3f(hand).sub(shoulder);float length=direction.length();var skin=Minecraft.getInstance().player.getSkin().texture();pose.pushPose();pose.translate(shoulder.x,shoulder.y,shoulder.z);pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,1,0),direction.normalize()));pose.scale(thick,length/.75F,thick);pose.translate(right?.0625:-.0625,.125,0);var oldArm=arm.storePose();var oldSleeve=sleeve.storePose();try{arm.loadPose(PartPose.ZERO);sleeve.loadPose(PartPose.ZERO);arm.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),light,OverlayTexture.NO_OVERLAY);if(showSleeve)sleeve.render(pose,buffers.getBuffer(RenderType.entityTranslucent(skin)),light,OverlayTexture.NO_OVERLAY);}finally{arm.loadPose(oldArm);sleeve.loadPose(oldSleeve);pose.popPose();}}
}
