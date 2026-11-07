package br.mareeletrica;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
/** Aim remote: links up to 16 mortars / barrage launchers (or one floodlight) and aims them all where the player looks. */
public final class AimRemoteItem extends Item {
 public static final int MAX_LINKS=16;
 public AimRemoteItem(Properties p){super(p);}
 public static CompoundTag data(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
 private static void save(ItemStack s,CompoundTag t){s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
 public static boolean active(ItemStack s){return s.is(MareEletrica.AIM_REMOTE.get())&&data(s).getBoolean("Active");}
 /** Linked positions (older remotes stored a single "Position"). */
 public static List<BlockPos> links(CompoundTag t){
  var out=new ArrayList<BlockPos>();
  if(t.contains("Links",Tag.TAG_LIST))for(var e:t.getList("Links",Tag.TAG_LONG))out.add(BlockPos.of(((LongTag)e).getAsLong()));
  else if(t.getBoolean("Bound"))out.add(BlockPos.of(t.getLong("Position")));
  return out;
 }
 private static void storeLinks(CompoundTag t,List<BlockPos> links){var l=new ListTag();for(var p:links)l.add(LongTag.valueOf(p.asLong()));t.put("Links",l);t.putBoolean("Bound",!links.isEmpty());t.remove("Position");}
 private static boolean aimable(BlockEntity be){return be instanceof MachineEntity m&&(m.kind==Kind.LAMP||m.kind==Kind.MORTAR)||be instanceof Barrage.LauncherEntity||be instanceof Batteries.Gun;}
 /** Everything linked that is loaded, in range and usable right now. */
 public static List<BlockEntity> targets(Player p,ItemStack s){
  var t=data(s);var out=new ArrayList<BlockEntity>();
  if(!p.level().dimension().location().toString().equals(t.getString("Dimension"))||!p.mayBuild())return out;
  for(var pos:links(t)){
   if(p.distanceToSqr(pos.getCenter())>256*256||!p.level().hasChunkAt(pos)||!p.level().mayInteract(p,pos))continue;
   var be=p.level().getBlockEntity(pos);if(aimable(be))out.add(be);
  }
  return out;
 }
 @Override public InteractionResult useOn(UseOnContext c){
  var p=c.getPlayer();if(p==null||!p.isShiftKeyDown())return InteractionResult.PASS;
  BlockPos pos=c.getClickedPos();var state=c.getLevel().getBlockState(pos);if(state.getBlock() instanceof LargeMachinePartBlock)pos=LargeMachineStructure.root(pos,state);
  var be=c.getLevel().getBlockEntity(pos);
  if(!aimable(be))return InteractionResult.PASS;
  if(be instanceof Batteries.Gun g&&g.owner!=null&&!g.owner.equals(p.getUUID())&&!p.isCreative()){if(!c.getLevel().isClientSide)p.displayClientMessage(Component.literal("§cEssa arma não é sua."),true);return InteractionResult.sidedSuccess(c.getLevel().isClientSide);}
  if(!c.getLevel().isClientSide){
   var t=data(c.getItemInHand());String dim=c.getLevel().dimension().location().toString();
   var links=dim.equals(t.getString("Dimension"))?links(t):new ArrayList<BlockPos>();
   boolean lamp=be instanceof MachineEntity m&&m.kind==Kind.LAMP;
   if(lamp||links.stream().anyMatch(x->c.getLevel().getBlockEntity(x) instanceof MachineEntity m&&m.kind==Kind.LAMP))links.clear();   // a floodlight is always controlled alone
   String msg;
   if(links.remove(pos))msg="Removido do controle. Ligados: "+links.size();
   else if(links.size()>=MAX_LINKS)msg="Limite de "+MAX_LINKS+" armas por controle.";
   else{links.add(pos.immutable());msg=lamp?"Holofote vinculado. Use no ar para mirar.":"Arma vinculada ("+links.size()+"/"+MAX_LINKS+"). Shift+clique em outras para adicionar.";}
   storeLinks(t,links);t.putString("Dimension",dim);t.putBoolean("Active",false);save(c.getItemInHand(),t);
   p.displayClientMessage(Component.literal(msg),true);
  }
  return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
 }
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> tips,TooltipFlag f){
  int n=links(data(s)).size();
  tips.add(Component.literal(n==0?"Shift + clique em morteiros, lançadores, torres, antiaérea, lança-granadas ou holofote para vincular":"Vinculados: "+n+" (Shift + clique de novo para remover)"));
 }
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){ItemStack s=p.getItemInHand(hand);if(!l.isClientSide){var t=data(s);var list=targets(p,s);if(list.isEmpty()&&cannons(p,s).isEmpty()){p.displayClientMessage(Component.literal("Shift + botão direito em morteiros, lançadores, canhões ou holofote. Alcance: 256 blocos."),true);return InteractionResultHolder.fail(s);}boolean on=!t.getBoolean("Active");t.putBoolean("Active",on);save(s,t);if(on)for(var be:list)if(be instanceof MachineEntity m){m.enabled=true;m.syncAim();}p.displayClientMessage(Component.literal(on?"Mirando "+(list.size()+cannons(p,s).size())+" arma(s). Clique esquerdo: disparar todas. Roda: canhão. Direito: sair.":"Mira fixada."),true);}return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
 @Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){if(l.isClientSide||!selected||!active(s)||!(e instanceof Player p)||l.getGameTime()%2!=0)return;var list=targets(p,s);var guns=cannons(p,s);if(list.isEmpty()&&guns.isEmpty()){var t=data(s);t.putBoolean("Active",false);save(s,t);return;}int sel=data(s).getInt("Barrel");var aim=p.pick(256,1F,false).getLocation();for(var be:list)apply(be,p,aim,sel);for(var c:guns)aimCannon(c,aim);}
 /** Aim one weapon: each turns toward the point the player is looking at (so a spread-out battery converges); the elevation follows the player's view. */
 private static void apply(BlockEntity be,Player p,net.minecraft.world.phys.Vec3 aim,int selection){
  float yaw=p.getYRot(),pitch=p.getXRot();
  var c=be.getBlockPos().getCenter();double dx=aim.x-c.x,dz=aim.z-c.z;
  if(be instanceof MachineEntity m0&&m0.kind!=Kind.LAMP&&dx*dx+dz*dz>9)yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
  if(be instanceof Barrage.LauncherEntity&&dx*dx+dz*dz>9)yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
  if(be instanceof Barrage.LauncherEntity l){l.aimAt(aim);return;}
  if(be instanceof Batteries.Gun g){g.aimAt(aim);return;}
  if(be instanceof MachineEntity m)apply(m,yaw,pitch,selection);
 }
 public static void apply(MachineEntity m,float yaw,float pitch,int selection){
  if(!Float.isFinite(yaw)||!Float.isFinite(pitch))return;
  if(m.kind==Kind.LAMP){m.lampYaw=(yaw+180)%360;m.lampPitch=Math.max(-85,Math.min(85,pitch));}
  else if(m.mortarAt==0){int az=MortarAim.azimuth(Math.round(yaw-m.getBlockState().getValue(MachineBlock.FACING).toYRot()));for(int i=0;i<6;i++)if(selection==0||selection==i+1){m.mortarAzimuth[i]=az;m.mortarElevation[i]=MortarAim.elevation(Math.round(-pitch));}}
  m.syncAim();if(m.kind==Kind.LAMP&&m.enabled&&m.getBlockState().getValue(MachineBlock.ACTIVE))FloodlightBeam.refresh(m);
 }
 public static void command(ServerPlayer p,int action,int value){var s=p.getMainHandItem();if(!active(s))return;var list=targets(p,s);var guns=cannons(p,s);if(list.isEmpty()&&guns.isEmpty())return;
  if(action==0){int fired=0;for(var c:guns)if(c.fire(p))fired++;for(var be:list){if(be instanceof MachineEntity m&&m.kind==Kind.MORTAR){FrontierMachines.action(m,p);fired++;}else if(be instanceof Barrage.LauncherEntity l&&l.fire())fired++;else if(be instanceof Batteries.Gun g&&g.trigger(false))fired++;}
   if(list.size()+guns.size()>1)p.displayClientMessage(Component.literal("Fogo! "+fired+" de "+(list.size()+guns.size())+" armas dispararam."),true);}
  if(action==2)for(var be:list)if(be instanceof Batteries.Gun g&&g.kind()==Batteries.Kind.SENTRY)g.trigger(true);   // attack held: machine gun keeps firing
  if(action==1){var t=data(s);t.putInt("Barrel",Math.floorMod(value,7));save(s,t);p.displayClientMessage(Component.literal(value==0?"Mira: todos os canhões":"Mira: canhão "+Math.floorMod(value,7)),true);}
 }
 // ---------------- field cannons (entities) ----------------
 public static void linkCannon(ItemStack s,Emplacements.Mounted m,Player p){
  var t=data(s);var l=t.getList("Cannons",net.minecraft.nbt.Tag.TAG_INT_ARRAY);var id=net.minecraft.nbt.NbtUtils.createUUID(m.getUUID());
  boolean removed=false;for(int i=0;i<l.size();i++)if(l.get(i).equals(id)){l.remove(i);removed=true;break;}
  if(!removed)l.add(id);t.put("Cannons",l);t.putString("Dimension",p.level().dimension().location().toString());t.putBoolean("Active",false);save(s,t);
  p.displayClientMessage(Component.literal(removed?"Canhão removido do controle.":"Canhão vinculado ao controle de mira."),true);
 }
 public static List<Emplacements.Mounted> cannons(Player p,ItemStack s){
  var out=new ArrayList<Emplacements.Mounted>();if(!(p.level() instanceof net.minecraft.server.level.ServerLevel sl))return out;
  for(var e:data(s).getList("Cannons",net.minecraft.nbt.Tag.TAG_INT_ARRAY)){var id=net.minecraft.nbt.NbtUtils.loadUUID(e);if(sl.getEntity(id) instanceof Emplacements.Mounted m&&m.distanceToSqr(p)<256*256&&m.getPassengers().isEmpty())out.add(m);}
  return out;
 }
 /** Turn toward the point and pick the flattest elevation that reaches it (direct fire). */
 private static void aimCannon(Emplacements.Mounted m,net.minecraft.world.phys.Vec3 aim){
  var c=m.position().add(0,1.3,0);double dx=aim.x-c.x,dz=aim.z-c.z,dy=aim.y-c.y,dist=Math.sqrt(dx*dx+dz*dz);
  float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));int best=45;
  for(int el=-5;el<=60;el++){double a=Math.toRadians(el),vx=Math.cos(a)*3.6,vy=Math.sin(a)*3.6,x=0,y=0;boolean hit=false;
   for(int t=0;t<400;t++){double px=x,py=y;x+=vx;y+=vy;vx*=.99;vy=vy*.99-.035;if(x>=dist){hit=py+(y-py)*(dist-px)/Math.max(1e-6,x-px)>=dy-.5;break;}if(y<dy-60)break;}
   if(hit){best=el;break;}}
  m.aim(yaw,-best);
 }
}
