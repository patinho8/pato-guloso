package br.mareeletrica;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
public final class WirelessPlateItem extends Item {
 public final int tier;
 public WirelessPlateItem(int tier,Properties p){super(p);this.tier=tier;}
 @Override public InteractionResult useOn(UseOnContext c){return WirelessPower.attach(c,tier);}
 @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,TooltipContext context,java.util.List<Component> lines,net.minecraft.world.item.TooltipFlag flag){lines.add(Component.literal("Instale no núcleo e na máquina. "+WirelessPower.RANGE[tier]+" blocos; "+WirelessPower.RATE[tier]+" FE / 10 ticks."));lines.add(Component.literal("Shift no núcleo: vincular. Shift na máquina: retirar placa."));}
}
