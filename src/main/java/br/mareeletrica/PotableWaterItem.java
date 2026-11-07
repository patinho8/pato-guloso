package br.mareeletrica;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Drinkable output of the potabilizer; hydration is applied on the server. */
public class PotableWaterItem extends Item {
    public PotableWaterItem(Properties properties) { super(properties.stacksTo(16)); }
    protected int hydration() { return 10; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level,player,hand);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide) return stack;
        if (entity instanceof Player player) {
            Hydration.drink(player,hydration());
            player.awardStat(Stats.ITEM_USED.get(this));
            return ItemUtils.createFilledResult(stack,player,new ItemStack(Items.GLASS_BOTTLE));
        }
        stack.shrink(1);
        if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
        entity.spawnAtLocation(new ItemStack(Items.GLASS_BOTTLE)); return stack;
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack,context,lines,flag);
        lines.add(Component.translatable(this instanceof HoneyWaterItem ? "hint.mareeletrica.honey_water" : "hint.mareeletrica.potable_water").withStyle(ChatFormatting.AQUA));
    }
}
