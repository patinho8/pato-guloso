package br.mareeletrica.client;

import br.mareeletrica.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class DmtClient {
 public static final KeyMapping LEFT=key("left",GLFW.GLFW_KEY_Z),RIGHT=key("right",GLFW.GLFW_KEY_X),RELEASE=key("release",GLFW.GLFW_KEY_C),REFILL=key("refill",GLFW.GLFW_KEY_R),OPEN=key("open",GLFW.GLFW_KEY_H),WEAPON=key("weapon",GLFW.GLFW_KEY_F),TOGGLE=key("toggle",GLFW.GLFW_KEY_U);
 private static KeyMapping key(String name,int key){return new KeyMapping("key.mareeletrica.dmt_"+name,InputConstants.Type.KEYSYM,key,"key.categories.mareeletrica");}
 private static boolean hookDown,grabbing;private static int useHeld;
 private record Seen(DmtPackets.State packet,long tick){}
 private static final Map<Integer,Seen> STATES=new HashMap<>();
 public static void receive(DmtPackets.State p){var mc=Minecraft.getInstance();if(mc.level==null)return;if(p.flags()==0)STATES.remove(p.entity());else STATES.put(p.entity(),new Seen(p,mc.level.getGameTime()));}
 public static DmtPackets.State state(int entity){var s=STATES.get(entity);return s==null?null:s.packet;}
 private static boolean equipped(){var mc=Minecraft.getInstance();return mc.player!=null&&DmtItem.enabled(mc.player)&&!SniperItem.firearm(mc.player.getMainHandItem())&&!RemoteClient.active()&&!AimRemoteItem.active(mc.player.getMainHandItem());}
 @SubscribeEvent public static void preTick(ClientTickEvent.Pre e){var mc=Minecraft.getInstance();if(equipped()&&mc.screen==null&&WEAPON.getKey().equals(mc.options.keySwapOffhand.getKey()))while(mc.options.keySwapOffhand.consumeClick()){} }
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){STATES.clear();}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(!mc.options.keyUse.isDown()){hookDown=false;useHeld=0;if(grabbing){grabbing=false;PacketDistributor.sendToServer(new DmtPackets.Command(13));}}
  else if(hookDown&&equipped()&&mc.screen==null&&++useHeld==8){grabbing=true;PacketDistributor.sendToServer(new DmtPackets.Command(12));}if(mc.level!=null)STATES.values().removeIf(s->mc.level.getGameTime()-s.tick>60);
  if(mc.player!=null&&mc.screen==null&&!DmtItem.equipped(mc.player).isEmpty())while(TOGGLE.consumeClick())PacketDistributor.sendToServer(new DmtPackets.Command(10));else while(TOGGLE.consumeClick()){}
  if(!equipped()||mc.screen!=null){while(LEFT.consumeClick()){}while(RIGHT.consumeClick()){}while(RELEASE.consumeClick()){}while(REFILL.consumeClick()){}while(OPEN.consumeClick()){}while(WEAPON.consumeClick()){}return;}
  while(OPEN.consumeClick())PacketDistributor.sendToServer(new DmtPackets.Command(5));while(WEAPON.consumeClick())PacketDistributor.sendToServer(new DmtPackets.Command(9));
  if(DmtCombat.mode(mc.player)>0&&mc.options.keyAttack.isDown()&&mc.player.tickCount%4==0)PacketDistributor.sendToServer(new DmtPackets.Command(8));
  while(LEFT.consumeClick())PacketDistributor.sendToServer(new DmtPackets.Command(0));while(RIGHT.consumeClick())PacketDistributor.sendToServer(new DmtPackets.Command(1));while(RELEASE.consumeClick())PacketDistributor.sendToServer(new DmtPackets.Command(2));while(REFILL.consumeClick())PacketDistributor.sendToServer(new DmtPackets.Command(3));
  if(mc.player.tickCount%2==0)PacketDistributor.sendToServer(new DmtPackets.Control(mc.options.keyJump.isDown(),(mc.options.keyRight.isDown()?1:0)-(mc.options.keyLeft.isDown()?1:0)));
 }
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered event){var mc=Minecraft.getInstance();if(!equipped()||mc.screen!=null)return;boolean hook=event.isUseItem(),attack=event.isAttack()&&DmtCombat.mode(mc.player)>0;if(!hook&&!attack)return;event.setCanceled(true);event.setSwingHand(false);if(hook){if(!hookDown&&event.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND){hookDown=true;PacketDistributor.sendToServer(new DmtPackets.Command(1));}}else PacketDistributor.sendToServer(new DmtPackets.Command(8));}
 @SubscribeEvent public static void scroll(InputEvent.MouseScrollingEvent event){var mc=Minecraft.getInstance();if(!equipped()||mc.screen!=null||AimRemoteItem.active(mc.player.getMainHandItem())||state(mc.player.getId())==null||event.getScrollDeltaY()==0)return;event.setCanceled(true);PacketDistributor.sendToServer(new DmtPackets.Command(event.getScrollDeltaY()>0?6:7));}
 public static void hud(GuiGraphics g,DeltaTracker dt){if(!equipped())return;var mc=Minecraft.getInstance();if(mc.screen!=null)return;var s=state(mc.player.getId());int fuel=s==null?DmtItem.fuel(DmtItem.equipped(mc.player)):s.fuel();int x=Math.max(4,g.guiWidth()-154),y=g.guiHeight()-58;g.fill(x,y,g.guiWidth()-6,y+35,0xB50D2028);g.drawString(mc.font,"DMT · "+fuel/20+" s · "+DmtCombat.name(DmtCombat.mode(mc.player)),x+5,y+5,0xA7E9DD,false);g.drawString(mc.font,OPEN.getTranslatedKeyMessage().getString()+": painel e ajuda · "+TOGGLE.getTranslatedKeyMessage().getString()+": desligar",x+5,y+20,0xDCE3E8,false);}
 private static void cable(PoseStack p,VertexConsumer b,Vec3 a,Vec3 end,boolean boost){Vec3 delta=end.subtract(a);Vec3 prev=a;int n=20;double sag=boost?0:Math.min(.6,delta.length()*.025);
  for(int i=1;i<=n;i++){double t=i/(double)n;Vec3 next=a.add(delta.scale(t)).add(0,-Math.sin(Math.PI*t)*sag,0);Vec3 normal=next.subtract(prev).normalize();b.addVertex(p.last(),(float)prev.x,(float)prev.y,(float)prev.z).setColor(15,16,19,255).setNormal(p.last(),(float)normal.x,(float)normal.y,(float)normal.z);b.addVertex(p.last(),(float)next.x,(float)next.y,(float)next.z).setColor(15,16,19,255).setNormal(p.last(),(float)normal.x,(float)normal.y,(float)normal.z);prev=next;}
 }
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;var mc=Minecraft.getInstance();if(mc.level==null||STATES.isEmpty())return;var poses=e.getPoseStack();var camera=e.getCamera().getPosition();var buffers=mc.renderBuffers().bufferSource();poses.pushPose();poses.translate(-camera.x,-camera.y,-camera.z);float dt=e.getPartialTick().getGameTimeDeltaPartialTick(false);
  for(var seen:STATES.values()){var s=seen.packet;if(!(mc.level.getEntity(s.entity()) instanceof Player player))continue;Vec3 hip=player.getPosition(dt).add(0,.72,0);double yaw=Math.toRadians(player.getYRot());Vec3 side=new Vec3(Math.cos(yaw)*.35,0,Math.sin(yaw)*.35);
   for(int hand=0;hand<2;hand++)if((s.flags()&(1<<hand))!=0){Vec3 end=hand==0?new Vec3(s.lx(),s.ly(),s.lz()):new Vec3(s.rx(),s.ry(),s.rz());cable(poses,buffers.getBuffer(RenderType.lines()),hip.add(side.scale(hand==0?1:-1)),end,(s.flags()&4)!=0);poses.pushPose();poses.translate(end.x-.12,end.y-.12,end.z-.12);poses.scale(.24F,.24F,.24F);DmtGearLayer.model("dmt_hook",poses,buffers,0xF000F0);poses.popPose();}
  }poses.popPose();buffers.endBatch(RenderType.lines());
 }
}
