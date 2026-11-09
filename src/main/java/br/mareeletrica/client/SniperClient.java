package br.mareeletrica.client;
import br.mareeletrica.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class SniperClient {
 public static final KeyMapping OPEN=new KeyMapping("key.mareeletrica.rifle_panel",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_J,"key.categories.mareeletrica");
 public static final KeyMapping RELOAD=new KeyMapping("key.mareeletrica.revolver_reload",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R,"key.categories.mareeletrica");
 public static int flashTicks;private static boolean triggerHeld,attackPressed;private static net.minecraft.world.item.Item triggerItem;private static long lastShift=-100;private static int clock,kickTicks;private static boolean waitRelease;public static float kick;private static float aim,previousAim;
 public static boolean aiming(){var mc=Minecraft.getInstance();return mc.screen==null&&!RemoteClient.active()&&!waitRelease&&SniperItem.scoped(mc.player);}
 public static boolean scoped(){return aiming()&&Minecraft.getInstance().options.getCameraType().isFirstPerson()&&aim>.95F;}
 public static float aimAmount(float partial){return Mth.lerp(partial,previousAim,aim);}
 public static int zoom(){var p=Minecraft.getInstance().player;return p==null?1:p.getMainHandItem().getItem() instanceof GunItem?GunLoadout.zoom(p.getMainHandItem()):p.getMainHandItem().is(MareEletrica.SMG.get())?SmgLoadout.zoom(p.getMainHandItem()):RifleLoadout.zoom(p.getMainHandItem());}
 public static float reloadAmount(net.minecraft.world.item.ItemStack stack){return ArmoryRenderer.drop(ArmoryRenderer.progress(stack,0));}
 public static double mouseScale(){var mc=Minecraft.getInstance();return scoped()?Math.max(.006,1.0/viewZoom()):1;}
 /** 0.80.0: only the sniper rifles get the full-screen scope; every other gun aims the same way, with a light zoom that grows a little with the optic */
 public static boolean sniperFamily(net.minecraft.world.item.ItemStack s){var f=WeaponFamily.of(s);return f==WeaponFamily.SNIPER||f==WeaponFamily.BARRETT||f==WeaponFamily.SVD;}
 public static double viewZoom(){var p=Minecraft.getInstance().player;if(p==null)return 1;var s=p.getMainHandItem();int z=zoom();
  if(!(s.getItem() instanceof GunItem)||sniperFamily(s))return z;
  double v=z<=1?(ModularLoadout.hasOptic(s)?1.25:1.15):z==2?1.5:z==3?1.75:z<=4?1.95:2.2;   // iron 1.15× · red dot 1.25× · holo 1.5× · prism 1.75× · 4× 1.95× · 6× 2.2×
  var f=WeaponFamily.of(s);if(f!=null&&f.pistol())v*=.94;else if(f==WeaponFamily.M249)v*=1.05;
  return v;}
 public static void shot(GunPackets.Shot p){var mc=Minecraft.getInstance();if(mc.player==null)return;if(p.sniper()){waitRelease=mc.options.keyUse.isDown();mc.options.keyUse.setDown(false);if(mc.player.isUsingItem())mc.gameMode.releaseUsingItem(mc.player);mc.player.stopUsingItem();aim=previousAim=0;}mc.player.setXRot(Mth.clamp(mc.player.getXRot()-p.pitch(),-89,89));mc.player.setYRot(mc.player.getYRot()+p.yaw());flashTicks=2;kickTicks=14;kick=p.sniper()?1F:mc.player.getMainHandItem().getItem() instanceof GunItem g?Math.min(.9F,.2F+g.profile.recoil*.13F):mc.player.getMainHandItem().is(MareEletrica.SMG.get())?.38F:.6F;
  var hf=WeaponFamily.of(mc.player.getMainHandItem());if(hf!=null&&hf.heavyRecoil()>1F){float h=hf.heavyRecoil();kick=Math.min(1.5F,kick*(.6F+h*.4F));kickTicks=18;shake=Math.min(1.6F,shake+(h-1F)*.9F);shakeSeed=mc.player.getRandom().nextFloat()*100F;}}
 /** 0.81.0: screen shake after a heavy shot */
 public static float shake,shakeSeed;
 @SubscribeEvent public static void heavyShake(net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles e){if(shake<=.01F)return;float t=(float)((clock+e.getPartialTick())*1.9F+shakeSeed),a=shake*shake;
  e.setRoll(e.getRoll()+(float)Math.sin(t*1.3F)*3.2F*a);e.setPitch(e.getPitch()+(float)Math.sin(t*2.1F)*.9F*a);e.setYaw(e.getYaw()+(float)Math.cos(t*1.7F)*.8F*a);}
 public static void prone(GunPackets.Prone state){var mc=Minecraft.getInstance();if(mc.level!=null&&mc.level.getEntity(state.entity()) instanceof net.minecraft.world.entity.player.Player p)ProneSystem.set(p,state.active());}
 public static void shiftPress(){if(clock-lastShift<=7&&clock>lastShift){PacketDistributor.sendToServer(new GunPackets.Command(2));lastShift=-100;}else lastShift=clock;}
 @SubscribeEvent public static void key(InputEvent.Key e){var mc=Minecraft.getInstance();if(mc.player!=null&&mc.screen==null&&!RemoteClient.active()&&e.getAction()==GLFW.GLFW_PRESS&&mc.options.keyShift.matches(e.getKey(),e.getScanCode()))shiftPress();}
 private static boolean automatic(){var s=Minecraft.getInstance().player.getMainHandItem();return s.is(MareEletrica.SMG.get())||s.getItem() instanceof GunItem g&&g.profile.automatic;}
 private static void stopTrigger(){triggerHeld=false;triggerItem=null;PacketDistributor.sendToServer(new GunPackets.Command(6));PacketDistributor.sendToServer(new GunPackets.Command(10));}
 private static void attack(boolean pressed){var mc=Minecraft.getInstance();var stack=mc.player.getMainHandItem();if(!pressed){attackPressed=false;if(triggerHeld)stopTrigger();return;}if(attackPressed)return;attackPressed=true;if(stack.getItem() instanceof GunItem){triggerHeld=automatic();triggerItem=stack.getItem();PacketDistributor.sendToServer(new GunPackets.Command(9));}else if(stack.is(MareEletrica.SMG.get())){triggerHeld=true;triggerItem=stack.getItem();PacketDistributor.sendToServer(new GunPackets.Command(5));}else PacketDistributor.sendToServer(new GunPackets.Command(0));}
 private static boolean physicalAttack(){var mc=Minecraft.getInstance();var key=mc.options.keyAttack.getKey();return key.getType()==InputConstants.Type.MOUSE?GLFW.glfwGetMouseButton(mc.getWindow().getWindow(),key.getValue())==GLFW.GLFW_PRESS:InputConstants.isKeyDown(mc.getWindow().getWindow(),key.getValue());}
 @SubscribeEvent public static void mouse(InputEvent.MouseButton.Pre e){var mc=Minecraft.getInstance();if(mc.options.keyUse.matchesMouse(e.getButton())&&e.getAction()==GLFW.GLFW_RELEASE)waitRelease=false;if(mc.player==null||mc.screen!=null||RemoteClient.active()||!SniperItem.firearm(mc.player.getMainHandItem()))return;if(mc.options.keyAttack.matchesMouse(e.getButton())){e.setCanceled(true);attack(e.getAction()==GLFW.GLFW_PRESS);}if(mc.options.keyUse.matchesMouse(e.getButton())&&waitRelease)e.setCanceled(true);}
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered e){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||RemoteClient.active()||!SniperItem.firearm(mc.player.getMainHandItem()))return;if(e.isAttack()){e.setCanceled(true);e.setSwingHand(false);attack(true);}if(e.isUseItem()&&waitRelease){e.setCanceled(true);e.setSwingHand(false);}}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){clock++;shake*=.8F;if(shake<.01F)shake=0;if(flashTicks>0)flashTicks--;var mc=Minecraft.getInstance();if(kickTicks>0){kickTicks--;kick*=.86F;}else kick=0;previousAim=aim;aim=Mth.clamp(aim+(aiming()?.2F:-.35F),0,1);if(mc.player==null){triggerHeld=attackPressed=false;triggerItem=null;aim=previousAim=0;waitRelease=false;lastShift=-100;return;}if(!physicalAttack())attackPressed=false;if(triggerHeld){if(mc.screen!=null||!mc.isWindowActive()||RemoteClient.active()||mc.player.getMainHandItem().getItem()!=triggerItem||!automatic()||!physicalAttack())stopTrigger();else if(clock%4==0)PacketDistributor.sendToServer(new GunPackets.Command(mc.player.getMainHandItem().getItem() instanceof GunItem?9:5));}if(waitRelease)mc.options.keyUse.setDown(false);while(OPEN.consumeClick())if(mc.screen==null&&SniperItem.firearm(mc.player.getMainHandItem()))PacketDistributor.sendToServer(new GunPackets.Command(1));while(RELOAD.consumeClick())if(mc.screen==null&&SniperItem.firearm(mc.player.getMainHandItem()))PacketDistributor.sendToServer(new GunPackets.Command(4));
 }
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){triggerHeld=attackPressed=false;triggerItem=null;flashTicks=0;waitRelease=false;aim=previousAim=kick=0;}
 @SubscribeEvent public static void fov(ComputeFovModifierEvent e){if(ProneSystem.prone(e.getPlayer()))e.setNewFovModifier(1F);}
 public static double opticalFov(double base,double magnification){return Math.toDegrees(2*Math.atan(Math.tan(Math.toRadians(base)/2)/Math.max(1,magnification)));}
 @SubscribeEvent public static void optical(ViewportEvent.ComputeFov e){var mc=Minecraft.getInstance();if(!e.usedConfiguredFov()||mc.player==null||!mc.options.getCameraType().isFirstPerson())return;float amount=aimAmount((float)e.getPartialTick());if(amount>0)e.setFOV(Mth.lerp(amount,e.getFOV(),opticalFov(mc.options.fov().get(),viewZoom())));}

 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void hand(RenderHandEvent e){var mc=Minecraft.getInstance();if(mc.player!=null&&!RemoteClient.active()&&SniperItem.firearm(mc.player.getMainHandItem())){e.setCanceled(true);if(e.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND&&(!scoped()||!(MareEletrica.isSniper(mc.player.getMainHandItem())||scopeOptic(mc.player.getMainHandItem()))))RifleHands.render(e);}}
 @SubscribeEvent public static void crosshair(RenderGuiLayerEvent.Pre e){var held=Minecraft.getInstance().player;if(held!=null&&SniperItem.firearm(held.getMainHandItem())&&e.getName().equals(VanillaGuiLayers.CROSSHAIR))e.setCanceled(true);if(scoped()&&!e.getName().equals(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"sniper_scope")))e.setCanceled(true);}
 public static void hud(GuiGraphics g,DeltaTracker dt){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;var gun=mc.player.getMainHandItem();if(scoped()&&scopeOptic(gun)){scopeView(g,GunLoadout.zoom(gun)+"×  ·  "+((GunItem)gun.getItem()).profile.title);return;}if(scoped()&&gun.getItem() instanceof GunItem){arsenalReticle(g,gun);return;}if(scoped()&&gun.is(MareEletrica.SMG.get())){smgReticle(g);return;}if(scoped()&&!ModularLoadout.hasOptic(gun)&&!MareEletrica.isSniper(gun)){ammo(g,gun);return;}if(scoped()){scopeView(g,RifleLoadout.zoom(gun)+"×  ·  Zero "+RifleLoadout.calibration(gun,0)+" m");return;}
  if(SniperItem.firearm(gun))redDot(g);
  if(SniperItem.firearm(gun)){String text=gun.getItem() instanceof GunItem weapon?weapon.profile.title+"  "+GunLoadout.rounds(gun)+"/"+(WeaponFamily.of(gun)==null?weapon.profile.capacity:Accessory.capacity(gun,WeaponFamily.of(gun)))+" · R: recarga · J: painel":gun.is(MareEletrica.SMG.get())?"SMG  "+SmgLoadout.rounds(gun)+"/30 · R: recarga · J: painel":"Sniper  "+Magazines.rounds(gun)+"/5 · R: recarga · J: acessórios";int x=8,y=g.guiHeight()-65;g.fill(x-3,y-3,x+mc.font.width(text)+5,y+23,0xA0192027);g.drawString(mc.font,text,x,y,0xDBE7EC);g.drawString(mc.font,ProneSystem.prone(mc.player)?(ProneSystem.supported(mc.player)?"BIPÉ APOIADO":"DEITADO · sem apoio estável"):"Shift 2×: deitar",x,y+12,ProneSystem.supported(mc.player)?0x88EEAB:0xA9BBC8);}
 }
 /** 0.72.0: an arsenal gun wearing a rifle scope (4× or more) looks through the same scope as the snipers */
 public static boolean scopeOptic(net.minecraft.world.item.ItemStack gun){return gun.getItem() instanceof GunItem&&ModularLoadout.hasOptic(gun)&&GunLoadout.zoom(gun)>=4&&sniperFamily(gun);}
 /** the sniper scope: black all around a round lens with the Valorant-style reticle */
 static void scopeView(GuiGraphics g,String label){var mc=Minecraft.getInstance();int w=g.guiWidth(),h=g.guiHeight(),cx=w/2,cy=h/2,r=Math.max(20,(int)(h*.44));
  for(int y=0;y<h;y++){int dy=y-cy,dx=Math.abs(dy)>=r?0:(int)Math.sqrt(r*r-dy*dy);g.fill(0,y,cx-dx,y+1,0xFF030405);g.fill(cx+dx,y,w,y+1,0xFF030405);}
  ring(g,cx,cy,r,2,0xFF0B0D10);reticle(g,cx,cy,r);g.drawCenteredString(mc.font,label,cx,h-18,0xC8D3D8);}
 /** 0.67.3: hip-fire crosshair = a small round dot (white with a dark outline) */
 private static void redDot(GuiGraphics g){   // 0.78.2: drawn in real screen pixels (3 px dot + 1 px outline), no longer grows with the GUI scale
  var win=Minecraft.getInstance().getWindow();float k=(float)win.getGuiScale();int cx=win.getWidth()/2,cy=win.getHeight()/2;
  g.pose().pushPose();g.pose().scale(1F/k,1F/k,1F);
  g.fill(cx-1,cy-2,cx+2,cy+3,0x90000000);g.fill(cx-2,cy-1,cx+3,cy+2,0x90000000);
  g.fill(cx-1,cy-1,cx+2,cy+2,0xFFF4F6F6);g.fill(cx,cy-1,cx+1,cy+2,0xFFFFFFFF);
  g.pose().popPose();}
 private static void ammo(GuiGraphics g,net.minecraft.world.item.ItemStack s){var f=WeaponFamily.of(s);if(f!=null)g.drawCenteredString(Minecraft.getInstance().font,Magazines.rounds(s)+" / "+Accessory.capacity(s,f)+"  ·  "+(ModularLoadout.hasOptic(s)?zoom()+"×":"FERRO"),g.guiWidth()/2,g.guiHeight()-22,0xD4E7DF);}
 private static void arsenalReticle(GuiGraphics g,net.minecraft.world.item.ItemStack s){if(ModularLoadout.hasOptic(s))opticReticle(g,GunLoadout.zoom(s));ammo(g,s);}
 /** 0.72.0: what you see through a short optic: 1× red dot, 2× holographic ring, 3× prism chevron (glowing, in the middle of the screen) */
 private static void opticReticle(GuiGraphics g,int zoom){int cx=g.guiWidth()/2,cy=g.guiHeight()/2;
  if(zoom<=1){g.fill(cx-2,cy-1,cx+3,cy+2,0x66FF2020);g.fill(cx-1,cy-2,cx+2,cy+3,0x66FF2020);g.fill(cx-1,cy-1,cx+2,cy+2,0xFFFF3030);g.fill(cx,cy,cx+1,cy+1,0xFFFFC0C0);return;}
  if(zoom==2){ring(g,cx,cy,9,1,0xD0FF3030);g.fill(cx-1,cy-1,cx+2,cy+2,0xFFFF3030);for(int k=0;k<4;k++){int dx=k==0?-11:k==1?10:0,dy=k==2?-11:k==3?10:0;if(dx!=0)g.fill(cx+dx,cy,cx+dx+2,cy+1,0xFFFF3030);else g.fill(cx,cy+dy,cx+1,cy+dy+2,0xFFFF3030);}return;}
  // prism: chevron on a post, with bullet-drop marks
  for(int i=0;i<5;i++){g.fill(cx-i,cy+i,cx-i+1,cy+i+1,0xFFFF3A2A);g.fill(cx+i,cy+i,cx+i+1,cy+i+1,0xFFFF3A2A);}
  g.fill(cx,cy+5,cx+1,cy+22,0xE0181818);for(int k=1;k<=3;k++)g.fill(cx-4+k,cy+6+k*4,cx+5-k,cy+7+k*4,0xE0181818);}
 private static void smgReticle(GuiGraphics g){ammo(g,Minecraft.getInstance().player.getMainHandItem());}
 private static void ring(GuiGraphics g,int cx,int cy,int r,int width,int color){for(int y=-r;y<=r;y++){int outer=(int)Math.sqrt(r*r-y*y),inner=Math.abs(y)<r-width?(int)Math.sqrt((r-width)*(r-width)-y*y):0;g.fill(cx-outer,cy+y,cx-inner+1,cy+y+1,color);g.fill(cx+inner,cy+y,cx+outer+1,cy+y+1,color);}}
 /** 0.67.3: Valorant-style sniper scope: thin black cross through the centre (no gap, no dots), getting thick towards the edge of the lens */
 private static void reticle(GuiGraphics g,int cx,int cy,int r){
  int ink=0xF0101214,thin=(int)(r*.62F);
  g.fill(cx-r,cy,cx+r+1,cy+1,ink);g.fill(cx,cy-r,cx+1,cy+r+1,ink);   // thin hairlines
  g.fill(cx-r,cy-1,cx-thin,cy+2,ink);g.fill(cx+thin+1,cy-1,cx+r+1,cy+2,ink);g.fill(cx-1,cy+thin+1,cx+2,cy+r+1,ink);g.fill(cx-1,cy-r,cx+2,cy-thin,ink);
  g.fill(cx-r,cy-2,cx-r+(r-thin)/2,cy+3,ink);g.fill(cx+r-(r-thin)/2,cy-2,cx+r+1,cy+3,ink);g.fill(cx-2,cy+r-(r-thin)/2,cx+3,cy+r+1,ink);g.fill(cx-2,cy-r,cx+3,cy-r+(r-thin)/2,ink);
 }

}
