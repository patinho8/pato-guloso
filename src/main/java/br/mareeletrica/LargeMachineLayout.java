package br.mareeletrica;

import java.util.ArrayList;
import java.util.List;

/** Bounded multiblock layout for 6 x 6 x 8 drills and 7 x 7 x 7 wheels. */
public final class LargeMachineLayout {
    public static final int PARTS = 288;
    private LargeMachineLayout() {}
    public static int[] offset(int part,int turn) { return offset(part,turn,3); }
    public static int[] offset(int part, int turn,int width) {
        if (part < 0 || part >= Math.max(PARTS,343) || width < 1 || width > 7) throw new IllegalArgumentException("part or width");
        int x=part%width, y=part/(width*width), z=part/width%width;
        return switch (Math.floorMod(turn, 4)) {
            case 1 -> new int[] {-z, y, x};
            case 2 -> new int[] {-x, y, -z};
            case 3 -> new int[] {z, y, -x};
            default -> new int[] {x, y, z};
        };
    }
    public static List<double[]> boxes(double[][] geometry,int part,int turn) { return boxes(geometry,part,turn,3); }
    public static List<double[]> boxes(double[][] geometry, int part, int turn,int width) {
        int[] o = offset(part, 0,width);
        List<double[]> result = new ArrayList<>();
        for (double[] b : geometry) {
            double x0 = Math.max(0, b[0]-o[0]*16), y0 = Math.max(0, b[1]-o[1]*16), z0 = Math.max(0, b[2]-o[2]*16);
            double x1 = Math.min(16, b[3]-o[0]*16), y1 = Math.min(16, b[4]-o[1]*16), z1 = Math.min(16, b[5]-o[2]*16);
            if (x0 >= x1 || y0 >= y1 || z0 >= z1) continue;
            result.add(switch (Math.floorMod(turn, 4)) {
                case 1 -> new double[] {16-z1, y0, x0, 16-z0, y1, x1};
                case 2 -> new double[] {16-x1, y0, 16-z1, 16-x0, y1, 16-z0};
                case 3 -> new double[] {z0, y0, 16-x1, z1, y1, 16-x0};
                default -> new double[] {x0, y0, z0, x1, y1, z1};
            });
        }
        return result;
    }
}
