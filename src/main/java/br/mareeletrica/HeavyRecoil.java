package br.mareeletrica;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** 0.81.0: big guns (Barrett, snipers, SVD, shotguns, M249) shove the shooter back. Lying down / bipod / crouching soak most of it. */
public final class HeavyRecoil {
 private HeavyRecoil(){}
 /** camera-kick multiplier for this shot (supported positions soften it) */
 public static float camera(ServerPlayer p,WeaponFamily f){float h=f.heavyRecoil();if(h<=1F)return 1F;return ProneSystem.supported(p)?1F+(h-1F)*.25F:p.isCrouching()?1F+(h-1F)*.6F:h;}
 public static void shove(ServerPlayer p,WeaponFamily f){
  float s=f.shove();if(s<=0||p.isCreative()&&p.getAbilities().flying)return;
  if(ProneSystem.supported(p))s*=.1F;else if(p.isCrouching())s*=.45F;
  Vec3 look=p.getLookAngle();Vec3 back=new Vec3(-look.x,0,-look.z);if(back.lengthSqr()<1e-4)return;back=back.normalize().scale(s);
  p.push(back.x,p.onGround()?Math.min(.25,s*.3):0,back.z);p.hurtMarked=true;
  if(f==WeaponFamily.BARRETT&&!ProneSystem.supported(p)&&!p.isCrouching())p.causeFoodExhaustion(.4F);
 }
}
