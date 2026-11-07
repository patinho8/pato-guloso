package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Delivery drones: a station with a chest (27 slots). Put a delivery drone in the first slot and link a destination with the route tag;
 *  every 30 s, if there are items, the drone takes them (up to 9 stacks), flies to the other station, unloads and comes back. */
public final class Delivery {
 public static final DeferredBlock<StationBlock> STATION=MareEletrica.BLOCKS.register("delivery_station",()->new StationBlock(BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL).noOcclusion()));
 public static final DeferredItem<BlockItem> STATION_ITEM=MareEletrica.ITEMS.register("delivery_station",()->new BlockItem(STATION.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Station>> STATION_ENTITY=MareEletrica.ENTITIES.register("delivery_station",()->BlockEntityType.Builder.of(Station::new,STATION.get()).build(null));
 public static final DeferredItem<DroneItem> DRONE_ITEM=MareEletrica.ITEMS.register("delivery_drone",()->new DroneItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<TagItem> ROUTE_TAG=MareEletrica.ITEMS.register("route_tag",()->new TagItem(new Item.Properties().stacksTo(1)));
 public static final Supplier<EntityType<Courier>> COURIER=MareEletrica.ACTORS.register("delivery_drone",()->
  EntityType.Builder.<Courier>of(Courier::new,MobCategory.MISC).sized(.9F,.5F).clientTrackingRange(12).updateInterval(2).build(MareEletrica.ID+":delivery_drone"));
 public static final TicketType<ChunkPos> TICKET=TicketType.create("mareeletrica_courier",Comparator.comparingLong(ChunkPos::toLong),60);
 private Delivery(){}
 public static void register(){}

 public static final class StationBlock extends BaseEntityBlock {
  public static final MapCodec<StationBlock> CODEC=simpleCodec(StationBlock::new);
  public StationBlock(BlockBehaviour.Properties p){super(p);}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Station(p,s);}
  @SuppressWarnings("unchecked")
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=STATION_ENTITY.get()?null:(BlockEntityTicker<T>)(BlockEntityTicker<Station>)Station::tick;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide&&l.getBlockEntity(p) instanceof Station st)player.openMenu(st);
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock())&&l.getBlockEntity(p) instanceof Station st)Containers.dropContents(l,p,st);super.onRemove(s,l,p,n,moving);}
 }

 public static final class Station extends BaseContainerBlockEntity {
  private NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
  public BlockPos target;public boolean busy;private long nextTry;
  public Station(BlockPos p,BlockState s){super(STATION_ENTITY.get(),p,s);}
  @Override protected Component getDefaultName(){return Component.literal(target==null?"Estação de entrega (sem destino)":"Estação de entrega → "+target.toShortString());}
  @Override protected NonNullList<ItemStack> getItems(){return items;}
  @Override protected void setItems(NonNullList<ItemStack> list){items=list;}
  @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return ChestMenu.threeRows(id,inv,this);}
  @Override public int getContainerSize(){return 27;}
  static void tick(Level l,BlockPos p,BlockState s,Station st){
   if(st.busy||st.target==null||l.getGameTime()<st.nextTry)return;
   st.nextTry=l.getGameTime()+600;
   if(!st.items.get(0).is(DRONE_ITEM.get()))return;
   var cargo=new ArrayList<ItemStack>();
   for(int i=1;i<27&&cargo.size()<9;i++)if(!st.items.get(i).isEmpty()){cargo.add(st.items.get(i).copy());st.items.set(i,ItemStack.EMPTY);}
   if(cargo.isEmpty())return;
   var c=new Courier(COURIER.get(),l);c.setPos(p.getX()+.5,p.getY()+1.1,p.getZ()+.5);c.home=p;c.dest=st.target;c.cargo.addAll(cargo);
   l.addFreshEntity(c);st.busy=true;st.setChanged();
   l.playSound(null,p,SoundEvents.BEEHIVE_EXIT,SoundSource.BLOCKS,1F,.8F);
  }
  /** Unloads into this station; whatever does not fit is dropped on top. */
  void receive(List<ItemStack> cargo){
   for(var s:cargo){var rest=s.copy();
    for(int i=1;i<27&&!rest.isEmpty();i++){var here=items.get(i);
     if(here.isEmpty()){items.set(i,rest);rest=ItemStack.EMPTY;}
     else if(ItemStack.isSameItemSameComponents(here,rest)){int n=Math.min(rest.getCount(),here.getMaxStackSize()-here.getCount());here.grow(n);rest.shrink(n);}}
    if(!rest.isEmpty())Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+1.2,worldPosition.getZ()+.5,rest);}
   setChanged();
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);ContainerHelper.saveAllItems(t,items,r);if(target!=null)t.putLong("Target",target.asLong());t.putBoolean("Busy",busy);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);items=NonNullList.withSize(27,ItemStack.EMPTY);ContainerHelper.loadAllItems(t,items,r);target=t.contains("Target")?BlockPos.of(t.getLong("Target")):null;busy=t.getBoolean("Busy");}
 }

 /** The delivery drone as an item, keeping its battery. */
 public static ItemStack courierItem(int battery){var s=new ItemStack(DRONE_ITEM.get());var t=new CompoundTag();t.putInt("Battery",battery);s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;}
 /** Delivery drone item: right-click a station to load it into the first slot, or the ground to set it down (right-click it to pick it up). */
 public static final class DroneItem extends Item {
  public DroneItem(Properties p){super(p);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,net.minecraft.world.item.TooltipFlag f){
   t.add(Component.literal("§7Na estação: entregas automáticas pela rota"));
   t.add(Component.literal("§7No chão: pilote com o controle de drone (Shift + clique para conectar)"));
   t.add(Component.literal("§7Pilotando: [B] pega itens/estação embaixo • [B] de novo: entrega/solta"));
  }
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();if(l.isClientSide)return InteractionResult.SUCCESS;
   if(l.getBlockEntity(c.getClickedPos()) instanceof Station st){if(st.getItem(0).isEmpty()){st.setItem(0,c.getItemInHand().split(1));st.setChanged();if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("§aDrone colocado na estação."),true);}return InteractionResult.CONSUME;}
   // on the ground: a delivery drone you can fly with the drone remote (Shift + right click it with the remote to pair)
   var pos=c.getClickedPos().relative(c.getClickedFace());var d=new Drones.DroneEntity(Drones.DRONE_ENTITY.get(),l);d.setCourier(true);
   d.setPos(pos.getX()+.5,pos.getY()+.05,pos.getZ()+.5);d.setYRot(c.getRotation());d.setBattery(Drones.DroneItem.battery(c.getItemInHand()));
   if(c.getPlayer()!=null){d.setOwner(c.getPlayer().getUUID());c.getPlayer().displayClientMessage(Component.literal("§7Drone de entrega no chão. §fShift + clique com o controle de drone§7 para pilotar."),true);}
   l.addFreshEntity(d);if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   return InteractionResult.CONSUME;
  }
 }

 /** Route tag: right-click the destination station, then the station that sends. */
 public static final class TagItem extends Item {
  public TagItem(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();if(!(l.getBlockEntity(c.getClickedPos()) instanceof Station st))return InteractionResult.PASS;
   if(l.isClientSide)return InteractionResult.SUCCESS;
   var s=c.getItemInHand();var data=s.get(DataComponents.CUSTOM_DATA);var p=c.getPlayer();
   if(data==null||!data.copyTag().contains("Dest")){var t=new CompoundTag();t.putLong("Dest",c.getClickedPos().asLong());s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));if(p!=null)p.displayClientMessage(Component.literal("§aDestino gravado. Agora clique na estação que vai enviar."),true);}
   else{var dest=BlockPos.of(data.copyTag().getLong("Dest"));if(dest.equals(c.getClickedPos())){if(p!=null)p.displayClientMessage(Component.literal("§eEscolha outra estação para enviar."),true);return InteractionResult.CONSUME;}
    st.target=dest;st.setChanged();s.remove(DataComponents.CUSTOM_DATA);if(p!=null)p.displayClientMessage(Component.literal("§aRota criada → "+dest.toShortString()+". Ponha um drone de entrega no 1º espaço."),true);}
   return InteractionResult.CONSUME;
  }
 }

 /** The flying courier: climbs, flies straight, lands on the destination, unloads, flies home. */
 public static final class Courier extends Entity {
  BlockPos home,dest;final List<ItemStack> cargo=new ArrayList<>();boolean returning;
  /** Set down by a player: just sits there until picked up. */
  public boolean parked;
  @Override public boolean isPickable(){return (parked||parkedSynced())&&!isRemoved();}
  @Override public net.minecraft.world.InteractionResult interact(Player p,net.minecraft.world.InteractionHand h){if(!parked&&!parkedSynced())return net.minecraft.world.InteractionResult.PASS;if(!level().isClientSide){var it=new ItemStack(DRONE_ITEM.get());if(!p.getInventory().add(it))p.drop(it,false);discard();}return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide);}
  @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float a){if(parked&&!level().isClientSide&&s.getEntity() instanceof Player p){var it=new ItemStack(DRONE_ITEM.get());if(!p.isCreative()&&!p.getInventory().add(it))p.drop(it,false);discard();return true;}return false;}
  public Courier(EntityType<? extends Courier> t,Level l){super(t,l);noPhysics=true;}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(PARKED,false);}
  private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> PARKED=SynchedEntityData.defineId(Courier.class,net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
  public boolean parkedSynced(){return entityData.get(PARKED);}
  @Override public boolean isNoGravity(){return true;}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<160*160;}
  @Override public void tick(){
   super.tick();
   if(level().isClientSide){if(!parked&&tickCount%2==0)level().addParticle(ParticleTypes.CLOUD,getX(),getY()-.2,getZ(),0,-.05,0);return;}
   var sl=(ServerLevel)level();
   if(parked){setDeltaMovement(Vec3.ZERO);if(!entityData.get(PARKED))entityData.set(PARKED,true);return;}
   if(home==null||dest==null){discard();return;}
   BlockPos goal=returning?home:dest;
   sl.getChunkSource().addRegionTicket(TICKET,chunkPosition(),2,chunkPosition());
   sl.getChunkSource().addRegionTicket(TICKET,new ChunkPos(goal),1,new ChunkPos(goal));
   Vec3 to=goal.getCenter().add(0,1.1,0);double flat=new Vec3(to.x-getX(),0,to.z-getZ()).length();
   double cruise=Math.max(home.getY(),dest.getY())+24;
   Vec3 v;
   if(flat>1.5){double vy=getY()<cruise?.35:0;Vec3 h=new Vec3(to.x-getX(),0,to.z-getZ()).normalize().scale(getY()>=cruise-4?.8:.15);v=h.add(0,vy,0);}
   else v=new Vec3((to.x-getX())*.3,Math.max(-.4,(to.y-getY())*.3),(to.z-getZ())*.3);
   setDeltaMovement(v);setPos(getX()+v.x,getY()+v.y,getZ()+v.z);
   setYRot((float)Math.toDegrees(Math.atan2(-v.x,v.z)));
   if(tickCount%30==0)sl.playSound(null,blockPosition(),SoundEvents.BEE_LOOP,SoundSource.NEUTRAL,1F,.7F);
   if(flat<.6&&Math.abs(to.y-getY())<.3){
    if(!returning){
     if(sl.getBlockEntity(dest) instanceof Station st)st.receive(cargo);else for(var s:cargo)Containers.dropItemStack(sl,getX(),getY(),getZ(),s);
     cargo.clear();returning=true;sl.playSound(null,dest,SoundEvents.ITEM_PICKUP,SoundSource.BLOCKS,1F,.8F);
    }else{
     if(sl.getBlockEntity(home) instanceof Station st){st.busy=false;st.setChanged();}
     discard();
    }
   }
   if(tickCount>20*60*5){if(sl.getBlockEntity(home) instanceof Station st){st.receive(cargo);st.busy=false;}discard();}
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){if(home!=null)t.putLong("Home",home.asLong());if(dest!=null)t.putLong("Dest",dest.asLong());t.putBoolean("Back",returning);t.putBoolean("Parked",parked);
   var l=new ListTag();for(var s:cargo)l.add(s.save(registryAccess()));t.put("Cargo",l);}
  @Override protected void readAdditionalSaveData(CompoundTag t){home=t.contains("Home")?BlockPos.of(t.getLong("Home")):null;dest=t.contains("Dest")?BlockPos.of(t.getLong("Dest")):null;returning=t.getBoolean("Back");parked=t.getBoolean("Parked");
   cargo.clear();for(var x:t.getList("Cargo",10))ItemStack.parse(registryAccess(),x).ifPresent(cargo::add);}
 }
}
