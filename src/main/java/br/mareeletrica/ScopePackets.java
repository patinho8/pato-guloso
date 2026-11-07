package br.mareeletrica;

import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ScopePackets {
 public static Consumer<State> client=p->{};
 public record State(int radius,long[] chunks) implements CustomPacketPayload {
  public static final Type<State> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"scope_view"));
  public static final StreamCodec<FriendlyByteBuf,State> CODEC=new StreamCodec<>() {
   public State decode(FriendlyByteBuf b){int radius=b.readVarInt();long[] chunks=b.readLongArray(null,ScopeSight.MAX_CHUNKS);return new State(Math.max(0,Math.min(ScopeSight.MAX_RADIUS,radius)),chunks);}
   public void encode(FriendlyByteBuf b,State p){b.writeVarInt(p.radius);b.writeLongArray(p.chunks);}
  };
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static void register(RegisterPayloadHandlersEvent e){e.registrar("1").playToClient(State.TYPE,State.CODEC,(p,c)->client.accept(p));}
}
