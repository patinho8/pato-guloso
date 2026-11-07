package br.mareeletrica;

/** Pure hydration progression; one step is one server second. */
public final class HydrationRules {
    public static final int MAX = 20, POINT_SECONDS = 90;
    private HydrationRules() {}
    public record State(int water, int effort) {
        public State { water=Math.max(0,Math.min(MAX,water)); effort=Math.max(0,Math.min(POINT_SECONDS-1,effort)); }
    }
    public static State step(State old, boolean running, boolean hot, boolean exempt) {
        if (exempt) return new State(MAX,0);
        int effort=old.effort()+1+(running?2:0)+(hot?1:0);
        return new State(Math.max(0,old.water()-effort/POINT_SECONDS),effort%POINT_SECONDS);
    }
    public static State drink(State old, int amount) { return new State(old.water()+Math.max(0,amount),0); }
}
