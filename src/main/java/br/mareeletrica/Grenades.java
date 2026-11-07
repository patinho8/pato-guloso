package br.mareeletrica;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

/** Grenades (frag, flash, smoke, incendiary). Own physics: they bounce off walls, roll on the ground, sink in water. They never break blocks. */
public final class Grenades {
 public enum Type {
  FRAG("frag_grenade",60), FLASH("flash_grenade",40), SMOKE("smoke_grenade",30), FIRE("incendiary_grenade",45);
  public final String id; public final int fuse;
  Type(String id,int fuse){this.id=id;this.fuse=fuse;}
 }

 public static final DeferredItem<GrenadeItem> FRAG=grenade(Type.FRAG),FLASH=grenade(Type.FLASH),SMOKE=grenade(Type.SMOKE),FIRE=grenade(Type.FIRE);
 public static final Supplier<EntityType<GrenadeEntity>> GRENADE=MareEletrica.ACTORS.register("grenade",()->
  EntityType.Builder.<GrenadeEntity>of(GrenadeEntity::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(8).updateInterval(1).build(MareEletrica.ID+":grenade"));

 private static DeferredItem<GrenadeItem> grenade(Type t){return MareEletrica.ITEMS.register(t.id,()->new GrenadeItem(t,new Item.Properties().stacksTo(8)));}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(FRAG.get());out.accept(FLASH.get());out.accept(SMOKE.get());out.accept(FIRE.get());}

