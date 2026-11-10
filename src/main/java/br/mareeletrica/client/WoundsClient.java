package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Wounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/** 0.65: what a wounded player sees, hears and looks like.
 *  - Each bullet: the screen goes dark for a moment and gets redder (a blood vignette that grows with every hit and with low health),
 *    plus blood splats; explosions darken it too.
 *  - Breathing: calm after a hit, heavy when badly hurt/bleeding, gasping (with heartbeat) when near death.
 *  - One broken leg: you crouch; both: prone. A broken arm hangs and is not drawn in first person.
 *  - Medical items: a first-person treatment animation; the arms move in third person too.
 *  - Wounds appear as blood marks on the player model (any skin, including Quick Skin's). */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class WoundsClient {
 static final Map<Integer,Wounds.State> STATES=new HashMap<>();
 static final Wounds.State NONE=new Wounds.State();
 public static Wounds.State of(int id){return STATES.getOrDefault(id,NONE);}
 public static void init(){
  Wounds.clientState=p->of(p.getId());
  Wounds.clientSync=s->{Minecraft.getInstance().execute(()->STATES.put(s.entity(),Wounds.State.load(s.data())));};
  Wounds.clientHit=h->Minecraft.getInstance().execute(()->hit(h.kind(),h.amount()));
 }
 // ---------------- screen effects ----------------
 static float blood,bloodO,dark,darkO;static final List<long[]> HITS=new ArrayList<>();
 record Splat(float x,float y,float size,int tex,int born,int life){}
 static final List<Splat> SPLATS=new ArrayList<>();static int clock;
 static void hit(int kind,float amount){
  var mc=Minecraft.getInstance();if(mc.player==null)return;var r=mc.player.getRandom();
  if(kind==0){blood=Math.min(1F,blood+.18F+Math.min(.08F,amount*.006F));dark=Math.max(dark,.42F);HITS.add(new long[]{clock});
   int n=1+r.nextInt(2);for(int i=0;i<n;i++)SPLATS.add(new Splat(r.nextFloat(),r.nextFloat(),.1F+r.nextFloat()*.12F,r.nextInt(3),clock,240+r.nextInt(160)));
   while(SPLATS.size()>6)SPLATS.remove(0);
   breathCd=Math.min(breathCd,8);   // a sharp breath right after the hit
  }else{dark=Math.max(dark,.65F);blood=Math.min(1F,blood+.08F);}
 }
 static int recentHits(){HITS.removeIf(h->clock-h[0]>600);return HITS.size();}
 static final ResourceLocation VIGNETTE=ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/gui/wounds/vignette.png");
 static final ResourceLocation[] SPLAT={tex("gui/wounds/splat_0"),tex("gui/wounds/splat_1"),tex("gui/wounds/splat_2")};
 static ResourceLocation tex(String p){return ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/"+p+".png");}
 public static void overlay(GuiGraphics g,DeltaTracker dt){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui&&!mc.player.isAlive())return;
  float pt=dt.getGameTimeDeltaPartialTick(false);float b=Mth.lerp(pt,bloodO,blood),d=Mth.lerp(pt,darkO,dark);
  int w=g.guiWidth(),h=g.guiHeight();
  RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
  // 1) the hit itself: the screen goes dark for a moment (fades quickly)
  if(d>.01F)g.fill(0,0,w,h,((int)(d*150)<<24));
  // 2) dark, see-through red closing in from the edges; the middle stays clear. Pulses with the heartbeat when it's bad
  if(b>.01F){
   float pulse=b>.55F?(Mth.sin((clock+pt)*.42F)*.5F+.5F)*.12F*b:0;
   g.fill(0,0,w,h,((int)(b*22)<<24)|0x300000);
   g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
   g.setColor(1,1,1,Mth.clamp(b*.8F+pulse,0,.85F));g.blit(VIGNETTE,0,0,w,h,0,0,256,256,256,256);g.setColor(1,1,1,1);
  }
  // 3) a few dark drops near the edges of the screen, fading out
  for(var s:SPLATS){int age=clock-s.born();float a=Mth.clamp(1F-(float)age/s.life(),0,1)*.55F;if(a<=0)continue;int size=(int)(s.size()*h);
   float ex=s.x()<.5F?s.x()*.35F:1-(1-s.x())*.35F,ey=s.y();   // pushed to the left/right edges
   int sx=(int)(ex*(w-size)),sy=(int)(ey*(h-size));
   g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
   g.setColor(1,1,1,a);g.blit(SPLAT[s.tex()],sx,sy,size,size,0,0,96,96,96,96);g.setColor(1,1,1,1);}
  RenderSystem.enableDepthTest();
 }
 /** Tiny body next to the hotbar (left of the offhand slot): grey = ok, amber = hurt, red = broken; bleeding timer beside it. */
 public static void hud(GuiGraphics g,DeltaTracker dt){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||mc.player.isSpectator())return;var s=of(mc.player.getId());if(!s.hurt())return;
  int x=g.guiWidth()/2-91-29-12,y=g.guiHeight()-20;   // hotbar is 182 wide; the offhand slot sits 29 px to its left
  part(g,x+2,y,3,3,-1,s);                     // head
  part(g,x+2,y+4,3,6,-1,s);                   // torso
  part(g,x,y+4,1,6,Wounds.RA,s);part(g,x+6,y+4,1,6,Wounds.LA,s);
  part(g,x+2,y+11,1,6,Wounds.RL,s);part(g,x+4,y+11,1,6,Wounds.LL,s);
  if(s.bleeding()){
   int sec=s.bleed/20;boolean blink=sec<60&&(clock/8)%2==0;String t=String.format("%d:%02d",sec/60,sec%60);
   var pose=g.pose();pose.pushPose();pose.translate(x-3,y+6,0);pose.scale(.75F,.75F,1);
   g.drawString(mc.font,t,-mc.font.width(t),0,blink?0xFFFFFFFF:0xFFE03030,true);pose.popPose();
  }
 }
 static void part(GuiGraphics g,int x,int y,int w,int h,int limb,Wounds.State s){
  int c;if(limb<0)c=s.bleeding()&&limb==-1&&h==6?0xE0B83030:0xC0D8D8D8;else{float k=s.limb[limb]/(float)Wounds.LIMB_HP;c=s.broken(limb)?0xF0E02020:k>0?0xE0E0A030:0xC0D8D8D8;}
  g.fill(x,y,x+w,y+h,c);
 }
 // ---------------- breathing ----------------
 static int breathCd=40;
 static int tier(Player p,Wounds.State s){
  float hp=p.getHealth()/p.getMaxHealth();int n=recentHits(),broken=s.armsBroken()+s.legsBroken();
  if(hp<.25F||broken>=3)return 3;
  if(n>=3||hp<.5F||s.bleeding()||broken>0)return 2;
  if(n>=1||hp<.75F)return 1;
  return 0;
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();clock++;
  bloodO=blood;darkO=dark;dark=Math.max(0,dark-.035F);
  if(mc.player==null||mc.level==null){blood=0;return;}
  var p=mc.player;var s=of(p.getId());
  if(!p.isAlive()){blood=0;dark=0;SPLATS.clear();HITS.clear();return;}
  float floor=Math.max(0,(1-p.getHealth()/p.getMaxHealth())*.55F)+(s.bleeding()?.2F:0);
  blood=Math.max(floor,blood-.0012F);
  if(s.bleeding())s.bleed=Math.max(0,s.bleed-1);   // local countdown between syncs
  if(mc.isPaused()||p.isCreative()||p.isSpectator())return;
  int t=tier(p,s);
  if(t>0&&--breathCd<=0){
   // 0.86.0: no more breathing sounds — only the heartbeat when about to die
   if(t==3)mc.getSoundManager().play(SimpleSoundInstance.forUI(Wounds.HEARTBEAT.get(),1F,.8F));
   breathCd=t==3?34:t==2?48:84;
  }else if(t==0)breathCd=Math.max(breathCd,20);
 }
 /** One leg: forced crouch, no jumping or sprinting. Both legs: prone (server). */
 @SubscribeEvent public static void input(MovementInputUpdateEvent e){
  var p=e.getEntity();if(p.isCreative()||p.isSpectator())return;var s=of(p.getId());int legs=s.legsBroken();if(legs==0)return;
  var in=e.getInput();in.jumping=false;if(legs==1)in.shiftKeyDown=true;p.setSprinting(false);
 }
 // ---------------- first person ----------------
 static boolean armBroken(Player p,HumanoidArm arm){var s=of(p.getId());return s.broken(arm==HumanoidArm.LEFT?Wounds.LA:Wounds.RA);}
 @SubscribeEvent public static void bareArm(RenderArmEvent e){if(armBroken(e.getPlayer(),e.getArm()))e.setCanceled(true);}
 @SubscribeEvent public static void hand(RenderHandEvent e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;
  var arm=e.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND?p.getMainArm():p.getMainArm().getOpposite();
  // a broken arm doesn't hold anything up in front of you
  if(armBroken(p,arm)&&!(p.isUsingItem()&&p.getUsedItemHand()==e.getHand())){if(e.getItemStack().isEmpty()||e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND){e.setCanceled(true);return;}}
  if(!p.isUsingItem()||p.getUsedItemHand()!=e.getHand()||!(e.getItemStack().getItem() instanceof Wounds.MedItem m))return;
  e.setCanceled(true);
  float t=p.getTicksUsingItem()+e.getPartialTick();boolean right=arm==HumanoidArm.RIGHT;float side=right?1:-1;
  var pose=e.getPoseStack();pose.pushPose();
  switch(m.kind){
   case BANDAGE,MEDKIT->{   // wraps around in small circles, down by your body
    pose.translate(side*(.18F+Mth.sin(t*.45F)*.12F),-.48F+Mth.cos(t*.45F)*.08F,-.62F);
    pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(t*.45F)*25F));pose.mulPose(Axis.XP.rotationDegrees(-20));
   }
   case SYRINGE->{   // turns toward you, then pushes in
    float k=Mth.clamp(t/m.getUseDuration(e.getItemStack(),p),0,1);
    pose.translate(side*(.3F-k*.15F),-.38F-k*.12F,-.55F+k*.12F);pose.mulPose(Axis.YP.rotationDegrees(side*(60F+k*30F)));pose.mulPose(Axis.XP.rotationDegrees(-30F*k));
   }
  }
  pose.scale(.7F,.7F,.7F);
  mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(p,e.getItemStack(),right?ItemDisplayContext.FIRST_PERSON_RIGHT_HAND:ItemDisplayContext.FIRST_PERSON_LEFT_HAND,!right,pose,e.getMultiBufferSource(),e.getPackedLight());
  pose.popPose();
 }
 // ---------------- third person pose (called from the PlayerModel mixin) ----------------
 public static void pose(PlayerModel<?> m,LivingEntity e,float age){
  var s=STATES.get(e.getId());
  if(e.isUsingItem()&&e.getUseItem().getItem() instanceof Wounds.MedItem){
   float k=Mth.sin(age*.6F)*.15F;
   if(s==null||!s.broken(Wounds.RA)){m.rightArm.xRot=-1.15F+k;m.rightArm.yRot=-.45F;m.rightArm.zRot=0;}
   if(s==null||!s.broken(Wounds.LA)){m.leftArm.xRot=-1.15F-k;m.leftArm.yRot=.45F;m.leftArm.zRot=0;}
  }
  if(s!=null){
   float sway=Mth.sin(age*.07F)*.05F;
   if(s.broken(Wounds.LA)){m.leftArm.xRot=.12F+sway;m.leftArm.yRot=0;m.leftArm.zRot=-.14F;}
   if(s.broken(Wounds.RA)){m.rightArm.xRot=.12F-sway;m.rightArm.yRot=0;m.rightArm.zRot=.14F;}
  }
  m.leftSleeve.copyFrom(m.leftArm);m.rightSleeve.copyFrom(m.rightArm);
 }
 // ---------------- blood marks on the body ----------------
 static final ResourceLocation[] WOUND={tex("entity/wound_0"),tex("entity/wound_1"),tex("entity/wound_2")};
 public static final class WoundLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
  public WoundLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> p){super(p);}
  @Override public void render(PoseStack pose,MultiBufferSource buf,int light,AbstractClientPlayer pl,float swing,float amount,float pt,float age,float yaw,float pitch){
   var s=STATES.get(pl.getId());if(s==null||s.decals.isEmpty()||pl.isInvisible())return;
   var m=getParentModel();boolean slim=pl.getSkin().model()==PlayerSkin.Model.SLIM;
   int i=0;
   for(var d:s.decals){
    var part=switch(d.zone()){case 0->m.head;case 1->m.body;case 2->m.leftArm;case 3->m.rightArm;case 4->m.leftLeg;default->m.rightLeg;};
    if(!part.visible)continue;
    float[] b=switch(d.zone()){case 0->new float[]{-4,-8,-4,4,0,4};case 1->new float[]{-4,0,-2,4,12,2};case 2->new float[]{-1,-2,-2,slim?2:3,10,2};case 3->new float[]{slim?-2:-3,-2,-2,1,10,2};default->new float[]{-2,0,-2,2,12,2};};
    EquipmentSlot slot=d.zone()==0?EquipmentSlot.HEAD:d.zone()>=4?EquipmentSlot.LEGS:EquipmentSlot.CHEST;
    float off=pl.getItemBySlot(slot).isEmpty()?(d.zone()==0?.55F:.3F):1.12F;
    pose.pushPose();part.translateAndRotate(pose);
    quad(pose,buf.getBuffer(RenderType.entityCutoutNoCull(WOUND[(i+d.face())%3])),light,b,d,off,2.6F);
    pose.popPose();i++;
   }
  }
  static void quad(PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer vc,int light,float[] b,Wounds.Decal d,float off,float size){
   float hs=size/2,x0=b[0],y0=b[1],z0=b[2],x1=b[3],y1=b[4],z1=b[5];
   float y=Mth.clamp(y0+d.v()*(y1-y0),y0+hs,y1-hs);
   float[][] v;float nx=0,nz=0;
   switch(d.face()){
    case 0->{float x=Mth.clamp(x1-d.u()*(x1-x0),x0+hs,x1-hs),z=z0-off;v=new float[][]{{x-hs,y-hs,z},{x+hs,y-hs,z},{x+hs,y+hs,z},{x-hs,y+hs,z}};nz=-1;}
    case 1->{float x=Mth.clamp(x0+d.u()*(x1-x0),x0+hs,x1-hs),z=z1+off;v=new float[][]{{x+hs,y-hs,z},{x-hs,y-hs,z},{x-hs,y+hs,z},{x+hs,y+hs,z}};nz=1;}
    case 2->{float z=Mth.clamp(z0+d.u()*(z1-z0),z0+hs,z1-hs),x=x1+off;v=new float[][]{{x,y-hs,z-hs},{x,y-hs,z+hs},{x,y+hs,z+hs},{x,y+hs,z-hs}};nx=1;}
    default->{float z=Mth.clamp(z0+d.u()*(z1-z0),z0+hs,z1-hs),x=x0-off;v=new float[][]{{x,y-hs,z+hs},{x,y-hs,z-hs},{x,y+hs,z-hs},{x,y+hs,z+hs}};nx=-1;}
   }
   float[][] uv={{0,0},{1,0},{1,1},{0,1}};var last=pose.last();
   for(int k=0;k<4;k++)vc.addVertex(last,v[k][0]/16F,v[k][1]/16F,v[k][2]/16F).setColor(255,255,255,255).setUv(uv[k][0],uv[k][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(last,nx,0,nz);
  }
 }
}
