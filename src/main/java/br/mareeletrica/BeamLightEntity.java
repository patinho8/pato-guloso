package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Shared lease: overlapping projectors extend the same light without deleting each other. */
public final class BeamLightEntity extends BlockEntity {
    public long expires;private long genericExpires;private final java.util.Map<Long,Long> owners=new java.util.HashMap<>();
    public void refreshFrom(long owner,long now){owners.entrySet().removeIf(e->e.getValue()<=now);owners.put(owner,now+40);expires=Math.max(genericExpires,owners.values().stream().mapToLong(Long::longValue).max().orElse(0));setChanged();}
    public void release(long owner,long now){owners.remove(owner);owners.entrySet().removeIf(e->e.getValue()<=now);expires=Math.max(genericExpires,owners.values().stream().mapToLong(Long::longValue).max().orElse(0));setChanged();}
    public BeamLightEntity(BlockPos pos, BlockState state) { super(MareEletrica.BEAM_ENTITY.get(), pos, state); }
    public void refresh(long now) { genericExpires=now+40;expires=Math.max(expires,genericExpires); setChanged(); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); tag.putLong("Expires", expires);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries); expires = tag.getLong("Expires");
    }
}
