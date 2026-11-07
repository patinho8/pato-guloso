package br.mareeletrica;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class MachinePackets {
    private MachinePackets() {}
    public record Distance(int menu,int value) implements CustomPacketPayload {
        public static final Type<Distance> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"projection_distance"));
        public static final StreamCodec<ByteBuf,Distance> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Distance::menu,ByteBufCodecs.VAR_INT,Distance::value,Distance::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record AimControl(int action,int value) implements CustomPacketPayload {
        public static final Type<AimControl> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"aim_control"));
        public static final StreamCodec<ByteBuf,AimControl> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,AimControl::action,ByteBufCodecs.VAR_INT,AimControl::value,AimControl::new);
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("3").playToServer(AimControl.TYPE,AimControl.CODEC,(p,c)->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer s)AimRemoteItem.command(s,p.action(),p.value());});
        event.registrar("3").playToServer(Distance.TYPE,Distance.CODEC,(packet,ctx)->{
            var player=ctx.player();
            if(player.containerMenu instanceof ConstructionMenu menu&&menu.containerId==packet.menu())menu.distance(player,packet.value());
        });
    }
}
