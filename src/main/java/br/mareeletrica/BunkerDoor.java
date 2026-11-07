package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Armoured bunker door: ignores redstone and hands. Opens for 4 seconds with the right code (keypad) or a programmed access card.
 *  The first code typed becomes the password and the one who typed it the owner. Shift + right-click with an empty hand (owner) clears the password. */
public final class BunkerDoor {
 public static final DeferredBlock<DoorBlockImpl> BLOCK=MareEletrica.BLOCKS.register("bunker_door",()->new DoorBlockImpl(BlockBehaviour.Properties.of().strength(25F,1200F).sound(SoundType.NETHERITE_BLOCK).noOcclusion().requiresCorrectToolForDrops()));
 public static final DeferredItem<DoubleHighBlockItem> ITEM=MareEletrica.ITEMS.register("bunker_door",()->new DoubleHighBlockItem(BLOCK.get(),new Item.Properties()));
 public static final DeferredItem<CardItem> CARD=MareEletrica.ITEMS.register("access_card",()->new CardItem(new Item.Properties().stacksTo(1)));
 public static final Supplier<BlockEntityType<Lock>> ENTITY=MareEletrica.ENTITIES.register("bunker_door",()->BlockEntityType.Builder.of(Lock::new,BLOCK.get()).build(null));
 /** Client hook: opens the keypad for the door at this (lower) position. */
 public static Consumer<BlockPos> openKeypad=p->{};
 private BunkerDoor(){}
 public static void register(){}
 static BlockPos lower(BlockState s,BlockPos p){return s.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?p.below():p;}
 static Lock lock(Level l,BlockPos p){var s=l.getBlockState(p);if(!(s.getBlock() instanceof DoorBlockImpl))return null;return l.getBlockEntity(lower(s,p)) instanceof Lock k?k:null;}
 static String cardCode(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?"":c.copyTag().getString("DoorCode");}

 public static void open(Level l,BlockPos any){
  var s=l.getBlockState(any);if(!(s.getBlock() instanceof DoorBlockImpl d))return;var low=lower(s,any);var st=l.getBlockState(low);
  if(!st.getValue(DoorBlock.OPEN))d.setOpen(null,l,st,low,true);
  l.scheduleTick(low,d,80);
  l.playSound(null,low,SoundEvents.NOTE_BLOCK_CHIME.value(),SoundSource.BLOCKS,1F,1.6F);
 }
 static void tryCode(ServerPlayer p,BlockPos pos,String code){
  if(p.distanceToSqr(pos.getCenter())>64||!code.matches("\\d{4,8}"))return;
  var k=lock(p.level(),pos);if(k==null)return;
  if(k.code.isEmpty()){k.code=code;k.owner=p.getUUID();k.setChanged();p.displayClientMessage(Component.literal("§aSenha definida. Você é o dono desta porta."),true);open(p.level(),pos);return;}
  if(k.code.equals(code)){open(p.level(),pos);p.displayClientMessage(Component.literal("§aAcesso liberado"),true);}
  else{p.level().playSound(null,pos,SoundEvents.NOTE_BLOCK_BASS.value(),SoundSource.BLOCKS,1F,.5F);p.displayClientMessage(Component.literal("§cSenha errada"),true);}
 }

 public static final class DoorBlockImpl extends DoorBlock implements EntityBlock {
  public static final MapCodec<DoorBlockImpl> CODEC=simpleCodec(DoorBlockImpl::new);
  public DoorBlockImpl(BlockBehaviour.Properties p){super(BlockSetType.IRON,p);}
  @Override public MapCodec<? extends DoorBlock> codec(){return CODEC;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.LOWER?new Lock(p,s):null;}
  @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos from,boolean moving){}   // armoured: redstone does nothing
  @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(s.getValue(OPEN))setOpen(null,l,s,p,false);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   var low=lower(s,p);
   if(player.isShiftKeyDown()){
    if(!l.isClientSide&&l.getBlockEntity(low) instanceof Lock k){
     if(k.owner==null||k.owner.equals(player.getUUID())||player.isCreative()){k.code="";k.owner=null;k.setChanged();player.displayClientMessage(Component.literal("Senha apagada: a próxima senha digitada vira a nova."),true);}
     else player.displayClientMessage(Component.literal("§cSó o dono pode apagar a senha."),true);
    }
    return InteractionResult.sidedSuccess(l.isClientSide);
   }
   if(l.isClientSide)openKeypad.accept(low);
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
   if(!stack.is(CARD.get()))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   if(l.isClientSide)return ItemInteractionResult.SUCCESS;
   var k=lock(l,p);var code=cardCode(stack);
   if(k!=null&&!k.code.isEmpty()&&k.code.equals(code)){open(l,p);player.displayClientMessage(Component.literal("§aCartão aceito"),true);}
   else{l.playSound(null,p,SoundEvents.NOTE_BLOCK_BASS.value(),SoundSource.BLOCKS,1F,.5F);player.displayClientMessage(Component.literal(code.isEmpty()?"§cCartão em branco — Shift + clique direito na porta para gravar":"§cCartão recusado"),true);}
   return ItemInteractionResult.CONSUME;
  }
 }

 public static final class Lock extends BlockEntity {
  public String code="";public UUID owner;
  public Lock(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putString("Code",code);if(owner!=null)t.putUUID("Owner",owner);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);code=t.getString("Code");owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;}
 }

 /** Access card: Shift + right-click a bunker door to copy its code (the owner's door, or a door with no owner). */
 public static final class CardItem extends Item {
  public CardItem(Properties p){super(p);}
  @Override public InteractionResult useOn(UseOnContext c){
   var l=c.getLevel();var p=c.getPlayer();if(p==null||!p.isShiftKeyDown())return InteractionResult.PASS;
   var k=lock(l,c.getClickedPos());if(k==null)return InteractionResult.PASS;
   if(l.isClientSide)return InteractionResult.SUCCESS;
   if(k.code.isEmpty()){p.displayClientMessage(Component.literal("§eDefina a senha da porta primeiro (clique nela)."),true);return InteractionResult.CONSUME;}
   if(k.owner!=null&&!k.owner.equals(p.getUUID())&&!p.isCreative()){p.displayClientMessage(Component.literal("§cSó o dono da porta pode gravar cartões."),true);return InteractionResult.CONSUME;}
   var t=new CompoundTag();t.putString("DoorCode",k.code);c.getItemInHand().set(DataComponents.CUSTOM_DATA,CustomData.of(t));
   p.displayClientMessage(Component.literal("§aCartão gravado para esta porta."),true);l.playSound(null,c.getClickedPos(),SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.BLOCKS,.8F,1.6F);
   return InteractionResult.CONSUME;
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<Component> t,TooltipFlag f){t.add(Component.literal(cardCode(s).isEmpty()?"Em branco • Shift + clique direito numa porta blindada para gravar":"Gravado • clique na porta para abrir"));}
  @Override public boolean isFoil(ItemStack s){return !cardCode(s).isEmpty();}
 }

 public record Code(BlockPos pos,String code) implements CustomPacketPayload {
  public static final Type<Code> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"door_code"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Code> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Code::pos,ByteBufCodecs.stringUtf8(8),Code::code,Code::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){e.registrar("14").playToServer(Code.TYPE,Code.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer sp)tryCode(sp,p.pos(),p.code());});}
 }
}
