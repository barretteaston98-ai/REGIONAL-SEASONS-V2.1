package dev.easton.regionalseasons;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class RegionalItems {
    public static final Item FORECAST_CALENDAR = Registry.register(
            Registries.ITEM, Identifier.of(RegionalSeasons.MOD_ID, "forecast_calendar"),
            new ForecastCalendarItem(new Item.Settings().maxCount(1)));
    public static final Item AA_BATTERY = Registry.register(Registries.ITEM,
            Identifier.of(RegionalSeasons.MOD_ID,"aa_battery"),new Item(new Item.Settings().maxCount(16)));
    public static final Item STORM_RADIO = Registry.register(Registries.ITEM,
            Identifier.of(RegionalSeasons.MOD_ID,"storm_radio"),new StormRadioItem(new Item.Settings().maxCount(1)));
    public static final Item WEATHER_RADAR = Registry.register(Registries.ITEM,
            Identifier.of(RegionalSeasons.MOD_ID,"weather_radar"),new WeatherRadarItem(new Item.Settings().maxCount(1)));
    private RegionalItems() {}
    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> { entries.add(FORECAST_CALENDAR);
            entries.add(AA_BATTERY); entries.add(STORM_RADIO); entries.add(WEATHER_RADAR); });
    }
}

