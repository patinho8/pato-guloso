package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Vehicle garage: park a helicopter or tank on the pad in front of it and store it (up to 6), paint it, call it back.
 *  Also the upgrade kits, the fuel can and the garage remote (calls your vehicle to you). */
public final class Garage {
 public static final int SLOTS=6;
 public static final DeferredBlock<GarageBlock> BLOCK=MareEletrica.BLOCKS.register("vehicle_garage",()->new GarageBlock(BlockBehaviour.Properties.of().strength(4F,12F).sound(SoundType.METAL).noOcclusion()));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("vehicle_garage",()->new BlockItem(BLOCK.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Store>> ENTITY=MareEletrica.ENTITIES.register("vehicle_garage",()->BlockEntityType.Builder.of(Store::new,BLOCK.get()).build(null));
 public static final DeferredItem<KitItem> FUEL=MareEletrica.ITEMS.register("fuel_can",()->new KitItem(new Item.Properties().stacksTo(16),-1));
 public static final DeferredItem<KitItem> ARMOR_KIT=MareEletrica.ITEMS.register("armor_kit",()->new KitItem(new Item.Properties().stacksTo(8),0));
 public static final DeferredItem<KitItem> ENGINE_KIT=MareEletrica.ITEMS.register("engine_kit",()->new KitItem(new Item.Properties().stacksTo(8),1));
 public static final DeferredItem<KitItem> AMMO_KIT=MareEletrica.ITEMS.register("ammo_kit",()->new KitItem(new Item.Properties().stacksTo(8),2));
 public static final DeferredItem<RemoteItem> REMOTE=MareEletrica.ITEMS.register("garage_remote",()->new RemoteItem(new Item.Properties().stacksTo(1)));
 public static Consumer<Data> openScreen=d->{};
 private Garage(){}
 public static void register(){}

 public static final class KitItem extends Item {
  public final int kind;
  public KitItem(Properties p,int kind){super(p);this.kind=kind;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal(kind<0?"Clique direito num helicóptero ou tanque: +1/3 do tanque":"Clique direito num helicóptero ou tanque: "+switch(kind){case 0->"+25% de casco";case 1->"+12% de velocidade";default->"+50% de munição";}+" (até nível 3)").withStyle(net.minecraft.ChatFormatting.GRAY));
  }
 }

 public static final class GarageBlock extends HorizontalDirectionalBlock implements EntityBlock {
  public static final MapCodec<GarageBlock> CODEC=simpleCodec(GarageBlock::new);
  public GarageBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  /** The console faces the player; the pad is in front of it. */
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(0,0,0,16,16,16);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Store(p,s);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide&&l.getBlockEntity(p) instanceof Store g)PacketDistributor.sendToPlayer((ServerPlayer)player,g.data((ServerLevel)l));
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }

