package br.mareeletrica;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.*;
/** Every extraction mouth owns a saved buffer; no unsaved items in the network. */
public final class ItemDuctEntity extends BlockEntity{
 public final ItemStackHandler buffer=new ItemStackHandler(1){protected void onContentsChanged(int i){setChanged();}};
 public ItemDuctEntity(BlockPos p,BlockState s){super(MareEletrica.ITEM_DUCT_ENTITY.get(),p,s);}
 private record Visit(BlockPos pos,Direction side){}
 public void tick(){
  if(level==null||level.isClientSide||level.getGameTime()%8!=Math.floorMod(worldPosition.asLong(),8)||!getBlockState().getValue(ItemDuctBlock.INTAKE)||level.hasNeighborSignal(worldPosition))return;
  Direction side=getBlockState().getValue(ItemDuctBlock.SOURCE);BlockPos sourcePos=worldPosition.relative(side);if(!level.hasChunkAt(sourcePos))return;
  IItemHandler source=level.getCapability(Capabilities.ItemHandler.BLOCK,sourcePos,side.getOpposite());if(source==null)return;
  var queue=new ArrayDeque<Visit>();Set<BlockPos> visited=new HashSet<>();List<IItemHandler> targets=new ArrayList<>();
  visited.add(worldPosition);for(Direction d:Direction.values())if(d!=side)queue.add(new Visit(worldPosition.relative(d),d.getOpposite()));
  int inspected=0;while(!queue.isEmpty()&&inspected++<768&&visited.size()<256){Visit v=queue.removeFirst();BlockPos p=v.pos();if(p.equals(sourcePos)||!level.hasChunkAt(p)||p.distManhattan(worldPosition)>64||!visited.add(p))continue;
   BlockState state=level.getBlockState(p);if(state.getBlock() instanceof ItemDuctBlock){for(Direction d:Direction.values())queue.addLast(new Visit(p.relative(d),d.getOpposite()));}
   else {if(p.distManhattan(sourcePos)==1&&state.getBlock() instanceof net.minecraft.world.level.block.ChestBlock&&level.getBlockState(sourcePos).getBlock() instanceof net.minecraft.world.level.block.ChestBlock)continue;IItemHandler h=level.getCapability(Capabilities.ItemHandler.BLOCK,p,v.side());if(h!=null&&h!=source&&h!=buffer)targets.add(h);}
  }
  if(targets.isEmpty())return;
  if(SafeItemTransfer.empty(buffer))SafeItemTransfer.stage(source,buffer,8,stack->targets.stream().anyMatch(h->SafeItemTransfer.insert(h,stack,true).getCount()<stack.getCount()));
  for(IItemHandler h:targets)if(SafeItemTransfer.deliver(buffer,h,8)>0)break;
 }
 protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.put("Buffer",buffer.serializeNBT(r));}
 protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);if(t.contains("Buffer"))buffer.deserializeNBT(r,t.getCompound("Buffer"));}
}
