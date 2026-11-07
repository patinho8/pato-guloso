package br.mareeletrica;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.65.6: skateboard. Put it on the ground, right-click to ride (Shift gets off).
 *  W pushes, S brakes, A/D turn. Downhill picks up speed. Space = ollie; Space while holding A/D = kickflip,
 *  holding S = pop shove-it. Land on rails, fences, walls or iron bars to grind (no friction + sparks).
 *  Movement runs on the rider's client (like a boat) so it feels instant; the server gets the positions. */
public final class Skate {
 private Skate(){}
 public static final Supplier<EntityType<Board>> ENTITY=MareEletrica.ACTORS.register("skateboard",()->EntityType.Builder.<Board>of(Board::new,MobCategory.MISC).sized(.95F,.3F).clientTrackingRange(10).updateInterval(2).build(MareEletrica.ID+":skateboard"));
 public static final DeferredItem<Item> ITEM=MareEletrica.ITEMS.register("skateboard",()->new BoardItem(new Item.Properties().stacksTo(1)));
 public static void register(net.neoforged.bus.api.IEventBus bus){bus.addListener(Skate::packets);}
 /** rider → server: the animation flags of the board (so everybody sees the push, the tricks and the stance) */
 public record Anim(int id,int flags) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
  public static final Type<Anim> TYPE=new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"skate_anim"));
  public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,Anim> CODEC=net.minecraft.network.codec.StreamCodec.composite(
   net.minecraft.network.codec.ByteBufCodecs.VAR_INT,Anim::id,net.minecraft.network.codec.ByteBufCodecs.VAR_INT,Anim::flags,Anim::new);
  @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type(){return TYPE;}
 }
 static void packets(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){
  e.registrar("1").playToServer(Anim.TYPE,Anim.CODEC,(p,c)->c.enqueueWork(()->{
   if(c.player().level().getEntity(p.id()) instanceof Board b&&b.getControllingPassenger()==c.player())b.getEntityData().set(Board.ANIM,p.flags()&0xFFF);}));
 }
 /** animation flags (0.66.0: 3 trick bits, manual, boardslide) */
 public static final int PUSH=1,BRAKE=2,GRIND=4,AIR=8,TRICK_SHIFT=4,LEFT=128,RIGHT=256,MANUAL=512,SLIDE=1024;
 /** tricks: 0 ollie, 1 kickflip, 2 heelflip, 3 pop shove-it, 4 360 flip, 5 air (off a ramp) */
 public static final String[] TRICKS={"Ollie","Kickflip","Heelflip","Pop Shove-it","360 Flip","Air"};
 static final int[] TRICK_PTS={50,150,150,120,300,80};
 /** set on the client: {forward, left, jump, sprint} of the local player's keys */
 public static Supplier<float[]> input=()->new float[4];
 public static final float MAX=.7F,PUSH_ACC=.024F;
 // ---------------- skatepark blocks ----------------
 public static final net.neoforged.neoforge.registries.DeferredBlock<HandrailBlock> RAIL=MareEletrica.BLOCKS.register("skate_rail",()->new HandrailBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(2F,6F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion()));
 public static final net.neoforged.neoforge.registries.DeferredBlock<RampBlock> RAMP=MareEletrica.BLOCKS.register("skate_ramp",()->new RampBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WOOD).strength(1.5F).sound(net.minecraft.world.level.block.SoundType.WOOD).noOcclusion()));
 public static final DeferredItem<net.minecraft.world.item.BlockItem> RAIL_ITEM=MareEletrica.ITEMS.register("skate_rail",()->new net.minecraft.world.item.BlockItem(RAIL.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Corrimão de skate: dê um ollie e caia em cima para fazer grind."));t.add(Component.literal("§8Segurando A/D ao cair: boardslide"));}});
 public static final DeferredItem<net.minecraft.world.item.BlockItem> RAMP_ITEM=MareEletrica.ITEMS.register("skate_ramp",()->new net.minecraft.world.item.BlockItem(RAMP.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Rampa de skate: suba rápido para voar. Sobe na direção em que você olha ao colocar."));t.add(Component.literal("§8No ar, aperte Espaço (+A/D/S) para uma manobra"));}});
 public static void creative(java.util.function.Consumer<ItemStack> out){out.accept(new ItemStack(ITEM.get()));out.accept(new ItemStack(RAIL_ITEM.get()));out.accept(new ItemStack(RAMP_ITEM.get()));}
 public static final class HandrailBlock extends net.minecraft.world.level.block.HorizontalDirectionalBlock {
  public HandrailBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,net.minecraft.core.Direction.NORTH));}
  @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection());}
  @Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.HorizontalDirectionalBlock> codec(){return com.mojang.serialization.MapCodec.unit(this);}
  @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){
   var f=s.getValue(FACING);return net.minecraft.world.phys.shapes.Shapes.or(Multiblock.box(f,7,10,0,9,12,16),Multiblock.box(f,7.3,0,7.3,8.7,10,8.7));}
 }
 /** a kicker ramp: 4 steps of 4 px under a smooth wooden slope, rising toward FACING */
 public static final class RampBlock extends net.minecraft.world.level.block.HorizontalDirectionalBlock {
  public RampBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,net.minecraft.core.Direction.NORTH));}
  @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection());}
  @Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.HorizontalDirectionalBlock> codec(){return com.mojang.serialization.MapCodec.unit(this);}
  @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){
   var f=s.getValue(FACING);var out=net.minecraft.world.phys.shapes.Shapes.empty();
   for(int i=0;i<4;i++)out=net.minecraft.world.phys.shapes.Shapes.or(out,Multiblock.box(f,0,0,12-4*i,16,4*(i+1),16-4*i));
   return out;}
 }

 public static final class BoardItem extends Item {
  public BoardItem(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();if(l.isClientSide)return InteractionResult.SUCCESS;
   var b=new Board(ENTITY.get(),l);var at=c.getClickLocation();b.moveTo(at.x,at.y,at.z,c.getPlayer()!=null?c.getPlayer().getYRot():0,0);
   if(!l.noCollision(b,b.getBoundingBox()))return InteractionResult.FAIL;
   l.addFreshEntity(b);l.playSound(null,b.blockPosition(),SoundEvents.WOOD_PLACE,SoundSource.PLAYERS,.8F,1.4F);
   if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   return InteractionResult.CONSUME;
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Ponha no chão e clique com o botão direito para subir (Shift desce)."));
   t.add(Component.literal("§7W empurra • S freia • A/D viram • Espaço: ollie • Ctrl: manual"));
   t.add(Component.literal("§7Espaço + A: kickflip • + D: heelflip • + S: shove-it • + S + A/D: 360 flip"));
   t.add(Component.literal("§7Corrimão, trilho, cerca: grind (A/D ao cair: boardslide) • rampas: voo"));}
 }

 public static final class Board extends Entity {
  public float speed;public int air,lerpSteps,pushCd;double lx,ly,lz,lyr;boolean jumpWas;public boolean grinding;
  /** client-side trick animation (only the rider's client) */
  public float flip,flipO,shove,shoveO,nose,noseO,lean,leanO;public int trick=-1;   // see TRICKS
  boolean lastRamp,slide,manual;int comboPts,comboN,comboIdle,manualTicks,grindTicks;
  public static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ANIM=SynchedEntityData.defineId(Board.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
  int localFlags,sentFlags=-1,animAir;
  public Board(EntityType<? extends Board> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(ANIM,0);}
  /** current animation flags: the rider's own client uses its live input, everybody else the synced value */
  public int anim(){return isControlledByLocalInstance()&&level().isClientSide?localFlags:entityData.get(ANIM);}
  @Override public LivingEntity getControllingPassenger(){return getFirstPassenger() instanceof Player p?p:null;}
  @Override public boolean shouldRiderSit(){return false;}
  @Override public boolean isPickable(){return !isRemoved();}
  @Override public float maxUpStep(){return .6F;}
  @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty();}
  public static final float SCALE=1.25F;   // 0.66.0: a bigger board
  public static final double DECK=.244*SCALE;   // top of the deck (grip tape) above the board's feet
  @Override protected Vec3 getPassengerAttachmentPoint(Entity e,EntityDimensions d,float s){return new Vec3(0,DECK,0);}
  /** feet exactly on the deck (the default puts a sitting rider 0.6 lower) and the body turned along the board */
  @Override protected void positionRider(Entity r,Entity.MoveFunction m){
   if(!hasPassenger(r))return;m.accept(r,getX(),getY()+DECK+(air>0&&trick>=0?Math.sin(Math.min(1,air/10.0)*Math.PI)*.05:0),getZ());
   if(r instanceof LivingEntity le){le.yBodyRot=getYRot()+90F;if(Math.abs(Mth.wrapDegrees(le.yBodyRotO-le.yBodyRot))>45)le.yBodyRotO=le.yBodyRot;}   // side stance, left foot forward
  }
  @Override public InteractionResult interact(Player p,InteractionHand hand){
   if(p.isSecondaryUseActive()||isVehicle())return InteractionResult.PASS;
   if(!level().isClientSide){p.startRiding(this);p.displayClientMessage(Component.literal("§7🛹 W empurra • A/D viram • Espaço: ollie (com A/D: kickflip, com S: shove-it) • Shift desce"),true);}
   return InteractionResult.sidedSuccess(level().isClientSide);
  }
  @Override public boolean hurt(DamageSource src,float amount){
   if(level().isClientSide||isRemoved()||isVehicle())return false;
   if(src.getEntity() instanceof Player p){if(!p.isCreative())spawnAtLocation(new ItemStack(ITEM.get()));discard();return true;}
   return false;
  }
  @Override public void lerpTo(double x,double y,double z,float yr,float xr,int steps){lx=x;ly=y;lz=z;lyr=yr;lerpSteps=3;}
  @Override public double lerpTargetX(){return lerpSteps>0?lx:getX();}
  @Override public double lerpTargetY(){return lerpSteps>0?ly:getY();}
  @Override public double lerpTargetZ(){return lerpSteps>0?lz:getZ();}
  @Override public float lerpTargetYRot(){return lerpSteps>0?(float)lyr:getYRot();}
  static boolean grindable(BlockState s){return s.getBlock() instanceof BaseRailBlock||s.is(BlockTags.FENCES)||s.is(BlockTags.WALLS)||s.getBlock() instanceof IronBarsBlock||s.getBlock() instanceof HandrailBlock;}
  @Override public void tick(){
   super.tick();
   flipO=flip;shoveO=shove;noseO=nose;leanO=lean;
   if(!isControlledByLocalInstance()){
    if(lerpSteps>0){lerpPositionAndRotationStep(lerpSteps,lx,ly,lz,lyr,getXRot());lerpSteps--;}
    if(level().isClientSide){int f=anim();animAir=(f&AIR)!=0?animAir+1:0;animate(f);}
    return;
   }
   var rider=getControllingPassenger();
   double yOld=getY();
   float fwd=0,left=0;boolean jump=false,sprint=false;
   if(rider!=null&&level().isClientSide){var in=input.get();fwd=in[0];left=in[1];jump=in[2]>0;sprint=in.length>3&&in[3]>0;}
   if(rider==null){speed*=onGround()?.8F:.98F;}
   var below=level().getBlockState(BlockPos.containing(getX(),getY()-.2,getZ()));
   boolean wasGrinding=grinding;grinding=onGround()&&speed>.1F&&(grindable(below)||grindable(level().getBlockState(blockPosition())));
   if(grinding&&!wasGrinding)slide=left!=0;   // A/D held when you land on the rail: boardslide (board across the rail)
   boolean onRamp=false;
   if(onGround()){double rr=Math.toRadians(getYRot());for(double o:new double[]{-.45,0,.45}){double x=getX()-Math.sin(rr)*o,z=getZ()+Math.cos(rr)*o;
    for(double dy:new double[]{.05,-.2})if(level().getBlockState(BlockPos.containing(x,getY()+dy,z)).getBlock() instanceof RampBlock)onRamp=true;}}
   if(onGround()){
    if(fwd>0&&speed<MAX*.8F){speed+=PUSH_ACC;if(--pushCd<=0){pushCd=14;sound(SoundEvents.GRAVEL_STEP,.35F,1.6F);}}
    else pushCd=Math.min(pushCd,2);
    if(fwd<0){speed*=.88F;if(speed>.15F&&tickCount%3==0)level().addParticle(ParticleTypes.POOF,getX(),getY()+.05,getZ(),0,.02,0);}
    speed=grinding?speed*.999F:Math.max(0,speed*.988F-.0012F);
    if(isInWater())speed*=.6F;
    if(jump&&!jumpWas&&rider!=null){   // ollie (+ the trick picked by the keys held)
     setDeltaMovement(getDeltaMovement().x,.46+speed*.16,getDeltaMovement().z);air=0;
     trick=pick(fwd,left);sound(SoundEvents.WOOD_HIT,.8F,1.5F);
     if(fwd<0)speed=Math.max(speed,.12F);
    }
   }else if(jump&&!jumpWas&&rider!=null&&trick==5&&air<8){trick=pick(fwd,left);if(trick==0)trick=5;else sound(SoundEvents.WOOD_HIT,.7F,1.7F);}   // flip trick in the air off a ramp
   jumpWas=jump;
   manual=onGround()&&rider!=null&&sprint&&fwd<=0&&speed>.12F&&!grinding;
   if(manual){manualTicks++;if(manualTicks%10==0&&rider instanceof Player p&&level().isClientSide)p.displayClientMessage(Component.literal("§b🛹 Manual §7"+manualTicks/20F+" s"),true);}
   else if(manualTicks>0){if(manualTicks>=20)score(rider,"Manual",manualTicks*3);manualTicks=0;}
   float turn=left*(onGround()?(3F+Math.min(speed,.5F)*4F):1.5F);if(grinding)turn*=.2F;
   if(rider!=null&&!((trick==1||trick==2||trick==4)&&!onGround()))setYRot(getYRot()-turn);   // flip tricks: A/D flip the board instead of turning it
   double r=Math.toRadians(getYRot());
   double vy=getDeltaMovement().y-(isNoGravity()?0:.08);vy*=.98;
   setDeltaMovement(-Math.sin(r)*speed,vy,Math.cos(r)*speed);
   boolean wasAir=!onGround();
   move(MoverType.SELF,getDeltaMovement());
   if(horizontalCollision){if(speed>.3F){sound(SoundEvents.WOOD_BREAK,.7F,1.2F);comboPts=0;comboN=0;}speed=0;}
   if(lastRamp&&!onGround()&&getDeltaMovement().y>-.1){   // off the lip of a ramp: launched up
    setDeltaMovement(getDeltaMovement().x,Math.max(getDeltaMovement().y,.22+speed*.85),getDeltaMovement().z);if(trick<0){trick=5;air=0;}sound(SoundEvents.WOOD_HIT,.6F,1.2F);}
   lastRamp=onRamp;
   if(onGround()){
    if(getDeltaMovement().y<0)setDeltaMovement(getDeltaMovement().x,0,getDeltaMovement().z);
    double dy=yOld-getY();if(dy>0)speed=Math.min(MAX,speed+(float)dy*.22F);       // downhill
    else if(dy<0&&!grinding)speed=Math.max(0,speed+(float)dy*.12F);              // uphill
    if(wasAir&&air>4&&trick>=0)land(rider);else if(wasAir&&air>8)sound(SoundEvents.WOOD_PLACE,.6F,1.2F);
    air=0;trick=-1;
    if(!grinding&&!manual&&comboN>0&&++comboIdle>30)endCombo(rider);
   }else air++;
   if(grinding){grindTicks++;comboIdle=0;}else if(grindTicks>0){if(grindTicks>=6)score(rider,slide?"Boardslide":"50-50 Grind",grindTicks*6);grindTicks=0;}
   if(manual)comboIdle=0;
   // animation: flags for everybody (sent to the server when they change), local trick animation
   if(level().isClientSide&&rider!=null){
    boolean inAir=!onGround()&&trick>=0;
    localFlags=(onGround()&&fwd>0&&!manual?PUSH:0)|(onGround()&&fwd<0&&speed>.05F&&!manual?BRAKE:0)|(grinding?GRIND:0)|(inAir?AIR:0)|(inAir?(trick+1)<<TRICK_SHIFT:0)|(left>0?LEFT:0)|(left<0?RIGHT:0)|(manual?MANUAL:0)|(grinding&&slide?SLIDE:0);
    if(localFlags!=sentFlags){sentFlags=localFlags;net.neoforged.neoforge.network.PacketDistributor.sendToServer(new Anim(getId(),localFlags));}
    animAir=inAir?air:0;animate(localFlags);
   }
   if(grinding){
    if(!wasGrinding){sound(SoundEvents.CHAIN_PLACE,.8F,1.6F);if(rider instanceof Player p&&level().isClientSide)p.displayClientMessage(Component.literal("§e🛹 "+(slide?"Boardslide!":"Grind!")),true);}
    if(level().isClientSide&&tickCount%2==0)level().addParticle(ParticleTypes.ELECTRIC_SPARK,getX(),getY(),getZ(),(random.nextFloat()-.5F)*.2F,.1F,(random.nextFloat()-.5F)*.2F);
    if(tickCount%6==0)sound(SoundEvents.CHAIN_STEP,.5F,1.8F);
   }else if(onGround()&&speed>.2F&&level().isClientSide&&tickCount%5==0)sound(SoundEvents.SAND_STEP,.18F,1.9F);
  }
  static int pick(float fwd,float left){return left!=0&&fwd<0?4:left>0?1:left<0?2:fwd<0?3:0;}
  void animate(int f){
   int tr=(f>>TRICK_SHIFT&7)-1;
   if(animAir>0&&tr>=0){float t=Math.min(1F,animAir/10F);
    flip=tr==1||tr==4?t*360F:tr==2?-t*360F:0;shove=tr==3?t*180F:tr==4?t*360F:0;nose=animAir<4?animAir*6F:Math.max(0,24-animAir*3F);}
   else{flip=flipO=0;shove=(f&SLIDE)!=0?90F:0;if((f&SLIDE)==0)shoveO=0;nose=(f&MANUAL)!=0?13F:0;if((f&MANUAL)==0)noseO=0;}
   float want=(f&LEFT)!=0?-9F:(f&RIGHT)!=0?9F:0;lean+=(want-lean)*.35F;   // the board leans into the turn
  }
  void land(LivingEntity rider){
   sound(SoundEvents.WOOD_PLACE,.9F,1.2F);
   if(level().isClientSide)for(int i=0;i<6;i++)level().addParticle(ParticleTypes.POOF,getX(),getY()+.05,getZ(),(random.nextFloat()-.5F)*.1F,.02F,(random.nextFloat()-.5F)*.1F);
   String name=trick==0&&air>16?"Ollie Gigante":TRICKS[Mth.clamp(trick,0,5)];
   score(rider,name,TRICK_PTS[Mth.clamp(trick,0,5)]+air*10+(int)(speed*200));
  }
  /** combo: tricks, grinds and manuals chained without stopping add up; the combo ends 1.5 s after you stop doing things */
  void score(LivingEntity rider,String name,int pts){
   if(!(rider instanceof Player p)||!level().isClientSide)return;
   comboN++;comboPts+=pts;comboIdle=0;
   p.displayClientMessage(Component.literal("§b🛹 "+name+"! §7+"+pts+(comboN>1?"  §e§lCOMBO x"+comboN+" §r§e"+comboPts*comboN:"")),true);
  }
  void endCombo(LivingEntity rider){
   if(rider instanceof Player p&&level().isClientSide&&comboN>1){p.displayClientMessage(Component.literal("§6§l🛹 COMBO x"+comboN+": "+comboPts*comboN+" pontos!"),true);sound(SoundEvents.PLAYER_LEVELUP,.4F,1.6F);}
   comboN=0;comboPts=0;comboIdle=0;
  }
  void sound(net.minecraft.sounds.SoundEvent s,float vol,float pitch){
   if(level().isClientSide)level().playLocalSound(getX(),getY(),getZ(),s,SoundSource.PLAYERS,vol,pitch,false);
   else level().playSound(null,getX(),getY(),getZ(),s,SoundSource.PLAYERS,vol,pitch);}
  @Override protected void addAdditionalSaveData(CompoundTag t){}
  @Override protected void readAdditionalSaveData(CompoundTag t){}
 }
}
