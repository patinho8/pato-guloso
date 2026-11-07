package br.mareeletrica;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
/** A three-dimensional, occluded cone that follows the lamp's lens. */
public final class FloodlightBeam {
 public static final int RANGE=48;
 private FloodlightBeam(){}
 public static Vec3 direction(MachineEntity lamp){return Vec3.directionFromRotation(lamp.lampPitch,lamp.lampYaw-180);}
 public static Vec3 origin(MachineEntity lamp){return lamp.getBlockPos().getCenter().add(0,.25,0).add(direction(lamp).scale(.5));}
 public static void clear(MachineEntity lamp){if(lamp.getLevel()==null||lamp.getLevel().isClientSide)return;for(var p:lamp.lampLights)release(lamp,p);lamp.lampLights.clear();}
 private static void release(MachineEntity lamp,BlockPos p){var level=lamp.getLevel();if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof BeamLightEntity light){light.release(lamp.getBlockPos().asLong(),level.getGameTime());if(light.expires<=level.getGameTime()&&level.getBlockState(p).is(MareEletrica.BEAM_LIGHT.get()))level.removeBlock(p,false);}}
 public static void refresh(MachineEntity lamp){var level=lamp.getLevel();if(level==null||level.isClientSide)return;long now=level.getGameTime();if(now-lamp.beamUpdated<4)return;lamp.beamUpdated=now;Vec3 origin=origin(lamp),forward=direction(lamp),side=Vec3.directionFromRotation(0,lamp.lampYaw-90),up=side.cross(forward).normalize();Set<BlockPos> targets=new HashSet<>();
  for(int distance=4;distance<=RANGE;distance+=4){int radius=(int)(distance*.22);for(int x=-radius;x<=radius;x+=4)for(int y=-radius;y<=radius;y+=4){if(x*x+y*y>radius*radius+1)continue;add(lamp,origin,origin.add(forward.scale(distance)).add(side.scale(x)).add(up.scale(y)),targets);}add(lamp,origin,origin.add(forward.scale(distance)),targets);}
  for(var p:lamp.lampLights)if(!targets.contains(p))release(lamp,p);lamp.lampLights.clear();lamp.lampLights.addAll(targets);
 }
 private static void add(MachineEntity lamp,Vec3 origin,Vec3 end,Set<BlockPos> targets){var level=lamp.getLevel();BlockPos p=BlockPos.containing(end);if(level.isOutsideBuildHeight(p)||!level.hasChunkAt(p)||p.equals(lamp.getBlockPos()))return;var state=level.getBlockState(p);if(!state.isAir()&&!state.is(MareEletrica.BEAM_LIGHT.get())||!level.getFluidState(p).isEmpty())return;var hit=level.clip(new ClipContext(origin,Vec3.atCenterOf(p),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));if(hit.getType()!=HitResult.Type.MISS)return;if(!state.is(MareEletrica.BEAM_LIGHT.get()))level.setBlock(p,MareEletrica.BEAM_LIGHT.get().defaultBlockState(),3);if(level.getBlockEntity(p) instanceof BeamLightEntity light){light.refreshFrom(lamp.getBlockPos().asLong(),level.getGameTime());level.scheduleTick(p,MareEletrica.BEAM_LIGHT.get(),20);targets.add(p.immutable());}}
}
