package br.mareeletrica;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Eight independent utility functions added in 0.4.0. Logical server only. */
public final class UtilityMachines {
    private UtilityMachines() {}
    private static final Map<Item, Item> COMPACT = Map.ofEntries(
        Map.entry(Items.IRON_INGOT, Items.IRON_BLOCK), Map.entry(Items.GOLD_INGOT, Items.GOLD_BLOCK),
        Map.entry(Items.COPPER_INGOT, Items.COPPER_BLOCK), Map.entry(Items.COAL, Items.COAL_BLOCK),
        Map.entry(Items.DIAMOND, Items.DIAMOND_BLOCK), Map.entry(Items.EMERALD, Items.EMERALD_BLOCK),
        Map.entry(Items.LAPIS_LAZULI, Items.LAPIS_BLOCK), Map.entry(Items.REDSTONE, Items.REDSTONE_BLOCK),
        Map.entry(Items.RAW_IRON, Items.RAW_IRON_BLOCK), Map.entry(Items.RAW_COPPER, Items.RAW_COPPER_BLOCK),
        Map.entry(Items.RAW_GOLD, Items.RAW_GOLD_BLOCK), Map.entry(Items.NETHERITE_INGOT, Items.NETHERITE_BLOCK));

    public static boolean tick(MachineEntity m) {
        if (!(m.getLevel() instanceof ServerLevel level)) return false;
        return switch (m.kind) {
            case SOLAR_PANEL -> solar(m, level);
            case COMPACTOR, BUCKET_FILLER -> process(m, level);
            case EFFECT_CLEANSER -> cleanse(m, level);
            case FOOD_STATION -> feedPlayers(m, level);
            case PRESENCE_SENSOR -> sense(m, level);
            case APIARY_EXTRACTOR -> honeycomb(m, level);
            case MINING_BOOSTER -> haste(m, level);
            default -> false;
        };
    }
    public static boolean validInput(Kind kind, int slot, ItemStack stack) {
        if (slot != 0 || stack.isEmpty()) return false;
        return kind == Kind.COMPACTOR ? COMPACT.containsKey(stack.getItem()) : kind == Kind.BUCKET_FILLER && stack.is(Items.BUCKET);
    }
    public static boolean isMeal(ItemStack s) {
        return !s.isEmpty() && s.get(DataComponents.FOOD) != null
            && (s.is(Items.BREAD) || s.is(Items.APPLE) || s.is(Items.CARROT) || s.is(Items.BAKED_POTATO)
            || s.is(Items.COOKED_BEEF) || s.is(Items.COOKED_PORKCHOP) || s.is(Items.COOKED_CHICKEN)
            || s.is(Items.COOKED_MUTTON) || s.is(Items.COOKED_RABBIT) || s.is(Items.COOKED_COD)
            || s.is(Items.COOKED_SALMON) || s.is(Items.GOLDEN_CARROT));
    }
    private static List<Player> players(MachineEntity m, ServerLevel level, int radius) {
        Vec3 center = Vec3.atCenterOf(m.getBlockPos());
        return level.getEntitiesOfClass(Player.class, new AABB(m.getBlockPos()).inflate(radius),
            player -> player.isAlive() && !player.isSpectator() && player.distanceToSqr(center) <= radius * radius);
    }
    private static BlockPos front(MachineEntity m) {
        return m.getBlockPos().relative(m.getBlockState().getValue(MachineBlock.FACING));
    }
    private static boolean solar(MachineEntity m, ServerLevel level) {
        // A charged internal buffer may still export at night, like the water generators.
        m.lastRate = PowerNetwork.send(m, false, 1_000, Kind::core) * 2;
        if (!level.dimensionType().hasSkyLight() || level.dimensionType().hasCeiling()
            || !level.isDay() || level.isRaining() || !level.canSeeSky(m.getBlockPos().above())
            || !level.getFluidState(m.getBlockPos()).isEmpty()) { m.status = 25; return false; }
        int generated = m.clean.add(150);
        m.status = generated > 0 ? 1 : 10;
        return generated > 0;
    }
    private static boolean outputFits(MachineEntity m, ItemStack result) {
        ItemStack out = m.inventory.getStackInSlot(8);
        return (out.isEmpty() || ItemStack.isSameItemSameComponents(out, result))
            && out.getCount() + result.getCount() <= result.getMaxStackSize();
    }
    private static void output(MachineEntity m, ItemStack result) {
        ItemStack old = m.inventory.getStackInSlot(8);
        m.inventory.setStackInSlot(8, old.isEmpty() ? result : old.copyWithCount(old.getCount() + result.getCount()));
    }
    private static boolean process(MachineEntity m, ServerLevel level) {
        ItemStack input = m.inventory.getStackInSlot(0);
        boolean bucket = m.kind == Kind.BUCKET_FILLER;
        int needed = bucket ? 1 : 9, fee = bucket ? 100 : 50;
        if (!validInput(m.kind, 0, input) || input.getCount() < needed) { m.progress = 0; m.status = 6; return false; }
        BlockPos source = front(m);
        if (bucket && (!level.hasChunkAt(source) || !level.getBlockState(source).is(Blocks.WATER)
            || !level.getFluidState(source).isSource())) { m.progress = 0; m.status = 26; return false; }
        ItemStack result = new ItemStack(bucket ? Items.WATER_BUCKET : COMPACT.get(input.getItem()));
        if (!outputFits(m, result)) { m.status = 7; return false; }
        if (m.clean.stored() < fee) { m.status = 2; return false; }
        boolean complete = m.progress + 10 >= m.kind.duration();
        // Consume exactly one source, never a waterlogged block or flowing water.
        // Nothing is removed when the output is blocked or energy is insufficient.
        if (complete && bucket && !level.setBlock(source, Blocks.AIR.defaultBlockState(), 3)) { m.status = 26; return false; }
        m.clean.pay(fee); m.progress += 10; m.status = 1;
        if (complete) {
            m.inventory.extractItem(0, needed, false);
            output(m, result); m.progress = 0;
        }
        return true;
    }
    private static boolean cleanse(MachineEntity m, ServerLevel level) {
        boolean worked = false;
        for (Player player : players(m, level, 5)) {
            for (var effect : List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.MOVEMENT_SLOWDOWN,
                MobEffects.WEAKNESS, MobEffects.BLINDNESS, MobEffects.HUNGER)) {
                if (!player.hasEffect(effect)) continue;
                if (m.clean.stored() < 300) { m.status = 2; return worked; }
                if (player.removeEffect(effect)) {
                    m.clean.pay(300); worked = true;
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY()+1, player.getZ(), 4, .3, .5, .3, 0);
                    break; // One effect per player per half second.
                }
            }
        }
        if (worked) m.status = 1;
        return worked;
    }
    private static boolean feedPlayers(MachineEntity m, ServerLevel level) {
        m.workCursor = Math.max(0, m.workCursor - 10);
        if (m.workCursor > 0) return false;
        boolean stocked = false;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack meal = m.inventory.getStackInSlot(slot);
            if (!isMeal(meal)) continue;
            stocked = true;
            for (Player player : players(m, level, 4)) {
                if (player.isCreative() || !player.getFoodData().needsFood()) continue;
                if (m.clean.stored() < 100) { m.status = 2; return false; }
                FoodProperties food = meal.get(DataComponents.FOOD);
                player.getFoodData().eat(food);
                m.inventory.extractItem(slot, 1, false); m.clean.pay(100);
                m.workCursor = 40; m.status = 1; m.setChanged(); return true;
            }
        }
        if (!stocked) m.status = 6;
        return false;
    }
    private static boolean sense(MachineEntity m, ServerLevel level) {
        if (!m.clean.pay(10)) { m.status = 2; return false; }
        boolean found = !players(m, level, 6).isEmpty();
        m.status = found ? 28 : 29;
        return found; // ACTIVE drives the comparator; never emits into its own power input.
    }
    private static boolean honeycomb(MachineEntity m, ServerLevel level) {
        BlockPos hive = front(m);
        if (!level.hasChunkAt(hive)) { m.status = 27; return false; }
        var state = level.getBlockState(hive);
        // Restrict to vanilla beehives: other mods may have additional harvesting rules.
        if (!(state.is(Blocks.BEEHIVE) || state.is(Blocks.BEE_NEST)) || state.getValue(BeehiveBlock.HONEY_LEVEL) < 5) {
            m.status = 27; return false;
        }
        ItemStack product = new ItemStack(Items.HONEYCOMB, 3);
        ItemStackHandler copy = new ItemStackHandler(9);
        for (int i = 0; i < 9; i++) copy.setStackInSlot(i, m.inventory.getStackInSlot(i).copy());
        if (!ItemHandlerHelper.insertItemStacked(copy, product.copy(), false).isEmpty()) { m.status = 7; return false; }
        if (m.clean.stored() < 200) { m.status = 2; return false; }
        if (!level.setBlock(hive, state.setValue(BeehiveBlock.HONEY_LEVEL, 0), 3)) return false;
        ItemHandlerHelper.insertItemStacked(m.inventory, product, false);
        m.clean.pay(200); m.status = 1;
        // Keep the hive block entity and bees intact. Extraction does not anger or eject bees.
        return true;
    }
    private static boolean haste(MachineEntity m, ServerLevel level) {
        boolean worked = false;
        for (Player player : players(m, level, 8)) {
            var current = player.getEffect(MobEffects.DIG_SPEED);
            if (current != null && (current.getAmplifier() > 0 || current.getDuration() > 40)) continue;
            if (m.clean.stored() < 120) { m.status = 2; return worked; }
            if (player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, true, true))) {
                m.clean.pay(120); worked = true;
            }
        }
        if (worked) m.status = 1;
        return worked;
    }
}
