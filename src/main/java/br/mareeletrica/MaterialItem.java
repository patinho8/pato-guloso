package br.mareeletrica;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class MaterialItem extends Item {
    private final String id;
    public MaterialItem(String id, Properties properties) { super(properties); this.id = id; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("hint.mareeletrica." + id).withStyle(ChatFormatting.AQUA));
    }
}
