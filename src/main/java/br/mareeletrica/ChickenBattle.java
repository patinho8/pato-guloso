package br.mareeletrica;

import br.mareeletrica.ChickenTypes.*;
import br.mareeletrica.FightChickens.FightChicken;
import com.mojang.serialization.MapCodec;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Rooster ring: a 5x5 boxing ring where two roosters fight a Pokémon-style turn battle. Each owner picks one of four moves
 *  per turn on a battle screen; the faster rooster acts first; types, buffs, burns, paralysis, freezing and critical hits
 *  all count. Alone? Crouch + click the ring to train against a computer rooster. */
public final class ChickenBattle {
 public static final DeferredBlock<RingBlock> BLOCK=MareEletrica.BLOCKS.register("rooster_ring",()->new RingBlock(BlockBehaviour.Properties.of().strength(2F).sound(SoundType.WOOL).noOcclusion()));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("rooster_ring",()->new BlockItem(BLOCK.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Ring>> ENTITY=MareEletrica.ENTITIES.register("rooster_ring",()->BlockEntityType.Builder.of(Ring::new,BLOCK.get()).build(null));
 /** Roosters currently in a ring battle (others with the battle tag are leftovers from a closed world). */
 public static final Set<UUID> ACTIVE=new HashSet<>();
 public static Consumer<State> openScreen=s->{};
 private ChickenBattle(){}
 public static void register(){}

 public static final class RingBlock extends HorizontalDirectionalBlock implements EntityBlock,Multiblock.Master {
  public static final MapCodec<RingBlock> CODEC=simpleCodec(RingBlock::new);
  // floor 5x5, corner posts (pixels, north frame, relative to the centre cell)
  static final double[][] BOXES={{-32,0,-32,48,4,48},{-32,4,-32,-29,26,-29},{45,4,-32,48,26,-29},{-32,4,45,-29,26,48},{45,4,45,48,26,48}};
  public RingBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public List<BlockPos> cells(BlockState s){return Multiblock.cellsOf(s.getValue(FACING),BOXES);}
  @Override public VoxelShape cellShape(BlockState s,BlockPos o){return Multiblock.slice(s.getValue(FACING),BOXES,o);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var st=defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());return Multiblock.fits(c,st)?st:null;}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack st){Multiblock.place(l,p,s);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){
   if(!s.is(n.getBlock())){if(l.getBlockEntity(p) instanceof Ring r)r.end(null,"O ringue foi quebrado.");Multiblock.clear(l,p,s);}super.onRemove(s,l,p,n,moving);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return cellShape(s,BlockPos.ZERO);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Ring(p,s);}
  @SuppressWarnings("unchecked")
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=ENTITY.get()?null:(BlockEntityTicker<T>)(BlockEntityTicker<Ring>)Ring::tick;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide&&l.getBlockEntity(p) instanceof Ring r)r.use((ServerPlayer)player);
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }

 /** One side of the battle. */
 static final class Fighter {
  UUID player,chicken;String owner;Breed breed;int lvl,hp,max,atkSt,defSt,spdSt,status,statusTurns;List<Move> moves;boolean cpu,flinch;int choice=-1;
  // status: 0 ok, 1 burned, 2 paralysed, 3 frozen
  static Fighter of(FightChicken c,ServerPlayer p,boolean cpu){
   var f=new Fighter();f.player=p==null?null:p.getUUID();f.chicken=c.getUUID();f.owner=cpu?"Computador":p.getName().getString();f.breed=c.breed();f.lvl=c.lvl();
   f.max=f.breed.hp+f.lvl*4+20;f.hp=f.max;f.moves=f.breed.battleMoves(f.lvl);f.cpu=cpu;return f;
  }
  static float stage(int s){return s>=0?(2+s)/2F:2F/(2-s);}
  float atk(){return (breed.atk+lvl*3)*stage(atkSt)*(status==1?.75F:1);}
  float def(){return (breed.def+lvl*3)*stage(defSt);}
  float spd(){return (breed.spd+lvl*3)*stage(spdSt)*(status==2?.5F:1);}
  CompoundTag tag(){
   var t=new CompoundTag();t.putString("Owner",owner);t.putInt("Breed",breed.ordinal());t.putInt("Lvl",lvl);t.putInt("Hp",hp);t.putInt("Max",max);
   t.putInt("Atk",atkSt);t.putInt("Def",defSt);t.putInt("Spd",spdSt);t.putInt("Status",status);t.putBoolean("Chosen",choice>=0);
   var m=new ListTag();for(var mv:moves)m.add(StringTag.valueOf(mv.name()));t.put("Moves",m);return t;
  }
 }

 public static final class Ring extends BlockEntity {
  Fighter a,b;int phase;   // 0 idle, 1 waiting for a 2nd player, 2 choosing, 3 resolving, 4 over
  int timer,turn;final Deque<Runnable> steps=new ArrayDeque<>();int stepDelay;final List<String> log=new ArrayList<>();String result="";
  public Ring(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  ServerLevel sl(){return (ServerLevel)level;}
  FightChicken chicken(Fighter f){return f!=null&&sl().getEntity(f.chicken) instanceof FightChicken c?c:null;}
  ServerPlayer player(Fighter f){return f==null||f.player==null?null:sl().getServer().getPlayerList().getPlayer(f.player);}
  Vec3 spot(boolean first){
   var face=getBlockState().getValue(HorizontalDirectionalBlock.FACING);var side=face.getClockWise();
   return Vec3.atBottomCenterOf(worldPosition).add(0,.25,0).add(side.getStepX()*(first?-1.4:1.4),0,side.getStepZ()*(first?-1.4:1.4));
  }
  static FightChicken mine(ServerPlayer p){
   FightChicken best=null;double bd=1e9;
   for(var c:p.level().getEntitiesOfClass(FightChicken.class,p.getBoundingBox().inflate(12),c->c.isOwnedBy(p)&&c.ko()==0&&!ACTIVE.contains(c.getUUID()))){double d=c.distanceToSqr(p);if(d<bd){bd=d;best=c;}}
   return best;
  }
  void use(ServerPlayer p){
   if(phase>=2){if(isIn(p))send(p,true);else p.displayClientMessage(Component.literal("§7Tem uma batalha acontecendo neste ringue."),true);return;}
   if(phase==1&&a!=null&&p.getUUID().equals(a.player)){
    if(p.isShiftKeyDown()){start(p,true);return;}
    p.displayClientMessage(Component.literal("§eEsperando outro jogador clicar no ringue... §7(agache + clique para treinar contra o computador)"),true);return;}
   var c=mine(p);
   if(c==null){p.displayClientMessage(Component.literal("§cTraga sua Galinha de Briga para perto do ringue (até 12 blocos)."),true);return;}
   if(phase==0){
    a=Fighter.of(c,p,false);
    if(p.isShiftKeyDown()){start(p,true);return;}
    phase=1;timer=20*60;place(c,true);
    for(var o:sl().getEntitiesOfClass(Player.class,new AABB(worldPosition).inflate(32)))o.displayClientMessage(Component.literal("§6🥊 "+p.getName().getString()+" quer uma batalha de galos! §7Clique no ringue com a sua galinha por perto."),false);
    p.displayClientMessage(Component.literal("§7Ninguém por perto? Agache + clique no ringue para treinar contra o computador."),false);
    return;
   }
   if(phase==1){b=Fighter.of(c,p,false);start(p,false);}
  }
  /** Starts a training battle against the computer (also used by the visual test). */
  public void train(ServerPlayer p,FightChicken c){if(phase!=0)return;a=Fighter.of(c,p,false);start(p,true);}
  boolean isIn(Player p){return a!=null&&p.getUUID().equals(a.player)||b!=null&&p.getUUID().equals(b.player);}
  void place(FightChicken c,boolean first){
   var s=spot(first);var center=Vec3.atBottomCenterOf(worldPosition);
   float yaw=(float)(Mth.atan2(center.z-s.z,center.x-s.x)*180/Math.PI)-90;
   c.setOrderedToSit(false);c.setInSittingPose(false);c.setTarget(null);c.getNavigation().stop();
   c.teleportTo(s.x,s.y,s.z);c.setYRot(yaw);c.setYBodyRot(yaw);c.setYHeadRot(yaw);c.setDeltaMovement(Vec3.ZERO);
   c.setNoAi(true);c.getPersistentData().putBoolean("MareBattle",true);ACTIVE.add(c.getUUID());
  }
  void start(ServerPlayer p,boolean cpu){
   if(cpu){
    var mineC=chicken(a);if(mineC==null){reset();return;}
    var e=FightChickens.ENTITY.get().create(sl());if(e==null){reset();return;}
    e.setBreed(Breed.common(sl().random));
    int lvl=Mth.clamp(mineC.lvl()+sl().random.nextInt(3)-1,1,10);e.setLevel(lvl);
    e.setTame(true,false);e.getPersistentData().putBoolean("MareCpu",true);
    var s=spot(false);e.moveTo(s.x,s.y,s.z,0,0);sl().addFreshEntity(e);
    b=Fighter.of(e,null,true);
   }
   place(chicken(a),true);place(chicken(b),false);
   phase=2;turn=1;timer=20*40;log.clear();
   say("§6🥊 BATALHA! §f"+a.owner+" ("+a.breed.label+" nv."+a.lvl+") §7vs §f"+b.owner+" ("+b.breed.label+" nv."+b.lvl+")");
   sl().playSound(null,worldPosition,SoundEvents.BELL_BLOCK,SoundSource.BLOCKS,1.5F,1F);
   syncHp();if(b.cpu)b.choice=cpuMove(b,a);broadcast(true);
  }
  void reset(){
   // tell the players the ring is free again (their battle screens close)
   if(level!=null&&!level.isClientSide)for(var f:new Fighter[]{a,b}){var p=player(f);if(p!=null){var t=new CompoundTag();t.putInt("Phase",0);PacketDistributor.sendToPlayer(p,new State(worldPosition,t,false));}}
   for(var f:new Fighter[]{a,b}){var c=chicken(f);if(c!=null){ACTIVE.remove(c.getUUID());c.getPersistentData().remove("MareBattle");c.setNoAi(false);c.setBattleHp(0,0);if(f.cpu)c.discard();}else if(f!=null)ACTIVE.remove(f.chicken);}
   a=b=null;phase=0;steps.clear();
  }
  void say(String s){log.add(s);while(log.size()>6)log.remove(0);}
  void syncHp(){var ca=chicken(a);if(ca!=null)ca.setBattleHp(a.hp,a.max);var cb=chicken(b);if(cb!=null)cb.setBattleHp(b.hp,b.max);}
  CompoundTag data(ServerPlayer viewer){
   var t=new CompoundTag();t.putInt("Phase",phase);t.putInt("Turn",turn);t.putInt("Timer",timer/20);t.putString("Result",result);
   boolean flip=b!=null&&viewer!=null&&viewer.getUUID().equals(b.player);
   if(a!=null)t.put(flip?"Foe":"Me",a.tag());if(b!=null)t.put(flip?"Me":"Foe",b.tag());
   var l=new ListTag();for(var s:log)l.add(StringTag.valueOf(s));t.put("Log",l);return t;
  }
  void send(ServerPlayer p,boolean open){PacketDistributor.sendToPlayer(p,new State(worldPosition,data(p),open));}
  void broadcast(boolean open){for(var f:new Fighter[]{a,b}){var p=player(f);if(p!=null)send(p,open);}}
  void choose(ServerPlayer p,int move){
   if(phase!=2||!isIn(p))return;
   var f=p.getUUID().equals(a.player)?a:b;
   if(move==9){var other=f==a?b:a;say("§c"+f.owner+" desistiu!");finish(other,f);return;}
   if(move<0||move>=f.moves.size())return;
   f.choice=move;broadcast(false);
   if(a.choice>=0&&b.choice>=0)resolve();
  }
  /** Picks a move for the computer: heals when low, buffs early, otherwise the strongest hit against this foe. */
  static int cpuMove(Fighter me,Fighter foe){
   int best=0;float bs=-1;var r=new Random();
   for(int i=0;i<me.moves.size();i++){var m=me.moves.get(i);float s;
    if(m.effect==Effect.HEAL||m.effect==Effect.HEAL_DEF)s=me.hp<me.max*.4F?200:5;
    else if(m.power==0)s=(me.atkSt+me.defSt+me.spdSt<2&&me.hp>me.max*.6F)?60:10;
    else s=m.power*m.type.against(foe.breed.type)*(m.type==me.breed.type?1.5F:1)*m.accuracy/100F;
    s*=.8F+r.nextFloat()*.4F;if(s>bs){bs=s;best=i;}}
   return best;
  }
  void resolve(){
   phase=3;var ma=a.moves.get(a.choice);var mb=b.moves.get(b.choice);
   boolean aFirst=ma.priority()!=mb.priority()?ma.priority()>mb.priority():a.spd()!=b.spd()?a.spd()>b.spd():sl().random.nextBoolean();
   Fighter f1=aFirst?a:b,f2=aFirst?b:a;Move m1=aFirst?ma:mb,m2=aFirst?mb:ma;
   a.flinch=b.flinch=false;first=f1;
   steps.add(()->act(f1,f2,m1));
   steps.add(()->{if(f2.hp>0&&f1.hp>0)act(f2,f1,m2);});
   steps.add(this::endOfTurn);
   stepDelay=10;broadcast(false);
  }
  void act(Fighter u,Fighter t,Move m){
   var cu=chicken(u);var ct=chicken(t);var r=sl().random;
   if(u.flinch){say("§7"+u.breed.label+" de "+u.owner+" se assustou e perdeu a vez!");return;}
   if(u.status==3){if(r.nextInt(100)<35){u.status=0;say("§b"+u.breed.label+" de "+u.owner+" descongelou!");}else{say("§b"+u.breed.label+" de "+u.owner+" está congelado!");fx(cu,ParticleTypes.SNOWFLAKE,SoundEvents.GLASS_HIT);return;}}
   if(u.status==2&&r.nextInt(100)<25){say("§e"+u.breed.label+" de "+u.owner+" está paralisado e não se mexe!");fx(cu,ParticleTypes.ELECTRIC_SPARK,SoundEvents.BEEHIVE_WORK);return;}
   say("§f"+u.breed.label+" de "+u.owner+" usou §l"+m.label+"§r§f!");
   if(cu!=null)cu.level().broadcastEntityEvent(cu,m.kind==2?FightChickens.FURY:m.kind==1?FightChickens.WINGS:m==Move.VOADORA||m==Move.AVALANCHE||m==Move.RASANTE_FLAMEJANTE||m==Move.MERGULHO_AEREO||m==Move.TERREMOTO||m==Move.CABECADA_DE_PEDRA?FightChickens.KICK:FightChickens.PECK);
   if(r.nextInt(100)>=m.accuracy){say("§7...mas errou!");sl().playSound(null,worldPosition,SoundEvents.PLAYER_ATTACK_NODAMAGE,SoundSource.PLAYERS,1F,1F);return;}
   if(m.power>0){
    float eff=m.type.against(t.breed.type);
    boolean crit=r.nextInt(100)<(m.effect==Effect.HIGH_CRIT?25:6);
    float L=10+u.lvl*5;
    float dmg=((2*L/5+2)*m.power*u.atk()/t.def()/60F+2)*(m.type==u.breed.type?1.5F:1)*eff*(crit?1.5F:1)*(.85F+r.nextFloat()*.15F);
    int d=Math.max(1,Math.round(dmg));t.hp=Math.max(0,t.hp-d);
    fx(ct,typeFx(m.type),typeSound(m.type));
    if(ct!=null){ct.hurtTime=10;ct.level().broadcastEntityEvent(ct,(byte)2);}
    if(crit)say("§6Acerto crítico!");
    if(eff>1)say("§a§lÉ super efetivo!");else if(eff<1)say("§7Não é muito efetivo...");
    say("§c-"+d+" de vida §7("+t.hp+"/"+t.max+")");
    if(m.effect==Effect.DRAIN){int h=Math.max(1,d*m.chance/100);u.hp=Math.min(u.max,u.hp+h);say("§a"+u.breed.label+" sugou "+h+" de vida.");}
    if(m.effect==Effect.RECOIL){int h=Math.max(1,d*m.chance/100);u.hp=Math.max(0,u.hp-h);say("§7"+u.breed.label+" se machucou com o impacto (-"+h+").");}
    if(t.hp>0&&t.status==0&&r.nextInt(100)<m.chance){
     switch(m.effect){
      case BURN->{if(t.breed.type!=Type.FOGO){t.status=1;say("§6"+t.breed.label+" de "+t.owner+" pegou fogo!");}}
      case PARALYZE->{if(t.breed.type!=Type.ELETRICO){t.status=2;say("§e"+t.breed.label+" de "+t.owner+" ficou paralisado!");}}
      case FREEZE->{if(t.breed.type!=Type.GELO){t.status=3;say("§b"+t.breed.label+" de "+t.owner+" congelou!");}}
      default->{}
     }
    }
    if(t.hp>0&&m.effect==Effect.FLINCH&&r.nextInt(100)<m.chance&&u==firstActor())t.flinch=true;
    if(t.hp>0&&m.effect==Effect.FOE_SPD_DOWN){t.spdSt=Math.max(-6,t.spdSt-1);say("§7A velocidade de "+t.breed.label+" caiu.");}
   }else{
    switch(m.effect){
     case ATK_UP->stat(u,1,0,0);case ATK_UP2->stat(u,2,0,0);case DEF_UP->stat(u,0,1,0);case DEF_UP2->stat(u,0,2,0);case SPD_UP2->stat(u,0,0,2);case ATK_SPD_UP->stat(u,1,0,1);
     case FOE_ATK_DOWN2->{t.atkSt=Math.max(-6,t.atkSt-2);say("§7O ataque de "+t.breed.label+" caiu muito!");fx(ct,ParticleTypes.SMOKE,SoundEvents.PHANTOM_AMBIENT);}
     case FOE_DEF_DOWN2->{t.defSt=Math.max(-6,t.defSt-2);say("§7A defesa de "+t.breed.label+" caiu muito!");fx(ct,ParticleTypes.SQUID_INK,SoundEvents.WITHER_AMBIENT);}
     case FOE_DEF_DOWN->{t.defSt=Math.max(-6,t.defSt-1);say("§7A defesa de "+t.breed.label+" caiu!");fx(ct,ParticleTypes.ANGRY_VILLAGER,SoundEvents.RAVAGER_ROAR);}
     case HEAL->{int h=u.max*m.chance/100;u.hp=Math.min(u.max,u.hp+h);say("§a"+u.breed.label+" recuperou vida! §7("+u.hp+"/"+u.max+")");fx(cu,ParticleTypes.HEART,SoundEvents.PLAYER_LEVELUP);}
     case HEAL_DEF->{int h=u.max*m.chance/100;u.hp=Math.min(u.max,u.hp+h);stat(u,0,1,0);say("§a"+u.breed.label+" recuperou vida!");fx(cu,ParticleTypes.SPLASH,SoundEvents.WEATHER_RAIN);}
     case PARALYZE->{if(t.status==0&&t.breed.type!=Type.ELETRICO){t.status=2;say("§e"+t.breed.label+" de "+t.owner+" ficou paralisado!");fx(ct,ParticleTypes.SPORE_BLOSSOM_AIR,SoundEvents.AZALEA_LEAVES_BREAK);}else say("§7Não teve efeito...");}
     default->{}
    }
   }
   syncHp();broadcast(false);
   if(t.hp<=0||u.hp<=0){steps.clear();steps.add(()->{if(t.hp<=0)finish(u,t);else finish(t,u);});}
  }
  Fighter first;Fighter firstActor(){return first;}
  void stat(Fighter u,int at,int de,int sp){
   u.atkSt=Math.min(6,u.atkSt+at);u.defSt=Math.min(6,u.defSt+de);u.spdSt=Math.min(6,u.spdSt+sp);
   var parts=new ArrayList<String>();if(at>0)parts.add("ataque");if(de>0)parts.add("defesa");if(sp>0)parts.add("velocidade");
   say("§b⬆ "+String.join(" e ",parts)+" de "+u.breed.label+(at+de+sp>=2&&parts.size()==1?" subiu muito!":" subiu!"));
   fx(chicken(u),ParticleTypes.ENCHANTED_HIT,SoundEvents.ENCHANTMENT_TABLE_USE);
  }
  void endOfTurn(){
   for(var f:new Fighter[]{a,b})if(f.hp>0&&f.status==1){int d=Math.max(1,f.max/16);f.hp=Math.max(0,f.hp-d);say("§6"+f.breed.label+" de "+f.owner+" sofre com a queimadura (-"+d+")");fx(chicken(f),ParticleTypes.FLAME,SoundEvents.FIRE_AMBIENT);}
   syncHp();
   if(a.hp<=0){finish(b,a);return;}if(b.hp<=0){finish(a,b);return;}
   a.choice=-1;b.choice=-1;turn++;phase=2;timer=20*40;
   if(b.cpu)b.choice=cpuMove(b,a);
   broadcast(false);
  }
  void finish(Fighter w,Fighter l){
   phase=4;steps.clear();timer=20*8;
   var cw=chicken(w);var cl=chicken(l);
   result=w.owner+" venceu!";
   say("§6🏆 "+w.breed.label+" de "+w.owner+" venceu a batalha!");
   if(cl!=null){cl.knockOut();fx(cl,ParticleTypes.POOF,SoundEvents.CHICKEN_DEATH);}
   if(cw!=null&&cl!=null){if(!w.cpu)cw.won(cl);else sl().sendParticles(ParticleTypes.TOTEM_OF_UNDYING,cw.getX(),cw.getY()+1,cw.getZ(),30,.4,.6,.4,.4);}
   sl().playSound(null,worldPosition,SoundEvents.BELL_BLOCK,SoundSource.BLOCKS,1.5F,.7F);
   if(cw!=null&&!w.cpu&&sl().random.nextInt(5)==0){cw.spawnAtLocation(RareEgg.item());say("§6✦ Prêmio raro: "+w.owner+" ganhou um OVO RARO!");}
   broadcast(false);
  }
  void end(Fighter w,String why){if(a!=null||b!=null){say("§7"+why);broadcast(false);}reset();}
  static ParticleOptions typeFx(Type t){
   return switch(t){case FOGO->ParticleTypes.FLAME;case AGUA->ParticleTypes.SPLASH;case PLANTA->ParticleTypes.HAPPY_VILLAGER;case ELETRICO->ParticleTypes.ELECTRIC_SPARK;
    case GELO->ParticleTypes.SNOWFLAKE;case PEDRA->new BlockParticleOption(ParticleTypes.BLOCK,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
    case VENTO->ParticleTypes.CLOUD;case SOMBRA->ParticleTypes.SQUID_INK;default->ParticleTypes.CRIT;};
  }
  static SoundEvent typeSound(Type t){
   return switch(t){case FOGO->SoundEvents.FIRECHARGE_USE;case AGUA->SoundEvents.PLAYER_SPLASH;case PLANTA->SoundEvents.GRASS_BREAK;case ELETRICO->SoundEvents.TRIDENT_THUNDER.value();
    case GELO->SoundEvents.GLASS_BREAK;case PEDRA->SoundEvents.STONE_BREAK;case VENTO->SoundEvents.ENDER_DRAGON_FLAP;case SOMBRA->SoundEvents.WITHER_AMBIENT;default->SoundEvents.PLAYER_ATTACK_STRONG;};
  }
  void fx(FightChicken c,ParticleOptions p,SoundEvent s){
   if(c==null)return;sl().sendParticles(p,c.getX(),c.getY()+.6,c.getZ(),18,.35,.4,.35,.06);
   sl().playSound(null,c.blockPosition(),s,SoundSource.PLAYERS,1F,1.1F);
  }
  static void tick(Level l,BlockPos p,BlockState s,Ring r){
   if(r.phase==0)return;
   // a fighter vanished (died, unloaded) or a player left: the battle is off
   if(r.phase>=2&&(r.chicken(r.a)==null||r.chicken(r.b)==null)){r.end(null,"Um dos galos sumiu. Batalha cancelada.");return;}
   if(r.phase==1){if(r.chicken(r.a)==null||--r.timer<=0){var pl=r.player(r.a);if(pl!=null)pl.displayClientMessage(Component.literal("§7Ninguém aceitou a batalha."),true);r.reset();}return;}
   if(r.phase>=2&&r.phase<4&&r.player(r.a)==null){r.say("§7"+r.a.owner+" saiu.");r.finish(r.b,r.a);return;}
   if(r.phase>=2&&r.phase<4&&!r.b.cpu&&r.player(r.b)==null){r.say("§7"+r.b.owner+" saiu.");r.finish(r.a,r.b);return;}
   // keep the roosters on their spots and facing each other
   if(l.getGameTime()%10==0)for(int i=0;i<2;i++){var c=r.chicken(i==0?r.a:r.b);if(c!=null&&c.position().distanceToSqr(r.spot(i==0))>.5)r.place(c,i==0);}
   switch(r.phase){
    case 2->{if(--r.timer<=0){var rnd=l.random;for(var f:new Fighter[]{r.a,r.b})if(f.choice<0){f.choice=rnd.nextInt(f.moves.size());r.say("§7"+f.owner+" demorou: golpe escolhido na sorte.");}r.resolve();}
     else if(r.timer%20==0)r.broadcast(false);}
    case 3->{if(--r.stepDelay<=0&&!r.steps.isEmpty()){var st=r.steps.poll();st.run();r.stepDelay=30;}}
    case 4->{if(--r.timer<=0)r.reset();}
    default->{}
   }
  }
  @Override public void setRemoved(){if(level!=null&&!level.isClientSide)reset();super.setRemoved();}
 }

 public record State(BlockPos pos,CompoundTag data,boolean open) implements CustomPacketPayload {
  public static final Type<State> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"rooster_battle"));
  public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,State::pos,ByteBufCodecs.COMPOUND_TAG,State::data,ByteBufCodecs.BOOL,State::open,State::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Choose(BlockPos pos,int move) implements CustomPacketPayload {
  public static final Type<Choose> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"rooster_choose"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Choose> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Choose::pos,ByteBufCodecs.VAR_INT,Choose::move,Choose::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
   var r=e.registrar("18");
   r.playToClient(State.TYPE,State.CODEC,(p,c)->openScreen.accept(p));
   r.playToServer(Choose.TYPE,Choose.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer sp&&sp.distanceToSqr(p.pos().getCenter())<48*48&&sp.level().getBlockEntity(p.pos()) instanceof Ring g)g.choose(sp,p.move());});
  }
 }
}
