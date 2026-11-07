package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Casino: 12 game machines (no power needed) played with casino chips. Chips are only for playing: they come from gold,
 *  emeralds and diamonds (crafting) and from winning, and buy nothing. All results are decided on the server. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class Casino {
 public enum Game {
  CRASH("Crash",0xFF2A6CFF),ROLETA("Roleta",0xFFC0182A),BLACKJACK("Blackjack",0xFF1F7A3A),PLINKO("Plinko",0xFFE0A020),MINES("Mines",0xFF3AA0A0),
  DICE("Dice",0xFF8A40D0),LIMBO("Limbo",0xFFFF6A20),KENO("Keno",0xFF2A9AE0),BACCARAT("Baccarat",0xFF8A1F2A),WHEEL("Wheel",0xFFE8C020),
  HILO("Hi-Lo",0xFF40B060),ANDAR_BAHAR("Andar Bahar",0xFFD04A90);
  public final String label;public final int color;
  Game(String l,int c){label=l;color=c;}
  public String id(){return name().toLowerCase(Locale.ROOT);}
 }
 public static final DeferredItem<Item> CHIP=MareEletrica.ITEMS.register("casino_chip",()->new Item(new Item.Properties().stacksTo(64)));
 public static final Map<Game,DeferredBlock<MachineBlock>> BLOCKS=new EnumMap<>(Game.class);
 public static final Map<Game,DeferredItem<BlockItem>> ITEMS=new EnumMap<>(Game.class);
 static{for(var g:Game.values()){var b=MareEletrica.BLOCKS.register("casino_"+g.id(),()->new MachineBlock(BlockBehaviour.Properties.of().strength(2.5F,6F).sound(SoundType.METAL).noOcclusion().lightLevel(s->7),g));
  BLOCKS.put(g,b);ITEMS.put(g,MareEletrica.ITEMS.register("casino_"+g.id(),()->new BlockItem(b.get(),new Item.Properties())));}}
 public static Consumer<State> openScreen=s->{};
 public static final int MAX_BET=1000;
 private Casino(){}
 public static void register(){}

 // ------------------------------------------------------------------ the machine block
 public static final class MachineBlock extends HorizontalDirectionalBlock implements Multiblock.Master {
  public static final MapCodec<MachineBlock> CODEC=simpleCodec(p->new MachineBlock(p,Game.CRASH));
  static final double[][] BOXES={{1,0,2,15,30,15}};
  public final Game game;
  public MachineBlock(BlockBehaviour.Properties p,Game g){super(p);game=g;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public List<BlockPos> cells(BlockState s){return Multiblock.cellsOf(s.getValue(FACING),BOXES);}
  @Override public VoxelShape cellShape(BlockState s,BlockPos o){return Multiblock.slice(s.getValue(FACING),BOXES,o);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var st=defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());return Multiblock.fits(c,st)?st:null;}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,ItemStack st){Multiblock.place(l,p,s);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock()))Multiblock.clear(l,p,s);super.onRemove(s,l,p,n,moving);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return cellShape(s,BlockPos.ZERO);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(player instanceof ServerPlayer sp){var se=SESSIONS.get(sp.getUUID());if(se!=null&&!se.pos.equals(p))se.leave(sp);se=new Session(game,p);SESSIONS.put(sp.getUUID(),se);se.send(sp,true);}
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }

 // ------------------------------------------------------------------ chips in the inventory
 public static int chips(Player p){int n=0;for(var s:p.getInventory().items)if(s.is(CHIP.get()))n+=s.getCount();return n;}
 static boolean take(Player p,int n){if(p.isCreative()&&chips(p)<n)return true;if(chips(p)<n)return false;
  for(var s:p.getInventory().items)if(s.is(CHIP.get())&&n>0){int t=Math.min(n,s.getCount());s.shrink(t);n-=t;}return true;}
 static void give(Player p,int n){while(n>0){int k=Math.min(64,n);var st=new ItemStack(CHIP.get(),k);if(!p.getInventory().add(st))p.drop(st,false);n-=k;}}

 // ------------------------------------------------------------------ sessions (one game per player)
 static final Map<UUID,Session> SESSIONS=new HashMap<>();
 static final Random R=new Random();
 static int card(){return R.nextInt(52);}            // rank = c % 13 (0 = A ... 12 = K), suit = c / 13
 static int bjValue(int[] cards,int n){int v=0,aces=0;for(int i=0;i<n;i++){int r=cards[i]%13;if(r==0){aces++;v+=11;}else v+=Math.min(10,r+1);}while(v>21&&aces-->0)v-=10;return v;}
 static int bacValue(List<Integer> cards){int v=0;for(int c:cards){int r=c%13;v+=r>=9?0:r+1;}return v%10;}

 public static final class Session {
  final Game game;final BlockPos pos;int bet=10,phase;   // 0 betting, 1 playing, 2 result
  String msg="";int win;CompoundTag d=new CompoundTag();
  // crash
  double crashAt,mult;int ticks;int autoOut;
  // blackjack
  int[] ph=new int[12],dh=new int[12];int pn,dn;
  // mines
  boolean[] mine=new boolean[25],open=new boolean[25];int mines,safe;
  // hi-lo
  int cur;double hlMult;int steps;
  static final List<Double> CRASH_HISTORY=new ArrayList<>();
  Session(Game g,BlockPos p){game=g;pos=p;}

  CompoundTag tag(ServerPlayer p){
   var t=d.copy();t.putString("Game",game.name());t.putInt("Bet",bet);t.putInt("Phase",phase);t.putString("Msg",msg);t.putInt("Win",win);t.putInt("Chips",chips(p));
   switch(game){
    case CRASH->{t.putDouble("Mult",mult);t.putInt("Ticks",ticks);if(phase==2)t.putDouble("CrashAt",crashAt);
     var h=new int[CRASH_HISTORY.size()];for(int i=0;i<h.length;i++)h[i]=(int)Math.round(CRASH_HISTORY.get(i)*100);t.putIntArray("History",h);}
    case BLACKJACK->{t.putIntArray("P",Arrays.copyOf(ph,pn));t.putIntArray("D",phase==1?new int[]{dh[0]}:Arrays.copyOf(dh,dn));t.putInt("PV",bjValue(ph,pn));t.putInt("DV",phase==1?bjValue(new int[]{dh[0]},1):bjValue(dh,dn));}
    case MINES->{var o=new int[25];for(int i=0;i<25;i++)o[i]=open[i]?(mine[i]?2:1):(phase==2&&mine[i]?3:0);t.putIntArray("Board",o);t.putInt("Mines",mines);t.putDouble("Mult",minesMult());}
    case HILO->{t.putInt("Card",cur);t.putDouble("Mult",hlMult);t.putInt("Steps",steps);}
    default->{}
   }
   return t;
  }
  void send(ServerPlayer p,boolean open){PacketDistributor.sendToPlayer(p,new State(pos,tag(p),open));}
  double minesMult(){double m=.97;for(int i=0;i<safe;i++)m*=(25.0-i)/(25.0-mines-i);return safe==0?1:m;}

  boolean pay(ServerPlayer p){if(bet<1){msg="§cAposta mínima: 1 ficha";return false;}if(!take(p,bet)){msg="§cFichas insuficientes";return false;}win=0;return true;}
  void result(ServerPlayer p,int amount,String text){
   phase=2;win=amount;msg=text;if(amount>0)give(p,amount);
   var l=p.level();
   if(amount>bet){l.playSound(null,p.blockPosition(),amount>=bet*10?SoundEvents.UI_TOAST_CHALLENGE_COMPLETE:SoundEvents.PLAYER_LEVELUP,SoundSource.PLAYERS,.8F,1.2F);
    ((ServerLevel)l).sendParticles(ParticleTypes.HAPPY_VILLAGER,pos.getX()+.5,pos.getY()+1.6,pos.getZ()+.5,10,.4,.3,.4,0);}
   else if(amount==0)l.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_BASS.value(),SoundSource.PLAYERS,.8F,.6F);
   if(amount>=bet*50&&bet>=10)for(var o:((ServerLevel)l).players())if(o.distanceToSqr(p)<64*64)o.sendSystemMessage(Component.literal("§6🎰 "+p.getName().getString()+" ganhou "+amount+" fichas no "+game.label+"!"));
  }

  /** a: action, x/y: parameters. 0 set bet, 1 play/start, 2/3/4 game actions, 9 leave. */
  void act(ServerPlayer p,int a,int x,int y){
   if(a==0){if(phase!=1)bet=Math.max(1,Math.min(MAX_BET,x));send(p,false);return;}
   if(a==9){leave(p);return;}
   switch(game){
    case CRASH->crash(p,a,x);
    case ROLETA->{if(a!=1||!pay(p))break;roleta(p,x,y);}
    case BLACKJACK->blackjack(p,a);
    case PLINKO->{if(a!=1||!pay(p))break;plinko(p,x);}
    case MINES->mines(p,a,x);
    case DICE->{if(a!=1||!pay(p))break;dice(p,x,y);}
    case LIMBO->{if(a!=1||!pay(p))break;limbo(p,x);}
    case KENO->{if(a!=1)break;keno(p,x,y);}
    case BACCARAT->{if(a!=1||!pay(p))break;baccarat(p,x);}
    case WHEEL->{if(a!=1||!pay(p))break;wheel(p,x);}
    case HILO->hilo(p,a);
    case ANDAR_BAHAR->{if(a!=1||!pay(p))break;andar(p,x);}
   }
   send(p,false);
  }
  /** Leaving in the middle of a game cashes out what can be cashed out. */
  void leave(ServerPlayer p){
   if(phase==1){
    switch(game){case CRASH->result(p,(int)Math.floor(bet*mult),"Saiu em "+fmt(mult)+"x");case MINES->{if(safe>0)result(p,(int)Math.floor(bet*minesMult()),"Retirou");else result(p,bet,"Devolvido");}
     case HILO->result(p,steps>0?(int)Math.floor(bet*hlMult):bet,steps>0?"Retirou":"Devolvido");case BLACKJACK->blackjack(p,3);default->{}}
   }
   SESSIONS.remove(p.getUUID());
  }
  static String fmt(double m){return String.format(Locale.ROOT,"%.2f",m);}

  // ---- Crash: the multiplier climbs until the rocket blows up; cash out before it does
  void crash(ServerPlayer p,int a,int x){
   if(a==1&&phase!=1){if(!pay(p))return;phase=1;ticks=0;mult=1;autoOut=x;double u=R.nextDouble();crashAt=Math.max(1,Math.floor(.97/(1-u)*100)/100);msg="O foguete subiu!";if(crashAt<1.01)crashTick(p);}
   else if((a==2||a==4)&&phase==1)result(p,(int)Math.floor(bet*mult),"§aRetirou em "+fmt(mult)+"x!");
  }
  void crashTick(ServerPlayer p){
   ticks++;mult=Math.floor(Math.exp(.0035*ticks*1.6)*100)/100;
   if(mult>=crashAt){mult=crashAt;CRASH_HISTORY.add(0,crashAt);while(CRASH_HISTORY.size()>12)CRASH_HISTORY.remove(12);result(p,0,"§c💥 Explodiu em "+fmt(crashAt)+"x");send(p,false);return;}
   if(autoOut>100&&mult*100>=autoOut){result(p,(int)Math.floor(bet*mult),"§aSaída automática em "+fmt(mult)+"x!");send(p,false);return;}
   if(ticks%2==0)send(p,false);
  }
  // ---- Roulette (European, single zero)
  public static final Set<Integer> RED=Set.of(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36);
  void roleta(ServerPlayer p,int type,int num){
   int n=R.nextInt(37);boolean red=RED.contains(n);
   double m=switch(type){
    case 0->n==num?36:0;case 1->n!=0&&red?2:0;case 2->n!=0&&!red?2:0;case 3->n!=0&&n%2==0?2:0;case 4->n%2==1?2:0;
    case 5->n>=1&&n<=18?2:0;case 6->n>=19?2:0;case 7->n!=0&&(n-1)/12==num-1?3:0;default->0;};
   d.putInt("Result",n);d.putInt("Spin",d.getInt("Spin")+1);
   result(p,(int)Math.floor(bet*m),(m>0?"§a":"§c")+"Caiu no "+n+(n==0?" (verde)":red?" (vermelho)":" (preto)")+(m>0?" — ganhou "+(int)Math.floor(bet*m)+"!":" — perdeu"));
  }
  // ---- Blackjack
  void blackjack(ServerPlayer p,int a){
   if(a==1&&phase!=1){if(!pay(p))return;phase=1;pn=dn=0;ph[pn++]=card();dh[dn++]=card();ph[pn++]=card();dh[dn++]=card();msg="Pedir ou parar?";
    if(bjValue(ph,pn)==21){dealer();int dv=bjValue(dh,dn);if(dv==21&&dn==2)result(p,bet,"Os dois fizeram Blackjack: empate");else result(p,(int)Math.floor(bet*2.5),"§6§lBLACKJACK!");}return;}
   if(phase!=1)return;
   if(a==2){ph[pn++]=card();if(bjValue(ph,pn)>21){result(p,0,"§cEstourou! ("+bjValue(ph,pn)+")");}else if(pn>=11)a=3;}
   if(a==4&&pn==2){if(!take(p,bet)){msg="§cFichas insuficientes para dobrar";return;}bet*=2;ph[pn++]=card();if(bjValue(ph,pn)>21){result(p,0,"§cEstourou! ("+bjValue(ph,pn)+")");bet/=2;return;}a=3;}
   if(a==3){dealer();int pv=bjValue(ph,pn),dv=bjValue(dh,dn);
    if(dv>21||pv>dv)result(p,bet*2,"§aVocê ganhou! "+pv+" x "+dv);else if(pv==dv)result(p,bet,"Empate: "+pv+" x "+dv);else result(p,0,"§cA banca ganhou: "+dv+" x "+pv);
    if(d.getBoolean("Doubled")){}}
  }
  void dealer(){while(bjValue(dh,dn)<17&&dn<11)dh[dn++]=card();}
  // ---- Plinko: 12 rows; the ball's left/right bounces decide the slot
  public static final double[][] PLINKO={{10,3,1.6,1.4,1.1,1,.5,1,1.1,1.4,1.6,3,10},{33,11,4,2,1.1,.6,.3,.6,1.1,2,4,11,33},{170,24,8.1,2,.7,.2,.2,.2,.7,2,8.1,24,170}};
  void plinko(ServerPlayer p,int risk){
   risk=Math.max(0,Math.min(2,risk));int path=0,slot=0;for(int i=0;i<12;i++)if(R.nextBoolean()){path|=1<<i;slot++;}
   double m=PLINKO[risk][slot];d.putInt("Path",path);d.putInt("Slot",slot);d.putInt("Risk",risk);d.putInt("Drop",d.getInt("Drop")+1);
   result(p,(int)Math.floor(bet*m),(m>=1?"§a":"§c")+fmt(m)+"x");
  }
  // ---- Mines: 5x5, open safe tiles, cash out any time
  void mines(ServerPlayer p,int a,int x){
   if(a==1&&phase!=1){if(!pay(p))return;phase=1;mines=Math.max(1,Math.min(24,x));Arrays.fill(mine,false);Arrays.fill(open,false);safe=0;
    for(int k=0;k<mines;){int i=R.nextInt(25);if(!mine[i]){mine[i]=true;k++;}}msg="Escolha um quadrado";return;}
   if(phase!=1)return;
   if(a==2&&x>=0&&x<25&&!open[x]){open[x]=true;
    if(mine[x]){result(p,0,"§c💣 BOOM! Era uma mina");return;}
    safe++;msg="§a💎 "+fmt(minesMult())+"x — continue ou retire";if(safe==25-mines)result(p,(int)Math.floor(bet*minesMult()),"§6Abriu tudo! "+fmt(minesMult())+"x");}
   if(a==4&&safe>0)result(p,(int)Math.floor(bet*minesMult()),"§aRetirou "+fmt(minesMult())+"x!");
  }
  // ---- Dice: x = chance in %, y = 0 roll under / 1 roll over
  void dice(ServerPlayer p,int chance,int over){
   chance=Math.max(2,Math.min(95,chance));int roll=R.nextInt(10000);
   boolean w=over==0?roll<chance*100:roll>=10000-chance*100;double m=99.0/chance;
   d.putInt("Roll",roll);d.putInt("Rolls",d.getInt("Rolls")+1);
   result(p,w?(int)Math.floor(bet*m):0,(w?"§a":"§c")+"Saiu "+String.format(Locale.ROOT,"%.2f",roll/100.0)+(w?" — ganhou "+fmt(m)+"x":" — perdeu"));
  }
  // ---- Limbo: x = target multiplier x100
  void limbo(ServerPlayer p,int target){
   double t=Math.max(1.01,Math.min(1000,target/100.0));double u=R.nextDouble();double res=Math.max(1,Math.floor(.99/(1-u)*100)/100);
   d.putDouble("Result",res);d.putInt("Rolls",d.getInt("Rolls")+1);
   boolean w=res>=t;result(p,w?(int)Math.floor(bet*t):0,(w?"§a":"§c")+fmt(res)+"x"+(w?" — ganhou!":" — perdeu"));
  }
  // ---- Keno: pick up to 10 of 40, 10 are drawn
  static final double[][] KENO={{},{0,3.9},{0,1.8,5},{0,1,3,10},{0,0,2,5,30},{0,0,1.5,4,12,70},{0,0,1,2,7,25,150},{0,0,1,1.5,4,12,60,300},{0,0,0,1.5,3,8,30,150,600},{0,0,0,1,2,5,15,60,300,1000},{0,0,0,1,1.5,3,8,30,120,500,1000}};
  void keno(ServerPlayer p,int lo,int hi){
   long mask=(lo&0xFFFFFFFFL)|((long)(hi&0xFF)<<32);int picks=Long.bitCount(mask);
   if(picks<1||picks>10){msg="§cEscolha de 1 a 10 números";return;}
   if(!pay(p))return;
   var all=new ArrayList<Integer>();for(int i=0;i<40;i++)all.add(i);Collections.shuffle(all,R);
   int[] drawn=new int[10];int hits=0;for(int i=0;i<10;i++){drawn[i]=all.get(i);if((mask>>drawn[i]&1)!=0)hits++;}
   d.putIntArray("Drawn",drawn);d.putInt("Hits",hits);d.putInt("Draws",d.getInt("Draws")+1);
   double m=KENO[picks][hits];result(p,(int)Math.floor(bet*m),(m>0?"§a":"§c")+hits+" acerto"+(hits==1?"":"s")+" — "+fmt(m)+"x");
  }
  // ---- Baccarat: x = 0 Player, 1 Banker, 2 Tie (real drawing rules)
  void baccarat(ServerPlayer p,int side){
   var P=new ArrayList<Integer>(List.of(card(),card()));var B=new ArrayList<Integer>(List.of(card(),card()));
   int pv=bacValue(P),bv=bacValue(B);
   if(pv<8&&bv<8){
    Integer third=null;if(pv<=5){third=card();P.add(third);}
    bv=bacValue(B);
    if(third==null){if(bv<=5)B.add(card());}
    else{int t=third%13>=9?0:third%13+1;
     boolean draw=bv<=2||bv==3&&t!=8||bv==4&&t>=2&&t<=7||bv==5&&t>=4&&t<=7||bv==6&&(t==6||t==7);if(draw)B.add(card());}
   }
   pv=bacValue(P);bv=bacValue(B);int winner=pv>bv?0:bv>pv?1:2;
   d.putIntArray("PC",P.stream().mapToInt(i->i).toArray());d.putIntArray("BC",B.stream().mapToInt(i->i).toArray());d.putInt("Deals",d.getInt("Deals")+1);
   String who=winner==0?"Jogador":winner==1?"Banca":"Empate";
   int pay=side==winner?(int)Math.floor(bet*(side==0?2:side==1?1.95:9)):winner==2?bet:0;
   result(p,pay,(side==winner?"§a":"§c")+who+" ("+pv+" x "+bv+")"+(side==winner?" — ganhou!":winner==2?" — aposta devolvida":" — perdeu"));
  }
  // ---- Wheel: 30 segments, x = risk
  public static final double[][] WHEEL={{1.5,1.2,1.2,1.2,0,1.2,1.2,1.2,1.2,0,1.5,1.2,1.2,1.2,0,1.2,1.2,1.2,1.2,0,1.5,1.2,1.2,1.2,0,1.2,1.2,1.2,1.2,0},
   {0,1.5,0,2,0,1.5,0,1.7,0,3,0,1.5,0,2,0,1.5,0,1.7,0,3,0,1.5,0,2,0,1.5,0,1.7,0,4},
   {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,29.7}};
  void wheel(ServerPlayer p,int risk){
   risk=Math.max(0,Math.min(2,risk));int i=R.nextInt(30);double m=WHEEL[risk][i];
   d.putInt("Seg",i);d.putInt("Risk",risk);d.putInt("Spin",d.getInt("Spin")+1);
   result(p,(int)Math.floor(bet*m),(m>=1?"§a":"§c")+fmt(m)+"x");
  }
  // ---- Hi-Lo: guess if the next card is higher or lower (ties count for you)
  void hilo(ServerPlayer p,int a){
   if(a==1&&phase!=1){if(!pay(p))return;phase=1;cur=card();hlMult=1;steps=0;msg="Maior ou menor?";return;}
   if(phase!=1)return;
   if(a==2||a==3){int r=cur%13;double prob=a==2?(13-r)/13.0:(r+1)/13.0;int next=card();int nr=next%13;
    boolean ok=a==2?nr>=r:nr<=r;d.putInt("Prev",cur);cur=next;
    if(!ok){result(p,0,"§cErrou! Saiu "+rank(nr));return;}
    steps++;hlMult*=.97/prob;msg="§aAcertou! "+fmt(hlMult)+"x — continue ou retire";}
   if(a==4&&steps>0)result(p,(int)Math.floor(bet*hlMult),"§aRetirou "+fmt(hlMult)+"x!");
  }
  static String rank(int r){return switch(r){case 0->"A";case 10->"J";case 11->"Q";case 12->"K";default->String.valueOf(r+1);};}
  // ---- Andar Bahar: x = 0 Andar, 1 Bahar
  void andar(ServerPlayer p,int side){
   int joker=card();var A=new ArrayList<Integer>();var B=new ArrayList<Integer>();int winner;
   for(int i=0;;i++){int c=card();if(i%2==0)A.add(c);else B.add(c);if(c%13==joker%13){winner=i%2;break;}}
   d.putInt("Joker",joker);d.putIntArray("A",A.stream().mapToInt(i->i).toArray());d.putIntArray("B",B.stream().mapToInt(i->i).toArray());d.putInt("Deals",d.getInt("Deals")+1);
   boolean w=side==winner;result(p,w?(int)Math.floor(bet*(side==0?1.9:2)):0,(w?"§a":"§c")+(winner==0?"Andar":"Bahar")+" venceu"+(w?" — ganhou!":" — perdeu"));
  }
 }

 @SubscribeEvent public static void tick(ServerTickEvent.Post e){
  if(SESSIONS.isEmpty())return;
  for(var it=SESSIONS.entrySet().iterator();it.hasNext();){
   var en=it.next();var p=e.getServer().getPlayerList().getPlayer(en.getKey());var s=en.getValue();
   if(p==null||p.distanceToSqr(s.pos.getCenter())>12*12){if(p!=null&&s.phase==1)s.leave(p);it.remove();continue;}
   if(s.game==Game.CRASH&&s.phase==1)s.crashTick(p);
  }
 }

 public record State(BlockPos pos,CompoundTag data,boolean open) implements CustomPacketPayload {
  public static final Type<State> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"casino_state"));
  public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,State::pos,ByteBufCodecs.COMPOUND_TAG,State::data,ByteBufCodecs.BOOL,State::open,State::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Action(int action,int x,int y) implements CustomPacketPayload {
  public static final Type<Action> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"casino_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Action::action,ByteBufCodecs.INT,Action::x,ByteBufCodecs.INT,Action::y,Action::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
   var r=e.registrar("19");
   r.playToClient(State.TYPE,State.CODEC,(p,c)->openScreen.accept(p));
   r.playToServer(Action.TYPE,Action.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer sp){var s=SESSIONS.get(sp.getUUID());if(s!=null)s.act(sp,p.action(),p.x(),p.y());}});
  }
 }
}
