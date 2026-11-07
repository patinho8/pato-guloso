package br.mareeletrica;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Fighter jet: the pilot's game sends attitude, throttle, velocity and trigger state every tick. */
@EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
public final class JetNet {
 private JetNet(){}
 public record Input(float qx,float qy,float qz,float qw,float throttle,float vx,float vy,float vz,boolean fire,boolean bomb,boolean flare,boolean eject) implements CustomPacketPayload {
  public static final Type<Input> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"jet_input"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Input> CODEC=StreamCodec.of(
   (b,p)->{b.writeFloat(p.qx);b.writeFloat(p.qy);b.writeFloat(p.qz);b.writeFloat(p.qw);b.writeFloat(p.throttle);b.writeFloat(p.vx);b.writeFloat(p.vy);b.writeFloat(p.vz);b.writeByte((p.fire?1:0)|(p.bomb?2:0)|(p.flare?4:0)|(p.eject?8:0));},
   b->{float x=b.readFloat(),y=b.readFloat(),z=b.readFloat(),w=b.readFloat(),t=b.readFloat(),vx=b.readFloat(),vy=b.readFloat(),vz=b.readFloat();int k=b.readByte();return new Input(x,y,z,w,t,vx,vy,vz,(k&1)!=0,(k&2)!=0,(k&4)!=0,(k&8)!=0);});
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
  e.registrar("10").playToServer(Input.TYPE,Input.CODEC,(p,c)->{
   if(!(c.player() instanceof ServerPlayer player)||!(player.getVehicle() instanceof Jets.JetEntity j))return;
   if(p.eject()){j.eject(player);return;}
   if(!Float.isFinite(p.vx())||!Float.isFinite(p.vy())||!Float.isFinite(p.vz())||!Float.isFinite(p.throttle()))return;
   j.input(player,p.qx(),p.qy(),p.qz(),p.qw(),p.throttle(),new Vec3(p.vx(),p.vy(),p.vz()),p.fire(),p.bomb(),p.flare());
  });
 }
}
