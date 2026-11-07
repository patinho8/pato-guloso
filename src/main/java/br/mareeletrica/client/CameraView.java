package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.lwjgl.glfw.GLFW;

/** Looking through a security camera: a client-only marker entity becomes the camera; the view is fixed, arrows switch cameras, X leaves. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class CameraView {
 public record Cam(BlockPos pos,Direction facing,String name){}
 private static List<Cam> cams=new ArrayList<>();private static int index=-1;
 private static Entity eye;private static CameraType previous;private static float savedYaw,savedPitch,camYaw,camPitch;private static int switchCooldown;
 public static boolean viewing(){return eye!=null;}
 public static void start(List<Cam> list,int which){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||list.isEmpty())return;
  if(eye==null){savedYaw=mc.player.getYRot();savedPitch=mc.player.getXRot();previous=mc.options.getCameraType();}
  cams=new ArrayList<>(list);index=Mth.clamp(which,0,cams.size()-1);
  mc.setScreen(null);place();
 }
 private static void place(){
  var mc=Minecraft.getInstance();var c=cams.get(index);
  if(eye==null){eye=EntityType.MARKER.create(mc.level);if(eye==null)return;eye.setId(-7_000_000);mc.level.addEntity(eye);}
  Vec3 at=Vec3.atCenterOf(c.pos()).add(Vec3.atLowerCornerOf(c.facing().getNormal()).scale(.32)).subtract(0,eye.getEyeHeight(),0);
  eye.moveTo(at.x,at.y,at.z,0,0);eye.xo=at.x;eye.yo=at.y;eye.zo=at.z;
  float yaw=c.facing().getAxis().isHorizontal()?c.facing().toYRot():mc.player.getYRot();
  float pitch=c.facing()==Direction.UP?-60F:c.facing()==Direction.DOWN?60F:15F;
  camYaw=yaw;camPitch=pitch;
  mc.player.setYRot(yaw);mc.player.setXRot(pitch);mc.player.yRotO=yaw;mc.player.xRotO=pitch;
  mc.options.setCameraType(CameraType.FIRST_PERSON);mc.setCameraEntity(eye);
 }
 public static void stop(){
  var mc=Minecraft.getInstance();
  if(eye!=null&&mc.level!=null)mc.level.removeEntity(eye.getId(),Entity.RemovalReason.DISCARDED);
  eye=null;
  if(mc.player!=null){mc.setCameraEntity(mc.player);mc.player.setYRot(savedYaw);mc.player.setXRot(savedPitch);if(previous!=null)mc.options.setCameraType(previous);}
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();
  if(eye==null)return;
  if(mc.player==null||mc.level==null||eye.level()!=mc.level){eye=null;return;}
  // fixed camera: the mouse does not move the view
  mc.player.setYRot(camYaw);mc.player.setXRot(camPitch);mc.player.yRotO=camYaw;mc.player.xRotO=camPitch;
  eye.setYRot(camYaw);eye.setXRot(camPitch);eye.yRotO=camYaw;eye.xRotO=camPitch;
  while(DroneClient.EXIT.consumeClick())stop();
  if(eye==null)return;
  if(switchCooldown>0)switchCooldown--;
  long w=mc.getWindow().getWindow();
  if(switchCooldown==0&&cams.size()>1){
   if(GLFW.glfwGetKey(w,GLFW.GLFW_KEY_RIGHT)==GLFW.GLFW_PRESS){index=(index+1)%cams.size();place();switchCooldown=6;}
   else if(GLFW.glfwGetKey(w,GLFW.GLFW_KEY_LEFT)==GLFW.GLFW_PRESS){index=(index+cams.size()-1)%cams.size();place();switchCooldown=6;}
  }
  if(mc.player.hurtTime>0)stop();   // getting hit pulls you out of the camera
 }
 @SubscribeEvent public static void move(MovementInputUpdateEvent e){
  if(eye==null)return;var in=e.getInput();
  in.forwardImpulse=0;in.leftImpulse=0;in.jumping=false;in.shiftKeyDown=false;in.up=false;in.down=false;in.left=false;in.right=false;
 }
 @SubscribeEvent public static void fixedAngles(net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles e){if(eye!=null){e.setYaw(camYaw);e.setPitch(camPitch);}}
 /** No hand or held item in the camera view. */
 @SubscribeEvent public static void noHand(net.neoforged.neoforge.client.event.RenderHandEvent e){if(eye!=null)e.setCanceled(true);}
 /** Wide security-camera lens. */
 @SubscribeEvent public static void lens(net.neoforged.neoforge.client.event.ViewportEvent.ComputeFov e){if(eye!=null)e.setFOV(100);}
 @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e){if(eye!=null){e.setCanceled(true);e.setSwingHand(false);}}
 @SubscribeEvent public static void hideVanilla(RenderGuiLayerEvent.Pre e){if(eye!=null&&e.getName().getNamespace().equals("minecraft"))e.setCanceled(true);}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  if(eye==null)return;var mc=Minecraft.getInstance();var g=e.getGuiGraphics();var f=mc.font;int w=g.guiWidth(),h=g.guiHeight();
  g.fill(0,0,w,h,0x22304030);   // cheap CCTV colour: washed-out greenish grey
  for(int y=0;y<h;y+=2)g.fill(0,y,w,y+1,0x18000000);   // scanlines
  int band=(int)((mc.level==null?0:mc.level.getGameTime())*3%(h+40))-20;g.fill(0,band,w,band+6,0x10FFFFFF);   // rolling interference band
  var rnd=new java.util.Random(mc.player.tickCount*31L);for(int i=0;i<160;i++){int x=rnd.nextInt(w),y=rnd.nextInt(h);g.fill(x,y,x+1,y+1,rnd.nextBoolean()?0x30FFFFFF:0x30000000);}   // grain
  g.fillGradient(0,0,w,h/4,0x70000000,0);g.fillGradient(0,h-h/4,w,h,0,0x70000000);   // vignette
  int m=24,l=22,c0=0xCCFFFFFF;   // viewfinder corner brackets and centre mark
  g.fill(m,m,m+l,m+2,c0);g.fill(m,m,m+2,m+l,c0);g.fill(w-m-l,m,w-m,m+2,c0);g.fill(w-m-2,m,w-m,m+l,c0);
  g.fill(m,h-m-2,m+l,h-m,c0);g.fill(m,h-m-l,m+2,h-m,c0);g.fill(w-m-l,h-m-2,w-m,h-m,c0);g.fill(w-m-2,h-m-l,w-m,h-m,c0);
  g.fill(w/2-6,h/2,w/2-2,h/2+1,0x99FFFFFF);g.fill(w/2+3,h/2,w/2+7,h/2+1,0x99FFFFFF);g.fill(w/2,h/2-6,w/2+1,h/2-2,0x99FFFFFF);g.fill(w/2,h/2+3,w/2+1,h/2+7,0x99FFFFFF);
  var c=cams.get(index);
  g.drawString(f,"CAM "+(index+1)+"/"+cams.size()+"  "+c.name()+"  ("+c.pos().getX()+", "+c.pos().getY()+", "+c.pos().getZ()+")",m+6,m+6,0xFFFFFFFF,true);
  long t=mc.level==null?0:mc.level.getDayTime()%24000;int hh=(int)((t/1000+6)%24),mm=(int)(t%1000*60/1000);
  long day=mc.level==null?0:mc.level.getDayTime()/24000+1;String clock=String.format("DIA %d  %02d:%02d",day,hh,mm);g.drawString(f,clock,w-m-6-f.width(clock),m+6,0xFFFFFFFF,true);
  if((mc.player.tickCount/10)%2==0)g.fill(m+6,m+20,m+12,m+26,0xFFFF4040);
  g.drawString(f,"REC",m+16,m+19,0xFFFFFFFF,true);
  String help=(cams.size()>1?"[← →] trocar câmera   ":"")+"[X] sair";
  g.drawString(f,help,w/2-f.width(help)/2,h-14,0xCCFFFFFF,true);
 }
}
