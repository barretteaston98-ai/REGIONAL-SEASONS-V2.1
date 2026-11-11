package dev.easton.regionalseasons;

import java.util.Random;

/** Game mechanics only: cold duration, exposure chances and recovery, with no Minecraft dependencies. */
public final class ExposureRules {
    public static final int TICKS_PER_SECOND = 20;
    public static final int COLD_MIN_TICKS = 7 * 60 * TICKS_PER_SECOND;
    public static final int COLD_MAX_TICKS = 10 * 60 * TICKS_PER_SECOND;
    public static final int IMMUNITY_TICKS = 2 * 60 * TICKS_PER_SECOND;
    public static final int EXPOSURE_ROLL_TICKS = 30 * TICKS_PER_SECOND;
    public static final int HELMET_MAX_ROLL_TICKS = 45 * TICKS_PER_SECOND;
    public static final double FULL_ARMOR_CHANCE = 0.0005;
    public static final double UNPROTECTED_CHANCE = 0.04;
    public static final double HELMET_CHANCE = 0.01;
    public static final double TRANSMISSION_CHANCE = 0.75;
    private ExposureRules() {}

    public static int coldDurationTicks(Random random) {
        return COLD_MIN_TICKS + random.nextInt(COLD_MAX_TICKS - COLD_MIN_TICKS + 1);
    }
    public static boolean atRisk(double apparentF) { return Double.isFinite(apparentF) && apparentF < 28.0; }
    public static double chancePerExposureRoll(boolean helmet) {
        return helmet ? HELMET_CHANCE : UNPROTECTED_CHANCE;
    }
    public static boolean rollsToInfect(double roll, boolean helmet) {
        return roll < chancePerExposureRoll(helmet);
    }
}
