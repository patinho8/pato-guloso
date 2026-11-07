package br.mareeletrica.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;

/** Item-model ball drawn centred on its hitbox (vanilla draws thrown items at the feet). */
public final class BallRenderer<T extends Entity & ItemSupplier> extends ThrownItemRenderer<T> {
 private final float lift;
 public BallRenderer(EntityRendererProvider.Context c,float scale,float lift){super(c,scale,false);this.lift=lift;}
 @Override public void render(T e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){p.pushPose();p.translate(0,lift,0);super.render(e,yaw,pt,p,b,light);p.popPose();}
}
