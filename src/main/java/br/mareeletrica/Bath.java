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
 public static void register(){}
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
  var s=p.level().getBlockState(seat.blockPosition());if(!(s.getBlock() instanceof BathBlock)||!s.getValue(FILLED))return;
  if(p.isOnFire())p.clearFire();p.removeEffect(MobEffects.POISON);if(p.tickCount%60==0)p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,70,0,true,false,true));
  if(p.level() instanceof ServerLevel sl&&p.tickCount%40==0)sl.sendParticles(ParticleTypes.BUBBLE_POP,p.getX(),seat.getY()+.4,p.getZ(),6,.4,.05,.4,.02);
 }
}
