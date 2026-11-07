package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.registries.DeferredItem;

/** Portable base: one use builds a 9x9 concrete bunker in front of you, with an armoured door, command panel, siren,
 *  small core, solar panels on the roof, lights, a chest and a bed. Needs a mostly clear, flat spot. */
public final class PortableBase {
 public static final DeferredItem<KitItem> ITEM=MareEletrica.ITEMS.register("portable_base",()->new KitItem(new Item.Properties().stacksTo(1)));
 private PortableBase(){}
 public static void register(){}
 public static final class KitItem extends Item {
  public KitItem(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();var p=c.getPlayer();if(p==null)return InteractionResult.PASS;
   if(l.isClientSide)return InteractionResult.SUCCESS;
   Direction f=p.getDirection();Direction r=f.getClockWise();
   BlockPos floor=c.getClickedPos();BlockPos origin=floor.relative(f,6);   // centre of the 9x9, in front of the player
   // check: the 9x9x5 volume above the floor must be mostly empty
   int solid=0;
   for(int a=-4;a<=4;a++)for(int b=-4;b<=4;b++)for(int y=1;y<=5;y++){var q=origin.relative(f,a).relative(r,b).above(y);if(!l.getBlockState(q).canBeReplaced())solid++;}
   if(solid>12){p.displayClientMessage(Component.literal("§eLugar ocupado — precisa de um espaço livre de 9x9 e 5 de altura."),true);return InteractionResult.FAIL;}
   var sl=(ServerLevel)l;
   BlockState wall=Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),floorB=Blocks.GRAY_CONCRETE.defaultBlockState(),trim=Blocks.YELLOW_CONCRETE.defaultBlockState(),glass=Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState();
   for(int a=-4;a<=4;a++)for(int b=-4;b<=4;b++){
    var col=origin.relative(f,a).relative(r,b);
    l.setBlock(col,floorB,3);
    boolean edge=Math.abs(a)==4||Math.abs(b)==4;
    for(int y=1;y<=4;y++){var q=col.above(y);
     BlockState st=Blocks.AIR.defaultBlockState();
     if(edge)st=y==1?trim:(y==2||y==3)&&(Math.abs(a)+Math.abs(b))%3==1&&!(Math.abs(a)==4&&Math.abs(b)==4)?glass:wall;
     l.setBlock(q,st,3);}
    l.setBlock(col.above(5),(a+b)%4==0&&!edge?Blocks.GLOWSTONE.defaultBlockState():wall,3);
   }
   // armoured door in the middle of the wall facing the player
   var door=origin.relative(f,-4);
   var ds=BunkerDoor.BLOCK.get().defaultBlockState().setValue(DoorBlock.FACING,f).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT);
   l.setBlock(door.above(1),ds.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),3);
   l.setBlock(door.above(2),ds.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),3);
   // inside: panel, siren, chest, core, bed
   l.setBlock(origin.relative(f,3).above(1),BaseControl.PANEL.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,f.getOpposite()),3);
   if(l.getBlockEntity(origin.relative(f,3).above(1)) instanceof BaseControl.Panel panel){panel.owner=p.getUUID();panel.members.add(p.getGameProfile().getName());panel.setChanged();}
   l.setBlock(origin.relative(f,-3).relative(r,3).above(4),BaseControl.SIREN.get().defaultBlockState().setValue(net.minecraft.world.level.block.DirectionalBlock.FACING,Direction.DOWN),3);
   l.setBlock(origin.relative(r,-3).relative(f,3).above(1),Blocks.CHEST.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,r),3);
   l.setBlock(origin.relative(r,3).relative(f,3).above(1),MareEletrica.MACHINES.get(Kind.CORE_SMALL).get().defaultBlockState().setValue(MachineBlock.FACING,f.getOpposite()),3);
   var bed=origin.relative(r,3).relative(f,-1).above(1);
   l.setBlock(bed,Blocks.RED_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING,f.getOpposite()).setValue(net.minecraft.world.level.block.BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.FOOT),3);
   l.setBlock(bed.relative(f.getOpposite()),Blocks.RED_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING,f.getOpposite()).setValue(net.minecraft.world.level.block.BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.HEAD),3);
   // roof: solar panels wired down to the core with a cable
   var coreTop=origin.relative(r,3).relative(f,3);
   for(int y=2;y<=5;y++)l.setBlock(coreTop.above(y),MareEletrica.CABLE.get().defaultBlockState(),3);
   for(int b=0;b<=2;b++)l.setBlock(origin.relative(r,b).relative(f,3).above(6),MareEletrica.MACHINES.get(Kind.SOLAR_PANEL).get().defaultBlockState(),3);
   l.setBlock(coreTop.above(6),MareEletrica.CABLE.get().defaultBlockState(),3);
   for(int b=0;b<=3;b++)l.setBlock(origin.relative(r,b).relative(f,2).above(6),MareEletrica.CABLE.get().defaultBlockState(),3);
   // refresh cable connections
   for(int a=-4;a<=4;a++)for(int b=-4;b<=4;b++)for(int y=0;y<=7;y++){var q=origin.relative(f,a).relative(r,b).above(y);l.blockUpdated(q,l.getBlockState(q).getBlock());var s=l.getBlockState(q);if(s.getBlock() instanceof CableBlock)l.setBlock(q,net.minecraft.world.level.block.Block.updateFromNeighbourShapes(s,l,q),3);}
   sl.playSound(null,origin,SoundEvents.ANVIL_PLACE,SoundSource.BLOCKS,1F,.6F);
   sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,origin.getX()+.5,origin.getY()+3,origin.getZ()+.5,80,4,2,4,.05);
   p.displayClientMessage(Component.literal("§aBase montada! Defina a senha da porta blindada clicando nela."),true);
   if(!p.isCreative())c.getItemInHand().shrink(1);
   return InteractionResult.CONSUME;
  }
 }
}
