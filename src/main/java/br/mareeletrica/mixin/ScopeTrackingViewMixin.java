package br.mareeletrica.mixin;
import br.mareeletrica.ScopeSight;
import java.util.function.Consumer;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ChunkTrackingView.class)
public interface ScopeTrackingViewMixin {
 @Inject(method="difference",at=@At("HEAD"),cancellable=true)
 private static void mare$sparseDifference(ChunkTrackingView old,ChunkTrackingView next,Consumer<ChunkPos> added,Consumer<ChunkPos> removed,CallbackInfo ci){
  if(!(old instanceof ScopeSight.View)&&!(next instanceof ScopeSight.View))return;
  old.forEach(p->{if(!next.contains(p))removed.accept(p);});next.forEach(p->{if(!old.contains(p))added.accept(p);});ci.cancel();
 }
}
