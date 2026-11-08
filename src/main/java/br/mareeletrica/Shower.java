package br.mareeletrica;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.74.0: the shower — goes on the wall above head height; click turns the water on and off. The water falls to the floor,
 *  makes noise, puts out anyone on fire under it (and fire on the floor) and leaves you clean and refreshed. */
public final class Shower {
 private Shower(){}
 public static final DeferredBlock<ShowerBlock> SHOWER=MareEletrica.BLOCKS.register("shower",()->new ShowerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F).sound(SoundType.METAL).noOcclusion().noCollission()));
 public static final Supplier<BlockEntityType<ShowerEntity>> ENTITY=MareEletrica.ENTITIES.register("shower",()->BlockEntityType.Builder.of(ShowerEntity::new,SHOWER.get()).build(null));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("shower",()->new BlockItem(SHOWER.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){out.add(Component.literal("§7Coloque na parede, acima da cabeça. Clique: liga/desliga a água."));out.add(Component.literal("§7Apaga fogo e deixa quem toma banho §brevigorado§7."));}});
 public static void register(){}
 public static final class ShowerBlock extends Furniture.ShapeBlock implements EntityBlock {
  public static final BooleanProperty ON=BlockStateProperties.POWERED;
  public ShowerBlock(Properties p){super(p,new double[][]{{6,3,14,10,7,16},{7.2,6,13.6,8.8,13.4,15.2},{7.2,11.8,7,8.8,13.4,15},{4,9.2,2.5,12,11.2,10.5}});registerDefaultState(defaultBlockState().setValue(ON,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ON);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   l.setBlock(pos,s.cycle(ON),3);l.playSound(null,pos,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.5F,s.getValue(ON)?.7F:1.1F);
   if(!s.getValue(ON))l.playSound(null,pos,SoundEvents.BUCKET_EMPTY,SoundSource.BLOCKS,.6F,1.3F);
   return InteractionResult.sidedSuccess(l.isClientSide);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ShowerEntity(p,s);}
  @SuppressWarnings("unchecked")
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return t!=ENTITY.get()?null:(BlockEntityTicker<T>)(BlockEntityTicker<ShowerEntity>)ShowerEntity::tick;}
 }
 public static final class ShowerEntity extends BlockEntity {
  int age;
  public ShowerEntity(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  /** the centre of the shower head in world space */
  static Vec3 head(BlockPos pos,Direction f){Vec3 c=Vec3.atLowerCornerOf(pos).add(.5,9.2/16,.5);return c.add(f.getStepX()*(1.5/16),0,f.getStepZ()*(1.5/16));}
  static void tick(Level l,BlockPos pos,BlockState s,ShowerEntity e){
   if(!s.getValue(ShowerBlock.ON))return;e.age++;Direction f=s.getValue(ShowerBlock.FACING);Vec3 h=head(pos,f);
   // how far the water falls
   int drop=1;while(drop<6&&!l.getBlockState(pos.below(drop)).isFaceSturdy(l,pos.below(drop),Direction.UP)&&l.getBlockState(pos.below(drop)).getFluidState().isEmpty())drop++;
   double floor=pos.getY()-drop+1;
   if(l.isClientSide){var r=l.random;
    for(int i=0;i<6;i++){double a=r.nextDouble()*Math.PI*2,d=Math.sqrt(r.nextDouble())*.22;l.addParticle(ParticleTypes.FALLING_WATER,h.x+Math.cos(a)*d,h.y-.02,h.z+Math.sin(a)*d,0,0,0);}
    for(int i=0;i<3;i++){double a=r.nextDouble()*Math.PI*2,d=Math.sqrt(r.nextDouble())*.4;l.addParticle(ParticleTypes.SPLASH,h.x+Math.cos(a)*d,floor+.05,h.z+Math.sin(a)*d,0,.1,0);}
    if(e.age%3==0)l.addParticle(ParticleTypes.RAIN,h.x+(r.nextDouble()-.5)*.5,floor+.05,h.z+(r.nextDouble()-.5)*.5,0,0,0);
    if(e.age%5==0){double y=floor+r.nextDouble()*(h.y-floor);l.addParticle(ParticleTypes.CLOUD,h.x+(r.nextDouble()-.5)*.6,y,h.z+(r.nextDouble()-.5)*.6,0,.01,0);}
    return;}
   if(e.age%30==1)l.playSound(null,pos,SoundEvents.WEATHER_RAIN_ABOVE,SoundSource.BLOCKS,.35F,1.4F);
   if(e.age%5!=0)return;
   var box=new AABB(h.x-.45,floor,h.z-.45,h.x+.45,h.y,h.z+.45);
   for(var en:l.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,box)){if(en.isOnFire()){en.clearFire();l.playSound(null,en.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.5F,1.5F);}
    if(en instanceof LivingEntity le){le.removeEffect(MobEffects.POISON);if(e.age%40==0)le.addEffect(new MobEffectInstance(MobEffects.REGENERATION,60,0,true,false,true));}}
   BlockPos fl=BlockPos.containing(h.x,floor,h.z);for(BlockPos b:BlockPos.betweenClosed(fl.offset(-1,0,-1),fl.offset(1,drop-1,1)))if(l.getBlockState(b).is(BlockTags_FIRE)){l.removeBlock(b,false);}
  }
  static final net.minecraft.tags.TagKey<Block> BlockTags_FIRE=net.minecraft.tags.BlockTags.FIRE;
 }
}
