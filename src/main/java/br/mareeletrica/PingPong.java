package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.67.0: table tennis. A 2 × 3 table with a net, a light ball with real bounces, and a paddle:
 *  right-click near the ball hits it to where you aim on the other half (it always clears the net if you aim on the table;
 *  a ball high above the table hit while looking down is a smash). Right-click with the paddle near the table and no ball: serve.
 *  Rules: the ball must bounce once on the other half; hitting it before it bounces, letting it bounce twice, hitting it out
 *  or onto your own half gives the point to the other side. First to 11 (by 2). Crouch + right-click the table (empty hand): new game. */
public final class PingPong {
 private PingPong(){}
 public static final IntegerProperty PART=IntegerProperty.create("part",0,5),ROT=IntegerProperty.create("rot",0,3);
 public static final DeferredBlock<TableBlock> TABLE=MareEletrica.BLOCKS.register("pingpong_table",()->new TableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(1.5F,3F).noOcclusion().sound(SoundType.WOOD)));
 public static final DeferredItem<Item> TABLE_ITEM=MareEletrica.ITEMS.register("pingpong_table",()->new TableItem(new Item.Properties().stacksTo(16)));
 public static final DeferredItem<Item> PADDLE=MareEletrica.ITEMS.register("pingpong_paddle",()->new PaddleItem(new Item.Properties().stacksTo(1)));
 public static final Supplier<BlockEntityType<TableEntity>> ENTITY=MareEletrica.ENTITIES.register("pingpong_table",()->BlockEntityType.Builder.of(TableEntity::new,TABLE.get()).build(null));
 public static final Supplier<EntityType<Ball>> BALL=MareEletrica.ACTORS.register("pingpong_ball",()->EntityType.Builder.<Ball>of(Ball::new,MobCategory.MISC).sized(.12F,.12F).clientTrackingRange(8).updateInterval(1).build(MareEletrica.ID+":pingpong_ball"));
 public static void register(){}
 public static void creative(java.util.function.Consumer<ItemStack> out){out.accept(new ItemStack(TABLE_ITEM.get()));out.accept(new ItemStack(PADDLE.get()));}

 // ---------------- geometry: u across 0..2, v along 0..3, net at v = 1.5 ----------------
 public static final float TW=2,TL=3,TOP=13/16F,NET=3.6F/16F;
 static Direction right(Direction f){return f.getCounterClockWise();}
 static BlockPos at(BlockPos o,Direction f,int c,int r){return o.relative(right(f),c).relative(f,r);}
 static Vec3 world(BlockPos o,Direction f,double u,double v){var rt=right(f);
  double ox=o.getX()+.5-.5*rt.getStepX()-.5*f.getStepX(),oz=o.getZ()+.5-.5*rt.getStepZ()-.5*f.getStepZ();
  return new Vec3(ox+rt.getStepX()*u+f.getStepX()*v,o.getY()+TOP,oz+rt.getStepZ()*u+f.getStepZ()*v);}
 /** table-local (u, height above the top, v) of a world point */
 static Vec3 local(BlockPos o,Direction f,Vec3 p){var rt=right(f);Vec3 d=p.subtract(world(o,f,0,0));
  return new Vec3(d.x*rt.getStepX()+d.z*rt.getStepZ(),d.y,d.x*f.getStepX()+d.z*f.getStepZ());}
 static TableEntity findMain(BlockGetter l,BlockPos pos){
  for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){var q=pos.offset(dx,0,dz);
   if(l.getBlockState(q).getBlock() instanceof TableBlock&&l.getBlockState(q).getValue(PART)==0&&l.getBlockEntity(q) instanceof TableEntity e&&e.contains(pos))return e;}
  return null;}
 public static TableEntity nearest(Level l,Vec3 p,double r){
  TableEntity best=null;double bd=r*r;var c=BlockPos.containing(p);
  for(var q:BlockPos.betweenClosed(c.offset(-5,-2,-5),c.offset(5,2,5))){var s=l.getBlockState(q);
   if(s.getBlock() instanceof TableBlock&&s.getValue(PART)==0&&l.getBlockEntity(q) instanceof TableEntity e){double d=e.center().distanceToSqr(p);if(d<bd){bd=d;best=e;}}}
  return best;}

 // ---------------- table blocks ----------------
 public static final class TableBlock extends BaseEntityBlock {
  static boolean removing;
  public TableBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PART,0).setValue(ROT,0));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART,ROT);}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
   VoxelShape top=Block.box(0,12,0,16,13,16);
   if(s.getValue(PART)%3!=1)return top;
   boolean alongZ=s.getValue(ROT)%2==0;   // the net runs across the table (along u)
   return Shapes.or(top,alongZ?Block.box(0,13,7.6,16,16.6,8.4):Block.box(7.6,13,0,8.4,16.6,16));}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(PART)==0?new TableEntity(p,s):null;}
  @Override public ItemStack getCloneItemStack(LevelReader l,BlockPos p,BlockState s){return new ItemStack(TABLE_ITEM.get());}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){
   if(!l.isClientSide&&findMain(l,pos) instanceof TableEntity e){
    if(p.isShiftKeyDown()){e.newGame();e.say("§a🏓 Novo jogo! Pegue a raquete e saque (botão direito perto da mesa).",false);}
    else p.displayClientMessage(Component.literal(e.status()),true);}
   return InteractionResult.sidedSuccess(l.isClientSide);}
  @Override public BlockState playerWillDestroy(Level l,BlockPos pos,BlockState s,Player p){
   if(!l.isClientSide&&!p.isCreative())popResource(l,pos,new ItemStack(TABLE_ITEM.get()));return super.playerWillDestroy(l,pos,s,p);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos pos,BlockState ns,boolean moved){
   if(!removing&&!s.is(ns.getBlock())&&!l.isClientSide){var e=findMain(l,pos);
    if(e!=null){removing=true;try{for(int c=0;c<2;c++)for(int r=0;r<3;r++){var q=at(e.getBlockPos(),e.facing,c,r);if(!q.equals(pos)&&l.getBlockState(q).is(this))l.setBlock(q,Blocks.AIR.defaultBlockState(),3);}}finally{removing=false;}}}
   super.onRemove(s,l,pos,ns,moved);}
 }
 public static final class TableItem extends Item {
  public TableItem(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();var f=c.getHorizontalDirection();BlockPos o=c.getClickedPos();if(!l.getBlockState(o).canBeReplaced())o=o.relative(c.getClickedFace());
   for(int col=0;col<2;col++)for(int r=0;r<3;r++){var q=at(o,f,col,r);
    if(!l.getBlockState(q).canBeReplaced()){if(c.getPlayer()!=null&&!l.isClientSide)c.getPlayer().displayClientMessage(Component.literal("§cA mesa precisa de 2 × 3 blocos livres (à frente e à esquerda)."),true);return InteractionResult.FAIL;}}
   if(l.isClientSide)return InteractionResult.SUCCESS;
   for(int col=0;col<2;col++)for(int r=0;r<3;r++)l.setBlock(at(o,f,col,r),TABLE.get().defaultBlockState().setValue(PART,col*3+r).setValue(ROT,f.get2DDataValue()),3);
   if(l.getBlockEntity(o) instanceof TableEntity e){e.facing=f;e.newGame();}
   l.playSound(null,o,SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,1F,1F);
   if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   return InteractionResult.CONSUME;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Mesa de 2 × 3 blocos (à frente e à sua esquerda). Jogue com a raquete."));}
 }

 // ---------------- the game ----------------
 public static final class TableEntity extends BlockEntity {
  public Direction facing=Direction.SOUTH;public final int[] score=new int[2];public final String[] name={"Lado A","Lado B"};
  int lastSide=-1,bouncedOn=-1,bounces;long resetAt=-1;
  public TableEntity(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  public Vec3 center(){return world(worldPosition,facing,TW/2,TL/2);}
  boolean contains(BlockPos p){for(int c=0;c<2;c++)for(int r=0;r<3;r++)if(at(worldPosition,facing,c,r).equals(p))return true;return false;}
  int sideOf(Vec3 p){return local(worldPosition,facing,p).z<TL/2?0:1;}
  void newGame(){score[0]=score[1]=0;name[0]="Lado A";name[1]="Lado B";lastSide=-1;setChanged();}
  public String status(){return "§e🏓 "+name[0]+" §f"+score[0]+" x "+score[1]+" §e"+name[1]+" §8• Shift + clique (mão vazia): novo jogo";}
  void say(String m,boolean bar){for(var p:level.players())if(p.distanceToSqr(center())<16*16)p.displayClientMessage(Component.literal(m),bar);}
  /** a paddle hit from side s; returns false if that hit itself was a fault (point already given) */
  boolean hit(int s,Player who){
   name[s]=who.getName().getString();
   if(lastSide>=0){
    if(lastSide==s){point(1-s,"tocou duas vezes");return false;}
    if(bouncedOn!=s){point(1-s,"voleio — a bola não quicou");return false;}
   }
   lastSide=s;bouncedOn=-1;bounces=0;return true;}
  void table(int s){
   if(lastSide<0)return;
   if(s==lastSide){point(1-s,"quicou no próprio lado");return;}
   bouncedOn=s;if(++bounces>=2)point(lastSide,"dois quiques");}
  void floor(){if(lastSide<0)return;if(bouncedOn==1-lastSide)point(lastSide,"não devolveu");else point(1-lastSide,"bola fora");}
  void point(int s,String why){
   score[s]++;lastSide=-1;setChanged();
   level.playSound(null,worldPosition,SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,.6F,s==0?1.2F:1.5F);
   boolean win=score[s]>=11&&score[s]-score[1-s]>=2;
   if(win){say("§6§l🏓 "+name[s]+" venceu "+score[s]+" x "+score[1-s]+"!",false);level.playSound(null,worldPosition,SoundEvents.PLAYER_LEVELUP,SoundSource.BLOCKS,.8F,1.2F);score[0]=score[1]=0;}
   else say("§e🏓 Ponto de "+name[s]+" §7("+why+") §f"+name[0]+" "+score[0]+" x "+score[1]+" "+name[1],true);
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("F",facing.get2DDataValue());t.putIntArray("Score",score);t.putString("N0",name[0]);t.putString("N1",name[1]);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);facing=Direction.from2DDataValue(t.getInt("F"));
   int[] s=t.getIntArray("Score");if(s.length==2){score[0]=s[0];score[1]=s[1];}if(t.contains("N0")){name[0]=t.getString("N0");name[1]=t.getString("N1");}}
 }

 // ---------------- ball ----------------
 public static final double G=.022,DRAG=.992;
 public static final class Ball extends Entity {
  BlockPos table;int idle,deadAt=-1;
  public Ball(EntityType<? extends Ball> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override public boolean isPickable(){return true;}
  @Override public boolean skipAttackInteraction(Entity a){if(a instanceof Player p&&!level().isClientSide)PaddleItem.swing(level(),p,this);return true;}
  @Override public boolean hurt(DamageSource s,float a){return false;}
  TableEntity table(){return table!=null&&level().getBlockEntity(table) instanceof TableEntity e?e:null;}
  @Override public void tick(){
   super.tick();
   Vec3 v=getDeltaMovement().add(0,-G,0).scale(DRAG);
   move(MoverType.SELF,v);
   double nx=v.x,ny=v.y,nz=v.z;
   if(verticalCollision&&v.y<0){
    var below=level().getBlockState(BlockPos.containing(getX(),getY()-.05,getZ()));boolean onTable=below.getBlock() instanceof TableBlock;
    double e=onTable?.86:.5;ny=-v.y*e;if(ny<.05)ny=0;
    if(!level().isClientSide&&-v.y>.06){
     level().playSound(null,blockPosition(),onTable?SoundEvents.NOTE_BLOCK_HAT.value():SoundEvents.WOOD_STEP,SoundSource.PLAYERS,onTable?.5F:.3F,onTable?2F:1.8F);
     var t=table();if(t!=null&&deadAt<0){if(onTable)t.table(t.sideOf(position()));else{t.floor();deadAt=tickCount+30;}}
    }
    if(ny==0){nx*=.9;nz*=.9;}
   }else if(verticalCollision)ny=-v.y*.3;
   if(horizontalCollision){Vec3 m=getDeltaMovement();if(Math.abs(m.x-v.x)>1e-6||true){nx=-v.x*.25;nz=-v.z*.25;}
    if(!level().isClientSide&&v.horizontalDistance()>.08)level().playSound(null,blockPosition(),SoundEvents.WOOL_STEP,SoundSource.PLAYERS,.3F,1.8F);}
   setDeltaMovement(nx,ny,nz);
   if(level().isClientSide)return;
   idle=v.lengthSqr()<1e-4?idle+1:0;
   if(idle>200||tickCount>20*60*3||deadAt>0&&tickCount>deadAt+40||getY()<level().getMinBuildHeight())discard();
  }
  @Override protected void readAdditionalSaveData(CompoundTag t){}
  @Override protected void addAdditionalSaveData(CompoundTag t){}
 }

 // ---------------- paddle ----------------
 public static final class PaddleItem extends Item {
  public PaddleItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var s=p.getItemInHand(h);if(l.isClientSide)return InteractionResultHolder.success(s);
   p.swing(h,true);
   Ball b=nearestBall(l,p);
   if(b!=null){swing(l,p,b);return InteractionResultHolder.consume(s);}
   var t=nearest(l,p.position(),4.5);
   if(t==null){p.displayClientMessage(Component.literal("§7Chegue perto de uma mesa de pingue-pongue."),true);return InteractionResultHolder.fail(s);}
   for(var old:l.getEntitiesOfClass(Ball.class,new net.minecraft.world.phys.AABB(t.center(),t.center()).inflate(6)))old.discard();
   // serve: the ball is tossed up above your end of the table
   int side=t.sideOf(p.position());Vec3 lp=local(t.getBlockPos(),t.facing,p.position());
   double u=Mth.clamp(lp.x,.3,1.7),v=side==0?-.15:TL+.15;
   Vec3 at=world(t.getBlockPos(),t.facing,u,v).add(0,.45,0);
   var ball=BALL.get().create(l);ball.setPos(at.x,at.y,at.z);ball.setDeltaMovement(0,.22,0);ball.table=t.getBlockPos();l.addFreshEntity(ball);
   t.lastSide=-1;t.name[side]=p.getName().getString();
   l.playSound(null,p.blockPosition(),SoundEvents.WOOL_STEP,SoundSource.PLAYERS,.4F,1.8F);
   p.displayClientMessage(Component.literal("§b🏓 Saque! §7Clique de novo para bater na bola."),true);
   return InteractionResultHolder.consume(s);}
  static Ball nearestBall(Level l,Player p){
   Vec3 eye=p.getEyePosition(),look=p.getLookAngle();Ball best=null;double bd=1e9;
   for(var b:l.getEntitiesOfClass(Ball.class,p.getBoundingBox().inflate(2.9))){Vec3 to=b.position().subtract(eye);double d=to.length();
    if(d>3.1||to.normalize().dot(look)<.25)continue;if(d<bd){bd=d;best=b;}}
   return best;}
  /** hit the ball to where the crosshair meets the other half of the table, high enough to clear the net */
  static void swing(Level l,Player p,Ball b){
   var t=b.table();if(t==null){t=nearest(l,b.position(),5);if(t==null)return;b.table=t.getBlockPos();}
   int side=t.sideOf(p.position());
   if(!t.hit(side,p)){b.deadAt=b.tickCount;return;}
   int opp=1-side;Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
   double u=TW/2,v=opp==0?TL*.25:TL*.75;
   if(look.y<-.02){double k=(t.center().y-eye.y)/look.y;Vec3 hit=eye.add(look.scale(k));Vec3 lh=local(t.getBlockPos(),t.facing,hit);
    u=lh.x;v=lh.z;}
   u=Mth.clamp(u,.15,TW-.15);v=opp==0?Mth.clamp(v,.15,TL/2-.15):Mth.clamp(v,TL/2+.15,TL-.15);
   Vec3 target=world(t.getBlockPos(),t.facing,u,v).add(0,.06,0);
   boolean smash=b.getY()>t.center().y+.45&&p.getXRot()>8;
   double dist=target.subtract(b.position()).horizontalDistance();double ticks=Mth.clamp(dist/(smash?.62:.38),smash?5:9,30);
   Vec3 vel=null;Vec3 netA=world(t.getBlockPos(),t.facing,0,TL/2),fwdDir=world(t.getBlockPos(),t.facing,0,TL/2+1).subtract(netA);
   for(int tries=0;tries<8;tries++){
    vel=arc(b.position(),target,ticks);
    if(clears(b.position(),vel,netA,fwdDir,t.center().y+NET+.05))break;
    ticks+=2.5;}
   b.setDeltaMovement(vel);b.hurtMarked=true;b.idle=0;
   l.playSound(null,b.blockPosition(),SoundEvents.WOOD_HIT,SoundSource.PLAYERS,smash?.9F:.6F,smash?1.5F:2F);
   if(smash){((ServerLevel)l).sendParticles(ParticleTypes.CRIT,b.getX(),b.getY(),b.getZ(),8,.1,.1,.1,.2);p.displayClientMessage(Component.literal("§c§l🏓 CORTADA!"),true);}
  }
  /** start velocity that reaches {@code to} after {@code n} ticks with this ball's gravity and drag */
  static Vec3 arc(Vec3 from,Vec3 to,double n){
   Vec3 d=to.subtract(from);double k=DRAG,S=k*(1-Math.pow(k,n))/(1-k);
   return new Vec3(d.x/S,(d.y+G*k/(1-k)*(n-S))/S,d.z/S);}
  /** simulate the flight and check its height where it crosses the net plane */
  static boolean clears(Vec3 p,Vec3 v,Vec3 netPoint,Vec3 along,double minY){
   double side0=p.subtract(netPoint).dot(along);
   for(int i=0;i<80;i++){v=v.add(0,-G,0).scale(DRAG);Vec3 q=p.add(v);double s1=q.subtract(netPoint).dot(along);
    if(Math.signum(s1)!=Math.signum(side0)&&s1!=0){double f=side0/(side0-s1);return p.y+(q.y-p.y)*f>=minY;}
    p=q;side0=s1;}
   return true;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Botão direito perto da mesa: saca • perto da bola: rebate para onde você mira no outro lado."));
   t.add(Component.literal("§7Bola alta + olhando para baixo: cortada. Até 11 pontos."));}
 }
}
