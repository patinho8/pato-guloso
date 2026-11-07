package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Radar table (3x3): a big round screen with a spinning dish on its mast. It shows aircraft (red) and vehicles and players (yellow)
 *  within 128 blocks, north up (no mobs). When an aircraft, drone or missile is within 128 blocks the alarm lamp lights and it gives a redstone signal. */
public final class Radar {
 public static final int RANGE=160,SCREEN_RANGE=128;
 public static final BooleanProperty ALERT=BooleanProperty.create("alert");
 public static final DeferredBlock<RadarBlock> BLOCK=MareEletrica.BLOCKS.register("radar",()->new RadarBlock(BlockBehaviour.Properties.of().strength(3F,8F).sound(SoundType.METAL).noOcclusion()));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("radar",()->new BlockItem(BLOCK.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<RadarEntity>> ENTITY=MareEletrica.ENTITIES.register("radar",()->BlockEntityType.Builder.of(RadarEntity::new,BLOCK.get()).build(null));
 private Radar(){}
 public static void register(){}
 /** 0 = not shown, 1 = aircraft / missile (red), 2 = vehicle or player (yellow). Mobs are never shown. */
 public static int kind(Entity e){
  if(e instanceof Jets.JetEntity||e instanceof Drones.DroneEntity||e instanceof Missiles.MissileEntity||e instanceof Vehicles.Helicopter h&&!h.onGround())return 1;
  if(e instanceof Player p&&!p.isSpectator()||e instanceof Vehicles.Base)return 2;
  return 0;
 }
 public static String name(Entity e){
  if(e instanceof Jets.JetEntity)return "Caça";if(e instanceof Vehicles.Helicopter)return "Helicóptero";if(e instanceof Drones.DroneEntity)return "Drone";
  if(e instanceof Missiles.MissileEntity)return "Míssil";if(e instanceof Vehicles.Tank)return "Tanque";
  return e.getName().getString();
 }
 private static String bearing(double dx,double dz){String[] n={"N","NE","L","SE","S","SO","O","NO"};double a=Math.toDegrees(Math.atan2(dx,-dz));return n[(int)Math.round(((a%360)+360)%360/45)%8];}

 public static final class RadarBlock extends HorizontalDirectionalBlock implements EntityBlock,Multiblock.Master {
  static final double[][] BOXES={{-16,13,-16,32,15,32},{-15,0,-15,-13,13,-13},{29,0,-15,31,13,-13},{-15,0,29,-13,13,31},{29,0,29,31,13,31},{26,15,26,30,32,30}};
  @Override public java.util.List<BlockPos> cells(BlockState s){return Multiblock.cellsOf(s.getValue(FACING),BOXES);}
  @Override public VoxelShape cellShape(BlockState s,BlockPos o){return Multiblock.slice(s.getValue(FACING),BOXES,o);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock()))Multiblock.clear(l,p,s);super.onRemove(s,l,p,n,moving);}

  public static final MapCodec<RadarBlock> CODEC=simpleCodec(RadarBlock::new);
  private static final VoxelShape SHAPE=Block.box(0,0,0,16,15,16);
  public RadarBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(ALERT,false));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ALERT);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var st=defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());return Multiblock.fits(c,st)?st:null;}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack st){Multiblock.place(l,p,s);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return cellShape(s,BlockPos.ZERO);}
  @Override protected boolean isSignalSource(BlockState s){return true;}
  @Override protected int getSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return s.getValue(ALERT)?15:0;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new RadarEntity(p,s);}
  @SuppressWarnings("unchecked")
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=ENTITY.get()?null:(BlockEntityTicker<T>)(BlockEntityTicker<RadarEntity>)RadarEntity::tick;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(l.isClientSide)return InteractionResult.SUCCESS;
   var c=p.getCenter();var found=new java.util.ArrayList<Entity>();
   for(var e:l.getEntitiesOfClass(Entity.class,new AABB(p).inflate(RANGE),e->kind(e)>0&&e!=player))if(kind(e)!=3)found.add(e);
   found.sort(java.util.Comparator.comparingDouble(e->e.distanceToSqr(c)));
   if(found.isEmpty()){player.displayClientMessage(Component.literal("Radar: nenhum contato a até "+RANGE+" blocos."),false);return InteractionResult.CONSUME;}
   player.displayClientMessage(Component.literal("§aRadar: "+found.size()+" contato(s)"),false);
   for(int i=0;i<Math.min(8,found.size());i++){var e=found.get(i);double dx=e.getX()-c.x,dz=e.getZ()-c.z;
    player.displayClientMessage(Component.literal((kind(e)==1?"§c":"§e")+" • "+name(e)+"  "+Math.round(Math.sqrt(dx*dx+dz*dz))+" m "+bearing(dx,dz)+"  alt "+(int)e.getY()),false);}
   return InteractionResult.CONSUME;
  }
 }

 public static final class RadarEntity extends BlockEntity {
  public RadarEntity(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  static void tick(Level l,BlockPos p,BlockState s,RadarEntity r){
   if(l.getGameTime()%20!=Math.floorMod(p.asLong(),20))return;
   boolean alert=!l.getEntitiesOfClass(Entity.class,new AABB(p).inflate(RANGE),e->kind(e)==1&&!ownAircraftOnGround(e)).isEmpty();
   if(alert!=s.getValue(ALERT)){l.setBlock(p,s.setValue(ALERT,alert),3);l.updateNeighborsAt(p,s.getBlock());if(alert)l.playSound(null,p,SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.BLOCKS,1F,.6F);}
   if(alert&&l instanceof ServerLevel&&l.getGameTime()%40<20)l.playSound(null,p,SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.BLOCKS,.6F,1.8F);
  }
  private static boolean ownAircraftOnGround(Entity e){return e.onGround();}
 }
}
