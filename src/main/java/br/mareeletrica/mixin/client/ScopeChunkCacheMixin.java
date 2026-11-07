package br.mareeletrica.mixin.client;
import br.mareeletrica.client.ScopeClient;
import java.util.function.Consumer;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ClientChunkCache.class)
public abstract class ScopeChunkCacheMixin {
 @ModifyVariable(method="updateViewRadius",at=@At("HEAD"),argsOnly=true)
 private int mare$retainRadius(int original){return Math.max(original,ScopeClient.radius());}
 @Inject(method="getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;",at=@At("HEAD"),cancellable=true)
 private void mare$retainedChunk(int x,int z,ChunkStatus status,boolean nonnull,CallbackInfoReturnable<LevelChunk> ci){var c=ScopeClient.cached(x,z);if(c!=null)ci.setReturnValue(c);}
 @Inject(method="replaceWithPacketData",at=@At("RETURN"))
 private void mare$remember(int x,int z,FriendlyByteBuf data,CompoundTag heights,Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> blockEntities,CallbackInfoReturnable<LevelChunk> ci){ScopeClient.remember(ci.getReturnValue());}
}
