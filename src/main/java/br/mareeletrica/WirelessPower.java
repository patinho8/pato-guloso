package br.mareeletrica;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
/** Thin attachments, one per machine face. The server transfers existing clean FE only. */
public final class WirelessPower {
 public static final int[] RATE={8_000,128_000,128_000},RANGE={32,128,64};
 public static final class Plate {public int tier;public Direction face;public Vec3 point;public BlockPos core;public Plate(int t,Direction f,Vec3 point,BlockPos core){tier=t;face=f;this.point=point;this.core=core;}}
 private static final Map<Level,Set<BlockPos>> LOADED=new WeakHashMap<>();
 private WirelessPower(){}
 public static boolean supported(MachineEntity m){return m.kind.core()||m.kind.consumer||m.kind==Kind.PURIFIER;}
 public static Vec3 socket(MachineEntity m,int tier){return MachinePanel.center(m.kind,m.getBlockState()).subtract(Vec3.atLowerCornerOf(m.getBlockState().getValue(MachineBlock.FACING).getNormal()).scale(.003)).add(Vec3.atLowerCornerOf(m.getBlockState().getValue(MachineBlock.FACING).getClockWise().getNormal()).scale((tier-1)*.12));}
 public static boolean onSocket(MachineEntity m,net.minecraft.core.Direction face,Vec3 hit,int tier){return face==m.getBlockState().getValue(MachineBlock.FACING)&&hit.subtract(Vec3.atLowerCornerOf(m.getBlockPos())).distanceTo(socket(m,tier))<.065;}
 public static boolean hasTier(MachineEntity m,int tier){return m.wireless.stream().anyMatch(p->p.tier==tier);}
 public static void register(MachineEntity m){if(m.getLevel()!=null&&!m.getLevel().isClientSide&&!m.wireless.isEmpty())LOADED.computeIfAbsent(m.getLevel(),l->new HashSet<>()).add(m.getBlockPos());}
 private static MachineEntity core(Level level,BlockPos pos,MachineEntity target,int tier){if(pos==null||!level.hasChunkAt(pos)||pos.distSqr(target.getBlockPos())>RANGE[tier]*RANGE[tier])return null;return level.getBlockEntity(pos) instanceof MachineEntity m&&m.kind.core()&&m.enabled&&hasTier(m,tier)?m:null;}
 private static MachineEntity nearest(MachineEntity target,int tier){return LOADED.getOrDefault(target.getLevel(),Set.of()).stream().map(pos->core(target.getLevel(),pos,target,tier)).filter(Objects::nonNull).min(Comparator.comparingDouble(m->m.getBlockPos().distSqr(target.getBlockPos()))).orElse(null);}
 public static InteractionResult attach(UseOnContext c,int tier){var player=c.getPlayer();var level=c.getLevel();var m=PotabilizerStructure.machineAt(level,c.getClickedPos());if(player==null||m==null||(!m.kind.core()&&!m.kind.consumer&&m.kind!=Kind.PURIFIER)||!player.mayBuild()||!level.mayInteract(player,c.getClickedPos()))return InteractionResult.PASS;if(!onSocket(m,c.getClickedFace(),c.getClickLocation(),tier)){if(!level.isClientSide)player.displayClientMessage(Component.literal("Instale o chip no encaixe marcado da base frontal."),true);return InteractionResult.FAIL;}if(level.isClientSide)return InteractionResult.SUCCESS;var held=c.getItemInHand();var tag=held.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
  if(player.isShiftKeyDown()&&m.kind.core()){tag.putLong("LinkedCore",m.getBlockPos().asLong());tag.putString("Dimension",level.dimension().location().toString());held.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));player.displayClientMessage(Component.literal("Placas vinculadas a este núcleo."),true);return InteractionResult.CONSUME;}
  if(player.isShiftKeyDown()){var found=m.wireless.stream().filter(p->p.tier==tier).findFirst().orElse(null);if(found!=null){m.wireless.remove(found);var returned=new ItemStack(MareEletrica.WIRELESS_PLATES.get(found.tier).get());if(!player.getInventory().add(returned))player.drop(returned,false);m.syncAim();}return InteractionResult.CONSUME;}
  if(m.wireless.stream().anyMatch(p->p.tier==tier)){player.displayClientMessage(Component.literal("Este encaixe já tem um chip. Shift + clique para retirar."),true);return InteractionResult.CONSUME;}
  BlockPos link=tag.contains("LinkedCore")&&tag.getString("Dimension").equals(level.dimension().location().toString())?BlockPos.of(tag.getLong("LinkedCore")):null;
  if(m.kind.core()){link=m.getBlockPos();tag.putLong("LinkedCore",link.asLong());tag.putString("Dimension",level.dimension().location().toString());held.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));}else if(link==null){var source=nearest(m,tier);if(source!=null)link=source.getBlockPos();}
  Vec3 relative=socket(m,tier);m.wireless.add(new Plate(tier,c.getClickedFace(),relative,link));register(m);m.syncAim();if(!player.isCreative())held.shrink(1);player.displayClientMessage(Component.literal(m.kind.core()?"Transmissor instalado. Instale outra placa igual na máquina.":link==null?"Receptor instalado; aguardando núcleo com placa igual.":"Receptor vinculado ao núcleo."),true);return InteractionResult.CONSUME;
 }
 public static int receive(MachineEntity target){if(target.getLevel()==null||!target.enabled||target.kind.core()||target.wireless.isEmpty())return 0;int total=0;Set<String> used=new HashSet<>();for(var plate:target.wireless){var source=core(target.getLevel(),plate.core,target,plate.tier);if(source==null&&plate.core==null){source=nearest(target,plate.tier);if(source!=null){plate.core=source.getBlockPos();target.syncAim();}}if(source==null||!used.add(source.getBlockPos()+":"+plate.tier))continue;long epoch=target.getLevel().getGameTime()/10;if(source.wirelessEpoch!=epoch){source.wirelessEpoch=epoch;Arrays.fill(source.wirelessUsed,0);}int budget=RATE[plate.tier]-source.wirelessUsed[plate.tier];int moved=source.clean.moveTo(target.clean,budget);if(moved>0)target.powered(Math.min(source.kind.voltage(),plate.tier+2));source.wirelessUsed[plate.tier]+=moved;total+=moved;if(moved>0)source.setChanged();}if(total>0)target.setChanged();return total;}
 public static void drop(MachineEntity m){if(m.getLevel()==null||m.getLevel().isClientSide)return;for(var p:m.wireless)Containers.dropItemStack(m.getLevel(),m.getBlockPos().getX()+.5,m.getBlockPos().getY()+.5,m.getBlockPos().getZ()+.5,new ItemStack(MareEletrica.WIRELESS_PLATES.get(p.tier).get()));m.wireless.clear();var set=LOADED.get(m.getLevel());if(set!=null)set.remove(m.getBlockPos());}
 public static ListTag save(MachineEntity m){var list=new ListTag();for(var p:m.wireless){var t=new CompoundTag();t.putInt("Tier",p.tier);t.putInt("Face",p.face.get3DDataValue());t.putDouble("X",p.point.x);t.putDouble("Y",p.point.y);t.putDouble("Z",p.point.z);if(p.core!=null)t.putLong("Core",p.core.asLong());list.add(t);}return list;}
 public static void load(MachineEntity m,ListTag list){m.wireless.clear();for(int i=0;i<Math.min(6,list.size());i++){var t=list.getCompound(i);int tier=t.getInt("Tier");if(tier>=0&&tier<3)m.wireless.add(new Plate(tier,Direction.from3DDataValue(t.getInt("Face")),new Vec3(t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z")),t.contains("Core")?BlockPos.of(t.getLong("Core")):null));}}
}
