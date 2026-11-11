package dev.easton.regionalseasons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SeasonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static volatile SeasonConfig INSTANCE = new SeasonConfig();

    // Server calendar and world simulation
    public int daysPerSeason = 14;
    public boolean enableSeasonalPrecipitation = true;
    public boolean enableSurfaceSnow = true;
    public boolean enableSeasonalIce = true;
    public boolean enableSeasonalThaw = true;
    // Hard snow cap is intentionally not configurable: 18 vanilla snow layers total (8+8+2).
    public boolean enableRareColdSnaps = true;
    public int coldSnapChanceOneInDays = 128;
    public double coldSnapExtremeShare = 0.02;
    public int lightStormWeight = 35;
    public int moderateStormWeight = 45;
    public int heavyStormWeight = 20;
    public boolean enableExposureEffects = true;
    public boolean enableStormRadio = true;
    public boolean enableWeatherRadar = true;
    public boolean enableHeatEffects = true;
    public int radioAlertCooldownSeconds = 90;
    public int radarRadiusBlocks = 1024; // presentation range, sampled using 32x32 coarse cells
    public double fanCoolingCelsius = 6;
    public double indoorTemperatureComfortCelsius = 20;
    public int environmentUpdateTicks = 40;
    public int samplesPerPlayer = 36;
    public int updateRadiusBlocks = 32;
    public double snowfallChancePerSample = 0.42;
    public double frostChancePerSample = 0.08;
    public double freezeChancePerSample = 0.16;

    // Climate: vanilla 0.8 biome temperature corresponds roughly to 16 C.
    // Default multipliers are small in hot climates and strong in temperate climates.
    public double seaLevelReference = 64.0;
    public double degreesCelsiusLostPerBlockAboveSeaLevel = 0.085;
    public double dayNightTemperatureRangeCelsius = 4.0;
    public double rainfallCoolingCelsius = 1.5;
    public double[] temperateOffsetsCelsius = {2.0, 11.0, -2.0, -14.0};
    public double[] coldOffsetsCelsius = {3.0, 9.0, -1.0, -10.0};
    public double[] tropicalOffsetsCelsius = {0.0, 2.0, 0.0, -1.0};
    public double[] aridOffsetsCelsius = {2.0, 8.0, 1.0, -4.0};
    public Map<String, Double> biomeBaseTemperatureCelsiusOverrides = new LinkedHashMap<>();
    public Map<String, Double> biomeSeasonStrengthOverrides = new LinkedHashMap<>();

    // V2: temperature and snow controls. Snow cap remains hard-coded to 18.
    public double waterCoolingCelsius = 6.0;
    public double icyWaterExtraCoolingCelsius = 5.0;
    public double shadeCoolingCelsius = 1.4;
    public double nearIceCoolingCelsius = 1.8;
    public double snowThresholdFahrenheit = 35.0;
    public double snowUnderLeavesChance = 0.32;
    public double snowOnLeavesChance = 0.10;
    public double maxSnowMeltingMultiplier = 0.9;
    public boolean allowSnowUnderLeaves = true;
    public boolean showForecastCalendar = true;
    public int hudX = 7;
    public int hudY = 7;

    // Client preferences (configure on each player's computer)
    public boolean showClimateHud = true;
    public boolean showFahrenheit = true;

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("regional-seasons.json");
    }

    public static void reload() {
        Path path = path();
        try {
            if (Files.exists(path)) {
                SeasonConfig parsed = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), SeasonConfig.class);
                if (parsed != null) INSTANCE = sanitize(parsed);
            } else {
                INSTANCE = new SeasonConfig();
                write();
            }
        } catch (Exception e) {
            RegionalSeasons.LOGGER.warn("Could not load {}, using previous/default config", path, e);
        }
    }

    private static SeasonConfig sanitize(SeasonConfig c) {
        c.daysPerSeason = Math.clamp(c.daysPerSeason, 1, 365);
        c.coldSnapChanceOneInDays = Math.clamp(c.coldSnapChanceOneInDays, 1, 100000);
        c.coldSnapExtremeShare = Math.clamp(c.coldSnapExtremeShare, 0, 1);
        c.lightStormWeight = Math.clamp(c.lightStormWeight, 0, 100);
        c.moderateStormWeight = Math.clamp(c.moderateStormWeight, 0, 100);
        c.heavyStormWeight = Math.clamp(c.heavyStormWeight, 0, 100);
        c.environmentUpdateTicks = Math.clamp(c.environmentUpdateTicks, 20, 12000);
        c.samplesPerPlayer = Math.clamp(c.samplesPerPlayer, 0, 200);
        c.updateRadiusBlocks = Math.clamp(c.updateRadiusBlocks, 8, 128);
        c.snowfallChancePerSample = Math.clamp(c.snowfallChancePerSample, 0, 1);
        c.frostChancePerSample = Math.clamp(c.frostChancePerSample, 0, 1);
        c.freezeChancePerSample = Math.clamp(c.freezeChancePerSample, 0, 1);
        c.degreesCelsiusLostPerBlockAboveSeaLevel = Math.clamp(c.degreesCelsiusLostPerBlockAboveSeaLevel, 0, 1);
        c.waterCoolingCelsius = Math.clamp(c.waterCoolingCelsius, 0.0, 20.0);
        c.icyWaterExtraCoolingCelsius = Math.clamp(c.icyWaterExtraCoolingCelsius, 0.0, 20.0);
        c.shadeCoolingCelsius = Math.clamp(c.shadeCoolingCelsius, 0.0, 10.0);
        c.nearIceCoolingCelsius = Math.clamp(c.nearIceCoolingCelsius, 0.0, 10.0);
        c.snowThresholdFahrenheit = Math.clamp(c.snowThresholdFahrenheit, 10.0, 38.0);
        c.snowUnderLeavesChance = Math.clamp(c.snowUnderLeavesChance, 0.0, 1.0);
        c.snowOnLeavesChance = Math.clamp(c.snowOnLeavesChance, 0.0, 1.0);
        c.maxSnowMeltingMultiplier = Math.clamp(c.maxSnowMeltingMultiplier, 0.0, 1.0);
        c.hudX = Math.clamp(c.hudX, 0, 300);
        c.hudY = Math.clamp(c.hudY, 0, 300);
        c.radioAlertCooldownSeconds = Math.clamp(c.radioAlertCooldownSeconds, 10, 3600);
        c.radarRadiusBlocks = 1024; // fixed radius for predictable performance
        c.fanCoolingCelsius = Math.clamp(c.fanCoolingCelsius, 0, 12);
        c.indoorTemperatureComfortCelsius = Math.clamp(c.indoorTemperatureComfortCelsius, 0, 30);
        c.dayNightTemperatureRangeCelsius = Math.clamp(c.dayNightTemperatureRangeCelsius, 0, 25);
        c.rainfallCoolingCelsius = Math.clamp(c.rainfallCoolingCelsius, 0, 25);
        c.temperateOffsetsCelsius = validOffsets(c.temperateOffsetsCelsius, new double[]{2, 11, -2, -14});
        c.coldOffsetsCelsius = validOffsets(c.coldOffsetsCelsius, new double[]{3, 9, -1, -10});
        c.tropicalOffsetsCelsius = validOffsets(c.tropicalOffsetsCelsius, new double[]{0, 2, 0, -1});
        c.aridOffsetsCelsius = validOffsets(c.aridOffsetsCelsius, new double[]{2, 8, 1, -4});
        if (c.biomeBaseTemperatureCelsiusOverrides == null) c.biomeBaseTemperatureCelsiusOverrides = new LinkedHashMap<>();
        if (c.biomeSeasonStrengthOverrides == null) c.biomeSeasonStrengthOverrides = new LinkedHashMap<>();
        return c;
    }

    private static double[] validOffsets(double[] offsets, double[] fallback) {
        return offsets != null && offsets.length == 4 ? offsets : fallback;
    }

    public static void write() throws IOException {
        Path path = path();
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(INSTANCE), StandardCharsets.UTF_8);
    }
}
