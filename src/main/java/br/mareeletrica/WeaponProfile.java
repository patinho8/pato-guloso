package br.mareeletrica;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
/** Fictional Minecraft balance; dimensions and timing are game units. */
public enum WeaponProfile {
 DESERT_EAGLE("desert_eagle","Desert Eagle",7,9,36,14,18,6,.12F,18F,false,true,1,160),
 USP("usp","USP",12,5,28,7,20,5,.10F,1.4F,false,true,1,160),
 M4A1("m4a1","M4A1",30,3,40,6,24,6,.35F,.9F,true,false,1,240),
 AK47("ak47","AK-47",30,4,46,8,22,7,.60F,1.5F,true,false,1,220),
 M870("m870","M870",6,20,50,4.5F,14,9,5F,5F,false,false,8,48),
 MP5("mp5","MP5",60,2,32,2.5F,16,8.5F,1.25F,.65F,true,false,1,150),
 MP40("mp40","MP40",32,3,34,2.8F,16,9F,1.4F,.75F,true,false,1,140),
 P90("p90","P90",50,2,30,2.3F,17,7.5F,1.0F,.55F,true,false,1,150),
 PYTHON("python","Python .357",6,10,34,12,22,6,.14F,12F,false,true,1,160),
 GLOCK("glock","Glock 18",20,2,26,5,20,6.5F,1.4F,1.6F,true,true,1,150),
 SCAR("scar","SCAR-H",20,4,44,9,24,6,.45F,1.7F,true,false,1,240),
 UMP45("ump45","UMP-45",25,3,34,4,17,7.5F,1.1F,.8F,true,false,1,150),
 SPAS12("spas12","SPAS-12",8,16,54,4,14,9,5F,5.5F,false,false,9,48),
 BERETTA("beretta","Beretta M9",15,4,28,6,20,5,.11F,1.3F,false,true,1,160),
 UZI("uzi","Uzi",32,2,32,2.6F,16,9F,1.5F,.7F,true,false,1,130),
 FAMAS("famas","FAMAS",25,2,40,5.5F,24,6.5F,.5F,1.1F,true,false,1,230),
 M249("m249","M249",100,2,90,6,24,8F,1.2F,1.2F,true,false,1,220),
 BARRETT("barrett","Barrett M82",10,14,60,24,30,9F,.04F,9F,false,false,1,320),
 MAC10("mac10","MAC-10",30,1,30,2.4F,15,10F,1.8F,.8F,true,false,1,120),
 GALIL("galil","Galil ACE",35,3,48,6.5F,23,7F,.45F,1.2F,true,false,1,230),
 AUG("aug","Steyr AUG",30,3,44,6F,25,6F,.3F,.9F,true,false,1,250),
 SAIGA12("saiga12","Saiga-12",8,6,48,4F,14,9F,5F,4.5F,false,false,8,46),
 SVD("svd","Dragunov SVD",10,8,54,16,28,8F,.08F,5F,false,false,1,300);
 public final String id,title;public final int capacity,interval,reloadTicks,pellets,range;public final float damage,speed,hipSpread,aimSpread,recoil;public final boolean automatic,pistol;
 WeaponProfile(String id,String title,int capacity,int interval,int reloadTicks,float damage,float speed,float hipSpread,float aimSpread,float recoil,boolean automatic,boolean pistol,int pellets,int range){this.id=id;this.title=title;this.capacity=capacity;this.interval=interval;this.reloadTicks=reloadTicks;this.damage=damage;this.speed=speed;this.hipSpread=hipSpread;this.aimSpread=aimSpread;this.recoil=recoil;this.automatic=automatic;this.pistol=pistol;this.pellets=pellets;this.range=range;}
 public Item ammo(){return switch(this){case DESERT_EAGLE,PYTHON->MareEletrica.REVOLVER_ROUND.get();case USP,GLOCK,BERETTA->Arsenal.USP_ROUND.get();case M4A1,AK47,SCAR,FAMAS,M249,GALIL,AUG->Arsenal.RIFLE_ROUND.get();case M870,SPAS12,SAIGA12->Arsenal.SHOTGUN_SHELL.get();case BARRETT,SVD->MareEletrica.SNIPER_ROUND.get();case MP5,MP40,P90,UMP45,UZI,MAC10->MareEletrica.SMG_ROUND.get();};}
 public static WeaponProfile of(ItemStack s){return s.getItem() instanceof GunItem g?g.profile:null;}
}
