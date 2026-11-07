package br.mareeletrica;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Server-only work, in bounded batches. Never loads chunks to find work. */
public final class AdvancedMachines {
    private AdvancedMachines() {}
    public static boolean isOre(BlockState state) { return ORES.contains(state.getBlock()); }
    private static final Set<Block> ORES = Set.of(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS);
    // Weak keys; values must not retain the level or its block entities.
    private static final Map<Level, Map<BlockPos, Integer>> PADS = new WeakHashMap<>();
    private static final String TELEPORT_COOLDOWN = "mareeletricaTeleportCooldown";

    public static boolean tick(MachineEntity m) {
        if (!(m.getLevel() instanceof ServerLevel level)) return false;
        return switch (m.kind) {
            case HARVESTER -> harvest(m, level);
            case PLANTER -> plant(m, level);
            case FISHER -> fish(m, level);
            case ORE_SCANNER -> scan(m, level);
            case TELEPORT_PAD -> transport(m, level);
            case CHARGER -> charge(m);
            case XP_COLLECTOR -> collectXp(m, level);
            case WEATHER_CONTROLLER -> weatherStatus(m, level);
            case TURRET -> turret(m, level);
            default -> false;
        };
    }

    public static boolean action(MachineEntity m, Player player) {
        if (!(m.getLevel() instanceof ServerLevel level) || !m.kind.actionButton()) return false;
        // Withdrawing stored XP and configuring channels are allowed while paused.
        if (m.kind == Kind.XP_COLLECTOR) {
            int amount = Math.min(100, m.storedXp);
            if (amount > 0) { m.storedXp -= amount; player.giveExperiencePoints(amount); m.setChanged(); }
            return true;
        }
        if (m.kind == Kind.TELEPORT_PAD) {
            m.channel = (m.channel + 1) % 16;
            PADS.computeIfAbsent(level, k -> new HashMap<>()).put(m.getBlockPos(), m.channel);
            m.setChanged(); return true;
        }
        if (!m.enabled || level.hasNeighborSignal(m.getBlockPos())) return false;
        if (m.kind == Kind.ORE_SCANNER) {
            if (m.scanning) return false;
            if (!m.clean.pay(4_000)) { m.status = 2; return false; }
            m.scanning = true; m.scanIndex = 0; m.orePosition = null; m.oreStateId = 0;
            m.status = 13; m.setChanged(); return true;
        }
        if (level.dimension() != Level.OVERWORLD) { m.status = 16; return false; }
        if (level.getGameTime() < m.weatherCooldown) { m.status = 19; return false; }
        if (!m.clean.pay(20_000)) { m.status = 2; return false; }
        boolean rain = !level.isRaining();
        level.setWeatherParameters(rain ? 0 : 6_000, rain ? 6_000 : 0, rain, false);
        m.weatherCooldown = level.getGameTime() + 1_200;
        m.status = 19; m.setChanged(); return true;
    }

    public static CropBlock cropFor(ItemStack stack) {
        if (stack.is(Items.WHEAT_SEEDS)) return (CropBlock) Blocks.WHEAT;
        if (stack.is(Items.CARROT)) return (CropBlock) Blocks.CARROTS;
        if (stack.is(Items.POTATO)) return (CropBlock) Blocks.POTATOES;
        if (stack.is(Items.BEETROOT_SEEDS)) return (CropBlock) Blocks.BEETROOTS;
        return null;
    }

    private static BlockPos nextCrop(MachineEntity m) {
        int index = Math.floorMod(m.workCursor, 243);
        m.workCursor = (index + 1) % 243;
        return m.getBlockPos().offset(index % 9 - 4, index / 81 - 1, (index / 9) % 9 - 4);
    }

    private static boolean fits(MachineEntity m, List<ItemStack> stacks) {
        // Simulate the whole batch in a copy, including competition between drops for slots.
        ItemStackHandler copy = new ItemStackHandler(9);
        for (int i = 0; i < 9; i++) copy.setStackInSlot(i, m.inventory.getStackInSlot(i).copy());
        for (ItemStack stack : stacks)
            if (!ItemHandlerHelper.insertItemStacked(copy, stack.copy(), false).isEmpty()) return false;
        return true;
    }

