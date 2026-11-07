package br.mareeletrica;

import java.util.List;

/** Pokémon-style data for the fighting roosters: elemental types, breeds (colour + type + base stats) and battle moves. */
public final class ChickenTypes {
 private ChickenTypes(){}

 public enum Type {
  NORMAL("Normal",0xFFC8C0A8),FOGO("Fogo",0xFFFF6A20),AGUA("Água",0xFF3C8CFF),PLANTA("Planta",0xFF52C040),ELETRICO("Elétrico",0xFFFFD820),
  GELO("Gelo",0xFF90E0FF),PEDRA("Pedra",0xFFA08868),VENTO("Vento",0xFFB8D8FF),SOMBRA("Sombra",0xFF8A50C8);
  public final String label;public final int color;
  Type(String l,int c){label=l;color=c;}
  /** Damage multiplier of an attack of this type against a defender of type d. */
  public float against(Type d){
   return switch(this){
    case NORMAL->d==PEDRA||d==SOMBRA?.5F:1;
    case FOGO->d==PLANTA||d==GELO?2:d==AGUA||d==PEDRA||d==FOGO?.5F:1;
    case AGUA->d==FOGO||d==PEDRA?2:d==PLANTA||d==AGUA?.5F:1;
    case PLANTA->d==AGUA||d==PEDRA?2:d==FOGO||d==VENTO||d==PLANTA?.5F:1;
    case ELETRICO->d==AGUA||d==VENTO?2:d==PEDRA||d==PLANTA||d==ELETRICO?.5F:1;
    case GELO->d==PLANTA||d==VENTO?2:d==FOGO||d==AGUA||d==GELO?.5F:1;
    case PEDRA->d==FOGO||d==GELO||d==VENTO?2:d==PLANTA?.5F:1;
    case VENTO->d==PLANTA?2:d==ELETRICO||d==PEDRA?.5F:1;
    case SOMBRA->d==SOMBRA||d==ELETRICO?2:d==NORMAL?.5F:1;
   };
  }
 }

 public enum Effect {NONE,ATK_UP,ATK_UP2,DEF_UP,DEF_UP2,SPD_UP2,ATK_SPD_UP,FOE_ATK_DOWN2,FOE_DEF_DOWN,FOE_DEF_DOWN2,FOE_SPD_DOWN,HEAL,HEAL_DEF,BURN,PARALYZE,FREEZE,DRAIN,RECOIL,FLINCH,HIGH_CRIT}

