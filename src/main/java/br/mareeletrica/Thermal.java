package br.mareeletrica;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.65.4: upgraded thermal goggles (the Gadgets item). Worn on the head: every living thing within 96 blocks glows through walls in heat colours
 *  (warm = orange/red, undead = cold blue), the world gets a dark thermal tint and you see in the dark. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class Thermal {
 private Thermal(){}
 public static void register(){}
 public static boolean wearing(Player p){return Gadgets.wearsGoggles(p);}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  var p=e.getEntity();if(p.level().isClientSide||p.tickCount%100!=0||!wearing(p))return;
  p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,260,0,false,false,false));
 }
}
