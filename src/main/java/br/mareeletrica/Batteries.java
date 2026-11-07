package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.64: more "mil tiros" machines like the barrage launcher. Each holds 1000 rounds, aims and fires by itself, never shoots
 *  players / tamed pets / villagers in the line of fire, and NONE of them breaks terrain.
 *  Plus the ammo crate: up to 1000 rounds of one kind in a single inventory slot (vanilla caps a stack at 99). */
public final class Batteries {
 public static final int MAX_AMMO=1000;

 public enum Kind {
  SENTRY("sentry_gun",4,40,10F/16F,.95,20F,6F),
  FLAK("flak_battery",8,110,9F/16F,1.45,10F,6F),
  GRENADE("auto_grenade_launcher",6,56,9F/16F,.8,8F,5F);
  public final String id;public final double min,max,muzzle;public final float pivotY,turn,pitchTurn;
  Kind(String id,double min,double max,float pivotY,double muzzle,float turn,float pitchTurn){this.id=id;this.min=min;this.max=max;this.pivotY=pivotY;this.muzzle=muzzle;this.turn=turn;this.pitchTurn=pitchTurn;}
  public Item ammo(){return switch(this){case SENTRY->Arsenal.RIFLE_ROUND.get();case FLAK->FLAK_SHELL.get();case GRENADE->GRENADE_ROUND.get();};}
  public String title(){return switch(this){case SENTRY->"Torre Sentinela";case FLAK->"Bateria Antiaérea";case GRENADE->"Lança-granadas Automático";};}
  public String ammoName(){return switch(this){case SENTRY->"balas de fuzil";case FLAK->"granadas antiaéreas";case GRENADE->"granadas 40mm";};}
  public String targets(){return switch(this){case SENTRY->"monstros até 40 blocos";case FLAK->"monstros voadores e aeronaves inimigas até 110 blocos";case GRENADE->"monstros no chão entre 6 e 56 blocos";};}
 }

