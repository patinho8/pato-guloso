package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.65.2 entertainment: an arcade cabinet (Snake, with a high score saved in the machine), a popcorn machine and a fireworks show launcher. */
public final class Fun {
 private Fun(){}
 static BlockBehaviour.Properties props(MapColor c){return BlockBehaviour.Properties.of().mapColor(c).strength(2F,6F).noOcclusion().sound(SoundType.METAL);}
 // ---------------- registration ----------------
 public static final DeferredBlock<ArcadeBlock> ARCADE=MareEletrica.BLOCKS.register("arcade",()->new ArcadeBlock(props(MapColor.COLOR_BLUE).lightLevel(s->7)));
 public static final DeferredBlock<PopcornBlock> POPCORN_MACHINE=MareEletrica.BLOCKS.register("popcorn_machine",()->new PopcornBlock(props(MapColor.COLOR_RED).lightLevel(s->5)));
 public static final DeferredBlock<ShowBlock> FIREWORKS=MareEletrica.BLOCKS.register("fireworks_show",()->new ShowBlock(props(MapColor.COLOR_GRAY)));
 public static final DeferredItem<BlockItem> ARCADE_ITEM=item(ARCADE,"§7Fliperama: Cobrinha, Pong para 2 jogadores (ou contra o computador) e Pato Guloso (labirinto). Recordes salvos na máquina.");
 public static final DeferredItem<BlockItem> POPCORN_MACHINE_ITEM=item(POPCORN_MACHINE,"§7Botão direito com trigo: estoura pipoca (sai 3 por trigo).");
 public static final DeferredItem<BlockItem> FIREWORKS_ITEM=item(FIREWORKS,"§7Botão direito ou sinal de redstone: show de fogos de 20 segundos com final.");
 public static final DeferredItem<Item> POPCORN=MareEletrica.ITEMS.register("popcorn",()->new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(.3F).fast().build())){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Crocante. Melhor ainda no fliperama."));}});
 public static final Supplier<BlockEntityType<ArcadeEntity>> ARCADE_ENTITY=MareEletrica.ENTITIES.register("arcade",()->BlockEntityType.Builder.of(ArcadeEntity::new,ARCADE.get()).build(null));
 public static final Supplier<BlockEntityType<PopcornEntity>> POPCORN_ENTITY=MareEletrica.ENTITIES.register("popcorn_machine",()->BlockEntityType.Builder.of(PopcornEntity::new,POPCORN_MACHINE.get()).build(null));
 public static final Supplier<BlockEntityType<ShowEntity>> SHOW_ENTITY=MareEletrica.ENTITIES.register("fireworks_show",()->BlockEntityType.Builder.of(ShowEntity::new,FIREWORKS.get()).build(null));
 private static DeferredItem<BlockItem> item(DeferredBlock<? extends Block> b,String tip){return MareEletrica.ITEMS.register(b.getId().getPath(),()->new BlockItem(b.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal(tip));}});}
 public static void register(IEventBus bus){bus.addListener(Fun::packets);}
 public static void creative(Consumer<ItemStack> out){out.accept(new ItemStack(ARCADE_ITEM.get()));out.accept(new ItemStack(POPCORN_MACHINE_ITEM.get()));out.accept(new ItemStack(POPCORN.get()));out.accept(new ItemStack(FIREWORKS_ITEM.get()));}

 // ---------------- shared: a block that faces the player ----------------
 public abstract static class Facing extends BaseEntityBlock {
  static final VoxelShape SHAPE=Block.box(1,0,1,15,16,15);
  protected Facing(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HorizontalDirectionalBlock.FACING);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,c.getHorizontalDirection().getOpposite());}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
 }
 static abstract class Synced extends BlockEntity {
  Synced(BlockEntityType<?> t,BlockPos p,BlockState s){super(t,p,s);}
  void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
  @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }

 // ---------------- arcade ----------------
 /** Opened on the client (the game itself runs there); set by the client side. */
 public static Consumer<BlockPos> openArcade=p->{};
 /** 0.66.0: the cabinet is 2 blocks tall (lower = cabinet + controls with the game data, upper = screen + marquee). */
 public static final class ArcadeBlock extends Facing {
  public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.world.level.block.state.properties.DoubleBlockHalf> HALF=net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;
  static final net.minecraft.world.level.block.state.properties.DoubleBlockHalf LOWER=net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER,UPPER=net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER;
  public ArcadeBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(HALF,LOWER));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(HALF);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){
   var pos=c.getClickedPos();if(pos.getY()>=c.getLevel().getMaxBuildHeight()-1||!c.getLevel().getBlockState(pos.above()).canBeReplaced(c))return null;
   return super.getStateForPlacement(c).setValue(HALF,LOWER);}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,ItemStack st){l.setBlock(p.above(),s.setValue(HALF,UPPER),3);}
  @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,net.minecraft.world.level.LevelAccessor l,BlockPos p,BlockPos np){
   boolean lower=s.getValue(HALF)==LOWER;
   if(d==(lower?Direction.UP:Direction.DOWN)&&!(n.is(this)&&n.getValue(HALF)!=s.getValue(HALF)))return lower&&!n.isAir()?s:net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
   return s;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(HALF)==LOWER?new ArcadeEntity(p,s):null;}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||s.getValue(HALF)!=LOWER?null:createTickerHelper(t,ARCADE_ENTITY.get(),(lv,ps,st,e)->e.pong.tick((ServerLevel)lv,ps));}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){
   if(s.getValue(HALF)==UPPER)pos=pos.below();
   if(l.isClientSide)openArcade.accept(pos);else l.playSound(null,pos,SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.BLOCKS,.8F,1.4F);
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }
 public static final class ArcadeEntity extends Synced {
  public int best,mazeBest;public String bestName="",mazeBestName="";public final Pong pong=new Pong();
  public ArcadeEntity(BlockPos p,BlockState s){super(ARCADE_ENTITY.get(),p,s);}
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("Best",best);t.putString("BestName",bestName);t.putInt("MazeBest",mazeBest);t.putString("MazeBestName",mazeBestName);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);best=t.getInt("Best");bestName=t.getString("BestName");mazeBest=t.getInt("MazeBest");mazeBestName=t.getString("MazeBestName");}
 }
 /** Client → server: a finished game's score. */
 public record Score(BlockPos pos,int score,int game) implements CustomPacketPayload {
  public Score(BlockPos pos,int score){this(pos,score,0);}
  public static final Type<Score> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"arcade_score"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Score> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Score::pos,ByteBufCodecs.VAR_INT,Score::score,ByteBufCodecs.VAR_INT,Score::game,Score::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 // ---------------- Pong for 2 (the match runs on the server, both players see the same game) ----------------
 /** Client → server. kind: 0 join, 1 paddle input (dir -1/0/1), 2 leave, 3 play against the computer. */
 public record PongCmd(BlockPos pos,int kind,int dir) implements CustomPacketPayload {
  public static final Type<PongCmd> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"pong_cmd"));
  public static final StreamCodec<RegistryFriendlyByteBuf,PongCmd> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,PongCmd::pos,ByteBufCodecs.VAR_INT,PongCmd::kind,ByteBufCodecs.INT,PongCmd::dir,PongCmd::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 /** Server → players at the machine: the whole match state, every tick. side: which paddle the receiver controls (0 left, 1 right, -1 watching). */
 public record PongState(BlockPos pos,int phase,float bx,float by,float ly,float ry,int ls,int rs,int timer,String left,String right,int side) implements CustomPacketPayload {
  public static final Type<PongState> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"pong_state"));
  public static final StreamCodec<RegistryFriendlyByteBuf,PongState> CODEC=StreamCodec.of((b,p)->{b.writeBlockPos(p.pos);b.writeByte(p.phase);b.writeFloat(p.bx);b.writeFloat(p.by);b.writeFloat(p.ly);b.writeFloat(p.ry);b.writeByte(p.ls);b.writeByte(p.rs);b.writeVarInt(p.timer);b.writeUtf(p.left);b.writeUtf(p.right);b.writeByte(p.side);},
   b->new PongState(b.readBlockPos(),b.readByte(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readByte(),b.readByte(),b.readVarInt(),b.readUtf(),b.readUtf(),b.readByte()));
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static Consumer<PongState> pongView=s->{};
 public static final class Pong {
  public static final float W=200,H=120,PH=28,BS=4,WIN=7;
  java.util.UUID left,right;boolean cpu;int phase,timer,ls,rs,inL,inR;float bx=W/2,by=H/2,vx,vy,ly=H/2,ry=H/2;String ln="",rn="";
  final java.util.Set<java.util.UUID> watchers=new java.util.HashSet<>();
  void cmd(ServerPlayer p,int kind,int dir){
   var id=p.getUUID();   switch(kind){
    case 0->{watchers.add(id);
     if(left==null||left.equals(id)){left=id;ln=p.getName().getString();}
     else if(right==null&&!cpu||right!=null&&right.equals(id)){right=id;rn=p.getName().getString();cpu=false;if(phase==0)serve(true);}
    }
    case 1->{dir=Integer.signum(dir);if(id.equals(left))inL=dir;else if(id.equals(right))inR=dir;}
    case 2->{watchers.remove(id);
     if(id.equals(left)||id.equals(right)){boolean wasLeft=id.equals(left);if(wasLeft){left=null;ln="";}else{right=null;rn="";}
      if(phase>0){phase=3;timer=60;if(wasLeft)rs=(int)WIN;else ls=(int)WIN;}
      if(left==null&&right!=null){left=right;ln=rn;right=null;rn="";}}
    }
    case 3->{if(id.equals(left)&&right==null){cpu=true;rn="Computador";serve(true);}}
   }
  }
  void serve(boolean fresh){if(fresh){ls=0;rs=0;}phase=1;timer=50;bx=W/2;by=H/2;vx=0;vy=0;}
  void tick(ServerLevel l,BlockPos pos){
   if(watchers.isEmpty()&&left==null)return;
   // drop people who walked away or left the game
   watchers.removeIf(id->!(l.getPlayerByUUID(id) instanceof ServerPlayer p)||p.distanceToSqr(pos.getCenter())>10*10);
   if(left!=null&&!watchers.contains(left))cmd0(l,left);if(right!=null&&!watchers.contains(right))cmd0(l,right);
   if(phase==1&&--timer<=0){phase=2;var r=l.random;float a=(r.nextFloat()-.5F)*.9F;float sp=2.3F;vx=(r.nextBoolean()?1:-1)*sp*Mth.cos(a);vy=sp*Mth.sin(a);}
   ly=Mth.clamp(ly+inL*3.6F,PH/2,H-PH/2);
   if(cpu){float target=vx>0?by:H/2;ry=Mth.clamp(ry+Mth.clamp(target-ry,-2.5F,2.5F),PH/2,H-PH/2);}else ry=Mth.clamp(ry+inR*3.6F,PH/2,H-PH/2);
   if(phase==2){
    bx+=vx;by+=vy;
    if(by<BS/2){by=BS/2;vy=-vy;beep(l,pos,1.6F);}if(by>H-BS/2){by=H-BS/2;vy=-vy;beep(l,pos,1.6F);}
    if(vx<0&&bx<=12&&bx>=4&&Math.abs(by-ly)<=PH/2+BS/2)bounce(l,pos,ly,true);
    if(vx>0&&bx>=W-12&&bx<=W-4&&Math.abs(by-ry)<=PH/2+BS/2)bounce(l,pos,ry,false);
    if(bx<-6||bx>W+6){if(bx<0)rs++;else ls++;l.playSound(null,pos,SoundEvents.NOTE_BLOCK_BASS.value(),SoundSource.BLOCKS,.8F,.6F);
     if(ls>=WIN||rs>=WIN){phase=3;timer=100;l.playSound(null,pos,SoundEvents.PLAYER_LEVELUP,SoundSource.BLOCKS,1F,1.2F);
      String w=ls>=WIN?ln:rn;for(var id:watchers)if(l.getPlayerByUUID(id) instanceof ServerPlayer p)p.displayClientMessage(Component.literal("§6§l🏓 "+w+" venceu o Pong! §r§7("+ls+" x "+rs+")"),false);}
     else serve(false);}
   }
   if(phase==3&&--timer<=0){phase=0;ls=0;rs=0;if(cpu){cpu=false;rn="";}if(left!=null&&right!=null)serve(true);}
   for(var id:watchers)if(l.getPlayerByUUID(id) instanceof ServerPlayer p)
    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p,new PongState(pos,phase,bx,by,ly,ry,ls,rs,timer,ln,rn,id.equals(left)?0:id.equals(right)?1:-1));
  }
  void cmd0(ServerLevel l,java.util.UUID id){if(l.getPlayerByUUID(id) instanceof ServerPlayer p)cmd(p,2,0);else{if(id.equals(left)){left=null;ln="";}if(id.equals(right)){right=null;rn="";}phase=0;}}
  void bounce(ServerLevel l,BlockPos pos,float py,boolean leftSide){
   float off=Mth.clamp((by-py)/(PH/2),-1,1);float sp=Math.min(5.2F,(float)Math.sqrt(vx*vx+vy*vy)*1.07F);float a=off*1.0F;
   vx=(leftSide?1:-1)*sp*Mth.cos(a);vy=sp*Mth.sin(a);bx=leftSide?12:W-12;beep(l,pos,1.1F);
  }
  static void beep(ServerLevel l,BlockPos pos,float pitch){l.playSound(null,pos,SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.BLOCKS,.5F,pitch);}
 }
 static void packets(RegisterPayloadHandlersEvent e){
  var reg=e.registrar("1");
  reg.playToServer(PongCmd.TYPE,PongCmd.CODEC,(p,c)->c.enqueueWork(()->{   if(c.player() instanceof ServerPlayer sp&&sp.distanceToSqr(p.pos().getCenter())<10*10&&sp.level().getBlockEntity(p.pos()) instanceof ArcadeEntity a)a.pong.cmd(sp,p.kind(),p.dir());}));
  reg.playToClient(PongState.TYPE,PongState.CODEC,(p,c)->pongView.accept(p));
  reg.playToServer(Score.TYPE,Score.CODEC,(p,c)->c.enqueueWork(()->{
   if(!(c.player() instanceof ServerPlayer sp)||sp.distanceToSqr(p.pos().getCenter())>64||p.score()<0||p.score()>(p.game()==1?2_000_000:2000))return;
   if(sp.level().getBlockEntity(p.pos()) instanceof ArcadeEntity a&&p.score()>(p.game()==1?a.mazeBest:a.best)){
    if(p.game()==1){a.mazeBest=p.score();a.mazeBestName=sp.getName().getString();}else{a.best=p.score();a.bestName=sp.getName().getString();}a.sync();
    sp.level().playSound(null,p.pos(),SoundEvents.PLAYER_LEVELUP,SoundSource.BLOCKS,1F,1.2F);
    sp.displayClientMessage(Component.literal("§6§lNOVO RECORDE no fliperama: "+p.score()+"!"),false);
   }
  }));
 }

 // ---------------- popcorn machine ----------------
 public static final class PopcornBlock extends Facing {
  public PopcornBlock(Properties p){super(p);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new PopcornEntity(p,s);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,POPCORN_ENTITY.get(),PopcornEntity::tick);}
  @Override protected ItemInteractionResult useItemOn(ItemStack st,BlockState s,Level l,BlockPos pos,Player p,InteractionHand h,BlockHitResult hit){
   if(!st.is(Items.WHEAT))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   if(!l.isClientSide&&l.getBlockEntity(pos) instanceof PopcornEntity e){
    if(e.cooking>0){p.displayClientMessage(Component.literal("§7Já está estourando..."),true);return ItemInteractionResult.CONSUME;}
    e.cooking=100;if(!p.isCreative())st.shrink(1);e.sync();
    l.playSound(null,pos,SoundEvents.FIRECHARGE_USE,SoundSource.BLOCKS,.5F,1.6F);
   }
   return ItemInteractionResult.sidedSuccess(l.isClientSide);
  }
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){
   if(!l.isClientSide)p.displayClientMessage(Component.literal("§7Coloque §ftrigo§7 na máquina de pipoca."),true);return InteractionResult.sidedSuccess(l.isClientSide);}
 }
 public static final class PopcornEntity extends Synced {
  public int cooking;
  public PopcornEntity(BlockPos p,BlockState s){super(POPCORN_ENTITY.get(),p,s);}
  static void tick(Level l,BlockPos pos,BlockState s,PopcornEntity e){
   if(e.cooking<=0)return;e.cooking--;var sl=(ServerLevel)l;var r=l.random;
   double x=pos.getX()+.5,y=pos.getY()+.75,z=pos.getZ()+.5;
   if(e.cooking<75&&r.nextInt(e.cooking<30?2:5)==0){   // pop!
    sl.sendParticles(ParticleTypes.WHITE_ASH,x,y,z,6,.15,.12,.15,.05);sl.sendParticles(ParticleTypes.CLOUD,x,y,z,1,.1,.1,.1,0);
    l.playSound(null,pos,SoundEvents.CHICKEN_EGG,SoundSource.BLOCKS,.35F,1.8F+r.nextFloat()*.4F);
   }
   if(e.cooking==0){
    var out=new net.minecraft.world.entity.item.ItemEntity(l,x,pos.getY()+1.1,z,new ItemStack(POPCORN.get(),3));out.setDeltaMovement(r.nextGaussian()*.05,.25,r.nextGaussian()*.05);l.addFreshEntity(out);
    l.playSound(null,pos,SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,.8F,1.6F);e.sync();
   }
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("Cooking",cooking);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);cooking=t.getInt("Cooking");}
 }

 // ---------------- fireworks show ----------------
 public static final class ShowBlock extends Facing {
  public ShowBlock(Properties p){super(p);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ShowEntity(p,s);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,SHOW_ENTITY.get(),ShowEntity::tick);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){
   if(!l.isClientSide&&l.getBlockEntity(pos) instanceof ShowEntity e)p.displayClientMessage(Component.literal(e.start()?"§6§lQue comece o show!":"§7O show já está rolando."),true);
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  @Override protected void neighborChanged(BlockState s,Level l,BlockPos pos,Block b,BlockPos from,boolean moving){
   boolean on=l.hasNeighborSignal(pos);
   if(!l.isClientSide&&l.getBlockEntity(pos) instanceof ShowEntity e){if(on&&!e.powered)e.start();e.powered=on;}
  }
 }
 public static final class ShowEntity extends Synced {
  int left,next;boolean powered;
  public ShowEntity(BlockPos p,BlockState s){super(SHOW_ENTITY.get(),p,s);}
  boolean start(){if(left>0)return false;left=400;next=0;setChanged();return true;}
  static void tick(Level l,BlockPos pos,BlockState s,ShowEntity e){
   if(e.left<=0)return;e.left--;
   boolean finale=e.left<50;
   if(--e.next>0)return;
   var r=l.random;int n=finale?3+r.nextInt(3):1+(r.nextInt(4)==0?1:0);
   for(int i=0;i<n;i++)launch(l,pos,r,finale);
   e.next=finale?4:6+r.nextInt(12);
   if(e.left==0)l.playSound(null,pos,SoundEvents.PLAYER_LEVELUP,SoundSource.BLOCKS,1F,.8F);
  }
  static final int[] PALETTE={0xFF3030,0xFF9A1F,0xFFE040,0x40FF60,0x30C0FF,0x6060FF,0xD050FF,0xFFFFFF,0xFF60B0};
  static void launch(Level l,BlockPos pos,RandomSource r,boolean finale){
   var shapes=FireworkExplosion.Shape.values();
   var list=new java.util.ArrayList<FireworkExplosion>();
   int bursts=1+(finale||r.nextInt(3)==0?1:0);
   for(int b=0;b<bursts;b++){
    var colors=new it.unimi.dsi.fastutil.ints.IntArrayList();int k=1+r.nextInt(3);for(int i=0;i<k;i++)colors.add(PALETTE[r.nextInt(PALETTE.length)]);
    var fade=new it.unimi.dsi.fastutil.ints.IntArrayList();if(r.nextBoolean())fade.add(PALETTE[r.nextInt(PALETTE.length)]);
    list.add(new FireworkExplosion(finale&&b==0?FireworkExplosion.Shape.LARGE_BALL:shapes[r.nextInt(shapes.length)],colors,fade,r.nextBoolean(),r.nextBoolean()));
   }
   var rocket=new ItemStack(Items.FIREWORK_ROCKET);rocket.set(DataComponents.FIREWORKS,new Fireworks(1+r.nextInt(2)+(finale?1:0),list));
   double x=pos.getX()+.5+r.nextGaussian()*.4,z=pos.getZ()+.5+r.nextGaussian()*.4;
   var fw=new FireworkRocketEntity(l,x,pos.getY()+1.1,z,rocket);
   fw.setDeltaMovement(r.nextGaussian()*.06,.05,r.nextGaussian()*.06);
   l.addFreshEntity(fw);
  }
 }
}
