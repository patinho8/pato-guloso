package br.mareeletrica;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class MachineItem extends BlockItem {
    public MachineItem(Block block, Item.Properties properties) { super(block, properties); }
    @Override public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c){if(((MachineBlock)getBlock()).kind==Kind.GIANT_WHEEL){var other=WheelConnections.wheel(c.getLevel(),c.getClickedPos());if(other!=null)return place(WheelConnections.placement(c,other,true));}return super.useOn(c);}
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        Kind kind = ((MachineBlock) getBlock()).kind;
        lines.add(Component.translatable("hint.mareeletrica." + kind.id).withStyle(ChatFormatting.AQUA));
        if (kind.voltage() > 0) lines.add(Component.literal("⚡ Sai tensão " + Kind.voltageName(kind.voltage())).withStyle(ChatFormatting.YELLOW));
        else if (kind.consumer && kind != Kind.POWER_CONTROLLER && kind != Kind.AUTO_CRANK) lines.add(Component.literal("⚡ Aguenta até tensão " + Kind.voltageName(kind.maxVoltage()) + " — mais que isso pifa (use um transformador)").withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatable(kind == Kind.POTABILIZER || kind.large() ? "hint.mareeletrica.potabilizer_interact" : "hint.mareeletrica.interact").withStyle(ChatFormatting.GRAY));
    }
}
