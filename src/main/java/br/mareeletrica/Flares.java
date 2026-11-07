package br.mareeletrica;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;
import org.joml.Vector3f;

/** Signal flares: fired high, they burn in a loud colour, drift down slowly, light the area and can be seen from far away. */
public final class Flares {
 /** Name, RGB, block texture used for the glowing core. */
 public static final String[] COLORS={"red","green","yellow","magenta","orange","cyan"};
 public static final String[] NAMES={"vermelho","verde","amarelo","magenta","laranja","ciano"};
 public static final int[] RGB={0xFF2A1A,0x3CFF3C,0xFFF230,0xFF30F0,0xFF8A10,0x30F0FF};
 public static int color(ItemStack s){var c=s.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);return c==null?0:Math.floorMod(c.copyTag().getInt("Color"),COLORS.length);}
 public static ItemStack of(int color){var s=new ItemStack(ITEM.get());var t=new CompoundTag();t.putInt("Color",color);s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(t));return s;}
 public static final DeferredItem<FlareItem> ITEM=MareEletrica.ITEMS.register("flare",()->new FlareItem(new Item.Properties().stacksTo(16)));
 public static final Supplier<EntityType<FlareEntity>> FLARE_ENTITY=MareEletrica.ACTORS.register("flare",()->
  EntityType.Builder.<FlareEntity>of(FlareEntity::new,MobCategory.MISC).sized(.4F,.4F).clientTrackingRange(20).updateInterval(2).build(MareEletrica.ID+":flare"));
 private Flares(){}
 /** Keeps the flare's chunk loaded (and its entities ticking) for the whole server while it burns; the ticket expires by itself 2 s after the flare is gone. */
 public static final net.minecraft.server.level.TicketType<net.minecraft.world.level.ChunkPos> FLARE_TICKET=net.minecraft.server.level.TicketType.create("mareeletrica_flare",java.util.Comparator.comparingLong(net.minecraft.world.level.ChunkPos::toLong),40);
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){for(int i=0;i<COLORS.length;i++)out.accept(of(i));}

 public static final class FlareItem extends Item {
  public FlareItem(Properties p){super(p);}
  @Override public Component getName(ItemStack s){return Component.literal("Sinalizador "+NAMES[color(s)]);}
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
   var s=player.getItemInHand(hand);
   if(!level.isClientSide){
    var f=new FlareEntity(FLARE_ENTITY.get(),level);f.setColor(color(s));var eye=player.getEyePosition();f.setPos(eye.x,eye.y-.1,eye.z);
    f.setDeltaMovement(player.getLookAngle().scale(.6).add(0,1.45,0));level.addFreshEntity(f);   // straight up, high
    level.playSound(null,player.blockPosition(),SoundEvents.FIREWORK_ROCKET_LAUNCH,SoundSource.PLAYERS,1.5F,.8F);
    if(!player.isCreative())s.shrink(1);
    player.getCooldowns().addCooldown(this,10);
   }
   return InteractionResultHolder.sidedSuccess(s,level.isClientSide);
  }
 }

 public static final class FlareEntity extends Entity {
  private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> COLOR=net.minecraft.network.syncher.SynchedEntityData.defineId(FlareEntity.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
  private BlockPos light;
  /** Countermeasure flare dropped by a jet: burns out quickly and does not leave lights behind. */
  public boolean decoy;
  public FlareEntity(EntityType<? extends FlareEntity> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){b.define(COLOR,0);}
  public int color(){return Math.floorMod(entityData.get(COLOR),COLORS.length);}
  public void setColor(int c){entityData.set(COLOR,c);}
  /** Seen from far away (normal small entities vanish after a few dozen blocks). */
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<320*320;}
  @Override public boolean isNoGravity(){return true;}
  @Override public void tick(){
   super.tick();
   Vec3 v=getDeltaMovement();
   // climbs, then hangs: falls very slowly (as if on a small parachute)
   double vy=v.y>0?v.y-.045:Math.max(-.04,v.y-.01);
   v=new Vec3(v.x*.96,vy,v.z*.96);
   if(onGround())v=Vec3.ZERO;
   setDeltaMovement(v);move(MoverType.SELF,v);
   if(level().isClientSide){
    int rgb=RGB[color()];var dust=new DustParticleOptions(new Vector3f((rgb>>16&255)/255F,(rgb>>8&255)/255F,(rgb&255)/255F),4F);
    for(int i=0;i<3;i++)level().addAlwaysVisibleParticle(dust,true,getX()+random.nextGaussian()*.15,getY()+random.nextGaussian()*.15,getZ()+random.nextGaussian()*.15,0,0,0);
    if(tickCount%2==0)level().addAlwaysVisibleParticle(ParticleTypes.FIREWORK,true,getX(),getY(),getZ(),random.nextGaussian()*.05,-.05,random.nextGaussian()*.05);
    if(tickCount%2==0)level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,true,getX(),getY()+.2,getZ(),0,.01,0);   // thick smoke column that hangs in the sky
    return;
   }
   if(tickCount%30==0)level().playSound(null,blockPosition(),SoundEvents.FIRE_AMBIENT,SoundSource.NEUTRAL,.8F,1.4F);
   if(!decoy)moveLight();
   if(!decoy&&tickCount%20==1&&level() instanceof net.minecraft.server.level.ServerLevel sl)sl.getChunkSource().addRegionTicket(FLARE_TICKET,chunkPosition(),2,chunkPosition());
   if(tickCount>(decoy?160:900)||isInWater())discard();
  }
  /** A real light: an invisible light block follows the flare. */
  private BlockPos ground;
  private void moveLight(){
   var at=blockPosition();
   if(!at.equals(light)){
    if(light!=null&&level().getBlockState(light).is(Blocks.LIGHT))level().removeBlock(light,false);light=null;
    if(level().getBlockState(at).isAir()){level().setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,15),3);light=at;}
   }
   // a block light only reaches 15 blocks, so the ground under a high flare gets its own light (dimmer the higher it burns)
   var floor=level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,at);
   int dist=at.getY()-floor.getY();
   if(dist<4||dist>60){clearGround();return;}
   if(floor.equals(ground))return;
   clearGround();
   if(level().getBlockState(floor).isAir()){level().setBlock(floor,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,Math.max(9,15-dist/10)),3);ground=floor;}
  }
  private void clearGround(){if(ground!=null&&level().getBlockState(ground).is(Blocks.LIGHT))level().removeBlock(ground,false);ground=null;}
  private void clearLight(){if(light!=null&&level().getBlockState(light).is(Blocks.LIGHT))level().removeBlock(light,false);light=null;clearGround();}
  @Override public void remove(RemovalReason reason){if(!level().isClientSide&&reason.shouldDestroy())clearLight();super.remove(reason);}
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putInt("Color",color());if(light!=null)t.putLong("Light",light.asLong());if(ground!=null)t.putLong("Ground",ground.asLong());}
  @Override protected void readAdditionalSaveData(CompoundTag t){setColor(t.getInt("Color"));if(t.contains("Light"))light=BlockPos.of(t.getLong("Light"));if(t.contains("Ground"))ground=BlockPos.of(t.getLong("Ground"));}
 }
}
