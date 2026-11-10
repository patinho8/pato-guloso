package br.mareeletrica;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.76.0: the claw-foot bathtub (2 blocks). Click with an empty hand fills it, click again to get in; Shift+click empties it.
 *  Your tame ducks love it: they hop in and swim around, and they all come when you take a bath. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class Bath {
 private Bath(){}
 public static final DeferredBlock<BathBlock> BATH=MareEletrica.BLOCKS.register("bathtub",()->new BathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(2F).sound(SoundType.STONE).noOcclusion()));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("bathtub",()->new BlockItem(BATH.get(),new Item.Properties().stacksTo(1)){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){
   out.add(Component.literal("§7Banheira de pé (2 blocos). Clique: enche de água • clique de novo: entrar"));
   out.add(Component.literal("§7Shift+clique: esvazia. §bSeus patos nadam nela§7 e vêm tomar banho com você."));}});
 /** 0.84.0: the big tub (3x3) — room for 4 people and a whole flock of ducks, with bubbles like a hot tub */
 public static final DeferredBlock<BigBathBlock> BIG=MareEletrica.BLOCKS.register("big_bathtub",()->new BigBathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(2.5F).sound(SoundType.STONE).noOcclusion()));
 public static final DeferredItem<BlockItem> BIG_ITEM=MareEletrica.ITEMS.register("big_bathtub",()->new BlockItem(BIG.get(),new Item.Properties().stacksTo(1)){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){
   out.add(Component.literal("§7Banheira grande (3x3), cabem 4 pessoas. Clique: enche • clique de novo: entrar"));
   out.add(Component.literal("§7Shift+clique: esvazia. §bBorbulha como ofurô§7 e o bando inteiro de patos nada junto."));}});
 public static void register(){}
 /** any tub, small or big, with water in it */
 public static boolean filled(BlockState s){return (s.getBlock() instanceof BathBlock||s.getBlock() instanceof BigBathBlock)&&s.getValue(FILLED);}
 public static boolean tub(BlockState s){return s.getBlock() instanceof BathBlock||s.getBlock() instanceof BigBathBlock;}
 /** middle of the water, and how far a duck may paddle along/across it */
 public static Vec3 middle(BlockPos pos,BlockState s){
  if(s.getBlock() instanceof BigBathBlock)return Vec3.atBottomCenterOf(BigBathBlock.center(pos,s));
  return Vec3.atBottomCenterOf(pos).add(Vec3.atBottomCenterOf(BathBlock.other(pos,s))).scale(.5);}
 public static Vec3 axis(BlockPos pos,BlockState s){Direction f=s.getValue(BathBlock.FACING);return new Vec3(f.getStepX(),0,f.getStepZ());}
 public static double along(BlockState s){return s.getBlock() instanceof BigBathBlock?1.0:.5;}
 public static double across(BlockState s){return s.getBlock() instanceof BigBathBlock?.9:.14;}
 public static List<BlockPos> cells(BlockPos pos,BlockState s){return s.getBlock() instanceof BigBathBlock?BigBathBlock.cells(BigBathBlock.center(pos,s),s.getValue(BathBlock.FACING)):List.of(pos,BathBlock.other(pos,s));}
 public static final net.minecraft.world.level.block.state.properties.IntegerProperty CELL=net.minecraft.world.level.block.state.properties.IntegerProperty.create("cell",0,8);
 static final double[][] BIG_BASE={{0,0,0,16,4,16}};
 /** 3x3: CELL = x + 3*z in the north-facing frame (x west→east, z north→south; the player stands to the north, the taps are on the far side) */
 public static final class BigBathBlock extends net.minecraft.world.level.block.HorizontalDirectionalBlock {
  final VoxelShape[][] shapes=new VoxelShape[9][4];
  public BigBathBlock(Properties p){super(p);
   for(int c=0;c<9;c++){int cx=c%3,cz=c/3;var boxes=new java.util.ArrayList<double[]>();boxes.add(new double[]{0,0,0,16,4,16});
    if(cz==0)boxes.add(new double[]{0,4,0,16,10.8,2});if(cz==2)boxes.add(new double[]{0,4,14,16,10.8,16});if(cx==0)boxes.add(new double[]{0,4,0,2,10.8,16});if(cx==2)boxes.add(new double[]{14,4,0,16,10.8,16});
    for(var d:Direction.Plane.HORIZONTAL)shapes[c][d.get2DDataValue()]=Furniture.shape(boxes.toArray(new double[0][]),d);}
   registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(CELL,1).setValue(FILLED,false));}
  @Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.HorizontalDirectionalBlock> codec(){return com.mojang.serialization.MapCodec.unit(this);}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,CELL,FILLED);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shapes[s.getValue(CELL)][s.getValue(FACING).get2DDataValue()];}
  /** local (dx,dz) in the north frame → world offset for facing f (same turn as the blockstate's y rotation) */
  static BlockPos rot(Direction f,int dx,int dz){return switch(f){case EAST->new BlockPos(-dz,0,dx);case SOUTH->new BlockPos(-dx,0,-dz);case WEST->new BlockPos(dz,0,-dx);default->new BlockPos(dx,0,dz);};}
  static BlockPos center(BlockPos pos,BlockState s){int c=s.getValue(CELL);return pos.subtract(rot(s.getValue(FACING),c%3-1,c/3-1));}
  static List<BlockPos> cells(BlockPos center,Direction f){var out=new java.util.ArrayList<BlockPos>();for(int c=0;c<9;c++)out.add(center.offset(rot(f,c%3-1,c/3-1)));return out;}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){
   Direction f=c.getHorizontalDirection().getOpposite();BlockPos mid=c.getClickedPos().offset(rot(f,0,1));
   for(var q:cells(mid,f))if(!q.equals(c.getClickedPos())&&(!c.getLevel().getBlockState(q).canBeReplaced(c)||!c.getLevel().getWorldBorder().isWithinBounds(q)))return null;
   return defaultBlockState().setValue(FACING,f).setValue(CELL,1);}
  @Override public void setPlacedBy(Level l,BlockPos pos,BlockState s,LivingEntity by,ItemStack st){
   if(l.isClientSide)return;var cs=cells(center(pos,s),s.getValue(FACING));for(int c=0;c<9;c++)if(c!=1)l.setBlock(cs.get(c),s.setValue(CELL,c),3);}
  @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos pos,BlockPos np){
   var cs=cells(center(pos,s),s.getValue(FACING));int i=cs.indexOf(np);
   if(i>=0&&!(n.is(this)&&n.getValue(CELL)==i&&n.getValue(FACING)==s.getValue(FACING)))return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
   return super.updateShape(s,d,n,l,pos,np);}
  @Override public BlockState playerWillDestroy(Level l,BlockPos pos,BlockState s,Player p){
   if(!l.isClientSide&&p.isCreative()){var cs=cells(center(pos,s),s.getValue(FACING));for(var q:cs)if(!q.equals(pos)&&l.getBlockState(q).is(this))l.setBlock(q,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),35);}
   return super.playerWillDestroy(l,pos,s,p);}
  static void setFilled(Level l,BlockPos pos,BlockState s,boolean on){for(var q:cells(center(pos,s),s.getValue(FACING))){var qs=l.getBlockState(q);if(qs.getBlock() instanceof BigBathBlock)l.setBlock(q,qs.setValue(FILLED,on),3);}}
  /** four places to sit, one in each corner, everyone facing the middle */
  static final double[][] SPOTS={{-.85,-.85},{.85,-.85},{-.85,.85},{.85,.85}};
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   if(p.isPassenger())return InteractionResult.PASS;
   if(l.isClientSide)return InteractionResult.SUCCESS;
   BlockPos mid=center(pos,s);Vec3 c=Vec3.atBottomCenterOf(mid);Direction f=s.getValue(FACING);
   if(p.isShiftKeyDown()){if(s.getValue(FILLED)){setFilled(l,pos,s,false);l.playSound(null,mid,SoundEvents.BUCKET_FILL,SoundSource.BLOCKS,1F,.7F);Pato.msg(p,"§7🛁 Banheira grande esvaziada.");}return InteractionResult.CONSUME;}
   if(!s.getValue(FILLED)){setFilled(l,pos,s,true);l.playSound(null,mid,SoundEvents.BUCKET_EMPTY,SoundSource.BLOCKS,1F,.8F);l.playSound(null,mid,SoundEvents.WEATHER_RAIN,SoundSource.BLOCKS,.7F,1.4F);
    if(l instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.SPLASH,c.x,c.y+.6,c.z,80,1.1,.1,1.1,.1);
    Pato.msg(p,"§b🛁 Banheira grande cheia! §7Clique de novo para entrar • Shift+clique esvazia");return InteractionResult.CONSUME;}
   // the free corner closest to where you clicked
   Vec3 at=null;double best=1e9;
   for(var sp:SPOTS){BlockPos o=rot(f,sp[0]<0?-1:1,sp[1]<0?-1:1);Vec3 q=c.add(o.getX()*Math.abs(sp[0]),.2,o.getZ()*Math.abs(sp[1]));
    if(!l.getEntitiesOfClass(Furniture.Seat.class,new net.minecraft.world.phys.AABB(q,q).inflate(.3)).isEmpty())continue;
    double d=q.distanceToSqr(p.position());if(d<best){best=d;at=q;}}
   if(at==null){Pato.msg(p,"§7A banheira já está cheia de gente!");return InteractionResult.CONSUME;}
   Vec3 look=c.subtract(at);float yaw=(float)Math.toDegrees(Math.atan2(-look.x,look.z));
   var seat=new Furniture.Seat(Furniture.SEAT.get(),l);seat.moveTo(at.x,at.y,at.z,yaw,0);l.addFreshEntity(seat);p.startRiding(seat);
   int n=0;for(var d:l.getEntitiesOfClass(Pato.Duck.class,p.getBoundingBox().inflate(20),d->d.isTame()&&d.isOwnedBy(p)&&!d.racer()&&!d.isPassenger()&&d.mode()!=Pato.SIT))if(d.goBath(mid,true))n++;
   Pato.msg(p,"§b🛁 Que delícia de ofurô! "+(n>0?"§e"+n+" pato"+(n>1?"s vieram":" veio")+" nadar com você!":"§7(apaga fogo e regenera)"));
   return InteractionResult.CONSUME;}
  @Override public void animateTick(BlockState s,Level l,BlockPos pos,RandomSource r){
   if(!s.getValue(FILLED))return;double y=pos.getY()+8.6/16;
   if(r.nextInt(2)==0)l.addParticle(ParticleTypes.BUBBLE_POP,pos.getX()+.1+r.nextDouble()*.8,y,pos.getZ()+.1+r.nextDouble()*.8,0,.03,0);
   if(s.getValue(CELL)==4&&r.nextInt(2)==0)l.addParticle(ParticleTypes.BUBBLE_COLUMN_UP,pos.getX()-.5+r.nextDouble()*2,y-.15,pos.getZ()-.5+r.nextDouble()*2,0,.05,0);
   if(s.getValue(CELL)==4&&r.nextInt(3)==0)l.addParticle(ParticleTypes.CLOUD,pos.getX()-.3+r.nextDouble()*1.6,y+.4,pos.getZ()-.3+r.nextDouble()*1.6,0,.025,0);
  }
 }
 public static final EnumProperty<BedPart> PART=BlockStateProperties.BED_PART;
 public static final BooleanProperty FILLED=BooleanProperty.create("filled");
 // shapes for the north-facing foot (at the clicked block) and head (towards +z)
 static final double[][] FOOT={{1,0,1,15,4,16},{1,4,1,15,10.8,3},{1,4,1,3,10.8,16},{13,4,1,15,10.8,16}};
 static final double[][] HEAD={{1,0,0,15,4,15},{1,4,13,15,10.8,15},{1,4,0,3,10.8,15},{13,4,0,15,10.8,15}};
 public static final class BathBlock extends Furniture.ShapeBlock {
  final VoxelShape[] head=new VoxelShape[4];
  public BathBlock(Properties p){super(p,FOOT);for(var d:Direction.Plane.HORIZONTAL)head[d.get2DDataValue()]=Furniture.shape(HEAD,d);
   registerDefaultState(defaultBlockState().setValue(PART,BedPart.FOOT).setValue(FILLED,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,PART,FILLED);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.getValue(PART)==BedPart.HEAD?head[s.getValue(FACING).get2DDataValue()]:super.getShape(s,l,p,c);}
  /** the other half: the head is behind the foot (away from whoever placed it) */
  static BlockPos other(BlockPos pos,BlockState s){Direction f=s.getValue(FACING);return s.getValue(PART)==BedPart.FOOT?pos.relative(f.getOpposite()):pos.relative(f);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){
   Direction f=c.getHorizontalDirection().getOpposite();BlockPos h=c.getClickedPos().relative(f.getOpposite());
   if(!c.getLevel().getBlockState(h).canBeReplaced(c)||!c.getLevel().getWorldBorder().isWithinBounds(h))return null;
   return defaultBlockState().setValue(FACING,f);}
  @Override public void setPlacedBy(Level l,BlockPos pos,BlockState s,LivingEntity by,ItemStack st){
   if(!l.isClientSide)l.setBlock(other(pos,s),s.setValue(PART,BedPart.HEAD),3);}
  @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos pos,BlockPos np){
   if(np.equals(other(pos,s))&&!(n.is(this)&&n.getValue(PART)!=s.getValue(PART)))return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
   return super.updateShape(s,d,n,l,pos,np);}
  @Override public BlockState playerWillDestroy(Level l,BlockPos pos,BlockState s,Player p){
   if(!l.isClientSide&&p.isCreative()&&s.getValue(PART)==BedPart.HEAD){BlockPos o=other(pos,s);var os=l.getBlockState(o);if(os.is(this)){l.setBlock(o,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),35);l.levelEvent(p,2001,o,Block.getId(os));}}
   return super.playerWillDestroy(l,pos,s,p);}
  /** both halves share the water */
  static void setFilled(Level l,BlockPos pos,BlockState s,boolean on){l.setBlock(pos,s.setValue(FILLED,on),3);BlockPos o=other(pos,s);var os=l.getBlockState(o);if(os.getBlock() instanceof BathBlock)l.setBlock(o,os.setValue(FILLED,on),3);}
  /** where you sit: the head half, facing the taps-free end */
  static Vec3 seat(BlockPos pos,BlockState s){BlockPos h=s.getValue(PART)==BedPart.HEAD?pos:other(pos,s);Direction f=s.getValue(FACING);return Vec3.atBottomCenterOf(h).add(f.getStepX()*.12,.2,f.getStepZ()*.12);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   if(p.isPassenger())return InteractionResult.PASS;
   if(l.isClientSide)return InteractionResult.SUCCESS;
   if(p.isShiftKeyDown()){if(s.getValue(FILLED)){setFilled(l,pos,s,false);l.playSound(null,pos,SoundEvents.BUCKET_FILL,SoundSource.BLOCKS,1F,.8F);Pato.msg(p,"§7🛁 Banheira esvaziada.");}return InteractionResult.CONSUME;}
   if(!s.getValue(FILLED)){setFilled(l,pos,s,true);l.playSound(null,pos,SoundEvents.BUCKET_EMPTY,SoundSource.BLOCKS,1F,.9F);l.playSound(null,pos,SoundEvents.WEATHER_RAIN,SoundSource.BLOCKS,.5F,1.6F);
    if(l instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.SPLASH,pos.getX()+.5,pos.getY()+.6,pos.getZ()+.5,30,.4,.1,.4,.1);
    Pato.msg(p,"§b🛁 Banheira cheia! §7Clique de novo para entrar • Shift+clique esvazia");return InteractionResult.CONSUME;}
   Vec3 at=seat(pos,s);
   if(!l.getEntitiesOfClass(Furniture.Seat.class,new net.minecraft.world.phys.AABB(BlockPos.containing(at))).isEmpty())return InteractionResult.CONSUME;
   var seat=new Furniture.Seat(Furniture.SEAT.get(),l);seat.moveTo(at.x,at.y,at.z,s.getValue(FACING).toYRot(),0);l.addFreshEntity(seat);p.startRiding(seat);
   int n=0;for(var d:l.getEntitiesOfClass(Pato.Duck.class,p.getBoundingBox().inflate(20),d->d.isTame()&&d.isOwnedBy(p)&&!d.racer()&&!d.isPassenger()&&d.mode()!=Pato.SIT))if(d.goBath(BlockPos.containing(at),true))n++;
   Pato.msg(p,"§b🛁 Ahhh, que banho bom! "+(n>0?"§e"+n+" pato"+(n>1?"s vieram":" veio")+" tomar banho com você!":"§7(apaga fogo e regenera)"));
   return InteractionResult.CONSUME;}
  @Override public void animateTick(BlockState s,Level l,BlockPos pos,RandomSource r){
   if(!s.getValue(FILLED))return;double y=pos.getY()+8.6/16;
   if(r.nextInt(3)==0)l.addParticle(ParticleTypes.BUBBLE_POP,pos.getX()+.2+r.nextDouble()*.6,y,pos.getZ()+.2+r.nextDouble()*.6,0,.02,0);
   if(s.getValue(PART)==BedPart.HEAD&&r.nextInt(4)==0){Direction f=s.getValue(FACING);double x=pos.getX()+.5-f.getStepX()*.3,z=pos.getZ()+.5-f.getStepZ()*.3;l.addParticle(ParticleTypes.CLOUD,x,y+.5,z,0,.02,0);}
  }
 }
 /** sitting in a filled tub: no fire, no poison, slow healing */
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  var p=e.getEntity();if(p.level().isClientSide||p.tickCount%20!=0||!(p.getVehicle() instanceof Furniture.Seat seat))return;
  var s=p.level().getBlockState(seat.blockPosition());if(!filled(s))return;
  if(p.isOnFire())p.clearFire();p.removeEffect(MobEffects.POISON);if(p.tickCount%60==0)p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,70,0,true,false,true));
  if(p.level() instanceof ServerLevel sl&&p.tickCount%40==0)sl.sendParticles(ParticleTypes.BUBBLE_POP,p.getX(),seat.getY()+.4,p.getZ(),6,.4,.05,.4,.02);
 }
}
