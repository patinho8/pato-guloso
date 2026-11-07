package br.mareeletrica.mixin;
import br.mareeletrica.RemoteSessions;
import br.mareeletrica.ScopeSight;
import br.mareeletrica.RemoteTracking;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin implements RemoteTracking {
 @Shadow private void applyChunkTrackingView(ServerPlayer p,ChunkTrackingView view){throw new AssertionError();}
 @Shadow abstract int getPlayerViewDistance(ServerPlayer p);
 @Shadow private void updateChunkTracking(ServerPlayer p){throw new AssertionError();}
 @Override public void mare$refresh(ServerPlayer p){updateChunkTracking(p);}
 @Inject(method="updateChunkTracking",at=@At("HEAD"),cancellable=true)
 private void mare$remoteView(ServerPlayer p,CallbackInfo ci){
  if(!RemoteSessions.active(p)){var view=ScopeSight.view(p,getPlayerViewDistance(p));if(view!=null){applyChunkTrackingView(p,view);ci.cancel();}return;}var center=RemoteSessions.viewChunk(p);
  if(!(p.getChunkTrackingView() instanceof ChunkTrackingView.Positioned c)||!c.center().equals(center)||c.viewDistance()!=RemoteSessions.viewDistance(p))applyChunkTrackingView(p,ChunkTrackingView.of(center,RemoteSessions.viewDistance(p)));ci.cancel();
 }
}
