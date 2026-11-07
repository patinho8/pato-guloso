package br.mareeletrica;

import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Lean/peek state: client tells the server, server tells everyone who can see the player. */
@EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
public final class LeanNet {
 public static Consumer<Show> client=p->{};
 public record Choose(int dir) implements CustomPacketPayload {
  public static final Type<Choose> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"lean_choose"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Choose> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Choose::dir,Choose::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Show(int entity,int dir) implements CustomPacketPayload {
  public static final Type<Show> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"lean_show"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Show> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Show::entity,ByteBufCodecs.VAR_INT,Show::dir,Show::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
  var r=e.registrar("4");
  r.playToServer(Choose.TYPE,Choose.CODEC,(p,c)->{
   if(!(c.player() instanceof ServerPlayer s))return;
   int dir=Mth.clamp(p.dir(),-1,1);
   if(dir!=0&&!SniperItem.firearm(s.getMainHandItem()))dir=0;
   PacketDistributor.sendToPlayersTrackingEntityAndSelf(s,new Show(s.getId(),dir));
  });
  r.playToClient(Show.TYPE,Show.CODEC,(p,c)->client.accept(p));
 }
}
