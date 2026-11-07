package br.mareeletrica;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
/** Advance only the sniper rounds outside normal simulation, never double tick a round. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class ScopedProjectiles {
 private static final Set<DmtProjectileEntity> ACTIVE=Collections.newSetFromMap(new IdentityHashMap<>());
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(!e.getLevel().isClientSide()&&e.getEntity() instanceof DmtProjectileEntity b&&b.isSniper())ACTIVE.add(b);}
 @SubscribeEvent public static void leave(EntityLeaveLevelEvent e){if(e.getEntity() instanceof DmtProjectileEntity b)ACTIVE.remove(b);}
 @SubscribeEvent public static void tick(LevelTickEvent.Post e){if(!(e.getLevel() instanceof ServerLevel level))return;
  for(var b:List.copyOf(ACTIVE)){
   if(b.isRemoved()){ACTIVE.remove(b);continue;}
   if(b.level()!=level||b.lastServerTick==level.getGameTime())continue;
   var c=b.chunkPosition();
   if(!level.isPositionEntityTicking(b.blockPosition())&&!level.tickRateManager().isEntityFrozen(b)){
    if(level.hasChunk(c.x,c.z))level.tickNonPassenger(b);else b.discard();
   }
  }
 }
 @SubscribeEvent public static void stop(ServerStoppedEvent e){ACTIVE.clear();}
}
