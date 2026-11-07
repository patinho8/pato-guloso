package br.mareeletrica.mixin;
import br.mareeletrica.RemoteSessions;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class PlayerPacketsMixin {
 @Shadow public ServerPlayer player;
 @Inject(method="handleSetCreativeModeSlot",at=@At("HEAD"),cancellable=true)
 private void mare$creativeEquipment(net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket packet,CallbackInfo ci){
  if(!player.server.isSameThread()||packet.slotNum()!=br.mareeletrica.DmtEquipment.slot(player.inventoryMenu))return;
  var stack=packet.itemStack();if(player.isCreative()&&(stack.isEmpty()||stack.is(br.mareeletrica.MareEletrica.DMT.get())&&stack.getCount()==1)){
   br.mareeletrica.DmtEquipment.set(player,stack.copy());player.inventoryMenu.broadcastChanges();br.mareeletrica.DmtEquipment.sync(player);
  }ci.cancel();
 }
 @Inject(method="handleMovePlayer",at=@At("HEAD"),cancellable=true)
 private void mare$stationaryBody(ServerboundMovePlayerPacket packet,CallbackInfo ci){if(player.server.isSameThread()&&RemoteSessions.active(player))ci.cancel();}
}
