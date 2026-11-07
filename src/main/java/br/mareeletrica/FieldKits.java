package br.mareeletrica;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredItem;

/** Ready-made sports fields: click the ground once to see the outline, click again to build the whole field in front of you
 *  (floor, lines, goals or hoops, net, scoreboard, lights and a ball). */
public final class FieldKits {
 public enum Kind {
  FUTEBOL("Campo de Futebol",90,60),BASQUETE("Quadra de Basquete",52,36),VOLEI("Quadra de Vôlei",42,24),TENIS("Quadra de Tênis",60,40),GALOS("Arena de Galos",27,27),
  BOLICHE("Pista de Boliche",31,9),GOLFE("Campo de Golfe",42,23),MINIGOLFE("Minigolfe (3 buracos)",19,17);
  public final String label;public final int len,wid;
  Kind(String l,int len,int wid){label=l;this.len=len;this.wid=wid;}
 }
 public static final Map<Kind,DeferredItem<KitItem>> ITEMS=new EnumMap<>(Kind.class);
 static{for(var k:Kind.values())ITEMS.put(k,MareEletrica.ITEMS.register("field_kit_"+k.name().toLowerCase(Locale.ROOT),()->new KitItem(k)));}
 private FieldKits(){}
 public static void register(){}

 /** Field frame: u runs away from the player (0..len-1), v across (centred), y = floor level. */
 static final class Frame {
  final ServerLevel l;final BlockPos o;final Direction f;final Kind k;
  Frame(ServerLevel l,BlockPos o,Direction f,Kind k){this.l=l;this.o=o;this.f=f;this.k=k;}
  BlockPos at(int u,int y,int v){var r=f.getClockWise();return o.relative(f,u).relative(r,v).above(y);}
  void set(int u,int y,int v,BlockState s){var p=at(u,y,v);if(l.getBlockState(p).getDestroySpeed(l,p)>=0)l.setBlock(p,s,2);}
  void big(int u,int v,BlockState s){var p=at(u,1,v);l.setBlock(p,s,3);Multiblock.place(l,p,s);}
  int vmin(){return -k.wid/2;}int vmax(){return vmin()+k.wid-1;}
 }
 static BlockState face(BlockState s,Direction d){return s.hasProperty(HorizontalDirectionalBlock.FACING)?s.setValue(HorizontalDirectionalBlock.FACING,d):s;}

