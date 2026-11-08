package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Pato;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/** 0.69.0: players under the Duck Potion are drawn as a duck; the local one can flap its wings in the air (jump). */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class DuckClient {
 private DuckClient(){}
 static final Map<UUID,Pato.Duck> LOOKS=new HashMap<>();
 static int flapCd,flaps;static boolean jumpWas;
 static Pato.Duck look(Player p){
  var d=LOOKS.get(p.getUUID());
  if(d==null||d.level()!=p.level()){d=Pato.ENTITY.get().create(p.level());LOOKS.put(p.getUUID(),d);}
  return d;}
 @SubscribeEvent public static void render(RenderPlayerEvent.Pre e){
  var p=e.getEntity();if(!Pato.isDuck(p))return;e.setCanceled(true);
  var d=look(p);float pt=e.getPartialTick();
  d.yBodyRotO=p.yHeadRotO;d.yBodyRot=p.yHeadRot;d.yHeadRotO=p.yHeadRotO;d.yHeadRot=p.yHeadRot;d.xRotO=p.xRotO;d.setXRot(p.getXRot());d.tickCount=p.tickCount;
  d.fakeWalkPos=p.walkAnimation.position(pt)*.9F;d.fakeWalkSpd=Math.min(1,p.walkAnimation.speed(pt)*1.6F);d.fakeSwim=p.isInWater();d.hurtTime=p.hurtTime;
  boolean air=!p.onGround()&&!p.isInWater()&&!p.isPassenger();d.flapO=d.flap;d.flap=air?d.flap+.5F:d.flap*.8F;
  var r=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(d);
  if(r instanceof DuckRenderer dr)dr.render(d,p.getYRot(),pt,e.getPoseStack(),e.getMultiBufferSource(),e.getPackedLight());
 }
 @SubscribeEvent public static void hand(RenderHandEvent e){var p=Minecraft.getInstance().player;if(p!=null&&Pato.isDuck(p)&&e.getItemStack().isEmpty())e.setCanceled(true);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;
  if(!Pato.isDuck(p)){flaps=0;return;}
  if(p.onGround()||p.isInWater())flaps=0;
  if(flapCd>0)flapCd--;
  boolean jump=p.input!=null&&p.input.jumping;
  if(jump&&!jumpWas&&!p.onGround()&&!p.isInWater()&&flapCd==0&&flaps<4&&!p.getAbilities().flying){
   var v=p.getDeltaMovement();var look=p.getLookAngle();p.setDeltaMovement(v.x+look.x*.12,.42,v.z+look.z*.12);flapCd=6;flaps++;
   for(int i=0;i<4;i++)p.level().addParticle(ParticleTypes.CLOUD,p.getRandomX(.4),p.getY()+.2,p.getRandomZ(.4),0,-.05,0);
   p.playSound(net.minecraft.sounds.SoundEvents.PHANTOM_FLAP,.4F,1.8F);}
  jumpWas=jump;
  if(mc.level!=null&&mc.level.getGameTime()%200==0)LOOKS.keySet().removeIf(id->mc.level.getPlayerByUUID(id)==null);
 }
}
