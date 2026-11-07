package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Ten-tube barrage launcher: fires salvos of rockets that climb high and come down on the target area. Holds up to 1000 rockets. */
public final class Barrage {
 public static final int MAX_AMMO=1000,TUBES=10,SHOT_GAP=3;
 public static final double SPEED=4.6,GRAVITY=.045;
 public static final DeferredBlock<LauncherBlock> BLOCK=MareEletrica.BLOCKS.register("barrage_launcher",()->new LauncherBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(4F,12F).noOcclusion().sound(SoundType.METAL)));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("barrage_launcher",()->new BlockItem(BLOCK.get(),new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> ROCKET=MareEletrica.ITEMS.register("barrage_rocket",()->new Item(new Item.Properties().stacksTo(99)));
 public static final Supplier<BlockEntityType<LauncherEntity>> ENTITY=MareEletrica.ENTITIES.register("barrage_launcher",()->BlockEntityType.Builder.of(LauncherEntity::new,BLOCK.get()).build(null));
 public static final Supplier<EntityType<RocketEntity>> ROCKET_ENTITY=MareEletrica.ACTORS.register("barrage_rocket",()->
  EntityType.Builder.<RocketEntity>of(RocketEntity::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(16).updateInterval(1).build(MareEletrica.ID+":barrage_rocket"));
 private Barrage(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(ITEM.get());out.accept(ROCKET.get());}
 /** Tube layout on the pod face, in pixels from the pod centre: 5 columns x 2 rows. */
 public static float tubeX(int i){return ((i%5)-2)*2.3F;}
 public static float tubeY(int i){return (i<5?1:-1)*1.4F;}
 public static final float PIVOT_Y=11F/16F;

 public static final class LauncherBlock extends BaseEntityBlock {
  public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
  public static final MapCodec<LauncherBlock> CODEC=simpleCodec(LauncherBlock::new);
  private static final VoxelShape SHAPE=Block.box(0,0,0,16,12,16);
  public LauncherBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection());}
  @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
  @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new LauncherEntity(pos,state);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,ENTITY.get(),LauncherEntity::tick);}
  @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
   if(!Batteries.AmmoCrate.holds(stack,ROCKET.get()))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   if(!level.isClientSide&&level.getBlockEntity(pos) instanceof LauncherEntity l){
    int n=Batteries.AmmoCrate.feed(stack,ROCKET.get(),MAX_AMMO-l.ammo,player);
    if(n<=0)player.displayClientMessage(Component.literal("Lançador cheio ("+MAX_AMMO+" foguetes)."),true);
    else{l.ammo+=n;l.sync();level.playSound(null,pos,SoundEvents.ARMOR_EQUIP_IRON.value(),SoundSource.BLOCKS,1F,.8F);player.displayClientMessage(Component.literal("Foguetes: "+l.ammo+" / "+MAX_AMMO),true);}
   }
   return ItemInteractionResult.sidedSuccess(level.isClientSide);
  }
  @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
   if(!level.isClientSide&&level.getBlockEntity(pos) instanceof LauncherEntity l){
    if(player.isShiftKeyDown()){l.auto=!l.auto;l.sync();player.displayClientMessage(Component.literal((l.auto?"ATAQUE AUTOMÁTICO ligado: ataca monstros entre 20 e 200 blocos.":"Ataque automático desligado.")+"  •  Foguetes "+l.ammo+"/"+MAX_AMMO),true);}
    else{   // turns to where you are looking (any direction) and fires
     l.aim(Math.round(player.getYRot()-l.facing().toYRot()),l.elevation);   // same elevation, turned the way you face
     if(!l.fire())player.displayClientMessage(Component.literal(l.ammo<=0?"Sem foguetes. Clique com foguetes do lançador para carregar.":"Recarregando os tubos..."),true);
    }
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
  @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos from,boolean moving){
   if(level.isClientSide||!(level.getBlockEntity(pos) instanceof LauncherEntity l))return;
   boolean on=level.hasNeighborSignal(pos);
   if(on&&!l.powered)l.fire();
   l.powered=on;
  }
  @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){   // the rockets inside come back in ammo crates
   if(!state.is(next.getBlock())&&level.getBlockEntity(pos) instanceof LauncherEntity l&&l.ammo>0){Batteries.AmmoCrate.drop(level,pos,ROCKET.get(),l.ammo);l.ammo=0;}
   super.onRemove(state,level,pos,next,moving);
  }
  @Override protected boolean triggerEvent(BlockState state,Level level,BlockPos pos,int id,int param){
   super.triggerEvent(state,level,pos,id,param);
   if(id==1&&level.getBlockEntity(pos) instanceof LauncherEntity l&&param>=0&&param<TUBES)l.firedAt[param]=level.getGameTime();
   return true;
  }
 }

 public static final class LauncherEntity extends BlockEntity {
  public int ammo,yaw,elevation=65,salvo=-1,shot;public boolean powered,auto;private long nextShot,readyAt;
  /** Client animation: smoothed aim and when each tube last fired (for recoil and flash). */
  public float curYaw,curYawO,curElev=65,curElevO=65;public final long[] firedAt=new long[TUBES];
  public LauncherEntity(BlockPos pos,BlockState state){super(ENTITY.get(),pos,state);java.util.Arrays.fill(firedAt,-100);}
  public Direction facing(){return getBlockState().getValue(LauncherBlock.FACING);}
  /** Turn toward a point and pick the high-arc elevation that lands on it (rockets go up and come down on it). */
  public boolean aimAt(Vec3 target){
   var c=Vec3.atLowerCornerOf(worldPosition).add(.5,PIVOT_Y,.5);double dx=target.x-c.x,dz=target.z-c.z,dist=Math.sqrt(dx*dx+dz*dz);
   if(dist<4)return false;
   int az=MortarAim.azimuth(Math.round((float)Math.toDegrees(Math.atan2(-dx,dz))-facing().toYRot()));
   int el=solve(dist,target.y-c.y);
   aim(az,el);return el>0;
  }
  /** Highest elevation (30..88) whose trajectory reaches the distance at that height; -best if it is out of range. */
  public static int solve(double dist,double dy){
   int best=45;double bestRange=-1;
   for(int el=88;el>=30;el--){
    double range=range(el,dy);
    if(range>=dist)return el;
    if(range>bestRange){bestRange=range;best=el;}
   }
   return -best;
  }
  /** Horizontal distance where a rocket fired at this elevation comes down through height dy (same physics as the entity). */
  public static double range(int el,double dy){
   double a=Math.toRadians(el),vx=Math.cos(a)*SPEED,vy=Math.sin(a)*SPEED,x=1.05*Math.cos(a),y=1.05*Math.sin(a);
   for(int t=0;t<900;t++){double px=x,py=y;x+=vx;y+=vy;vx*=.99;vy=vy*.99-GRAVITY;if(vy<0&&y<=dy&&py>dy)return px+(x-px)*(py-dy)/(py-y);if(y<dy-200)break;}
   return 0;
  }
  /** Sets the aim (any direction, 5..88 degrees up); returns how many ticks the turret needs to get there. */
  public int aim(int az,int el){if(el<0)el=-el;az=MortarAim.azimuth(az);el=Mth.clamp(el,5,88);
   int turn=(int)Math.ceil(Math.max(Math.abs(Mth.wrapDegrees((float)(az-yaw)))/8F,Math.abs(el-elevation)/4F));
   if(az!=yaw||el!=elevation){yaw=az;elevation=el;if(level!=null)turnUntil=Math.max(turnUntil,level.getGameTime()+turn);sync();}
   return turn;}
  private long turnUntil;
  public boolean fire(){
   if(level==null||ammo<=0||salvo>=0||level.getGameTime()<readyAt)return false;
   salvo=Math.min(TUBES,ammo);shot=0;nextShot=Math.max(level.getGameTime(),turnUntil);setChanged();return true;   // waits for the turret to finish turning
  }
  public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
  public static void tick(Level level,BlockPos pos,BlockState state,LauncherEntity l){
   if(level.isClientSide){
    l.curYawO=l.curYaw;l.curElevO=l.curElev;
    l.curYaw+=Mth.clamp(Mth.wrapDegrees(l.yaw-l.curYaw),-8F,8F);l.curElev+=Mth.clamp(l.elevation-l.curElev,-4F,4F);
    return;
   }
   if(l.auto&&l.salvo<0&&l.ammo>0&&level.getGameTime()>=l.readyAt&&level.getGameTime()%20==0){   // automatic attack: nearest monster in range
    var c=pos.getCenter();net.minecraft.world.entity.LivingEntity best=null;double bd=Double.MAX_VALUE;
    for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(200,120,200),e->e instanceof net.minecraft.world.entity.monster.Enemy&&e.isAlive())){
     double d=e.position().distanceToSqr(c.x,e.getY(),c.z);if(d<20*20||d>200*200)continue;if(d<bd){bd=d;best=e;}
    }
    if(best!=null&&l.aimAt(best.position().add(best.getDeltaMovement().scale(40))))l.fire();
   }
   if(l.salvo<0||level.getGameTime()<l.nextShot)return;
   if(l.shot>=l.salvo||l.ammo<=0){l.salvo=-1;l.readyAt=level.getGameTime()+40;l.sync();level.playSound(null,pos,SoundEvents.PISTON_CONTRACT,SoundSource.BLOCKS,.8F,.6F);return;}
   int tube=l.shot++;l.ammo--;l.nextShot=level.getGameTime()+SHOT_GAP;
   var f=l.facing();double[] d=MortarAim.direction(l.elevation,l.yaw,f.getStepX(),f.getStepZ());Vec3 dir=new Vec3(d[0],d[1],d[2]);
   // tube mouth: pivot + forward, shifted by the tube's place on the pod face
   Vec3 right=new Vec3(-dir.z,0,dir.x).normalize(),up=right.cross(dir).normalize();
   Vec3 mouth=Vec3.atLowerCornerOf(pos).add(.5,PIVOT_Y,.5).add(dir.scale(1.05)).add(right.scale(tubeX(tube)/16F)).add(up.scale(tubeY(tube)/16F));
   var r=new RocketEntity(ROCKET_ENTITY.get(),level);r.setPos(mouth.x,mouth.y,mouth.z);r.setItem(new ItemStack(ROCKET.get()));
   var rnd=level.getRandom();r.setDeltaMovement(dir.scale(SPEED).add(rnd.nextGaussian()*.03,rnd.nextGaussian()*.03,rnd.nextGaussian()*.03));
   level.addFreshEntity(r);
   level.blockEvent(pos,state.getBlock(),1,tube);
   level.playSound(null,pos,MareEletrica.LAUNCHER_FIRE.get(),SoundSource.BLOCKS,1.2F,.9F+rnd.nextFloat()*.2F);
   if(level instanceof ServerLevel sl){sl.sendParticles(ParticleTypes.LARGE_SMOKE,mouth.x,mouth.y,mouth.z,8,.2,.2,.2,.04);sl.sendParticles(ParticleTypes.FLAME,mouth.x,mouth.y,mouth.z,6,.1,.1,.1,.08);
    Vec3 back=Vec3.atLowerCornerOf(pos).add(.5,PIVOT_Y,.5).subtract(dir.scale(.6));sl.sendParticles(ParticleTypes.CLOUD,back.x,back.y,back.z,10,.25,.2,.25,.06);}   // back blast
   if(l.shot%2==0)l.sync();else l.setChanged();
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("Ammo",ammo);t.putInt("Yaw",yaw);t.putInt("Elevation",elevation);t.putBoolean("Powered",powered);t.putBoolean("Auto",auto);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);ammo=t.getInt("Ammo");yaw=t.getInt("Yaw");elevation=t.contains("Elevation")?t.getInt("Elevation"):65;powered=t.getBoolean("Powered");auto=t.getBoolean("Auto");}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
  @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }

 /** Rocket: climbs steeply, arcs over and comes down; whistles on the way down and explodes on impact. */
 public static final class RocketEntity extends ThrowableItemProjectile {
  private boolean whistled;
  public RocketEntity(EntityType<? extends RocketEntity> type,Level level){super(type,level);}
  @Override protected Item getDefaultItem(){return ROCKET.get();}
  @Override protected double getDefaultGravity(){return GRAVITY;}
  @Override public void tick(){
   super.tick();
   Vec3 v=getDeltaMovement();
   if(level().isClientSide){
    level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,getX(),getY(),getZ(),0,.01,0);
    if(tickCount<25)level().addParticle(ParticleTypes.FLAME,getX(),getY(),getZ(),-v.x*.1,-v.y*.1,-v.z*.1);
    return;
   }
   if(!whistled&&v.y<-.4){whistled=true;level().playSound(null,getX(),getY(),getZ(),MareEletrica.SHELL_WHISTLE.get(),SoundSource.NEUTRAL,1.5F,.9F+random.nextFloat()*.2F);}
   if(tickCount>600||getY()<level().getMinBuildHeight()-16)discard();
  }
  @Override protected void onHit(HitResult result){
   super.onHit(result);
   if(level().isClientSide)return;
   level().explode(this,null,null,getX(),getY(),getZ(),2.8F,false,Level.ExplosionInteraction.TNT,   // blows up blocks too
    ParticleTypes.EXPLOSION,ParticleTypes.EXPLOSION_EMITTER,MareEletrica.SILENT);
   level().playSound(null,getX(),getY(),getZ(),MareEletrica.FRAG_BLAST.get(),SoundSource.NEUTRAL,1.2F,.75F+random.nextFloat()*.2F);
   if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+.5,getZ(),30,1.2,.6,1.2,.05);
   discard();
  }
 }
}