 public static final class GrenadeItem extends Item {
  public final Type type;
  public GrenadeItem(Type type,Properties properties){super(properties);this.type=type;}
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
   var stack=p.getItemInHand(hand);
   if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(stack);
   if(!level.isClientSide){
    boolean underhand=p.isShiftKeyDown();
    var g=new GrenadeEntity(GRENADE.get(),level);g.setKind(type);g.setOwner(p);
    g.setPos(p.getX(),p.getEyeY()-(underhand?.5:.15),p.getZ());
    g.shootFromRotation(p,underhand?Math.min(p.getXRot(),10F):p.getXRot()-4F,p.getYRot(),0F,underhand?.55F:1.15F,.6F);
    level.addFreshEntity(g);
    level.playSound(null,p.blockPosition(),SoundEvents.SNOWBALL_THROW,SoundSource.PLAYERS,.7F,.6F);
    level.playSound(null,p.blockPosition(),SoundEvents.TRIPWIRE_CLICK_OFF,SoundSource.PLAYERS,.8F,1.6F);
   }
   if(!p.isCreative())stack.shrink(1);
   p.getCooldowns().addCooldown(this,12);
   return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> tips,net.minecraft.world.item.TooltipFlag f){
   tips.add(Component.literal("M2: arremessar • Shift+M2: rolar por baixo"));
   tips.add(Component.literal(switch(type){case FRAG->"Explode em 3 s. Dano de explosão, não quebra blocos";case FLASH->"Deixa a tela branca e com zumbido";case SMOKE->"Nuvem de fumaça grossa por 25 s";case FIRE->"Espalha fogo no chão e em quem estiver perto";}));
  }
 }

 public static final class GrenadeEntity extends Projectile {
  private static final EntityDataAccessor<Integer> KIND=SynchedEntityData.defineId(GrenadeEntity.class,EntityDataSerializers.INT);
  private static final EntityDataAccessor<Boolean> SMOKING=SynchedEntityData.defineId(GrenadeEntity.class,EntityDataSerializers.BOOLEAN);
  private int fuse=-1,smokeTicks=-1;
  /** Client-side spin for the renderer (degrees) and its previous value. */
  public float spin,spinO;
  public GrenadeEntity(EntityType<? extends GrenadeEntity> type,Level level){super(type,level);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(KIND,0);b.define(SMOKING,false);}
  public void setKind(Type t){entityData.set(KIND,t.ordinal());}
  public Type kind(){return Type.values()[Mth.clamp(entityData.get(KIND),0,Type.values().length-1)];}
  public boolean smoking(){return entityData.get(SMOKING);}

  @Override public void tick(){
   super.tick();
   // ---------- physics: gravity, drag, collisions with bounce, rolling friction ----------
   Vec3 v=getDeltaMovement();
   boolean water=isInWater();
   v=v.add(0,water?-.012:-.05,0).scale(water?.80:.99);
   // entity hits along the path: bounce back off people and mobs instead of passing through
   if(!level().isClientSide&&v.lengthSqr()>.01){
    var hit=ProjectileUtil.getEntityHitResult(level(),this,position(),position().add(v),getBoundingBox().expandTowards(v).inflate(.2),e->e.isPickable()&&!e.isSpectator()&&(e!=getOwner()||tickCount>8));
    if(hit!=null){v=v.multiply(-.25,.3,-.25);level().playSound(null,blockPosition(),SoundEvents.WOOD_HIT,SoundSource.NEUTRAL,.6F,1.4F);}
   }
   Vec3 before=v;
   setDeltaMovement(v);
   move(MoverType.SELF,v);
   Vec3 after=getDeltaMovement();
   double impact=0;
   if(horizontalCollision){
    double nx=after.x,nz=after.z;
    if(Math.abs(after.x)<1e-6&&Math.abs(before.x)>1e-4){nx=-before.x*.45;impact=Math.max(impact,Math.abs(before.x));}
    if(Math.abs(after.z)<1e-6&&Math.abs(before.z)>1e-4){nz=-before.z*.45;impact=Math.max(impact,Math.abs(before.z));}
    after=new Vec3(nx,after.y,nz);
   }
   if(verticalCollision){
    double ny=Math.abs(before.y)>.12?-before.y*.38:0;impact=Math.max(impact,Math.abs(before.y));
    after=new Vec3(after.x*.72,ny,after.z*.72);   // ground friction: it rolls and stops
    if(Math.abs(after.x)<.004)after=new Vec3(0,after.y,after.z);
    if(Math.abs(after.z)<.004)after=new Vec3(after.x,after.y,0);
   }
   setDeltaMovement(after);
   if(!level().isClientSide&&onGround()&&after.horizontalDistance()>.025&&tickCount%6==0)   // dragging / rolling over the ground
    level().playSound(null,blockPosition(),MareEletrica.GRENADE_ROLL.get(),SoundSource.NEUTRAL,(float)Math.min(.9,.25+after.horizontalDistance()*3),.85F+random.nextFloat()*.3F);
   if(impact>.1&&!level().isClientSide)level().playSound(null,blockPosition(),SoundEvents.CHAIN_HIT,SoundSource.NEUTRAL,(float)Math.min(1,.25+impact),1.1F+random.nextFloat()*.3F);
   if(level().isClientSide){spinO=spin;spin+=(float)after.horizontalDistance()*(onGround()?160F:60F);}
   if(level().isClientSide||!(level() instanceof ServerLevel sl))return;
   // ---------- fuse and effects ----------
   var t=kind();
   if(smokeTicks>=0){
    smokeTicks--;
    // keeps the cloud thick: new puffs fill the volume while the old ones fade
    if(tickCount%3==0)sl.sendParticles(MareEletrica.SMOKE_CLOUD.get(),getX(),getY()+1.1,getZ(),smokeTicks>40?7:2,2.6,1.1,2.6,.015);
    if(tickCount%3==0)sl.sendParticles(ParticleTypes.SMOKE,getX(),getY()+.2,getZ(),3,.05,.05,.05,.03);
    if(tickCount%20==0)sl.playSound(null,blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.NEUTRAL,.25F,.5F);
    if(smokeTicks<=0)discard();
    return;
   }
   if(fuse<0)fuse=t.fuse;
   if(--fuse>0)return;
   switch(t){
    case FRAG->{frag(sl);discard();}
    case FLASH->{flash(sl);discard();}
    case FIRE->{burst(sl);discard();}
    case SMOKE->{smokeTicks=360;entityData.set(SMOKING,true);sl.sendParticles(MareEletrica.SMOKE_CLOUD.get(),getX(),getY()+1,getZ(),70,1.2,.6,1.2,.12);sl.playSound(null,blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.NEUTRAL,1.2F,.6F);}
   }
  }
  private void flash(ServerLevel sl){
   sl.sendParticles(ParticleTypes.FLASH,getX(),getY()+.2,getZ(),1,0,0,0,0);
   sl.sendParticles(ParticleTypes.END_ROD,getX(),getY()+.2,getZ(),40,.3,.3,.3,.25);
   sl.playSound(null,blockPosition(),SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,SoundSource.NEUTRAL,1.5F,1.4F);
   // looking at it blinds you from far away (up to 64 blocks); with your back turned only close range matters
   for(LivingEntity e:sl.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(64))){
    double dist=e.distanceTo(this);
    if(dist>64||!e.hasLineOfSight(this))continue;
    Vec3 toGrenade=position().subtract(e.getEyePosition()).normalize();
    double facing=e.getViewVector(1F).dot(toGrenade);
    float strength;
    if(facing>.55)strength=(float)Math.max(.9,1-dist/220);                     // straight at it: full flash even far away
    else if(facing>.15)strength=(float)Math.max(.45,(1-dist/48)*.9);           // corner of the eye
    else{if(dist>16)continue;strength=(float)Math.max(.15,(1-dist/16)*.45);}   // behind you
    if(e instanceof net.minecraft.server.level.ServerPlayer sp)net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,new FlashNet.Flash(strength));
    else e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,(int)(40+strength*120),0,false,false));
   }
  }
  private void frag(ServerLevel sl){
   double x=getX(),y=getY()+.15,z=getZ();
   sl.explode(this,null,null,x,y,z,3.4F,false,Level.ExplosionInteraction.NONE,ParticleTypes.EXPLOSION,ParticleTypes.EXPLOSION_EMITTER,MareEletrica.SILENT);
   sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,x,y+.2,z,1,0,0,0,0);
   sl.sendParticles(ParticleTypes.FLASH,x,y+.3,z,1,0,0,0,0);
   sl.sendParticles(ParticleTypes.FLAME,x,y+.3,z,45,.2,.2,.2,.32);
   sl.sendParticles(ParticleTypes.LAVA,x,y+.2,z,14,.4,.2,.4,0);
   sl.sendParticles(ParticleTypes.LARGE_SMOKE,x,y+.6,z,60,1.6,.8,1.6,.06);
   sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,x,y+.8,z,14,.9,.6,.9,.02);
   for(int i=0;i<36;i++){double a=i*Math.PI*2/36;sl.sendParticles(ParticleTypes.POOF,x+Math.cos(a)*.6,y+.1,z+Math.sin(a)*.6,0,Math.cos(a),.05,Math.sin(a),.55);}   // ground shockwave ring
   var below=sl.getBlockState(blockPosition().below());
   if(!below.isAir())sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,below),x,y+.2,z,50,.6,.4,.6,.25);   // dirt/debris thrown up
   sl.playSound(null,blockPosition(),MareEletrica.FRAG_BLAST.get(),SoundSource.NEUTRAL,1.5F,.9F+random.nextFloat()*.15F);
  }
  private void burst(ServerLevel sl){
   sl.sendParticles(ParticleTypes.FLAME,getX(),getY()+.3,getZ(),70,1.3,.5,1.3,.04);
   sl.sendParticles(ParticleTypes.LAVA,getX(),getY()+.3,getZ(),12,.6,.2,.6,0);
   sl.playSound(null,blockPosition(),SoundEvents.FIRECHARGE_USE,SoundSource.NEUTRAL,1.5F,.8F);
   for(LivingEntity e:sl.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(4))){
    if(e.distanceTo(this)<=4)e.setRemainingFireTicks(120);
   }
   var center=blockPosition();
   for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
    if(dx*dx+dz*dz>10||random.nextFloat()<.25F)continue;
    for(int dy=1;dy>=-2;dy--){
     var p=center.offset(dx,dy,dz);
     if(sl.getBlockState(p).isAir()&&sl.getBlockState(p.below()).isFaceSturdy(sl,p.below(),net.minecraft.core.Direction.UP)){sl.setBlockAndUpdate(p,net.minecraft.world.level.block.BaseFireBlock.getState(sl,p));break;}
    }
   }
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Kind",entityData.get(KIND));t.putInt("Fuse",fuse);t.putInt("Smoke",smokeTicks);}
  @Override protected void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);entityData.set(KIND,t.getInt("Kind"));fuse=t.contains("Fuse")?t.getInt("Fuse"):-1;smokeTicks=t.contains("Smoke")?t.getInt("Smoke"):-1;entityData.set(SMOKING,smokeTicks>=0);}
  @Override public boolean isPickable(){return false;}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<4096;}
  @Override public ItemStack getPickResult(){return new ItemStack(switch(kind()){case FRAG->FRAG.get();case FLASH->FLASH.get();case SMOKE->SMOKE.get();case FIRE->FIRE.get();});}
 }
}
