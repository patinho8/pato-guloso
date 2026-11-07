package br.mareeletrica;

import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class GunPackets {
 public static Consumer<Shot> clientShot=p->{};public static Consumer<Prone> clientProne=p->{};
 public record Command(int action) implements CustomPacketPayload {
  public static final Type<Command> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"gun_command"));
  public static final StreamCodec<FriendlyByteBuf,Command> CODEC=new StreamCodec<>(){public Command decode(FriendlyByteBuf b){return new Command(b.readVarInt());}public void encode(FriendlyByteBuf b,Command p){b.writeVarInt(p.action);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Shot(boolean sniper,float pitch,float yaw) implements CustomPacketPayload {
  public static final Type<Shot> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"gun_shot"));
  public static final StreamCodec<FriendlyByteBuf,Shot> CODEC=new StreamCodec<>(){public Shot decode(FriendlyByteBuf b){return new Shot(b.readBoolean(),b.readFloat(),b.readFloat());}public void encode(FriendlyByteBuf b,Shot p){b.writeBoolean(p.sniper);b.writeFloat(p.pitch);b.writeFloat(p.yaw);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Prone(int entity,boolean active) implements CustomPacketPayload {
  public static final Type<Prone> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"prone"));
  public static final StreamCodec<FriendlyByteBuf,Prone> CODEC=new StreamCodec<>(){public Prone decode(FriendlyByteBuf b){return new Prone(b.readVarInt(),b.readBoolean());}public void encode(FriendlyByteBuf b,Prone p){b.writeVarInt(p.entity);b.writeBoolean(p.active);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("1");r.playToServer(Command.TYPE,Command.CODEC,(p,c)->{if(!(c.player() instanceof ServerPlayer s))return;if((p.action==0||p.action==3||p.action==5||p.action==9)&&!Wounds.canFire(s)){if(p.action==5)SmgItem.trigger(s,false);if(p.action==9)GunItem.trigger(s,false);return;}switch(p.action){case 0->SniperItem.fire(s);case 1->{if(s.getMainHandItem().getItem() instanceof GunItem)GunItem.open(s);else if(s.getMainHandItem().is(MareEletrica.SMG.get()))SmgItem.open(s);else SniperItem.open(s);}case 2->ProneSystem.toggle(s);case 3->RevolverItem.fire(s);case 4->{if(s.getMainHandItem().getItem() instanceof GunItem)GunItem.reload(s);else if(s.getMainHandItem().is(MareEletrica.SMG.get()))SmgItem.reload(s);else Magazines.reload(s);}case 5->SmgItem.trigger(s,true);case 6->SmgItem.trigger(s,false);case 7->ScopeSight.request(s);case 8->ScopeSight.clear(s,true);case 9->GunItem.trigger(s,true);case 10->GunItem.trigger(s,false);}});r.playToClient(Shot.TYPE,Shot.CODEC,(p,c)->clientShot.accept(p));r.playToClient(Prone.TYPE,Prone.CODEC,(p,c)->clientProne.accept(p));}
}
