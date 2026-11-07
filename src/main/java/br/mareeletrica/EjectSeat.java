package br.mareeletrica;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** 0.65.4: after an ejection, opens the parachute by itself a moment later and keeps fall damage off until then. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class EjectSeat {
 private static final Map<UUID,Integer> PENDING=new HashMap<>();
 private EjectSeat(){}
 public static void arm(ServerPlayer p){PENDING.put(p.getUUID(),14);}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;var left=PENDING.get(p.getUUID());if(left==null)return;
  p.fallDistance=0;
  if(p.onGround()||p.isInWater()){PENDING.remove(p.getUUID());return;}
  if(left>0){PENDING.put(p.getUUID(),left-1);return;}
  PENDING.remove(p.getUUID());Parachutes.tryDeploy(p,false);
 }
}
