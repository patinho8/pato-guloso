package br.mareeletrica.client;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
/** Smoke grenade cloud (Counter-Strike style): big soft puffs that swell, hang in the air as a solid wall and slowly fade. */
public final class SmokeCloudParticle extends TextureSheetParticle {
 private final float maxSize;
 public SmokeCloudParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
  super(level,x,y,z,vx,vy,vz);xd=vx;yd=vy;zd=vz;hasPhysics=true;friction=.94F;gravity=-.0006F;
  pickSprite(sprites);lifetime=220+random.nextInt(80);maxSize=1.9F+random.nextFloat()*.9F;quadSize=maxSize*.35F;
  float g=.66F+random.nextFloat()*.1F;rCol=g;gCol=g;bCol=g+.02F;alpha=0;roll=random.nextFloat()*6.28F;oRoll=roll;
 }
 @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
 @Override public void tick(){
  super.tick();
  float t=(float)age/lifetime;
  quadSize+=(maxSize-quadSize)*.06F;   // swells quickly, then holds
  alpha=Math.min(1F,age/12F)*(t>.75F?Math.max(0,1-(t-.75F)/.25F):1F)*.88F;
  oRoll=roll;roll+=.002F;
 }
}