    private static boolean harvest(MachineEntity m, ServerLevel level) {
        if (m.clean.stored() < 200) { m.status = 2; return false; }
        for (int i = 0; i < 16; i++) {
            BlockPos pos = nextCrop(m);
            if (!level.hasChunkAt(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) continue;
            List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, ItemStack.EMPTY);
            if (!fits(m, drops)) { m.status = 7; return false; }
            if (!level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)) continue;
            m.clean.pay(200);
            for (ItemStack drop : drops) ItemHandlerHelper.insertItemStacked(m.inventory, drop, false);
            m.status = 1; m.setChanged(); return true;
        }
        return false;
    }

    private static boolean plant(MachineEntity m, ServerLevel level) {
        int slot = -1;
        CropBlock crop = null;
        for (int i = 0; i < 9; i++) {
            crop = cropFor(m.inventory.getStackInSlot(i));
            if (crop != null) { slot = i; break; }
        }
        if (slot < 0) { m.status = 6; return false; }
        if (m.clean.stored() < 50) { m.status = 2; return false; }
        for (int i = 0; i < 16; i++) {
            BlockPos pos = nextCrop(m);
            if (!level.hasChunkAt(pos) || !level.isEmptyBlock(pos)
                || !(level.getBlockState(pos.below()).getBlock() instanceof FarmBlock)) continue;
            BlockState seedling = crop.defaultBlockState();
            if (!seedling.canSurvive(level, pos) || !level.setBlock(pos, seedling, 3)) continue;
            m.inventory.extractItem(slot, 1, false); m.clean.pay(50);
            m.status = 1; m.setChanged(); return true;
        }
        return false;
    }

    private static boolean fish(MachineEntity m, ServerLevel level) {
        BlockPos center = m.getBlockPos().relative(m.getBlockState().getValue(MachineBlock.FACING), 2).below();
        int water = 0;
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            BlockPos pos = center.offset(x, 0, z);
            if (level.hasChunkAt(pos) && level.getFluidState(pos).is(FluidTags.WATER)) water++;
        }
        if (water < 6) { m.status = 3; return false; }
        ItemStack result = new ItemStack(m.workCursor % 2 == 0 ? Items.COD : Items.SALMON);
        if (!fits(m, List.of(result))) { m.status = 7; return false; }
        if (!m.clean.pay(10)) { m.status = 2; return false; }
        m.progress += 10;
        if (m.progress >= 400) {
            ItemHandlerHelper.insertItemStacked(m.inventory, result, false);
            m.progress = 0; m.workCursor = (m.workCursor + 1) % 2;
        }
        m.status = 1; return true;
    }

    private static boolean scan(MachineEntity m, ServerLevel level) {
        if (!m.scanning) {
            m.status = m.orePosition != null ? 15 : m.scanIndex >= 9_248 ? 14 : 0;
            return false;
        }
        for (int i = 0; i < 128 && m.scanIndex < 9_248; i++) {
            int index = m.scanIndex++;
            BlockPos pos = m.getBlockPos().offset(index % 17 - 8, -1 - index / 289, (index / 17) % 17 - 8);
            if (pos.getY() < level.getMinBuildHeight() || !level.hasChunkAt(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (!ORES.contains(state.getBlock())) continue;
            m.orePosition = pos; m.oreStateId = Block.getId(state); m.scanning = false;
            m.status = 15; m.setChanged(); return true;
        }
        m.scanning = m.scanIndex < 9_248;
        m.status = m.scanning ? 13 : 14; m.setChanged(); return m.scanning;
    }

    private static boolean charge(MachineEntity m) {
        ItemStack stack = m.inventory.getStackInSlot(0);
        var battery = stack.getCapability(Capabilities.EnergyStorage.ITEM, null);
        if (battery == null || !battery.canReceive()) { m.status = 6; return false; }
        if (m.clean.stored() == 0) { m.status = 2; return false; }
        int accepted = battery.receiveEnergy(Math.min(400, m.clean.stored()), false);
        if (accepted <= 0) { m.status = 10; return false; }
        m.clean.take(accepted); m.inventory.setStackInSlot(0, stack);
        m.lastRate = accepted * 2; m.status = 1; m.setChanged(); return true;
    }

    private static boolean collectXp(MachineEntity m, ServerLevel level) {
        if (m.storedXp >= 10_000) { m.status = 10; return false; }
        if (m.clean.stored() < 2) { m.status = 2; return false; }
        boolean worked = false;
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, new AABB(m.getBlockPos()).inflate(4), e -> e.isAlive())) {
            // Vanilla can merge equal-value orbs. Preserve the entire group, never just its unit value.
            CompoundTag saved = new CompoundTag(); orb.saveWithoutId(saved);
            long total = (long) orb.getValue() * Math.max(1, saved.getInt("Count"));
            if (total <= 0 || total > 10_000 - m.storedXp || total * 2 > m.clean.stored()) continue;
            m.clean.pay((int) total * 2); m.storedXp += (int) total; orb.discard(); worked = true;
        }
        if (worked) { m.status = 1; m.setChanged(); }
        return worked;
    }

    private static boolean weatherStatus(MachineEntity m, ServerLevel level) {
        m.status = level.dimension() != Level.OVERWORLD ? 16 : level.getGameTime() < m.weatherCooldown ? 19 : m.clean.stored() < 20_000 ? 2 : 17;
        return m.status == 17;
    }

    private static boolean turret(MachineEntity m, ServerLevel level) {
        m.workCursor = Math.max(0, m.workCursor - 10);
        if (m.workCursor > 0) { m.status = 20; return true; }
        if (m.clean.stored() < 400) { m.status = 2; return false; }
        Vec3 start = Vec3.atCenterOf(m.getBlockPos()).add(0, 0.6, 0);
        int mode = BaseControl.turretMode(level, m.getBlockPos());
        if (mode == 0) { m.status = 82; return false; }
        net.minecraft.world.entity.LivingEntity target = null; double closest = 144;
        for (net.minecraft.world.entity.LivingEntity mob : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new AABB(m.getBlockPos()).inflate(12), e -> e.isAlive() && (e instanceof Enemy || mode == 2 && e instanceof net.minecraft.world.entity.player.Player p && BaseControl.intruder(level, m.getBlockPos(), p)))) {
            Vec3 end = mob.getBoundingBox().getCenter(); double distance = start.distanceToSqr(end);
            if (distance > closest) continue;
            boolean loaded = true;
            for (int i = 0; i <= 24; i++) {
                if (!level.hasChunkAt(BlockPos.containing(start.add(end.subtract(start).scale(i / 24.0))))) { loaded = false; break; }
            }
            if (!loaded) continue;
            // Solid walls interrupt the beam. The scan considers hostiles only.
            if (level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob)).getType() != HitResult.Type.MISS) continue;
            closest = distance; target = mob;
        }
        if (target == null) return false;
        Vec3 end = target.getBoundingBox().getCenter();
        if (!target.hurt(level.damageSources().magic(), 4F)) return false;
        m.clean.pay(400); m.workCursor = 20;
        Vec3 delta = end.subtract(start);
        if (delta.x * delta.x + delta.z * delta.z > 0.01) {
            Direction facing = Direction.getNearest((float) delta.x, 0, (float) delta.z);
            if (m.getBlockState().getValue(MachineBlock.FACING) != facing)
                level.setBlock(m.getBlockPos(), m.getBlockState().setValue(MachineBlock.FACING, facing), 3);
        }
        for (int i = 0; i <= 16; i++) {
            Vec3 point = start.add(delta.scale(i / 16.0));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.015, 0.015, 0.015, 0);
        }
        m.status = 1; m.setChanged(); return true;
    }

    private static MachineEntity destination(MachineEntity source, ServerLevel level) {
        Map<BlockPos, Integer> pads = PADS.computeIfAbsent(level, k -> new HashMap<>());
        pads.put(source.getBlockPos(), source.channel);
        MachineEntity result = null; double best = 128 * 128 + 1;
        var iterator = pads.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next(); BlockPos pos = entry.getKey();
            if (!level.hasChunkAt(pos)) { iterator.remove(); continue; }
            if (!(level.getBlockEntity(pos) instanceof MachineEntity pad) || pad.kind != Kind.TELEPORT_PAD || pad.isRemoved()) {
                iterator.remove(); continue;
            }
            if (pad == source || pad.channel != source.channel || !pad.enabled || pad.clean.stored() < 500 || level.hasNeighborSignal(pos)) continue;
            double distance = pos.distSqr(source.getBlockPos());
            if (distance > 128 * 128 || distance >= best || !level.getWorldBorder().isWithinBounds(pos)) continue;
            if (pos.getY() + 2 >= level.getMaxBuildHeight()) continue;
            if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                || !level.getBlockState(pos.above(2)).getCollisionShape(level, pos.above(2)).isEmpty()
                || !level.getFluidState(pos.above()).isEmpty() || !level.getFluidState(pos.above(2)).isEmpty()) continue;
            best = distance; result = pad;
        }
        return result;
    }

    private static boolean transport(MachineEntity m, ServerLevel level) {
        MachineEntity target = destination(m, level);
        if (m.clean.stored() < 1_000) { m.status = 2; return false; }
        if (target == null) { m.status = 18; return false; }
        m.status = 17;
        BlockPos pos = m.getBlockPos();
        AABB abovePad = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1.5, pos.getZ() + 1);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, abovePad,
            p -> p.isAlive() && !p.isSpectator() && !p.isPassenger() && p.isShiftKeyDown())) {
            if (player.getPersistentData().getLong(TELEPORT_COOLDOWN) > level.getGameTime()) continue;
            if (m.clean.stored() < 1_000 || target.clean.stored() < 500) break;
            BlockPos dest = target.getBlockPos();
            double x = dest.getX() + 0.5, y = dest.getY() + 1, z = dest.getZ() + 0.5;
            if (!level.noCollision(player, player.getBoundingBox().move(x - player.getX(), y - player.getY(), z - player.getZ()))) continue;
            m.clean.pay(1_000); target.clean.pay(500); target.setChanged();
            player.getPersistentData().putLong(TELEPORT_COOLDOWN, level.getGameTime() + 60);
            player.teleportTo(x, y, z); player.fallDistance = 0;
            m.status = 1; m.setChanged(); return true;
        }
        return true;
    }
}
