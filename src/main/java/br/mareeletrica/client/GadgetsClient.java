package br.mareeletrica.client;

import br.mareeletrica.*;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Binoculars zoom + range finder, GPS readout and thermal vision. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class GadgetsClient {
 private static final Set<LivingEntity> HOT=new HashSet<>();
 private GadgetsClient(){}
 private static boolean binoculars(){var p=Minecraft.getInstance().player;return p!=null&&p.isUsingItem()&&p.getUseItem().is(Gadgets.BINOCULARS.get());}
 @SubscribeEvent public static void fov(ComputeFovModifierEvent e){if(binoculars())e.setNewFovModifier(.085F);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();var p=mc.player;
  // 0.65.4: thermal glow now comes from ThermalClient (heat colours, no glowing tag on the mobs); just clear tags left by older versions
  for(var it=HOT.iterator();it.hasNext();){var le=it.next();if(!le.isRemoved())le.setGlowingTag(false);it.remove();}
 }
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.options.hideGui)return;
  var g=e.getGuiGraphics();var f=mc.font;int w=g.guiWidth(),h=g.guiHeight(),cx=w/2,cy=h/2;
  if(binoculars()){
   // two-tube mask
   int r=Math.min(w/4,h/2-6);
   for(int y=0;y<h;y++){
    double dy=y-cy;double half=Math.sqrt(Math.max(0,r*r-dy*dy));
    int l1=(int)(cx-r*.95-half),r1=(int)(cx-r*.95+half),l2=(int)(cx+r*.95-half),r2=(int)(cx+r*.95+half);
    if(half<=0){g.fill(0,y,w,y+1,0xFF000000);continue;}
    g.fill(0,y,Math.max(0,l1),y+1,0xFF000000);g.fill(Math.min(r1,l2),y,Math.max(r1,l2),y+1,r1<l2?0xFF000000:0);g.fill(Math.min(w,r2),y,w,y+1,0xFF000000);
   }
   g.fill(cx-6,cy,cx+7,cy+1,0xFFFF4040);g.fill(cx,cy-6,cx+1,cy+7,0xFFFF4040);
   // range finder
   var eye=p.getEyePosition(1F);var end=eye.add(p.getViewVector(1F).scale(2000));
   var hit=mc.level.clip(new ClipContext(eye,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.ANY,p));
   double dist=hit.getType()==HitResult.Type.MISS?-1:hit.getLocation().distanceTo(eye);String label="";
   var ent=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(p,eye,hit.getType()==HitResult.Type.MISS?end:hit.getLocation(),new AABB(eye,end).inflate(2),x->!x.isSpectator()&&x.isPickable(),dist<0?4_000_000:dist*dist);
   if(ent!=null){dist=ent.getLocation().distanceTo(eye);label=ent.getEntity().getName().getString();}
   String txt=dist<0?"---- m":String.format("%.0f m",dist);
   g.drawString(f,txt,cx-f.width(txt)/2,cy+12,0xFFFF5050,true);
   if(!label.isEmpty())g.drawString(f,label,cx-f.width(label)/2,cy+23,0xFFFFD0D0,true);
   g.drawString(f,"12x",cx+r/2,cy-r/2,0xAAFFFFFF,true);
  }
  // GPS
  var gps=p.getMainHandItem().is(Gadgets.GPS.get())?p.getMainHandItem():p.getOffhandItem().is(Gadgets.GPS.get())?p.getOffhandItem():null;
  if(gps!=null){
   int x=8,y=8;var pos=p.blockPosition();
   g.fill(x-3,y-3,x+150,y+36,0x99102030);
   g.drawString(f,"GPS  X "+pos.getX()+"  Y "+pos.getY()+"  Z "+pos.getZ(),x,y,0xFF8FE8FF,false);
   String[] dirs={"Sul","Sudoeste","Oeste","Noroeste","Norte","Nordeste","Leste","Sudeste"};
   g.drawString(f,"Direção: "+dirs[Math.floorMod(Math.round(p.getYRot()/45F),8)],x,y+11,0xFFCCE8F0,false);
   var mark=Gadgets.GpsItem.mark(gps);
   if(mark!=null){double dx=mark.getX()+.5-p.getX(),dz=mark.getZ()+.5-p.getZ();int d=(int)Math.sqrt(dx*dx+dz*dz);
    float rel=Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(-dx,dz))-p.getYRot());String arrow=Math.abs(rel)<22?"↑":Math.abs(rel)>158?"↓":rel>0?(rel<68?"↗":rel<112?"→":"↘"):(rel>-68?"↖":rel>-112?"←":"↙");
    g.drawString(f,"Ponto: "+d+" m  "+arrow,x,y+22,0xFFFFE070,false);}
  }
 }
}