 /** Builds a field with its start at o, running in direction f (also used by tests). */
 public static void buildAt(ServerLevel l,BlockPos o,Direction f,Kind k){build(new Frame(l,o,f,k));}
 static void build(Frame F){
  var k=F.k;int m=4;
  // clear the space (with a margin for goals, posts and benches) and lay a firm base
  for(int u=-m;u<k.len+m;u++)for(int v=F.vmin()-m;v<=F.vmax()+m;v++){
   for(int y=1;y<=10;y++)F.set(u,y,v,Blocks.AIR.defaultBlockState());
   if(F.l.getBlockState(F.at(u,-1,v)).isAir())F.set(u,-1,v,Blocks.DIRT.defaultBlockState());
   F.set(u,0,v,(u<0||u>=k.len||v<F.vmin()||v>F.vmax())?(k==Kind.TENIS?Blocks.GREEN_CONCRETE.defaultBlockState():k==Kind.FUTEBOL||k==Kind.GOLFE?Blocks.GRASS_BLOCK.defaultBlockState():k==Kind.BOLICHE?Blocks.DARK_OAK_PLANKS.defaultBlockState():Blocks.STONE_BRICKS.defaultBlockState()):floor(k,u,v));
  }
  if(k==Kind.BOLICHE){bowling(F);return;}
  if(k==Kind.GOLFE){golf(F);return;}
  if(k==Kind.MINIGOLFE){minigolf(F);return;}
  var W=Blocks.WHITE_CONCRETE.defaultBlockState();
  // border lines
  for(int u=0;u<k.len;u++){F.set(u,0,F.vmin(),W);F.set(u,0,F.vmax(),W);}
  for(int v=F.vmin();v<=F.vmax();v++){F.set(0,0,v,W);F.set(k.len-1,0,v,W);}
  Direction in=F.f,out=F.f.getOpposite();
  switch(k){
   case FUTEBOL->{
    for(int v=F.vmin();v<=F.vmax();v++)F.set(k.len/2,0,v,W);
    for(int a=0;a<360;a+=3){int u=k.len/2+(int)Math.round(Math.cos(Math.toRadians(a))*9),v=(int)Math.round(Math.sin(Math.toRadians(a))*9);F.set(u,0,v,W);}
    for(int s=0;s<2;s++){int u0=s==0?0:k.len-1,dir=s==0?1:-1;
     // penalty area 10 deep x 21 wide, goal area 4 x 11, penalty spot, the arc
     for(int i=0;i<=14;i++){F.set(u0+dir*i,0,-17,W);F.set(u0+dir*i,0,17,W);}for(int v=-17;v<=17;v++)F.set(u0+dir*14,0,v,W);
     for(int i=0;i<=6;i++){F.set(u0+dir*i,0,-8,W);F.set(u0+dir*i,0,8,W);}for(int v=-8;v<=8;v++)F.set(u0+dir*6,0,v,W);
     F.set(u0+dir*10,0,0,W);
     for(int a=-60;a<=60;a+=4){int u=u0+dir*(10+(int)Math.round(Math.cos(Math.toRadians(a))*7)),v=(int)Math.round(Math.sin(Math.toRadians(a))*7);if(dir*(u-u0)>14)F.set(u,0,v,W);}}
    // goals sit just behind the goal lines, mouths facing the pitch
    F.big(-1,0,face(Sports.GOAL.get().defaultBlockState(),in));
    F.big(k.len,0,face(Sports.GOAL.get().defaultBlockState(),out));
    F.big(k.len/2+6,F.vmin()-2,face(Scoreboard.BLOCK.get().defaultBlockState(),F.f.getCounterClockWise().getOpposite()));
    lights(F);ball(F,Sports.Kind.SOCCER,k.len/2,0);
   }
   case BASQUETE->{
    for(int v=F.vmin();v<=F.vmax();v++)F.set(k.len/2,0,v,W);
    for(int s=0;s<2;s++){int u0=s==0?0:k.len-1,dir=s==0?1:-1;
     for(int i=1;i<=9;i++)for(int v=-5;v<=5;v++)F.set(u0+dir*i,0,v,Blocks.ORANGE_CONCRETE.defaultBlockState());
     for(int a=0;a<360;a+=8){int u=u0+dir*(9+(int)Math.round(Math.cos(Math.toRadians(a))*5)),v=(int)Math.round(Math.sin(Math.toRadians(a))*5);if(dir*(u-u0)>=9)F.set(u,0,v,W);}
     for(int a=-90;a<=90;a+=2){int u=u0+dir*(int)Math.round(2+Math.cos(Math.toRadians(a))*15),v=(int)Math.round(Math.sin(Math.toRadians(a))*15);F.set(u,0,v,W);}}
    for(int a=0;a<360;a+=5){int u=k.len/2+(int)Math.round(Math.cos(Math.toRadians(a))*4),v=(int)Math.round(Math.sin(Math.toRadians(a))*4);F.set(u,0,v,W);}
    F.big(1,0,face(Sports.HOOP.get().defaultBlockState(),in));   // rims 1 block inside each end line
    F.big(k.len-2,0,face(Sports.HOOP.get().defaultBlockState(),out));
    F.big(k.len/2+6,F.vmin()-2,face(Scoreboard.BLOCK.get().defaultBlockState(),F.f.getCounterClockWise().getOpposite()));
    lights(F);ball(F,Sports.Kind.BASKET,k.len/2,0);
   }
   case VOLEI->{
    int mid=k.len/2;
    for(int v=F.vmin()-1;v<=F.vmax()+1;v++){
     if(v<F.vmin()||v>F.vmax()){for(int y=1;y<=3;y++)F.set(mid,y,v,Blocks.OAK_FENCE.defaultBlockState());continue;}
     F.set(mid,0,v,W);F.set(mid,2,v,netState(F));F.set(mid,3,v,netState(F));}
    for(int v=F.vmin();v<=F.vmax();v++){F.set(mid-7,0,v,W);F.set(mid+7,0,v,W);}
    lights(F);ball(F,Sports.Kind.VOLLEY,mid-9,0);
   }
   case TENIS->{
    int mid=k.len/2;
    for(int v=F.vmin()-1;v<=F.vmax()+1;v++){if(v<F.vmin()||v>F.vmax()){F.set(mid,1,v,Blocks.OAK_FENCE.defaultBlockState());continue;}F.set(mid,1,v,netState(F));}
    for(int u=0;u<k.len;u++){F.set(u,0,F.vmin()+5,W);F.set(u,0,F.vmax()-5,W);}
    int sv=16;for(int u=mid-sv;u<=mid+sv;u++)F.set(u,0,0,W);
    for(int v=F.vmin()+5;v<=F.vmax()-5;v++){F.set(mid-sv,0,v,W);F.set(mid+sv,0,v,W);}
    F.set(1,0,0,W);F.set(k.len-2,0,0,W);
    lights(F);
   }
   case GALOS->{
    F.big(k.len/2,0,face(ChickenBattle.BLOCK.get().defaultBlockState(),out));
    // benches around the ring
    for(int u=1;u<k.len-1;u++){F.set(u,1,F.vmin(),stair(F.f.getClockWise()));F.set(u,1,F.vmax(),stair(F.f.getCounterClockWise()));}
    lights(F);
   }
  }
 }
 /** Bowling alley: approach, foul line, 22-block maple lane with arrows, gutters, side walls, pin deck with the pinsetter,
  *  back pit and a lit masking unit over the pins. u runs from the bowler to the pins. */
 static void bowling(Frame F){
  var face=F.f;BlockState lane=Games.LANE.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,face),arrow=Games.LANE_ARROW.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,face),
   gutter=Games.GUTTER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,face),wall=Blocks.DARK_OAK_PLANKS.defaultBlockState(),black=Blocks.BLACK_WOOL.defaultBlockState();
  int L0=6,PS=28;   // lane starts at u=6 (foul line u=5), pinsetter at u=28 (head pin 4 blocks before it)
  for(int u=0;u<F.k.len;u++)for(int v=-4;v<=4;v++){
   int a=Math.abs(v);
   if(u<L0-1){F.set(u,0,v,a<=3?Blocks.BIRCH_PLANKS.defaultBlockState():Blocks.POLISHED_ANDESITE.defaultBlockState());continue;}
   if(u==L0-1){F.set(u,0,v,a<=2?Blocks.RED_CONCRETE.defaultBlockState():Blocks.BIRCH_PLANKS.defaultBlockState());continue;}   // foul line
   if(a<=1)F.set(u,0,v,(u==L0+6||u==L0+7&&a==0)?arrow:lane);
   else if(a==2)F.set(u,0,v,gutter);
   else if(a==3){F.set(u,0,v,wall);F.set(u,1,v,wall);}
   else F.set(u,0,v,Blocks.POLISHED_ANDESITE.defaultBlockState());
  }
  for(int v=-3;v<=3;v++){F.set(PS+2,1,v,black);F.set(PS+2,2,v,black);F.set(PS+1,0,v,Blocks.BLACK_CONCRETE.defaultBlockState());}   // pit + back cushion
  for(int u=PS-2;u<=PS+2;u++)for(int v=-3;v<=3;v++)F.set(u,3,v,Math.abs(v)<=2&&u==PS-2?Blocks.SEA_LANTERN.defaultBlockState():wall);   // masking unit
  for(int y=1;y<=2;y++){F.set(PS-2,y,-3,wall);F.set(PS-2,y,3,wall);}
  var ps=F.at(PS,1,0);F.l.setBlock(ps,Games.PINSETTER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,face.getOpposite()),3);
  if(F.l.getBlockEntity(ps) instanceof Games.PinsetterEntity pe)pe.setPins();
  for(int u:new int[]{2,12,22})for(int v:new int[]{-4,4})F.set(u,1,v,Blocks.LANTERN.defaultBlockState());
  for(int i=0;i<3;i++){var at=F.at(1,1,-2+i*2);var it=new net.minecraft.world.entity.item.ItemEntity(F.l,at.getX()+.5,at.getY()+.2,at.getZ()+.5,new ItemStack(Games.BOWLING_BALL_ITEM.get()));it.setDeltaMovement(0,0,0);F.l.addFreshEntity(it);}
 }
 /** Golf hole: tee box, a dog-leg fairway (mowing stripes), rough, two bunkers, a pond, the green with the hole and flag, trees. */
 static void golf(Frame F){
  int GU=36,GV=4,R=5;
  for(int u=-4;u<F.k.len+4;u++)for(int v=F.vmin()-4;v<=F.vmax()+4;v++){
   double c=u<16?0:Math.min(GV,(u-16)*GV/16.0);double dv=Math.abs(v-c);
   double dg=Math.hypot(u-GU,v-GV);
   BlockState s;
   if(dg<=R)s=Blocks.GREEN_CONCRETE.defaultBlockState();
   else if(dg<=R+1.3)s=Blocks.MOSS_BLOCK.defaultBlockState();
   else if(u>=0&&u<=3&&Math.abs(v)<=2)s=Blocks.MOSS_BLOCK.defaultBlockState();
   else if(u>3&&u<GU&&dv<=3.6)s=((u/3)%2==0)?Blocks.GRASS_BLOCK.defaultBlockState():Blocks.MOSS_BLOCK.defaultBlockState();
   else{s=Blocks.GRASS_BLOCK.defaultBlockState();if(F.l.random.nextInt(3)==0&&u>=0&&u<F.k.len)F.set(u,1,v,Blocks.SHORT_GRASS.defaultBlockState());}
   F.set(u,0,v,s);
  }
  for(int u=29;u<=31;u++)for(int v=-2;v<=0;v++)F.set(u,0,v,Blocks.SAND.defaultBlockState());          // greenside bunker
  for(int u=19;u<=21;u++)for(int v=6;v<=8;v++)F.set(u,0,v,Blocks.SAND.defaultBlockState());            // fairway bunker
  for(int u=21;u<=25;u++)for(int v=-9;v<=-6;v++){F.set(u,-1,v,Blocks.SAND.defaultBlockState());F.set(u,0,v,Blocks.WATER.defaultBlockState());}   // pond
  F.set(1,1,-2,Blocks.RED_WOOL.defaultBlockState());F.set(1,1,2,Blocks.RED_WOOL.defaultBlockState());     // tee markers
  F.set(GU,0,GV,Games.GOLF_HOLE.get().defaultBlockState());
  for(int[] t:new int[][]{{8,-8},{14,9},{27,11},{38,-7},{5,9},{33,12}}){
   for(int y=1;y<=4;y++)F.set(t[0],y,t[1],Blocks.OAK_LOG.defaultBlockState());
   for(int du=-2;du<=2;du++)for(int dv=-2;dv<=2;dv++)for(int y=4;y<=6;y++)if(Math.abs(du)+Math.abs(dv)+(y-4)<=4&&!(du==0&&dv==0&&y<5))F.set(t[0]+du,y,t[1]+dv,Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT,true));
  }
  var at=F.at(2,1,0);
  for(var st:new ItemStack[]{new ItemStack(Games.GOLF_CLUB.get()),new ItemStack(Games.GOLF_BALL_ITEM.get(),3)}){var it=new net.minecraft.world.entity.item.ItemEntity(F.l,at.getX()+.5,at.getY()+.2,at.getZ()+.5,st);it.setDeltaMovement(0,0,0);F.l.addFreshEntity(it);}
 }
 /** Minigolf: three 3-wide lanes with bumpers — 1: through the windmill, 2: up a ramp, across a plateau and down, 3: a zig-zag bank shot. */
 static void minigolf(Frame F){
  var f=F.f;BlockState turf=MiniGolf.TURF.get().defaultBlockState(),border=MiniGolf.BORDER.get().defaultBlockState(),tee=MiniGolf.TEE.get().defaultBlockState(),
   hole=Games.GOLF_HOLE.get().defaultBlockState(),path=Blocks.SMOOTH_SANDSTONE.defaultBlockState(),up=MiniGolf.RAMP.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,f),
   down=MiniGolf.RAMP.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,f.getOpposite());
  int L=F.k.len;int[] centers={-5,0,5};
  for(int u=-1;u<=L;u++)for(int v=-8;v<=8;v++)F.set(u,0,v,path);
  for(int c:centers){
   for(int u=0;u<L;u++)for(int v=c-1;v<=c+1;v++)F.set(u,0,v,turf);
   for(int u=-1;u<=L;u++){F.set(u,1,c-2,border);F.set(u,1,c+2,border);}
   for(int v=c-1;v<=c+1;v++){F.set(-1,1,v,border);F.set(L,1,v,border);}
   F.set(1,0,c,tee);F.set(L-3,0,c,hole);
  }
  // 1: windmill in the middle of the lane, sails facing the tee — the only way through is its tunnel
  int c=centers[0],wm=8;F.set(wm,1,c-1,border);F.set(wm,1,c+1,border);
  var w=F.at(wm,1,c);F.l.setBlock(w,MiniGolf.WINDMILL.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,f.getOpposite()),3);
  // 2: ramp up, plateau (one block higher), ramp down
  c=centers[1];
  for(int v=c-1;v<=c+1;v++){F.set(5,1,v,up);for(int u=6;u<=9;u++)F.set(u,1,v,turf);F.set(10,1,v,down);}
  for(int u=5;u<=10;u++){F.set(u,2,c-2,border);F.set(u,2,c+2,border);}
  // 3: zig-zag: gaps on alternating sides
  c=centers[2];F.set(5,1,c-1,border);F.set(5,1,c,border);F.set(9,1,c,border);F.set(9,1,c+1,border);F.set(13,1,c-1,border);
  for(int u:new int[]{-1,L})for(int v:new int[]{-8,8})F.set(u,1,v,Blocks.LANTERN.defaultBlockState());
  for(int u=2;u<L;u+=5)for(int v:new int[]{-8,8})F.set(u,1,v,Blocks.POTTED_RED_TULIP.defaultBlockState());
  var at=F.at(-2,1,0);
  for(var st:new ItemStack[]{new ItemStack(MiniGolf.PUTTER.get()),new ItemStack(MiniGolf.PUTTER.get()),new ItemStack(Games.GOLF_BALL_ITEM.get(),3)}){var it=new net.minecraft.world.entity.item.ItemEntity(F.l,at.getX()+.5,at.getY()+.2,at.getZ()+.5,st);it.setDeltaMovement(0,0,0);F.l.addFreshEntity(it);}
 }
 static BlockState netState(Frame F){var s=TennisBall.NET.get().defaultBlockState();return face(s,F.f);}
 static BlockState stair(Direction d){return Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING,d);}
 static BlockState floor(Kind k,int u,int v){
  return switch(k){
   case FUTEBOL->(u/3)%2==0?Blocks.GRASS_BLOCK.defaultBlockState():Blocks.MOSS_BLOCK.defaultBlockState();   // mowing stripes
   case BASQUETE->Blocks.SPRUCE_PLANKS.defaultBlockState();
   case VOLEI->Blocks.SAND.defaultBlockState();
   case TENIS->Blocks.BLUE_CONCRETE.defaultBlockState();
   case GALOS->Blocks.POLISHED_ANDESITE.defaultBlockState();
   case BOLICHE->Blocks.BIRCH_PLANKS.defaultBlockState();
   case GOLFE,MINIGOLFE->Blocks.GRASS_BLOCK.defaultBlockState();
  };
 }
 /** Floodlights: posts with a white LED lamp on top at the four corners. */
 static void lights(Frame F){
  int L=F.k.len;int[][] c=L>=80?new int[][]{{-2,F.vmin()-2},{-2,F.vmax()+2},{L+1,F.vmin()-2},{L+1,F.vmax()+2},{L/4,F.vmin()-3},{L/4,F.vmax()+3},{L/2,F.vmin()-3},{L/2,F.vmax()+3},{3*L/4,F.vmin()-3},{3*L/4,F.vmax()+3}}
   :L>=30?new int[][]{{-2,F.vmin()-2},{-2,F.vmax()+2},{L+1,F.vmin()-2},{L+1,F.vmax()+2},{L/2,F.vmin()-3},{L/2,F.vmax()+3}}
   :new int[][]{{-2,F.vmin()-2},{-2,F.vmax()+2},{L+1,F.vmin()-2},{L+1,F.vmax()+2}};
  var lamp=Lamps.BLOCKS.get(0).get().defaultBlockState();
  for(var p:c){int hgt=L>=80?11:L>=60?9:L>=40?7:5;for(int y=1;y<=hgt;y++)F.set(p[0],y,p[1],Blocks.IRON_BARS.defaultBlockState());F.set(p[0],hgt+1,p[1],lamp);}
 }
 static void ball(Frame F,Sports.Kind kind,int u,int v){
  var b=Sports.BALL.get().create(F.l);if(b==null)return;var p=F.at(u,1,v);
  b.setItem(new ItemStack(Sports.BALLS.get(kind).get()));b.moveTo(p.getX()+.5,p.getY()+.1,p.getZ()+.5,0,0);F.l.addFreshEntity(b);
 }

 public static final class KitItem extends Item {
  final Kind kind;
  /** player -> (origin, time) of the outline shown by the first click */
  static final Map<UUID,long[]> PENDING=new HashMap<>();
  KitItem(Kind k){super(new Item.Properties().stacksTo(16));kind=k;}
  @Override public Component getName(ItemStack s){return Component.literal(kind.label+" (pronto)");}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7"+kind.len+" x "+kind.wid+" blocos, com tudo montado"));
   t.add(Component.literal("§7Clique no chão: mostra a área • clique de novo: constrói"));
   t.add(Component.literal("§cApaga o que estiver na área!"));
  }
  @Override public InteractionResult useOn(UseOnContext c){
   if(!(c.getLevel() instanceof ServerLevel sl)||c.getPlayer()==null)return InteractionResult.SUCCESS;
   Player p=c.getPlayer();var f=p.getDirection();var o=c.getClickedPos().relative(f,2);
   var F=new Frame(sl,o,f,kind);long key=o.asLong();long now=sl.getGameTime();
   var prev=PENDING.get(p.getUUID());
   if(prev!=null&&prev[0]==key&&now-prev[1]<200){
    PENDING.remove(p.getUUID());build(F);
    sl.playSound(null,o,SoundEvents.PLAYER_LEVELUP,SoundSource.BLOCKS,1F,1.2F);
    p.displayClientMessage(Component.literal("§a✔ "+kind.label+" pronto!"),true);
    if(!p.isCreative())c.getItemInHand().shrink(1);
    return InteractionResult.CONSUME;
   }
   PENDING.put(p.getUUID(),new long[]{key,now});
   // outline with particles
   for(int u=0;u<kind.len;u++)for(int v:new int[]{F.vmin(),F.vmax()}){var q=F.at(u,1,v);sl.sendParticles(p instanceof net.minecraft.server.level.ServerPlayer sp?ParticleTypes.HAPPY_VILLAGER:ParticleTypes.HAPPY_VILLAGER,q.getX()+.5,q.getY()+.2,q.getZ()+.5,1,0,0,0,0);}
   for(int v=F.vmin();v<=F.vmax();v++)for(int u:new int[]{0,kind.len-1}){var q=F.at(u,1,v);sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,q.getX()+.5,q.getY()+.2,q.getZ()+.5,1,0,0,0,0);}
   p.displayClientMessage(Component.literal("§e"+kind.label+": a área está marcada com brilhos verdes. §fClique de novo no mesmo lugar para construir §7(apaga o que estiver lá)"),true);
   return InteractionResult.CONSUME;
  }
 }
}
