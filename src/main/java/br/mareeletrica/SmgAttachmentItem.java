package br.mareeletrica;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
public final class SmgAttachmentItem extends Item {
 public final boolean optic;public final int value;
 public SmgAttachmentItem(boolean optic,int value){super(new Properties().stacksTo(optic?1:16));this.optic=optic;this.value=value;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<Component> lines,TooltipFlag f){lines.add(Component.literal(optic?"Óptica antiga "+value+"×":"Pintura completa para a submetralhadora"));lines.add(Component.literal("Mesa de trabalho: combine com um pente vazio para adaptar à arma."));}
}
