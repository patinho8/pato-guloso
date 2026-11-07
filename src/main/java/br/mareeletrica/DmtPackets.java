package br.mareeletrica;

import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class DmtPackets {
 public static Consumer<State> client=p->{};
 public static Consumer<Equipment> clientEquipment=p->{};
 public record Equipment(int entity,net.minecraft.world.item.ItemStack stack) implements CustomPacketPayload {
  public static final Type<Equipment> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"dmt_equipment"));
  public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,Equipment> CODEC=StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.VAR_INT,Equipment::entity,net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC,Equipment::stack,Equipment::new);
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Command(int action) implements CustomPacketPayload {
  public static final Type<Command> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"dmt_command"));
  public static final StreamCodec<FriendlyByteBuf,Command> CODEC=new StreamCodec<>(){public Command decode(FriendlyByteBuf b){return new Command(b.readVarInt());}public void encode(FriendlyByteBuf b,Command p){b.writeVarInt(p.action);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Control(boolean boost,float side) implements CustomPacketPayload {
  public static final Type<Control> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"dmt_control"));
  public static final StreamCodec<FriendlyByteBuf,Control> CODEC=new StreamCodec<>(){public Control decode(FriendlyByteBuf b){return new Control(b.readBoolean(),b.readFloat());}public void encode(FriendlyByteBuf b,Control p){b.writeBoolean(p.boost);b.writeFloat(p.side);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record State(int entity,int flags,int fuel,double lx,double ly,double lz,double rx,double ry,double rz,float leftLength,float rightLength) implements CustomPacketPayload {
  public State(int entity,int flags,int fuel,double lx,double ly,double lz,double rx,double ry,double rz){this(entity,flags,fuel,lx,ly,lz,rx,ry,rz,24,24);}
  public static final Type<State> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"dmt_state"));
  public static final StreamCodec<FriendlyByteBuf,State> CODEC=new StreamCodec<>(){public State decode(FriendlyByteBuf b){return new State(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readFloat());}public void encode(FriendlyByteBuf b,State p){b.writeVarInt(p.entity);b.writeVarInt(p.flags);b.writeVarInt(p.fuel);b.writeDouble(p.lx);b.writeDouble(p.ly);b.writeDouble(p.lz);b.writeDouble(p.rx);b.writeDouble(p.ry);b.writeDouble(p.rz);b.writeFloat(p.leftLength);b.writeFloat(p.rightLength);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("2");r.playToClient(Equipment.TYPE,Equipment.CODEC,(p,c)->clientEquipment.accept(p));r.playToServer(Command.TYPE,Command.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer s)DmtSystem.command(s,p.action);});r.playToServer(Control.TYPE,Control.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer s)DmtSystem.control(s,p.boost,p.side);});r.playToClient(State.TYPE,State.CODEC,(p,c)->client.accept(p));}
}
