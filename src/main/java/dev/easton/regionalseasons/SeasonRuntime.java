package dev.easton.regionalseasons;

/** Server authority is forwarded by network packet to clients that have the mod. */
public final class SeasonRuntime {
    private SeasonRuntime() {}
    public static volatile Season current = Season.SPRING;
    public static volatile int dayOfSeason = 1;
    public static volatile int seasonLength = 14;
    public static volatile boolean synced = false;
    public static volatile double coldSnapDegreesC = 0;
    public static volatile long radarSeed = 0;
    public static volatile long radarTimeTicks = 0;
    public static volatile boolean isRaining = false;

    public static void update(Season season, int day, int length) {
        update(season, day, length, coldSnapDegreesC);
    }

    public static void update(Season season, int day, int length, double coldSnap) {
        coldSnapDegreesC = coldSnap;
        current = season;
        dayOfSeason = day;
        seasonLength = length;
        synced = true;
    }
    public static void updateRadar(long seed,long time,boolean raining) {
        radarSeed=seed; radarTimeTicks=time; isRaining=raining;
    }
}

