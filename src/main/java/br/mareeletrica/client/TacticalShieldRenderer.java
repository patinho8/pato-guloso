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

/** 0.87.0 (model from tools/shield_gen.py): curved riot shield — angled wings, steel frame, real see-through visor, POLÍCIA lettering, reflective band, bolts, flashlight, foam back, grip and arm strap.
 *  0.86.0: the tactical shield has its own model — a tall black ballistic panel with a steel frame, a glass viewport,
 *  a white "POLÍCIA"-style band, bolts and an arm strap behind (nothing in common with the wooden shield). */
public final class TacticalShieldRenderer extends BlockEntityWithoutLevelRenderer {
 static final ResourceLocation TEX=ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/entity/tactical_shield.png");
 private static ModelPart model;
 TacticalShieldRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 @Override public void renderByItem(ItemStack s,ItemDisplayContext ctx,PoseStack pose,MultiBufferSource buf,int light,int overlay){
  if(model==null)model=TacticalShieldModel.build();
  ModelPart glass=model.getChild("glass"),wr=model.getChild("wr"),lens=wr.getChild("lens");
  pose.pushPose();if(ctx.firstPerson()){pose.translate(.05,-.05,0);pose.scale(.86F,.86F,.86F);}pose.scale(1F,-1F,-1F);pose.translate(0,-.12,0);
  // 1) body: panel, steel frame, foam, grip, strap, flashlight
  glass.visible=false;lens.visible=false;
  model.render(pose,ItemRenderer.getFoilBufferDirect(buf,RenderType.entityCutoutNoCull(TEX),true,s.hasFoil()),light,overlay);
  // 2) see-through visor
  glass.visible=true;glass.render(pose,buf.getBuffer(RenderType.entityTranslucent(TEX)),light,overlay);
  // 3) the flashlight lens glows
  lens.visible=true;pose.pushPose();wr.translateAndRotate(pose);lens.render(pose,buf.getBuffer(RenderType.entityCutoutNoCull(TEX)),net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,overlay);pose.popPose();
  pose.popPose();}
 public static void register(RegisterClientExtensionsEvent e){
  e.registerItem(new IClientItemExtensions(){private TacticalShieldRenderer r;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(r==null)r=new TacticalShieldRenderer();return r;}},Wounds.TAC_SHIELD.get());}
 public static void properties(){
  ItemProperties.register(Wounds.TAC_SHIELD.get(),ResourceLocation.withDefaultNamespace("blocking"),(s,l,e,i)->e!=null&&e.isUsingItem()&&e.getUseItem()==s?1F:0F);}
}
