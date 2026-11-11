package dev.easton.regionalseasons;

import dev.easton.regionalseasons.network.ForecastPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/** A deterministic approximate outlook, clearly distinct from vanilla's future random weather. */
public final class RegionalForecast {
    private RegionalForecast() {}
    public static ForecastPayload forPlayer(ServerPlayerEntity player) {
        var world = player.getServerWorld();
        var server = player.getServer();
        BlockPos pos = player.getBlockPos();
        double currentC = ClimateMath.temperatureC(world, pos);
        double noonC = currentC + (world.isDay() ? 0.0 : 2.2);
        String[] days = new String[3];
        int[] highsF = new int[3];
        int[] lowsF = new int[3];
        int[] precipitation = new int[3];
        for (int i=0;i<3;i++) {
            // Deterministic approximate outlook, not a guarantee for future vanilla rain RNG.
            long hash = world.getSeed() ^ (world.getTime()/24000L + i)*0x9E3779B97F4A7C15L
                    ^ ((long)pos.getX()/96)*0xC2B2AE3D27D4EB4FL;
            int probability = (int)Math.floorMod(hash ^ (hash >>> 32), 66) + 16;
            double drift = Math.sin((world.getTime()/24000.0+i)*0.83 + pos.getX()/300.0) * 2.5;
            highsF[i] = (int)Math.round(ClimateMath.cToF(noonC + drift + 2.8));
            lowsF[i] = (int)Math.round(ClimateMath.cToF(noonC + drift - 4.0));
            precipitation[i] = probability;
            boolean snow = highsF[i] < SeasonConfig.INSTANCE.snowThresholdFahrenheit;
            days[i] = (i==0 ? "Today" : i==1 ? "Tomorrow" : "Day +2")
                    + " · " + (probability>=55 ? (snow?"Snow likely":"Rain possible") : "Partly cloudy");
        }
        int localStorm=RadarMath.intensity(pos.getX(),pos.getZ(),world.getTime(),WorldCalendar.radarSeed(),world.isRaining());
        double airF=ClimateMath.cToF(ClimateMath.precipitationC(world.getBiome(pos).value().getTemperature(),pos));
        String current = (localStorm>0 && world.getBiome(pos).value().hasPrecipitation()
                ? (SeasonRules.snows(airF)?"Snowing":"Raining") : "Dry")
                + " · " + ClimateMath.formatTemperature(currentC);
        return new ForecastPayload(WorldCalendar.season(server).display,
                WorldCalendar.dayOfSeason(server), WorldCalendar.year(server), current, days, highsF, lowsF, precipitation);
    }
}

