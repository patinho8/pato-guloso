package br.mareeletrica;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.70.0: gun painting. A Paint Bench lets you colour each part of the gun you hold (body, barrel, stock, bolt, magazine, sight)
 *  with any colour, a finish (matte, metallic, glowing, rainbow), a two-colour pattern (stripes, dots, camo) and a sticker.
 *  Sprays are spent per change; the solvent cleans a part. The paint lives in the gun's CUSTOM_DATA ("Paint"). */
public final class GunPaint {
 private GunPaint(){}
 public static final String[] PARTS={"Corpo","Cano","Coronha","Ferrolho","Carregador","Mira"};
 public static final String[] FINISH={"Fosco","Metálico","Brilhante","Arco-íris"};
 public static final String[] PATTERN={"Liso","Listras","Bolinhas","Camuflado"};
 public static final String[] STICKERS={"Nenhum","Caveira","Pato","Raio","Estrela","Chama","Coração"};
 public static final String[] STICKER_TEX={"","skull","duck","bolt","star","flame","heart"};

 static class Spray extends Item {
  final String tip;
  Spray(int uses,String tip){super(new Item.Properties().durability(uses));this.tip=tip;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7"+tip));t.add(Component.literal("§8Cargas: "+(s.getMaxDamage()-s.getDamageValue())+"/"+s.getMaxDamage()));}
  @Override public boolean isBarVisible(ItemStack s){return s.getDamageValue()>0;}
 }
 public static final DeferredItem<Item> SPRAY=MareEletrica.ITEMS.register("spray_paint",()->new Spray(32,"Tinta de qualquer cor. Use na Bancada de Pintura segurando uma arma."));
 public static final DeferredItem<Item> SPRAY_METAL=MareEletrica.ITEMS.register("spray_metallic",()->new Spray(16,"Acabamento metálico cromado."));
 public static final DeferredItem<Item> SPRAY_GLOW=MareEletrica.ITEMS.register("spray_glow",()->new Spray(16,"Acabamento que brilha no escuro."));
 public static final DeferredItem<Item> SPRAY_RAINBOW=MareEletrica.ITEMS.register("spray_rainbow",()->new Spray(16,"Acabamento arco-íris: a cor muda sozinha."));
 public static final DeferredItem<Item> SOLVENT=MareEletrica.ITEMS.register("paint_solvent",()->new Spray(16,"Remove a pintura de uma peça (ou o adesivo)."));

