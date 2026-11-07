package br.mareeletrica.mixin;
import br.mareeletrica.DmtEquipment;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Player.class)
public abstract class PlayerEquipmentMixin {
 @Inject(method="dropEquipment",at=@At("TAIL"))
 private void mare$dropDmt(CallbackInfo ci){DmtEquipment.drop((Player)(Object)this);br.mareeletrica.CamoEquipment.drop((Player)(Object)this);}
}
