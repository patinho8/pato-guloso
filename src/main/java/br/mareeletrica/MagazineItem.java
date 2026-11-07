package br.mareeletrica;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
public final class MagazineItem extends Item {
 public final WeaponFamily family;private final boolean filled;
 public MagazineItem(WeaponFamily f,boolean filled){super(new Properties().stacksTo(1));family=f;this.filled=filled;}
 public static int rounds(ItemStack s){if(!(s.getItem() instanceof MagazineItem m))return 0;var t=RifleLoadout.tag(s);return Math.clamp(t.contains("Rounds")?t.getInt("Rounds"):m.filled?m.family.capacity:0,0,m.family.capacity);}
 public static ItemStack make(WeaponFamily f,int count){var s=new ItemStack(ArmoryParts.magazine(f,count>0));var t=RifleLoadout.tag(s);t.putInt("Rounds",Math.clamp(count,0,f.capacity));s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(level.isClientSide)return InteractionResultHolder.success(s);int count=rounds(s),added=0;if(!p.isCreative())for(int i=0;i<p.getInventory().getContainerSize()&&count+added<family.capacity;i++){var a=p.getInventory().getItem(i);if(a.is(family.ammo())){int n=Math.min(a.getCount(),family.capacity-count-added);a.shrink(n);added+=n;}}if(p.isCreative())added=family.capacity-count;if(added==0){p.displayClientMessage(Component.literal(count==family.capacity?"Pente cheio.":"Faltam cartuchos de "+family.title+" no inventário."),true);return InteractionResultHolder.fail(s);}var loaded=make(family,count+added);p.setItemInHand(hand,loaded);p.getInventory().setChanged();return InteractionResultHolder.success(loaded);}
 @Override public boolean isBarVisible(ItemStack s){return true;}
 @Override public int getBarWidth(ItemStack s){return Math.round(13F*rounds(s)/family.capacity);}
 @Override public int getBarColor(ItemStack s){return rounds(s)>0?0x69C7BF:0xA54D51;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag flags){out.add(Component.literal(family.title+" • "+rounds(s)+" / "+family.capacity));out.add(Component.literal("M2: abastecer com cartuchos do inventário"));out.add(Component.literal("R com a arma: trocar o pente • J: reserva"));}
}
