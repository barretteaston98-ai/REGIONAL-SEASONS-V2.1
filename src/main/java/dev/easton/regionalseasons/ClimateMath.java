package dev.easton.regionalseasons;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import java.util.Locale;

/** Ambient air temperature, environmental feels-like temperature, and seasonal precipitation. */
public final class ClimateMath {
    public enum Climate { TEMPERATE, COLD, TROPICAL, ARID }
    private ClimateMath() {}

    public static Climate classify(float vanillaTemp, String biomeId) {
        String id = biomeId.toLowerCase(Locale.ROOT);
        if (has(id, "desert", "badlands", "savanna", "mesa")) return Climate.ARID;
        if (has(id, "jungle", "rainforest", "tropical", "mangrove")) return Climate.TROPICAL;
        if (has(id, "snow", "frozen", "ice", "grove", "taiga", "peaks")) return Climate.COLD;
        if (vanillaTemp < 0.25f) return Climate.COLD;
        if (vanillaTemp > 1.45f) return Climate.ARID;
        if (vanillaTemp >= 0.90f) return Climate.TROPICAL;
        return Climate.TEMPERATE;
    }

    private static boolean has(String id, String... terms) {
        for (String term : terms) if (id.contains(term)) return true;
        return false;
    }

    public static double baseC(float vanillaTemp, Climate climate) {
        double c = 16.0 + (vanillaTemp - 0.8) * 25.0;
        return climate == Climate.TROPICAL ? Math.max(c, 25.0) : c;
    }

    public static double offset(Climate climate, Season season) {
        SeasonConfig c = SeasonConfig.INSTANCE;
        return switch (climate) {
            case TEMPERATE -> c.temperateOffsetsCelsius[season.ordinal()];
            case COLD -> c.coldOffsetsCelsius[season.ordinal()];
            case TROPICAL -> c.tropicalOffsetsCelsius[season.ordinal()];
            case ARID -> c.aridOffsetsCelsius[season.ordinal()];
        };
    }

    private static double seasonal(Climate climate) {
        // Fade each new season in for its first three days to avoid hard day-boundary jumps.
        Season current = SeasonRuntime.current;
        Season previous = Season.fromIndex(current.ordinal() - 1);
        double progress = Math.clamp(SeasonRuntime.dayOfSeason / 3.0, 0.0, 1.0);
        progress = progress * progress * (3 - 2 * progress);
        return offset(climate, previous) * (1 - progress) + offset(climate, current) * progress;
    }

    public static double precipitationC(float vanillaTemp, BlockPos pos) {
        Climate climate = classify(vanillaTemp, "");
        SeasonConfig c = SeasonConfig.INSTANCE;
        // Intentionally excludes water/shade effects: precipitation follows AIR temperature.
        return baseC(vanillaTemp, climate) + seasonal(climate)
                - Math.max(0, pos.getY() - c.seaLevelReference) * c.degreesCelsiusLostPerBlockAboveSeaLevel
                + SeasonRuntime.coldSnapDegreesC;
    }

