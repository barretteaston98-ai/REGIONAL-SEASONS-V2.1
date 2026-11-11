package dev.easton.regionalseasons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Per-save day offset. World.getTime() is play time, not clock time. */
public final class WorldCalendar {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static long offsetDays;
    private static MinecraftServer owner;
    private static long lastClimateDay = Long.MIN_VALUE;
    private static long coldSnapEndsAtDay = Long.MIN_VALUE;
    private static double coldSnapDegreesC;
    private static long radarSeed;

    private static final class Data {
        long offsetDays;
        long lastClimateDay = Long.MIN_VALUE;
        long coldSnapEndsAtDay = Long.MIN_VALUE;
        double coldSnapDegreesC;
        long radarSeed;
    }

    private WorldCalendar() {}

    private static Path path(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve("regional-seasons-calendar.json");
    }

    public static void load(MinecraftServer server) {
        owner = server;
        offsetDays = 0;
        lastClimateDay = Long.MIN_VALUE;
        coldSnapEndsAtDay = Long.MIN_VALUE;
        coldSnapDegreesC = 0;
        radarSeed = 0;
        try {
            Path path = path(server);
            if (Files.exists(path)) {
                Data data = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), Data.class);
                if (data != null) {
                    offsetDays = data.offsetDays;
                    lastClimateDay = data.lastClimateDay;
                    coldSnapEndsAtDay = data.coldSnapEndsAtDay;
                    coldSnapDegreesC = data.coldSnapDegreesC;
                    radarSeed = data.radarSeed;
                }
            }
        } catch (Exception e) {
            RegionalSeasons.LOGGER.warn("Could not load seasonal calendar", e);
        }
        if (radarSeed == 0) { radarSeed = server.getOverworld().random.nextLong(); save(server); }
        syncRuntime(server);
    }

    public static void save(MinecraftServer server) {
        try {
            Path path = path(server);
            Files.createDirectories(path.getParent());
            Data data = new Data();
            data.offsetDays = offsetDays;
            data.lastClimateDay = lastClimateDay;
            data.coldSnapEndsAtDay = coldSnapEndsAtDay;
            data.coldSnapDegreesC = coldSnapDegreesC;
            data.radarSeed = radarSeed;
            Files.writeString(path, GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            RegionalSeasons.LOGGER.warn("Could not save seasonal calendar", e);
        }
    }

    private static long days(MinecraftServer server) {
        return Math.floorDiv(server.getOverworld().getTime(), 24000L) + offsetDays;
    }

    public static Season season(MinecraftServer server) {
        long day = Math.floorMod(days(server), (long) SeasonConfig.INSTANCE.daysPerSeason * 4);
        return Season.fromIndex((int) (day / SeasonConfig.INSTANCE.daysPerSeason));
    }

    public static int dayOfSeason(MinecraftServer server) {
        return (int) Math.floorMod(days(server), SeasonConfig.INSTANCE.daysPerSeason) + 1;
    }

    public static long year(MinecraftServer server) {
        return Math.floorDiv(days(server), SeasonConfig.INSTANCE.daysPerSeason * 4L) + 1;
    }

    public static void set(MinecraftServer server, Season season) {
        int length = SeasonConfig.INSTANCE.daysPerSeason;
        long desired = (long) season.ordinal() * length;
        long current = Math.floorMod(days(server), 4L * length);
        offsetDays += desired - current;
        save(server);
        syncRuntime(server);
    }

    public static void skip(MinecraftServer server, int days) {
        offsetDays += days;
        save(server);
        syncRuntime(server);
    }

    public static double coldSnapDegreesC() { return coldSnapDegreesC; }
    public static long radarSeed() { return radarSeed; }

    public static void advanceClimate(MinecraftServer server) {
        long now = Math.floorDiv(server.getOverworld().getTime(), 24000L);
        if (now <= lastClimateDay) return;
        lastClimateDay = now;
        if (coldSnapEndsAtDay <= now) coldSnapDegreesC = 0;
        SeasonConfig config = SeasonConfig.INSTANCE;
        if (config.enableRareColdSnaps && coldSnapDegreesC == 0
                && server.getOverworld().random.nextInt(config.coldSnapChanceOneInDays) == 0) {
            double roll = server.getOverworld().random.nextDouble();
            double drop = roll < config.coldSnapExtremeShare ? (35 + server.getOverworld().random.nextInt(14))
                    : roll < 0.25 ? (23 + server.getOverworld().random.nextInt(12))
                    : (10 + server.getOverworld().random.nextInt(13));
            coldSnapDegreesC = -drop;
            coldSnapEndsAtDay = now + 1 + server.getOverworld().random.nextInt(3);
            server.getPlayerManager().broadcast(net.minecraft.text.Text.literal("§b[Regional Seasons] A cold snap is sweeping through!"), false);
        }
        save(server);
        syncRuntime(server);
    }

    public static void syncRuntime(MinecraftServer server) {
        if (server == null || server.getOverworld() == null) return;
        SeasonRuntime.update(season(server), dayOfSeason(server), SeasonConfig.INSTANCE.daysPerSeason, coldSnapDegreesC);
    }

    public static void clear() {
        owner = null;
        offsetDays = 0;
        coldSnapDegreesC = 0;
        radarSeed = 0;
        lastClimateDay = Long.MIN_VALUE;
        coldSnapEndsAtDay = Long.MIN_VALUE;
        SeasonRuntime.synced = false;
    }

    public static MinecraftServer owner() { return owner; }
}
