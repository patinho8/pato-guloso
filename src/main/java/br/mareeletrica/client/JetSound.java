package br.mareeletrica.client;

import br.mareeletrica.Jets;
import br.mareeletrica.MareEletrica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/** Jet engine loop that follows the aircraft; louder and higher with throttle. */
public final class JetSound extends AbstractTickableSoundInstance {
 private final Jets.JetEntity jet;
 private JetSound(Jets.JetEntity jet){super(MareEletrica.JET_ENGINE.get(),SoundSource.NEUTRAL,SoundInstance.createUnseededRandom());this.jet=jet;looping=true;delay=0;volume=.2F;pitch=.6F;x=jet.getX();y=jet.getY();z=jet.getZ();}
 public static void start(Jets.JetEntity jet){var s=new JetSound(jet);jet.engineSound=s;Minecraft.getInstance().getSoundManager().play(s);}
 @Override public void tick(){
  if(jet.isRemoved()){stop();return;}
  x=jet.getX();y=jet.getY();z=jet.getZ();
  float t=jet.isControlledByLocalInstance()?jet.throttle:jet.syncedThrottle();
  volume=.18F+t*1.1F;pitch=.55F+t*.75F+(float)jet.getDeltaMovement().length()*.1F;
 }
}
