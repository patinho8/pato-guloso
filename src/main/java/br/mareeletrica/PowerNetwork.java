package br.mareeletrica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** Widest available route; cable capacity is shared by all senders in each ten-tick window. */
public final class PowerNetwork {
    public static final int NORMAL=8_000,CYBER=128_000;
    private record Step(BlockPos pos,int capacity,List<BlockPos> cables) {}
    private record Route(MachineEntity machine,List<BlockPos> cables) {}
    private static final class Ledger {long epoch=Long.MIN_VALUE;final Map<BlockPos,Integer> used=new HashMap<>();}
    private static final Map<Level,Ledger> LEDGERS=new WeakHashMap<>();
    private PowerNetwork() {}
    private static Ledger ledger(Level level) {
        Ledger ledger=LEDGERS.computeIfAbsent(level,l->new Ledger());long epoch=level.getGameTime()/10;
        if(epoch!=ledger.epoch) {ledger.epoch=epoch;ledger.used.clear();}return ledger;
    }
    private static int capacity(Level level,BlockPos p,Ledger ledger) {
        if(!(level.getBlockState(p).getBlock() instanceof CableBlock cable))return 0;
        return Math.max(0,cable.capacity-ledger.used.getOrDefault(p,0));
    }
    private static List<Route> routes(Level level,BlockPos origin) {
        Ledger ledger=ledger(level);var queue=new PriorityQueue<Step>((a,b)->Integer.compare(b.capacity,a.capacity));
        Map<BlockPos,Integer> best=new HashMap<>();Map<BlockPos,Route> terminals=new HashMap<>();
        queue.add(new Step(origin,Integer.MAX_VALUE,List.of()));best.put(origin,Integer.MAX_VALUE);
        if(level.getBlockEntity(origin) instanceof MachineEntity m&&m.kind.large()) {
            int count=LargeMachineStructure.parts(m.kind,m.getBlockState());
            for(int part=1;part<count;part++) {BlockPos p=LargeMachineStructure.position(m,part);if(level.hasChunkAt(p)){queue.add(new Step(p,Integer.MAX_VALUE,List.of()));best.put(p,Integer.MAX_VALUE);}}
        }
        int visited=0;
        while(!queue.isEmpty()&&visited++<4096) {
            Step current=queue.remove();if(current.capacity<best.getOrDefault(current.pos,0))continue;
            for(Direction direction:Direction.values()) {
                BlockPos next=current.pos.relative(direction);
                if(!level.hasChunkAt(next)||Math.abs(next.getX()-origin.getX())>128||Math.abs(next.getY()-origin.getY())>128||Math.abs(next.getZ()-origin.getZ())>128)continue;
                if(level.getBlockState(next).getBlock() instanceof CableBlock) {
                    int cap=Math.min(current.capacity,capacity(level,next,ledger));if(cap<=best.getOrDefault(next,0))continue;
                    best.put(next,cap);var path=new ArrayList<>(current.cables);path.add(next);queue.add(new Step(next,cap,List.copyOf(path)));
                } else {
                    MachineEntity target=PotabilizerStructure.machineAt(level,next);
                    if(target==null||!target.enabled||target.getBlockPos().equals(origin))continue;
                    terminals.putIfAbsent(target.getBlockPos(),new Route(target,current.cables));
                    if(target.kind==Kind.GIANT_WHEEL){for(int part=0;part<LargeMachineStructure.parts(target.kind,target.getBlockState());part++){var cell=LargeMachineStructure.position(target,part);if(level.hasChunkAt(cell)&&current.capacity>best.getOrDefault(cell,0)){best.put(cell,current.capacity);queue.add(new Step(cell,current.capacity,current.cables));}}}
                }
            }
        }
        return new ArrayList<>(terminals.values());
    }
    public static List<MachineEntity> endpoints(Level level,BlockPos origin) {return new ArrayList<>(routes(level,origin).stream().map(Route::machine).toList());}
    public static int send(MachineEntity source,boolean dirty,int budget,Predicate<Kind> accepts) {
        Level level=source.getLevel();if(level==null)return 0;Ledger ledger=ledger(level);
        List<Route> routes=routes(level,source.getBlockPos());routes.removeIf(r->!accepts.test(r.machine.kind));if(routes.isEmpty())return 0;
        EnergyTank tank=dirty?source.dirty:source.clean;int remaining=Math.min(budget,tank.stored()),total=0;
        int start=Math.floorMod(source.routeCursor++,routes.size()),quota=Math.max(1,remaining/routes.size());
        int voltage=source.kind.voltage();
        for(int i=0;i<routes.size()&&remaining>0;i++) {
            Route route=routes.get((start+i)%routes.size());int cap=Math.min(remaining,quota);
            for(BlockPos p:route.cables)cap=Math.min(cap,capacity(level,p,ledger));
            int moved=tank.moveTo(dirty?route.machine.dirty:route.machine.clean,cap);
            if(moved>0) {for(BlockPos p:route.cables)ledger.used.merge(p,moved,Integer::sum);route.machine.setChanged();total+=moved;remaining-=moved;
                if(voltage>0){int v=voltage;for(BlockPos p:route.cables)if(level.getBlockState(p).getBlock() instanceof CableBlock c)v=Math.min(v,c.voltageLimit);route.machine.powered(v);}}
        }
        if(total>0)source.setChanged();return total;
    }
}
