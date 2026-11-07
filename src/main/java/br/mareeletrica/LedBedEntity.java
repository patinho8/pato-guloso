package br.mareeletrica;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class LedBedEntity extends BlockEntity {
 public LedBedEntity(BlockPos pos,BlockState state){super(MareEletrica.LED_BED_ENTITY.get(),pos,state);}
}
