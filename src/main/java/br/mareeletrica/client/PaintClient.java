package br.mareeletrica.client;

import br.mareeletrica.GunPaint;
import br.mareeletrica.MareEletrica;
import br.mareeletrica.WeaponFamily;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent;

/** 0.70.0: draws gun paint. Each paintable region has overlay models with exactly the gun's geometry (re-UV'd onto shared paint
 *  textures, tinted); they are drawn after the gun in the same render type, so with LEQUAL depth they cover it exactly. */
public final class PaintClient {
 private PaintClient(){}
 /** "fam key" -> regions that have overlay models */
 static final Map<String,List<Integer>> REG=new HashMap<>();
 /** fam -> x0,x1,y,z,size (model units) of the sticker spot */
 static final Map<String,float[]> DECAL=new HashMap<>();
 static boolean loaded;
 static void load(){
  if(loaded)return;loaded=true;
  try(var in=PaintClient.class.getResourceAsStream("/assets/mareeletrica/paint_index.txt")){
   if(in==null)return;
   for(String l:new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8)).lines().toList()){
    var p=l.trim().split(" ");if(p.length<3)continue;
    if(p[0].equals("decal")){DECAL.put(p[1],new float[]{Float.parseFloat(p[2]),Float.parseFloat(p[3]),Float.parseFloat(p[4]),Float.parseFloat(p[5]),Float.parseFloat(p[6])});continue;}
    REG.computeIfAbsent(p[0]+" "+p[1],k->new ArrayList<>()).add(Integer.parseInt(p[2]));}
  }catch(Exception e){System.err.println("paint index: "+e);}
 }
 static final String[] VAR={"s","1","2","3"};
 public static void register(ModelEvent.RegisterAdditional e){load();for(var en:REG.entrySet()){var k=en.getKey().replace(' ','_');for(int r:en.getValue())for(var v:VAR)e.register(ClientEvents.part("armory21p_"+k+"_"+r+"_"+v));}}
 public static String tag(ItemStack s,WeaponFamily f){return s.is(MareEletrica.HUNTER.get())?"sniperh":f.id;}
 static String fam(String tag,WeaponFamily f,String key){load();return REG.containsKey(tag+" "+key)?tag:f.id;}
 /** regions this gun can paint */
 public static boolean[] available(ItemStack s){
  load();var f=WeaponFamily.of(s);boolean[] a=new boolean[GunPaint.PARTS.length];if(f==null)return a;String t=tag(s,f);
  for(var en:REG.entrySet()){var k=en.getKey();if(k.startsWith(t+" ")||(k.startsWith(f.id+" ")&&!k.endsWith(" body")&&!k.endsWith(" view")))for(int r:en.getValue())a[r]=true;}
  return a;}
 static void raw(String name,VertexConsumer vc,PoseStack pose,float r,float g,float b,int light){
  var mc=Minecraft.getInstance();
  mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),vc,null,mc.getModelManager().getModel(ClientEvents.part(name)),r,g,b,light,OverlayTexture.NO_OVERLAY);}
 static float time(){var mc=Minecraft.getInstance();return mc.level==null?0:mc.level.getGameTime()+mc.getTimer().getGameTimeDeltaPartialTick(false);}
 static float[] rgb(int c){return new float[]{(c>>16&255)/255F,(c>>8&255)/255F,(c&255)/255F};}
 static float[] rainbow(float off){int c=java.awt.Color.HSBtoRGB((time()*.008F+off)%1F,.85F,1F);return rgb(c);}
 /** a gun piece: the plain model, then any paint on its regions */
 public static void model(ItemStack s,WeaponFamily f,String key,String base,PoseStack pose,MultiBufferSource b,int light){
  var p=GunPaint.paint(s);
  if(p.isEmpty()){DmtGearLayer.model(base,pose,b,light);return;}
  var cut=RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
  raw(base,b.getBuffer(cut),pose,1,1,1,light);
  String fam=fam(tag(s,f),f,key);var regs=REG.get(fam+" "+key);if(regs==null)return;
  List<String> glint=null;
  for(int r:regs){
   if(!p.contains("r"+r))continue;
   var c=p.getCompound("r"+r);int fin=c.getInt("F"),pat=c.getInt("P");
   float[] c1=fin==3?rainbow(r*.13F):rgb(c.getInt("C")),c2=fin==3?rainbow(r*.13F+.5F):rgb(c.getInt("D"));
   if(fin==1){for(int i=0;i<3;i++){c1[i]=Math.min(1,c1[i]*.85F+.18F);c2[i]=Math.min(1,c2[i]*.85F+.18F);}}
   int L=fin==2?15728880:light;
   String nm="armory21p_"+fam+"_"+key+"_"+r+"_";
   raw(nm+"s",b.getBuffer(cut),pose,c1[0],c1[1],c1[2],L);
   if(pat>0)raw(nm+pat,b.getBuffer(cut),pose,c2[0],c2[1],c2[2],L);
   if(fin==1){if(glint==null)glint=new ArrayList<>();glint.add(nm+"s");}
  }
  if(glint!=null)for(var n:glint)raw(n,b.getBuffer(RenderType.entityGlint()),pose,1,1,1,light);
 }
 /** the sticker on both sides of the receiver */
 public static void sticker(ItemStack s,WeaponFamily f,PoseStack pose,MultiBufferSource b,int light){
  int st=GunPaint.paint(s).getInt("S");if(st<=0||st>=GunPaint.STICKER_TEX.length)return;
  load();var d=DECAL.get(tag(s,f));if(d==null)return;
  var vc=b.getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/sticker/"+GunPaint.STICKER_TEX[st]+".png")));
  float h=d[4]/2/16,y=d[2]/16,z=d[3]/16,e=.0015F;var m=pose.last();
  // east side (+x): image right = towards the muzzle (-z)
  float x=d[1]/16+e;
  v(vc,m,x,y-h,z+h,0,1,light,1);v(vc,m,x,y-h,z-h,1,1,light,1);v(vc,m,x,y+h,z-h,1,0,light,1);v(vc,m,x,y+h,z+h,0,0,light,1);
  x=d[0]/16-e;
  v(vc,m,x,y-h,z-h,0,1,light,-1);v(vc,m,x,y-h,z+h,1,1,light,-1);v(vc,m,x,y+h,z+h,1,0,light,-1);v(vc,m,x,y+h,z-h,0,0,light,-1);
 }
 static void v(VertexConsumer vc,PoseStack.Pose m,float x,float y,float z,float u,float vv,int light,int nx){
  vc.addVertex(m,x,y,z).setColor(255,255,255,255).setUv(u,vv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,0,0);}
 public static CompoundTag copy(ItemStack s){return GunPaint.paint(s).copy();}
}
