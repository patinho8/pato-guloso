package br.mareeletrica.client;

import br.mareeletrica.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Hold the lean key with a firearm, then A/D to peek left/right instead of strafing. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class LeanClient {
 public static final KeyMapping KEY=new KeyMapping("key.mareeletrica.lean",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_Z,"key.categories.mareeletrica");
 private static final Map<Integer,Float> CURRENT=new HashMap<>(),TARGET=new HashMap<>();
 private static float local;private static int sent;private static net.minecraft.world.phys.Vec3 applied=net.minecraft.world.phys.Vec3.ZERO,rightAxis=net.minecraft.world.phys.Vec3.ZERO;

 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static final class Keys{@SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(KEY);}}

 public static boolean holding(){
  var mc=Minecraft.getInstance();var p=mc.player;
  return p!=null&&mc.screen==null&&KEY.isDown()&&SniperItem.firearm(p.getMainHandItem())&&!ProneSystem.prone(p);
 }
 public static float local(){return local;}
 public static float of(Player p){return p==Minecraft.getInstance().player?local:CURRENT.getOrDefault(p.getId(),0F);}
 public static void remote(LeanNet.Show p){TARGET.put(p.entity(),(float)p.dir());}

 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();
  if(mc.player==null){local=0;sent=0;CURRENT.clear();TARGET.clear();return;}
  int dir=0;
  if(holding()){if(mc.options.keyLeft.isDown())dir-=1;if(mc.options.keyRight.isDown())dir+=1;}
  if(local==0&&dir!=0){double yaw=Math.toRadians(mc.player.getYRot());rightAxis=new net.minecraft.world.phys.Vec3(-Math.cos(yaw),0,-Math.sin(yaw));applied=net.minecraft.world.phys.Vec3.ZERO;}
  local+=(dir-local)*.35F;if(dir==0&&Math.abs(local)<.02F)local=0;
  if(local==0)applied=net.minecraft.world.phys.Vec3.ZERO;
  else if(!mc.player.isPassenger()){
   var step=rightAxis.scale(local*.42).subtract(applied);double len=step.length();
   if(len>1.0E-4){if(len>.12)step=step.scale(.12/len);var before=mc.player.position();mc.player.move(net.minecraft.world.entity.MoverType.SELF,step);var moved=mc.player.position().subtract(before);applied=applied.add(moved.x,0,moved.z);}
  }
  if(dir!=sent){sent=dir;PacketDistributor.sendToServer(new LeanNet.Choose(dir));}
  for(var entry:TARGET.entrySet()){float now=CURRENT.getOrDefault(entry.getKey(),0F);now+=(entry.getValue()-now)*.35F;if(entry.getValue()==0F&&Math.abs(now)<.02F)now=0;CURRENT.put(entry.getKey(),now);}
  TARGET.entrySet().removeIf(x->x.getValue()==0F&&CURRENT.getOrDefault(x.getKey(),0F)==0F);
  CURRENT.entrySet().removeIf(x->x.getValue()==0F);
 }
 @SubscribeEvent public static void move(MovementInputUpdateEvent e){
  if(!holding())return;
  var input=e.getInput();input.leftImpulse=0;input.left=false;input.right=false;
 }
 @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
  if(local!=0)e.setRoll(e.getRoll()+local*5F);
  var pl=Minecraft.getInstance().player;
  if(pl!=null&&ProneSystem.prone(pl)){double speed=pl.getDeltaMovement().horizontalDistance();
   if(speed>.008){float amp=(float)Math.min(1,speed*22),t=(pl.tickCount+(float)e.getPartialTick())*.34F;e.setRoll(e.getRoll()+(float)Math.sin(t)*2.0F*amp);e.setYaw(e.getYaw()+(float)Math.sin(t)*.9F*amp);e.setPitch(e.getPitch()+(float)Math.abs(Math.cos(t))*.5F*amp);}}
 }
}
