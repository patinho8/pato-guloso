package br.mareeletrica.client;
import br.mareeletrica.ExplorerEntity;
import br.mareeletrica.MareEletrica;
import br.mareeletrica.RemotePackets;
import br.mareeletrica.VehicleRules;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class RemoteClient {
 private static RemotePackets.State state=new RemotePackets.State(0,0,0,0,0,0);
 private static float yaw,pitch;private static Vec3 anchor;private static int ticks,missing;
 private static ClientLevel sessionLevel;
 private static LocalPlayer sessionPlayer;
 private static ExplorerEntity cameraTarget;
 private static boolean cameraReady,waitingRelease,flightToggle;private static CameraType previousCamera=CameraType.FIRST_PERSON;
 public static boolean active(){return state.token()!=0;}
 public static Vec3 renderPosition(){
  return active()&&cameraReady&&cameraTarget!=null&&Minecraft.getInstance().getCameraEntity()==cameraTarget?cameraTarget.position():null;
 }
 public static void receive(RemotePackets.State packet){
  var mc=Minecraft.getInstance();if(packet.token()==0){reset();return;}
  if(mc.level==null||mc.player==null)return;
  if(!active()||packet.token()!=state.token()){
   if(active())reset();
   previousCamera=mc.options.getCameraType();sessionLevel=mc.level;sessionPlayer=mc.player;
   anchor=mc.player.position();cameraReady=false;waitingRelease=false;ticks=missing=0;
  }
  state=packet;
 }
 public static void requestFlight(){if(active())flightToggle=true;}
 public static void openInventory(){if(active())PacketDistributor.sendToServer(new RemotePackets.Cargo(state.token()));}
 public static void turn(double dx,double dy){yaw=(yaw+(float)dx*.15F)%360;pitch=VehicleRules.pitch(pitch+(float)dy*.15F);}
 private static void restoreCamera(){
  var mc=Minecraft.getInstance();
  // Never pass a missing player or an entity from the previous world to renderer hooks.
  if(mc.player!=null&&mc.level!=null&&mc.player.level()==mc.level&&mc.getCameraEntity()!=mc.player)mc.setCameraEntity(mc.player);
  if(active())mc.options.setCameraType(previousCamera);
  cameraReady=false;cameraTarget=null;
 }
 private static void reset(){
  if(active())restoreCamera();
  state=new RemotePackets.State(0,0,0,0,0,0);anchor=null;sessionLevel=null;sessionPlayer=null;
  cameraReady=false;waitingRelease=false;ticks=missing=0;
 }
 public static void exit(){
  if(!active()||waitingRelease)return;
  waitingRelease=true;restoreCamera();
  if(Minecraft.getInstance().getConnection()!=null)PacketDistributor.sendToServer(new RemotePackets.Drive(state.token(),0,yaw,pitch,2));
  else reset();
 }
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){reset();}
 @SubscribeEvent public static void tick(ClientTickEvent.Pre event){
  var mc=Minecraft.getInstance();if(!active())return;
  if(mc.player==null||mc.level==null||mc.player!=sessionPlayer||mc.level!=sessionLevel||!mc.player.isAlive()){reset();return;}
  if(mc.screen!=null&&!(mc.screen instanceof ExplorerScreen)){exit();return;}
  if(anchor!=null){mc.player.setPos(anchor.x,anchor.y,anchor.z);mc.player.setDeltaMovement(Vec3.ZERO);}
  if(waitingRelease)return;
  if(mc.level.getEntity(state.entityId()) instanceof ExplorerEntity rover&&rover.isAlive()&&!rover.isRemoved()&&mc.level.hasChunkAt(rover.blockPosition())){
   if(cameraTarget!=rover){
    yaw=rover.getYRot();pitch=rover.getXRot();
    if(mc.getCameraEntity()!=rover)mc.setCameraEntity(rover);
    cameraTarget=rover;cameraReady=true;
   }else if(mc.getCameraEntity()!=rover){exit();return;}
   mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);missing=0;
  }else{
   if(cameraReady)restoreCamera();
   if(++missing>160){exit();return;}
  }
  yaw+=(mc.options.keyRight.isDown()?3:0)-(mc.options.keyLeft.isDown()?3:0);
  float forward=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);
  if(mc.screen!=null)forward=0;
  if(++ticks%2==0){int flags=cameraReady?4:0;if(mc.screen==null){flags|=mc.options.keyAttack.isDown()?1:0;flags|=mc.options.keyJump.isDown()?16:0;flags|=mc.options.keyShift.isDown()?32:0;flags|=flightToggle?8:0;}flightToggle=false;PacketDistributor.sendToServer(new RemotePackets.Drive(state.token(),cameraReady?forward:0,yaw,pitch,flags));}
 }
 @SubscribeEvent public static void input(MovementInputUpdateEvent e){if(!active())return;var i=e.getInput();i.forwardImpulse=i.leftImpulse=0;i.up=i.down=i.left=i.right=i.jumping=i.shiftKeyDown=false;e.getEntity().setSprinting(false);}
 @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e){if(active()){e.setCanceled(true);e.setSwingHand(false);}}
 @SubscribeEvent public static void scroll(InputEvent.MouseScrollingEvent e){if(active())e.setCanceled(true);}
 @SubscribeEvent public static void key(InputEvent.Key e){if(active()&&Minecraft.getInstance().screen==null&&e.getAction()==GLFW.GLFW_PRESS){if(e.getKey()==GLFW.GLFW_KEY_G)requestFlight();if(e.getKey()==GLFW.GLFW_KEY_I)openInventory();}if(active()&&e.getAction()==GLFW.GLFW_PRESS&&Minecraft.getInstance().screen==null&&(e.getKey()==GLFW.GLFW_KEY_R||e.getKey()==GLFW.GLFW_KEY_ESCAPE))exit();}
 @SubscribeEvent public static void distance(net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent e){if(active()&&e.getCamera().getEntity()==cameraTarget)e.setDistance(7F);}
 @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){if(active()&&cameraReady&&e.getCamera().getEntity()==cameraTarget){e.setYaw(yaw);e.setPitch(pitch);e.setRoll(0);}}
 public static void hud(GuiGraphics g,DeltaTracker delta){
  if(!active()||waitingRelease)return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;int w=g.guiWidth(),h=g.guiHeight();g.fill(8,8,w-8,55,0xCC111025);
  g.drawString(mc.font,Component.translatable("remote.mareeletrica.hud",state.energy()/1000,state.ammo(),state.distance()),15,14,0xDCBAFF,false);
  g.drawWordWrap(mc.font,Component.translatable(cameraReady?"remote.mareeletrica.controls":"remote.mareeletrica.loading"),15,28,w-30,0xDAEFFF);
  // Leave room for the player's hunger, thirst and hotbar below the vehicle panel.
  g.fill(8,h-78,w-8,h-48,0xAA111025);
  g.drawCenteredString(mc.font,Component.translatable("remote.mareeletrica.mode_"+state.mode()),w/2,h-75,0xB890FF);if(cameraTarget!=null)g.drawCenteredString(mc.font,"Gás: "+cameraTarget.fuelSeconds()+" s | I: carga | G: avião | Espaço/Shift: altitude",w/2,h-62,0xB1E8FF);int x=w/2,y=h/2;g.fill(x-5,y,x+6,y+1,0xFFCA9AFF);g.fill(x,y-5,x+1,y+6,0xFFCA9AFF);
 }
}
