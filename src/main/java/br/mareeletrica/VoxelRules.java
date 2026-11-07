package br.mareeletrica;
public final class VoxelRules {
 public static final int MAX_EDGE=128,MAX_VOLUME=524288,MAX_CELLS=65536;
 private VoxelRules(){}
 public static int key(int x,int y,int z){return x|(y<<7)|(z<<14);}
 public static int x(int k){return k&127;}public static int y(int k){return k>>7&127;}public static int z(int k){return k>>14&127;}
 public static int sample(int p,int old,int size){return Math.min(old-1,p*old/size);}
 public static boolean validSize(int x,int y,int z){return x>0&&y>0&&z>0&&x<=128&&y<=128&&z<=128&&(long)x*y*z<=MAX_VOLUME;}
}
