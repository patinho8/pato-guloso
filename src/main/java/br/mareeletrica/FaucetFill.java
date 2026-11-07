package br.mareeletrica;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
/** One source volume per delivery. Closed pools fill from their floor upward. */
public final class FaucetFill {
 private FaucetFill(){}
 private static boolean passable(ServerLevel l,BlockPos p){if(!l.hasChunkAt(p)||l.isOutsideBuildHeight(p))return false;BlockState s=l.getBlockState(p);return s.isAir()||s.is(MareEletrica.FAUCET_STREAM.get())||s.is(MareEletrica.PRESSURIZED_WATER_BLOCK.get())||s.is(net.minecraft.world.level.block.Blocks.WATER);}
 public static BlockPos deposit(ServerLevel l,BlockPos mouth){
  if(!passable(l,mouth))return null;
  BlockPos floor=mouth;int fall=0;
  while(fall++<128&&passable(l,floor.below()))floor=floor.below();
  if(fall>=128||!l.hasChunkAt(floor.below())||l.isOutsideBuildHeight(floor.below()))return null;
  for(int y=floor.getY();y<=mouth.getY();y++){
   BlockPos seed=new BlockPos(floor.getX(),y,floor.getZ());if(!passable(l,seed))return null;
   var queue=new ArrayDeque<BlockPos>();var seen=new HashSet<BlockPos>();queue.add(seed);BlockPos candidate=null;boolean open=false;
   while(!queue.isEmpty()&&seen.size()<2048){BlockPos p=queue.removeFirst();if(!seen.add(p))continue;
    if(!l.hasChunkAt(p)||p.distManhattan(seed)>24){open=true;break;}
    if(!passable(l,p))continue;
    if(passable(l,p.below())&&!l.getFluidState(p.below()).isSource()){open=true;break;}
    if(!l.getFluidState(p).isSource()&&candidate==null)candidate=p;
    for(Direction d:Direction.Plane.HORIZONTAL)queue.addLast(p.relative(d));
   }
   if(!queue.isEmpty()||seen.size()>=2048)open=true;
   if(open)return !l.getFluidState(seed).isSource()?seed:null;
   if(candidate!=null)return candidate;
  }return null;
 }
 public static void stream(ServerLevel l,BlockPos mouth,Direction nozzle){
  for(int i=0;i<128;i++){BlockPos p=mouth.below(i);if(!l.hasChunkAt(p)||l.isOutsideBuildHeight(p))break;var s=l.getBlockState(p);if(!s.isAir()&&!s.is(MareEletrica.FAUCET_STREAM.get()))break;var flow=MareEletrica.FAUCET_STREAM.get().defaultBlockState();if(i==0&&nozzle.getAxis().isHorizontal())flow=flow.setValue(FaucetStreamBlock.FACING,nozzle.getOpposite());l.setBlock(p,flow,2);l.scheduleTick(p,MareEletrica.FAUCET_STREAM.get(),10);}
 }
}
