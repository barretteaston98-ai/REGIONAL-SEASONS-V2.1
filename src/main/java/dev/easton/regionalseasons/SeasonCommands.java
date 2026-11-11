package dev.easton.regionalseasons;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SeasonCommands {
    private SeasonCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("seasons")
                .executes(ctx -> status(ctx.getSource()))
                .then(literal("info").executes(ctx -> status(ctx.getSource())))
                .then(literal("temp").executes(ctx -> temperature(ctx.getSource())))
                .then(literal("help").executes(ctx -> help(ctx.getSource())))
                .then(literal("set")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(argument("season", StringArgumentType.word())
                                .suggests((ctx, suggestions) -> {
                                    for (Season s : Season.values()) suggestions.suggest(s.name().toLowerCase());
                                    return suggestions.buildFuture();
                                })
                                .executes(ctx -> set(ctx.getSource(), StringArgumentType.getString(ctx, "season")))))
                .then(literal("skip")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(argument("days", IntegerArgumentType.integer(1, 10000))
                                .executes(ctx -> skip(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "days")))))
                .then(literal("reload")
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> reload(ctx.getSource()))));
    }

    private static int status(ServerCommandSource source) {
        var server = source.getServer();
        Season season = WorldCalendar.season(server);
        String message = "§6Regional Seasons §7| " + season.color + season.display
                + "§7 | Day §f" + WorldCalendar.dayOfSeason(server) + "/" + SeasonConfig.INSTANCE.daysPerSeason
                + "§7 | Year §f" + WorldCalendar.year(server);
        source.sendFeedback(() -> Text.literal(message), false);
        return 1;
    }

    private static int temperature(ServerCommandSource source) {
        BlockPos pos = BlockPos.ofFloored(source.getPosition());
        double degrees = ClimateMath.temperatureC(source.getWorld(), pos);
        var entry = source.getWorld().getBiome(pos);
        String biome = entry.getKey().map(key -> key.getValue().toString()).orElse("unknown");
        source.sendFeedback(() -> Text.literal("§bClimate §7| " + ClimateMath.formatTemperature(degrees)
                + "§7 | §f" + biome + "§7 | Y=" + pos.getY()), false);
        return 1;
    }

    private static int help(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("§6/seasons§7, §6/seasons temp§7, §6/seasons info§7, §6/seasons set <season>§7, §6/seasons skip <days>§7, §6/seasons reload"), false);
        return 1;
    }

    private static int set(ServerCommandSource source, String target) {
        Season season = Season.fromName(target);
        if (season == null) {
            source.sendError(Text.literal("Use spring, summer, autumn, or winter."));
            return 0;
        }
        WorldCalendar.set(source.getServer(), season);
        RegionalSeasons.syncAll(source.getServer());
        source.sendFeedback(() -> Text.literal("Season changed to " + season.display + "."), true);
        return 1;
    }

    private static int skip(ServerCommandSource source, int days) {
        WorldCalendar.skip(source.getServer(), days);
        RegionalSeasons.syncAll(source.getServer());
        source.sendFeedback(() -> Text.literal("Skipped " + days + " seasonal days."), true);
        return 1;
    }

    private static int reload(ServerCommandSource source) {
        SeasonConfig.reload();
        WorldCalendar.syncRuntime(source.getServer());
        RegionalSeasons.syncAll(source.getServer());
        source.sendFeedback(() -> Text.literal("Regional Seasons server config reloaded."), true);
        return 1;
    }
}
