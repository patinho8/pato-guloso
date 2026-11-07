package br.mareeletrica.mixin.client;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Slot.class)
public interface SlotPositionAccessor {
 @Mutable @Accessor("x") void mare$setX(int x);
 @Mutable @Accessor("y") void mare$setY(int y);
}
