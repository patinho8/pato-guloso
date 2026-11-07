package br.mareeletrica;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** No block entities per projected voxel. Owners renew one lease for the whole structure. */
public final class HologramIndex extends SavedData {
    private record Cell(long owner,long stamp) {}
    private record Lease(long stamp,long until) {}
    private long serial;
    public long nextStamp() {setDirty();return ++serial;}
    private final Map<Long,Cell> cells=new HashMap<>();
    private final Map<Long,Lease> owners=new HashMap<>();
    public static HologramIndex get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(HologramIndex::new,HologramIndex::load,null),"mare_holograms");
    }
    private static HologramIndex load(CompoundTag nbt,HolderLookup.Provider lookup) {
        HologramIndex data=new HologramIndex();data.serial=nbt.getLong("Serial");ListTag list=nbt.getList("Cells",10);
        for(int i=0;i<list.size();i++) { CompoundTag c=list.getCompound(i);data.cells.put(c.getLong("P"),new Cell(c.getLong("O"),c.getLong("S"))); }
        // On reload, controllers have to renew their leases before scheduled expiry.
        return data;
    }
    @Override public CompoundTag save(CompoundTag nbt,HolderLookup.Provider lookup) {
        ListTag list=new ListTag();cells.forEach((p,c)->{CompoundTag n=new CompoundTag();n.putLong("P",p);n.putLong("O",c.owner);n.putLong("S",c.stamp);list.add(n);});
        nbt.putLong("Serial",serial);nbt.put("Cells",list);return nbt;
    }
    public boolean current(BlockPos p,BlockPos root,long stamp) {Cell c=cells.get(p.asLong());return c!=null&&c.owner==root.asLong()&&c.stamp==stamp;}
    public boolean claimable(BlockPos p,BlockPos root) { Cell c=cells.get(p.asLong());return c==null||c.owner==root.asLong(); }
    public void claim(BlockPos p,BlockPos root,long stamp) { cells.put(p.asLong(),new Cell(root.asLong(),stamp));setDirty(); }
    public void renew(BlockPos root,long stamp,long now) { owners.put(root.asLong(),new Lease(stamp,now+60)); }
    public void stop(BlockPos root) { owners.remove(root.asLong()); }
    public boolean alive(BlockPos p,long now) { Cell c=cells.get(p.asLong());Lease l=c==null?null:owners.get(c.owner);return l!=null&&l.stamp==c.stamp&&l.until>now; }
    public void remove(BlockPos p) { if(cells.remove(p.asLong())!=null)setDirty(); }
}
