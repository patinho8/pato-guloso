package br.mareeletrica;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Fictional Minecraft artillery projectile; explosion follows normal game explosion rules. */
public final class CannonballEntity extends ThrowableItemProjectile {
    public CannonballEntity(EntityType<? extends CannonballEntity> type,Level level){super(type,level);}
    @Override protected Item getDefaultItem(){return MareEletrica.CANNONBALL.get();}
    @Override protected double getDefaultGravity(){return .035;}
    @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>400)discard();}
    @Override protected void onHit(HitResult hit){
        super.onHit(hit);
        if(!level().isClientSide){level().explode(this,getX(),getY(),getZ(),4.5F,Level.ExplosionInteraction.TNT);discard();}
    }
}
