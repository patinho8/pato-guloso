package br.mareeletrica;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/** Horizontal fan of lighting anchors; no Minecraft dependency so occlusion can be tested. */
public final class BeamPattern {
    public record Point(int forward, int sideways) {}
    private BeamPattern() {}
    public static List<Point> points() {
        List<Point> result = new ArrayList<>();
        for (int distance = 4; distance <= 16; distance += 4) {
            int width = distance == 4 ? 0 : distance == 8 ? 4 : 8;
            for (int side = -width; side <= width; side += 4) result.add(new Point(distance, side));
        }
        return List.copyOf(result);
    }
    public static List<Point> ray(Point target) {
        var cells = new LinkedHashSet<Point>();
        int steps = 4 * Math.max(Math.abs(target.forward()), Math.abs(target.sideways()));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / Math.max(1, steps);
            cells.add(new Point((int) Math.floor(0.5 + target.forward() * t), (int) Math.floor(0.5 + target.sideways() * t)));
        }
        return List.copyOf(cells);
    }
    public static List<Point> visible(Predicate<Point> clear) {
        var cache = new HashMap<Point, Boolean>();
        List<Point> visible = new ArrayList<>();
        for (Point target : points()) {
            boolean valid = true;
            for (Point cell : ray(target)) {
                if (!cache.computeIfAbsent(cell, clear::test)) { valid = false; break; }
            }
            if (valid) visible.add(target);
        }
        return List.copyOf(visible);
    }
}
