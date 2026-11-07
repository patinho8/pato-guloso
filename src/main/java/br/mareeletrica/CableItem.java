package br.mareeletrica;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class CableItem extends BlockItem {
    public CableItem(CableBlock block,Properties properties){super(block,properties);}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        super.appendHoverText(stack,context,lines,flag);
        lines.add(Component.translatable("hint.mareeletrica."+(((CableBlock)getBlock()).voltageLimit<4?"transformer":getBlock()==MareEletrica.WIND_CABLE.get()?"wind_cable":((CableBlock)getBlock()).cyber?"cyber_cable":"cable")).withStyle(net.minecraft.ChatFormatting.AQUA));
    }
}
