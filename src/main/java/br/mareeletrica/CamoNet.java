package br.mareeletrica;

import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
public final class CamoNet {
 public static Consumer<Sync> client=p->{};
 public record Sync(int entity,ItemStack head,ItemStack chest,ItemStack legs,ItemStack feet) implements CustomPacketPayload {
  public static final Type<Sync> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"camo_equipment"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Sync> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Sync::entity,ItemStack.OPTIONAL_STREAM_CODEC,Sync::head,ItemStack.OPTIONAL_STREAM_CODEC,Sync::chest,ItemStack.OPTIONAL_STREAM_CODEC,Sync::legs,ItemStack.OPTIONAL_STREAM_CODEC,Sync::feet,Sync::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){e.registrar("3").playToClient(Sync.TYPE,Sync.CODEC,(p,c)->client.accept(p));}
}
