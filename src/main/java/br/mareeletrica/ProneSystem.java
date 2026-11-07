package br.mareeletrica;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid=MareEletrica.ID)
public final class ProneSystem {
 private static final ResourceLocation SPEED=ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"prone_speed");
 public static boolean prone(Player p){return p!=null&&p.getPersistentData().getBoolean("MareProne");}
 public static boolean supported(Player p){return prone(p)&&p.onGround()&&!p.isInWater()&&p.getDeltaMovement().horizontalDistanceSqr()<.0009&&RifleLoadout.bipod(p.getMainHandItem());}
 public static void toggle(ServerPlayer p){if(p.isSpectator()||!p.isAlive()||p.containerMenu!=p.inventoryMenu||RemoteSessions.active(p))return;if(!prone(p)&&(!p.onGround()||p.isPassenger()||p.isInWater()||p.isFallFlying()))return;if(prone(p)&&!p.level().noCollision(p,Pose.STANDING==p.getPose()?p.getBoundingBox():p.getDimensions(Pose.STANDING).makeBoundingBox(p.position())))return;set(p,!prone(p));}
 public static void set(Player p,boolean value){p.getPersistentData().putBoolean("MareProne",value);p.setForcedPose(value?Pose.SWIMMING:null);var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null){speed.removeModifier(SPEED);if(value)speed.addTransientModifier(new AttributeModifier(SPEED,.3,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}if(value){p.setSprinting(false);p.setPose(Pose.SWIMMING);}updateBipod(p);if(p instanceof ServerPlayer s)PacketDistributor.sendToPlayersTrackingEntityAndSelf(s,new GunPackets.Prone(s.getId(),value));}
 private static void updateBipod(Player p){var gun=p.getMainHandItem();if(!MareEletrica.isSniper(gun))return;boolean deployed=supported(p);var tag=RifleLoadout.tag(gun);if(tag.getBoolean("BipodDeployed")!=deployed){tag.putBoolean("BipodDeployed",deployed);gun.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));p.getInventory().setChanged();}}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;updateBipod(p);if(!prone(p))return;if(!p.isAlive()||p.isSpectator()||p.isInWater()||p.isPassenger()||p.isFallFlying()){set(p,false);return;}p.setForcedPose(Pose.SWIMMING);p.setSprinting(false);}
 @SubscribeEvent public static void track(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer p&&e.getTarget() instanceof Player target)PacketDistributor.sendToPlayer(p,new GunPackets.Prone(target.getId(),prone(target)));}
 @SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)set(p,false);}
 @SubscribeEvent public static void change(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)set(p,false);}
 @SubscribeEvent public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)set(p,false);}
}
