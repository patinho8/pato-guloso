package br.mareeletrica;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Shift + right-click the top of a block with a gun: it is laid down on the ground (as it is, with its loadout). Right-click picks it up. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class GroundGuns {
 public static final Supplier<EntityType<GroundGun>> ENTITY=MareEletrica.ACTORS.register("ground_gun",()->
  EntityType.Builder.<GroundGun>of(GroundGun::new,MobCategory.MISC).sized(.9F,.25F).clientTrackingRange(8).updateInterval(20).build(MareEletrica.ID+":ground_gun"));
 private GroundGuns(){}
 public static void register(){}
 public static boolean gun(ItemStack s){return SniperItem.firearm(s)||s.getItem() instanceof GunItem;}
 @SubscribeEvent public static void place(PlayerInteractEvent.RightClickBlock e){
  var p=e.getEntity();var s=e.getItemStack();
  if(!p.isShiftKeyDown()||e.getFace()!=net.minecraft.core.Direction.UP||!gun(s))return;
  e.setCanceled(true);e.setCancellationResult(InteractionResult.sidedSuccess(e.getLevel().isClientSide));
  if(e.getLevel().isClientSide)return;
  var g=new GroundGun(ENTITY.get(),e.getLevel());var at=e.getHitVec().getLocation();
  g.setPos(at.x,at.y,at.z);g.setYRot(p.getYRot()+90F);g.setStack(s.copy());
  e.getLevel().addFreshEntity(g);
  p.setItemInHand(e.getHand(),ItemStack.EMPTY);
  e.getLevel().playSound(null,g.blockPosition(),SoundEvents.ARMOR_EQUIP_IRON.value(),SoundSource.PLAYERS,.8F,.8F);
 }
 public static final class GroundGun extends Entity {
  private static final EntityDataAccessor<ItemStack> STACK=SynchedEntityData.defineId(GroundGun.class,EntityDataSerializers.ITEM_STACK);
  public GroundGun(EntityType<? extends GroundGun> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(STACK,ItemStack.EMPTY);}
  public ItemStack stack(){return entityData.get(STACK);}
  public void setStack(ItemStack s){entityData.set(STACK,s);}
  @Override public boolean isPickable(){return true;}
  @Override public void tick(){super.tick();if(!onGround()){setDeltaMovement(getDeltaMovement().add(0,-.04,0).scale(.98));move(MoverType.SELF,getDeltaMovement());}else setDeltaMovement(Vec3.ZERO);}
  @Override public InteractionResult interact(Player p,InteractionHand hand){
   if(!level().isClientSide){var s=stack();if(!s.isEmpty()&&!p.getInventory().add(s.copy()))p.drop(s.copy(),false);playSound(SoundEvents.ITEM_PICKUP,.6F,1F);discard();}
   return InteractionResult.sidedSuccess(level().isClientSide);
  }
  @Override public boolean hurt(DamageSource s,float a){if(!level().isClientSide&&!isRemoved()&&s.getEntity() instanceof Player){spawnAtLocation(stack());discard();}return true;}
  @Override protected void addAdditionalSaveData(CompoundTag t){if(!stack().isEmpty())t.put("Item",stack().save(registryAccess()));}
  @Override protected void readAdditionalSaveData(CompoundTag t){if(t.contains("Item"))setStack(ItemStack.parse(registryAccess(),t.getCompound("Item")).orElse(ItemStack.EMPTY));}
 }
}
