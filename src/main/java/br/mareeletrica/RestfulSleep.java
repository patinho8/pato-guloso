package br.mareeletrica;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
@EventBusSubscriber(modid=MareEletrica.ID)
public final class RestfulSleep {
 public static final int DURATION=20*60*5;
 private RestfulSleep(){}
 @SubscribeEvent public static void wake(PlayerWakeUpEvent e){
  if(!(e.getEntity() instanceof ServerPlayer p)||e.wakeImmediately()||e.updateLevel()||!p.isSleepingLongEnough())return;
  var pos=p.getSleepingPos();
  if(pos.isEmpty()||!p.level().getBlockState(pos.get()).is(MareEletrica.LED_BED.get()))return;
  p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,DURATION,0));
  p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,DURATION,0));
 }
}
