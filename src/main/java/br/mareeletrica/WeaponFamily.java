package br.mareeletrica;

import net.minecraft.world.item.*;

/** Stable accessory and magazine families; never inferred from the shared ammo item. */
public enum WeaponFamily {
 DESERT_EAGLE("desert_eagle","Desert Eagle",7,42,1,2,3), USP("usp","USP",12,34,1,2,3),
 M4A1("m4a1","M4A1",30,48,1,3,6), AK47("ak47","AK-47",30,52,1,2,4),
 M870("m870","M870",6,54,1,2,3), MP5("mp5","MP5",60,38,1,2,3),
 SMG("smg","SMG Vetor",30,38,1,2,4), SNIPER("sniper","Sniper",5,54,5,12,24), MP40("mp40","MP40",32,40,1,2,3), P90("p90","P90",50,34,1,2,3), PYTHON("python","Python .357",6,40,1,2,3),
 GLOCK("glock","Glock 18",20,34,1,2,3), SCAR("scar","SCAR-H",20,50,1,3,6), UMP45("ump45","UMP-45",25,38,1,2,3), SPAS12("spas12","SPAS-12",8,56,1,2,3),
 BERETTA("beretta","Beretta M9",15,34,1,2,3), UZI("uzi","Uzi",32,36,1,2,3), FAMAS("famas","FAMAS",25,46,1,3,6), M249("m249","M249",100,90,1,2,4), BARRETT("barrett","Barrett M82",10,60,4,8,12),
 MAC10("mac10","MAC-10",30,32,1,2,3), GALIL("galil","Galil ACE",35,50,1,3,6), AUG("aug","Steyr AUG",30,46,1,3,6), SAIGA12("saiga12","Saiga-12",8,50,1,2,3), SVD("svd","Dragunov SVD",10,56,4,8,12);
 public final String id,title;public final int capacity,reloadTicks;public final int[] zooms;
 WeaponFamily(String id,String title,int cap,int ticks,int... zooms){this.id=id;this.title=title;capacity=cap;reloadTicks=ticks;this.zooms=zooms;}
 public boolean pistol(){return this==DESERT_EAGLE||this==USP||this==PYTHON||this==GLOCK||this==BERETTA;}
 public Item ammo(){return switch(this){case DESERT_EAGLE,PYTHON->MareEletrica.REVOLVER_ROUND.get();case USP,GLOCK,BERETTA->Arsenal.USP_ROUND.get();case M4A1,AK47,SCAR,FAMAS,M249,GALIL,AUG->Arsenal.RIFLE_ROUND.get();case M870,SPAS12,SAIGA12->Arsenal.SHOTGUN_SHELL.get();case MP5,SMG,MP40,P90,UMP45,UZI,MAC10->MareEletrica.SMG_ROUND.get();case SNIPER,BARRETT,SVD->MareEletrica.SNIPER_ROUND.get();};}
 public Item item(){return switch(this){case DESERT_EAGLE->MareEletrica.REVOLVER.get();case USP->Arsenal.USP.get();case M4A1->Arsenal.M4A1.get();case AK47->Arsenal.AK47.get();case M870->Arsenal.M870.get();case MP5->Arsenal.MP5.get();case MP40->Arsenal.MP40.get();case P90->Arsenal.P90.get();case PYTHON->Arsenal.PYTHON.get();case GLOCK->Arsenal.GLOCK.get();case SCAR->Arsenal.SCAR.get();case UMP45->Arsenal.UMP45.get();case SPAS12->Arsenal.SPAS12.get();case BERETTA->Arsenal.BERETTA.get();case UZI->Arsenal.UZI.get();case FAMAS->Arsenal.FAMAS.get();case M249->Arsenal.M249.get();case BARRETT->Arsenal.BARRETT.get();case MAC10->Arsenal.MAC10.get();case GALIL->Arsenal.GALIL.get();case AUG->Arsenal.AUG.get();case SAIGA12->Arsenal.SAIGA12.get();case SVD->Arsenal.SVD.get();case SMG->MareEletrica.SMG.get();case SNIPER->MareEletrica.SNIPER.get();};}
 public static WeaponFamily of(ItemStack s){if(s.getItem() instanceof GunItem g)return valueOf(g.profile.name());if(s.is(MareEletrica.SMG.get()))return SMG;if(MareEletrica.isSniper(s))return SNIPER;return null;}
 public String modules(){return this==SNIPER?"RifleModules":this==SMG?"SmgModules":"ArsenalModules";}
 public int reserve(){return this==SNIPER?4:3;}
}
