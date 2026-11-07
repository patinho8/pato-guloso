package br.mareeletrica;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
public final class EditorPackets {
 public static Consumer<Snapshot> client=p->{};public static Consumer<Paint> clientPaint=p->{};
 public record Edit(int menu,int revision,int action,int a,int b,int c) implements CustomPacketPayload{
  public static final Type<Edit> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"editor_edit"));
  public static final StreamCodec<FriendlyByteBuf,Edit> CODEC=new StreamCodec<>(){public Edit decode(FriendlyByteBuf b){return new Edit(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt());}public void encode(FriendlyByteBuf b,Edit p){b.writeVarInt(p.menu);b.writeVarInt(p.revision);b.writeVarInt(p.action);b.writeVarInt(p.a);b.writeVarInt(p.b);b.writeVarInt(p.c);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Stroke(int menu,int revision,int color,int[] keys) implements CustomPacketPayload{
  public static final Type<Stroke> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"editor_stroke"));
  public static final StreamCodec<FriendlyByteBuf,Stroke> CODEC=new StreamCodec<>(){public Stroke decode(FriendlyByteBuf b){return new Stroke(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarIntArray(128));}public void encode(FriendlyByteBuf b,Stroke p){b.writeVarInt(p.menu);b.writeVarInt(p.revision);b.writeVarInt(p.color);b.writeVarIntArray(p.keys);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Paint(int menu,int revision,int color,int[] keys) implements CustomPacketPayload{
  public static final Type<Paint> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"editor_paint"));
  public static final StreamCodec<FriendlyByteBuf,Paint> CODEC=new StreamCodec<>(){public Paint decode(FriendlyByteBuf b){return new Paint(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarIntArray(128));}public void encode(FriendlyByteBuf b,Paint p){b.writeVarInt(p.menu);b.writeVarInt(p.revision);b.writeVarInt(p.color);b.writeVarIntArray(p.keys);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Snapshot(int menu,int revision,int width,int height,int depth,int[] cells) implements CustomPacketPayload{
  public static final Type<Snapshot> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"editor_view"));
  public static final StreamCodec<FriendlyByteBuf,Snapshot> CODEC=new StreamCodec<>(){public Snapshot decode(FriendlyByteBuf b){return new Snapshot(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarIntArray(VoxelRules.MAX_CELLS*3));}public void encode(FriendlyByteBuf b,Snapshot p){b.writeVarInt(p.menu);b.writeVarInt(p.revision);b.writeVarInt(p.width);b.writeVarInt(p.height);b.writeVarInt(p.depth);b.writeVarIntArray(p.cells);}};
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static void send(ServerPlayer player,ConstructionMenu menu,MachineEntity m){var p=m.editor.plan(m);int[] a=new int[p.cells().size()*3];int i=0;for(var c:p.cells()){a[i++]=VoxelRules.key(c.pos().getX(),c.pos().getY(),c.pos().getZ());a[i++]=Block.getId(c.original());a[i++]=m.editor.color(c);}PacketDistributor.sendToPlayer(player,new Snapshot(menu.containerId,m.editor.revision,p.width(),p.height(),p.depth(),a));}
 public static void register(RegisterPayloadHandlersEvent e){var r=e.registrar("2");r.playToClient(Paint.TYPE,Paint.CODEC,(p,c)->clientPaint.accept(p));r.playToServer(Stroke.TYPE,Stroke.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer s&&s.containerMenu instanceof ConstructionMenu m&&m.containerId==p.menu&&m.stillValid(s)&&s.mayBuild()&&!s.isSpectator())m.stroke(s,p);});r.playToClient(Snapshot.TYPE,Snapshot.CODEC,(p,c)->client.accept(p));r.playToServer(Edit.TYPE,Edit.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer s&&s.containerMenu instanceof ConstructionMenu m&&m.containerId==p.menu&&m.stillValid(s)&&!s.isSpectator())m.edit(s,p);});}
}
