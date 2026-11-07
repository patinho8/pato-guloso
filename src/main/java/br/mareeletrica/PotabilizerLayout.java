package br.mareeletrica;

import java.util.ArrayList;
import java.util.List;

/** Shared 2 x 2 x 3 layout. Turns are NORTH, EAST, SOUTH, WEST, in that order. */
public final class PotabilizerLayout {
    public static final int PARTS = 12;
    private PotabilizerLayout() {}
    public static int[] offset(int part, int turn) {
        if (part < 0 || part >= PARTS) throw new IllegalArgumentException("part");
        int x = part % 2, y = part / 4, z = (part / 2) % 2;
        return switch (Math.floorMod(turn, 4)) {
            case 1 -> new int[] {-z, y, x};
            case 2 -> new int[] {-x, y, -z};
            case 3 -> new int[] {z, y, -x};
            default -> new int[] {x, y, z};
        };
    }
    public static List<double[]> boxes(int part, int turn) {
        int[] o = offset(part, 0);
        List<double[]> result = new ArrayList<>();
        for (double[] b : PotabilizerGeometry.BOXES) {
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
