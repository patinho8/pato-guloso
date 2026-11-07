package br.mareeletrica;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
/** Manual projectile: hostile mobs only, no terrain damage. */
public final class RoverBulletEntity extends ThrowableItemProjectile {
 public int roverId=-1;
 public RoverBulletEntity(EntityType<? extends RoverBulletEntity> type,Level level){super(type,level);}
 @Override protected Item getDefaultItem(){return MareEletrica.ROVER_BULLET.get();}
 @Override protected double getDefaultGravity(){return 0;}
 @Override protected boolean canHitEntity(Entity entity){return entity.getId()!=roverId&&super.canHitEntity(entity);}
 @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>20)discard();}
 @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide&&hit.getEntity() instanceof Enemy){hit.getEntity().hurt(damageSources().thrown(this,getOwner()),getOwner() instanceof ExterminatorEntity?14:6);if(getOwner() instanceof ExterminatorEntity e&&hit.getEntity() instanceof net.minecraft.world.entity.LivingEntity l)e.hit(l);}}
 @Override protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide)discard();}
}
