package br.mareeletrica;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class HoneyWaterItem extends PotableWaterItem {
    public HoneyWaterItem(Properties properties) { super(properties); }
    @Override protected int hydration() { return 12; }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) player.getFoodData().eat(2,.3F);
        return super.finishUsingItem(stack,level,entity);
    }
}
