package br.mareeletrica;

/** Pure processing rules; one call represents ten server ticks. */
public final class PotableWaterCycle {
    public static final int DURATION = 80;
    public static final int FILTER_USES = 8;
    private PotableWaterCycle() {}
    public record State(int progress, int filter, int profile) {
        public State {
            progress = Math.max(0, Math.min(DURATION - 10, progress));
            filter = Math.max(0, Math.min(FILTER_USES, filter));
            profile = Math.max(0, Math.min(2, profile));
        }
    }
    public record Step(State state, int energy, int charcoal, boolean bottle, int status) {}
    public static Step step(State old, int water, int energy, boolean glass, boolean charcoal, boolean room) {
        water = Math.max(0, Math.min(2, water));
        int progress = old.profile() == water ? old.progress() : 0;
        State current = new State(progress, old.filter(), water);
        if (water == 0) return new Step(new State(0, old.filter(), 0), 0, 0, false, 26);
        if (!glass) return new Step(new State(0, old.filter(), water), 0, 0, false, 6);
        if (!room) return new Step(current, 0, 0, false, 7);
        if (old.filter() == 0 && !charcoal) return new Step(current, 0, 0, false, 5);
        int fee = water == 2 ? 200 : 100;
        if (energy < fee) return new Step(current, 0, 0, false, 2);
        int filter = old.filter(), consumed = 0;
        if (filter == 0) { filter = FILTER_USES; consumed = 1; }
        progress += 10;
        boolean complete = progress >= DURATION;
        if (complete) { progress = 0; filter--; }
        return new Step(new State(progress, filter, water), fee, consumed, complete, water == 2 ? 32 : 31);
    }
}
