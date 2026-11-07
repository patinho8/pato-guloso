package br.mareeletrica;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

@net.neoforged.fml.common.EventBusSubscriber(modid=MareEletrica.ID)
/** Parachute: use it while falling (or jump out of a jet carrying one) and glide down; W to glide forward, mouse to steer. */
public final class Parachutes {
 public static final DeferredItem<ParachuteItem> ITEM=MareEletrica.ITEMS.register("parachute",()->new ParachuteItem(new Item.Properties().stacksTo(1)));
 public static final Supplier<EntityType<ParachuteEntity>> ENTITY=MareEletrica.ACTORS.register("parachute",()->
  EntityType.Builder.<ParachuteEntity>of(ParachuteEntity::new,MobCategory.MISC).sized(.6F,.4F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":parachute"));
 private Parachutes(){}
 /** Shift is usually still held when you bail out of a jet: don't let that drop you off the parachute right away. */
 @net.neoforged.bus.api.SubscribeEvent public static void dismount(net.neoforged.neoforge.event.entity.EntityMountEvent e){
  if(e.isDismounting()&&e.getEntityBeingMounted() instanceof ParachuteEntity c&&!c.isRemoved()&&!c.onGround()&&!c.isInWater()&&c.tickCount<30)e.setCanceled(true);
 }
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(ITEM.get());}
 public static boolean carries(Player p){if(p.isCreative())return true;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(ITEM.get()))return true;return false;}
 /** Opens a parachute over the player (server). */
 public static boolean tryDeploy(Player p,boolean auto){
  if(p.level().isClientSide||p.isPassenger()||p.onGround()||p.isInWater()||(auto&&!carries(p)))return false;
  var c=new ParachuteEntity(ENTITY.get(),p.level());c.setPos(p.getX(),p.getY(),p.getZ());c.setYRot(p.getYRot());
  c.setDeltaMovement(p.getDeltaMovement().multiply(.6,0,.6).add(0,-.2,0));
  p.level().addFreshEntity(c);p.startRiding(c,true);p.fallDistance=0;
  p.level().playSound(null,p.blockPosition(),SoundEvents.ARMOR_EQUIP_ELYTRA.value(),SoundSource.PLAYERS,1.2F,.7F);
  p.displayClientMessage(Component.literal("Paraquedas aberto! W plana para frente, o mouse guia. Shift solta."),true);
  return true;
 }

 public static final class ParachuteItem extends Item {
  public ParachuteItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
   var s=player.getItemInHand(hand);
   if(player.onGround()||player.isPassenger()){if(!level.isClientSide)player.displayClientMessage(Component.literal("Abra o paraquedas no ar (ou carregue-o ao ejetar do caça)."),true);return InteractionResultHolder.fail(s);}
   if(!level.isClientSide)tryDeploy(player,false);
   return InteractionResultHolder.sidedSuccess(s,level.isClientSide);
  }
 }

 public static final class ParachuteEntity extends Entity {
  public float inForward;public float sway,swayO;
  public ParachuteEntity(EntityType<? extends ParachuteEntity> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
  @Override public LivingEntity getControllingPassenger(){return getFirstPassenger() instanceof Player p?p:null;}
  @Override public boolean isNoGravity(){return true;}
  @Override public boolean shouldRiderSit(){return false;}
  @Override protected void positionRider(Entity rider,Entity.MoveFunction move){if(hasPassenger(rider))move.accept(rider,getX(),getY(),getZ());}
  @Override public void tick(){
   super.tick();
   swayO=sway;sway=Mth.sin(tickCount*.08F)*5F;
   var rider=getControllingPassenger();
   if(level().isClientSide){
    if(rider!=null&&isControlledByLocalInstance()){
     setYRot(rider.getYRot());
     double r=Math.toRadians(getYRot());Vec3 f=new Vec3(-Math.sin(r),0,Math.cos(r));
     Vec3 v=getDeltaMovement();double glide=.08+inForward*.17;
     v=new Vec3(v.x*.9+f.x*glide*.1,Math.max(-.16,v.y-.03)*.95,v.z*.9+f.z*glide*.1);
     if(v.y<-.16)v=new Vec3(v.x,-.16,v.z);
     setDeltaMovement(v);move(MoverType.SELF,v);
    }
    return;
   }
   if(rider==null||onGround()||isInWater()||tickCount>6000){ejectPassengers();if(rider!=null)rider.fallDistance=0;discard();return;}
   rider.fallDistance=0;
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){}
  @Override protected void readAdditionalSaveData(CompoundTag t){}
 }
}
