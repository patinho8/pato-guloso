package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Each occupied cell renders its own bounded model. Only the controller owns items and energy. */
public final class PotabilizerStructure {
    private static final VoxelShape[][] SHAPES = new VoxelShape[12][4];
    static {
        for (int part=0; part<12; part++) for (int turn=0; turn<4; turn++) {
            VoxelShape shape = Shapes.empty();
            for (double[] b : PotabilizerLayout.boxes(part, turn)) shape = Shapes.or(shape, Block.box(b[0],b[1],b[2],b[3],b[4],b[5]));
            SHAPES[part][turn] = shape.optimize();
        }
    }
    private PotabilizerStructure() {}
    public static int turn(Direction facing) {
        return switch(facing) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
    }
    public static VoxelShape shape(int part, Direction facing) { return SHAPES[part][turn(facing)]; }
    public static BlockPos position(BlockPos root, Direction facing, int part) {
        int[] o = PotabilizerLayout.offset(part, turn(facing)); return root.offset(o[0],o[1],o[2]);
    }
    public static BlockPos root(BlockPos pos, BlockState state) {
        int[] o = PotabilizerLayout.offset(state.getValue(PotabilizerPartBlock.PART), turn(state.getValue(MachineBlock.FACING)));
        return pos.offset(-o[0],-o[1],-o[2]);
    }
    public static boolean isRoot(BlockState state) { return state.getBlock() instanceof MachineBlock block && block.kind == Kind.POTABILIZER; }
    private static boolean matches(BlockState state, Direction facing, int part) {
        return state.getBlock() instanceof PotabilizerPartBlock && state.getValue(PotabilizerPartBlock.PART) == part
            && state.getValue(MachineBlock.FACING) == facing;
    }
    public static MachineEntity machineAt(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return null;
        if (level.getBlockEntity(pos) instanceof MachineEntity machine) return machine;
        if (level.getBlockEntity(pos) instanceof PotabilizerPartEntity part) return part.controller();
        if (level.getBlockEntity(pos) instanceof LargeMachinePartEntity part) return part.controller();
        return null;
    }
    public static boolean hasSpace(Level level, BlockPos root, Direction facing, boolean includeRoot) {
        for (int part=includeRoot ? 0 : 1; part<12; part++) {
            BlockPos p = position(root,facing,part);
            if (level.isOutsideBuildHeight(p) || !level.getWorldBorder().isWithinBounds(p) || !level.hasChunkAt(p)
                || !level.getBlockState(p).isAir() || !level.getFluidState(p).isEmpty() || level.getBlockEntity(p) != null) return false;
            if (!level.isUnobstructed(null,shape(part,facing).move(p.getX(),p.getY(),p.getZ()))) return false;
        }
        return true;
    }
    public static void form(Level level, BlockPos root, BlockState controller) {
        if (level.isClientSide) return;
        Direction facing = controller.getValue(MachineBlock.FACING);
        if (!hasSpace(level,root,facing,false)) { level.destroyBlock(root,true); return; }
        for (int part=1; part<12; part++) {
            BlockPos p = position(root,facing,part);
            BlockState state = MareEletrica.POTABILIZER_PART.get().defaultBlockState()
                .setValue(PotabilizerPartBlock.PART,part).setValue(MachineBlock.FACING,facing);
            if (!level.getBlockState(p).isAir() || !level.setBlock(p,state,3)) {
                level.destroyBlock(root,true); return; // Removing the root rolls back only its own placed parts.
            }
        }
    }
    /** 1 complete, 0 broken, -1 unknown because a required chunk is unavailable. */
    public static int integrity(MachineEntity m) {
        Level level = m.getLevel(); if (level == null) return -1;
        Direction facing = m.getBlockState().getValue(MachineBlock.FACING);
        for (int part=1; part<12; part++) if (!level.hasChunkAt(position(m.getBlockPos(),facing,part))) return -1;
        for (int part=1; part<12; part++) if (!matches(level.getBlockState(position(m.getBlockPos(),facing,part)),facing,part)) return 0;
        return 1;
    }
    public static void removeParts(Level level, BlockPos root, Direction facing) {
        for (int part=1; part<12; part++) {
            BlockPos p = position(root,facing,part);
            if (level.hasChunkAt(p) && matches(level.getBlockState(p),facing,part)) level.removeBlock(p,false);
        }
        // Parts in unloaded chunks clean themselves after the root chunk is available again.
    }
    public static void partRemoved(Level level, BlockPos pos, BlockState old) {
        BlockPos root = root(pos,old);
        if (level.hasChunkAt(root) && isRoot(level.getBlockState(root))
            && level.getBlockState(root).getValue(MachineBlock.FACING) == old.getValue(MachineBlock.FACING)) level.destroyBlock(root,true);
        // A missing part is detected by the root after chunks load. Parts never drop a machine item.
    }
    public static void syncActive(MachineEntity m, boolean active) {
        Level level = m.getLevel(); if (level == null) return;
        Direction facing = m.getBlockState().getValue(MachineBlock.FACING);
        for (int part=1; part<12; part++) {
            BlockPos p = position(m.getBlockPos(),facing,part);
            if (!level.hasChunkAt(p)) continue;
            BlockState state = level.getBlockState(p);
            if (matches(state,facing,part) && state.getValue(MachineBlock.ACTIVE) != active)
                level.setBlock(p,state.setValue(MachineBlock.ACTIVE,active),2);
        }
    }
    public static void interact(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        MachineEntity m = machineAt(level,pos);
        if (m == null || m.kind == Kind.GIANT_WHEEL) return;
        if (player.isShiftKeyDown()) player.displayClientMessage(Component.translatable(m.kind.large()?"gui.mareeletrica.large_rotate":"gui.mareeletrica.potabilizer_rotate"),true);
        else player.openMenu(m);
    }
}
