package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.recarga): slow-motion reload of several guns, 8 screenshots each. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualRecarga {
 static int tick;static final int D=100,SLOT=125;
 static final float[] P={.08F,.20F,.34F,.50F,.62F,.71F,.77F,.87F};
 static WeaponFamily[] guns(){String g=System.getProperty("mareeletrica.recarga.guns","");
  if(g.isEmpty())return new WeaponFamily[]{WeaponFamily.M4A1,WeaponFamily.AK47,WeaponFamily.GLOCK,WeaponFamily.DESERT_EAGLE,WeaponFamily.M249,WeaponFamily.AUG,WeaponFamily.M870,WeaponFamily.MP5,WeaponFamily.BARRETT};
  return java.util.Arrays.stream(g.split(",")).map(x->WeaponFamily.valueOf(x.equals("hunter")?"SNIPER":x.toUpperCase())).toArray(WeaponFamily[]::new);}
 static boolean hunter(int k){String[] g=System.getProperty("mareeletrica.recarga.guns","").split(",");return k<g.length&&g[k].equals("hunter");}
 static int dur(WeaponFamily f){return Magazines.loosePre(f)+f.capacity*Magazines.loosePer(f)+Magazines.loosePost(f);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"rec_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.recarga"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(tick==50&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60){mc.gui.getChat().clearMessages(false);srv(()->{var p=sp();var l=p.serverLevel();l.setDayTime(6000);for(var m:l.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,p.getBoundingBox().inflate(60)))m.discard();p.setXRot(5);p.setInvisible(Boolean.getBoolean("mareeletrica.recarga.inv"));});}
  var G=guns();int t=tick-80;if(t<0)return;int k=t/SLOT,r=t%SLOT;if(k>=G.length){if(r==0)srv(()->{var p=sp();WeaponFamily[] H={WeaponFamily.M4A1,WeaponFamily.BARRETT,WeaponFamily.GLOCK,WeaponFamily.M249,WeaponFamily.SNIPER,WeaponFamily.M870,WeaponFamily.PYTHON,WeaponFamily.AK47,WeaponFamily.AUG};for(int i=0;i<9;i++)p.getInventory().setItem(i,new ItemStack(H[i].item()));p.getInventory().selected=8;});
   if(r==20){mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);}if(r==30){shot("hotbar");System.out.println("[REC] DONE");}return;}
  var f=G[k];
  final int kk=k;if(r==0)srv(()->{var p=sp();var s=new ItemStack(hunter(kk)?MareEletrica.HUNTER.get():f.item());long now=p.serverLevel().getGameTime()+5;
   CustomData.update(DataComponents.CUSTOM_DATA,s,tg->{tg.putBoolean("ArmoryFormat",true);tg.putInt("MagRounds",0);
    if(Magazines.loose(f)){int d=dur(f);tg.putInt("ShellLoad",f.capacity);tg.putInt("ShellDone",0);tg.putLong("ReloadStart",now);tg.putLong("ReloadEnd",now+d);tg.putInt("ReloadDuration",d);tg.putUUID("ReloadOwner",p.getUUID());return;}
    tg.put("PendingMagazine",MagazineItem.make(f,f.capacity).save(p.registryAccess()));
    tg.putLong("ReloadStart",now);tg.putLong("ReloadEnd",now+100000);tg.putInt("ReloadDuration",D);tg.putUUID("ReloadOwner",p.getUUID());});
   p.getInventory().setItem(p.getInventory().selected,s);if(Magazines.loose(f))p.getInventory().setItem(20,new ItemStack(f.ammo(),64));});
  for(int i=0;i<P.length;i++)if(r==6+Math.round(P[i]*(Magazines.loose(f)?dur(f):D)))shot((hunter(k)?"hunter":f.id)+"_"+i);
 }
}
