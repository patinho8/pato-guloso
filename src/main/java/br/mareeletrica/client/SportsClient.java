package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Sports;
import br.mareeletrica.SportsMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Sports game mode on the client: clicks near a ball become sport moves, and the body animates (leg kick, keeper dive/catch, jump shot, bump). */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class SportsClient {
 private record Move(int type,long start){}
 private static final Map<Integer,Move> MOVES=new HashMap<>();
 public static final int LENGTH=8;
 /** Duration of each animation: kicks and catches are quick, the volleyball block is held up for a second. */
 static int length(int type){return switch(type){case 3->10;case 4->9;case 5->11;case 6->20;case 7->14;default->LENGTH;};}
 private SportsClient(){}
 /** The four special sport moves; rebind them in Options → Controls → Maré Elétrica. Move 1 defaults to Q (it then replaces dropping the item while a ball is near). */
 public static final net.minecraft.client.KeyMapping[] KEYS={
  key(1,org.lwjgl.glfw.GLFW.GLFW_KEY_Q),key(2,org.lwjgl.glfw.GLFW.GLFW_KEY_R),key(3,org.lwjgl.glfw.GLFW.GLFW_KEY_C),key(4,org.lwjgl.glfw.GLFW.GLFW_KEY_G),key(5,org.lwjgl.glfw.GLFW.GLFW_KEY_V)};
 /** Client copy of the cooldowns (game time when each button is ready again), so the keys can't be spammed and the HUD shows the wait. */
 private static final long[] READY=new long[7];
 private static boolean ready(SportsMode.Mode m,int button){var mc=Minecraft.getInstance();long now=mc.level.getGameTime();if(now<READY[button])return false;READY[button]=now+SportsMode.cooldown(m,button);
  if(button>=2&&button<=5)for(int i=2;i<=5;i++)if(i!=button)READY[i]=Math.max(READY[i],now+16);return true;}
 private static net.minecraft.client.KeyMapping key(int n,int k){return new net.minecraft.client.KeyMapping("key.mareeletrica.sport_"+n,com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,k,"key.categories.mareeletrica");}
 public static void anim(SportsMode.Anim a){var mc=Minecraft.getInstance();if(mc.level!=null)MOVES.put(a.entity(),new Move(a.move(),mc.level.getGameTime()));}
 /** 0..1 progress of the current move of this entity, or -1. */
 public static float progress(LivingEntity e,float pt,int[] type){
  var m=MOVES.get(e.getId());if(m==null||e.level()==null)return -1;
  float t=(e.level().getGameTime()-m.start+pt)/length(m.type);if(t>=1){return -1;}
  type[0]=m.type;return Math.max(0,t);
 }
 private static boolean ballNear(Player p,Sports.Kind k,double r){return !p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().inflate(r),b->b.kind()==k).isEmpty();}
 private static boolean holding(Player p){return !p.level().getEntitiesOfClass(Sports.SportBall.class,p.getBoundingBox().inflate(3),b->b.holderId()==p.getId()).isEmpty();}
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.screen!=null)return;
  var m=SportsMode.mode(p);if(m==null||m==SportsMode.Mode.TENNIS)return;
  boolean hold=holding(p);
  if(e.isAttack()){
   if(!hold&&!ballNear(p,switch(m){case BASKET->Sports.Kind.BASKET;case VOLLEY->Sports.Kind.VOLLEY;default->Sports.Kind.SOCCER;},6))return;   // no ball around: normal click (mine, fight)
   e.setCanceled(true);e.setSwingHand(m!=SportsMode.Mode.SOCCER&&m!=SportsMode.Mode.VOLLEY&&!(m==SportsMode.Mode.KEEPER&&hold));
   if(ready(m,0))PacketDistributor.sendToServer(new SportsMode.Action(0));
  }else if(e.isUseItem()&&e.getHand()==InteractionHand.MAIN_HAND&&(hold||(m==SportsMode.Mode.SOCCER||m==SportsMode.Mode.VOLLEY)&&ballNear(p,m==SportsMode.Mode.VOLLEY?Sports.Kind.VOLLEY:Sports.Kind.SOCCER,3.5))){
   e.setCanceled(true);e.setSwingHand(m!=SportsMode.Mode.VOLLEY);if(ready(m,1))PacketDistributor.sendToServer(new SportsMode.Action(1));
  }
 }
 /** Special moves 1..4 while a ball of your sport is near (or you hold it). A key shared with "drop item" (Q) won't drop your item then. */
 @SubscribeEvent public static void keys(net.neoforged.neoforge.client.event.ClientTickEvent.Pre e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.screen!=null)return;
  var m=SportsMode.mode(p);if(m==null||m==SportsMode.Mode.TENNIS){for(var k:KEYS)while(k.consumeClick()){}return;}
  var kind=switch(m){case BASKET->Sports.Kind.BASKET;case VOLLEY->Sports.Kind.VOLLEY;default->Sports.Kind.SOCCER;};
  boolean near=holding(p)||ballNear(p,kind,m==SportsMode.Mode.KEEPER?9:5);
  for(int i=0;i<KEYS.length;i++){
   var k=KEYS[i];
   if(!near&&!(m==SportsMode.Mode.VOLLEY&&i==3)){while(k.consumeClick()){}continue;}
   if(k.same(mc.options.keyDrop))while(mc.options.keyDrop.consumeClick()){}
   if(i==4&&m!=SportsMode.Mode.SOCCER){while(k.consumeClick()){}continue;}   // V: football only (dominate the ball)
   int button=i==0?2:2+i;
   while(k.consumeClick()){if(!ready(m,button))continue;PacketDistributor.sendToServer(new SportsMode.Action(button));if(m!=SportsMode.Mode.VOLLEY&&button!=6)p.swing(InteractionHand.MAIN_HAND);}
  }
 }
 /** Called from the player model mixin after vanilla posing. */
 public static void pose(PlayerModel<?> model,LivingEntity e){
  int[] type={0};float t=progress(e,Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false),type);if(t<0)return;
  float s=Mth.sin(t*Mth.PI);
  switch(type[0]){
   case 0->{model.rightLeg.xRot=-Mth.lerp(t<.35F?t/.35F:1,0,1)*1.7F*(t<.35F?1:s+.2F)+(t<.15F?.6F*(1-t/.15F):0);model.leftArm.xRot=-.6F*s;model.rightArm.xRot=.5F*s;}   // wind-up, swing through, follow-through
   case 1->{model.rightArm.xRot=model.leftArm.xRot=-1.9F*s-.2F;model.rightArm.zRot=-.25F*s;model.leftArm.zRot=.25F*s;}           // hands up to catch
   case 2->{model.rightArm.xRot=model.leftArm.xRot=-2.9F*s;model.rightArm.zRot=-.15F;model.leftArm.zRot=.15F;}                     // jump shot, both arms over the head
   case 3->{float a=env(t,.25F,.3F);model.rightArm.xRot=model.leftArm.xRot=-1.25F*a-.15F*s;model.rightArm.yRot=-.42F*a;model.leftArm.yRot=.42F*a;   // manchete: straight arms joined in front, knees bent
    model.rightLeg.xRot=model.leftLeg.xRot=-.35F*a;}
   case 4->{float a=env(t,.2F,.25F);model.rightArm.xRot=model.leftArm.xRot=-2.55F*a-.35F*s;model.rightArm.zRot=.28F*a;model.leftArm.zRot=-.28F*a;model.rightArm.yRot=-.2F*a;model.leftArm.yRot=.2F*a;}   // toque: hands over the forehead, push up
   case 5->{float r=t<.45F?-3.05F*(t/.45F):Mth.lerp((t-.45F)/.55F,-3.05F,.55F);model.rightArm.xRot=r;model.rightArm.zRot=t<.45F?.25F*(t/.45F):.25F*(1-(t-.45F)/.55F);   // cortada: arm cocked high, whips down
    model.leftArm.xRot=-2.6F*env(t,.15F,.55F);model.leftArm.zRot=-.2F*env(t,.15F,.55F);}
   case 6->{float a=env(t,.12F,.18F);model.rightArm.xRot=model.leftArm.xRot=-3.0F*a;model.rightArm.zRot=-.12F*a;model.leftArm.zRot=.12F*a;}   // bloqueio: both arms straight up, a wall over the net
   case 7->{float toss=t<.55F?Mth.sin(t/.55F*Mth.PI):0;model.leftArm.xRot=-2.5F*toss;   // saque: left hand tosses, right arm loads and swings over
    float r=t<.35F?0:t<.65F?-2.9F*((t-.35F)/.3F):Mth.lerp((t-.65F)/.35F,-2.9F,-.9F);model.rightArm.xRot=r;}
   default->{model.rightArm.xRot=model.leftArm.xRot=-1.2F*s;model.rightArm.yRot=-.45F*s;model.leftArm.yRot=.45F*s;}               // bump: arms together in front
  }
  model.leftPants.copyFrom(model.leftLeg);
  model.rightPants.copyFrom(model.rightLeg);model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);
 }
 /** Envelope 0→1→0: rises during the first {@code in} of the move, holds, falls during the last {@code out}. */
 static float env(float t,float in,float out){return Math.min(1,Math.min(t/in,(1-t)/out));}
 /** First person: in volleyball you see both arms do the move (bump, set, spike, block, serve). */
 @SubscribeEvent public static void arms(RenderHandEvent e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;
  int[] type={0};float t=progress(p,e.getPartialTick(),type);if(t<0||type[0]<3)return;
  e.setCanceled(true);if(e.getHand()!=InteractionHand.MAIN_HAND)return;
  if(!(mc.getEntityRenderDispatcher().getRenderer(p) instanceof PlayerRenderer r))return;
  PlayerModel<AbstractClientPlayer> model=r.getModel();
  for(int side=-1;side<=1;side+=2){
   // pitch: 0 = arm pointing straight up from below the screen, 90 = pointing forward
   float pitch,lift,inward;
   switch(type[0]){
    case 3->{float a=env(t,.25F,.3F);pitch=78;lift=-.25F+a*.32F;inward=16*a;}
    case 4->{float a=env(t,.2F,.25F);pitch=18-a*6;lift=-.55F+a*.5F+Mth.sin(t*Mth.PI)*.06F;inward=8*a;}
    case 5->{if(side==1){float a=t<.45F?t/.45F:1-(t-.45F)/.55F;pitch=t<.45F?-10:Mth.lerp((t-.45F)/.55F,-10,95);lift=-.7F+a*.62F;inward=6;}else{float a=env(t,.15F,.55F);pitch=20;lift=-.7F+a*.55F;inward=4;}}
    case 6->{float a=env(t,.12F,.18F);pitch=8;lift=-.8F+a*.72F;inward=-4;}
    default->{if(side==-1){float a=t<.55F?Mth.sin(t/.55F*Mth.PI):0;pitch=15;lift=-.75F+a*.6F;inward=6;}else{float a=t<.35F?0:t<.65F?(t-.35F)/.3F:1-(t-.65F)/.35F*.6F;pitch=t<.65F?-5:Mth.lerp((t-.65F)/.35F,-5,70);lift=-.75F+a*.6F;inward=4;}}
   }
   var arm=side==1?model.rightArm:model.leftArm;var sleeve=side==1?model.rightSleeve:model.leftSleeve;
   var saved=arm.storePose();var savedS=sleeve.storePose();
   PoseStack ps=e.getPoseStack();ps.pushPose();
   ps.translate(side*.36F,-.42F+lift,-.62F);
   ps.mulPose(Axis.XP.rotationDegrees(-pitch));
   ps.mulPose(Axis.ZP.rotationDegrees(side*inward));
   ps.translate(side*.06F,-.1F,0);
   arm.setPos(0,0,0);arm.xRot=0;arm.yRot=0;arm.zRot=0;sleeve.copyFrom(arm);
   arm.render(ps,e.getMultiBufferSource().getBuffer(RenderType.entitySolid(p.getSkin().texture())),e.getPackedLight(),OverlayTexture.NO_OVERLAY);
   sleeve.render(ps,e.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(p.getSkin().texture())),e.getPackedLight(),OverlayTexture.NO_OVERLAY);
   arm.loadPose(saved);sleeve.loadPose(savedS);ps.popPose();
  }
 }
 /** First person: your leg comes up from the bottom of the screen when you kick. */
 @SubscribeEvent public static void leg(RenderHandEvent e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||e.getHand()!=InteractionHand.MAIN_HAND)return;
  int[] type={0};float t=progress(p,e.getPartialTick(),type);if(t<0||type[0]!=0)return;
  if(!(mc.getEntityRenderDispatcher().getRenderer(p) instanceof PlayerRenderer r))return;
  PlayerModel<AbstractClientPlayer> model=r.getModel();
  float s=Mth.sin(t*Mth.PI);
  PoseStack ps=e.getPoseStack();ps.pushPose();
  ps.translate(.18,-1.55+s*.85,-.55-s*.25);
  ps.mulPose(Axis.XP.rotationDegrees(-20-s*55));
  ps.mulPose(Axis.ZP.rotationDegrees(180));
  var leg=model.rightLeg;var pants=model.rightPants;var saved=leg.storePose();var savedP=pants.storePose();
  leg.setPos(0,0,0);leg.xRot=0;leg.yRot=0;leg.zRot=0;pants.copyFrom(leg);
  var buf=e.getMultiBufferSource().getBuffer(RenderType.entitySolid(p.getSkin().texture()));
  leg.render(ps,buf,e.getPackedLight(),OverlayTexture.NO_OVERLAY);
  var bufT=e.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(p.getSkin().texture()));
  pants.render(ps,bufT,e.getPackedLight(),OverlayTexture.NO_OVERLAY);
  leg.loadPose(saved);pants.loadPose(savedP);
  ps.popPose();
 }
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.options.hideGui)return;var m=SportsMode.mode(p);if(m==null)return;
  var g=e.getGuiGraphics();String s="⚽ Modo "+m.title+(holding(p)?"  •  com a bola":"");
  int y=g.guiHeight()-34;
  if(m!=SportsMode.Mode.TENNIS){
   var base=SportsMode.moves(m);String[] names=m==SportsMode.Mode.SOCCER?java.util.Arrays.copyOf(base,5):base;if(names.length==5)names[4]=holding(p)?"Soltar a bola":"Dominar / conduzir a bola";
   y=g.guiHeight()-34-names.length*10;long now=mc.level.getGameTime();
   for(int i=0;i<names.length;i++){String k=KEYS[i].getTranslatedKeyMessage().getString().toUpperCase(java.util.Locale.ROOT);
    int button=i==0?2:2+i;long wait=READY[button]-now;boolean cd=wait>0;
    g.fill(5,y+i*10+10,7+mc.font.width(k)+2,y+i*10+19,cd?0xA0400000:0xA0000000);
    g.drawString(mc.font,k,7,y+i*10+11,cd?0xFF907050:0xFFFFE070,false);
    int nx=12+mc.font.width(k);g.drawString(mc.font,names[i],nx,y+i*10+11,cd?0xFF808080:0xFFFFFFFF,true);
    if(cd){int w=mc.font.width(names[i]);float f=Math.min(1,wait/(float)SportsMode.cooldown(m,button));g.fill(nx,y+i*10+19,nx+(int)(w*f),y+i*10+20,0xFFFF7040);}}
  }
  g.drawString(mc.font,s,6,y,0xFFFFE070,true);
 }
}
