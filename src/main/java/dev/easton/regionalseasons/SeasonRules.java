package dev.easton.regionalseasons;

/** Pure V2 rules. These routines are independently tested without Minecraft dependencies. */
public final class SeasonRules {
    public static final int MAX_SNOW_LAYERS = 18;
    public static final double SNOW_BELOW_F = 35.0;
    private SeasonRules() {}
    public static boolean snows(double airF) { return Double.isFinite(airF) && airF < SNOW_BELOW_F; }
    public static int addLayer(int depth) { return Math.min(MAX_SNOW_LAYERS, Math.max(0, depth)+1); }
    public static double accumulationChance(int stormIntensity) {
        return switch (stormIntensity) { case 1 -> 0.18; case 2 -> 0.45; case 3 -> 0.85; default -> 0.0; };
    }
    public static double meltChance(double ambientF) {
        if (ambientF < 35.0) return 0.0;
        if (ambientF < 42.0) return 0.08;
        if (ambientF < 53.6) return 0.35;
        return 0.90;
    }
}
