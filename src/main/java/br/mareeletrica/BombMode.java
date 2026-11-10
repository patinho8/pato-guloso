package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 0.83.0: "Modo Bomba" — a small Counter-Strike: a desert map (two bomb sites, mid, long, tunnels),
 * TR vs CT with bots, rounds, the bomb (plant 3 s inside a site, 40 s fuse, defuse 5/10 s), first to 7.
 */
public final class BombMode {
 private BombMode(){}
 public static final DeferredBlock<TerminalBlock> TERMINAL=MareEletrica.BLOCKS.register("bomb_terminal",()->new TerminalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(-1F,3600000F).sound(net.minecraft.world.level.block.SoundType.METAL).lightLevel(s->7)));
 public static final DeferredItem<BlockItem> TERMINAL_ITEM=MareEletrica.ITEMS.register("bomb_terminal",()->new BlockItem(TERMINAL.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Terminal>> TERMINAL_ENTITY=MareEletrica.ENTITIES.register("bomb_terminal",()->BlockEntityType.Builder.of(Terminal::new,TERMINAL.get()).build(null));
 public static final DeferredItem<MapItem> MAP=MareEletrica.ITEMS.register("bomb_map",()->new MapItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<BombItem> BOMB=MareEletrica.ITEMS.register("match_bomb",()->new BombItem(new Item.Properties().stacksTo(1)));
 public static final Supplier<EntityType<BombBot>> BOT=MareEletrica.ACTORS.register("bomb_bot",()->EntityType.Builder.<BombBot>of(BombBot::new,MobCategory.MISC).sized(.6F,1.8F).eyeHeight(1.62F).clientTrackingRange(10).build(MareEletrica.ID+":bomb_bot"));

 public static void register(IEventBus bus){
  bus.addListener((EntityAttributeCreationEvent e)->e.put(BOT.get(),BombBot.attributes().build()));
  NeoForge.EVENT_BUS.addListener(BombMode::death);NeoForge.EVENT_BUS.addListener(BombMode::damage);NeoForge.EVENT_BUS.addListener(BombMode::logout);
 }
 public static void creative(java.util.function.Consumer<ItemStack> out){out.accept(new ItemStack(MAP.get()));out.accept(new ItemStack(TERMINAL_ITEM.get()));}

 // ======================================================= map (40 x 30 cells, 2 blocks each)
 static final int CELL=2,W=40,H=30,WALL=6;
 /** open areas {x0,z0,x1,z1} in cells */
 static final int[][] OPEN={
  {1,1,11,9},{14,1,25,5},{28,1,38,9},           // B site, CT spawn, A site
  {11,3,14,4},{25,3,28,4},                      // CT → B, CT → A
  {18,5,21,22},                                 // mid
  {21,12,30,13},{29,9,30,13},                   // catwalk to A
  {8,14,18,15},{8,9,9,15},                      // window to B
  {33,10,38,24},{1,10,6,24},                    // long A, B tunnels
  {12,24,27,28},{27,24,33,25},{6,24,12,25},{18,22,21,24}};   // TR spawn and its exits
 static final int[] SITE_A={28,1,38,9},SITE_B={1,1,11,9},SPAWN_CT={14,1,25,5},SPAWN_T={12,24,27,28};
 /** crates {x,z,height} */
 static final int[][] CRATES={{31,5,2},{32,5,1},{35,3,2},{30,8,1},{36,7,1},{4,4,2},{5,4,1},{8,7,2},{3,7,1},{19,12,1},{20,17,2},{35,17,2},{36,13,1},{3,18,2},{4,13,1},{15,26,1},{24,26,1},{23,3,1},{16,3,1},{10,14,1},{27,12,1}};
 static final int[] TERMINAL_CELL={13,27};
 static boolean open(int gx,int gz){for(var r:OPEN)if(gx>=r[0]&&gx<=r[2]&&gz>=r[1]&&gz<=r[3])return true;return false;}
 static boolean in(int[] r,int gx,int gz){return gx>=r[0]&&gx<=r[2]&&gz>=r[1]&&gz<=r[3];}
 static int crate(int gx,int gz){for(var c:CRATES)if(c[0]==gx&&c[1]==gz)return c[2];return 0;}
 static final String[] LETTER_A={".###.","#...#","#####","#...#","#...#"},LETTER_B={"####.","#...#","####.","#...#","####."};

 /** builds the arena with its NW corner at o (o.y = floor level of the players' feet) */
 public static BlockPos build(ServerLevel l,BlockPos o){
  var floor=Blocks.SMOOTH_SANDSTONE.defaultBlockState();var wall=Blocks.SANDSTONE.defaultBlockState();var stripe=Blocks.CUT_SANDSTONE.defaultBlockState();var cap=Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState();
  var siteFloor=Blocks.CUT_RED_SANDSTONE.defaultBlockState();var ctFloor=Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();var tFloor=Blocks.YELLOW_TERRACOTTA.defaultBlockState();
  int flags=2|16;
  for(int x=-1;x<=W*CELL;x++)for(int z=-1;z<=H*CELL;z++){
   int gx=Math.floorDiv(x,CELL),gz=Math.floorDiv(z,CELL);boolean inside=x>=0&&z>=0&&x<W*CELL&&z<H*CELL;boolean op=inside&&open(gx,gz);
   var base=o.offset(x,0,z);
   l.setBlock(base.below(2),Blocks.SANDSTONE.defaultBlockState(),flags);
   l.setBlock(base.below(),!op?floor:in(SITE_A,gx,gz)||in(SITE_B,gx,gz)?siteFloor:in(SPAWN_CT,gx,gz)?ctFloor:in(SPAWN_T,gx,gz)?tFloor:floor,flags);
   for(int y=0;y<12;y++){
    BlockState s=net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    if(!op&&y<WALL)s=y==2||y==4?stripe:wall;else if(!op&&y==WALL)s=cap;
    else if(op){int c=crate(gx,gz);if(c>0&&y<c*CELL)s=(gx+gz+y)%3==0?Blocks.SPRUCE_PLANKS.defaultBlockState():Blocks.OAK_PLANKS.defaultBlockState();}
    l.setBlock(base.above(y),s,flags);
   }
  }
  // big A / B painted on the sites
  paint(l,o,SITE_A,LETTER_A);paint(l,o,SITE_B,LETTER_B);
  // lanterns along the walls of the corridors
  for(int gx=0;gx<W;gx+=3)for(int gz=0;gz<H;gz+=3)if(open(gx,gz)&&crate(gx,gz)==0){var p=o.offset(gx*CELL,3,gz*CELL);for(var d:net.minecraft.core.Direction.Plane.HORIZONTAL){var w=p.relative(d);if(!l.getBlockState(w).isAir()&&l.getBlockState(p).isAir()){l.setBlock(p,Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING,false),flags);break;}}}
  var term=o.offset(TERMINAL_CELL[0]*CELL,0,TERMINAL_CELL[1]*CELL);
  l.setBlock(term,TERMINAL.get().defaultBlockState(),3);
  if(l.getBlockEntity(term) instanceof Terminal t){t.origin=o;t.setChanged();}
  return term;
 }
 static void paint(ServerLevel l,BlockPos o,int[] site,String[] letter){
  int cx=(site[0]+site[2]+1)*CELL/2,cz=(site[1]+site[3]+1)*CELL/2;var red=Blocks.RED_CONCRETE.defaultBlockState();
  for(int r=0;r<5;r++)for(int c=0;c<5;c++)if(letter[r].charAt(c)=='#')for(int dx=0;dx<2;dx++)for(int dz=0;dz<2;dz++)l.setBlock(o.offset(cx-5+c*2+dx,-1,cz-5+r*2+dz),red,2);
 }

 // ======================================================= teams
 /** players in a running match: uuid → team (0 TR, 1 CT) */
 static final Map<UUID,Integer> TEAMS=new HashMap<>();
 static final Map<UUID,BlockPos> PLAYER_MATCH=new HashMap<>();
 public static int team(Entity e){if(e instanceof BombBot b)return b.team();if(e instanceof Player p){var t=TEAMS.get(p.getUUID());return t==null?-1:t;}return -1;}
 public static boolean sameTeam(Entity a,Entity b){int x=team(a),y=team(b);return x>=0&&x==y;}
 public static boolean enemies(BombBot bot,LivingEntity e){int t=team(e);if(t<0||t==bot.team())return false;return !(e instanceof Player p)||!p.isSpectator()&&!p.isCreative();}

 // ======================================================= items
 public static final class MapItem extends Item {
  public MapItem(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext c){
   if(!(c.getLevel() instanceof ServerLevel l)||c.getPlayer()==null)return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
   var p=c.getPlayer();var at=c.getClickedPos().above();
   // the player ends up inside the TR spawn
   var o=at.offset(-19*CELL,0,-26*CELL);
   var term=build(l,o);
   p.teleportTo(term.getX()+3.5,term.getY(),term.getZ()+.5);
   p.displayClientMessage(Component.literal("§6💣 Mapa \"Deserto\" construído! §7Clique no terminal para escolher o time, Shift+clique para começar."),false);
   l.playSound(null,at,SoundEvents.ANVIL_USE,SoundSource.BLOCKS,1F,.8F);
   if(!p.isCreative())c.getItemInHand().shrink(1);
   return InteractionResult.SUCCESS;
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Clique num bloco para construir o mapa \"Deserto\" (80×60) do Modo Bomba."));
   t.add(Component.literal("§cSubstitui tudo o que estiver na área!"));}
 }
 /** the round's bomb: hold right-click for 3 s inside a bomb site */
 public static final class BombItem extends Item {
  public BombItem(Properties p){super(p);}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 60;}
  @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.BOW;}
  @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
   var m=active(l);
   if(m==null||m.inSite(p.blockPosition())==0||m.bombPos()!=null){if(!l.isClientSide)p.displayClientMessage(Component.literal("§eLeve a bomba até o bombsite §cA§e ou §cB§e para plantar."),true);return InteractionResultHolder.fail(p.getItemInHand(h));}
   p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
  @Override public void onUseTick(Level l,LivingEntity e,ItemStack s,int left){
   if(l.isClientSide||!(e instanceof ServerPlayer p))return;int done=60-left;
   if(done%10==0)l.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_HAT.value(),SoundSource.PLAYERS,.7F,1.2F+done/60F);
   p.displayClientMessage(Component.literal("§c💣 Plantando "+"█".repeat(done/6)+"§8"+"█".repeat(10-done/6)),true);
   var m=active(l);if(m==null||m.inSite(p.blockPosition())==0)p.stopUsingItem();}
  @Override public ItemStack finishUsingItem(ItemStack s,Level l,LivingEntity e){
   var m=active(l);if(!l.isClientSide&&m!=null&&e instanceof ServerPlayer p&&m.inSite(p.blockPosition())!=0){m.plant(p,p.blockPosition());return ItemStack.EMPTY;}
   return s;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Segure o botão direito por 3 s dentro do bombsite A ou B."));}
 }

 // ======================================================= terminal
 public static final class TerminalBlock extends BaseEntityBlock {
  public static final MapCodec<TerminalBlock> CODEC=simpleCodec(TerminalBlock::new);
  public TerminalBlock(BlockBehaviour.Properties p){super(p);}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Terminal(p,s);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,TERMINAL_ENTITY.get(),Terminal::tick);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   if(!l.isClientSide&&l.getBlockEntity(pos) instanceof Terminal t&&p instanceof ServerPlayer sp)t.use(sp);
   return InteractionResult.sidedSuccess(l.isClientSide);}
  @Override public boolean onDestroyedByPlayer(BlockState s,Level l,BlockPos pos,Player p,boolean h,net.minecraft.world.level.material.FluidState f){
   if(l.getBlockEntity(pos) instanceof Terminal t&&t.running()){if(!l.isClientSide)p.displayClientMessage(Component.literal("§cTermine a partida antes (Shift+clique no terminal)."),true);return false;}
   return super.onDestroyedByPlayer(s,l,pos,p,h,f);}
 }
 static BlockPos activePos;
 public static Terminal active(Level l){return activePos!=null&&l.getBlockEntity(activePos) instanceof Terminal t&&t.running()?t:null;}

 public static final class Terminal extends BlockEntity {
  public BlockPos origin;
  int choice=1,bots=5;           // the clicking player's team, bots per side (5v5)
  boolean running;int phase,timer,round,scoreT,scoreCT,site,lastWinner=-1;BlockPos bomb;
  final Map<UUID,Integer> players=new LinkedHashMap<>();
  ServerBossEvent bar;List<String> names=new ArrayList<>();
  static final int FREEZE=100,ROUND=2300,AFTER=120,WIN=7,FUSE=800;
  static final String[] NAMES={"Pato","Marreco","Ganso","Cisne","Frango","Galo","Peru","Tucano","Arara","Pombo","Coruja","Gavião"};
  public Terminal(BlockPos p,BlockState s){super(TERMINAL_ENTITY.get(),p,s);}
  public boolean running(){return running;}
  public boolean frozen(){return running&&(phase==0||phase==2);}
  public BlockPos bombPos(){return bomb;}
  ServerLevel sl(){return (ServerLevel)level;}

  void use(ServerPlayer p){
   if(origin==null){p.displayClientMessage(Component.literal("§cEste terminal não tem mapa. Use o item \"Mapa Modo Bomba\"."),true);return;}
   if(p.isShiftKeyDown()){if(running)end("§7Partida encerrada.");else start(p);return;}
   if(running){p.displayClientMessage(Component.literal("§7Partida em andamento: §bCT "+scoreCT+" §7× §6"+scoreT+" TR§7 — Shift+clique encerra."),true);return;}
   if(p.getMainHandItem().isEmpty()){choice=1-choice;}else bots=bots%5+1;
   p.displayClientMessage(Component.literal("§7Seu time: "+(choice==1?"§bCT (contra-terroristas)":"§6TR (terroristas)")+" §7· "+bots+"x"+bots+" com bots §8(mão vazia: troca o time · item na mão: muda o tamanho) §7· Shift+clique: começar"),true);
  }
  public void start(ServerPlayer starter){
   if(active(level)!=null){starter.displayClientMessage(Component.literal("§cJá tem uma partida rolando em outro mapa."),true);return;}
   players.clear();players.put(starter.getUUID(),choice);int next=1-choice;
   for(var p:sl().players())if(p!=starter&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<16*16){players.put(p.getUUID(),next);next=1-next;}
   running=true;round=0;scoreT=0;scoreCT=0;activePos=worldPosition;
   for(var e:players.entrySet()){TEAMS.put(e.getKey(),e.getValue());PLAYER_MATCH.put(e.getKey(),worldPosition);}
   bar=new ServerBossEvent(Component.literal("Modo Bomba"),BossEvent.BossBarColor.YELLOW,BossEvent.BossBarOverlay.PROGRESS);
   for(var p:online())bar.addPlayer(p);
   broadcast("§6💣 MODO BOMBA §7— primeiro a §f"+WIN+"§7 rounds vence! TR: plante a bomba no §cA§7 ou §cB§7. CT: impeça ou desarme (tesoura = 5 s).");
   newRound();setChanged();
  }
  List<ServerPlayer> online(){var out=new ArrayList<ServerPlayer>();for(var id:players.keySet()){var p=sl().getServer().getPlayerList().getPlayer(id);if(p!=null)out.add(p);}return out;}
  void broadcast(String s){for(var p:online())p.sendSystemMessage(Component.literal(s));}
  void title(String t,String sub){for(var p:online()){p.connection.send(new ClientboundSetTitlesAnimationPacket(5,50,15));p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(t)));}}
  public List<BombBot> bots(){return origin==null?List.of():level.getEntitiesOfClass(BombBot.class,new net.minecraft.world.phys.AABB(origin).inflate(0).expandTowards(W*CELL,12,H*CELL).inflate(4),b->b.terminal.equals(worldPosition));}
  BlockPos block(int gx,int gz){return origin.offset(gx*CELL,0,gz*CELL);}
  BlockPos randomIn(int[] r){var rnd=level.random;for(int i=0;i<40;i++){int gx=r[0]+rnd.nextInt(r[2]-r[0]+1),gz=r[1]+rnd.nextInt(r[3]-r[1]+1);if(open(gx,gz)&&crate(gx,gz)==0)return block(gx,gz).offset(rnd.nextInt(CELL),0,rnd.nextInt(CELL));}return block((r[0]+r[2])/2,(r[1]+r[3])/2);}
  public int inSite(BlockPos p){if(origin==null)return 0;int gx=Math.floorDiv(p.getX()-origin.getX(),CELL),gz=Math.floorDiv(p.getZ()-origin.getZ(),CELL);if(Math.abs(p.getY()-origin.getY())>3)return 0;return in(SITE_A,gx,gz)?1:in(SITE_B,gx,gz)?2:0;}

  void newRound(){
   round++;phase=0;timer=FREEZE;bomb=null;site=1+level.random.nextInt(2);
   for(var b:bots())b.discard();
   clearBomb();
   int[] count={0,0};for(var t:players.values())count[t]++;
   var ps=online();
   for(var p:ps){int t=players.get(p.getUUID());p.setGameMode(GameType.ADVENTURE);p.setHealth(p.getMaxHealth());p.getFoodData().setFoodLevel(20);p.removeAllEffects();p.clearFire();
    var at=randomIn(t==1?SPAWN_CT:SPAWN_T);p.teleportTo(sl(),at.getX()+.5,at.getY(),at.getZ()+.5,t==1?180:0,0);kit(p,t);}
   names=new ArrayList<>(List.of(NAMES));Collections.shuffle(names,new Random(level.random.nextLong()));
   for(int t=0;t<2;t++)for(int i=count[t];i<bots;i++)spawnBot(t,i);
   // the bomb goes to a random terrorist
   var ts=new ArrayList<Object>();for(var p:ps)if(players.get(p.getUUID())==0)ts.add(p);for(var b:bots())if(!b.ct())ts.add(b);
   if(!ts.isEmpty())giveBomb(ts.get(level.random.nextInt(ts.size())));
   title("§fRound "+round,"§bCT "+scoreCT+" §7× §6"+scoreT+" TR");
   level.playSound(null,worldPosition,SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.BLOCKS,2F,1F);
  }
  void giveBomb(Object who){if(who instanceof BombBot b)b.carrier=true;else if(who instanceof ServerPlayer p){place(p,4,tagged(new ItemStack(BOMB.get())));p.displayClientMessage(Component.literal("§c💣 Você está com a bomba!"),false);}}
  void spawnBot(int team,int i){
   var b=BOT.get().create(level);if(b==null)return;var at=randomIn(team==1?SPAWN_CT:SPAWN_T);
   b.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,team==1?180:0,0);b.terminal=worldPosition;b.getEntityData().set(BombBot.TEAM,team);b.getEntityData().set(BombBot.SKIN,level.random.nextInt(9));
   b.botName=names.isEmpty()?NAMES[level.random.nextInt(NAMES.length)]:names.remove(0);b.setCustomName(Component.literal((team==1?"§b":"§6")+"BOT "+b.botName));b.setCustomNameVisible(false);
   boolean pistol=level.random.nextInt(5)==0;
   b.setItemSlot(EquipmentSlot.MAINHAND,gun(team==1?(pistol?Arsenal.USP.get():Arsenal.M4A1.get()):(pistol?Arsenal.GLOCK.get():Arsenal.AK47.get())));
   var vest=new ItemStack(Items.LEATHER_CHESTPLATE);vest.set(DataComponents.DYED_COLOR,new DyedItemColor(team==1?0x2B4C8C:0xB58A4C,false));b.setItemSlot(EquipmentSlot.CHEST,vest);
   if(team==1){var helmet=new ItemStack(Items.LEATHER_HELMET);helmet.set(DataComponents.DYED_COLOR,new DyedItemColor(0x1E3560,false));b.setItemSlot(EquipmentSlot.HEAD,helmet);}
   for(var s:EquipmentSlot.values())b.setDropChance(s,0);
   level.addFreshEntity(b);
  }
  static ItemStack gun(Item i){var s=new ItemStack(i);var f=WeaponFamily.of(s);if(f!=null){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt("ArmoryFormat",21));Magazines.rounds(s,f.capacity);}return s;}
  static ItemStack tagged(ItemStack s){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putBoolean("BombKit",true));return s;}
  static boolean kitItem(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getBoolean("BombKit");}
  /** puts a kit item in a hotbar slot; whatever was there moves to the backpack (or stays put if the backpack is full) */
  static void place(ServerPlayer p,int slot,ItemStack s){var inv=p.getInventory();var old=inv.getItem(slot);
   if(!old.isEmpty()&&!kitItem(old)){int free=-1;for(int i=9;i<36;i++)if(inv.getItem(i).isEmpty()){free=i;break;}
    if(free<0){inv.add(s);return;}inv.setItem(free,old);}
   inv.setItem(slot,s);}
  static void clearKit(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(kitItem(inv.getItem(i)))inv.setItem(i,ItemStack.EMPTY);}
  void kit(ServerPlayer p,int team){
   clearKit(p);var inv=p.getInventory();
   var rifle=team==1?WeaponFamily.M4A1:WeaponFamily.AK47;var pistol=team==1?WeaponFamily.USP:WeaponFamily.GLOCK;
   place(p,0,tagged(gun(rifle.item())));place(p,1,tagged(gun(pistol.item())));place(p,2,tagged(new ItemStack(team==1?Knives.BUTTERFLY.get():Knives.KARAMBIT.get())));
   if(team==1)place(p,3,tagged(new ItemStack(Items.SHEARS)));
   inv.add(tagged(MagazineItem.make(rifle,rifle.capacity)));inv.add(tagged(MagazineItem.make(rifle,rifle.capacity)));inv.add(tagged(MagazineItem.make(pistol,pistol.capacity)));
   var vest=new ItemStack(Items.LEATHER_CHESTPLATE);vest.set(DataComponents.DYED_COLOR,new DyedItemColor(team==1?0x2B4C8C:0xB58A4C,false));
   if(p.getItemBySlot(EquipmentSlot.CHEST).isEmpty())p.setItemSlot(EquipmentSlot.CHEST,tagged(vest));
   inv.selected=0;
   inv.setChanged();
  }

  // ---------------------------------------------------- bomb
  public void plant(LivingEntity who,BlockPos at){
   if(bomb!=null||phase!=1)return;
   var pos=at;if(!level.getBlockState(pos).isAir())pos=pos.above();
   level.setBlock(pos,C4.BLOCK.get().defaultBlockState().setValue(C4.C4Block.FACING,net.minecraft.core.Direction.UP).setValue(C4.C4Block.ARMED,true),3);
   if(level.getBlockEntity(pos) instanceof C4.C4Entity c){c.arm(FUSE);c.match=worldPosition;c.setChanged();}
   bomb=pos;timer=FUSE;
   if(who instanceof ServerPlayer p)for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(BOMB.get()))p.getInventory().setItem(i,ItemStack.EMPTY);
   broadcast("§c💣 A bomba foi plantada no bombsite "+(inSite(pos)==1?"A":"B")+"! §7("+(who instanceof Player pl?pl.getName().getString():who.getName().getString())+")");
   title("","§cBomba plantada!");
   level.playSound(null,pos,SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.BLOCKS,2F,.6F);
  }
  public void defusedBy(LivingEntity who){
   if(bomb==null)return;
   if(level.getBlockEntity(bomb) instanceof C4.C4Entity c)c.disarm();
   level.setBlock(bomb,C4.BLOCK.get().defaultBlockState().setValue(C4.C4Block.ARMED,false),3);
   level.playSound(null,bomb,SoundEvents.SHEEP_SHEAR,SoundSource.BLOCKS,1.5F,1.1F);
   roundWon(1,"§b✂ Bomba desarmada"+(who!=null?" por "+who.getName().getString():"")+"!");
  }
  /** called by the C4 itself */
  public void defused(ServerPlayer p){roundWon(1,"§b✂ Bomba desarmada por "+p.getName().getString()+"!");}
  public void exploded(){bomb=null;roundWon(0,"§6💥 A bomba explodiu!");}
  void clearBomb(){if(bomb!=null&&level.getBlockState(bomb).getBlock() instanceof C4.C4Block){if(level.getBlockEntity(bomb) instanceof C4.C4Entity c)c.disarm();level.removeBlock(bomb,false);}bomb=null;
   if(origin!=null)for(var p:online())for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(BOMB.get()))p.getInventory().setItem(i,ItemStack.EMPTY);}

  // ---------------------------------------------------- deaths
  public void killed(LivingEntity dead,Entity by){
   String a=by==null?"":(team(by)==1?"§b":"§6")+name(by)+" §7matou ";
   broadcast(a+(team(dead)==1?"§b":"§6")+name(dead));
   // the bomb passes to another terrorist
   boolean hadBomb=dead instanceof BombBot b?b.carrier:dead instanceof ServerPlayer p&&p.getInventory().contains(new ItemStack(BOMB.get()));
   if(hadBomb&&bomb==null&&phase==1){if(dead instanceof BombBot b)b.carrier=false;
    var ts=new ArrayList<Object>();for(var p:online())if(players.get(p.getUUID())==0&&p!=dead&&!p.isSpectator())ts.add(p);for(var b:bots())if(!b.ct()&&b!=dead&&b.isAlive())ts.add(b);
    if(!ts.isEmpty())giveBomb(ts.get(level.random.nextInt(ts.size())));}
  }
  static String name(Entity e){return e instanceof BombBot b?"BOT "+b.botName:e.getName().getString();}
  int alive(int team){int n=0;for(var p:online())if(players.get(p.getUUID())==team&&!p.isSpectator())n++;for(var b:bots())if(b.team()==team&&b.isAlive())n++;return n;}

  void roundWon(int team,String why){
   if(phase==2)return;phase=2;timer=AFTER;lastWinner=team;
   if(team==1)scoreCT++;else scoreT++;
   title(team==1?"§bCT venceram o round":"§6TR venceram o round",why);broadcast(why+" §7— §bCT "+scoreCT+" §7× §6"+scoreT+" TR");
   for(var p:online())p.playNotifySound(players.get(p.getUUID())==team?SoundEvents.UI_TOAST_CHALLENGE_COMPLETE:SoundEvents.VILLAGER_NO,SoundSource.MASTER,.8F,1F);
   setChanged();
  }
  void end(String why){
   running=false;if(activePos!=null&&activePos.equals(worldPosition))activePos=null;
   for(var b:bots())b.discard();clearBomb();
   for(var p:online()){clearKit(p);var chest=p.getItemBySlot(EquipmentSlot.CHEST);if(kitItem(chest))p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);
    p.setGameMode(GameType.SURVIVAL);p.setHealth(p.getMaxHealth());p.removeAllEffects();p.teleportTo(sl(),worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+1.5,0,0);p.sendSystemMessage(Component.literal(why));}
   if(bar!=null){bar.removeAllPlayers();bar=null;}
   for(var id:players.keySet()){TEAMS.remove(id);PLAYER_MATCH.remove(id);}
   players.clear();setChanged();
  }

  // ---------------------------------------------------- goals for the bots
  public BlockPos nextGoal(BombBot b){
   if(bomb!=null){if(b.ct())return bomb;var r=level.random;return bomb.offset(r.nextInt(9)-4,0,r.nextInt(9)-4);}
   int[] siteR=(b.ct()?(b.getId()%2==0):site==1)?SITE_A:SITE_B;
   if(!b.ct()&&!b.carrier&&level.random.nextInt(3)==0)return randomIn(new int[]{18,8,21,16});   // some TR push mid
   return randomIn(siteR);
  }

  public static void tick(Level l,BlockPos pos,BlockState s,Terminal t){
   if(!t.running)return;
   if(activePos==null)activePos=pos;
   var ps=t.online();
   if(ps.isEmpty()){t.end("");return;}
   if(t.timer>0)t.timer--;
   // freeze: nobody moves
   if(t.phase==0)for(var p:ps)if(!p.isSpectator()){p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,5,10,false,false));p.addEffect(new MobEffectInstance(MobEffects.JUMP,5,200,false,false));}
   if(t.phase==0&&t.timer<=0){t.phase=1;t.timer=ROUND;t.title("","§aVAI! VAI! VAI!");for(var p:ps)p.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.MASTER,1F,1.5F);}
   else if(t.phase==1){
    if(t.bomb!=null&&!(l.getBlockState(t.bomb).getBlock() instanceof C4.C4Block))t.bomb=null;
    if(t.alive(1)==0)t.roundWon(0,"§6Todos os CT foram eliminados.");
    else if(t.alive(0)==0&&t.bomb==null)t.roundWon(1,"§bTodos os TR foram eliminados.");
    else if(t.timer<=0&&t.bomb==null)t.roundWon(1,"§bO tempo acabou.");
   }
   else if(t.phase==2&&t.timer<=0){
    if(t.scoreCT>=WIN||t.scoreT>=WIN){boolean ct=t.scoreCT>=WIN;t.title(ct?"§b🏆 CT VENCERAM!":"§6🏆 TR VENCERAM!","§7"+t.scoreCT+" × "+t.scoreT);t.end((ct?"§b🏆 Os CT venceram":"§6🏆 Os TR venceram")+" a partida por "+Math.max(t.scoreCT,t.scoreT)+" × "+Math.min(t.scoreCT,t.scoreT)+"!");return;}
    t.newRound();
   }
   // HUD
   if(t.bar!=null&&l.getGameTime()%5==0){
    int sec=(t.timer+19)/20;String clock=String.format("%d:%02d",sec/60,sec%60);
    String mid=t.phase==0?"§eCompra/espera "+clock:t.phase==2?"§7Próximo round...":t.bomb!=null?"§c💣 BOMBA "+clock:"§f"+clock;
    t.bar.setName(Component.literal("§bCT "+t.scoreCT+"  §7|  "+mid+"  §7|  §6"+t.scoreT+" TR   §8("+t.alive(1)+" vs "+t.alive(0)+")"));
    t.bar.setColor(t.bomb!=null?BossEvent.BossBarColor.RED:t.phase==0?BossEvent.BossBarColor.YELLOW:BossEvent.BossBarColor.WHITE);
    t.bar.setProgress(Math.max(0,Math.min(1,t.timer/(float)(t.phase==0?FREEZE:t.phase==2?AFTER:t.bomb!=null?FUSE:ROUND))));
    for(var p:ps)if(!t.bar.getPlayers().contains(p))t.bar.addPlayer(p);
   }
  }
  @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider r){super.saveAdditional(tag,r);if(origin!=null)tag.putLong("Origin",origin.asLong());tag.putInt("Choice",choice);tag.putInt("Bots",bots);}
  @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider r){super.loadAdditional(tag,r);origin=tag.contains("Origin")?BlockPos.of(tag.getLong("Origin")):null;choice=tag.getInt("Choice");bots=tag.contains("Bots")?tag.getInt("Bots"):5;}
  @Override public void setRemoved(){if(running&&level!=null&&!level.isClientSide)end("§7Partida encerrada.");super.setRemoved();}
 }

 // ======================================================= events
 static Terminal matchOf(ServerPlayer p){var pos=PLAYER_MATCH.get(p.getUUID());return pos!=null&&p.level().getBlockEntity(pos) instanceof Terminal t&&t.running()?t:null;}
 static void death(LivingDeathEvent e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;var m=matchOf(p);if(m==null)return;
  e.setCanceled(true);p.setHealth(p.getMaxHealth());p.clearFire();p.removeAllEffects();
  m.killed(p,e.getSource().getEntity());
  for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(BOMB.get()))p.getInventory().setItem(i,ItemStack.EMPTY);
  p.setGameMode(GameType.SPECTATOR);p.displayClientMessage(Component.literal("§7Você morreu — assistindo até o próximo round."),true);
 }
 static void damage(LivingIncomingDamageEvent e){
  var src=e.getSource().getEntity();if(src==null||src==e.getEntity())return;
  if(team(e.getEntity())>=0&&sameTeam(src,e.getEntity()))e.setCanceled(true);
  if(e.getEntity() instanceof ServerPlayer p){var m=matchOf(p);if(m!=null&&m.frozen())e.setCanceled(true);}
 }
 static void logout(PlayerEvent.PlayerLoggedOutEvent e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;var m=matchOf(p);if(m==null)return;
  Terminal.clearKit(p);p.setGameMode(GameType.SURVIVAL);m.players.remove(p.getUUID());TEAMS.remove(p.getUUID());PLAYER_MATCH.remove(p.getUUID());
  if(m.players.isEmpty())m.end("");
 }
}
