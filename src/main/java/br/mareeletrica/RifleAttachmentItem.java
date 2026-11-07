package br.mareeletrica;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** A game-only module fitted in the rifle's matching inventory socket. */
public final class RifleAttachmentItem extends Item {
 public enum Type { SCOPE, SUPPRESSOR, BIPOD, STICKER }
 public final Type type; public final int value;
 public RifleAttachmentItem(Type type,int value){super(new Properties().stacksTo(type==Type.STICKER?16:1));this.type=type;this.value=value;}
 @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
  lines.add(Component.literal(switch(type){case SCOPE->"Ampliação "+value+"× • regulagem começa em 0 ao equipar";case SUPPRESSOR->"Silenciador nível "+value+" • reduz o estampido sem apagar o disparo";case BIPOD->"Deitado e parado no chão: estabiliza a sniper";case STICKER->"Pintura do corpo da arma • preserva lentes e peças pretas";}));
  lines.add(Component.literal("Miras/bipé: J na sniper. Pinturas/silenciadores antigos: combine com pente vazio para adaptar."));
 }
}
