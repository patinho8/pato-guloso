package br.mareeletrica;
import net.minecraft.world.phys.Vec3;
/** Fixed-length cable: constrain radial travel while preserving tangential movement. */
public final class DmtRope {
 private DmtRope(){}
 public static Vec3 constrain(Vec3 hip,Vec3 velocity,Vec3 anchor,double length){
  Vec3 next=hip.add(velocity).subtract(anchor);double distance=next.length();
  if(distance<=length||distance<1.0E-6)return velocity;
  return anchor.add(next.scale(length/distance)).subtract(hip);
 }
}
