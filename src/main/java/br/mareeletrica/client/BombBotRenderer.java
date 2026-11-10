package br.mareeletrica.client;

import br.mareeletrica.BombBot;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/** 0.83.0: Bomb-mode bots look like players (default skins), with the team's vest and the gun held up, aimed. */
public final class BombBotRenderer extends HumanoidMobRenderer<BombBot,PlayerModel<BombBot>> {
 static final String[] SKINS={"steve","alex","ari","efe","kai","makena","noor","sunny","zuri"};
 static final ResourceLocation[] TEX=new ResourceLocation[SKINS.length];
 static{for(int i=0;i<SKINS.length;i++)TEX[i]=ResourceLocation.withDefaultNamespace("textures/entity/player/wide/"+SKINS[i]+".png");}
 public BombBotRenderer(EntityRendererProvider.Context c){
  super(c,new Model(c.bakeLayer(ModelLayers.PLAYER)),.5F);
  addLayer(new HumanoidArmorLayer<>(this,new HumanoidArmorModel<>(c.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),new HumanoidArmorModel<>(c.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),c.getModelManager()));
 }
 @Override public ResourceLocation getTextureLocation(BombBot b){return TEX[Math.floorMod(b.getEntityData().get(BombBot.SKIN),TEX.length)];}
 static final class Model extends PlayerModel<BombBot> {
  Model(net.minecraft.client.model.geom.ModelPart root){super(root,false);}
  @Override public void setupAnim(BombBot b,float limb,float limbAmount,float age,float headYaw,float headPitch){
   // both arms up, holding the gun to the shoulder; working (planting/defusing) = crouched over the bomb
   boolean working=b.getEntityData().get(BombBot.WORK)>0;
   rightArmPose=working?HumanoidModel.ArmPose.EMPTY:HumanoidModel.ArmPose.BOW_AND_ARROW;leftArmPose=HumanoidModel.ArmPose.EMPTY;crouching=working;
   super.setupAnim(b,limb,limbAmount,age,headYaw,headPitch);
   if(working){rightArm.xRot=-1.1F+(float)Math.sin(age*.6F)*.15F;leftArm.xRot=-1.0F;rightArm.yRot=-.2F;leftArm.yRot=.2F;}
  }
 }
}
