package br.mareeletrica;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Controller screen: the server sends a snapshot; the client sends single edits and gets a fresh snapshot back. */
@EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
public final class ControlNet {
 public static BiConsumer<BlockPos,CompoundTag> open=(p,t)->{};
 private ControlNet(){}
 public static void register(){}
 public record Open(BlockPos pos,CompoundTag data) implements CustomPacketPayload {
  public static final Type<Open> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"controller_open"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Open> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Open::pos,ByteBufCodecs.COMPOUND_TAG,Open::data,Open::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 /** action: 0 limit, 1 seconds on, 2 seconds off, 3 toggle device, 4 device share, 5 remove device, 6 refresh. */
 public record Edit(BlockPos pos,int action,int index,int value) implements CustomPacketPayload {
  public static final Type<Edit> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"controller_edit"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Edit> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Edit::pos,ByteBufCodecs.VAR_INT,Edit::action,ByteBufCodecs.VAR_INT,Edit::index,ByteBufCodecs.VAR_INT,Edit::value,Edit::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
  var r=e.registrar("9");
  r.playToClient(Open.TYPE,Open.CODEC,(p,c)->open.accept(p.pos(),p.data()));
  r.playToServer(Edit.TYPE,Edit.CODEC,(p,c)->{
   if(!(c.player() instanceof ServerPlayer player))return;
   if(player.distanceToSqr(p.pos().getCenter())>100)return;
   if(!(player.level().getBlockEntity(p.pos()) instanceof MachineEntity m)||m.kind!=Kind.POWER_CONTROLLER)return;
   boolean dev=p.index()>=0&&p.index()<m.links.size();
   switch(p.action()){
    case 0->m.ctrlLimit=Mth.clamp(p.value(),5,100);
    case 1->m.ctrlOn=Mth.clamp(p.value(),0,3600);
    case 2->m.ctrlOff=Mth.clamp(p.value(),0,3600);
    case 3->{if(dev)m.links.get(p.index()).on=!m.links.get(p.index()).on;}
    case 4->{if(dev)m.links.get(p.index()).share=Mth.clamp(p.value(),0,100);}
    case 5->{if(dev){var removed=m.links.remove(p.index());var t=PotabilizerStructure.machineAt(player.level(),removed.pos);if(t!=null&&!t.enabled){t.enabled=true;t.setChanged();}}}
    default->{}
   }
   m.setChanged();ControlLinks.open(player,m);
  });
 }
}
