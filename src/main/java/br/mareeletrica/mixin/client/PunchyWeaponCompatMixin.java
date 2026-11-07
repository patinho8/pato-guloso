package br.mareeletrica.mixin.client;
import br.mareeletrica.SniperItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Firearms own both hands; Punchy remains active on every other item. */
@Pseudo
@Mixin(targets="punchy.config.PunchyConfig",remap=false)
public abstract class PunchyWeaponCompatMixin {
 @Inject(method={"isItemBlacklisted","isItemBlacklistDualHanded"},at=@At("HEAD"),cancellable=true,require=0)
 private static void mare$weapon(ItemStack item,CallbackInfoReturnable<Boolean> cir){if(SniperItem.firearm(item))cir.setReturnValue(true);}
 @Inject(method="isHandBlacklisted",at=@At("HEAD"),cancellable=true,require=0)
 private static void mare$hands(Player p,InteractionHand hand,CallbackInfoReturnable<Boolean> cir){if(p!=null&&SniperItem.firearm(p.getMainHandItem()))cir.setReturnValue(true);}
}
