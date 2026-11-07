package br.mareeletrica;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

/** New industry and utility machines. All work runs on the logical server. */
public final class IndustrialMachines {
    private IndustrialMachines() {}
    public static boolean tick(MachineEntity m) {
        if (!(m.getLevel() instanceof ServerLevel level)) return false;
        if (m.kind.industrial()) return process(m, level);
        return switch (m.kind) {
            case ANIMAL_FEEDER -> feed(m, level);
            case SHEARER -> shear(m, level);
            case EXTINGUISHER -> extinguish(m, level);
            case BRIDGE_PROJECTOR -> bridge(m, level);
            default -> false;
        };
    }
    public static boolean isFeed(ItemStack s) {
        return s.is(Items.WHEAT) || s.is(Items.WHEAT_SEEDS) || s.is(Items.CARROT) || s.is(Items.POTATO)
            || s.is(Items.BEETROOT) || s.is(Items.BEETROOT_SEEDS) || s.is(Items.MELON_SEEDS) || s.is(Items.PUMPKIN_SEEDS);
    }
    private static boolean enchanted(ItemStack s) {
        return !s.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
            || !s.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }
    public static boolean validInput(Kind kind, int slot, ItemStack s) {
        if (s.isEmpty() || slot < 0 || slot >= kind.inputs()) return false;
        return switch (kind) {
            case PRESS -> s.is(MareEletrica.IRON_GRIT.get()) || s.is(MareEletrica.COPPER_GRIT.get()) || s.is(MareEletrica.GOLD_GRIT.get());
            case SAWMILL -> !planks(s).isEmpty();
            case FREEZER -> s.is(Items.SNOWBALL) || s.is(Items.ICE) || s.is(Items.PACKED_ICE);
            case REPAIRER -> slot == 0 ? s.isDamageableItem() && s.isDamaged() : s.is(MareEletrica.IRON_PLATE.get());
            case DISENCHANTER -> enchanted(s);
            case ASSEMBLER -> s.is(slot == 0 ? MareEletrica.IRON_PLATE.get() : slot == 1 ? MareEletrica.COPPER_COIL.get() : MareEletrica.GOLD_CONTACT.get());
            default -> false;
        };
    }
    private static ItemStack planks(ItemStack input) {
        String id = BuiltInRegistries.ITEM.getKey(input.getItem()).toString();
        // Explicit supported vanilla logs/wood, including stripped variants and Nether stems.
        for (String wood : List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "crimson", "warped")) {
            String suffix = wood.equals("crimson") || wood.equals("warped") ? "stem" : "log";
            String bark = suffix.equals("stem") ? "hyphae" : "wood";
            if (id.equals("minecraft:" + wood + "_" + suffix) || id.equals("minecraft:stripped_" + wood + "_" + suffix)
                || id.equals("minecraft:" + wood + "_" + bark) || id.equals("minecraft:stripped_" + wood + "_" + bark))
                return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(wood + "_planks")), 6);
        }
        return ItemStack.EMPTY;
    }
    private static boolean waterNear(MachineEntity m, ServerLevel level) {
        if (level.getFluidState(m.getBlockPos()).is(FluidTags.WATER)) return true;
        for (Direction d : Direction.values()) {
            BlockPos p = m.getBlockPos().relative(d);
            if (level.hasChunkAt(p) && level.getFluidState(p).is(FluidTags.WATER)) return true;
        }
        return false;
    }
    private static boolean process(MachineEntity m, ServerLevel level) {
        ItemStack input = m.inventory.getStackInSlot(0), result = ItemStack.EMPTY;
        int count = 1, fee = 100;
        switch (m.kind) {
            case PRESS -> {
                count = 2;
                if (input.is(MareEletrica.IRON_GRIT.get())) result = new ItemStack(MareEletrica.IRON_PLATE.get());
                else if (input.is(MareEletrica.COPPER_GRIT.get())) result = new ItemStack(MareEletrica.COPPER_COIL.get());
                else if (input.is(MareEletrica.GOLD_GRIT.get())) result = new ItemStack(MareEletrica.GOLD_CONTACT.get());
            }
            case SAWMILL -> { result = planks(input); fee = 50; }
            case FREEZER -> {
                fee = 50;
                if (input.is(Items.SNOWBALL)) {
                    if (!waterNear(m, level)) { m.status = 3; return false; }
                    result = new ItemStack(Items.ICE);
                } else if (input.is(Items.ICE)) { count = 9; result = new ItemStack(Items.PACKED_ICE); }
                else if (input.is(Items.PACKED_ICE)) { count = 9; result = new ItemStack(Items.BLUE_ICE); }
            }
            case REPAIRER -> {
                fee = 250;
                if (input.isDamageableItem() && input.isDamaged() && m.inventory.getStackInSlot(1).is(MareEletrica.IRON_PLATE.get())) {
                    result = input.copyWithCount(1); result.setDamageValue(Math.max(0, input.getDamageValue() - 100));
                }
            }
            case DISENCHANTER -> {
                fee = 250;
                if (enchanted(input)) {
                    result = input.is(Items.ENCHANTED_BOOK) ? new ItemStack(Items.BOOK) : input.copyWithCount(1);
                    result.remove(DataComponents.ENCHANTMENTS); result.remove(DataComponents.STORED_ENCHANTMENTS);
                    result.remove(DataComponents.REPAIR_COST);
                }
            }
            case ASSEMBLER -> {
                fee = 150;
                if (input.is(MareEletrica.IRON_PLATE.get()) && m.inventory.getStackInSlot(1).is(MareEletrica.COPPER_COIL.get())
                    && m.inventory.getStackInSlot(2).is(MareEletrica.GOLD_CONTACT.get())) result = new ItemStack(MareEletrica.CONTROL_MODULE.get());
            }
            default -> { return false; }
        }
        if (result.isEmpty() || input.getCount() < count) { m.progress = 0; m.status = 6; return false; }
        ItemStack output = m.inventory.getStackInSlot(8);
        if ((!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, result))
            || output.getCount() + result.getCount() > result.getMaxStackSize()) { m.status = 7; return false; }
        if (!m.clean.pay(fee)) { m.status = 2; return false; }
        m.progress += 10; m.status = 1;
        if (m.progress >= m.kind.duration()) {
            m.inventory.extractItem(0, count, false);
            if (m.kind == Kind.REPAIRER || m.kind == Kind.ASSEMBLER) m.inventory.extractItem(1, 1, false);
            if (m.kind == Kind.ASSEMBLER) m.inventory.extractItem(2, 1, false);
            m.inventory.setStackInSlot(8, output.isEmpty() ? result : output.copyWithCount(output.getCount() + result.getCount()));
            m.progress = 0;
        }
        return true;
    }
    private static boolean ready(MachineEntity m, int energy) {
        m.workCursor = Math.max(0, m.workCursor - 10);
        if (m.workCursor > 0) return false;
        if (m.clean.stored() < energy) { m.status = 2; return false; }
        return true;
    }
    private static boolean feed(MachineEntity m, ServerLevel level) {
        if (!ready(m, 150)) return false;
        boolean food = false;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack feed = m.inventory.getStackInSlot(slot);
            if (!isFeed(feed)) continue;
            food = true;
            for (Animal animal : level.getEntitiesOfClass(Animal.class, new AABB(m.getBlockPos()).inflate(5), a -> a.isAlive() && !a.isBaby() && a.canFallInLove())) {
                if (!animal.isFood(feed)) continue;
                animal.setInLove(null); m.inventory.extractItem(slot, 1, false); m.clean.pay(150);
                m.workCursor = 20; m.status = 1; m.setChanged(); return true;
            }
        }
        if (!food) m.status = 6;
        return false;
    }
    private static boolean fits(MachineEntity m, ItemStack stack) {
        ItemStackHandler copy = new ItemStackHandler(9);
        for (int i = 0; i < 9; i++) copy.setStackInSlot(i, m.inventory.getStackInSlot(i).copy());
        return ItemHandlerHelper.insertItemStacked(copy, stack.copy(), false).isEmpty();
    }
    private static boolean shear(MachineEntity m, ServerLevel level) {
        if (!ready(m, 150)) return false;
        for (Sheep sheep : level.getEntitiesOfClass(Sheep.class, new AABB(m.getBlockPos()).inflate(5), s -> s.isAlive() && s.readyForShearing())) {
            var wool = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(sheep.getColor().getName() + "_wool"));
            ItemStack drops = new ItemStack(wool, 1 + level.random.nextInt(3));
            if (!fits(m, drops)) { m.status = 7; continue; }
            sheep.setSheared(true); ItemHandlerHelper.insertItemStacked(m.inventory, drops, false); m.clean.pay(150);
            m.workCursor = 40; m.status = 1; m.setChanged(); return true;
        }
        return false;
    }
    private static boolean extinguish(MachineEntity m, ServerLevel level) {
        if (!waterNear(m, level)) { m.status = 3; return false; }
        if (m.clean.stored() < 20) { m.status = 2; return false; }
        boolean worked = false;
        for (int i = 0; i < 32; i++) {
            int index = Math.floorMod(m.workCursor++, 405);
            BlockPos p = m.getBlockPos().offset(index % 9 - 4, index / 81 - 2, (index / 9) % 9 - 4);
            if (!level.hasChunkAt(p) || !(level.getBlockState(p).getBlock() instanceof BaseFireBlock) || m.clean.stored() < 20) continue;
            if (level.setBlock(p, Blocks.AIR.defaultBlockState(), 3)) { m.clean.pay(20); worked = true; }
        }
        m.workCursor = Math.floorMod(m.workCursor, 405);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(m.getBlockPos()).inflate(4), e -> e.isAlive() && e.isOnFire())) {
            if (!m.clean.pay(50)) break;
            entity.clearFire(); worked = true;
        }
        if (worked) { m.status = 1; level.sendParticles(ParticleTypes.SPLASH, m.getBlockPos().getX()+0.5, m.getBlockPos().getY()+1, m.getBlockPos().getZ()+0.5, 10, 1, 0.5, 1, 0); }
        return worked;
    }
    private static boolean bridge(MachineEntity m, ServerLevel level) {
        int reach = BridgePower.reach(m.clean.stored());
        if (reach == 0) { m.status = 2; return false; }
        Direction forward = m.getBlockState().getValue(MachineBlock.FACING), side = forward.getClockWise();
        int tiles = 0;
        for (int distance = 1; distance <= reach; distance++) {
            BlockPos center = m.getBlockPos().relative(forward, distance);
            if (!level.hasChunkAt(center) || level.isOutsideBuildHeight(center)) break;
            var centerState = level.getBlockState(center);
            if ((!centerState.isAir() && !centerState.is(MareEletrica.BRIDGE_TILE.get())) || !level.getFluidState(center).isEmpty()) break;
            for (int offset = -1; offset <= 1; offset++) {
                BlockPos p = center.relative(side, offset);
                if (!level.hasChunkAt(p)) continue;
                var state = level.getBlockState(p);
                if ((!state.isAir() && !state.is(MareEletrica.BRIDGE_TILE.get())) || !level.getFluidState(p).isEmpty()) continue;
                if (!state.is(MareEletrica.BRIDGE_TILE.get())) level.setBlock(p, MareEletrica.BRIDGE_TILE.get().defaultBlockState(), 3);
                if (level.getBlockEntity(p) instanceof BeamLightEntity lease) {
                    lease.refresh(level.getGameTime()); tiles++; m.projectedLength = distance;
                }
            }
        }
        if (tiles == 0) { m.status = 21; return false; }
        int fee = BridgePower.tickCost(m.projectedLength);
        m.clean.pay(fee); m.lastRate = fee * 2; m.status = 1; return true;
    }
}
