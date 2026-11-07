package br.mareeletrica.client;

import br.mareeletrica.Skate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/** 0.65.8: the skater pose. The body stands sideways on the board (left foot forward), legs apart along the deck, arms out for
 *  balance — then: back foot kicking when pushing, deep crouch with tucked legs and arms up in the air, arms wide on a grind,
 *  leaning back when braking and into the turn. */
public final class SkatePose {
 private SkatePose(){}
 public static void pose(PlayerModel<?> m,LivingEntity e){
  if(!(e.getVehicle() instanceof Skate.Board b))return;
  int f=b.anim();float t=e.tickCount+Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
  float wob=Mth.sin(t*.13F)*.07F,crouch=1F;
  // base stance
  m.rightLeg.xRot=0;m.leftLeg.xRot=0;m.rightLeg.yRot=0;m.leftLeg.yRot=0;
  m.rightLeg.zRot=.3F;m.leftLeg.zRot=-.3F;
  m.rightArm.xRot=-.15F;m.leftArm.xRot=-.15F;m.rightArm.yRot=0;m.leftArm.yRot=0;
  m.rightArm.zRot=.8F+wob;m.leftArm.zRot=-.8F+wob;
  m.body.xRot=0;m.body.zRot=0;m.head.zRot=0;
  int tr=(f>>Skate.TRICK_SHIFT&7)-1;
  if((f&Skate.AIR)!=0){                         // in the air: crouched, knees up, arms high
   crouch=3F;m.rightLeg.xRot=-.7F;m.leftLeg.xRot=-.7F;m.rightLeg.zRot=.45F;m.leftLeg.zRot=-.45F;
   m.rightArm.zRot=1.5F;m.leftArm.zRot=-1.5F;m.rightArm.xRot=-.4F;m.leftArm.xRot=-.4F;
   if(tr==1||tr==4){m.leftLeg.zRot=-.85F;m.leftLeg.xRot=-.3F;}   // kickflip / 360 flip: front foot flicks off the toe edge
   if(tr==2){m.leftLeg.zRot=-.85F;m.leftLeg.xRot=.35F;}           // heelflip: off the heel edge
   if(tr==3||tr==4){m.rightLeg.zRot=.75F;m.rightLeg.xRot=-.2F;}   // shove-it: the back foot scoops
   if(tr==5){m.rightArm.zRot=.6F;m.rightArm.xRot=-1.1F;m.leftArm.zRot=-1.6F;}   // ramp air: grab
  }else if((f&Skate.GRIND)!=0){                 // grind: low and arms wide; boardslide: body square to the rail
   crouch=2.5F;m.rightArm.zRot=1.35F+Mth.sin(t*.6F)*.12F;m.leftArm.zRot=-1.35F+Mth.sin(t*.6F+1)*.12F;m.rightLeg.zRot=.4F;m.leftLeg.zRot=-.4F;
   if((f&Skate.SLIDE)!=0){m.rightLeg.zRot=.15F;m.leftLeg.zRot=-.15F;m.rightArm.xRot=-.9F;m.leftArm.xRot=-.9F;}
  }else if((f&Skate.MANUAL)!=0){                // manual: weight on the back foot, front leg straight, arms balancing
   crouch=1.6F;m.rightLeg.zRot=.15F;m.leftLeg.zRot=-.6F;m.body.zRot=.08F;m.rightArm.zRot=1.25F+Mth.sin(t*.5F)*.15F;m.leftArm.zRot=-1.35F+Mth.sin(t*.5F+2)*.15F;
  }else if((f&Skate.PUSH)!=0){                  // push: bend on the front leg, the back foot reaches down, sweeps back along the board, then comes back up
   float ph=(t*.075F)%1F,sweep,lift;
   if(ph<.62F){float k=ph/.62F;sweep=Mth.lerp(k,.35F,1.25F);lift=0;}       // foot on the ground, pushing back
   else{float k=(ph-.62F)/.38F;sweep=Mth.lerp(k,1.25F,.35F);lift=Mth.sin(k*Mth.PI)*.45F;}   // foot lifts and swings forward again
   crouch=2.6F;m.leftLeg.zRot=-.04F;m.rightLeg.zRot=sweep;m.rightLeg.xRot=-lift;
   m.body.zRot=.12F;m.head.zRot=-.08F;
   m.leftArm.zRot=-.35F-.5F*(sweep-.35F);m.rightArm.zRot=.45F+.35F*(sweep-.35F);m.leftArm.xRot=-.35F;m.rightArm.xRot=.25F*(sweep-.8F);
  }else if((f&Skate.BRAKE)!=0){                 // brake: weight on the back foot, arms forward
   crouch=1.5F;m.rightLeg.zRot=.15F;m.leftLeg.zRot=-.5F;m.leftArm.zRot=-1.2F;m.rightArm.zRot=.35F;m.leftArm.xRot=-.6F;
  }
  if((f&Skate.LEFT)!=0){m.leftArm.zRot-=.3F;m.rightArm.zRot-=.3F;}
  if((f&Skate.RIGHT)!=0){m.leftArm.zRot+=.3F;m.rightArm.zRot+=.3F;}
  // bend the knees: lower everything above the legs (the legs stay planted on the deck)
  m.body.y+=crouch;m.head.y+=crouch;m.rightArm.y+=crouch;m.leftArm.y+=crouch;
  m.hat.copyFrom(m.head);m.jacket.copyFrom(m.body);m.leftSleeve.copyFrom(m.leftArm);m.rightSleeve.copyFrom(m.rightArm);
  m.leftPants.copyFrom(m.leftLeg);m.rightPants.copyFrom(m.rightLeg);
 }
}
