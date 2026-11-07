package br.mareeletrica;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public final class RemotePackets {
 private RemotePackets(){}
 public static Consumer<State> clientState=packet->{};
 public record Drive(int token,float forward,float yaw,float pitch,int flags) implements CustomPacketPayload {
  public static final Type<Drive> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"rover_drive"));
  public static final StreamCodec<ByteBuf,Drive> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Drive::token,ByteBufCodecs.FLOAT,Drive::forward,ByteBufCodecs.FLOAT,Drive::yaw,ByteBufCodecs.FLOAT,Drive::pitch,ByteBufCodecs.VAR_INT,Drive::flags,Drive::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record State(int token,int entityId,int energy,int ammo,int mode,int distance) implements CustomPacketPayload {
  public static final Type<State> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"rover_view"));
  public static final StreamCodec<ByteBuf,State> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,State::token,ByteBufCodecs.VAR_INT,State::entityId,ByteBufCodecs.VAR_INT,State::energy,ByteBufCodecs.VAR_INT,State::ammo,ByteBufCodecs.VAR_INT,State::mode,ByteBufCodecs.VAR_INT,State::distance,State::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Aim(int menu,int barrel,int elevation,int azimuth) implements CustomPacketPayload {
  public static final Type<Aim> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"mortar_aim"));
  public static final StreamCodec<ByteBuf,Aim> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Aim::menu,ByteBufCodecs.VAR_INT,Aim::barrel,ByteBufCodecs.VAR_INT,Aim::elevation,ByteBufCodecs.VAR_INT,Aim::azimuth,Aim::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Cargo(int token) implements CustomPacketPayload{
  public static final Type<Cargo> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"rover_cargo"));
  public static final StreamCodec<ByteBuf,Cargo> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Cargo::token,Cargo::new);
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static void register(RegisterPayloadHandlersEvent event){
  var r=event.registrar("4");r.playToServer(Cargo.TYPE,Cargo.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer s)RemoteSessions.openCargo(s,p.token());});
  r.playToServer(Drive.TYPE,Drive.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer s)RemoteSessions.input(s,p);});
  r.playToClient(State.TYPE,State.CODEC,(p,c)->clientState.accept(p));
  r.playToServer(Aim.TYPE,Aim.CODEC,(p,c)->{if(c.player().containerMenu instanceof MortarMenu m&&m.containerId==p.menu())m.aim(c.player(),p.barrel(),p.elevation(),p.azimuth());});
 }
}
