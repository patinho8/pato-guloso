package br.mareeletrica;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
public final class SniperItem extends Item {
 public SniperItem(Properties properties){super(properties);}
 public static boolean firearm(ItemStack stack){return MareEletrica.isSniper(stack)||stack.getItem() instanceof GunItem||stack.is(MareEletrica.SMG.get());}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(player.getItemInHand(hand));if(player.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(player.getItemInHand(hand));player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand));}
 public static void open(ServerPlayer p){if(!p.isAlive()||p.isSpectator()||RemoteSessions.active(p)||p.containerMenu!=p.inventoryMenu||!MareEletrica.isSniper(p.getMainHandItem()))return;p.stopUsingItem();Magazines.finish(p,p.getMainHandItem(),false);p.openMenu(new SimpleMenuProvider((id,inv,who)->new RifleMenu(id,inv),Component.literal("Oficina da sniper")));}
 @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 72000;}
 @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.NONE;}
 public static boolean scoped(Player p){return p!=null&&p.isUsingItem()&&(MareEletrica.isSniper(p.getUseItem())||p.getUseItem().is(MareEletrica.SMG.get())||p.getUseItem().getItem() instanceof GunItem);}
 public static double zeroAngle(int distance){if(distance<=0)return 0;double x=0,y=0,vx=40,vy=0;for(int i=0;i<40;i++){double step=Math.min(1,(distance-x)/vx);x+=vx*step;y+=vy*step;if(x>=distance)return Math.toDegrees(Math.atan2(-y,x));vx*=.99;vy=vy*.99-.015;}return 0;}
 public static float spread(Player p){if(!scoped(p))return !p.onGround()?20F:p.isSprinting()?16F:ProneSystem.supported(p)?8F:12F;return ProneSystem.prone(p)&&!ProneSystem.supported(p)?1.4F:0F;}
 public static float shotVolume(int silencer){return silencer<=0?12F:new float[]{0,3.2F,2.8F,2.4F,2F,1.6F}[Math.min(5,silencer)];}
 public static float recoil(Player p){return (ProneSystem.supported(p)?6.5F:15F)*(1-RifleLoadout.suppressor(p.getMainHandItem())*.02F);}
 public static void smoke(ServerPlayer p,Vec3 aim,int count){var pos=p.getEyePosition().add(aim.scale(1.1));p.serverLevel().sendParticles(MareEletrica.MUZZLE_SMOKE.get(),pos.x,pos.y,pos.z,count,.025,.025,.025,.015);}
 public static void fire(ServerPlayer p){var gun=p.getMainHandItem();if(!MareEletrica.isSniper(gun)||!p.isAlive()||p.isSpectator()||RemoteSessions.active(p)||p.containerMenu!=p.inventoryMenu||p.getCooldowns().isOnCooldown(gun.getItem())||Magazines.reloading(gun))return;
  var kit=RifleLoadout.of(p);int count=Magazines.rounds(gun);if(count==0){p.displayClientMessage(Component.literal("Pente vazio. R para trocar o pente."),true);return;}kit.rounds(count-1);
  double up=zeroAngle(RifleLoadout.calibration(gun,0))+RifleLoadout.calibration(gun,2)*.02;double side=RifleLoadout.calibration(gun,1)*.02;var aim=Vec3.directionFromRotation((float)(p.getXRot()-up),(float)(p.getYRot()+side));
  var bullet=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),p.level());bullet.setSniper(true);bullet.setOwner(p);bullet.setPos(p.getEyePosition());bullet.shoot(aim.x,aim.y,aim.z,40,spread(p));p.level().addFreshEntity(bullet);
  int silencer=RifleLoadout.suppressor(gun);float recoil=recoil(p);p.getCooldowns().addCooldown(gun.getItem(),35);p.stopUsingItem();smoke(p,aim,silencer>0?3:6);
  p.level().playSound(null,p.blockPosition(),silencer>0?MareEletrica.SNIPER_SILENCED.get():MareEletrica.SNIPER_SHOT.get(),SoundSource.PLAYERS,shotVolume(silencer),silencer>0?.94F:1F);
  float hv=HeavyRecoil.camera(p,WeaponFamily.SNIPER);HeavyRecoil.shove(p,WeaponFamily.SNIPER);
  PacketDistributor.sendToPlayer(p,new GunPackets.Shot(true,recoil*hv,(p.getRandom().nextFloat()-.5F)*3.5F*hv));
 }
 @Override public boolean shouldCauseReequipAnimation(ItemStack oldStack,ItemStack newStack,boolean changed){return changed||oldStack.getItem()!=newStack.getItem();}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<Component> lines,TooltipFlag f){lines.add(Component.literal("M2: mirar • M1: disparar • R: trocar pente • J: acessórios"));lines.add(Component.literal("Mira "+RifleLoadout.zoom(s)+"× • regulagem "+RifleLoadout.calibration(s,0)+" blocos • 60 de dano (20 em golems e chefes)"));lines.add(Component.literal("Shift duas vezes: deitar/levantar. Bipé estabiliza ao ficar parado."));}
}
