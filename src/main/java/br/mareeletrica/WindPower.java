package br.mareeletrica;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/** Wind energy (0.46.0): five turbines that turn faster up high, in open terrain and in storms, an anemometer mast that boosts
 *  nearby turbines, a wind battery and an inverter (both work as cores) and a control panel that shows the total and can brake them all. */
public final class WindPower {
 private WindPower(){}
 public static final int RADIUS_MAST=24,RADIUS_PANEL=32;
 private static final Map<ResourceKey<Level>,Map<BlockPos,Kind>> SITES=new HashMap<>();

 /** Per-10-tick output in calm, sea-level wind. */
 public static int base(Kind k){return switch(k){case WIND_SMALL->120;case WIND_MEDIUM->320;case WIND_GIANT->1100;case WINDMILL->180;case WIND_ROOF->100;default->0;};}
 /** Client rotor speed in degrees per tick. */
 public static float spin(Kind k){return switch(k){case WIND_SMALL->9F;case WIND_MEDIUM->5F;case WIND_GIANT->2.2F;case WINDMILL->3.5F;case WIND_ROOF->12F;default->6F;};}
 /** Blocks of free air the rotor needs straight above the block. */
 public static int clearance(Kind k){return switch(k){case WIND_GIANT->9;case WIND_MEDIUM->2;case WINDMILL->2;default->1;};}

 public static void register(MachineEntity m){
  if(m.getLevel()==null||m.getLevel().isClientSide)return;
  if(m.kind==Kind.WIND_MAST||m.kind==Kind.WIND_PANEL)SITES.computeIfAbsent(m.getLevel().dimension(),d->new HashMap<>()).put(m.getBlockPos().immutable(),m.kind);
 }
 private static List<MachineEntity> near(MachineEntity m,Kind kind,int radius){
  var map=SITES.get(m.getLevel().dimension());if(map==null)return List.of();
  var out=new ArrayList<MachineEntity>();
  for(var it=map.entrySet().iterator();it.hasNext();){
   var e=it.next();if(e.getValue()!=kind)continue;var p=e.getKey();
   if(p.distSqr(m.getBlockPos())>radius*radius)continue;
   if(!m.getLevel().hasChunkAt(p))continue;
   if(!(m.getLevel().getBlockEntity(p) instanceof MachineEntity x)||x.kind!=kind){it.remove();continue;}
   out.add(x);
  }
  return out;
 }
 /** Wind strength at a position, 0..~3. */
 public static float wind(ServerLevel level,BlockPos pos){
  if(!level.dimensionType().hasSkyLight()||level.dimensionType().hasCeiling())return 0;
  float height=Mth.clamp((pos.getY()-58)/40F,.25F,1.8F);
  float weather=level.isThundering()?1.7F:level.isRaining()?1.35F:1F;
  float gust=.85F+.3F*Mth.sin(level.getGameTime()/600F+(pos.asLong()%97));
  // open terrain: count solid blocks in a 5x5x3 box around the rotor
  int solid=0;
  for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int dy=1;dy<=3;dy++){if(dx==0&&dz==0)continue;var q=pos.offset(dx,dy,dz);if(level.hasChunkAt(q)&&!level.getBlockState(q).getCollisionShape(level,q).isEmpty())solid++;}
  float open=1F-Math.min(.85F,solid/28F);
  return height*weather*gust*open;
 }
 public static boolean tick(MachineEntity m){
  if(!(m.getLevel() instanceof ServerLevel level))return false;
  if(m.kind==Kind.WIND_MAST){float w=wind(level,m.getBlockPos().above());m.status=w<=0?70:72;m.lastRate=0;return w>0;}
  if(m.kind==Kind.WIND_PANEL){
   int total=0,count=0;
   for(var be:turbines(m))if(be.kind.windTurbine()){total+=be.lastRate;count++;}
   m.lastRate=total;m.status=count==0?75:73;return count>0;
  }
  // turbine: push power to cores, batteries and inverters on the cable network
  m.lastRate=0;
  int sent=PowerNetwork.send(m,false,64_000,Kind::core);
  for(var panel:near(m,Kind.WIND_PANEL,RADIUS_PANEL))if(!panel.enabled){m.status=71;m.lastRate=sent*2;return false;}
  for(int i=1;i<=clearance(m.kind);i++){var q=m.getBlockPos().above(i);if(!level.getBlockState(q).getCollisionShape(level,q).isEmpty()){m.status=74;m.lastRate=sent*2;return false;}}
  float w=wind(level,m.getBlockPos().above(clearance(m.kind)));
  if(m.kind==Kind.WIND_ROOF&&builtOn(level,m.getBlockPos()))w*=1.5F;
  if(!near(m,Kind.WIND_MAST,RADIUS_MAST).isEmpty())w*=1.2F;
  int rate=Math.round(base(m.kind)*w);
  if(rate<=0){m.status=70;m.lastRate=sent*2;return false;}
  int generated=m.clean.add(rate);
  m.lastRate=Math.max(sent,generated)*2;m.status=generated>0?1:10;
  return generated>0;
 }
 /** Rooftop turbines produce more on top of a building (three or more solid blocks right under them). */
 private static boolean builtOn(ServerLevel l,BlockPos p){int n=0;for(int i=1;i<=6&&n<3;i++){var q=p.below(i);if(l.getBlockState(q).isAir())return false;if(!l.getBlockState(q).getCollisionShape(l,q).isEmpty())n++;}return n>=3;}
 /** Turbines whose block entities are loaded within the panel radius. */
 private static List<MachineEntity> turbines(MachineEntity panel){
  var out=new ArrayList<MachineEntity>();var level=panel.getLevel();var c=panel.getBlockPos();
  int r=RADIUS_PANEL;
  for(int cx=(c.getX()-r)>>4;cx<=(c.getX()+r)>>4;cx++)for(int cz=(c.getZ()-r)>>4;cz<=(c.getZ()+r)>>4;cz++){
   if(!level.hasChunk(cx,cz))continue;
   for(var be:level.getChunk(cx,cz).getBlockEntities().values())if(be instanceof MachineEntity me&&me.kind.windTurbine()&&be.getBlockPos().distSqr(c)<=r*r)out.add(me);
  }
  return out;
 }
}
