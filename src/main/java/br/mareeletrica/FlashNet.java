package br.mareeletrica;

import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Server -> client: you were flashbanged with this strength (0..1). */
@EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
public final class FlashNet {
 public static Consumer<Float> client=s->{};
 private FlashNet(){}
 public record Flash(float strength) implements CustomPacketPayload {
  public static final Type<Flash> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"flashbang"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Flash> CODEC=StreamCodec.composite(ByteBufCodecs.FLOAT,Flash::strength,Flash::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){e.registrar("8").playToClient(Flash.TYPE,Flash.CODEC,(p,c)->client.accept(p.strength()));}
}
