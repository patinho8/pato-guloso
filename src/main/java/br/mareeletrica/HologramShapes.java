package br.mareeletrica;

import java.util.ArrayList;
import java.util.List;

/** 128 bounded styles; shared by collision and exported translucent meshes. */
public final class HologramShapes {
    private HologramShapes() {}
    private static void add(List<double[]> out,double a,double b,double c,double d,double e,double f) { out.add(new double[]{a,b,c,d,e,f}); }
    public static List<double[]> boxes(int style) {
        List<double[]> out=new ArrayList<>();
        if(style==2||style==3) add(out,0,style==2?0:8,0,16,style==2?8:16,16);
        else if(style==4||style==104) add(out,0,0,0,16,style==4?2:1,16);
        else if(style>=8&&style<48) {
            int code=style-8,turn=code%4,shape=code/8;boolean top=code/4%2==1;
            add(out,0,0,0,16,8,16);
            if(shape<=2) add(out,0,8,0,16,16,8);
            if(shape==1)add(out,0,8,8,8,16,16);
            if(shape==2)add(out,8,8,8,16,16,16);
            if(shape==3)add(out,0,8,0,8,16,8);
            if(shape==4)add(out,8,8,0,16,16,8);
            List<double[]> rotated=new ArrayList<>();
            for(double[] b:out) {
                if(top) {double y=b[1];b[1]=16-b[4];b[4]=16-y;}
                rotated.add(rotate(b,turn));
            }
            return rotated;
        } else if(style>=48&&style<96) {
            int mask=(style-48)%16,kind=(style-48)/16;
            double lo=kind==0?6:kind==1?4:7,hi=16-lo;
            add(out,lo,0,lo,hi,16,hi);
            for(int direction=0;direction<4;direction++) if((mask&(1<<direction))!=0) {
                if(kind==0) { out.add(rotate(new double[]{7,5,0,9,8,6},direction));out.add(rotate(new double[]{7,12,0,9,15,6},direction)); }
                else out.add(rotate(new double[]{lo,0,0,hi,kind==1?14:16,lo},direction));
            }
        } else if(style>=96&&style<104) {
            int turn=(style-96)%4+(style>=100?1:0);out.add(rotate(new double[]{0,0,13,16,16,16},turn));
        } else if(style==105) add(out,6,0,6,10,11,10);
        else if(style==106) add(out,0,0,0,16,9,16);
        else if(style==107) add(out,0,0,0,16,15,16);
        else if(style==108) { add(out,3,0,7,13,12,9);add(out,7,0,3,9,12,13); }
        else if(style==109) add(out,5,0,5,11,9,11);
        else if(style==110||style==111) add(out,0,style==110?0:13,0,16,style==110?3:16,16);
        else add(out,0,0,0,16,16,16);
        return out;
    }
    private static double[] rotate(double[] b,int turn) {
        return switch(Math.floorMod(turn,4)) {
            case 1->new double[]{16-b[5],b[1],b[0],16-b[2],b[4],b[3]};
            case 2->new double[]{16-b[3],b[1],16-b[5],16-b[0],b[4],16-b[2]};
            case 3->new double[]{b[2],b[1],16-b[3],b[5],b[4],16-b[0]};
            default->b.clone();
        };
    }
}
