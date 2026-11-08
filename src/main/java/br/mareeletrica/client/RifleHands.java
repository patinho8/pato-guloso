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
 public static void render(RenderHandEvent e){var mc=Minecraft.getInstance();var player=mc.player;if(player==null)return;var gun=player.getMainHandItem();var f=WeaponFamily.of(gun);if(f==null)return;boolean pistol=f.pistol(),right=player.getMainArm()==HumanoidArm.RIGHT;float side=right?1:-1;float progress=ArmoryRenderer.progress(gun,e.getPartialTick()),reload=ArmoryRenderer.drop(progress);boolean reloading=Magazines.reloading(gun);float reloadPose=reloading?ArmoryRenderer.reloadPose(progress):0;float aim=SniperClient.aimAmount(e.getPartialTick()),smooth=aim*aim*(3-2*aim),kick=SniperClient.kick;
  float scale=pistol?.62F:.66F,sight=ArmoryRenderer.sightY(gun),aimY=-(sight/16F-.5F)*scale;
  float walk=(float)Math.min(1,player.getDeltaMovement().horizontalDistance()*(ProneSystem.prone(player)?26:8)),phase=(player.tickCount+e.getPartialTick())*.72F;
  float swayX=Mth.sin(phase)*walk*.012F,swayY=Mth.cos(phase*2)*walk*.008F;
  float[] ins=InspectClient.gunPose(f,e.getPartialTick(),smooth>.05F||reloadPose>.01F||kick>.01F);
  var rifle=new Matrix4f().translation(side*(Mth.lerp(smooth,.30F,0)-reloadPose*.14F)+swayX+LeanClient.local()*.05F,Mth.lerp(smooth,-.37F,aimY)+swayY+reloadPose*.32F,Mth.lerp(smooth,pistol?-.68F:-.80F,pistol?-.83F:-1.06F)-reloadPose*.12F+kick*(f==WeaponFamily.DESERT_EAGLE?.31F:.17F))
   .translate(side*ins[0],ins[1],ins[2]).rotateY(side*ins[4]).rotateX(ins[3]).rotateZ(side*ins[5])
   .rotateZ((float)Math.toRadians(-side*reloadPose*28-LeanClient.local()*5F))
   .rotateX((float)Math.toRadians(kick*(f==WeaponFamily.DESERT_EAGLE?42F:f==WeaponFamily.PYTHON?34F:f==WeaponFamily.SPAS12?20F:f==WeaponFamily.BARRETT?26F:pistol?15F:11F)+reloadPose*6F+(f==WeaponFamily.PYTHON&&reloading?ReloadPath.pyTilt(progress)*26F:0F)))
   .scale(scale).translate(-.5F,-.5F,-.9F);
  var pose=e.getPoseStack();var buffers=e.getMultiBufferSource();int light=e.getPackedLight();
  if(!player.isInvisible()&&mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer){var model=renderer.getModel();var grip=rifle.transformPosition(new Vector3f(.5F,.23F,.76F));var support=new Vector3f(.5F,pistol?.24F:.42F,pistol?.69F:f==WeaponFamily.SMG?.1F:-.12F);
   if(reloading&&ReloadPath.special(f)){support.set(ReloadPath.hand(f,progress,support));}
   else if(reloading){float reach=Math.min(1,progress/.12F);support.lerp(new Vector3f(.5F-reload*.28F,.12F-reload*.36F,ArmoryRenderer.magazineZ(f)+reload*.04F),reach);}
   rifle.transformPosition(support);arm(pose,buffers,light,right?model.rightArm:model.leftArm,right?model.rightSleeve:model.leftSleeve,right,player.isModelPartShown(right?PlayerModelPart.RIGHT_SLEEVE:PlayerModelPart.LEFT_SLEEVE),new Vector3f(side*.42F,-.59F,.12F),grip);
   if(f!=WeaponFamily.DESERT_EAGLE||reloading)arm(pose,buffers,light,right?model.leftArm:model.rightArm,right?model.leftSleeve:model.rightSleeve,!right,player.isModelPartShown(right?PlayerModelPart.LEFT_SLEEVE:PlayerModelPart.RIGHT_SLEEVE),new Vector3f(-side*.35F,-.64F,.04F),support);
  }
  pose.pushPose();pose.mulPose(rifle);RifleRenderer.parts(gun,pose,buffers,light,ProneSystem.supported(player),true);
  if(SniperClient.flashTicks>0&&RifleLoadout.suppressor(gun)==0){pose.pushPose();pose.translate(0,(ArmoryRenderer.bore(gun,f)-8.9)/16,(ArmoryRenderer.muzzle(gun,f)+10.5)/16);DmtGearLayer.model("gun_flash",pose,buffers,15728880);pose.popPose();}
  if(kick>.08F){pose.pushPose();pose.translate((1-kick)*.6,.1+kick*.16,.05);pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(kick*180));DmtGearLayer.model("gun_case",pose,buffers,light);pose.popPose();}pose.popPose();
 }
 static void arm(PoseStack pose,MultiBufferSource buffers,int light,ModelPart arm,ModelPart sleeve,boolean right,boolean showSleeve,Vector3f shoulder,Vector3f hand){arm(pose,buffers,light,arm,sleeve,right,showSleeve,shoulder,hand,.68F);}
 static void arm(PoseStack pose,MultiBufferSource buffers,int light,ModelPart arm,ModelPart sleeve,boolean right,boolean showSleeve,Vector3f shoulder,Vector3f hand,float thick){var direction=new Vector3f(hand).sub(shoulder);float length=direction.length();var skin=Minecraft.getInstance().player.getSkin().texture();pose.pushPose();pose.translate(shoulder.x,shoulder.y,shoulder.z);pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,1,0),direction.normalize()));pose.scale(thick,length/.75F,thick);pose.translate(right?.0625:-.0625,.125,0);var oldArm=arm.storePose();var oldSleeve=sleeve.storePose();try{arm.loadPose(PartPose.ZERO);sleeve.loadPose(PartPose.ZERO);arm.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(skin)),light,OverlayTexture.NO_OVERLAY);if(showSleeve)sleeve.render(pose,buffers.getBuffer(RenderType.entityTranslucent(skin)),light,OverlayTexture.NO_OVERLAY);}finally{arm.loadPose(oldArm);sleeve.loadPose(oldSleeve);pose.popPose();}}
}
