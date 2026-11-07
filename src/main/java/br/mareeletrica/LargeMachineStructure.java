package br.mareeletrica;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Every cell uses a local 0..16 mesh and the same clipped collision geometry. */
public final class LargeMachineStructure {
    private static final Map<Kind,VoxelShape[][]> SHAPES=new EnumMap<>(Kind.class);
    private static final VoxelShape[][] LEGACY_WHEEL=make(HydroGeometry.GIANT_WHEEL,3,27);
    private static final VoxelShape[][] LEGACY_DOCK=make(LargeGeometry.LEGACY_DOCK,1,1);
    private static final VoxelShape[][] LEGACY=make(LargeGeometry.LEGACY_QUARRY,3,36);
    static { for(Kind kind:Kind.values()) if(kind.large())SHAPES.put(kind,make(geometry(kind),kind.structureWidth(),kind.structureParts())); }
    private static double[][] geometry(Kind kind) {
        return switch(kind) { case CORE_QUANTUM->QuantumGeometry.BOXES;case PURIFIER->PurifierGeometry.BOXES;case GIANT_WHEEL->QuantumGeometry.WHEEL;case CURRENT_TURBINE->HydroGeometry.CURRENT_TURBINE;case FALL_TURBINE->HydroGeometry.FALL_TURBINE;case PRESSURE_TURBINE->HydroGeometry.PRESSURE_TURBINE;case GAS_REFILL->scaled(GasRefillerGeometry.BOXES,.6);case EXTERMINATOR->scaled(GuardianGeometry.BOXES,.6);case QUARRY->LargeGeometry.QUARRY;case GEOTHERMAL->LargeGeometry.GEOTHERMAL;case ABYSSAL->LargeGeometry.ABYSSAL;case MORTAR->LargeGeometry.MORTAR;case ROVER_BASE->LargeGeometry.ROVER_BASE;default->LargeGeometry.GREENHOUSE; };
    }
    /** The exterminator base and the gas refiller were designed 5x5x5; since 0.42.0 they are built 3x3x3 (same design, 60%). */
    private static double[][] scaled(double[][] g,double f){double[][] o=new double[g.length][];for(int i=0;i<g.length;i++){o[i]=new double[6];for(int k=0;k<6;k++)o[i][k]=g[i][k]*f;}return o;}
    private static VoxelShape[][] make(double[][] geometry,int width,int count) {
        VoxelShape[][] out=new VoxelShape[count][4];
        for(int part=0;part<count;part++)for(int turn=0;turn<4;turn++) {
            VoxelShape shape=Shapes.empty();
            for(double[] b:LargeMachineLayout.boxes(geometry,part,turn,width))shape=Shapes.or(shape,Block.box(b[0],b[1],b[2],b[3],b[4],b[5]));
            out[part][turn]=shape.optimize();
        }
        return out;
    }
    private LargeMachineStructure() {}
    public static boolean wide(BlockState s){return s.hasProperty(MachineBlock.WIDE)&&s.getValue(MachineBlock.WIDE);}
    public static int width(Kind k,boolean expanded){return width(k,expanded,false);}
    public static int width(Kind k,BlockState s){return width(k,s.getValue(MachineBlock.EXPANDED),wide(s));}
    public static int width(Kind k,boolean e,boolean wide){return k==Kind.GIANT_WHEEL?(wide?7:e?3:5):(k==Kind.CORE_QUANTUM||k==Kind.PURIFIER)&&!e?1:k==Kind.ROVER_BASE&&!e?1:k==Kind.QUARRY&&!e?3:k.structureWidth();}
    public static int parts(Kind k,boolean e){return parts(k,e,false);}
    public static int parts(Kind k,BlockState s){return parts(k,s.getValue(MachineBlock.EXPANDED),wide(s));}
    public static int parts(Kind k,boolean e,boolean wide){return k==Kind.GIANT_WHEEL?(wide?343:e?27:125):(k==Kind.CORE_QUANTUM||k==Kind.PURIFIER)&&!e?1:k==Kind.ROVER_BASE&&!e?1:k==Kind.QUARRY&&!e?36:k.structureParts();}
    public static VoxelShape shape(Kind k,int part,Direction facing,boolean e){return shape(k,part,facing,e,false);}
    public static VoxelShape shape(Kind k,int part,Direction facing,boolean e,boolean wide){if((k==Kind.PURIFIER||k==Kind.CORE_QUANTUM)&&!e)return Block.box(1,0,1,15,15,15);var shapes=k==Kind.GIANT_WHEEL&&!wide?LEGACY_WHEEL:k==Kind.ROVER_BASE&&!e?LEGACY_DOCK:k==Kind.QUARRY&&!e?LEGACY:SHAPES.get(k);return part<shapes.length?shapes[part][PotabilizerStructure.turn(facing)]:Shapes.empty();}
    public static BlockPos position(BlockPos root,Direction f,int part,Kind k,boolean e){return position(root,f,part,k,e,false);}
    public static BlockPos position(BlockPos root,Direction f,int part,Kind k,boolean e,boolean wide){int[] o=LargeMachineLayout.offset(part,PotabilizerStructure.turn(f),width(k,e,wide));return root.offset(o[0],o[1],o[2]);}
    public static BlockPos position(MachineEntity m,int part){var s=m.getBlockState();return position(m.getBlockPos(),s.getValue(MachineBlock.FACING),part,m.kind,s.getValue(MachineBlock.EXPANDED),wide(s));}
    public static BlockPos root(BlockPos pos,BlockState state){Kind kind=((LargeMachinePartBlock)state.getBlock()).kind;int[] o=LargeMachineLayout.offset(LargeMachinePartBlock.part(state),PotabilizerStructure.turn(state.getValue(MachineBlock.FACING)),width(kind,state));return pos.offset(-o[0],-o[1],-o[2]);}
    public static boolean isRoot(BlockState state,Kind kind) { return state.getBlock() instanceof MachineBlock b && b.kind==kind; }
    private static boolean space(Level level,BlockPos p,Kind kind){var s=level.getBlockState(p);return s.isAir()&&s.getFluidState().isEmpty()||kind.waterStructure()&&(s.is(net.minecraft.world.level.block.Blocks.WATER)||s.is(MareEletrica.PRESSURIZED_WATER_BLOCK.get()));}
    private static boolean matches(BlockState state,Direction facing,Kind kind,int part,boolean expanded,boolean wide) {
        return state.getBlock() instanceof LargeMachinePartBlock b && b.kind==kind
            && wide(state)==wide && state.getValue(MachineBlock.EXPANDED)==expanded && LargeMachinePartBlock.part(state)==part && state.getValue(MachineBlock.FACING)==facing;
    }
    public static boolean hasSpace(Level level,BlockPos root,Direction facing,Kind kind,boolean includeRoot) {
        return hasSpace(level,root,facing,kind,includeRoot,kind==Kind.QUARRY||kind==Kind.ROVER_BASE||kind==Kind.GIANT_WHEEL||kind==Kind.PURIFIER||kind==Kind.CORE_QUANTUM,kind==Kind.GIANT_WHEEL);
    }
    private static boolean hasSpace(Level level,BlockPos root,Direction facing,Kind kind,boolean includeRoot,boolean expanded,boolean wide) {
        for (int part=includeRoot?0:1;part<parts(kind,expanded,wide);part++) {
            BlockPos p=position(root,facing,part,kind,expanded,wide);
            if (!level.hasChunkAt(p) || level.isOutsideBuildHeight(p) || !level.getWorldBorder().isWithinBounds(p)
                || !space(level,p,kind) || level.getBlockEntity(p)!=null
                || !level.isUnobstructed(null,shape(kind,part,facing,expanded,wide).move(p.getX(),p.getY(),p.getZ()))) return false;
        }
        return true;
    }
    public static void form(Level level,BlockPos root,BlockState controller) {
        if (level.isClientSide) return;
        Kind kind=((MachineBlock)controller.getBlock()).kind;
        Direction facing=controller.getValue(MachineBlock.FACING);
        boolean expanded=controller.getValue(MachineBlock.EXPANDED),wide=wide(controller);
        if (!hasSpace(level,root,facing,kind,false,expanded,wide)) { level.destroyBlock(root,true);return; }
        for (int part=1;part<parts(kind,expanded,wide);part++) {
            BlockPos p=position(root,facing,part,kind,expanded,wide);
            var block=MareEletrica.LARGE_PARTS.get(kind).get();
            BlockState state=block.defaultBlockState().setValue(block.partProperty(),part).setValue(MachineBlock.FACING,facing).setValue(MachineBlock.EXPANDED,expanded);
            if(kind==Kind.GIANT_WHEEL)state=state.setValue(MachineBlock.WIDE,wide);
            if(kind.waterStructure())state=state.setValue(MachineBlock.WATERLOGGED,level.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER));
            if (!space(level,p,kind) || !level.setBlock(p,state,3)) { level.destroyBlock(root,true);return; }
        }
    }
    public static int integrity(MachineEntity m) {
        Level level=m.getLevel();if (level==null) return -1;
        Direction facing=m.getBlockState().getValue(MachineBlock.FACING);
        boolean expanded=m.getBlockState().getValue(MachineBlock.EXPANDED),wide=wide(m.getBlockState());
        for (int part=1;part<parts(m.kind,expanded,wide);part++) if (!level.hasChunkAt(position(m.getBlockPos(),facing,part,m.kind,expanded,wide))) return -1;
        for (int part=1;part<parts(m.kind,expanded,wide);part++)
            if (!matches(level.getBlockState(position(m.getBlockPos(),facing,part,m.kind,expanded,wide)),facing,m.kind,part,expanded,wide)) return 0;
        return 1;
    }
    public static void removeParts(Level level,BlockPos root,Direction facing,Kind kind,boolean expanded) {removeParts(level,root,facing,kind,expanded,false);}
    public static void removeParts(Level level,BlockPos root,Direction facing,Kind kind,boolean expanded,boolean wide) {
        for (int part=1;part<parts(kind,expanded,wide);part++) {
            BlockPos p=position(root,facing,part,kind,expanded,wide);
            if (level.hasChunkAt(p) && matches(level.getBlockState(p),facing,kind,part,expanded,wide)) {if(kind.waterStructure()&&level.getBlockState(p).getValue(MachineBlock.WATERLOGGED))level.setBlock(p,net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(),3);else level.removeBlock(p,false);}
        }
    }
    public static void partRemoved(Level level,BlockPos pos,BlockState old) {
        BlockPos root=root(pos,old);Kind kind=((LargeMachinePartBlock)old.getBlock()).kind;
        if (level.hasChunkAt(root) && isRoot(level.getBlockState(root),kind)
            && level.getBlockState(root).getValue(MachineBlock.FACING)==old.getValue(MachineBlock.FACING)
            && wide(level.getBlockState(root))==wide(old)&&level.getBlockState(root).getValue(MachineBlock.EXPANDED)==old.getValue(MachineBlock.EXPANDED)) level.destroyBlock(root,true);
    }
    public static void syncActive(MachineEntity m,boolean active) {
        Level level=m.getLevel();if(level==null)return;
        Direction facing=m.getBlockState().getValue(MachineBlock.FACING);
        boolean expanded=m.getBlockState().getValue(MachineBlock.EXPANDED),wide=wide(m.getBlockState());
        for(int part=1;part<parts(m.kind,expanded,wide);part++) {
            BlockPos p=position(m.getBlockPos(),facing,part,m.kind,expanded,wide);if(!level.hasChunkAt(p))continue;
            BlockState state=level.getBlockState(p);
            if(matches(state,facing,m.kind,part,expanded,wide) && state.getValue(MachineBlock.ACTIVE)!=active)
                level.setBlock(p,state.setValue(MachineBlock.ACTIVE,active),2);
        }
    }
}
