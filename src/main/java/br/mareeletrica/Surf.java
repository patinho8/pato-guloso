package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.67.0: surfing. Waves are real moving swells on the water (from a wave machine, or out at sea behind you while you surf):
 *  lie on the board and paddle (W, A/D), paddle with a wave as it reaches you to catch it — you stand up and ride its face:
 *  A/D carve along the wave (down the face = faster, up = slower), Space near the crest = aerial (+A/D: 360),
 *  a hard turn back = cutback. Points for every second on the wave and for every manoeuvre. Shift gets off. */
public final class Surf {
 private Surf(){}
 public static final Supplier<EntityType<Board>> BOARD=MareEletrica.ACTORS.register("surfboard",()->EntityType.Builder.<Board>of(Board::new,MobCategory.MISC).sized(.8F,.2F).clientTrackingRange(10).updateInterval(2).build(MareEletrica.ID+":surfboard"));
 public static final Supplier<EntityType<Wave>> WAVE=MareEletrica.ACTORS.register("surf_wave",()->EntityType.Builder.<Wave>of(Wave::new,MobCategory.MISC).sized(1F,1F).noSave().clientTrackingRange(12).updateInterval(20).build(MareEletrica.ID+":surf_wave"));
 public static final DeferredItem<Item> BOARD_ITEM=MareEletrica.ITEMS.register("surfboard",()->new BoardItem(new Item.Properties().stacksTo(1)));
 public static final DeferredBlock<MachineBlock> MACHINE=MareEletrica.BLOCKS.register("wave_machine",()->new MachineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3F,6F).sound(SoundType.METAL)));
 public static final DeferredItem<BlockItem> MACHINE_ITEM=MareEletrica.ITEMS.register("wave_machine",()->new BlockItem(MACHINE.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Coloque na beira da água, virado para ela: solta uma onda a cada 7 s."));t.add(Component.literal("§8A onda anda na direção em que você olhava ao colocar."));}});
 public static final Supplier<BlockEntityType<MachineEntity>> MACHINE_ENTITY=MareEletrica.ENTITIES.register("wave_machine",()->BlockEntityType.Builder.of(MachineEntity::new,MACHINE.get()).build(null));
 public static void register(net.neoforged.bus.api.IEventBus bus){bus.addListener(Surf::packets);}
 public static void creative(java.util.function.Consumer<ItemStack> out){out.accept(new ItemStack(BOARD_ITEM.get()));out.accept(new ItemStack(MACHINE_ITEM.get()));}
 /** animation flags (rider → server → everybody) */
 public static final int PADDLE=1,RIDE=2,AIR=4,LEFT=8,RIGHT=16,SPIN=32,STROKE=64;
 public record Anim(int id,int flags) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
  public static final Type<Anim> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"surf_anim"));
  public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,Anim> CODEC=net.minecraft.network.codec.StreamCodec.composite(
   net.minecraft.network.codec.ByteBufCodecs.VAR_INT,Anim::id,net.minecraft.network.codec.ByteBufCodecs.VAR_INT,Anim::flags,Anim::new);
  @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 }
 static void packets(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){
  e.registrar("1").playToServer(Anim.TYPE,Anim.CODEC,(p,c)->c.enqueueWork(()->{
   if(c.player().level().getEntity(p.id()) instanceof Board b&&b.getControllingPassenger()==c.player())b.getEntityData().set(Board.ANIM,p.flags()&0xFF);}));
 }

 /** top of the water column at x,z near height y (NaN if no water there) */
 public static double surface(Level l,double x,double y,double z){
  var p=BlockPos.containing(x,y+.5,z);
  for(int i=0;i<4;i++){var fs=l.getFluidState(p);
   if(fs.is(FluidTags.WATER)){var up=l.getFluidState(p.above());if(up.is(FluidTags.WATER)){p=p.above();continue;}return p.getY()+fs.getHeight(l,p);}
   p=p.below();}
  return Double.NaN;}

 // ---------------- wave ----------------
 public static final double WAVE_SPEED=.28;
 public static final class Wave extends Entity {
  static final EntityDataAccessor<Float> SX=SynchedEntityData.defineId(Wave.class,EntityDataSerializers.FLOAT),SZ=SynchedEntityData.defineId(Wave.class,EntityDataSerializers.FLOAT),
   SY=SynchedEntityData.defineId(Wave.class,EntityDataSerializers.FLOAT),YAWD=SynchedEntityData.defineId(Wave.class,EntityDataSerializers.FLOAT),
   WIDTH=SynchedEntityData.defineId(Wave.class,EntityDataSerializers.FLOAT),HEIGHT=SynchedEntityData.defineId(Wave.class,EntityDataSerializers.FLOAT);
  static final EntityDataAccessor<Integer> BORN=SynchedEntityData.defineId(Wave.class,EntityDataSerializers.INT);
  public Wave(EntityType<? extends Wave> t,Level l){super(t,l);noPhysics=true;}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(SX,0F);b.define(SZ,0F);b.define(SY,0F);b.define(YAWD,0F);b.define(WIDTH,12F);b.define(HEIGHT,1.5F);b.define(BORN,0);}
  public static Wave spawn(ServerLevel l,Vec3 at,float yawDeg,float width,float height){
   var w=new Wave(WAVE.get(),l);w.entityData.set(SX,(float)at.x);w.entityData.set(SZ,(float)at.z);w.entityData.set(SY,(float)at.y);w.entityData.set(YAWD,yawDeg);
   w.entityData.set(WIDTH,width);w.entityData.set(HEIGHT,height);w.entityData.set(BORN,(int)l.getGameTime());w.setPos(at.x,at.y,at.z);l.addFreshEntity(w);
   l.playSound(null,BlockPos.containing(at),SoundEvents.AMBIENT_UNDERWATER_EXIT,SoundSource.AMBIENT,.6F,.6F);return w;}
  public float width(){return entityData.get(WIDTH);}public float height(){return entityData.get(HEIGHT);}
  public Vec3 dir(){double r=Math.toRadians(entityData.get(YAWD));return new Vec3(-Math.sin(r),0,Math.cos(r));}
  public float yawDeg(){return entityData.get(YAWD);}
  public double age(float pt){return level().getGameTime()-entityData.get(BORN)+pt;}
  public Vec3 crest(float pt){return new Vec3(entityData.get(SX),entityData.get(SY),entityData.get(SZ)).add(dir().scale(WAVE_SPEED*age(pt)));}
  /** along (+ ahead of the crest, on the steep face) and across the wave */
  public double[] local(Vec3 p,float pt){Vec3 d=p.subtract(crest(pt));Vec3 f=dir();return new double[]{d.x*f.x+d.z*f.z,d.x*-f.z+d.z*f.x};}
  /** height of the water above the flat sea at (along, across) */
  public double height(double a,double b){
   double H=height(),half=width()/2;double k=Mth.clamp(1-(Math.abs(b)-half)/2.5,0,1);if(k<=0)return 0;
   double h;
   if(a<-5||a>2.6)h=0;
   else if(a<=0){double x=(a+5)/5;h=H*x*x*(3-2*x);}
   else{double x=a/2.6;h=H*(1-x*x*(3-2*x));}
   return h*k;}
  @Override public void tick(){
   super.tick();
   Vec3 c=crest(0);setPos(c.x,c.y,c.z);setYRot(yawDeg());
   if(level().isClientSide){   // foam blowing off the crest
    var f=dir();Vec3 s=new Vec3(-f.z,0,f.x);
    for(int i=0;i<3;i++){double b=(random.nextDouble()-.5)*width();Vec3 q=c.add(s.scale(b)).add(f.scale(.2));level().addParticle(i==0?ParticleTypes.CLOUD:ParticleTypes.SPLASH,q.x,q.y+height()*.95,q.z,f.x*.1,.05,f.z*.1);}
    return;}
   double age=age(0);
   boolean water=!Double.isNaN(surface(level(),c.x+dir().x*1.5,c.y,c.z+dir().z*1.5));
   if(age>400||!water){   // breaks on the shore (or fades out)
    if(level() instanceof ServerLevel sl){var f=dir();Vec3 s=new Vec3(-f.z,0,f.x);for(int i=0;i<24;i++){Vec3 q=c.add(s.scale((random.nextDouble()-.5)*width()));sl.sendParticles(ParticleTypes.SPLASH,q.x,q.y+.4,q.z,4,.3,.2,.3,.1);}
     level().playSound(null,blockPosition(),SoundEvents.GENERIC_SPLASH,SoundSource.AMBIENT,.8F,.7F);}
    discard();}
  }
  @Override public void lerpTo(double x,double y,double z,float yr,float xr,int steps){}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<128*128;}
  @Override protected void readAdditionalSaveData(CompoundTag t){}
  @Override protected void addAdditionalSaveData(CompoundTag t){}
 }

 // ---------------- wave machine ----------------
 public static final class MachineBlock extends BaseEntityBlock {
  public MachineBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HorizontalDirectionalBlock.FACING);}
  @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,c.getHorizontalDirection());}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new MachineEntity(p,s);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,MACHINE_ENTITY.get(),(lv,ps,st,e)->e.tick());}
 }
 public static final class MachineEntity extends BlockEntity {
  public MachineEntity(BlockPos p,BlockState s){super(MACHINE_ENTITY.get(),p,s);}
  void tick(){
   if(!(level instanceof ServerLevel sl)||level.getGameTime()%140!=(worldPosition.asLong()&63))return;
   if(level.getNearestPlayer(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),64,false)==null)return;
   var f=getBlockState().getValue(HorizontalDirectionalBlock.FACING);Vec3 at=Vec3.atCenterOf(worldPosition).add(Vec3.atLowerCornerOf(f.getNormal()).scale(3));
   double s=surface(level,at.x,worldPosition.getY(),at.z);if(Double.isNaN(s))s=surface(level,at.x,worldPosition.getY()-1,at.z);if(Double.isNaN(s))return;
   Wave.spawn(sl,new Vec3(at.x,s,at.z),f.toYRot(),14F,1.8F);
   sl.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP,at.x,s,at.z,30,1.5,.2,1.5,.1);
  }
 }

 // ---------------- board ----------------
 public static final class BoardItem extends Item {
  public BoardItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);var hit=l.clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(6)),ClipContext.Block.OUTLINE,ClipContext.Fluid.SOURCE_ONLY,p));
   if(hit.getType()!=HitResult.Type.BLOCK||!l.getFluidState(hit.getBlockPos()).is(FluidTags.WATER))return InteractionResultHolder.pass(s);
   if(!l.isClientSide){var b=new Board(BOARD.get(),l);var at=hit.getLocation();b.moveTo(at.x,at.y,at.z,p.getYRot(),0);l.addFreshEntity(b);
    l.playSound(null,b.blockPosition(),SoundEvents.GENERIC_SPLASH,SoundSource.PLAYERS,.5F,1.3F);if(!p.isCreative())s.shrink(1);}
   return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Botão direito na água: põe a prancha. Clique nela para subir (Shift desce)."));
   t.add(Component.literal("§7Deitado: W rema, A/D vira. Reme junto com a onda quando ela chegar: você levanta!"));
   t.add(Component.literal("§7Na onda: A/D curva • Espaço na crista: aéreo (+A/D: 360) • virada forte: cutback"));
   t.add(Component.literal("§8No mar as ondas vêm de trás de você; numa piscina use a Máquina de Ondas."));}
 }
 public static final class Board extends Entity {
  public static final EntityDataAccessor<Integer> ANIM=SynchedEntityData.defineId(Board.class,EntityDataSerializers.INT);
  public float speed,ride,lean,leanO,spin,spinO,tilt,tiltO;public int state,air,lerpSteps;   // state 0 paddle, 1 riding, 2 air
  double lx,ly,lz,lyr,vy;boolean jumpWas;int localFlags,sentFlags=-1,waveId=-1,points,ridingTicks,turnWin;float turnSum,lastYaw;int swellCd=120;
  public Board(EntityType<? extends Board> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(ANIM,0);}
  public int anim(){return isControlledByLocalInstance()&&level().isClientSide?localFlags:entityData.get(ANIM);}
  @Override public LivingEntity getControllingPassenger(){return getFirstPassenger() instanceof Player p?p:null;}
  @Override public boolean shouldRiderSit(){return false;}
  @Override public boolean isPickable(){return !isRemoved();}
  @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty();}
  /** lying: the rider is lowered so the camera sits just above the board (the renderer lays the body down) */
  public static final double LIE_DROP=1.3,DECK=.16;
  @Override protected Vec3 getPassengerAttachmentPoint(Entity e,EntityDimensions d,float s){return new Vec3(0,DECK,0);}
  @Override protected void positionRider(Entity r,Entity.MoveFunction m){
   if(!hasPassenger(r))return;boolean lying=(anim()&(RIDE|AIR))==0;
   m.accept(r,getX(),getY()+DECK-(lying?LIE_DROP:0),getZ());
   if(r instanceof LivingEntity le){le.yBodyRot=getYRot()+(lying?0:90F);if(Math.abs(Mth.wrapDegrees(le.yBodyRotO-le.yBodyRot))>45)le.yBodyRotO=le.yBodyRot;}
  }
  @Override public InteractionResult interact(Player p,InteractionHand hand){
   if(p.isSecondaryUseActive()||isVehicle())return InteractionResult.PASS;
   if(!level().isClientSide){p.startRiding(this);p.displayClientMessage(Component.literal("§b🏄 Deitado: W rema, A/D vira • reme junto quando a onda chegar • Shift desce"),true);}
   return InteractionResult.sidedSuccess(level().isClientSide);}
  @Override public boolean hurt(DamageSource src,float amount){
   if(level().isClientSide||isRemoved()||isVehicle())return false;
   if(src.getEntity() instanceof Player p){if(!p.isCreative())spawnAtLocation(new ItemStack(BOARD_ITEM.get()));discard();return true;}return false;}
  @Override public void lerpTo(double x,double y,double z,float yr,float xr,int steps){lx=x;ly=y;lz=z;lyr=yr;lerpSteps=3;}
  @Override public double lerpTargetX(){return lerpSteps>0?lx:getX();}
  @Override public double lerpTargetY(){return lerpSteps>0?ly:getY();}
  @Override public double lerpTargetZ(){return lerpSteps>0?lz:getZ();}
  @Override public float lerpTargetYRot(){return lerpSteps>0?(float)lyr:getYRot();}
  Wave wave(){return waveId>=0&&level().getEntity(waveId) instanceof Wave w&&!w.isRemoved()?w:null;}
  /** the wave lifting this spot (biggest), or null */
  Wave waveAt(Vec3 p){Wave best=null;double bh=.02;for(var w:level().getEntitiesOfClass(Wave.class,getBoundingBox().inflate(24))){double[] lc=w.local(p,0);double h=w.height(lc[0],lc[1]);if(h>bh){bh=h;best=w;}}return best;}
  double waveHeight(Vec3 p){double h=0;for(var w:level().getEntitiesOfClass(Wave.class,getBoundingBox().inflate(24))){double[] lc=w.local(p,0);h=Math.max(h,w.height(lc[0],lc[1]));}return h;}
  @Override public void tick(){
   super.tick();leanO=lean;spinO=spin;tiltO=tilt;
   if(!level().isClientSide)serverSwell();
   if(!isControlledByLocalInstance()){
    if(lerpSteps>0){lerpPositionAndRotationStep(lerpSteps,lx,ly,lz,lyr,getXRot());lerpSteps--;}
    if(level().isClientSide)animate(anim());
    return;}
   var rider=getControllingPassenger();
   float fwd=0,left=0;boolean jump=false;
   if(rider!=null&&level().isClientSide){var in=Skate.input.get();fwd=in[0];left=in[1];jump=in[2]>0;}
   double r=Math.toRadians(getYRot());Vec3 head=new Vec3(-Math.sin(r),0,Math.cos(r));
   double sea=surface(level(),getX(),getY(),getZ());
   boolean wet=!Double.isNaN(sea);
   Wave w=wave();
   if(state==1&&w==null)loseWave(rider,"a onda acabou");
   if(!wet&&state!=2){   // beached: it just slides to a stop on land
    speed*=.7F;vy-=.08;move(MoverType.SELF,new Vec3(head.x*speed,vy,head.z*speed));if(onGround())vy=0;state=0;flags(rider,fwd,left);return;}
   switch(state){
    case 0->{   // lying and paddling
     if(fwd>0)speed=Math.min(.24F,speed+.014F);else if(fwd<0)speed*=.85F;speed*=.975F;
     setYRot(getYRot()-left*3.2F);
     Vec3 v=head.scale(speed);
     Wave lift=waveAt(position());
     if(lift!=null&&rider!=null){double[] lc=lift.local(position(),0);
      if(lc[0]>=-.2&&lc[0]<=2.4&&Math.abs(lc[1])<lift.width()/2&&head.dot(lift.dir())>.25&&(speed>.06F||fwd>0)){   // caught it!
       state=1;waveId=lift.getId();ride=.22F;points=0;ridingTicks=0;turnSum=0;lastYaw=getYRot();
       sound(SoundEvents.GENERIC_SPLASH,.7F,1.2F);msg(rider,"§b§l🌊 PEGOU A ONDA! §7A/D: curva • Espaço na crista: aéreo");}
      else v=v.add(lift.dir().scale(.06));}   // a passing swell nudges you along
     move(MoverType.SELF,new Vec3(v.x,0,v.z));
     setPos(getX(),sea-.06+waveHeight(position()),getZ());
    }
    case 1->{   // standing, riding the face: the wave carries you; pointing down the face pulls you toward its foot, up toward the lip,
               // and the sideways part of your heading runs you along the wave (faster after dropping down the face)
     setYRot(getYRot()-left*5F);r=Math.toRadians(getYRot());head=new Vec3(-Math.sin(r),0,Math.cos(r));
     Vec3 dir=w.dir(),across=new Vec3(-dir.z,0,dir.x);double down=head.dot(dir),side=head.dot(across);
     double[] lc=w.local(position(),0);
     ride=Mth.clamp(ride+(float)(down*.008)-.0015F,.06F,.36F);
     double aTarget=.25+1.6*Math.max(0,down)*Math.max(0,down)-.4*Math.max(0,-down);   // across the wave you stay high on the face
     double along=WAVE_SPEED+Mth.clamp((aTarget-lc[0])*.2,-.1,.1);
     rv=dir.scale(along).add(across.scale(side*(.08+ride*.6)));
     move(MoverType.SELF,new Vec3(rv.x,0,rv.z));
     lc=w.local(position(),0);
     if(lc[0]>3.2||lc[0]<-1.6||Math.abs(lc[1])>w.width()/2+1.5){loseWave(rider,lc[0]<-1.6?"passou por cima da onda":Math.abs(lc[1])>w.width()/2?"saiu pela ponta da onda":"a onda passou");break;}
     setPos(getX(),sea-.04+w.height(lc[0],lc[1]),getZ());
     ridingTicks++;if(ridingTicks%10==0)points+=5;
     // cutback: a big turn (> 150°) in under 1.5 s
     float dy=Mth.wrapDegrees(getYRot()-lastYaw);lastYaw=getYRot();turnSum+=Math.abs(dy);if(++turnWin>30){turnWin=0;turnSum=0;}
     if(turnSum>150){turnSum=0;turnWin=0;score(rider,"Cutback",150);}
     if(jump&&!jumpWas&&lc[0]<1.1){state=2;air=0;vy=.3+ride*.4;spin=0;spinTarget=left!=0?360*Math.signum(-left):0;sound(SoundEvents.GENERIC_SPLASH,.6F,1.5F);}
     if(level().isClientSide&&tickCount%2==0){Vec3 sp=position().subtract(head.scale(.9));level().addParticle(ParticleTypes.SPLASH,sp.x,sp.y+.1,sp.z,-head.x*.2,.15,-head.z*.2);}
     if(tickCount%8==0)sound(SoundEvents.BOAT_PADDLE_WATER,.35F,1.4F);
    }
    case 2->{   // aerial: keeps the riding velocity
     air++;vy-=.055;Vec3 v=rv;
     move(MoverType.SELF,new Vec3(v.x,vy,v.z));
     if(horizontalCollision)rv=rv.scale(.3);
     if(verticalCollision&&vy<0&&Double.isNaN(surface(level(),getX(),getY(),getZ()))){   // came down on dry land
      vy=0;spin=0;spinTarget=0;sound(SoundEvents.WOOD_FALL,.8F,1F);loseWave(rider,"caiu fora da água");state=0;break;}
     if(spinTarget!=0)spin=(float)Mth.clamp(spinTarget*Math.min(1,air/14.0),-360,360);
     double[] lc=w!=null?w.local(position(),0):null;
     double floor=(wet?sea:getY()-1)+(lc!=null?w.height(lc[0],lc[1]):0);
     if(vy<0&&getY()<=floor){
      setPos(getX(),floor,getZ());vy=0;
      boolean onWave=lc!=null&&lc[0]>-1.4&&lc[0]<2.2&&Math.abs(lc[1])<w.width()/2+1;
      score(rider,spinTarget!=0?"Aéreo 360":"Aéreo",(spinTarget!=0?400:250)+air*10);spin=0;spinTarget=0;sound(SoundEvents.GENERIC_SPLASH,.8F,1F);
      if(onWave)state=1;else loseWave(rider,"caiu fora da onda");}
    }
   }
   jumpWas=jump;
   float want=state==1?(left>0?-14F:left<0?14F:0):0;lean+=(want-lean)*.3F;
   tilt=state==1?-12F:0;
   flags(rider,fwd,left);
  }
  double spinTarget;Vec3 rv=Vec3.ZERO;
  void flags(LivingEntity rider,float fwd,float left){
   if(!level().isClientSide||rider==null)return;
   localFlags=(state==0?PADDLE:0)|(state==1?RIDE:0)|(state==2?AIR:0)|(left>0?LEFT:0)|(left<0?RIGHT:0)|(spinTarget!=0?SPIN:0)|(state==0&&fwd>0?STROKE:0);
   if(localFlags!=sentFlags){sentFlags=localFlags;net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Anim(getId(),localFlags));}
  }
  void animate(int f){float want=(f&RIDE)!=0?((f&LEFT)!=0?-14F:(f&RIGHT)!=0?14F:0):0;lean+=(want-lean)*.3F;tilt=(f&RIDE)!=0?-12F:0;
   if((f&AIR)!=0&&(f&SPIN)!=0)spin=Math.min(360,spin+26F);else spin=0;}
  void loseWave(LivingEntity rider,String why){
   if(state!=0&&rider instanceof Player p&&level().isClientSide&&points>0)p.displayClientMessage(Component.literal("§b🌊 Onda encerrada §7("+why+") §e"+points+" pontos"),false);
   state=0;waveId=-1;speed=Math.min(.12F,ride);points=0;}
  void score(LivingEntity rider,String name,int pts){points+=pts;if(rider instanceof Player p&&level().isClientSide)p.displayClientMessage(Component.literal("§b🏄 "+name+"! §7+"+pts+" §e(onda: "+points+")"),true);}
  void msg(LivingEntity rider,String t){if(rider instanceof Player p&&level().isClientSide)p.displayClientMessage(Component.literal(t),true);}
  void sound(net.minecraft.sounds.SoundEvent s,float vol,float pitch){
   if(level().isClientSide)level().playLocalSound(getX(),getY(),getZ(),s,SoundSource.PLAYERS,vol,pitch,false);
   else level().playSound(null,getX(),getY(),getZ(),s,SoundSource.PLAYERS,vol,pitch);}
  /** out at sea (deep water, no machine waves around) a swell rolls in from behind the surfer every ~8 s */
  void serverSwell(){
   if(!(level() instanceof ServerLevel sl)||getControllingPassenger()==null||--swellCd>0)return;swellCd=150+random.nextInt(60);
   double s=surface(level(),getX(),getY(),getZ());if(Double.isNaN(s))return;
   for(int d=1;d<=3;d++)if(!level().getFluidState(BlockPos.containing(getX(),s-d-.5,getZ())).is(FluidTags.WATER))return;   // only in deep water
   if(!level().getEntitiesOfClass(Wave.class,getBoundingBox().inflate(30)).isEmpty())return;
   double r=Math.toRadians(getYRot());Vec3 head=new Vec3(-Math.sin(r),0,Math.cos(r));Vec3 at=position().subtract(head.scale(20));
   double s2=surface(level(),at.x,s,at.z);if(Double.isNaN(s2))return;
   Wave.spawn(sl,new Vec3(at.x,s2,at.z),getYRot(),16F,1.3F+random.nextFloat()*.6F);
   if(getControllingPassenger() instanceof Player p)p.displayClientMessage(Component.literal("§3🌊 Vem uma onda atrás de você — reme!"),true);
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){}
  @Override protected void readAdditionalSaveData(CompoundTag t){}
 }
}
