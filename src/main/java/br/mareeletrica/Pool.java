package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.65.5: pool (sinuca, 8-ball). The table is 2 × 4 blocks; the main block (corner where it was placed) runs the physics.
 *  Hold right-click with the cue to charge, let go to shoot the white ball in the direction you are looking (an aim line shows
 *  where it goes). Up to 2 players: the first to shoot is player 1, the next one to shoot joins as player 2.
 *  Rules: the first ball potted decides the groups (0.66.0: red 1–7 / blue 9–15, the 8 is black); potting one of yours = shoot again;
 *  white in the pocket or hitting nothing = foul (turn passes); the 8 wins only after all of yours are in, before that it loses.
 *  Crouch + right-click the table with an empty hand to rack the balls again. */
public final class Pool {
 private Pool(){}
 public static final IntegerProperty KIND=IntegerProperty.create("kind",0,2),ROT=IntegerProperty.create("rot",0,3);
 public static final BooleanProperty MAIN=BooleanProperty.create("main");
 public static final DeferredBlock<TableBlock> TABLE=MareEletrica.BLOCKS.register("pool_table",()->new TableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(2F,4F).noOcclusion().sound(SoundType.WOOD)));
 public static final DeferredItem<Item> TABLE_ITEM=MareEletrica.ITEMS.register("pool_table",()->new TableItem(new Item.Properties().stacksTo(16)));
 public static final DeferredItem<Item> CUE=MareEletrica.ITEMS.register("pool_cue",()->new CueItem(new Item.Properties().stacksTo(1)));
 public static final Supplier<BlockEntityType<PoolEntity>> ENTITY=MareEletrica.ENTITIES.register("pool_table",()->BlockEntityType.Builder.of(PoolEntity::new,TABLE.get()).build(null));
 public static void register(IEventBus bus){bus.addListener(Pool::packets);}
 public static void creative(Consumer<ItemStack> out){out.accept(new ItemStack(TABLE_ITEM.get()));out.accept(new ItemStack(CUE.get()));}

 // ---------------- table geometry (local units = blocks; u across 0..2, v along 0..4) ----------------
 public static final float TW=2,TL=4,RAIL=.125F,R=.07F,FELT=13/16F;
 static final float[][] POCKETS={{RAIL,RAIL,.115F},{TW-RAIL,RAIL,.115F},{RAIL,TL-RAIL,.115F},{TW-RAIL,TL-RAIL,.115F},{RAIL,TL/2,.11F},{TW-RAIL,TL/2,.11F}};
 /** piece kind / rotation for each [column][row] of the table, in the table's own frame */
 static final int[][][] PIECE={{{0,0},{1,0},{2,0},{0,3}},{{0,1},{2,2},{1,2},{0,2}}};
 public static Direction right(Direction f){return f.getCounterClockWise();}
 static BlockPos at(BlockPos origin,Direction f,int c,int r){return origin.relative(right(f),c).relative(f,r);}
 /** world position of a table-local point (y = felt height) */
 public static Vec3 world(BlockPos origin,Direction f,double u,double v){
  var rt=right(f);double ox=origin.getX()+.5-.5*rt.getStepX()-.5*f.getStepX(),oz=origin.getZ()+.5-.5*rt.getStepZ()-.5*f.getStepZ();
  return new Vec3(ox+rt.getStepX()*u+f.getStepX()*v,origin.getY()+FELT,oz+rt.getStepZ()*u+f.getStepZ()*v);}
 static PoolEntity findMain(BlockGetter l,BlockPos pos){
  for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){var q=pos.offset(dx,0,dz);var s=l.getBlockState(q);
   if(s.getBlock() instanceof TableBlock&&s.getValue(MAIN)&&l.getBlockEntity(q) instanceof PoolEntity e&&e.contains(pos))return e;}
  return null;}

 // ---------------- block ----------------
 public static final class TableBlock extends BaseEntityBlock {
  static final VoxelShape SHAPE=Block.box(0,0,0,16,15,16);
  static boolean removing;
  public TableBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,0).setValue(ROT,0).setValue(MAIN,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND,ROT,MAIN);}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(MAIN)?new PoolEntity(p,s):null;}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||!s.getValue(MAIN)?null:createTickerHelper(t,ENTITY.get(),(lv,ps,st,e)->e.tick());}
  @Override public ItemStack getCloneItemStack(LevelReader l,BlockPos p,BlockState s){return new ItemStack(TABLE_ITEM.get());}
  @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult h){
   return stack.is(CUE.get())?ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION:ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){
   if(!l.isClientSide&&findMain(l,pos) instanceof PoolEntity e){
    if(p.isShiftKeyDown()){e.newGame();e.say("§a🎱 Mesa arrumada! Pegue um taco e dê a primeira tacada.",false);}
    else p.displayClientMessage(Component.literal(e.status()),true);
   }
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  @Override public BlockState playerWillDestroy(Level l,BlockPos pos,BlockState s,Player p){
   if(!l.isClientSide&&!p.isCreative())popResource(l,pos,new ItemStack(TABLE_ITEM.get()));
   return super.playerWillDestroy(l,pos,s,p);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos pos,BlockState ns,boolean moved){
   if(!removing&&!s.is(ns.getBlock())&&!l.isClientSide){
    var e=findMain(l,pos);
    if(e!=null){removing=true;try{for(int c=0;c<2;c++)for(int r=0;r<4;r++){var q=at(e.getBlockPos(),e.facing,c,r);if(!q.equals(pos)&&l.getBlockState(q).is(this))l.setBlock(q,Blocks.AIR.defaultBlockState(),3);}}finally{removing=false;}}
   }
   super.onRemove(s,l,pos,ns,moved);
  }
 }

 // ---------------- table item: places the 8 pieces ----------------
 public static final class TableItem extends Item {
  public TableItem(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();var f=c.getHorizontalDirection();BlockPos o=c.getClickedPos();
   if(!l.getBlockState(o).canBeReplaced())o=o.relative(c.getClickedFace());
   for(int col=0;col<2;col++)for(int r=0;r<4;r++){var q=at(o,f,col,r);
    if(!l.getBlockState(q).canBeReplaced()||!l.isUnobstructed(TABLE.get().defaultBlockState(),q,CollisionContext.empty())){
     if(c.getPlayer()!=null&&!l.isClientSide)c.getPlayer().displayClientMessage(Component.literal("§cA mesa precisa de um espaço livre de 2 × 4 blocos (à frente e à esquerda)."),true);
     return InteractionResult.FAIL;}}
   if(l.isClientSide)return InteractionResult.SUCCESS;
   for(int col=0;col<2;col++)for(int r=0;r<4;r++){var pc=PIECE[col][r];
    l.setBlock(at(o,f,col,r),TABLE.get().defaultBlockState().setValue(KIND,pc[0]).setValue(ROT,(pc[1]+f.get2DDataValue())%4).setValue(MAIN,col==0&&r==0),3);}
   if(l.getBlockEntity(o) instanceof PoolEntity e){e.facing=f;e.newGame();}
   l.playSound(null,o,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,1F,.8F);
   if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   return InteractionResult.CONSUME;
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Mesa de 2 × 4 blocos (à frente e à sua esquerda). Jogue com o taco de sinuca."));
   t.add(Component.literal("§8Shift + botão direito com a mão vazia: arruma as bolas de novo."));}
 }

 // ---------------- cue ----------------
 public static final int FULL_CHARGE=30;
 public static final class CueItem extends Item {
  public CueItem(Properties p){super(p);}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
  @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.NONE;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
  @Override public void releaseUsing(ItemStack s,Level l,LivingEntity e,int left){
   if(l.isClientSide||!(e instanceof ServerPlayer p))return;
   float power=Mth.clamp((72000-left)/(float)FULL_CHARGE,.05F,1F);
   var t=nearest(l,p.position());
   if(t==null){p.displayClientMessage(Component.literal("§7Chegue perto de uma mesa de sinuca."),true);return;}
   var look=p.getLookAngle();double lx=look.x,lz=look.z,n=Math.sqrt(lx*lx+lz*lz);if(n<1e-3)return;lx/=n;lz/=n;
   var rt=right(t.facing);t.shoot(p,lx*rt.getStepX()+lz*rt.getStepZ(),lx*t.facing.getStepX()+lz*t.facing.getStepZ(),power);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Segure o botão direito perto da mesa para carregar e solte para tacar a bola branca."));
   t.add(Component.literal("§7A tacada vai para onde você olha: a linha branca mostra o caminho."));}
 }
 /** nearest table whose centre is within 4.5 blocks */
 public static PoolEntity nearest(Level l,Vec3 p){
  PoolEntity best=null;double bd=4.5*4.5;var c=BlockPos.containing(p);
  for(var q:BlockPos.betweenClosed(c.offset(-5,-2,-5),c.offset(5,2,5))){var s=l.getBlockState(q);
   if(s.getBlock() instanceof TableBlock&&s.getValue(MAIN)&&l.getBlockEntity(q) instanceof PoolEntity e){double d=e.center().distanceToSqr(p);if(d<bd){bd=d;best=e;}}}
  return best;}

 // ---------------- the game ----------------
 public static final class PoolEntity extends BlockEntity {
  public Direction facing=Direction.SOUTH;
  public final float[] x=new float[16],z=new float[16],vx=new float[16],vz=new float[16],ox=new float[16],oz=new float[16];
  public final boolean[] in=new boolean[16];
  public final org.joml.Quaternionf[] roll=new org.joml.Quaternionf[16];
  public final UUID[] pl=new UUID[2];public final String[] name={"",""};public final int[] group={0,0};
  public int turn,phase;public String winner="";public boolean moving;
  int shooter,firstHit=-1,clicks;final List<Integer> potted=new ArrayList<>();
  public PoolEntity(BlockPos p,BlockState s){super(ENTITY.get(),p,s);for(int i=0;i<16;i++)roll[i]=new org.joml.Quaternionf();rack();}
  public Vec3 center(){return world(worldPosition,facing,TW/2,TL/2);}
  boolean contains(BlockPos p){for(int c=0;c<2;c++)for(int r=0;r<4;r++)if(at(worldPosition,facing,c,r).equals(p))return true;return false;}
  public static boolean solid(int b){return b>=1&&b<=7;}
  public static boolean stripe(int b){return b>=9;}
  static int groupOf(int b){return solid(b)?1:stripe(b)?2:0;}
  static String groupName(int g){return g==1?"§cvermelhas§r":g==2?"§9azuis§r":"—";}
  void rack(){
   var rnd=new java.util.Random();var rest=new ArrayList<Integer>();for(int b=2;b<=15;b++)if(b!=8&&b!=9)rest.add(b);java.util.Collections.shuffle(rest,rnd);
   int[] order=new int[15];int k=0;
   for(int row=0;row<5;row++)for(int j=0;j<=row;j++){int slot=k++;
    order[slot]=row==0?1:(row==2&&j==1)?8:(row==4&&j==0)?9:rest.remove(rest.size()-1);}
   if(rnd.nextBoolean()){int a=10,b2=14;int t=order[a];order[a]=order[b2];order[b2]=t;}
   k=0;float dv=R*1.735F*1.01F,du=R*2*1.01F;
   for(int row=0;row<5;row++)for(int j=0;j<=row;j++){int b=order[k++];x[b]=TW/2+(j-row/2F)*du;z[b]=2.85F+row*dv;}
   x[0]=TW/2;z[0]=.95F;
   for(int i=0;i<16;i++){vx[i]=vz[i]=0;in[i]=false;ox[i]=x[i];oz[i]=z[i];}
   moving=false;
  }
  public void newGame(){rack();pl[0]=pl[1]=null;name[0]=name[1]="";group[0]=group[1]=0;turn=0;phase=0;winner="";sync();}
  String status(){
   if(phase==0)return "§a🎱 Sinuca §7— pegue um taco e dê a tacada. Shift + clique (mão vazia) arruma as bolas.";
   if(phase==2)return "§6🎱 "+winner+" §7— Shift + clique (mão vazia) para outra partida.";
   return "§e🎱 Vez de "+name[turn]+" §7("+groupName(group[turn])+")"+(pl[1]==null?" §8• outro jogador entra dando uma tacada":" §8• "+name[0]+" x "+name[1]);}
  void say(String msg,boolean bar){for(var p:level.players())if(p.distanceToSqr(center())<14*14)p.displayClientMessage(Component.literal(msg),bar);}

  void shoot(ServerPlayer p,double du,double dv,float power){
   if(moving){p.displayClientMessage(Component.literal("§7Espere as bolas pararem."),true);return;}
   if(phase==2){p.displayClientMessage(Component.literal("§7Fim de jogo — Shift + botão direito na mesa (mão vazia) para arrumar de novo."),true);return;}
   var id=p.getUUID();int me=id.equals(pl[0])?0:id.equals(pl[1])?1:-1;
   if(me<0){
    if(pl[0]==null){me=0;pl[0]=id;name[0]=p.getName().getString();turn=0;}
    else if(pl[1]==null){me=1;pl[1]=id;name[1]=p.getName().getString();turn=1;say("§b🎱 "+name[1]+" entrou na partida contra "+name[0]+"!",false);}
    else{p.displayClientMessage(Component.literal("§7Mesa ocupada: "+name[0]+" x "+name[1]+"."),true);return;}
   }
   if(me!=turn){p.displayClientMessage(Component.literal("§7Vez de §e"+name[turn]+"§7."),true);return;}
   if(in[0])spotCue();
   phase=1;shooter=me;potted.clear();firstHit=-1;
   double sp=.04+power*.46;vx[0]=(float)(du*sp);vz[0]=(float)(dv*sp);moving=true;
   level.playSound(null,BlockPos.containing(world(worldPosition,facing,x[0],z[0])),SoundEvents.WOOD_HIT,SoundSource.PLAYERS,.6F+power*.4F,1.6F);
   sync();
  }
  void spotCue(){x[0]=TW/2;z[0]=.95F;vx[0]=vz[0]=0;in[0]=false;
   for(int tries=0;tries<40&&overlaps(0);tries++){x[0]=RAIL+R+(float)Math.random()*(TW-2*RAIL-2*R);z[0]=RAIL+R+(float)Math.random()*1.2F;}}
  boolean overlaps(int i){for(int j=0;j<16;j++)if(j!=i&&!in[j]&&Mth.square(x[i]-x[j])+Mth.square(z[i]-z[j])<4*R*R)return true;return false;}

  void tick(){
   if(!moving)return;
   var l=(ServerLevel)level;clicks=0;
   int SUB=8;
   for(int s=0;s<SUB;s++){
    for(int i=0;i<16;i++)if(!in[i]){x[i]+=vx[i]/SUB;z[i]+=vz[i]/SUB;}
    for(int i=0;i<16;i++){if(in[i])continue;for(int j=i+1;j<16;j++){if(in[j])continue;
     float dx=x[j]-x[i],dz=z[j]-z[i],d2=dx*dx+dz*dz;if(d2>=4*R*R||d2<1e-9F)continue;
     float d=(float)Math.sqrt(d2),nx=dx/d,nz=dz/d,ov=2*R-d;x[i]-=nx*ov/2;z[i]-=nz*ov/2;x[j]+=nx*ov/2;z[j]+=nz*ov/2;
     float vn=(vx[i]-vx[j])*nx+(vz[i]-vz[j])*nz;if(vn<=0)continue;
     float imp=vn*.975F;vx[i]-=imp*nx;vz[i]-=imp*nz;vx[j]+=imp*nx;vz[j]+=imp*nz;
     if(firstHit<0&&(i==0||j==0))firstHit=i==0?j:i;
     click(l,(x[i]+x[j])/2,(z[i]+z[j])/2,vn);}}
    for(int i=0;i<16;i++){if(in[i])continue;
     for(var pk:POCKETS)if(Mth.square(x[i]-pk[0])+Mth.square(z[i]-pk[1])<pk[2]*pk[2]){pot(l,i,pk);break;}
     if(in[i])continue;
     float lo=RAIL+R,hiU=TW-RAIL-R,hiV=TL-RAIL-R;
     if(x[i]<lo){x[i]=lo;if(vx[i]<0){click(l,x[i],z[i],-vx[i]*.5F);vx[i]=-vx[i]*.8F;}}else if(x[i]>hiU){x[i]=hiU;if(vx[i]>0){click(l,x[i],z[i],vx[i]*.5F);vx[i]=-vx[i]*.8F;}}
     if(z[i]<lo){z[i]=lo;if(vz[i]<0){click(l,x[i],z[i],-vz[i]*.5F);vz[i]=-vz[i]*.8F;}}else if(z[i]>hiV){z[i]=hiV;if(vz[i]>0){click(l,x[i],z[i],vz[i]*.5F);vz[i]=-vz[i]*.8F;}}
    }
   }
   boolean any=false;
   for(int i=0;i<16;i++){if(in[i])continue;float sp=(float)Math.sqrt(vx[i]*vx[i]+vz[i]*vz[i]),ns=sp*.986F-.0007F;
    if(ns<=0){vx[i]=vz[i]=0;}else{vx[i]*=ns/sp;vz[i]*=ns/sp;any=true;}}
   var c=world(worldPosition,facing,TW/2,TL/2);
   PacketDistributor.sendToPlayersNear(l,null,c.x,c.y,c.z,48,new Balls(worldPosition,x.clone(),z.clone(),mask()));
   if(!any){moving=false;resolve();}
  }
  int mask(){int m=0;for(int i=0;i<16;i++)if(in[i])m|=1<<i;return m;}
  void click(ServerLevel l,float u,float v,float speed){
   if(clicks>=2||speed<.004F)return;clicks++;
   l.playSound(null,BlockPos.containing(world(worldPosition,facing,u,v)),SoundEvents.NOTE_BLOCK_HAT.value(),SoundSource.BLOCKS,Math.min(1F,.25F+speed*4F),1.7F+(float)Math.random()*.3F);}
  void pot(ServerLevel l,int i,float[] pk){
   in[i]=true;vx[i]=vz[i]=0;potted.add(i);var w=world(worldPosition,facing,pk[0],pk[1]);
   l.playSound(null,BlockPos.containing(w),SoundEvents.WOOD_FALL,SoundSource.BLOCKS,.8F,1.4F);
   l.sendParticles(ParticleTypes.CRIT,w.x,w.y+.05,w.z,6,.05,.02,.05,.05);
  }
  void resolve(){
   if(phase!=1){sync();return;}
   int me=shooter,other=1-me;boolean twoPlayers=pl[1]!=null;
   boolean foul=in[0]||firstHit<0;
   if(potted.contains(8)){
    boolean cleared=group[me]!=0;for(int b=1;b<=15;b++)if(groupOf(b)==group[me]&&!in[b])cleared=false;
    boolean win=cleared&&!in[0];phase=2;
    winner=win?name[me]+" venceu!":twoPlayers?name[other]+" venceu! §7("+name[me]+" encaçapou a 8 antes da hora)":name[me]+" perdeu §7(a 8 caiu antes da hora)";
    say("§6§l🎱 "+winner,false);level.playSound(null,worldPosition,win?SoundEvents.PLAYER_LEVELUP:SoundEvents.VILLAGER_NO,SoundSource.BLOCKS,1F,1F);
    sync();return;
   }
   if(group[me]==0)for(int b:potted)if(groupOf(b)!=0&&!foul){group[me]=groupOf(b);group[other]=3-group[me];
    say("§b🎱 "+name[me]+" fica com as "+groupName(group[me])+(twoPlayers?", "+name[other]+" com as "+groupName(group[other]):"")+".",false);break;}
   boolean mine=false;for(int b:potted)if(b!=0&&(group[me]==0||groupOf(b)==group[me]))mine=true;
   boolean again=!foul&&mine;
   if(in[0])spotCue();
   String why=in[0]?"§cFalta: a branca caiu! ":firstHit<0?"§cFalta: não acertou nenhuma bola! ":"";
   if(!again&&twoPlayers)turn=other;
   say(why+(again?"§a"+name[me]+" joga de novo!":"§eVez de "+name[turn]+" §7("+groupName(group[turn])+")"),true);
   sync();
  }
  // ---------------- client side ----------------
  public void apply(Balls b){
   for(int i=0;i<16;i++){ox[i]=x[i];oz[i]=z[i];x[i]=b.x()[i];z[i]=b.z()[i];boolean nowIn=(b.in()>>i&1)!=0;
    if(!in[i]&&!nowIn){float dx=x[i]-ox[i],dz=z[i]-oz[i],d=(float)Math.sqrt(dx*dx+dz*dz);if(d>1e-5F)roll[i].premul(new org.joml.Quaternionf().rotateAxis(d/R,dz/d,0,-dx/d));}
    in[i]=nowIn;}
   moving=true;
  }
  // ---------------- save / sync ----------------
  void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){
   super.saveAdditional(t,r);t.putInt("F",facing.get2DDataValue());
   int[] a=new int[32];for(int i=0;i<16;i++){a[i]=Float.floatToIntBits(x[i]);a[16+i]=Float.floatToIntBits(z[i]);}t.putIntArray("Balls",a);t.putInt("In",mask());
   for(int i=0;i<2;i++){if(pl[i]!=null)t.putUUID("P"+i,pl[i]);t.putString("N"+i,name[i]);t.putInt("G"+i,group[i]);}
   t.putInt("Turn",turn);t.putInt("Phase",phase);t.putString("Winner",winner);t.putBoolean("Moving",moving);
   if(moving){int[] v=new int[32];for(int i=0;i<16;i++){v[i]=Float.floatToIntBits(vx[i]);v[16+i]=Float.floatToIntBits(vz[i]);}t.putIntArray("Vel",v);
    t.putInt("Shooter",shooter);t.putInt("FirstHit",firstHit);t.putIntArray("Potted",potted.stream().mapToInt(Integer::intValue).toArray());}
  }
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
   super.loadAdditional(t,r);facing=Direction.from2DDataValue(t.getInt("F"));
   int[] a=t.getIntArray("Balls");if(a.length==32)for(int i=0;i<16;i++){x[i]=ox[i]=Float.intBitsToFloat(a[i]);z[i]=oz[i]=Float.intBitsToFloat(a[16+i]);}
   int m=t.getInt("In");for(int i=0;i<16;i++)in[i]=(m>>i&1)!=0;
   for(int i=0;i<2;i++){pl[i]=t.hasUUID("P"+i)?t.getUUID("P"+i):null;name[i]=t.getString("N"+i);group[i]=t.getInt("G"+i);}
   turn=t.getInt("Turn");phase=t.getInt("Phase");winner=t.getString("Winner");moving=t.getBoolean("Moving");
   int[] v=t.getIntArray("Vel");if(v.length==32)for(int i=0;i<16;i++){vx[i]=Float.intBitsToFloat(v[i]);vz[i]=Float.intBitsToFloat(v[16+i]);}
   else for(int i=0;i<16;i++)vx[i]=vz[i]=0;
   if(t.contains("Shooter")){shooter=t.getInt("Shooter");firstHit=t.getInt("FirstHit");potted.clear();for(int b:t.getIntArray("Potted"))potted.add(b);}
  }
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
  @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }

 // ---------------- packet: ball positions every tick while they roll ----------------
 public record Balls(BlockPos pos,float[] x,float[] z,int in) implements CustomPacketPayload {
  public static final Type<Balls> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"pool_balls"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Balls> CODEC=StreamCodec.of((b,p)->{b.writeBlockPos(p.pos);for(int i=0;i<16;i++){b.writeFloat(p.x[i]);b.writeFloat(p.z[i]);}b.writeInt(p.in);},
   b->{var pos=b.readBlockPos();float[] x=new float[16],z=new float[16];for(int i=0;i<16;i++){x[i]=b.readFloat();z[i]=b.readFloat();}return new Balls(pos,x,z,b.readInt());});
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 static void packets(RegisterPayloadHandlersEvent e){
  e.registrar("1").playToClient(Balls.TYPE,Balls.CODEC,(p,c)->c.enqueueWork(()->{if(c.player().level().getBlockEntity(p.pos()) instanceof PoolEntity t)t.apply(p);}));
 }
}
