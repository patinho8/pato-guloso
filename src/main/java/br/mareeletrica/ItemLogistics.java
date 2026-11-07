package br.mareeletrica;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/** Back = source, front = destination. Item ducts are separate from electrical cables. */
public final class ItemLogistics {
    private ItemLogistics() {}
    private record Visit(BlockPos pos, Direction side) {}
    private static List<IItemHandler> destinations(MachineEntity m, ServerLevel level, Direction facing) {
        BlockPos origin = m.getBlockPos(), source = origin.relative(facing.getOpposite());
        ArrayDeque<Visit> queue = new ArrayDeque<>();
        Set<BlockPos> ducts = new HashSet<>(); Set<Visit> visited = new HashSet<>();
        List<IItemHandler> result = new ArrayList<>();
        queue.add(new Visit(origin.relative(facing), facing.getOpposite()));
        int inspected = 0;
        while (!queue.isEmpty() && inspected++ < 768) {
            Visit visit = queue.removeFirst(); BlockPos p = visit.pos();
            if (p.equals(origin) || p.equals(source) || !visited.add(visit) || !level.hasChunkAt(p)) continue;
            if (Math.abs(p.getX()-origin.getX()) > 32 || Math.abs(p.getY()-origin.getY()) > 32 || Math.abs(p.getZ()-origin.getZ()) > 32) continue;
            if (level.getBlockState(p).getBlock() instanceof ItemDuctBlock) {
                if (ducts.size() >= 128 || !ducts.add(p)) continue;
                for (Direction direction : Direction.values()) queue.addLast(new Visit(p.relative(direction), direction.getOpposite()));
            } else {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, p, visit.side());
                if (handler != null && handler != m.inventory && handler != m.itemPort) result.add(handler);
            }
        }
        return result;
    }
    public static boolean tick(MachineEntity m) {
        if (!(m.getLevel() instanceof ServerLevel level)) return false;
        if (m.clean.stored() < 20) { m.status = 2; return false; }
        Direction facing = m.getBlockState().getValue(MachineBlock.FACING);
        List<IItemHandler> targets = destinations(m, level, facing);
        if (targets.isEmpty()) { m.status = 23; return false; }
        int quota = Math.min(8, m.clean.stored() / 20);
        if (SafeItemTransfer.empty(m.inventory)) {
            BlockPos sourcePos = m.getBlockPos().relative(facing.getOpposite());
            IItemHandler source = level.hasChunkAt(sourcePos) ? level.getCapability(Capabilities.ItemHandler.BLOCK, sourcePos, facing) : null;
            if (source == null) { m.status = 22; return false; }
            boolean staged = SafeItemTransfer.stage(source, m.inventory, quota,
                stack -> targets.stream().anyMatch(target -> SafeItemTransfer.insert(target, stack, true).getCount() < stack.getCount()));
            if (!staged) { m.status = 24; return false; }
        }
        // BFS order selects the first reachable destination that accepts the current buffered item.
        for (IItemHandler target : targets) {
            int moved = SafeItemTransfer.deliver(m.inventory, target, quota);
            if (moved <= 0) continue;
            m.clean.pay(moved * 20); m.lastRate = moved * 2; m.status = 1; m.setChanged(); return true;
        }
        m.status = 7; return false;
    }
}
