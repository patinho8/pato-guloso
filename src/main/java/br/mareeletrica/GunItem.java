package br.mareeletrica;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@EventBusSubscriber(modid=MareEletrica.ID)
public class GunItem extends Item {
 public final WeaponProfile profile;
 private record Trigger(ItemStack gun,long expires){}
 private static final Map<UUID,Trigger> triggers=new HashMap<>();
 public GunItem(WeaponProfile p,Properties properties){super(properties);profile=p;}
 private static boolean valid(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&!RemoteSessions.active(p)&&p.containerMenu==p.inventoryMenu&&p.getMainHandItem().getItem() instanceof GunItem;}
 public static void trigger(ServerPlayer p,boolean down){if(!down||!valid(p)){triggers.remove(p.getUUID());return;}var profile=WeaponProfile.of(p.getMainHandItem());if(profile.automatic)triggers.put(p.getUUID(),new Trigger(p.getMainHandItem(),p.serverLevel().getGameTime()+8));fire(p);}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var t=triggers.get(p.getUUID());if(t==null)return;if(!valid(p)||p.getMainHandItem()!=t.gun||p.serverLevel().getGameTime()>t.expires){triggers.remove(p.getUUID());return;}fire(p);}
 @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){triggers.remove(e.getEntity().getUUID());}
 public static void open(ServerPlayer p){if(!valid(p))return;trigger(p,false);p.stopUsingItem();Magazines.finish(p,p.getMainHandItem(),false);p.openMenu(new SimpleMenuProvider((id,inv,who)->new GunMenu(id,inv),Component.literal("Oficina · "+WeaponProfile.of(p.getMainHandItem()).title)));}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){if(h!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(p.getItemInHand(h));if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(p.getItemInHand(h));p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
 @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.NONE;}
 public static void reload(ServerPlayer p){Magazines.reload(p);}
 public static void fire(ServerPlayer p){
  if(!valid(p)||Magazines.reloading(p.getMainHandItem())||p.getCooldowns().isOnCooldown(p.getMainHandItem().getItem()))return;
  var kit=GunLoadout.of(p);var profile=WeaponProfile.of(kit.gun);int count=GunLoadout.rounds(kit.gun);
  if(count==0){trigger(p,false);p.getCooldowns().addCooldown(kit.gun.getItem(),6);p.displayClientMessage(Component.literal("Carregador vazio. R para recarregar."),true);return;}
  kit.rounds(count-1);var aim=p.getLookAngle();boolean ads=SniperItem.scoped(p);float spread=(ads?profile.aimSpread:profile.hipSpread)*Accessory.spread(kit.gun,p,ads);if(!p.onGround())spread*=1.6F;if(ads&&p.getDeltaMovement().horizontalDistanceSqr()>.003)spread*=1.8F;
  for(int i=0;i<profile.pellets;i++){var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),p.level());b.setFirearm(profile.damage,profile.range,profile.pellets>1,profile.ammo());b.setOwner(p);b.setPos(p.getEyePosition());b.shoot(aim.x,aim.y,aim.z,profile.speed,spread);b.setDeltaMovement(b.getDeltaMovement().normalize().scale(profile.speed));p.level().addFreshEntity(b);}
  p.getCooldowns().addCooldown(kit.gun.getItem(),profile.interval);int silencer=RifleLoadout.suppressor(kit.gun);SniperItem.smoke(p,aim,silencer>0?1:profile.pistol?2:3);var sound=silencer>0?MareEletrica.SMG_SILENCED.get():profile.automatic?MareEletrica.SMG_SHOT.get():MareEletrica.REVOLVER_SHOT.get();float pitch=switch(profile){case DESERT_EAGLE->.78F;case USP->1.25F;case M4A1->.94F;case AK47->.72F;case M870->.55F;case MP5->1.15F;case MP40->1.0F;case P90->1.3F;case PYTHON->.85F;case GLOCK->1.3F;case SCAR->.8F;case UMP45->1.05F;case SPAS12->.5F;case BERETTA->1.15F;case UZI->1.25F;case FAMAS->1.0F;case M249->.85F;case BARRETT->.45F;case MAC10->1.2F;case GALIL->.85F;case AUG->.95F;case SAIGA12->.75F;case SVD->.6F;};p.level().playSound(null,p.blockPosition(),sound,SoundSource.PLAYERS,silencer>0?1.5F-silencer*.12F:profile.pistol?3.5F:5F,pitch);PacketDistributor.sendToPlayer(p,new GunPackets.Shot(false,profile.recoil*(ads?.8F:1F)*Accessory.recoil(kit.gun,p),(p.getRandom().nextFloat()-.5F)*profile.recoil*.35F*Accessory.recoil(kit.gun,p)));p.inventoryMenu.broadcastChanges();
 }
 @Override public boolean shouldCauseReequipAnimation(ItemStack a,ItemStack b,boolean changed){return changed||a.getItem()!=b.getItem();}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> l,TooltipFlag f){l.add(Component.literal("M1: "+(profile.automatic?"rajada":"disparar")+" • M2: mirar • R: recarregar • J: acessórios"));l.add(Component.literal("Carregador "+GunLoadout.rounds(s)+"/"+profile.capacity+" • "+(profile.pellets>1?profile.pellets+" projéteis por cartucho":profile.damage+" de dano")));l.add(Component.literal("Munição: ").append(profile.ammo().getDescription()));}
}
