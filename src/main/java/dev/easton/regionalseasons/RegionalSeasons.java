package dev.easton.regionalseasons;

import dev.easton.regionalseasons.network.SeasonSyncPayload;
import dev.easton.regionalseasons.network.ForecastPayload;
import dev.easton.regionalseasons.network.RadioPayload;
import dev.easton.regionalseasons.network.RadarPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RegionalSeasons implements ModInitializer {
    public static final String MOD_ID = "regionalseasons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static Season lastSeason;
    private static int lastDay;

    @Override
    public void onInitialize() {
        SeasonConfig.reload();
        RegionalItems.initialize();
        RegionalBlocks.initialize();
        ExposureSystem.register();
        PayloadTypeRegistry.playS2C().register(SeasonSyncPayload.ID, SeasonSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ForecastPayload.ID, ForecastPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RadioPayload.ID, RadioPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RadarPayload.ID, RadarPayload.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SeasonCommands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WorldCalendar.load(server);
            lastSeason = WorldCalendar.season(server);
            lastDay = WorldCalendar.dayOfSeason(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            WorldCalendar.save(server);
            WorldCalendar.clear();
            ExposureSystem.clear();
            RadioAlerts.clear();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> server.execute(() -> sync(handler.player)));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) {
                WorldCalendar.advanceClimate(server);
                WorldCalendar.syncRuntime(server);
                SeasonRuntime.updateRadar(WorldCalendar.radarSeed(), server.getOverworld().getTime(), server.getOverworld().isRaining());
                Season newSeason = WorldCalendar.season(server);
                int newDay = WorldCalendar.dayOfSeason(server);
                if (newSeason != lastSeason || newDay != lastDay) {
                    lastSeason = newSeason;
                    lastDay = newDay;
                    syncAll(server);
                }
            }
            if (server.getTicks() % 100 == 0) syncAll(server);
            if (server.getTicks() % SeasonConfig.INSTANCE.environmentUpdateTicks == 0) SeasonWeather.tick(server);
            ExposureSystem.tick(server);
            RadioAlerts.tick(server);
        });
        LOGGER.info("Regional Seasons V2.1 source loaded: Fabric Minecraft 1.21.1");
    }

    public static void sync(ServerPlayerEntity player) {
        if (!ServerPlayNetworking.canSend(player, SeasonSyncPayload.ID)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerPlayNetworking.send(player, new SeasonSyncPayload(
                WorldCalendar.season(server).ordinal(),
                WorldCalendar.dayOfSeason(server),
                SeasonConfig.INSTANCE.daysPerSeason,
                (int)Math.round(WorldCalendar.coldSnapDegreesC()*10),
                WorldCalendar.radarSeed(), player.getServerWorld().isRaining()
        ));
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) sync(player);
    }
}
