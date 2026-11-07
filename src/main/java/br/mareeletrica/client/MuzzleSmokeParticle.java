package br.mareeletrica.client;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
/** Short-lived translucent muzzle haze. */
public final class MuzzleSmokeParticle extends TextureSheetParticle {
 private final SpriteSet sprites;private final float initialAlpha;
 public MuzzleSmokeParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){super(level,x,y,z,vx,vy,vz);this.sprites=sprites;xd=vx;yd=vy+.008;zd=vz;hasPhysics=false;friction=.92F;quadSize=.065F+random.nextFloat()*.025F;lifetime=14+random.nextInt(7);rCol=.72F;gCol=.75F;bCol=.78F;alpha=initialAlpha=.20F;setSpriteFromAge(sprites);}
 @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
 @Override public void tick(){super.tick();alpha=initialAlpha*Math.max(0,1-(float)age/lifetime);quadSize+=.003F;setSpriteFromAge(sprites);}
}
