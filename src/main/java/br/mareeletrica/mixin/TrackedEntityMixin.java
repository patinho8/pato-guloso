package br.mareeletrica.mixin;
import br.mareeletrica.RemoteSessions;
import br.mareeletrica.ScopeSight;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(targets="net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntityMixin {
 @Shadow @Final private Entity entity;
 @ModifyExpressionValue(method="updatePlayer",at=@At(value="INVOKE",target="Ljava/lang/Math;min(II)I"))
 private int mare$scopeRange(int original,@Local(argsOnly=true) ServerPlayer p){return ScopeSight.sees(p,entity)?576:original;}
 @Redirect(method="updatePlayer",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"))
 private Vec3 mare$viewerPosition(ServerPlayer p){return RemoteSessions.viewPosition(p);}
}
