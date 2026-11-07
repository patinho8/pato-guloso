package br.mareeletrica;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
public final class RemoteControlItem extends Item {
 public RemoteControlItem(Properties properties){super(properties);}
 @Override public InteractionResult useOn(UseOnContext c){
  BlockPos pos=c.getClickedPos();var level=c.getLevel();var state=level.getBlockState(pos);
  if(state.getBlock() instanceof LargeMachinePartBlock)pos=LargeMachineStructure.root(pos,state);
  if(!(level.getBlockEntity(pos) instanceof MachineEntity base)||base.kind!=Kind.ROVER_BASE)return InteractionResult.PASS;
  if(!level.isClientSide&&c.getPlayer()!=null){
   CompoundTag tag=c.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
   tag.putLong("Dock",pos.asLong());tag.putString("Dimension",level.dimension().location().toString());tag.putBoolean("Bound",true);
   c.getItemInHand().set(DataComponents.CUSTOM_DATA,CustomData.of(tag));c.getPlayer().displayClientMessage(Component.translatable("remote.mareeletrica.bound",pos.getX(),pos.getY(),pos.getZ()),true);
  }
  return InteractionResult.sidedSuccess(level.isClientSide);
 }
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
  var stack=player.getItemInHand(hand);
  if(player instanceof ServerPlayer server){if(RemoteSessions.active(server))RemoteSessions.stop(server,"remote.mareeletrica.ended");else RemoteSessions.start(server,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag());}
  return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
 }
 @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
  super.appendHoverText(stack,context,lines,flag);lines.add(Component.translatable("hint.mareeletrica.rover_remote"));
  var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(tag.getBoolean("Bound")){var p=BlockPos.of(tag.getLong("Dock"));lines.add(Component.literal(p.getX()+", "+p.getY()+", "+p.getZ()));}
 }
}