 /** kind: 0 physical (peck/kick animation), 1 special (wings), 2 status (no damage). */
 public enum Move {
  BICADA("Bicada",Type.NORMAL,40,100,0,Effect.NONE,0,"Uma bicada bem dada."),
  BICADA_RAPIDA("Bicada Rápida",Type.NORMAL,35,100,0,Effect.NONE,0,"Sempre ataca primeiro."),
  VOADORA("Voadora",Type.NORMAL,70,90,0,Effect.NONE,0,"Pulo com chute de dois pés."),
  CANTO_DE_GUERRA("Canto de Guerra",Type.NORMAL,0,100,2,Effect.ATK_UP2,0,"Ataque sobe muito."),
  PENAS_ERICADAS("Penas Eriçadas",Type.NORMAL,0,100,2,Effect.DEF_UP2,0,"Defesa sobe muito."),
  OLHAR_BRAVO("Olhar Bravo",Type.NORMAL,0,100,2,Effect.FOE_DEF_DOWN,0,"Defesa do inimigo cai."),
  NINHO("Descanso no Ninho",Type.NORMAL,0,100,2,Effect.HEAL,50,"Recupera metade da vida."),
  BICO_EM_BRASA("Bico em Brasa",Type.FOGO,60,100,0,Effect.BURN,20,"20% de queimar."),
  CRISTA_FLAMEJANTE("Crista Flamejante",Type.FOGO,90,85,1,Effect.BURN,15,"Forte. 15% de queimar."),
  DANCA_DO_SOL("Dança do Sol",Type.FOGO,0,100,2,Effect.ATK_SPD_UP,0,"Ataque e velocidade sobem."),
  JATO_DAGUA("Jato d'Água",Type.AGUA,50,100,1,Effect.NONE,0,"Esguicho forte."),
  ONDA_DE_PENAS("Onda de Penas",Type.AGUA,85,90,1,Effect.NONE,0,"Uma onda inteira."),
  CHUVA_CALMA("Chuva Calma",Type.AGUA,0,100,2,Effect.HEAL_DEF,35,"Cura 35% e defesa sobe."),
  CHICOTE_DE_CAPIM("Chicote de Capim",Type.PLANTA,55,100,0,Effect.NONE,0,"Chicotada de mato."),
  SEMENTE_SUGADORA("Semente Sugadora",Type.PLANTA,55,100,1,Effect.DRAIN,50,"Rouba metade do dano em vida."),
  ESPOROS("Esporos",Type.PLANTA,0,75,2,Effect.PARALYZE,100,"Paralisa o inimigo."),
  BICADA_CHOQUE("Bicada Choque",Type.ELETRICO,60,100,0,Effect.PARALYZE,20,"20% de paralisar."),
  RAIO_DO_GALO("Raio do Galo",Type.ELETRICO,95,80,1,Effect.PARALYZE,15,"Raio do céu! 15% de paralisar."),
  CARGA("Carga Elétrica",Type.ELETRICO,0,100,2,Effect.SPD_UP2,0,"Velocidade sobe muito."),
  GRANIZO("Granizo",Type.GELO,55,100,1,Effect.FREEZE,10,"10% de congelar."),
  BAFO_GELADO("Bafo Gelado",Type.GELO,40,100,1,Effect.FOE_SPD_DOWN,100,"O inimigo fica mais lento."),
  NEVASCA("Nevasca",Type.GELO,95,75,1,Effect.FREEZE,20,"Forte. 20% de congelar."),
  PEDRADA("Pedrada",Type.PEDRA,65,95,0,Effect.NONE,0,"Joga uma pedra."),
  CASCA_DURA("Casca Dura",Type.PEDRA,0,100,2,Effect.DEF_UP2,0,"Defesa sobe muito."),
  AVALANCHE("Avalanche",Type.PEDRA,105,80,0,Effect.RECOIL,25,"Fortíssimo, mas machuca você."),
  RAJADA("Rajada",Type.VENTO,50,100,1,Effect.NONE,0,"Vento cortante."),
  FURACAO_DE_ASAS("Furacão de Asas",Type.VENTO,80,90,1,Effect.FLINCH,20,"20% do inimigo perder a vez."),
  VOO_ALTO("Voo Alto",Type.VENTO,0,100,2,Effect.ATK_SPD_UP,0,"Ataque e velocidade sobem."),
  GARRA_SOMBRIA("Garra Sombria",Type.SOMBRA,70,100,0,Effect.HIGH_CRIT,0,"Muita chance de crítico."),
  MEDO("Medo",Type.SOMBRA,0,90,2,Effect.FOE_ATK_DOWN2,0,"Ataque do inimigo cai muito."),
  PESADELO("Pesadelo",Type.SOMBRA,85,90,1,Effect.FLINCH,25,"25% do inimigo perder a vez."),
  // ---- exclusive moves (0.55.1) ----
  FAGULHA("Fagulha",Type.FOGO,40,100,1,Effect.BURN,10,"Faísca de fogo. 10% de queimar."),
  RASANTE_FLAMEJANTE("Rasante Flamejante",Type.FOGO,80,95,0,Effect.RECOIL,15,"Mergulho em chamas; machuca um pouco você."),
  OVO_INFERNAL("Ovo Infernal",Type.FOGO,110,75,1,Effect.BURN,30,"Golpe final de fogo. 30% de queimar."),
  BOLHAS("Bolhas",Type.AGUA,40,100,1,Effect.NONE,0,"Bolhas que estouram no inimigo."),
  CASCATA("Cascata",Type.AGUA,80,100,0,Effect.FLINCH,20,"Pancada de água. 20% do inimigo perder a vez."),
  TSUNAMI("Tsunami do Galinheiro",Type.AGUA,110,80,1,Effect.NONE,0,"Golpe final de água."),
  FOLHA_NAVALHA("Folha Navalha",Type.PLANTA,55,95,0,Effect.HIGH_CRIT,0,"Folhas afiadas. Muito crítico."),
  RAIZES("Raízes",Type.PLANTA,0,100,2,Effect.HEAL_DEF,30,"Cura 30% e defesa sobe."),
  TEMPESTADE_PETALAS("Tempestade de Pétalas",Type.PLANTA,110,85,1,Effect.NONE,0,"Golpe final de planta."),
  FAISCA("Faísca",Type.ELETRICO,40,100,1,Effect.PARALYZE,10,"Choquinho. 10% de paralisar."),
  CHOQUE_RAPIDO("Choque Rápido",Type.ELETRICO,40,100,0,Effect.NONE,0,"Sempre ataca primeiro."),
  TROVOADA("Trovoada",Type.ELETRICO,110,75,1,Effect.PARALYZE,30,"Golpe final elétrico. 30% de paralisar."),
  BICADA_DE_GELO("Bicada de Gelo",Type.GELO,55,100,0,Effect.FREEZE,10,"Bico gelado. 10% de congelar."),
  ARMADURA_DE_GELO("Armadura de Gelo",Type.GELO,0,100,2,Effect.DEF_UP2,0,"Penas de gelo: defesa sobe muito."),
  ERA_DO_GELO("Era do Gelo",Type.GELO,110,75,1,Effect.FREEZE,25,"Golpe final de gelo. 25% de congelar."),
  CASCALHO("Cascalho",Type.PEDRA,40,100,0,Effect.NONE,0,"Chuva de pedrinhas."),
  CABECADA_DE_PEDRA("Cabeçada de Pedra",Type.PEDRA,80,90,0,Effect.FLINCH,20,"20% do inimigo perder a vez."),
  TERREMOTO("Terremoto",Type.PEDRA,110,90,0,Effect.NONE,0,"Golpe final de pedra."),
  CORTE_DE_VENTO("Corte de Vento",Type.VENTO,40,100,1,Effect.NONE,0,"Sempre ataca primeiro."),
  MERGULHO_AEREO("Mergulho Aéreo",Type.VENTO,85,95,0,Effect.HIGH_CRIT,0,"Mergulho do céu. Muito crítico."),
  CICLONE("Ciclone",Type.VENTO,110,80,1,Effect.FLINCH,20,"Golpe final de vento."),
  SOMBRA_FURTIVA("Sombra Furtiva",Type.SOMBRA,40,100,0,Effect.NONE,0,"Sempre ataca primeiro."),
  MALDICAO("Maldição",Type.SOMBRA,0,90,2,Effect.FOE_DEF_DOWN2,0,"Defesa do inimigo cai muito."),
  ECLIPSE("Eclipse",Type.SOMBRA,110,80,1,Effect.FLINCH,20,"Golpe final de sombra.");
  public final String label,desc;public final Type type;public final int power,accuracy,kind,chance;public final Effect effect;
  Move(String l,Type t,int p,int a,int k,Effect e,int ch,String d){label=l;type=t;power=p;accuracy=a;kind=k;effect=e;chance=ch;desc=d;}
  public int priority(){return this==BICADA_RAPIDA||this==CHOQUE_RAPIDO||this==CORTE_DE_VENTO||this==SOMBRA_FURTIVA?1:0;}
 }

