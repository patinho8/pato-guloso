package br.mareeletrica;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Coloured LED lamps: right-click turns them on/off, a redstone signal turns them on. Lit lamps glow in their colour. */
public final class Lamps {
 public static final String[] COLORS={"white","red","orange","yellow","lime","green","cyan","blue","purple","pink","magenta","light_blue","amber","teal","rainbow"};
 public static final String[] NAMES={"branca","vermelha","laranja","amarela","verde-limão","verde","ciano","azul","roxa","rosa","magenta","azul-claro","âmbar","turquesa","arco-íris"};
 public static final int[] RGB={0xFFF4E8,0xFF2020,0xFF7A10,0xFFE020,0x70FF30,0x20D040,0x20F0FF,0x2060FF,0xA030FF,0xFF50C0,0xFF28E6,0x50BEFF,0xFFAA28,0x14E6AA,-1};
 /** 0.72.0: the rainbow lamp cycles through the hues */
 public static final int RAINBOW=14;
 public static int rgb(int color,long time,float pt){if(color!=RAINBOW)return RGB[color];return java.awt.Color.HSBtoRGB(((time+pt)*.004F)%1F,.9F,1F)&0xFFFFFF;}
 public static final BooleanProperty LIT=BooleanProperty.create("lit");
 public static final List<DeferredBlock<LampBlock>> BLOCKS=new ArrayList<>();
 public static final List<DeferredItem<BlockItem>> ITEMS=new ArrayList<>();
 static{
  for(int i=0;i<COLORS.length;i++){final int c=i;
   var b=MareEletrica.BLOCKS.register("led_lamp_"+COLORS[i],()->new LampBlock(BlockBehaviour.Properties.of().strength(.6F).sound(SoundType.GLASS).noOcclusion().isViewBlocking((st,l,pp)->false).isSuffocating((st,l,pp)->false).lightLevel(s->s.getValue(LIT)?15:0),c));
   BLOCKS.add(b);ITEMS.add(MareEletrica.ITEMS.register("led_lamp_"+COLORS[i],()->new BlockItem(b.get(),new Item.Properties())));
  }
 }
 public static final Supplier<BlockEntityType<Glow>> ENTITY=MareEletrica.ENTITIES.register("led_lamp",()->BlockEntityType.Builder.of(Glow::new,BLOCKS.stream().map(Supplier::get).toArray(Block[]::new)).build(null));
 private Lamps(){}
 public static void register(){}

 public static final class LampBlock extends Block implements EntityBlock {
  public final int color;
  public LampBlock(BlockBehaviour.Properties p,int color){super(p);this.color=color;registerDefaultState(stateDefinition.any().setValue(LIT,true));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LIT);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Glow(p,s);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide){l.setBlock(p,s.cycle(LIT),3);l.playSound(null,p,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,.5F,s.getValue(LIT)?.8F:1.2F);}
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos from,boolean moving){
   if(!l.isClientSide&&l.hasNeighborSignal(p)&&!s.getValue(LIT))l.setBlock(p,s.setValue(LIT,true),3);
  }
 }
 public static final class Glow extends BlockEntity {public Glow(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}}
}
