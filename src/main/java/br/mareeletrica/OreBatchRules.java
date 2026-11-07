package br.mareeletrica;
public final class OreBatchRules {
 public static final int[] WEIGHTS={540000,420000,320000,650000,30000,6000,2000};public static final int TOTAL=1968000;
 private OreBatchRules(){}public static int amount(int gas){return 100+100*Math.max(0,Math.min(10,gas));}
 public static int select(int roll){if(roll<0||roll>=TOTAL)throw new IllegalArgumentException();for(int i=0;i<WEIGHTS.length;i++){if(roll<WEIGHTS[i])return i;roll-=WEIGHTS[i];}throw new AssertionError();}
}