 public static Consumer<BlockPos> openBench=p->{};
 public static final class BenchBlock extends Furniture.ShapeBlock {
  public BenchBlock(BlockBehaviour.Properties p){super(p,new double[][]{{0,0,0,16,14,16},{1,14,13.5,15,24,16}});}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   if(WeaponFamily.of(p.getMainHandItem())==null){if(!l.isClientSide)p.displayClientMessage(Component.literal("§e🎨 Segure uma arma na mão principal para pintar."),true);return InteractionResult.SUCCESS;}
   if(l.isClientSide)openBench.accept(pos);
   return InteractionResult.SUCCESS;}
 }
 public static final DeferredBlock<BenchBlock> BENCH=MareEletrica.BLOCKS.register("paint_bench",()->new BenchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2F).sound(SoundType.WOOD).noOcclusion()));
 public static final DeferredItem<BlockItem> BENCH_ITEM=MareEletrica.ITEMS.register("paint_bench",()->new BlockItem(BENCH.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("§7Segure uma arma e clique para pintar cada peça:"));t.add(Component.literal("§7qualquer cor, acabamento, padrão e adesivo."));}});
 public static void creative(Consumer<ItemStack> out){for(var i:List.of(BENCH_ITEM,SPRAY,SPRAY_METAL,SPRAY_GLOW,SPRAY_RAINBOW,SOLVENT))out.accept(new ItemStack(i.get()));}
 public static void register(){}

 // ---------- data ----------
 public static CompoundTag paint(ItemStack s){return RifleLoadout.tag(s).getCompound("Paint");}
 public static boolean painted(ItemStack s){var p=paint(s);return !p.isEmpty();}
 public static void set(ItemStack s,CompoundTag p){CustomData.update(DataComponents.CUSTOM_DATA,s,t->{if(p.isEmpty())t.remove("Paint");else t.put("Paint",p);});}
 /** keeps only known keys with sane values */
 public static CompoundTag clean(CompoundTag in){
  var o=new CompoundTag();
  for(int i=0;i<PARTS.length;i++){var k="r"+i;if(!in.contains(k))continue;var r=in.getCompound(k);var c=new CompoundTag();
   c.putInt("C",r.getInt("C")&0xFFFFFF);c.putInt("F",Math.clamp(r.getInt("F"),0,3));c.putInt("P",Math.clamp(r.getInt("P"),0,3));c.putInt("D",r.getInt("D")&0xFFFFFF);o.put(k,c);}
  int st=Math.clamp(in.getInt("S"),0,STICKERS.length-1);if(st>0)o.putInt("S",st);
  return o;}
 /** sprays needed to go from a to b: [paint, metallic, glow, rainbow, solvent] */
 public static int[] cost(CompoundTag a,CompoundTag b){
  int[] c=new int[5];
  for(int i=0;i<PARTS.length;i++){var k="r"+i;boolean had=a.contains(k),has=b.contains(k);
   if(!has){if(had)c[4]++;continue;}
   var n=b.getCompound(k);if(had&&a.getCompound(k).equals(n))continue;
   c[0]++;if(n.getInt("P")>0)c[0]++;int f=n.getInt("F");if(f>0)c[f]++;}
  int sa=a.getInt("S"),sb=b.getInt("S");if(sa!=sb){if(sb>0)c[0]++;else c[4]++;}
  return c;}
 public static Item[] SPRAYS(){return new Item[]{SPRAY.get(),SPRAY_METAL.get(),SPRAY_GLOW.get(),SPRAY_RAINBOW.get(),SOLVENT.get()};}
 public static int[] charges(Player p){var it=SPRAYS();int[] c=new int[5];for(var s:p.getInventory().items)for(int i=0;i<5;i++)if(s.is(it[i]))c[i]+=s.getMaxDamage()-s.getDamageValue();return c;}
 static void spend(Player p,Item item,int n){for(var s:p.getInventory().items){if(n<=0)return;if(!s.is(item))continue;int left=s.getMaxDamage()-s.getDamageValue(),use=Math.min(left,n);n-=use;if(use>=left)s.shrink(1);else s.setDamageValue(s.getDamageValue()+use);}}

 public record Apply(BlockPos pos,CompoundTag paint) implements CustomPacketPayload {
  public static final Type<Apply> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"gun_paint"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Apply> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Apply::pos,ByteBufCodecs.COMPOUND_TAG,Apply::paint,Apply::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 static final String[] CAN_NAMES={"Spray de Tinta","Spray Metálico","Spray Brilhante","Spray Arco-íris","Solvente"};
 static void apply(ServerPlayer sp,Apply p){
  if(sp.distanceToSqr(p.pos().getCenter())>64||!sp.level().getBlockState(p.pos()).is(BENCH.get()))return;
  var gun=sp.getMainHandItem();if(WeaponFamily.of(gun)==null)return;
  var now=paint(gun);var next=clean(p.paint());int[] cost=cost(now,next);
  if(!sp.getAbilities().instabuild){int[] have=charges(sp);
   for(int i=0;i<5;i++)if(cost[i]>have[i]){sp.displayClientMessage(Component.literal("§cFalta "+CAN_NAMES[i]+": precisa de "+cost[i]+", tem "+have[i]+"."),true);return;}
   var it=SPRAYS();for(int i=0;i<5;i++)if(cost[i]>0)spend(sp,it[i],cost[i]);}
  set(gun,next);
  sp.level().playSound(null,p.pos(),SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.6F,1.7F);
  sp.displayClientMessage(Component.literal("§a🎨 Arma pintada!"),true);
 }
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
   e.registrar("21").playToServer(Apply.TYPE,Apply.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer sp)apply(sp,p);});
  }
 }
}
