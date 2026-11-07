package br.mareeletrica;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
public final class CamoArmorItem extends ArmorItem {
 public final String biome;
 public CamoArmorItem(String biome,Type type){super(Camouflage.MATERIAL,type,new Item.Properties().durability(type.getDurability(18)));this.biome=biome;}
 @Override public boolean canEquip(ItemStack stack,EquipmentSlot slot,LivingEntity entity){return false;}
 @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand hand){
  var held=p.getItemInHand(hand);
  if(!level.isClientSide){int i=CamoEquipment.index(getType());var gear=CamoEquipment.inventory(p);var old=gear.getStackInSlot(i);gear.setStackInSlot(i,held.copyWithCount(1));held.shrink(1);
   if(!old.isEmpty()){if(held.isEmpty())p.setItemInHand(hand,old);else if(!p.getInventory().add(old))p.drop(old,false);}
   level.playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER.value(),net.minecraft.sounds.SoundSource.PLAYERS,1F,1F);}
  return net.minecraft.world.InteractionResultHolder.sidedSuccess(p.getItemInHand(hand),level.isClientSide);
 }
 @Override public ResourceLocation getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner){return ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/models/armor/camo_"+biome+".png");}
 @Override public void appendHoverText(ItemStack s,TooltipContext context,List<Component> tips,TooltipFlag flag){super.appendHoverText(s,context,tips,flag);tips.add(Component.translatable("tooltip.mareeletrica.camouflage",Component.translatable("biome."+(biome.equals("stone_cave")||biome.equals("deepslate_cave")?"mareeletrica":"minecraft")+"."+biome)).withStyle(net.minecraft.ChatFormatting.DARK_GREEN));tips.add(Component.translatable("tooltip.mareeletrica.camouflage_equip").withStyle(net.minecraft.ChatFormatting.GRAY));}
}