 public static final class Store extends BlockEntity {
  public final List<CompoundTag> stored=new ArrayList<>();
  public Store(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  /** Centre of the 7x7 pad in front of the console. */
  public Vec3 pad(){var f=getBlockState().getValue(HorizontalDirectionalBlock.FACING);return worldPosition.relative(f,4).getBottomCenter();}
  List<Vehicles.Base> parked(Level l){return l.getEntitiesOfClass(Vehicles.Base.class,new AABB(BlockPos.containing(pad())).inflate(3.5,2,3.5),v->v.getPassengers().isEmpty());}
  static String describe(Vehicles.Base v){return (v instanceof Vehicles.Helicopter?"Helicóptero":"Tanque")+" • "+Vehicles.Base.PAINTS[v.paint()];}
  Data data(ServerLevel l){
   var t=new CompoundTag();var list=new ListTag();
   for(var c:stored){var x=new CompoundTag();x.putString("Name",c.getString("GarageName"));x.putInt("Health",c.getInt("GarageHp"));x.putInt("Fuel",c.getInt("GarageFuel"));list.add(x);}
   t.put("Stored",list);
   var parked=new ListTag();
   for(var v:parked(l)){var x=new CompoundTag();x.putInt("Id",v.getId());x.putString("Name",describe(v));x.putInt("Health",Math.round(v.health()*100/v.maxHealth()));x.putInt("Fuel",v.fuel()*100/v.maxFuel());
    x.putInt("Paint",v.paint());x.putInt("A",v.upgrade(0));x.putInt("E",v.upgrade(1));x.putInt("M",v.upgrade(2));parked.add(x);}
   t.put("Parked",parked);
   return new Data(worldPosition,t);
  }
  void action(ServerPlayer p,int a,int index,int arg){
   var l=(ServerLevel)level;
   switch(a){
    case 1->{   // store a parked vehicle
     var v=find(l,index);if(v==null)break;
     if(stored.size()>=SLOTS){p.displayClientMessage(Component.literal("§eGaragem cheia ("+SLOTS+" vagas)."),true);break;}
     var tag=new CompoundTag();if(!v.save(tag))break;
     tag.putString("GarageName",describe(v));tag.putInt("GarageHp",Math.round(v.health()*100/v.maxHealth()));tag.putInt("GarageFuel",v.fuel()*100/v.maxFuel());
     stored.add(tag);v.discard();l.playSound(null,worldPosition,SoundEvents.PISTON_CONTRACT,SoundSource.BLOCKS,1F,.6F);
    }
    case 2->{   // take one out onto the pad
     if(index<0||index>=stored.size())break;
     if(!parked(l).isEmpty()){p.displayClientMessage(Component.literal("§eTire o veículo que está no pátio primeiro."),true);break;}
     var v=spawn(l,stored.get(index),pad());if(v!=null){stored.remove(index);l.playSound(null,worldPosition,SoundEvents.PISTON_EXTEND,SoundSource.BLOCKS,1F,.6F);}
    }
    case 3->{var v=find(l,index);if(v!=null){v.setPaint(arg);l.playSound(null,BlockPos.containing(v.position()),SoundEvents.DYE_USE,SoundSource.NEUTRAL,1F,1F);}}
    default->{}
   }
   setChanged();PacketDistributor.sendToPlayer(p,data(l));
  }
  Vehicles.Base find(ServerLevel l,int id){return l.getEntity(id) instanceof Vehicles.Base v&&parked(l).contains(v)?v:null;}
  static Vehicles.Base spawn(ServerLevel l,CompoundTag tag,Vec3 at){
   var e=EntityType.loadEntityRecursive(tag.copy(),l,x->{x.moveTo(at.x,at.y,at.z,x.getYRot(),0);return x;});
   if(!(e instanceof Vehicles.Base v))return null;
   v.setUUID(UUID.randomUUID());
   l.addFreshEntity(v);return v;
  }
  /** Garage remote: takes the first stored vehicle out and sends it to the player. */
  Vehicles.Base dispatch(ServerLevel l){
   if(stored.isEmpty())return null;
   for(int i=0;i<stored.size();i++){
    var v=spawn(l,stored.get(i),pad().add(0,stored.get(i).getString("id").contains("helicopter")?0:0,0));
    if(v!=null){stored.remove(i);setChanged();return v;}
   }
   return null;
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);var l=new ListTag();l.addAll(stored);t.put("Vehicles",l);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);stored.clear();for(var x:t.getList("Vehicles",10))stored.add((CompoundTag)x);}
 }

 /** Shift + right-click a garage to link. Right-click: your vehicle comes to you — from the garage if one is stored,
  *  otherwise the nearest empty helicopter or tank within 200 blocks. */
 public static final class RemoteItem extends Item {
  public RemoteItem(Properties p){super(p);}
  static BlockPos linked(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null||!c.copyTag().contains("Garage")?null:BlockPos.of(c.copyTag().getLong("Garage"));}
  @Override public InteractionResult useOn(UseOnContext c){
   if(c.getPlayer()==null||!c.getPlayer().isShiftKeyDown()||!(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof Store))return InteractionResult.PASS;
   if(!c.getLevel().isClientSide){var t=new CompoundTag();t.putLong("Garage",c.getClickedPos().asLong());c.getItemInHand().set(DataComponents.CUSTOM_DATA,CustomData.of(t));c.getPlayer().displayClientMessage(Component.literal("§aControle ligado a esta garagem."),true);}
   return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
  }
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){
   var s=p.getItemInHand(hand);
   if(l.isClientSide)return InteractionResultHolder.success(s);
   var sl=(ServerLevel)l;Vehicles.Base v=null;
   var g=linked(s);
   if(g!=null&&sl.hasChunkAt(g)&&sl.getBlockEntity(g) instanceof Store store)v=store.dispatch(sl);
   if(v==null){double bd=200*200;for(var x:sl.getEntitiesOfClass(Vehicles.Base.class,p.getBoundingBox().inflate(200),x->x.getPassengers().isEmpty())){double d=x.distanceToSqr(p);if(d<bd){bd=d;v=x;}}}
   if(v==null){p.displayClientMessage(Component.literal("§eNenhum veículo disponível (garagem vazia e nenhum vazio por perto)."),true);return InteractionResultHolder.fail(s);}
   if(v.fuel()<=0){p.displayClientMessage(Component.literal("§cO veículo está sem combustível."),true);return InteractionResultHolder.fail(s);}
   var land=l.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.blockPosition().relative(p.getDirection(),6));
   v.autoTarget=Vec3.atBottomCenterOf(land);
   p.getCooldowns().addCooldown(this,40);
   l.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.PLAYERS,1F,1.4F);
   p.displayClientMessage(Component.literal("§a"+(v instanceof Vehicles.Helicopter?"Helicóptero":"Tanque")+" a caminho ("+Math.round(v.distanceTo(p))+" m)"),true);
   return InteractionResultHolder.success(s);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){var g=linked(s);t.add(Component.literal(g==null?"Shift + clique direito numa garagem para ligar":"Garagem em "+g.toShortString()).withStyle(net.minecraft.ChatFormatting.GRAY));}
 }

 // ------------------------------------------------------------ network
 public record Data(BlockPos pos,CompoundTag data) implements CustomPacketPayload {
  public static final Type<Data> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"garage_data"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Data> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Data::pos,ByteBufCodecs.COMPOUND_TAG,Data::data,Data::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Action(BlockPos pos,int action,int index,int arg) implements CustomPacketPayload {
  public static final Type<Action> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"garage_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Action::pos,ByteBufCodecs.VAR_INT,Action::action,ByteBufCodecs.VAR_INT,Action::index,ByteBufCodecs.VAR_INT,Action::arg,Action::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
   var r=e.registrar("17");
   r.playToClient(Data.TYPE,Data.CODEC,(p,c)->openScreen.accept(p));
   r.playToServer(Action.TYPE,Action.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer sp&&sp.distanceToSqr(p.pos().getCenter())<100&&sp.level().getBlockEntity(p.pos()) instanceof Store g)g.action(sp,p.action(),p.index(),p.arg());});
  }
 }
}
