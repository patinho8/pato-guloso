package br.mareeletrica;

/** Energy policy shared by the server projector, menu and offline boundary tests. */
public final class BridgePower {
    public static final int CAPACITY = 100_000;
    public static final int WIDTH = 3;
    public static final int MAX_LENGTH = 60;
    private BridgePower() {}

    /** Six blocks per tier, with a 10,000 FE reserve band above the top threshold. */
    public static int reach(int stored) {
        if (stored < tickCost(6)) return 0;
        return 6 * (1 + Math.min(9, stored / 10_000));
    }

    /** Called twice per second. Charge for the actual length, including shared tiles. */
    public static int tickCost(int length) {
        return length <= 0 ? 0 : 20 + 10 * Math.min(MAX_LENGTH, length);
    }
}
