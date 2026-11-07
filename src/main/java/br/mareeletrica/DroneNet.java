package br.mareeletrica;

import java.util.function.IntConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Drone piloting packets: input every tick, actions (drop bomb / exit) and the camera session. */
@EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
public final class DroneNet {
 public static IntConsumer sessionHandler=id->{};
 private DroneNet(){}
 public record Input(float forward,float strafe,boolean up,boolean down,boolean boost,float yaw,float pitch,double x,double y,double z) implements CustomPacketPayload {
  public static final Type<Input> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"drone_input"));
  // 7 fields: written by hand (StreamCodec.composite only goes up to 6 in 1.21.1)
  public static final StreamCodec<RegistryFriendlyByteBuf,Input> CODEC=StreamCodec.of(
   (b,p)->{b.writeFloat(p.forward);b.writeFloat(p.strafe);b.writeByte((p.up?1:0)|(p.down?2:0)|(p.boost?4:0));b.writeFloat(p.yaw);b.writeFloat(p.pitch);b.writeDouble(p.x);b.writeDouble(p.y);b.writeDouble(p.z);},
   b->{float f=b.readFloat(),s=b.readFloat();int k=b.readByte();float yaw=b.readFloat(),pitch=b.readFloat();return new Input(f,s,(k&1)!=0,(k&2)!=0,(k&4)!=0,yaw,pitch,b.readDouble(),b.readDouble(),b.readDouble());});
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 /** kind 1 = drop bomb, 2 = leave the drone. */
 public record Action(int kind) implements CustomPacketPayload {
  public static final Type<Action> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"drone_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Action::kind,Action::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Session(int entityId) implements CustomPacketPayload {
  public static final Type<Session> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"drone_session"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Session> CODEC=StreamCodec.composite(ByteBufCodecs.INT,Session::entityId,Session::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
  var r=e.registrar("7");
  r.playToServer(Input.TYPE,Input.CODEC,(p,c)->{
   if(!(c.player() instanceof ServerPlayer player))return;
   var d=Drones.droneOf(player);if(d==null)return;
   d.inForward=Math.max(-1,Math.min(1,p.forward()));d.inStrafe=Math.max(-1,Math.min(1,p.strafe()));d.inUp=p.up();d.inDown=p.down();d.inBoost=p.boost();d.inYaw=p.yaw();d.inPitch=p.pitch();d.inputAt=d.level().getGameTime();d.clientPosition(p.x(),p.y(),p.z());
  });
  r.playToServer(Action.TYPE,Action.CODEC,(p,c)->{
   if(!(c.player() instanceof ServerPlayer player))return;
   if(p.kind()==2){Drones.end(player);return;}   // exit works even if the drone is already gone
   var d=Drones.droneOf(player);if(d==null)return;
   if(p.kind()==1){if(d.courier())d.cargoAction(player);else if(d.hasBomb())d.dropBomb();else player.displayClientMessage(Component.literal("Sem bomba no drone."),true);}
   else if(p.kind()==2)Drones.end(player);
  });
  r.playToClient(Session.TYPE,Session.CODEC,(p,c)->sessionHandler.accept(p.entityId()));
 }
}
