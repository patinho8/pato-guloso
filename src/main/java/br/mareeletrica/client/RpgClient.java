package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Radar;
import br.mareeletrica.WarPlus;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.network.PacketDistributor;

/** RPG on the client: held with both hands, right click aims down the sights (zoom + sight with lock box), left click fires. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class RpgClient {
 private static float aim,aimO;
 /** Recoil: 1 right after a shot, fades away; kickLeft = how much upward camera kick is still to be applied. */
 private static float recoil,recoilO,kickLeft;
 private RpgClient(){}
 static boolean holding(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.player.getMainHandItem().getItem() instanceof WarPlus.LauncherItem;}
 static boolean aiming(){var mc=Minecraft.getInstance();return WarPlus.LauncherItem.aiming(mc.player);}
 public static final IClientItemExtensions EXT=new IClientItemExtensions(){
  @Override public HumanoidModel.ArmPose getArmPose(LivingEntity e,InteractionHand hand,ItemStack s){return HumanoidModel.ArmPose.CROSSBOW_HOLD;}
  /** First person: the tube rests on the shoulder; aiming brings the sight to the middle of the screen. */
  @Override public boolean applyForgeHandTransform(PoseStack p,net.minecraft.client.player.LocalPlayer player,HumanoidArm arm,ItemStack s,float pt,float equip,float swing){
   float a=net.minecraft.util.Mth.lerp(pt,aimO,aim);int side=arm==HumanoidArm.RIGHT?1:-1;
   p.translate(side*net.minecraft.util.Mth.lerp(a,.42F,0F),net.minecraft.util.Mth.lerp(a,-.42F,-.27F),net.minecraft.util.Mth.lerp(a,-.75F,-.55F));
   p.translate(0,equip*-.6F,0);
   float r=net.minecraft.util.Mth.lerp(pt,recoilO,recoil);
   if(r>0){p.translate(0,r*.06F,r*.32F);p.mulPose(com.mojang.math.Axis.XP.rotationDegrees(r*14F));}
   return true;
  }
 };
 /** While aiming, Minecraft throws away left clicks (an item is "in use"): catch them first and fire. */
 @SubscribeEvent public static void fireWhileAiming(net.neoforged.neoforge.client.event.ClientTickEvent.Pre e){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||!holding())return;
  boolean shot=false;while(mc.options.keyAttack.consumeClick())shot=true;
  if(shot)shoot();
 }
 /** Sends the shot; when the launcher is ready and loaded, the client already kicks back (the server pushes the player). */
 static void shoot(){
  var mc=Minecraft.getInstance();var p=mc.player;var s=p.getMainHandItem();
  boolean ready=!p.getCooldowns().isOnCooldown(s.getItem());
  boolean ammo=p.isCreative();if(!ammo)for(var st:p.getInventory().items)if(st.is(br.mareeletrica.Missiles.MISSILE.get())){ammo=true;break;}
  PacketDistributor.sendToServer(new WarPlus.Fire());
  if(ready&&ammo){recoil=1F;recoilO=1F;kickLeft=aiming()?6F:9F;}
 }
 /** Shakes the camera for a moment after the shot. */
 @SubscribeEvent public static void shake(net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles e){
  float r=(float)net.minecraft.util.Mth.lerp(e.getPartialTick(),recoilO,recoil);if(r<=.01F)return;
  double t=(Minecraft.getInstance().player.tickCount+e.getPartialTick())*2.7;float k=r*r;
  e.setPitch(e.getPitch()+(float)Math.sin(t*1.9)*k*1.6F);
  e.setYaw(e.getYaw()+(float)Math.cos(t*1.3)*k*1.2F);
  e.setRoll(e.getRoll()+(float)Math.sin(t)*k*3.5F);
 }
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
  recoilO=recoil;recoil=Math.max(0,recoil-.12F);
  var pl=Minecraft.getInstance().player;
  if(pl!=null&&kickLeft>0){float k=Math.min(kickLeft,kickLeft*.55F+.5F);pl.setXRot(net.minecraft.util.Mth.clamp(pl.getXRot()-k,-90,90));pl.xRotO=pl.getXRot();kickLeft-=k;}
  aimO=aim;aim=net.minecraft.util.Mth.clamp(aim+(aiming()?.25F:-.3F),0,1);}
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered e){
  if(!e.isAttack()||!holding()||Minecraft.getInstance().screen!=null)return;
  e.setCanceled(true);e.setSwingHand(false);shoot();
 }
 /** Looking through the sight: the tube itself is not drawn. */
 @SubscribeEvent public static void hand(net.neoforged.neoforge.client.event.RenderHandEvent e){if(aim>.6F&&holding())e.setCanceled(true);}
 @SubscribeEvent public static void fov(ComputeFovModifierEvent e){if(aiming()&&Minecraft.getInstance().options.getCameraType().isFirstPerson())e.setNewFovModifier(e.getNewFovModifier()*.5F);}
 @SubscribeEvent public static void crosshair(RenderGuiLayerEvent.Pre e){if(aiming()&&e.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR))e.setCanceled(true);}
 @SubscribeEvent public static void sight(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();if(!aiming()||mc.options.hideGui||!mc.options.getCameraType().isFirstPerson())return;
  var g=e.getGuiGraphics();var f=mc.font;int w=g.guiWidth(),h=g.guiHeight(),cx=w/2,cy=h/2;
  int lock=WarPlus.LauncherItem.lock(mc.player.getMainHandItem());boolean locked=lock>=WarPlus.LauncherItem.LOCK;
  var t=WarPlus.LauncherItem.aimed(mc.player);
  int G=0xFF6CFF7A,R=0xFFFF4040,c=locked?R:G;
  // dark sight tube around the view
  int r=Math.min(w,h)/2-16;
  for(int y=0;y<h;y+=2){int dy=y-cy;int half=dy*dy<r*r?(int)Math.sqrt(r*r-dy*dy):0;g.fill(0,y,cx-half,y+2,0xE0000000);g.fill(cx+half,y,w,y+2,0xE0000000);}
  // reticle: cross with a gap, range marks below
  g.fill(cx-40,cy,cx-6,cy+1,G);g.fill(cx+7,cy,cx+41,cy+1,G);g.fill(cx,cy-30,cx+1,cy-6,G);
  for(int i=1;i<=3;i++){g.fill(cx-8+i*2,cy+8+i*10,cx+9-i*2,cy+9+i*10,0xAA6CFF7A);g.drawString(f,String.valueOf(i*50),cx+12,cy+4+i*10,0xAA6CFF7A,false);}
  // lock box: grows tighter while locking
  int box=locked?10:22-lock/2;
  if(t!=null){g.fill(cx-box,cy-box,cx+box,cy-box+1,c);g.fill(cx-box,cy+box,cx+box,cy+box+1,c);g.fill(cx-box,cy-box,cx-box+1,cy+box,c);g.fill(cx+box,cy-box,cx+box+1,cy+box+1,c);
   String s=locked?"◉ TRAVADO: "+Radar.name(t)+"  "+Math.round(t.distanceTo(mc.player))+" m":"TRAVANDO "+Radar.name(t)+"  "+lock*100/WarPlus.LauncherItem.LOCK+"%";
   g.drawString(f,s,cx-f.width(s)/2,cy+r/2,c,true);}
  else{String s="Clique: disparar  •  mire num veículo para travar";g.drawString(f,s,cx-f.width(s)/2,cy+r/2,0xCCFFFFFF,true);}
  int ammo=0;for(var st:mc.player.getInventory().items)if(st.is(br.mareeletrica.Missiles.MISSILE.get()))ammo+=st.getCount();
  String a=mc.player.isCreative()?"MÍSSEIS ∞":"MÍSSEIS "+ammo;g.drawString(f,a,cx+r/2,cy+r/2+14,ammo>0||mc.player.isCreative()?G:R,true);
 }
}
