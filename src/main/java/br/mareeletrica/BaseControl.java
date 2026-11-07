package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Base command: the command desk (panel), alarm sirens, base members, turret modes and the energy shield.
 *  Everything works inside the panel's base area: {@link #RADIUS} blocks around it. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class BaseControl {
 public static final int RADIUS=64,SHIELD_RADIUS=16;
 public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
 public static final DeferredBlock<PanelBlock> PANEL=MareEletrica.BLOCKS.register("command_panel",()->new PanelBlock(BlockBehaviour.Properties.of().strength(4F,12F).sound(SoundType.METAL).noOcclusion().lightLevel(s->7)));
 public static final DeferredItem<BlockItem> PANEL_ITEM=MareEletrica.ITEMS.register("command_panel",()->new BlockItem(PANEL.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Panel>> PANEL_ENTITY=MareEletrica.ENTITIES.register("command_panel",()->BlockEntityType.Builder.of(Panel::new,PANEL.get()).build(null));
 public static final DeferredBlock<SirenBlock> SIREN=MareEletrica.BLOCKS.register("alarm_siren",()->new SirenBlock(BlockBehaviour.Properties.of().strength(2F).sound(SoundType.METAL).noOcclusion().lightLevel(s->s.getValue(ACTIVE)?12:0)));
 public static final DeferredItem<BlockItem> SIREN_ITEM=MareEletrica.ITEMS.register("alarm_siren",()->new BlockItem(SIREN.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Siren>> SIREN_ENTITY=MareEletrica.ENTITIES.register("alarm_siren",()->BlockEntityType.Builder.of(Siren::new,SIREN.get()).build(null));
 /** Client hooks. */
 public static Consumer<PanelData> openScreen=d->{};
 private BaseControl(){}
 public static void register(){}

 // ------------------------------------------------------------ area helpers
 /** Block entities of a type within r blocks (loaded chunks only). */
 public static <T> List<T> around(Level l,BlockPos c,int r,Class<T> type){
  var out=new ArrayList<T>();
  for(int cx=(c.getX()-r)>>4;cx<=(c.getX()+r)>>4;cx++)for(int cz=(c.getZ()-r)>>4;cz<=(c.getZ()+r)>>4;cz++){
   if(!l.hasChunk(cx,cz))continue;
   for(var be:l.getChunk(cx,cz).getBlockEntities().values())if(type.isInstance(be)&&be.getBlockPos().distSqr(c)<=(double)r*r)out.add(type.cast(be));
  }
  return out;
 }
 /** The command panel whose area covers this position, or null. */
 public static Panel panelFor(Level l,BlockPos p){Panel best=null;double bd=1e18;for(var x:around(l,p,RADIUS,Panel.class)){double d=x.getBlockPos().distSqr(p);if(d<bd){bd=d;best=x;}}return best;}
 public static boolean member(Panel panel,Player p){return panel==null||panel.isMember(p);}
 /** 0 = turrets off, 1 = monsters only, 2 = monsters and intruders. Default 1 without a panel. */
 public static int turretMode(Level l,BlockPos p){var panel=panelFor(l,p);return panel==null?1:panel.turretMode;}
 public static boolean intruder(Level l,BlockPos p,Player pl){if(pl.isCreative()||pl.isSpectator())return false;var panel=panelFor(l,p);return panel!=null&&!panel.isMember(pl);}
 public static boolean lockdown(Level l,BlockPos p){var panel=panelFor(l,p);return panel!=null&&panel.alarm;}

 // ------------------------------------------------------------ command panel
 public static final class PanelBlock extends HorizontalDirectionalBlock implements EntityBlock,Multiblock.Master {
  static final double[][] BOXES={{-16,0,1,32,14,15},{-15,14,11,31,24,12.5}};
  @Override public java.util.List<BlockPos> cells(BlockState s){return Multiblock.cellsOf(s.getValue(FACING),BOXES);}
  @Override public VoxelShape cellShape(BlockState s,BlockPos o){return Multiblock.slice(s.getValue(FACING),BOXES,o);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock()))Multiblock.clear(l,p,s);super.onRemove(s,l,p,n,moving);}

  public static final MapCodec<PanelBlock> CODEC=simpleCodec(PanelBlock::new);
  private static final VoxelShape SHAPE=Block.box(0,0,0,16,14,16);
  public PanelBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(ACTIVE,false));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ACTIVE);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var st=defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());return Multiblock.fits(c,st)?st:null;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return cellShape(s,BlockPos.ZERO);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Panel(p,s);}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack st){Multiblock.place(l,p,s);if(!l.isClientSide&&who instanceof Player pl&&l.getBlockEntity(p) instanceof Panel b){b.owner=pl.getUUID();b.members.add(pl.getGameProfile().getName());b.setChanged();}}
  @SuppressWarnings("unchecked")
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=PANEL_ENTITY.get()?null:(BlockEntityTicker<T>)(BlockEntityTicker<Panel>)Panel::tick;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(l.isClientSide||!(l.getBlockEntity(p) instanceof Panel b))return InteractionResult.sidedSuccess(l.isClientSide);
   if(!b.isMember(player)){player.displayClientMessage(Component.literal("§cAcesso negado — você não é membro desta base."),true);return InteractionResult.CONSUME;}
   PacketDistributor.sendToPlayer((ServerPlayer)player,b.data((ServerLevel)l,true));
   return InteractionResult.CONSUME;
  }
 }

 public static final class Panel extends BlockEntity {
  public UUID owner;public final List<String> members=new ArrayList<>();
  public boolean armed=true,alarm,shield=true;public int turretMode=1;private int quiet;
  /** Short summary shown on the desk monitors (synced to clients). */
  public int contacts,genRate,useRate;public long stored,storedMax;public int problems;
  public Panel(BlockPos p,BlockState s){super(PANEL_ENTITY.get(),p,s);}
  public boolean isMember(Player p){return p.isCreative()&&members.isEmpty()||owner==null||p.getUUID().equals(owner)||members.stream().anyMatch(n->n.equalsIgnoreCase(p.getGameProfile().getName()));}
  static void tick(Level l,BlockPos p,BlockState s,Panel b){
   if(l.getGameTime()%10!=Math.floorMod(p.asLong(),10))return;
   var sl=(ServerLevel)l;
   boolean trigger=false;String why="";
   if(b.armed){
    // the alarm only watches for war machines: planes and helicopters (128 blocks) and tanks (64 blocks) driven by someone
    // who isn't a base member. People and monsters don't set it off.
    for(var e:l.getEntitiesOfClass(Entity.class,new AABB(p).inflate(128),e->e instanceof Jets.JetEntity||e instanceof Vehicles.Helicopter||e instanceof Vehicles.Tank)){
     if(e instanceof Vehicles.Tank&&e.distanceToSqr(p.getCenter())>64*64)continue;
     var pilot=e.getControllingPassenger();if(pilot==null||pilot instanceof Player pl&&b.isMember(pl))continue;
     trigger=true;why=Radar.name(e)+" a "+Math.round(Math.sqrt(e.distanceToSqr(p.getCenter())))+" m";break;}
   }
   if(trigger){b.quiet=0;if(!b.alarm)b.setAlarm(sl,true,why);}
   else if(b.alarm&&++b.quiet>=20)b.setAlarm(sl,false,"");
   if(l.getGameTime()%40==Math.floorMod(p.asLong(),10))b.summary(sl);
  }
  void setAlarm(ServerLevel l,boolean on,String why){
   alarm=on;setChanged();
   for(var s:around(l,worldPosition,RADIUS,Siren.class)){var st=s.getBlockState();if(st.getValue(ACTIVE)!=on)l.setBlock(s.getBlockPos(),st.setValue(ACTIVE,on),3);}
   if(on){
    for(var k:around(l,worldPosition,RADIUS,BunkerDoor.Lock.class)){var st=l.getBlockState(k.getBlockPos());if(st.getBlock() instanceof DoorBlock d&&st.getValue(DoorBlock.OPEN))d.setOpen(null,l,st,k.getBlockPos(),false);}
    for(var pl:l.getEntitiesOfClass(Player.class,new AABB(worldPosition).inflate(RADIUS),this::isMember))pl.displayClientMessage(Component.literal("§c⚠ ALARME DA BASE: "+why+" — portas blindadas trancadas"),true);
   }
   l.setBlock(worldPosition,getBlockState().setValue(ACTIVE,on),3);
  }
  /** Energy totals of the base area, for the desk monitors and the panel screen. */
  void summary(ServerLevel l){
   int gen=0,use=0,bad=0;long st=0,max=0;
   for(var m:around(l,worldPosition,RADIUS,MachineEntity.class)){
    if(m.kind.core()){st+=m.clean.stored();max+=m.kind.capacity;}
    else if(m.kind.consumer){use+=m.lastRate;if(m.burnt||m.overvolt>0)bad++;}
    else gen+=m.lastRate;
   }
   contacts=l.getEntitiesOfClass(Entity.class,new AABB(worldPosition).inflate(Radar.RANGE),e->Radar.kind(e)==1).size();
   genRate=gen;useRate=use;stored=st;storedMax=max;problems=bad;
   setChanged();l.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
  }
  /** Everything the panel screen shows. */
  PanelData data(ServerLevel l,boolean open){
   summary(l);
   var t=new CompoundTag();
   t.putBoolean("Armed",armed);t.putBoolean("Alarm",alarm);t.putBoolean("Shield",shield);t.putInt("Turrets",turretMode);
   t.putInt("Gen",genRate);t.putInt("Use",useRate);t.putLong("Stored",stored);t.putLong("Max",storedMax);
   int machines=0,burnt=0,over=0;var bad=new ListTag();
   for(var m:around(l,worldPosition,RADIUS,MachineEntity.class)){machines++;if(m.burnt||m.overvolt>0){if(m.burnt)burnt++;else over++;if(bad.size()<8){var b=new CompoundTag();b.putString("Name",Component.translatable("block.mareeletrica."+m.kind.id).getString());b.putBoolean("Burnt",m.burnt);b.putLong("Pos",m.getBlockPos().asLong());bad.add(b);}}}
   t.putInt("Machines",machines);t.putInt("Burnt",burnt);t.putInt("Over",over);t.put("Bad",bad);
   var cont=new ListTag();var c=worldPosition.getCenter();
   for(var e:l.getEntitiesOfClass(Entity.class,new AABB(worldPosition).inflate(Radar.RANGE),e->Radar.kind(e)>0)){
    if(cont.size()>=24)break;var x=new CompoundTag();x.putString("Name",Radar.name(e));x.putInt("Kind",Radar.kind(e));x.putDouble("DX",e.getX()-c.x);x.putDouble("DZ",e.getZ()-c.z);x.putInt("Y",(int)e.getY());
    x.putBoolean("Friend",e instanceof Player pl?isMember(pl):e.getControllingPassenger() instanceof Player pl2&&isMember(pl2));cont.add(x);
   }
   t.put("Contacts",cont);
   var mem=new ListTag();for(var m:members)mem.add(StringTag.valueOf(m));t.put("Members",mem);
   if(open){
    var cams=new ListTag();
    for(int dx=-40;dx<=40&&cams.size()<16;dx++)for(int dz=-40;dz<=40&&cams.size()<16;dz++)for(int dy=-12;dy<=16;dy++){
     var q=worldPosition.offset(dx,dy,dz);if(!l.hasChunkAt(q))continue;var bs=l.getBlockState(q);
     if(bs.is(Cameras.CAMERA.get())){var x=new CompoundTag();x.putLong("Pos",q.asLong());x.putInt("Facing",bs.getValue(Cameras.CameraBlock.FACING).get3DDataValue());cams.add(x);}
    }
    t.put("Cameras",cams);
   }
   t.putInt("Doors",(around(l,worldPosition,RADIUS,GarageDoors.Anchor.class).size()+4)/5);
   t.putInt("Helipads",around(l,worldPosition,RADIUS,BaseBlocks.Helipad.class).size());
   t.putInt("Shields",(int)around(l,worldPosition,RADIUS,MachineEntity.class).stream().filter(m->m.kind==Kind.SHIELD).count());
   return new PanelData(worldPosition,t,open);
  }
  void action(ServerPlayer p,int a,String arg){
   var l=(ServerLevel)level;
   switch(a){
    case 1->{armed=!armed;if(!armed&&alarm)setAlarm(l,false,"");}
    case 2->turretMode=(turretMode+1)%3;
    case 3->shield=!shield;
    case 4->{for(var d:around(l,worldPosition,RADIUS,GarageDoors.Anchor.class))d.set(l,true);}
    case 5->{for(var d:around(l,worldPosition,RADIUS,GarageDoors.Anchor.class))d.set(l,false);}
    case 6->{var r=BaseBlocks.callHelicopter(l,worldPosition);p.displayClientMessage(Component.literal(r),true);}
    case 7->{String n=arg.trim();if(!n.isEmpty()&&n.length()<=16&&members.stream().noneMatch(n::equalsIgnoreCase)&&members.size()<32)members.add(n);}
    case 8->{if(owner==null||p.getUUID().equals(owner)||p.isCreative())members.removeIf(n->n.equalsIgnoreCase(arg)&&!n.equalsIgnoreCase(p.getGameProfile().getName()));}
    case 9->{if(alarm)setAlarm(l,false,"");quiet=0;}
    default->{}
   }
   setChanged();
   PacketDistributor.sendToPlayer(p,data(l,false));
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);if(owner!=null)t.putUUID("Owner",owner);var m=new ListTag();for(var n:members)m.add(StringTag.valueOf(n));t.put("Members",m);
   t.putBoolean("Armed",armed);t.putBoolean("Alarm",alarm);t.putBoolean("Shield",shield);t.putInt("Turrets",turretMode);
   t.putInt("Contacts",contacts);t.putInt("Gen",genRate);t.putInt("Use",useRate);t.putLong("Stored",stored);t.putLong("Max",storedMax);t.putInt("Problems",problems);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;members.clear();for(var x:t.getList("Members",8))members.add(x.getAsString());
   armed=!t.contains("Armed")||t.getBoolean("Armed");alarm=t.getBoolean("Alarm");shield=!t.contains("Shield")||t.getBoolean("Shield");turretMode=t.contains("Turrets")?t.getInt("Turrets"):1;
   contacts=t.getInt("Contacts");genRate=t.getInt("Gen");useRate=t.getInt("Use");stored=t.getLong("Stored");storedMax=t.getLong("Max");problems=t.getInt("Problems");}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
  @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }

 // ------------------------------------------------------------ alarm siren
 public static final class SirenBlock extends DirectionalBlock implements EntityBlock {
  public static final MapCodec<SirenBlock> CODEC=simpleCodec(SirenBlock::new);
  public SirenBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP).setValue(ACTIVE,false));}
  @Override protected MapCodec<? extends DirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ACTIVE);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getClickedFace());}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(4,4,4,12,12,12);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Siren(p,s);}
  @SuppressWarnings("unchecked")
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=SIREN_ENTITY.get()?null:(BlockEntityTicker<T>)(BlockEntityTicker<Siren>)Siren::tick;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide){var panel=panelFor(l,p);player.displayClientMessage(Component.literal(panel==null?"§eSirene sem painel de comando por perto (até "+RADIUS+" blocos).":"Sirene ligada ao painel em "+panel.getBlockPos().toShortString()),true);}
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }
 public static final class Siren extends BlockEntity {
  public Siren(BlockPos p,BlockState s){super(SIREN_ENTITY.get(),p,s);}
  static void tick(Level l,BlockPos p,BlockState s,Siren b){if(s.getValue(ACTIVE)&&l.getGameTime()%40==0)l.playSound(null,p,MareEletrica.SIREN_SOUND.get(),SoundSource.BLOCKS,1.2F,1F);}
 }

 // ------------------------------------------------------------ energy shield
 private static final Map<ResourceKey<Level>,Set<BlockPos>> SHIELDS=new HashMap<>();
 /** Shield generator machine (Kind.SHIELD): needs a lot of power; a panel can switch it off. */
 public static boolean shieldTick(MachineEntity m){
  var l=(ServerLevel)m.getLevel();var set=SHIELDS.computeIfAbsent(l.dimension(),d->new HashSet<>());
  var panel=panelFor(l,m.getBlockPos());
  boolean on=(panel==null||panel.shield)&&m.clean.pay(2500);
  if(on)set.add(m.getBlockPos().immutable());else set.remove(m.getBlockPos());
  m.status=on?80:panel!=null&&!panel.shield?81:2;m.lastRate=on?5000:0;
  if(on&&l.getGameTime()%40<10)l.playSound(null,m.getBlockPos(),SoundEvents.BEACON_AMBIENT,SoundSource.BLOCKS,.5F,.6F);
  return on;
 }
 static List<MachineEntity> shieldsAt(Level l,Vec3 at){
  var set=SHIELDS.get(l.dimension());if(set==null||set.isEmpty())return List.of();
  var out=new ArrayList<MachineEntity>();
  for(var it=set.iterator();it.hasNext();){var p=it.next();
   if(!(l.getBlockEntity(p) instanceof MachineEntity m)||m.kind!=Kind.SHIELD||!m.getBlockState().getValue(MachineBlock.ACTIVE)){if(l.hasChunkAt(p))it.remove();continue;}
   if(p.getCenter().distanceToSqr(at)<=SHIELD_RADIUS*SHIELD_RADIUS)out.add(m);}
  return out;
 }
 static boolean inside(BlockPos shield,Vec3 at){return shield.getCenter().distanceToSqr(at)<=SHIELD_RADIUS*SHIELD_RADIUS;}
 /** Explosions do not break anything or hurt anyone under an active dome (each one costs the shield energy). */
 @SubscribeEvent public static void explosion(ExplosionEvent.Detonate e){
  var l=e.getLevel();if(l.isClientSide)return;var set=SHIELDS.get(l.dimension());if(set==null||set.isEmpty())return;
  Vec3 c=e.getExplosion().center();
  for(var p:new ArrayList<>(set)){
   if(p.getCenter().distanceToSqr(c)>(SHIELD_RADIUS+12)*(SHIELD_RADIUS+12))continue;
   if(!(l.getBlockEntity(p) instanceof MachineEntity m)||m.kind!=Kind.SHIELD)continue;
   int before=e.getAffectedBlocks().size();
   e.getAffectedBlocks().removeIf(b->inside(p,b.getCenter()));
   e.getAffectedEntities().removeIf(x->inside(p,x.position()));
   if(before!=e.getAffectedBlocks().size()){m.clean.take(20_000);((ServerLevel)l).sendParticles(ParticleTypes.ELECTRIC_SPARK,c.x,c.y,c.z,40,2,2,2,.4);}
  }
 }
 /** Shots from outside the dome stop at its surface. */
 @SubscribeEvent public static void projectile(ProjectileImpactEvent e){
  Projectile pr=e.getProjectile();var l=pr.level();if(l.isClientSide)return;
  var at=e.getRayTraceResult().getLocation();
  for(var m:shieldsAt(l,at)){
   var owner=pr.getOwner();
   if(owner!=null&&inside(m.getBlockPos(),owner.position()))continue;   // shots fired from inside go out normally
   e.setCanceled(true);
   ((ServerLevel)l).sendParticles(ParticleTypes.ELECTRIC_SPARK,pr.getX(),pr.getY(),pr.getZ(),12,.3,.3,.3,.3);
   l.playSound(null,pr.blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.BLOCKS,1F,1.4F);
   m.clean.take(3_000);pr.discard();return;
  }
 }

 // ------------------------------------------------------------ network
 public record PanelData(BlockPos pos,CompoundTag data,boolean open) implements CustomPacketPayload {
  public static final Type<PanelData> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"panel_data"));
  public static final StreamCodec<RegistryFriendlyByteBuf,PanelData> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,PanelData::pos,ByteBufCodecs.COMPOUND_TAG,PanelData::data,ByteBufCodecs.BOOL,PanelData::open,PanelData::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record PanelAction(BlockPos pos,int action,String arg) implements CustomPacketPayload {
  public static final Type<PanelAction> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"panel_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf,PanelAction> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,PanelAction::pos,ByteBufCodecs.VAR_INT,PanelAction::action,ByteBufCodecs.stringUtf8(32),PanelAction::arg,PanelAction::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
   var r=e.registrar("16");
   r.playToClient(PanelData.TYPE,PanelData.CODEC,(p,c)->openScreen.accept(p));
   r.playToServer(PanelAction.TYPE,PanelAction.CODEC,(p,c)->{
    if(!(c.player() instanceof ServerPlayer sp)||sp.distanceToSqr(p.pos().getCenter())>100)return;
    if(sp.level().getBlockEntity(p.pos()) instanceof Panel b&&b.isMember(sp)){if(p.action()==0)PacketDistributor.sendToPlayer(sp,b.data((ServerLevel)sp.level(),false));else b.action(sp,p.action(),p.arg());}
   });
  }
 }
}
