package br.mareeletrica;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** 0.74.0: the tactical flashlight really lights where the gun points (invisible light blocks that follow the aim). */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class AccessoryLight {
 private AccessoryLight(){}
 record Lit(ServerLevel level,BlockPos[] at){}
 static final Map<UUID,Lit> LIT=new HashMap<>();
 public static HitResult ray(Player p,Vec3 eye,Vec3 look,double range){
  Vec3 end=eye.add(look.scale(range));BlockHitResult block=p.level().clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
  Vec3 stop=block.getType()==HitResult.Type.MISS?end:block.getLocation();
  var ent=ProjectileUtil.getEntityHitResult(p.level(),p,eye,stop,new AABB(eye,stop).inflate(1),e->!e.isSpectator()&&e.isPickable(),0F);
  return ent!=null?ent:block;
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;
  var gun=p.getMainHandItem();var k=WeaponFamily.of(gun)==null?null:Accessory.beam(gun);
  if(k==null||!k.light()||!p.isAlive()||p.isSpectator()){clear(p.getUUID());return;}
  if(p.tickCount%2!=0)return;
  Vec3 eye=p.getEyePosition(),look=p.getViewVector(1);var hit=ray(p,eye,look,40);
  Vec3 at=hit.getLocation().subtract(look.scale(.6));double d=at.distanceTo(eye);
  BlockPos a=spot(p.serverLevel(),BlockPos.containing(at)),b=d>9?spot(p.serverLevel(),BlockPos.containing(eye.add(look.scale(d*.5)))):null,c=spot(p.serverLevel(),BlockPos.containing(eye.add(look.scale(Math.min(3,d)))));
  var now=new BlockPos[]{a,b,c};var old=LIT.get(p.getUUID());
  if(old!=null)for(BlockPos o:old.at)if(o!=null&&!contains(now,o)&&old.level.getBlockState(o).is(Blocks.LIGHT))old.level.removeBlock(o,false);
  int[] lv={15,12,10};for(int i=0;i<3;i++)if(now[i]!=null&&p.level().getBlockState(now[i]).isAir())p.level().setBlock(now[i],Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,lv[i]),3);
  LIT.put(p.getUUID(),new Lit(p.serverLevel(),now));
 }
 static boolean contains(BlockPos[] a,BlockPos o){for(var x:a)if(o.equals(x))return true;return false;}
 static BlockPos spot(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.isAir()||s.is(Blocks.LIGHT)?at:null;}
 static void clear(UUID id){var old=LIT.remove(id);if(old!=null)for(BlockPos o:old.at)if(o!=null&&old.level.getBlockState(o).is(Blocks.LIGHT))old.level.removeBlock(o,false);}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){clear(e.getEntity().getUUID());}
 @SubscribeEvent public static void dim(PlayerEvent.PlayerChangedDimensionEvent e){clear(e.getEntity().getUUID());}
}
