package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Wounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/** 0.86.0: every explosion (TNT, creepers, grenades, C4, missiles, vehicles…) sounds better:
 *  a close blast with crack and sub-bass, a far rolling rumble with echoes heard from far away, and debris falling after. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class ExplosionSounds {
 private ExplosionSounds(){}
 record Pending(SoundEvent ev,double x,double y,double z,float vol,float pitch,int delay){}
 static final List<Pending> QUEUE=new ArrayList<>();
 static final RandomSource R=RandomSource.create();
 static boolean boom(ResourceLocation l){
  return l.equals(SoundEvents.GENERIC_EXPLODE.value().getLocation())||l.equals(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"frag_blast"));}
 @SubscribeEvent public static void play(PlaySoundEvent e){
  SoundInstance s=e.getOriginalSound();if(s==null||!boom(s.getLocation()))return;
  var mc=Minecraft.getInstance();if(mc.gameRenderer==null)return;
  double x=s.getX(),y=s.getY(),z=s.getZ();e.setSound(null);
  double dist=s.isRelative()?0:mc.gameRenderer.getMainCamera().getPosition().distanceTo(new net.minecraft.world.phys.Vec3(x,y,z));
  int travel=(int)(dist/17);   // the sound arrives a bit later the farther away it is
  float big=Math.min(1F,.6F+.1F*Math.max(1F,s.getVolume()));   // TNT/creeper (volume 4) = full
  float p=.88F+R.nextFloat()*.2F;
  QUEUE.add(new Pending(Wounds.EXP_NEAR.get(),x,y,z,big,p,travel));
  QUEUE.add(new Pending(Wounds.EXP_FAR.get(),x,y,z,big*(dist<20?.55F:1F),p*.95F,travel+1));
  if(dist<48)QUEUE.add(new Pending(Wounds.EXP_DEBRIS.get(),x,y,z,.7F*big,.9F+R.nextFloat()*.25F,travel+6+R.nextInt(5)));
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(QUEUE.isEmpty())return;var mc=Minecraft.getInstance();
  if(mc.level==null){QUEUE.clear();return;}
  var copy=new ArrayList<>(QUEUE);QUEUE.clear();
  for(var q:copy){
   if(q.delay()>0){QUEUE.add(new Pending(q.ev(),q.x(),q.y(),q.z(),q.vol(),q.pitch(),q.delay()-1));continue;}
   mc.getSoundManager().play(new SimpleSoundInstance(q.ev(),net.minecraft.sounds.SoundSource.BLOCKS,q.vol(),q.pitch(),R,q.x(),q.y(),q.z()));}
 }
}