 // ---------------- registration ----------------
 private static DeferredBlock<BatteryBlock> block(Kind k){return MareEletrica.BLOCKS.register(k.id,()->new BatteryBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(4F,12F).noOcclusion().sound(SoundType.METAL),k));}
 private static DeferredItem<BlockItem> item(Kind k,DeferredBlock<BatteryBlock> b){return MareEletrica.ITEMS.register(k.id,()->new BlockItem(b.get(),new Item.Properties().stacksTo(1)){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Controlada pelo Controle de Mira: olhe para mirar, clique para atirar."));t.add(Component.literal("§7Guarda até "+MAX_AMMO+" "+k.ammoName()+" • não quebra o terreno"));
   t.add(Component.literal("§8Shift + clique com o Controle de Mira: vincula • clique com munição ou caixa: carrega"));}});}
 public static final DeferredBlock<BatteryBlock> SENTRY=block(Kind.SENTRY),FLAK=block(Kind.FLAK),GRENADE=block(Kind.GRENADE);
 public static final DeferredItem<BlockItem> SENTRY_ITEM=item(Kind.SENTRY,SENTRY),FLAK_ITEM=item(Kind.FLAK,FLAK),GRENADE_ITEM=item(Kind.GRENADE,GRENADE);
 public static final DeferredItem<Item> FLAK_SHELL=MareEletrica.ITEMS.register("flak_shell",()->new MaterialItem("flak_shell",new Item.Properties().stacksTo(99)));
 public static final DeferredItem<Item> GRENADE_ROUND=MareEletrica.ITEMS.register("grenade_round",()->new MaterialItem("grenade_round",new Item.Properties().stacksTo(99)));
 public static final DeferredItem<AmmoCrate> CRATE=MareEletrica.ITEMS.register("ammo_crate",()->new AmmoCrate(new Item.Properties().stacksTo(1)));
 public static final Supplier<BlockEntityType<Gun>> ENTITY=MareEletrica.ENTITIES.register("battery_gun",()->BlockEntityType.Builder.of(Gun::new,SENTRY.get(),FLAK.get(),GRENADE.get()).build(null));
 public static final Supplier<EntityType<FlakShell>> FLAK_ENTITY=MareEletrica.ACTORS.register("flak_shell",()->
  EntityType.Builder.<FlakShell>of(FlakShell::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(16).updateInterval(1).build(MareEletrica.ID+":flak_shell"));
 public static final Supplier<EntityType<GrenadeShot>> GRENADE_ENTITY=MareEletrica.ACTORS.register("grenade_round",()->
  EntityType.Builder.<GrenadeShot>of(GrenadeShot::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(12).updateInterval(1).build(MareEletrica.ID+":grenade_round"));
 private Batteries(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(SENTRY_ITEM.get());out.accept(FLAK_ITEM.get());out.accept(GRENADE_ITEM.get());out.accept(FLAK_SHELL.get());out.accept(GRENADE_ROUND.get());out.accept(CRATE.get());
  for(var a:List.of(Barrage.ROCKET.get(),Arsenal.RIFLE_ROUND.get(),FLAK_SHELL.get(),GRENADE_ROUND.get()))out.accept(AmmoCrate.of(a,MAX_AMMO));}

 // ---------------- who is shot / who is protected ----------------
 static boolean friendly(Entity e){return e instanceof Player p&&!p.isSpectator()||e instanceof TamableAnimal t&&t.isTame()||e instanceof AbstractVillager||e instanceof AbstractGolem||e.isVehicle()&&e.getFirstPassenger() instanceof Player;}
 static boolean flying(Entity e){return e instanceof net.minecraft.world.entity.FlyingMob||e instanceof Blaze||e instanceof Vex||e instanceof WitherBoss;}
 static boolean wants(Kind k,Entity e,UUID owner){
  if(!e.isAlive()||e.isRemoved()||e.isInvisible())return false;
  // 0.64.5: the guns no longer pick targets; only the flak proximity fuse uses this, and only for enemy aircraft
  if(k!=Kind.FLAK||!Missiles.target(e))return false;
  var pilot=Missiles.pilotOf(e);return pilot!=null&&!pilot.equals(owner);
 }

 // ---------------- block ----------------
 public static final class BatteryBlock extends BaseEntityBlock {
  public final Kind kind;
  private static final VoxelShape SHAPE=Block.box(1,0,1,15,14,15);
  public BatteryBlock(BlockBehaviour.Properties p,Kind kind){super(p);this.kind=kind;}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return MapCodec.unit(this);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
  @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new Gun(pos,state);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,ENTITY.get(),Gun::tick);}
  @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){if(placer!=null&&level.getBlockEntity(pos) instanceof Gun g){g.owner=placer.getUUID();g.yawT=g.yaw=placer.getYRot()+180F;g.setChanged();}}
  @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
   if(!AmmoCrate.holds(stack,kind.ammo()))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   if(!level.isClientSide&&level.getBlockEntity(pos) instanceof Gun g){
    int n=AmmoCrate.feed(stack,kind.ammo(),MAX_AMMO-g.ammo,player);
    if(n<=0)player.displayClientMessage(Component.literal(kind.title()+" cheia ("+MAX_AMMO+")."),true);
    else{g.ammo+=n;g.sync();level.playSound(null,pos,SoundEvents.ARMOR_EQUIP_IRON.value(),SoundSource.BLOCKS,1F,.8F);player.displayClientMessage(Component.literal("Munição: "+g.ammo+" / "+MAX_AMMO),true);}
   }
   return ItemInteractionResult.sidedSuccess(level.isClientSide);
  }
  @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
   if(!level.isClientSide&&level.getBlockEntity(pos) instanceof Gun g)
    player.displayClientMessage(Component.literal("§e"+kind.title()+"§f — munição "+g.ammo+"/"+MAX_AMMO+" • controle com o §bControle de Mira§f (Shift + clique nela para vincular)"),true);
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
  @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
   if(!state.is(next.getBlock())&&level.getBlockEntity(pos) instanceof Gun g&&g.ammo>0){AmmoCrate.drop(level,pos,kind.ammo(),g.ammo);g.ammo=0;}
   super.onRemove(state,level,pos,next,moving);
  }
  @Override protected boolean triggerEvent(BlockState state,Level level,BlockPos pos,int id,int param){
   super.triggerEvent(state,level,pos,id,param);
   if(id==1&&level.getBlockEntity(pos) instanceof Gun g)g.firedAt[param&1]=level.getGameTime();
   return true;
  }
 }

 // ---------------- block entity: target, aim, fire ----------------
 public static final class Gun extends BlockEntity {
  public int ammo;public boolean armed=true;public UUID owner;public float yawT,pitchT=10;
  public float yaw,pitch=10,yawO,pitchO,spin,spinO,spinV;public final long[] firedAt={-100,-100};
  private int cooldown,burst,shots;
  public Gun(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  public Kind kind(){return getBlockState().getBlock() instanceof BatteryBlock b?b.kind:Kind.SENTRY;}
  public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
  Vec3 pivot(){return Vec3.atLowerCornerOf(worldPosition).add(.5,kind().pivotY,.5);}
  static Vec3 dir(float yaw,float pitch){double r=Math.toRadians(yaw),p=Math.toRadians(pitch);return new Vec3(-Math.sin(r)*Math.cos(p),Math.sin(p),Math.cos(r)*Math.cos(p));}
  public static void tick(Level level,BlockPos pos,BlockState state,Gun g){
   Kind k=g.kind();
   if(level.isClientSide){
    g.yawO=g.yaw;g.pitchO=g.pitch;g.spinO=g.spin;
    g.yaw+=Mth.clamp(Mth.wrapDegrees(g.yawT-g.yaw),-k.turn,k.turn);g.pitch+=Mth.clamp(g.pitchT-g.pitch,-k.pitchTurn,k.pitchTurn);
    boolean firing=level.getGameTime()-Math.max(g.firedAt[0],g.firedAt[1])<6;
    g.spinV=firing?Math.min(48F,g.spinV+8F):g.spinV*.9F;g.spin+=g.spinV;
    return;
   }
   // 0.64.5: no more automatic targeting — it only aims and fires when a player commands it with the Aim Remote
   if(g.cooldown>0)g.cooldown--;
   long now=level.getGameTime();
   if(g.ammo<=0||g.cooldown>0)return;
   boolean go=now<g.fireUntil||k==Kind.GRENADE&&g.burst>0;
   if(!go)return;
   Vec3 d=dir(g.yawT,g.pitchT);Vec3 muzzle=g.pivot().add(d.scale(k.muzzle));
   g.fire(k,(ServerLevel)level,state,muzzle,d,null,g.aimDist);
   if(k==Kind.FLAK)g.fireUntil=0;   // one pair of shells per click
  }
  long fireUntil;double aimDist=60;
  /** Aim Remote: turn toward the point the player looks at (the grenade launcher picks the lob that lands there). */
  public void aimAt(Vec3 point){
   Kind k=kind();Vec3 from=pivot();Vec3 dv=point.subtract(from);
   aimDist=Mth.clamp(dv.length(),4,k==Kind.FLAK?160:k.max);
   float ty=(float)Math.toDegrees(Math.atan2(-dv.x,dv.z)),tp;
   if(k==Kind.GRENADE){int el=lowArc(dv.horizontalDistance(),dv.y);tp=el==Integer.MIN_VALUE?45:el;}
   else tp=(float)Math.toDegrees(Math.atan2(dv.y,dv.horizontalDistance()));
   tp=Mth.clamp(tp,k==Kind.FLAK?-5:-30,85);
   if(Math.abs(Mth.wrapDegrees(ty-yawT))>.5F||Math.abs(tp-pitchT)>.5F){yawT=ty;pitchT=tp;sync();}
  }
  /** Aim Remote trigger. Click: sentry fires a short burst, flak one pair of shells, grenade launcher a 3-round burst.
   *  Held (sentry only): keeps firing while the button is down. Returns false when empty. */
  public boolean trigger(boolean held){
   if(ammo<=0)return false;long now=level.getGameTime();
   switch(kind()){
    case SENTRY->fireUntil=Math.max(fireUntil,now+(held?3:10));
    case FLAK->{if(!held&&cooldown<=0)fireUntil=now+3;}
    case GRENADE->{if(!held&&burst<=0&&cooldown<=0)burst=3;}
   }
   return true;
  }
  /** Lowest elevation (−10..50) whose lob lands at that distance/height; MIN_VALUE if out of range. Same physics as GrenadeShot. */
  static int lowArc(double dist,double dy){
   for(int el=-10;el<=50;el++){
    double a=Math.toRadians(el),vx=Math.cos(a)*GrenadeShot.SPEED,vy=Math.sin(a)*GrenadeShot.SPEED,x=.8*Math.cos(a),y=.8*Math.sin(a);
    for(int i=0;i<300;i++){double px=x,py=y;x+=vx;y+=vy;vx*=.99;vy=vy*.99-GrenadeShot.GRAVITY;
     if(vy<0&&y<=dy&&py>dy){double hit=px+(x-px)*(py-dy)/(py-y);if(hit>=dist)return el;break;}
     if(y<dy-40)break;}
   }
   return Integer.MIN_VALUE;
  }
  void fire(Kind k,ServerLevel sl,BlockState state,Vec3 muzzle,Vec3 d,Entity target,double dist){
   var rnd=sl.getRandom();ammo--;shots++;
   switch(k){
    case SENTRY->{
     cooldown=2;
     Vec3 dd=d.add(rnd.nextGaussian()*.01,rnd.nextGaussian()*.01,rnd.nextGaussian()*.01).normalize();
     Tracers.fire(sl,null,null,muzzle,dd,5.5F,5F,null);
     sl.sendParticles(ParticleTypes.FLAME,muzzle.x,muzzle.y,muzzle.z,1,0,0,0,0);sl.sendParticles(ParticleTypes.SMOKE,muzzle.x,muzzle.y,muzzle.z,1,.04,.04,.04,.01);
     sl.playSound(null,muzzle.x,muzzle.y,muzzle.z,MareEletrica.JET_GUN.get(),SoundSource.BLOCKS,1.2F,1.35F+rnd.nextFloat()*.15F);
     sl.blockEvent(worldPosition,state.getBlock(),1,0);
    }
    case FLAK->{
     cooldown=7;int barrel=shots&1;
     Vec3 side=new Vec3(-d.z,0,d.x).normalize().scale(barrel==0?-.2:.2);Vec3 m=muzzle.add(side);
     var s=new FlakShell(FLAK_ENTITY.get(),sl);s.setPos(m.x,m.y,m.z);s.setItem(new ItemStack(FLAK_SHELL.get()));s.owner=owner;
     s.fuse=(int)Math.ceil(dist/3.8)+1;s.setDeltaMovement(d.scale(3.8).add(rnd.nextGaussian()*.02,rnd.nextGaussian()*.02,rnd.nextGaussian()*.02));
     sl.addFreshEntity(s);
     sl.sendParticles(ParticleTypes.LARGE_SMOKE,m.x,m.y,m.z,6,.15,.15,.15,.03);sl.sendParticles(ParticleTypes.FLAME,m.x,m.y,m.z,4,.05,.05,.05,.05);
     sl.playSound(null,m.x,m.y,m.z,SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,1.6F,1.7F+rnd.nextFloat()*.2F);
     sl.blockEvent(worldPosition,state.getBlock(),1,barrel);
    }
    case GRENADE->{
     burst=Math.max(0,burst-1);cooldown=burst>0?5:30;
     var gr=new GrenadeShot(GRENADE_ENTITY.get(),sl);gr.setPos(muzzle.x,muzzle.y,muzzle.z);gr.setItem(new ItemStack(GRENADE_ROUND.get()));
     gr.setDeltaMovement(d.scale(GrenadeShot.SPEED).add(rnd.nextGaussian()*.012,0,rnd.nextGaussian()*.012));sl.addFreshEntity(gr);
     sl.sendParticles(ParticleTypes.CLOUD,muzzle.x,muzzle.y,muzzle.z,5,.1,.1,.1,.02);
     sl.playSound(null,muzzle.x,muzzle.y,muzzle.z,SoundEvents.DISPENSER_LAUNCH,SoundSource.BLOCKS,1.6F,.55F);
     sl.playSound(null,muzzle.x,muzzle.y,muzzle.z,MareEletrica.LAUNCHER_FIRE.get(),SoundSource.BLOCKS,.8F,1.6F);
     sl.blockEvent(worldPosition,state.getBlock(),1,0);
    }
   }
   if(shots%8==0||ammo==0)sync();else setChanged();
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("Ammo",ammo);t.putBoolean("Armed",armed);if(owner!=null)t.putUUID("Owner",owner);t.putFloat("Yaw",yawT);t.putFloat("Pitch",pitchT);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);ammo=t.getInt("Ammo");armed=!t.contains("Armed")||t.getBoolean("Armed");owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;yawT=t.getFloat("Yaw");pitchT=t.contains("Pitch")?t.getFloat("Pitch"):10;}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
  @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }

 /** Blast that hurts what is around it but leaves every block in place. */
 static void blast(Entity src,float power,float vehicleDamage,double vehicleRadius){
  var l=src.level();
  l.explode(src,null,null,src.getX(),src.getY(),src.getZ(),power,false,Level.ExplosionInteraction.NONE,ParticleTypes.EXPLOSION,ParticleTypes.EXPLOSION,MareEletrica.SILENT);
  if(vehicleDamage>0)for(var e:l.getEntitiesOfClass(Entity.class,src.getBoundingBox().inflate(vehicleRadius),x->x instanceof Jets.JetEntity||x instanceof Drones.DroneEntity||x instanceof Vehicles.Base))
   e.hurt(src.damageSources().mobProjectile(src,null),vehicleDamage);
 }

 /** Anti-aircraft shell: time fuse set for the target's distance + proximity fuse; bursts into a black puff. */
 public static final class FlakShell extends ThrowableItemProjectile {
  UUID owner;int fuse=40;
  public FlakShell(EntityType<? extends FlakShell> t,Level l){super(t,l);}
  @Override protected Item getDefaultItem(){return FLAK_SHELL.get();}
  @Override protected double getDefaultGravity(){return .01;}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<256*256;}
  @Override public void tick(){
   super.tick();
   if(level().isClientSide||isRemoved())return;
   if(--fuse<=0||!level().getEntitiesOfClass(Entity.class,getBoundingBox().inflate(3.5),e->wants(Kind.FLAK,e,owner)).isEmpty())burst();
  }
  @Override protected void onHit(HitResult r){super.onHit(r);if(!level().isClientSide&&!isRemoved())burst();}
  void burst(){
   blast(this,2.2F,10F,4.5);
   if(level() instanceof ServerLevel sl){sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY(),getZ(),26,.7,.7,.7,.02);sl.sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),20,.9,.9,.9,.04);}
   level().playSound(null,getX(),getY(),getZ(),MareEletrica.FRAG_BLAST.get(),SoundSource.NEUTRAL,1.2F,1.25F+random.nextFloat()*.2F);
   discard();
  }
  @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Fuse",fuse);if(owner!=null)t.putUUID("Owner",owner);}
  @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);fuse=t.getInt("Fuse");owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;}
 }

 /** 40mm grenade: lobbed in a low arc, bursts on impact (no block damage). */
 public static final class GrenadeShot extends ThrowableItemProjectile {
  public static final double SPEED=1.7,GRAVITY=.05;
  public GrenadeShot(EntityType<? extends GrenadeShot> t,Level l){super(t,l);}
  @Override protected Item getDefaultItem(){return GRENADE_ROUND.get();}
  @Override protected double getDefaultGravity(){return GRAVITY;}
  @Override public void tick(){super.tick();if(level().isClientSide)level().addParticle(ParticleTypes.SMOKE,getX(),getY(),getZ(),0,0,0);else if(tickCount>200)discard();}
  @Override protected void onHit(HitResult r){
   super.onHit(r);
   if(level().isClientSide||isRemoved())return;
   blast(this,2F,6F,3);
   if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+.3,getZ(),14,.6,.3,.6,.03);
   level().playSound(null,getX(),getY(),getZ(),MareEletrica.FRAG_BLAST.get(),SoundSource.NEUTRAL,1.5F,1.1F+random.nextFloat()*.2F);
   discard();
  }
 }

 // ---------------- ammo crate: up to 1000 rounds of one kind in one slot ----------------
 public static final class AmmoCrate extends Item {
  public AmmoCrate(Properties p){super(p);}
  static List<Item> accepted(){return List.of(Barrage.ROCKET.get(),Arsenal.RIFLE_ROUND.get(),FLAK_SHELL.get(),GRENADE_ROUND.get(),MareEletrica.CANNONBALL.get(),Missiles.MISSILE.get());}
  public static boolean accepts(Item i){return accepted().contains(i);}
  public static Item type(ItemStack s){
   var d=s.get(DataComponents.CUSTOM_DATA);if(d==null)return null;var t=d.copyTag();if(!t.contains("CrateAmmo")||t.getInt("CrateCount")<=0)return null;
   var rl=ResourceLocation.tryParse(t.getString("CrateAmmo"));return rl==null?null:BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
  }
  public static int count(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d==null?0:d.copyTag().getInt("CrateCount");}
  public static void set(ItemStack s,Item type,int n){
   CustomData.update(DataComponents.CUSTOM_DATA,s,t->{if(n<=0||type==null){t.remove("CrateAmmo");t.remove("CrateCount");}else{t.putString("CrateAmmo",BuiltInRegistries.ITEM.getKey(type).toString());t.putInt("CrateCount",n);}});
  }
  public static ItemStack of(Item type,int n){var s=new ItemStack(CRATE.get());set(s,type,n);return s;}
  /** Plain ammo of that kind, or a crate holding it. */
  public static boolean holds(ItemStack held,Item ammo){return held.is(ammo)||held.getItem() instanceof AmmoCrate&&type(held)==ammo;}
  /** Loads a machine from the stack in hand; returns how many rounds went in. */
  public static int feed(ItemStack held,Item ammo,int space,Player p){
   if(space<=0)return 0;
   if(held.is(ammo)){int n=Math.min(held.getCount(),space);if(!p.isCreative())held.shrink(n);return n;}
   if(held.getItem() instanceof AmmoCrate&&type(held)==ammo){int c=count(held),n=Math.min(c,space);if(!p.isCreative())set(held,ammo,c-n);return n;}
   return 0;
  }
  /** Drops what a broken machine held, packed in crates (nothing is lost). */
  public static void drop(Level l,BlockPos pos,Item type,int n){while(n>0){int k=Math.min(n,MAX_AMMO);Containers.dropItemStack(l,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,of(type,k));n-=k;}}
  /** Puts ammo into the crate; returns how many were taken. */
  public static int insert(ItemStack crate,ItemStack ammo){
   if(ammo.isEmpty()||!accepts(ammo.getItem()))return 0;
   Item t=type(crate);if(t!=null&&t!=ammo.getItem())return 0;
   int c=count(crate),n=Math.min(ammo.getCount(),MAX_AMMO-c);if(n<=0)return 0;
   set(crate,ammo.getItem(),c+n);return n;
  }
  public static ItemStack extract(ItemStack crate,int max){
   Item t=type(crate);if(t==null)return ItemStack.EMPTY;int c=count(crate),n=Math.min(Math.min(c,max),new ItemStack(t).getMaxStackSize());
   set(crate,t,c-n);return new ItemStack(t,n);
  }
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
   ItemStack crate=p.getItemInHand(hand);
   if(!level.isClientSide){
    var inv=p.getInventory();
    if(p.isShiftKeyDown()){   // take one stack out
     var out=extract(crate,99);
     if(out.isEmpty())p.displayClientMessage(Component.literal("Caixa vazia."),true);
     else{if(!inv.add(out))p.drop(out,false);level.playSound(null,p.blockPosition(),SoundEvents.BUNDLE_REMOVE_ONE,SoundSource.PLAYERS,1F,.8F);}
    }else{   // pull every round of its kind from the inventory (an empty crate takes the first ammo it finds)
     Item t=type(crate);int moved=0;
     if(t==null)for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(!s.isEmpty()&&accepts(s.getItem())){t=s.getItem();break;}}
     if(t!=null)for(int i=0;i<inv.getContainerSize()&&count(crate)<MAX_AMMO;i++){var s=inv.getItem(i);if(s.is(t)){int n=insert(crate,s);s.shrink(n);moved+=n;}}
     if(moved>0)level.playSound(null,p.blockPosition(),SoundEvents.BUNDLE_INSERT,SoundSource.PLAYERS,1F,.8F);
     else if(t==null)p.displayClientMessage(Component.literal("Sem munição no inventário (foguetes, balas de fuzil, granadas, balas de canhão, mísseis)."),true);
    }
    var t=type(crate);p.displayClientMessage(Component.literal(t==null?"Caixa de Munição: vazia":"Caixa: "+t.getDescription().getString()+" "+count(crate)+" / "+MAX_AMMO),true);
   }
   return InteractionResultHolder.sidedSuccess(crate,level.isClientSide);
  }
  /** Inventory: click the crate holding ammo on the cursor to pack it; right-click it with an empty cursor to take a stack out. */
  @Override public boolean overrideOtherStackedOnMe(ItemStack crate,ItemStack other,Slot slot,ClickAction action,Player p,SlotAccess access){
   if(!slot.allowModification(p))return false;
   if(other.isEmpty()){
    if(action!=ClickAction.SECONDARY)return false;
    var out=extract(crate,99);if(out.isEmpty())return false;access.set(out);p.playSound(SoundEvents.BUNDLE_REMOVE_ONE,.8F,.8F);return true;
   }
   int n=insert(crate,other);if(n<=0)return false;other.shrink(n);p.playSound(SoundEvents.BUNDLE_INSERT,.8F,.8F);return true;
  }
  @Override public boolean isBarVisible(ItemStack s){return count(s)>0;}
  @Override public int getBarWidth(ItemStack s){return Math.round(13F*count(s)/MAX_AMMO);}
  @Override public int getBarColor(ItemStack s){return 0xE8B040;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   Item type=type(s);
   t.add(Component.literal(type==null?"Vazia":type.getDescription().getString()+": "+count(s)+" / "+MAX_AMMO).withStyle(ChatFormatting.GOLD));
   t.add(Component.literal("§7Guarda até "+MAX_AMMO+" de um tipo de munição num slot só."));
   t.add(Component.literal("§8Botão direito: puxa a munição do inventário • Shift + direito: tira um pacote"));
   t.add(Component.literal("§8Clique numa máquina com a caixa para carregar"));
  }
 }
}
