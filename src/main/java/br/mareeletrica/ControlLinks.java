package br.mareeletrica;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredItem;

/** Power-controller links: devices recorded with a pendrive, each with its own on/off switch and energy share. */
public final class ControlLinks {
 public static final int MAX=8,RANGE=64;
 public static final DeferredItem<PendriveItem> PENDRIVE=MareEletrica.ITEMS.register("pendrive",()->new PendriveItem(new Item.Properties().stacksTo(1)));
 private ControlLinks(){}
 public static void register(){}

 public static final class Link {
  public final BlockPos pos;public boolean on=true;public int share=100;
  public Link(BlockPos pos){this.pos=pos.immutable();}
  CompoundTag save(){var t=new CompoundTag();t.putLong("Pos",pos.asLong());t.putBoolean("On",on);t.putInt("Share",share);return t;}
  static Link load(CompoundTag t){var l=new Link(BlockPos.of(t.getLong("Pos")));l.on=!t.contains("On")||t.getBoolean("On");l.share=t.contains("Share")?t.getInt("Share"):100;return l;}
 }
 public static ListTag save(List<Link> links){var l=new ListTag();for(var k:links)l.add(k.save());return l;}
 public static void load(List<Link> links,ListTag tag){links.clear();for(int i=0;i<tag.size()&&i<MAX;i++)links.add(Link.load(tag.getCompound(i)));}

 /** Everything the controller screen needs, as one tag. */
 public static CompoundTag snapshot(MachineEntity c){
  var t=new CompoundTag();t.putInt("Limit",c.ctrlLimit);t.putInt("On",c.ctrlOn);t.putInt("Off",c.ctrlOff);
  t.putInt("Energy",c.clean.stored());t.putInt("Capacity",c.clean.capacity());
  var list=new ListTag();
  for(var k:c.links){
   var e=new CompoundTag();e.putLong("Pos",k.pos.asLong());e.putBoolean("On",k.on);e.putInt("Share",k.share);
   var level=c.getLevel();boolean loaded=level!=null&&level.hasChunkAt(k.pos);
   var state=loaded?level.getBlockState(k.pos):null;
   e.putString("Name",state==null?"":state.getBlock().getDescriptionId());
   e.putBoolean("Camera",state!=null&&state.getBlock() instanceof Cameras.CameraBlock);
   if(state!=null&&state.getBlock() instanceof Cameras.CameraBlock)e.putInt("Facing",state.getValue(Cameras.CameraBlock.FACING).get3DDataValue());
   var m=loaded?PotabilizerStructure.machineAt(level,k.pos):null;
   e.putBoolean("Missing",state==null||state.isAir());
   if(m!=null){e.putInt("Charge",m.clean.capacity()>0?m.clean.stored()*100/m.clean.capacity():0);e.putBoolean("Machine",true);}
   list.add(e);
  }
  t.put("Links",list);return t;
 }
 public static void open(ServerPlayer player,MachineEntity c){PacketDistributor.sendToPlayer(player,new ControlNet.Open(c.getBlockPos(),snapshot(c)));}

 /** Controller tick (every 10 game ticks): cycle, per-device switch, energy shared out by percentage, leftovers to automatic crankers on the cables. */
 public static boolean tick(MachineEntity c){
  boolean cycleOn=ControlMachines.poweredNow(c);
  int budget=Math.max(0,ControlMachines.WINDOW_MAX*Math.max(0,Math.min(100,c.ctrlLimit))/100);
  int shares=0;
  for(var k:c.links)if(k.on)shares+=Math.max(0,k.share);
  int sent=0;var level=c.getLevel();
  for(var k:c.links){
   if(!level.hasChunkAt(k.pos))continue;
   var m=PotabilizerStructure.machineAt(level,k.pos);if(m==null||m==c)continue;
   boolean want=k.on&&cycleOn;
   if(m.kind.consumer&&m.enabled!=want){m.enabled=want;m.setChanged();}
   if(want&&m.kind.consumer&&shares>0&&k.share>0){int part=budget*k.share/shares;sent+=c.clean.moveTo(m.clean,Math.max(0,part));m.setChanged();}
  }
  if(cycleOn)sent+=PowerNetwork.send(c,false,Math.max(0,budget-sent),kd->kd==Kind.AUTO_CRANK);
  c.lastRate=sent*2;c.status=!cycleOn?57:sent>0?1:(c.clean.stored()>0?10:2);
  return cycleOn&&sent>0;
 }

 public static final class PendriveItem extends Item {
  public PendriveItem(Properties p){super(p);}
  static BlockPos saved(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);if(d==null)return null;var t=d.copyTag();return t.contains("Link")?BlockPos.of(t.getLong("Link")):null;}
  @Override public InteractionResult useOn(UseOnContext c){
   var level=c.getLevel();var player=c.getPlayer();var pos=c.getClickedPos();var stack=c.getItemInHand();
   if(level.isClientSide)return InteractionResult.SUCCESS;
   var state=level.getBlockState(pos);
   var target=PotabilizerStructure.machineAt(level,pos);
   BlockPos record=target!=null?target.getBlockPos():state.getBlock() instanceof Cameras.CameraBlock?pos:null;
   if(record==null){if(player!=null)player.displayClientMessage(Component.literal("O pen drive só grava aparelhos do mod e câmeras."),true);return InteractionResult.FAIL;}
   var t=new CompoundTag();t.putLong("Link",record.asLong());t.putString("Name",level.getBlockState(record).getBlock().getDescriptionId());
   stack.set(DataComponents.CUSTOM_DATA,CustomData.of(t));
   level.playSound(null,pos,SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.BLOCKS,.6F,1.8F);
   if(player!=null)player.displayClientMessage(Component.translatable(level.getBlockState(record).getBlock().getDescriptionId()).append(Component.literal(" gravado no pen drive. Agora clique no Bloco de Controle com ele.")),true);
   return InteractionResult.CONSUME;
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> tips,TooltipFlag f){
   var p=saved(s);
   tips.add(p==null?Component.literal("Vazio. Shift + clique direito num aparelho ou câmera para gravar."):Component.literal("Gravado: ").append(Component.translatable(s.get(DataComponents.CUSTOM_DATA).copyTag().getString("Name"))).append(Component.literal(" ("+p.getX()+", "+p.getY()+", "+p.getZ()+")")));
   tips.add(Component.literal("Clique direito no Bloco de Controle para conectar."));
  }
 }
 /** Right-click on the controller holding a filled pendrive. */
 public static void link(ServerPlayer player,MachineEntity c,ItemStack pendrive){
  var p=PendriveItem.saved(pendrive);
  if(p==null){player.displayClientMessage(Component.literal("Pen drive vazio: Shift + clique direito num aparelho ou câmera primeiro."),true);return;}
  if(p.equals(c.getBlockPos())){player.displayClientMessage(Component.literal("Esse é o próprio Bloco de Controle."),true);return;}
  if(p.distSqr(c.getBlockPos())>RANGE*RANGE){player.displayClientMessage(Component.literal("Longe demais (máximo "+RANGE+" blocos)."),true);return;}
  for(var k:c.links)if(k.pos.equals(p)){player.displayClientMessage(Component.literal("Esse aparelho já está conectado."),true);open(player,c);return;}
  if(c.links.size()>=MAX){player.displayClientMessage(Component.literal("Limite de "+MAX+" aparelhos por Bloco de Controle."),true);return;}
  c.links.add(new Link(p));c.setChanged();
  c.getLevel().playSound(null,c.getBlockPos(),SoundEvents.BEACON_ACTIVATE,SoundSource.BLOCKS,.5F,1.8F);
  open(player,c);
 }
}
