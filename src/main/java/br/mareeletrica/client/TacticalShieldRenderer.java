package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Wounds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** 0.86.0: the tactical shield has its own model — a tall black ballistic panel with a steel frame, a glass viewport,
 *  a white "POLÍCIA"-style band, bolts and an arm strap behind (nothing in common with the wooden shield). */
public final class TacticalShieldRenderer extends BlockEntityWithoutLevelRenderer {
 static final ResourceLocation TEX=ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/entity/tactical_shield.png");
 private static ModelPart model;
 TacticalShieldRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 /** same coordinate frame as the vanilla shield (front at z = -2, -y = up), so the vanilla hand poses fit */
 static ModelPart build(){
  var mesh=new MeshDefinition();var r=mesh.getRoot();
  r.addOrReplaceChild("plate",CubeListBuilder.create().texOffs(0,0).addBox(-7F,-15F,-2F,14,30,1),PartPose.ZERO);
  r.addOrReplaceChild("rim_l",CubeListBuilder.create().texOffs(32,0).addBox(-8F,-15F,-2.5F,1,30,2),PartPose.ZERO);
  r.addOrReplaceChild("rim_r",CubeListBuilder.create().texOffs(40,0).addBox(7F,-15F,-2.5F,1,30,2),PartPose.ZERO);
  r.addOrReplaceChild("rim_t",CubeListBuilder.create().texOffs(0,32).addBox(-8F,-16F,-2.5F,16,1,2),PartPose.ZERO);
  r.addOrReplaceChild("rim_b",CubeListBuilder.create().texOffs(0,36).addBox(-8F,15F,-2.5F,16,1,2),PartPose.ZERO);
  r.addOrReplaceChild("visor",CubeListBuilder.create().texOffs(0,40).addBox(-5F,-13F,-2.4F,10,5,1),PartPose.ZERO);
  r.addOrReplaceChild("band",CubeListBuilder.create().texOffs(24,47).addBox(-7F,1F,-2.15F,14,3,1),PartPose.ZERO);
  r.addOrReplaceChild("handle",CubeListBuilder.create().texOffs(48,0).addBox(-1F,-3F,-1F,2,6,6),PartPose.ZERO);
  r.addOrReplaceChild("strap",CubeListBuilder.create().texOffs(40,52).addBox(-3F,6F,-1F,6,1,4),PartPose.ZERO);
  return LayerDefinition.create(mesh,64,64).bakeRoot();}
 @Override public void renderByItem(ItemStack s,ItemDisplayContext ctx,PoseStack pose,MultiBufferSource buf,int light,int overlay){
  if(model==null)model=build();
  pose.pushPose();pose.scale(1F,-1F,-1F);pose.translate(0,-.12,0);
  var vc=ItemRenderer.getFoilBufferDirect(buf,RenderType.entityCutoutNoCull(TEX),true,s.hasFoil());
  model.render(pose,vc,light,overlay);
  pose.popPose();}
 public static void register(RegisterClientExtensionsEvent e){
  e.registerItem(new IClientItemExtensions(){private TacticalShieldRenderer r;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(r==null)r=new TacticalShieldRenderer();return r;}},Wounds.TAC_SHIELD.get());}
 public static void properties(){
  ItemProperties.register(Wounds.TAC_SHIELD.get(),ResourceLocation.withDefaultNamespace("blocking"),(s,l,e,i)->e!=null&&e.isUsingItem()&&e.getUseItem()==s?1F:0F);}
}
