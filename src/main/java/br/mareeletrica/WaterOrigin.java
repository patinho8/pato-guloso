package br.mareeletrica;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
public final class WaterOrigin extends SavedData {
 private final Set<Long> artificial=new HashSet<>();
 public static WaterOrigin get(ServerLevel l){return l.getDataStorage().computeIfAbsent(new SavedData.Factory<>(WaterOrigin::new,WaterOrigin::load,null),"mare_water_origin");}
 public static WaterOrigin load(CompoundTag t,HolderLookup.Provider lookup){var d=new WaterOrigin();for(long pos:t.getLongArray("Artificial"))d.artificial.add(pos);return d;}
 @Override public CompoundTag save(CompoundTag t,HolderLookup.Provider lookup){t.putLongArray("Artificial",artificial.stream().mapToLong(Long::longValue).toArray());return t;}
 public static boolean artificial(Level l,BlockPos p){return l instanceof ServerLevel s&&get(s).artificial.contains(p.asLong());}
 public static boolean eligible(Level l,BlockPos p){return l.hasChunkAt(p)&&l.getFluidState(p).is(FluidTags.WATER)&&!artificial(l,p);}
 public static void mark(ServerLevel l,BlockPos p){var d=get(l);if(d.artificial.add(p.asLong()))d.setDirty();}
 public static void clear(ServerLevel l,BlockPos p){var d=get(l);if(d.artificial.remove(p.asLong()))d.setDirty();}
}
