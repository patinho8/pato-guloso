package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Wounds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayers;
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

/** 0.85.0: the tactical shield — the vanilla shield shape, taller, black with a viewing window. */
public final class TacticalShieldRenderer extends BlockEntityWithoutLevelRenderer {
 static final ResourceLocation TEX=ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/entity/tactical_shield.png");
 private ShieldModel model;
 TacticalShieldRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 @Override public void renderByItem(ItemStack s,ItemDisplayContext ctx,PoseStack pose,MultiBufferSource buf,int light,int overlay){
  if(model==null)model=new ShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SHIELD));
  pose.pushPose();pose.scale(1F,-1F,-1F);
  var vc=ItemRenderer.getFoilBufferDirect(buf,model.renderType(TEX),true,s.hasFoil());
  model.handle().render(pose,vc,light,overlay);
  pose.pushPose();pose.translate(0,.06,0);pose.scale(1.12F,1.25F,1.4F);model.plate().render(pose,vc,light,overlay);pose.popPose();
  pose.popPose();}
 public static void register(RegisterClientExtensionsEvent e){
  e.registerItem(new IClientItemExtensions(){private TacticalShieldRenderer r;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(r==null)r=new TacticalShieldRenderer();return r;}},Wounds.TAC_SHIELD.get());}
 public static void properties(){
  ItemProperties.register(Wounds.TAC_SHIELD.get(),ResourceLocation.withDefaultNamespace("blocking"),(s,l,e,i)->e!=null&&e.isUsingItem()&&e.getUseItem()==s?1F:0F);}
}