    public static double temperatureC(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos).value();
        String id = world.getBiome(pos).getKey().map(k -> k.getValue().toString()).orElse("unknown");
        Climate climate = classify(biome.getTemperature(), id);
        SeasonConfig c = SeasonConfig.INSTANCE;
        double base = c.biomeBaseTemperatureCelsiusOverrides.getOrDefault(id, baseC(biome.getTemperature(), climate));
        double strength = c.biomeSeasonStrengthOverrides.getOrDefault(id, 1.0);
        double season = seasonal(climate) * Math.clamp(strength, 0.0, 5.0);
        double altitude = -Math.max(0, pos.getY() - c.seaLevelReference) * c.degreesCelsiusLostPerBlockAboveSeaLevel;
        double localTime = Math.floorMod(world.getTimeOfDay(), 24000L);
        double dayCycle = Math.cos(2.0 * Math.PI * (localTime - 6000.0) / 24000.0)
                * c.dayNightTemperatureRangeCelsius;
        double rainCooling = world.isRaining() && biome.hasPrecipitation() ? -c.rainfallCoolingCelsius : 0;
        return base + season + altitude + dayCycle + rainCooling + SeasonRuntime.coldSnapDegreesC;
    }

    public static double feelsLikeC(World world, BlockPos pos) {
        double result = temperatureC(world, pos);
        SeasonConfig cfg = SeasonConfig.INSTANCE;
        boolean inWater = world.getFluidState(pos).isIn(FluidTags.WATER)
                || world.getFluidState(pos.up()).isIn(FluidTags.WATER);
        boolean nearIce = false;
        for (int dx = -2; dx <= 2 && !nearIce; dx++) {
            for (int dz = -2; dz <= 2 && !nearIce; dz++) {
                BlockPos near = pos.add(dx, 0, dz);
                nearIce = world.getBlockState(near).isOf(Blocks.ICE)
                        || world.getBlockState(near).isOf(Blocks.PACKED_ICE)
                        || world.getBlockState(near).isOf(Blocks.BLUE_ICE)
                        || world.getBlockState(near).isOf(Blocks.FROSTED_ICE);
            }
        }
        if (inWater) result -= cfg.waterCoolingCelsius + (nearIce ? cfg.icyWaterExtraCoolingCelsius : 0);
        else if (nearIce) result -= cfg.nearIceCoolingCelsius;
        if (world.isDay() && !world.isSkyVisible(pos.up()) && hasLeavesOverhead(world, pos)) {
            result -= cfg.shadeCoolingCelsius;
        }
        // Roofed rooms moderate outdoor temperature instead of magically creating a cold snap.
        boolean indoors=!world.isSkyVisible(pos.up()) && !hasLeavesOverhead(world,pos);
        if (indoors) result += (cfg.indoorTemperatureComfortCelsius-result)*.32;
        double localHeat=0;
        double localCool=0;
        // 5x5x5 bounded local heat scan, evaluated on the HUD's 3-second sampling cycle.
        for(int dx=-2;dx<=2;dx++) for(int dy=-1;dy<=2;dy++) for(int dz=-2;dz<=2;dz++) {
            BlockState s=world.getBlockState(pos.add(dx,dy,dz));
            double ds=Math.sqrt(dx*dx+dy*dy+dz*dz);
            double falloff=1.0/(1.0+ds);
            if(s.isOf(Blocks.LAVA)) localHeat=Math.max(localHeat,30*falloff);
            else if(s.isOf(Blocks.FIRE)||s.isOf(Blocks.SOUL_FIRE)) localHeat=Math.max(localHeat,16*falloff);
            else if(s.isOf(Blocks.CAMPFIRE)||s.isOf(Blocks.SOUL_CAMPFIRE)) localHeat=Math.max(localHeat,12*falloff);
            else if(s.isOf(Blocks.TORCH)||s.isOf(Blocks.WALL_TORCH)) localHeat=Math.max(localHeat,4*falloff);
            else if(s.isOf(RegionalBlocks.AC_FAN)) localCool=Math.max(localCool,cfg.fanCoolingCelsius*falloff);
        }
        result+=localHeat-localCool;
        return result;
    }

    private static boolean hasLeavesOverhead(World world, BlockPos pos) {
        for (int y = 1; y <= 9; y++) {
            if (world.getBlockState(pos.up(y)).isIn(BlockTags.LEAVES)) return true;
        }
        return false;
    }

    public static double cToF(double c) { return c * 1.8 + 32.0; }
    public static double fToC(double f) { return (f - 32.0) / 1.8; }
    public static String formatTemperature(double c) {
        if (SeasonConfig.INSTANCE.showFahrenheit) return Math.round(cToF(c)) + "°F";
        return Math.round(c) + "°C";
    }
}

