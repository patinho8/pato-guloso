package br.mareeletrica;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.65.4: support radio.
 *  Right-click: a supply crate comes down by parachute where you look (a chest of meds, ammo and food) — every 2 minutes.
 *  Crouch + right-click: air strike where you look — red smoke marks the spot, 5 s later 8 bombs fall on it — every 3 minutes. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class Support {
 public static final int SUPPLY_CD=2400,STRIKE_CD=3600,STRIKE_DELAY=100;
 public static final DeferredItem<Item> RADIO=MareEletrica.ITEMS.register("support_radio",()->new RadioItem(new Item.Properties().stacksTo(1)));
 public static final Supplier<EntityType<Crate>> CRATE=MareEletrica.ACTORS.register("supply_crate",()->EntityType.Builder.<Crate>of(Crate::new,MobCategory.MISC).sized(.9F,.9F).clientTrackingRange(16).build(MareEletrica.ID+":supply_crate"));
 private Support(){}
 public static void register(){}

 public static final class RadioItem extends Item {
  public RadioItem(Properties p){super(p);}
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
   var stack=p.getItemInHand(hand);
   if(level.isClientSide)return InteractionResultHolder.success(stack);
   var hit=level.clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(160)),ClipContext.Block.OUTLINE,ClipContext.Fluid.ANY,p));
   if(hit.getType()==HitResult.Type.MISS){p.displayClientMessage(Component.literal("§7Mire no chão (até 160 blocos)."),true);return InteractionResultHolder.fail(stack);}
   Vec3 at=hit.getLocation();var sl=(ServerLevel)level;long now=level.getGameTime();var data=p.getPersistentData();
   boolean strike=p.isShiftKeyDown();String key=strike?"MareStrikeAt":"MareSupplyAt";long ready=data.getLong(key);
   if(now<ready&&!p.isCreative()){p.displayClientMessage(Component.literal("§c📻 Base: aguarde "+(ready-now)/20+" s para pedir de novo."),true);return InteractionResultHolder.fail(stack);}
   data.putLong(key,now+(strike?STRIKE_CD:SUPPLY_CD));
   level.playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.PLAYERS,1F,.6F);
   if(strike){
    STRIKES.add(new Strike(sl,at,now+STRIKE_DELAY,p.getUUID()));
    p.displayClientMessage(Component.literal("§c§l📻 ATAQUE AÉREO confirmado! §7Saia de perto da fumaça vermelha: 5 segundos."),false);
   }else{
    var c=new Crate(CRATE.get(),level);double top=Math.min(level.getMaxBuildHeight()-2,Math.max(at.y+60,level.getHeight(Heightmap.Types.MOTION_BLOCKING,(int)Math.floor(at.x),(int)Math.floor(at.z))+50));
    c.setPos(at.x,top,at.z);level.addFreshEntity(c);
    p.displayClientMessage(Component.literal("§a📻 Suprimentos a caminho! §7Olhe para o céu."),false);
   }
   return InteractionResultHolder.consume(stack);
  }
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Botão direito no chão: caixa de suprimentos de paraquedas §8(a cada 2 min)"));
   t.add(Component.literal("§7Shift + botão direito: ataque aéreo onde você mira §8(a cada 3 min)"));}
 }

 // ---------------- air strike ----------------
 record Strike(ServerLevel level,Vec3 at,long time,java.util.UUID caller){}
 static final List<Strike> STRIKES=new ArrayList<>();
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){
  if(STRIKES.isEmpty())return;
  for(var it=STRIKES.iterator();it.hasNext();){
   var s=it.next();var l=s.level();long now=l.getGameTime();var r=l.random;
   if(now<s.time()){   // red smoke column marks the target, a whistle as the bombs come in
    if(now%2==0)l.sendParticles(new DustParticleOptions(new org.joml.Vector3f(1,.1F,.1F),2.5F),s.at().x,s.at().y+.3,s.at().z,6,.25,1.2,.25,0);
    if(now%3==0)l.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,s.at().x,s.at().y+.3,s.at().z,1,.1,.1,.1,.01);
    if(s.time()-now==40)l.playSound(null,BlockPos.containing(s.at()),MareEletrica.SHELL_WHISTLE.get(),SoundSource.HOSTILE,1.5F,.8F);
    continue;
   }
   int k=(int)(now-s.time());
   if(k%5==0&&k<40){   // 8 bombs, one every 1/4 s, scattered around the mark
    double x=s.at().x+r.nextGaussian()*3.5,z=s.at().z+r.nextGaussian()*3.5;double y=l.getHeight(Heightmap.Types.MOTION_BLOCKING,(int)Math.floor(x),(int)Math.floor(z));
    l.explode(null,x,y,z,3.6F,Level.ExplosionInteraction.TNT);
    l.sendParticles(ParticleTypes.EXPLOSION_EMITTER,x,y+.5,z,1,0,0,0,0);l.sendParticles(ParticleTypes.LARGE_SMOKE,x,y+1,z,30,1.5,1,1.5,.05);
   }
   if(k>=40)it.remove();
  }
 }

 // ---------------- supply crate ----------------
 public static final class Crate extends Entity {
  public float sway,swayO;
  public Crate(EntityType<? extends Crate> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override public void tick(){
   super.tick();swayO=sway;sway=(float)Math.sin(tickCount*.07)*6F;
   setDeltaMovement(new Vec3(Math.sin(tickCount*.03)*.02,-.18,0));move(MoverType.SELF,getDeltaMovement());
   if(level().isClientSide){if(tickCount%3==0)level().addParticle(new DustParticleOptions(new org.joml.Vector3f(.2F,1F,.3F),1.4F),getX(),getY()-.2,getZ(),0,-.05,0);return;}
   if(onGround()||isInWater()||tickCount>1200)land();
  }
  void land(){
   var l=(ServerLevel)level();BlockPos pos=blockPosition();
   if(!l.getBlockState(pos).canBeReplaced())pos=pos.above();
   l.setBlockAndUpdate(pos,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,net.minecraft.core.Direction.NORTH));
   if(l.getBlockEntity(pos) instanceof ChestBlockEntity c){
    var loot=List.of(new ItemStack(Wounds.BANDAGE.get(),4),new ItemStack(Wounds.SYRINGE.get(),2),new ItemStack(Wounds.MEDKIT.get()),
     new ItemStack(Arsenal.RIFLE_ROUND.get(),64),new ItemStack(Arsenal.RIFLE_ROUND.get(),64),new ItemStack(Items.COOKED_BEEF,8),new ItemStack(Items.GOLDEN_APPLE,2),new ItemStack(Barrage.ROCKET.get(),16));
    int i=0;for(var s:loot)c.setItem(i+=2,s);
   }
   l.sendParticles(ParticleTypes.CLOUD,getX(),getY()+.3,getZ(),25,.6,.2,.6,.05);
   l.playSound(null,pos,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,1.5F,.6F);l.playSound(null,pos,SoundEvents.ANVIL_LAND,SoundSource.BLOCKS,.5F,1.4F);
   discard();
  }
  @Override protected void readAdditionalSaveData(CompoundTag t){}
  @Override protected void addAdditionalSaveData(CompoundTag t){}
 }
}
