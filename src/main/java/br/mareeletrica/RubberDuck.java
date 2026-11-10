package br.mareeletrica;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.88.0: rubber duck — floats in water and in the bathtubs, bobs and drifts, squeaks when squeezed or stepped on;
 *  real ducks nearby turn to look and quack back. Four colours: yellow, pink, blue and a rare golden one. */
public final class RubberDuck {
 private RubberDuck(){}
 public static final String[] COLORS={"amarelo","rosa","azul","dourado"};
 public static final DeferredHolder<SoundEvent,SoundEvent> SQUEAK=MareEletrica.SOUNDS.register("rubber_squeak",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"rubber_squeak")));
 public static final Supplier<EntityType<Toy>> ENTITY=MareEletrica.ACTORS.register("rubber_duck",()->EntityType.Builder.<Toy>of(Toy::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(8).updateInterval(2).build(MareEletrica.ID+":rubber_duck"));
 public static final DeferredItem<ToyItem> ITEM=MareEletrica.ITEMS.register("rubber_duck",()->new ToyItem(new Item.Properties().stacksTo(16)));
 public static void register(){}
 public static int variant(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d==null?0:Mth.clamp(d.copyTag().getInt("Variant"),0,3);}
 public static ItemStack stack(int v){var s=new ItemStack(ITEM.get());if(v>0)s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag(v)));return s;}
 static CompoundTag tag(int v){var t=new CompoundTag();t.putInt("Variant",v);return t;}
 public static void creative(java.util.function.Consumer<ItemStack> out){for(int v=0;v<4;v++)out.accept(stack(v));}

 public static final class ToyItem extends Item {
  public ToyItem(Properties p){super(p);}
  @Override public Component getName(ItemStack s){return Component.literal("Patinho de borracha "+COLORS[variant(s)]);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Clique: coloca na água, na banheira ou no chão (no ar: joga)."));
   t.add(Component.literal("§7Clique nele: aperta (squeak!) • Shift+clique: pega de volta."));
   t.add(Component.literal("§eOs patos de verdade adoram."));}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);if(l.isClientSide)return InteractionResultHolder.success(s);
   Vec3 eye=p.getEyePosition(),end=eye.add(p.getLookAngle().scale(5));
   var hit=l.clip(new ClipContext(eye,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.ANY,p));
   var d=new Toy(ENTITY.get(),l);d.setVariant(variant(s));
   if(hit.getType()==HitResult.Type.BLOCK){
    var bp=hit.getBlockPos();var st=l.getBlockState(bp);Vec3 at=hit.getLocation();
    if(Bath.filled(st))at=new Vec3(at.x,bp.getY()+.5,at.z);                          // straight onto the tub water
    else at=at.add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(.2));
    d.moveTo(at.x,at.y,at.z,p.getYRot()+180,0);
   }else{d.moveTo(eye.x,eye.y-.2,eye.z,p.getYRot()+180,0);d.setDeltaMovement(p.getLookAngle().scale(.65).add(0,.15,0));}   // throw it
   l.addFreshEntity(d);d.squeak(1F);
   if(!p.getAbilities().instabuild)s.shrink(1);
   return InteractionResultHolder.consume(s);}
 }

 public static final class Toy extends Entity {
  static final EntityDataAccessor<Integer> VARIANT=SynchedEntityData.defineId(Toy.class,EntityDataSerializers.INT);
  static final EntityDataAccessor<Integer> SQUISH=SynchedEntityData.defineId(Toy.class,EntityDataSerializers.INT);
  public float squishO,squishV;int stepCd,driftT;float spin;
  public Toy(EntityType<? extends Toy> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(VARIANT,0);b.define(SQUISH,0);}
  public int variant(){return entityData.get(VARIANT);}
  public void setVariant(int v){entityData.set(VARIANT,Mth.clamp(v,0,3));}
  @Override public boolean isPickable(){return !isRemoved();}
  @Override public boolean isPushable(){return true;}
  @Override public boolean canBeCollidedWith(){return false;}
  @Override public boolean fireImmune(){return true;}
  /** water surface the duck floats on, or NaN */
  double surface(){
   BlockPos bp=blockPosition();
   for(BlockPos q:new BlockPos[]{bp,bp.below()}){
    var st=level().getBlockState(q);if(Bath.filled(st))return q.getY()+8.6/16;
    var f=level().getFluidState(q);if(!f.isEmpty()){
     if(!level().getFluidState(q.above()).isEmpty())return q.getY()+1.6;   // deep under: rise
     return q.getY()+f.getHeight(level(),q);}}
   return Double.NaN;}
  public boolean floating(){double s=surface();return !Double.isNaN(s)&&getY()<s+.12;}
  @Override public void tick(){
   super.tick();squishO=squishV;squishV=entityData.get(SQUISH)>0?1:Math.max(0,squishV-.25F);
   if(!level().isClientSide&&entityData.get(SQUISH)>0)entityData.set(SQUISH,entityData.get(SQUISH)-1);
   Vec3 v=getDeltaMovement();double s=surface();
   if(!Double.isNaN(s)&&getY()<s+.12){
    // bob on the surface, drift slowly with a little spin
    double target=s-.06+Mth.sin(tickCount*.12F+getId())*.018;
    if(--driftT<=0){driftT=60+random.nextInt(80);spin=(random.nextFloat()-.5F)*3F;}
    double dx=Mth.sin(tickCount*.021F+getId())*.0035,dz=Mth.cos(tickCount*.017F+getId()*1.7F)*.0035;
    v=new Vec3(v.x*.9+dx,(target-getY())*.25,v.z*.9+dz);
    setYRot(getYRot()+spin*.3F);
    if(level().isClientSide&&random.nextInt(40)==0)level().addParticle(ParticleTypes.BUBBLE_POP,getX(),s+.02,getZ(),0,0,0);
   }else{
    v=v.add(0,-.04,0).scale(.98);if(onGround())v=new Vec3(v.x*.6,v.y,v.z*.6);}
   setDeltaMovement(v);move(MoverType.SELF,v);
   if(getY()<level().getMinBuildHeight()-16)discard();
   if(level().isClientSide){if(variant()==3&&random.nextInt(25)==0)level().addParticle(ParticleTypes.WAX_ON,getX()+(random.nextDouble()-.5)*.4,getY()+.3+random.nextDouble()*.2,getZ()+(random.nextDouble()-.5)*.4,0,0,0);return;}
   // stepped on: squeak
   if(stepCd>0)stepCd--;
   else for(var p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(.05,.25,.05),x->!x.isSpectator()&&x.getY()>getY()+.15&&x.getY()<getY()+.55)){squeak(1.1F);stepCd=12;setDeltaMovement(getDeltaMovement().add((random.nextDouble()-.5)*.3,0,(random.nextDouble()-.5)*.3));break;}
  }
  public void squeak(float vol){
   if(level().isClientSide)return;entityData.set(SQUISH,4);
   float pitch=(variant()==3?.85F:variant()==1?1.15F:variant()==2?1.05F:1F)+(random.nextFloat()-.5F)*.15F;
   level().playSound(null,getX(),getY(),getZ(),SQUEAK.get(),SoundSource.NEUTRAL,vol,pitch);
   if(level() instanceof ServerLevel sl&&floating())sl.sendParticles(ParticleTypes.SPLASH,getX(),getY()+.1,getZ(),6,.12,.02,.12,.05);
   // real ducks answer
   for(var d:level().getEntitiesOfClass(Pato.Duck.class,getBoundingBox().inflate(10),x->x.isAlive()&&!x.isPassenger())){
    d.getLookControl().setLookAt(this,40,40);
    if(random.nextFloat()<.6F)Pato.quack(d,.9F,1.2F+random.nextFloat()*.4F);
    if(level() instanceof ServerLevel sl&&random.nextFloat()<.35F)sl.sendParticles(ParticleTypes.HEART,d.getX(),d.getY()+d.getBbHeight()+.2,d.getZ(),1,.1,.05,.1,0);}
  }
  @Override public InteractionResult interact(Player p,InteractionHand h){
   if(level().isClientSide)return InteractionResult.SUCCESS;
   if(p.isShiftKeyDown()){var st=stack(variant());if(!p.getInventory().add(st))spawnAtLocation(st);discard();
    level().playSound(null,blockPosition(),net.minecraft.sounds.SoundEvents.ITEM_PICKUP,SoundSource.PLAYERS,.4F,1.6F);return InteractionResult.CONSUME;}
   squeak(1F);if(!floating()&&onGround())setDeltaMovement(getDeltaMovement().add(0,.22,0));else setDeltaMovement(getDeltaMovement().add(0,-.08,0));
   return InteractionResult.CONSUME;}
  @Override public boolean hurt(DamageSource s,float a){
   if(isInvulnerableTo(s))return false;
   if(!level().isClientSide&&s.getEntity()!=null){var dir=position().subtract(s.getEntity().position()).normalize();setDeltaMovement(dir.x*.5,floating()?.05:.3,dir.z*.5);squeak(1.2F);}
   return true;}
  @Override public boolean isInvulnerableTo(DamageSource s){return s.getEntity()==null&&!s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY);}
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putInt("Variant",variant());}
  @Override protected void readAdditionalSaveData(CompoundTag t){setVariant(t.getInt("Variant"));}
  @Override public ItemStack getPickResult(){return stack(variant());}
 }
}
