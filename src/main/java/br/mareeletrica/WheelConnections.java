package br.mareeletrica;
import net.minecraft.core.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.Level;
public final class WheelConnections {
 private WheelConnections(){}
 public static int width(MachineEntity m){return LargeMachineStructure.width(m.kind,m.getBlockState());}
 public static BlockPos port(MachineEntity m,boolean front){int w=width(m);return HydroGenerators.local(m,w/2,w/2,front?-1:w);}
 public static MachineEntity wheel(Level level,BlockPos pos){var m=PotabilizerStructure.machineAt(level,pos);return m!=null&&m.kind==Kind.GIANT_WHEEL?m:null;}
 public static Direction face(UseOnContext c,MachineEntity m){var axis=m.getBlockState().getValue(MachineBlock.FACING);if(c.getClickedFace().getAxis()==axis.getAxis())return c.getClickedFace();var center=HydroGenerators.local(m,width(m)/2,width(m)/2,width(m)/2).getCenter();return c.getClickLocation().subtract(center).dot(net.minecraft.world.phys.Vec3.atLowerCornerOf(axis.getNormal()))>=0?axis:axis.getOpposite();}
 public static BlockPlaceContext placement(UseOnContext c,MachineEntity m,boolean wheel){var face=face(c,m);var forward=m.getBlockState().getValue(MachineBlock.FACING);BlockPos target=wheel?m.getBlockPos().relative(face,width(m)):port(m,face==forward);return new AlignedContext(c,target,forward.getOpposite());}
 private static final class AlignedContext extends BlockPlaceContext {private BlockPos target;private Direction facing;AlignedContext(UseOnContext c,BlockPos target,Direction facing){super(c);this.target=target;this.facing=facing;}@Override public BlockPos getClickedPos(){return target==null?super.getClickedPos():target;}@Override public Direction getHorizontalDirection(){return facing==null?super.getHorizontalDirection():facing;}}
 public static boolean connected(MachineEntity m,boolean front){var level=m.getLevel();var port=port(m,front);if(!level.hasChunkAt(port))return false;if(level.getBlockState(port).getBlock() instanceof CableBlock)return true;var next=wheel(level,port);return next!=null&&next!=m&&next.getBlockState().getValue(MachineBlock.FACING).getAxis()==m.getBlockState().getValue(MachineBlock.FACING).getAxis();}
}