 public enum Breed {
  NORMAL("Galo de Briga",Type.NORMAL,45,55,45,50,new int[]{150,46,26},new int[]{232,150,40},new int[]{26,30,28},new int[]{110,52,30},
   Move.BICADA,Move.VOADORA,Move.CANTO_DE_GUERRA,Move.BICADA_RAPIDA,Move.OLHAR_BRAVO,Move.NINHO),
  FOGO("Galo Brasa",Type.FOGO,42,62,40,58,new int[]{205,62,22},new int[]{255,176,44},new int[]{96,20,10},new int[]{178,40,18},
   Move.FAGULHA,Move.BICO_EM_BRASA,Move.DANCA_DO_SOL,Move.RASANTE_FLAMEJANTE,Move.CRISTA_FLAMEJANTE,Move.OVO_INFERNAL),
  AGUA("Galo Maré",Type.AGUA,50,48,52,46,new int[]{40,92,196},new int[]{116,196,244},new int[]{18,40,112},new int[]{30,70,162},
   Move.BOLHAS,Move.JATO_DAGUA,Move.CHUVA_CALMA,Move.CASCATA,Move.ONDA_DE_PENAS,Move.TSUNAMI),
  PLANTA("Galo Folha",Type.PLANTA,52,50,50,44,new int[]{62,140,50},new int[]{176,214,84},new int[]{30,80,30},new int[]{50,112,40},
   Move.CHICOTE_DE_CAPIM,Move.FOLHA_NAVALHA,Move.ESPOROS,Move.SEMENTE_SUGADORA,Move.RAIZES,Move.TEMPESTADE_PETALAS),
  ELETRICO("Galo Faísca",Type.ELETRICO,40,52,40,66,new int[]{232,200,40},new int[]{255,242,124},new int[]{56,56,70},new int[]{204,170,30},
   Move.FAISCA,Move.CHOQUE_RAPIDO,Move.CARGA,Move.BICADA_CHOQUE,Move.RAIO_DO_GALO,Move.TROVOADA),
  GELO("Galo Geada",Type.GELO,46,52,46,52,new int[]{206,232,246},new int[]{150,210,242},new int[]{88,150,204},new int[]{172,212,236},
   Move.GRANIZO,Move.BICADA_DE_GELO,Move.BAFO_GELADO,Move.ARMADURA_DE_GELO,Move.NEVASCA,Move.ERA_DO_GELO),
  PEDRA("Galo Rocha",Type.PEDRA,55,58,62,32,new int[]{122,112,100},new int[]{172,152,122},new int[]{58,54,50},new int[]{100,90,80},
   Move.CASCALHO,Move.PEDRADA,Move.CASCA_DURA,Move.CABECADA_DE_PEDRA,Move.AVALANCHE,Move.TERREMOTO),
  VENTO("Galo Ventania",Type.VENTO,42,50,40,64,new int[]{236,236,242},new int[]{204,222,252},new int[]{118,158,222},new int[]{222,226,236},
   Move.CORTE_DE_VENTO,Move.RAJADA,Move.VOO_ALTO,Move.MERGULHO_AEREO,Move.FURACAO_DE_ASAS,Move.CICLONE),
  SOMBRA("Galo Sombrio",Type.SOMBRA,44,60,42,56,new int[]{42,30,56},new int[]{112,62,152},new int[]{14,10,20},new int[]{62,40,82},
   Move.SOMBRA_FURTIVA,Move.GARRA_SOMBRIA,Move.MEDO,Move.MALDICAO,Move.PESADELO,Move.ECLIPSE),
  /** 0.65.4: only hatches from the rare egg. Strong all-rounder with moves from several types. */
  DOURADO("Galo Dourado",Type.NORMAL,58,68,58,62,new int[]{226,176,36},new int[]{255,226,108},new int[]{150,98,18},new int[]{208,158,28},
   Move.BICADA,Move.VOADORA,Move.CANTO_DE_GUERRA,Move.RAIO_DO_GALO,Move.CRISTA_FLAMEJANTE,Move.ECLIPSE);
  public final String label;public final Type type;public final int hp,atk,def,spd;public final int[] body,neck,tail,wing;public final List<Move> moves;
  Breed(String l,Type t,int hp,int atk,int def,int spd,int[] body,int[] neck,int[] tail,int[] wing,Move... m){label=l;type=t;this.hp=hp;this.atk=atk;this.def=def;this.spd=spd;this.body=body;this.neck=neck;this.tail=tail;this.wing=wing;moves=List.of(m);}
  public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
  /** Moves known at a level: 2 at the start, one more at levels 3, 5, 7 and 9. */
  public List<Move> known(int lvl){int n=Math.min(moves.size(),2+(lvl>=3?1:0)+(lvl>=5?1:0)+(lvl>=7?1:0)+(lvl>=9?1:0));return moves.subList(0,n);}
  /** The (up to) four moves used in battle: the newest ones learned. */
  public List<Move> battleMoves(int lvl){var k=known(lvl);return k.subList(Math.max(0,k.size()-4),k.size());}
  public static Breed of(int i){var v=values();return v[Math.floorMod(i,v.length)];}
  /** Breeds that can come from random eggs / random opponents (everything except the rare golden one). */
  public static Breed common(net.minecraft.util.RandomSource r){return values()[r.nextInt(DOURADO.ordinal())];}
  public boolean rare(){return this==DOURADO;}
 }
}
