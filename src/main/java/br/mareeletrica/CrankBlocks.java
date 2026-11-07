package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** The hand crank: place it touching a giant wheel and right-click to turn it by hand. */
public final class CrankBlocks {
 public static final DeferredBlock<HandCrank> HAND_CRANK=MareEletrica.BLOCKS.register("hand_crank",()->new HandCrank(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2F).noOcclusion().sound(SoundType.METAL)));
 public static final DeferredItem<CrankItem> HAND_CRANK_ITEM=MareEletrica.ITEMS.register("hand_crank",()->new CrankItem(new Item.Properties()));
 private CrankBlocks(){}
 public static void register(){}

 /** Hand crank: right-click a giant wheel to mount it on the wheel's axle, then right-click the wheel to turn it. */
 public static final class CrankItem extends Item {
  public CrankItem(Properties p){super(p);}
  @Override public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c){
   var level=c.getLevel();var wheel=PotabilizerStructure.machineAt(level,c.getClickedPos());
   if(wheel==null||wheel.kind!=Kind.GIANT_WHEEL){if(!level.isClientSide&&c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("Clique na roda gigante para encaixar a manivela."),true);return InteractionResult.FAIL;}
   if(wheel.crank){if(!level.isClientSide&&c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("Essa roda já tem manivela. Clique nela com a mão vazia para girar."),true);return InteractionResult.FAIL;}
   if(!level.isClientSide){
    wheel.crank=true;wheel.syncAim();
    if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
    level.playSound(null,c.getClickedPos(),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,1F,.9F);
    if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("Manivela encaixada. Clique na roda com a mão vazia para girar."),true);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
 }
 /** Legacy: crank blocks placed by 0.29-0.36 keep working. */
 public static final class HandCrank extends Block {
  private static final VoxelShape SHAPE=Block.box(3,0,3,13,14,13);
  public HandCrank(BlockBehaviour.Properties properties){super(properties);}
  @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext ctx){return SHAPE;}
  @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
   if(!level.isClientSide){
    var wheel=ControlMachines.wheelAt(level,pos);
    if(wheel==null){player.displayClientMessage(Component.literal("Encoste a manivela em uma roda gigante."),true);return InteractionResult.CONSUME;}
    wheel.drive=Math.min(ControlMachines.DRIVE_CAP,wheel.drive+100);wheel.setChanged();
    player.causeFoodExhaustion(.35F);
    level.playSound(null,pos,SoundEvents.WOODEN_TRAPDOOR_OPEN,SoundSource.BLOCKS,.6F,.6F+level.random.nextFloat()*.2F);
    player.displayClientMessage(Component.literal("Roda girando por mais "+(wheel.drive/20)+" s."),true);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
 }
}
