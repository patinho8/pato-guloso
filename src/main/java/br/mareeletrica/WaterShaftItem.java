package br.mareeletrica;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.InteractionResult;
public final class WaterShaftItem extends BlockItem {
 public WaterShaftItem(Block block,Properties p){super(block,p);}
 @Override public InteractionResult useOn(UseOnContext c){var wheel=WheelConnections.wheel(c.getLevel(),c.getClickedPos());return wheel==null?super.useOn(c):place(WheelConnections.placement(c,wheel,false));}
}
