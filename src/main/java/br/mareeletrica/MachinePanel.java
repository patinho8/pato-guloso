package br.mareeletrica;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
/** A visible front service panel, with identical selection geometry in every multiblock cell. */
public final class MachinePanel {
 public static boolean supported(Kind k){return k.core()||k.consumer||k==Kind.PURIFIER;}
 public static int width(Kind k,BlockState s){return k.large()?LargeMachineStructure.width(k,s):k==Kind.POTABILIZER?2:1;}
 public static Vec3 center(Kind k,BlockState s){var f=s.getValue(MachineBlock.FACING);return new Vec3(.5,.17,.5).add(Vec3.atLowerCornerOf(f.getClockWise().getNormal()).scale(width(k,s)/2.0-.5)).add(Vec3.atLowerCornerOf(f.getNormal()).scale(.501));}
 public static VoxelShape add(BlockState s,VoxelShape base){Kind kind;BlockPos root=BlockPos.ZERO;if(s.getBlock() instanceof MachineBlock m)kind=m.kind;else if(s.getBlock() instanceof LargeMachinePartBlock m){kind=m.kind;root=LargeMachineStructure.root(BlockPos.ZERO,s);}else if(s.getBlock() instanceof PotabilizerPartBlock){kind=Kind.POTABILIZER;root=PotabilizerStructure.root(BlockPos.ZERO,s);}else return base;if(!supported(kind))return base;
  var f=s.getValue(MachineBlock.FACING);var c=center(kind,s).add(Vec3.atLowerCornerOf(root)).subtract(Vec3.atLowerCornerOf(f.getNormal()).scale(.041));double rx=f.getAxis()==Direction.Axis.X?.03:.21,rz=f.getAxis()==Direction.Axis.Z?.03:.21;double x0=Math.max(0,c.x-rx),x1=Math.min(1,c.x+rx),y0=Math.max(0,c.y-.08),y1=Math.min(1,c.y+.08),z0=Math.max(0,c.z-rz),z1=Math.min(1,c.z+rz);return x0<x1&&y0<y1&&z0<z1?Shapes.or(base,Shapes.box(x0,y0,z0,x1,y1,z1)):base;
 }
}
