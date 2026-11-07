package br.mareeletrica.client;

import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/** Draws the camouflage clothes worn in the extra slots, over vanilla and modded armor. */
public final class CamoLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
 private static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
 public CamoLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
 @SuppressWarnings({"unchecked","rawtypes"})
 @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,float limbSwing,float limbAmount,float partial,float age,float headYaw,float headPitch){
  if(player.isInvisible())return;
  var gear=CamoEquipment.inventory(player);
  for(int i=0;i<4;i++){
   var stack=gear.getStackInSlot(i);
   if(!(stack.getItem() instanceof CamoArmorItem c))continue;
   HumanoidModel model=CamoModel.overlay(SLOTS[i]);
   getParentModel().copyPropertiesTo(model);
   var texture=ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/models/armor/camo_"+c.biome+".png");
   model.renderToBuffer(pose,buffers.getBuffer(RenderType.armorCutoutNoCull(texture)),light,OverlayTexture.NO_OVERLAY,-1);
  }
 }
}
