package cern.rade.services.japc.stats;

/**
 * Counts events per second over the last minute, plus a running total.
 */
final class RateWindow {
    static final int SECONDS = 60;
    private final long[] seconds = new long[SECONDS];
    private final long[] counts = new long[SECONDS];
    private long total;

    synchronized void record(long epochSecond) {
        int slot = Math.floorMod(epochSecond, SECONDS);
        if (seconds[slot] != epochSecond) {
            seconds[slot] = epochSecond;
            counts[slot] = 0;
        }
        counts[slot]++;
        total++;
    }

    synchronized long lastMinute(long nowEpochSecond) {
        long sum = 0;
        for (int slot = 0; slot < SECONDS; slot++) {
            long age = nowEpochSecond - seconds[slot];
            if (age >= 0 && age < SECONDS) {
                sum += counts[slot];
            }
        }
        return sum;
    }

    synchronized long total() {
        return total;
    }
}
