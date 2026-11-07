package br.mareeletrica;

/** Integer-only storage. No generation, conversion, or implicit overfilling. */
public final class EnergyTank {
    private final int capacity;
    private int stored;
    public EnergyTank(int capacity) { this.capacity = Math.max(0, capacity); }
    public int stored() { return stored; }
    public int capacity() { return capacity; }
    public int room() { return capacity - stored; }
    public void load(int value) { stored = Math.max(0, Math.min(capacity, value)); }
    public int add(int amount) {
        int accepted = Math.min(room(), Math.max(0, amount));
        stored += accepted; return accepted;
    }
    public int take(int amount) {
        int removed = Math.min(stored, Math.max(0, amount));
        stored -= removed; return removed;
    }
    public boolean pay(int amount) {
        if (amount < 0 || stored < amount) return false;
        stored -= amount; return true;
    }
    public int moveTo(EnergyTank target, int limit) {
        if (target == this) return 0;
        int moved = Math.min(Math.max(0, limit), Math.min(stored, target.room()));
        stored -= moved; target.stored += moved;
        return moved;
    }
    /** Purification consumes five dirty units for every four usable units. */
    public int purifyInto(EnergyTank target, int dirtyLimit) {
        if (target == this) return 0;
        int groups = Math.min(Math.min(stored, Math.max(0, dirtyLimit)) / 5, target.room() / 4);
        stored -= groups * 5; target.stored += groups * 4;
        return groups * 5;
    }
}
