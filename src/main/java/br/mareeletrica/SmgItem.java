package br.mareeletrica;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@EventBusSubscriber(modid=MareEletrica.ID)
public final class SmgItem extends Item {
 private record Trigger(ItemStack gun,long expires){}
 private static final Map<UUID,Trigger> triggers=new HashMap<>();
 public SmgItem(Properties p){super(p);}
 private static boolean valid(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&!RemoteSessions.active(p)&&p.containerMenu==p.inventoryMenu&&p.getMainHandItem().is(MareEletrica.SMG.get());}
 public static void trigger(ServerPlayer p,boolean down){if(!down||!valid(p)){triggers.remove(p.getUUID());return;}triggers.put(p.getUUID(),new Trigger(p.getMainHandItem(),p.serverLevel().getGameTime()+8));fire(p);}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var t=triggers.get(p.getUUID());if(t==null)return;if(!valid(p)||p.getMainHandItem()!=t.gun||p.serverLevel().getGameTime()>t.expires){triggers.remove(p.getUUID());return;}fire(p);}
 @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){triggers.remove(e.getEntity().getUUID());}
 public static void open(ServerPlayer p){if(!valid(p))return;trigger(p,false);p.stopUsingItem();Magazines.finish(p,p.getMainHandItem(),false);p.openMenu(new SimpleMenuProvider((id,inv,who)->new SmgMenu(id,inv),Component.literal("Oficina da submetralhadora")));}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand h){if(h!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(p.getItemInHand(h));p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
 @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.NONE;}
 public static void reload(ServerPlayer p){Magazines.reload(p);}
 public static void fire(ServerPlayer p){if(!valid(p)||Magazines.reloading(p.getMainHandItem())||p.getCooldowns().isOnCooldown(MareEletrica.SMG.get()))return;var kit=SmgLoadout.of(p);int rounds=SmgLoadout.rounds(kit.gun);if(rounds==0){trigger(p,false);p.getCooldowns().addCooldown(kit.gun.getItem(),6);p.displayClientMessage(Component.literal("Carregador vazio. R para recarregar."),true);return;}kit.rounds(rounds-1);var aim=p.getLookAngle();var b=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),p.level());b.setSmg(true);b.setOwner(p);b.setPos(p.getEyePosition());b.shoot(aim.x,aim.y,aim.z,12,SniperItem.scoped(p)?(p.getDeltaMovement().horizontalDistanceSqr()>.003?2.4F:1.35F):9F);p.level().addFreshEntity(b);p.getCooldowns().addCooldown(kit.gun.getItem(),2);int silencer=RifleLoadout.suppressor(kit.gun);SniperItem.smoke(p,aim,1);p.level().playSound(null,p.blockPosition(),silencer>0?MareEletrica.SMG_SILENCED.get():MareEletrica.SMG_SHOT.get(),SoundSource.PLAYERS,silencer>0?1.4F-silencer*.12F:3F,1+(p.getRandom().nextFloat()-.5F)*.08F);PacketDistributor.sendToPlayer(p,new GunPackets.Shot(false,SniperItem.scoped(p)?.65F:1.2F,(p.getRandom().nextFloat()-.5F)*.6F));}
 @Override public boolean shouldCauseReequipAnimation(ItemStack a,ItemStack b,boolean changed){return changed||a.getItem()!=b.getItem();}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){lines.add(Component.literal("M1 segurar: rajada • M2: mirar • R: recarregar • J: painel"));lines.add(Component.literal("Carregador "+SmgLoadout.rounds(s)+"/30 • 10 tiros/s • 3 de dano por tiro"));}
}
