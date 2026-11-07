package br.mareeletrica;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 0.65.4: the rare golden egg. Placed on the ground it wobbles, cracks louder and louder and after 15 s a Golden Rooster hatches, already yours.
 *  Your tamed roosters lay one now and then (very rarely), and winning a ring battle has a 1 in 5 chance of giving one. */
public final class RareEgg extends Entity {
 public static final int HATCH=300;
 public static final Supplier<EntityType<RareEgg>> TYPE=MareEletrica.ACTORS.register("rare_egg",()->EntityType.Builder.<RareEgg>of(RareEgg::new,MobCategory.MISC).sized(.45F,.55F).clientTrackingRange(8).build(MareEletrica.ID+":rare_egg"));
 public UUID owner;public int age;public float wobble,wobbleO;
 public RareEgg(EntityType<? extends RareEgg> t,Level l){super(t,l);}
 public static void register(){}
 public static ItemStack item(){return new ItemStack(FightChickens.BREED_ITEMS.get(ChickenTypes.Breed.DOURADO.ordinal()).get());}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 @Override public boolean isPickable(){return true;}
 @Override public void tick(){
  super.tick();age++;
  if(!onGround())setDeltaMovement(getDeltaMovement().add(0,-.04,0));move(net.minecraft.world.entity.MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale(.8));
  // wobbles more and more often as it gets close
  float k=age/(float)HATCH;wobbleO=wobble;
  if(level().isClientSide){int every=Math.max(8,(int)(60-k*50));wobble=age%every<10?(float)Math.sin((age%every)*.9)*(.25F+k*.35F):0;
   if(random.nextInt(12)==0)level().addParticle(new DustParticleOptions(new org.joml.Vector3f(1F,.82F,.2F),.8F),getX()+random.nextGaussian()*.2,getY()+.3+random.nextDouble()*.3,getZ()+random.nextGaussian()*.2,0,.02,0);
   return;}
  int every=Math.max(8,(int)(60-k*50));
  if(age%every==0)level().playSound(null,blockPosition(),SoundEvents.TURTLE_EGG_CRACK,SoundSource.NEUTRAL,.5F+k,1.3F-k*.4F);
  if(age>=HATCH)hatch();
 }
 void hatch(){
  var l=(ServerLevel)level();var c=FightChickens.ENTITY.get().create(l);if(c==null){discard();return;}
  c.moveTo(getX(),getY(),getZ(),random.nextFloat()*360,0);c.setBreed(ChickenTypes.Breed.DOURADO);
  if(owner!=null){c.setTame(true,true);c.setOwnerUUID(owner);}
  l.addFreshEntity(c);
  l.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,getX(),getY()+.5,getZ(),60,.4,.5,.4,.5);
  l.sendParticles(new DustParticleOptions(new org.joml.Vector3f(1F,.85F,.25F),1.5F),getX(),getY()+.4,getZ(),40,.5,.4,.5,0);
  l.playSound(null,blockPosition(),SoundEvents.TURTLE_EGG_HATCH,SoundSource.NEUTRAL,1.2F,1F);
  l.playSound(null,blockPosition(),SoundEvents.PLAYER_LEVELUP,SoundSource.NEUTRAL,1F,1.4F);
  if(owner!=null&&l.getPlayerByUUID(owner) instanceof Player p)p.displayClientMessage(Component.literal("§6§l✦ Nasceu um GALO DOURADO! ✦ §7(raça rara, só sai desse ovo)"),false);
  discard();
 }
 /** Punch it to take it back before it hatches. */
 @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float a){
  if(level().isClientSide||isRemoved())return false;
  if(s.getEntity() instanceof Player p){if(!p.isCreative()&&!p.getInventory().add(item()))spawnAtLocation(item());discard();return true;}
  return false;
 }
 @Override public InteractionResult interact(Player p,InteractionHand h){
  if(!level().isClientSide)p.displayClientMessage(Component.literal("§6Ovo raro: "+Math.max(0,(HATCH-age)/20)+" s para chocar."),true);return InteractionResult.SUCCESS;}
 @Override protected void readAdditionalSaveData(CompoundTag t){age=t.getInt("Age");owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;}
 @Override protected void addAdditionalSaveData(CompoundTag t){t.putInt("Age",age);if(owner!=null)t.putUUID("Owner",owner);}
 /** A tamed rooster lays one now and then (≈ once every 3 in-game days of being out). */
 static void maybeLay(FightChickens.FightChicken c){
  if(!c.isTame()||c.isBaby()||c.getRandom().nextInt(72000)!=0)return;
  c.spawnAtLocation(item());c.playSound(SoundEvents.CHICKEN_EGG,1F,.7F);
  if(c.getOwner() instanceof Player p)p.displayClientMessage(Component.literal("§6✦ Seu "+c.breed().label+" botou um OVO RARO!"),false);
 }
}
