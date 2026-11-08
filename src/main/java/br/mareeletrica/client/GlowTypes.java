package br.mareeletrica.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** 0.72.0: additive light that never writes depth, so overlapping glows from many lamps just add up (no flicker, no dark patches). */
public final class GlowTypes extends RenderType {
 private GlowTypes(String n,VertexFormat f,VertexFormat.Mode m,int b,boolean c,boolean s,Runnable a,Runnable z){super(n,f,m,b,c,s,a,z);}
 /** light painted on block faces: pulled slightly towards the camera so it never z-fights */
 public static final RenderType WASH=create("mareeletrica_led_wash",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,16384,false,false,
  CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY).setWriteMaskState(COLOR_WRITE)
   .setLayeringState(VIEW_OFFSET_Z_LAYERING).setCullState(NO_CULL).createCompositeState(false));
 /** the halo around the bulb */
 public static final RenderType HALO=create("mareeletrica_led_halo",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,4096,false,false,
  CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY).setWriteMaskState(COLOR_WRITE)
   .setCullState(NO_CULL).createCompositeState(false));
}
