package br.mareeletrica;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
/** A bounded swept ray: thin panes and several blocks in one tick cannot skip a target. */
public final class SniperBallistics {
 public static final TagKey<Block> PENETRABLE=TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"sniper_penetrable"));
 public static final TagKey<EntityType<?>> BOSSES=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"sniper_base_damage"));
 private static final TagKey<EntityType<?>> COMMON_BOSSES=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("c","bosses"));
 public static final TagKey<EntityType<?>> SOFT_TARGETS=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"sniper_pierceable"));
 public static boolean pierceable(Entity entity){return entity instanceof LivingEntity&&entity.getType().is(SOFT_TARGETS)&&damage(entity)>20;}
 public static float damage(Entity target){Entity actual=target instanceof net.minecraft.world.entity.boss.EnderDragonPart part?part.parentMob:target;return actual instanceof IronGolem||actual instanceof WitherBoss||actual instanceof EnderDragon||actual.getType().is(BOSSES)||actual.getType().is(COMMON_BOSSES)?20F:60F;}
 public static HitResult trace(DmtProjectileEntity bullet,Set<BlockPos> crossed){var from=bullet.position();var to=from.add(bullet.getDeltaMovement());
  var block=bullet.level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,bullet){
   @Override public VoxelShape getBlockShape(BlockState state,BlockGetter world,BlockPos pos){var shape=super.getBlockShape(state,world,pos);if(state.is(PENETRABLE)&&!shape.isEmpty()&&shape.clip(from,to,pos)!=null&&(crossed.contains(pos)||crossed.size()<4)){crossed.add(pos.immutable());return Shapes.empty();}return shape;}
  });
  Vec3 end=block.getType()==HitResult.Type.MISS?to:block.getLocation();var entity=ProjectileUtil.getEntityHitResult(bullet.level(),bullet,from,end,bullet.getBoundingBox().expandTowards(bullet.getDeltaMovement()).inflate(1),bullet::canStrike);
  return entity!=null?entity:block;
 }
}
