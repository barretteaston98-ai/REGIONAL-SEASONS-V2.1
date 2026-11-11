# Regional Seasons 2.1.0 — Minecraft 1.21.1 / Fabric

This repository is an **experimental SOURCE candidate**, not a compiled, tested mod release. It adds regional storms, radar, a battery-powered blizzard radio, air/feels-like temperatures, colds, heat effects, and an AC fan on top of V2's seasonal calendar and 18-layer snow. **It may require source fixes before it compiles on GitHub Actions or launches in Minecraft.**

## Important: ZIP vs JAR
- **Source ZIP is not installable** in Modrinth or Minecraft.
- Build the source with Java 21, Gradle and Fabric Loom (GitHub Actions is included). Only after the workflow succeeds can you install the built `regional-seasons-2.1.0.jar` into `mods`.
- Do not put a `*-sources.jar` in `mods`.
- Do not use both V2.0 and V2.1 JARs together.

## How to build with GitHub (no local Gradle install)
1. Download/extract this ZIP.
2. Open the existing `regional-seasons-v2` repository on github.com (where V2.0 was compiled). On the **Code** tab, move/replace all top-level source files from this ZIP: `src/`, `build.gradle`, `gradle.properties`, `settings.gradle`, `.github/workflows/build-jar.yml`, etc. Retain the folder structure. **Do not upload the enclosing folder as a nested folder.**
3. Commit changes to `main`. Navigate to **Actions → Build Fabric 1.21.1 JAR** and select the newest run, or **Run workflow** to start it manually.
4. A green checkmark means the source built, not that it is gameplay-tested. Open the completed run → Artifacts → `regional-seasons-v2.1-fabric-1.21.1`; download ZIP and extract the regular `regional-seasons-2.1.0.jar` from inside.
5. If red X, open the failing Gradle step and copy the **first compiler error**. The source package is not a mod until those errors are resolved.

## Modrinth App — you and a friend
1. Each player creates a **Minecraft Java 1.21.1** installation with **Fabric**.
2. Inside that instance, add **Fabric API** for 1.21.1 and **Essential Mod** for Fabric 1.21.1 using **Add Content**.
3. Open the instance's **Open Folder** menu and copy the *compiled* `regional-seasons-2.1.0.jar` into `mods/` on **BOTH** PCs. Remove V1.1/V2.0 JARs.
4. Both launch the same instance, ideally back up the world first; confirm `Regional Seasons` appears in the mod list and the world loads.
5. Essential: become friends in Essential's Social menu; host selects **Host** → chooses a world → **Start Hosting** → **Add Members** and selects their friend. The friend selects the host in Essential **Social → Chat → Join Game**, or **Multiplayer → Friends**.
6. Hosting uses the host player's own computer; the host must be online for the friend to play. Both players must have matching mods and mod versions.
7. If joining fails, verify matching Minecraft version, Fabric Loader, Fabric API, Essential and Regional Seasons JAR; compare game logs.

## Gameplay / testing
- `/seasons` for status, `/seasons temp` local temperature, `/seasons set winter` as OP.
- `/give @s regionalseasons:forecast_calendar` opens three-day approximate forecast.
- `/give @s regionalseasons:weather_radar` opens 2,048-block-wide **estimated** local radar with PRECIP / TEMP tabs.
- `/give @s regionalseasons:storm_radio` and `/give @s regionalseasons:aa_battery 2`; hold radio in MAIN hand, two batteries in OFF hand; sneak + right-click to insert, then normal right-click.
- `/give @s regionalseasons:ac_fan` to place a passive cooling fan within a few blocks.
- For winter weather as OP: `/seasons set winter` then `/weather rain`. Be aware rain-to-snow is local only where the radar field has a precipitation cell and the actual AIR temperature is **below 35°F**. If you don't see snow, check radar and move to an active blue/white snow region.
- To verify local precipitation consistency, check that when snowflakes fall, **rain streaks are absent** at that location. In hot cells, only rain should appear; outside active cells, neither should fall.
- To test cold risks, set `enableExposureEffects` to `true` in `config/regional-seasons.json` (default for a new 2.1 config, but preexisting configs may still have it disabled) and restart/reload configuration.

## Configuration
The file is created at `config/regional-seasons.json` inside each Modrinth instance. The server/Essential HOST controls gameplay settings. Clients control their HUD preferences. Back up config and world before updates.

Key configuration fields include `enableExposureEffects`, `enableStormRadio`, `enableWeatherRadar`, `enableHeatEffects`, `radioAlertCooldownSeconds`, `environmentUpdateTicks`, `samplesPerPlayer`, `daysPerSeason`, `showClimateHud`, `hudX`, `hudY`, `degreesCelsiusLostPerBlockAboveSeaLevel`, `snowThresholdFahrenheit`, `fanCoolingCelsius`, and indoor comfort temperature.

The **18-layer snow cap is intentionally hardcoded**, not configurable.

## Recipes
- **AA battery ×2:** iron nugget–redstone–iron nugget across the middle row; plus one iron nugget centered above and below (` I ` / `IRI` / ` I `).
- **Storm Radio (secret):** copper + iron + redstone + iron (shaped ` CI` / `RIR` / `III`). No custom recipe book unlock advancement is included; the recipe JSON exists and can be discovered.
- **Weather Radar:** compass in center, redstone on sides, iron top/bottom corners, glass pane bottom center (` I ` / `RCR` / `IGI`).
- **AC Fan:** iron and redstone (` I ` / `IRI` / `III`) — crafting uses **iron ingots**, not battery nuggets.
- **Forecast calendar:** eight paper around one ink sac in center (existing V2 recipe).

## Known limitations and safety notes
- No Minecraft gameplay or Essential session has been executed in this environment; screenshots cannot truthfully be called gameplay tests.
- Remote temperature shading is a forecast estimate, not full chunk-by-chunk biome observation. To keep radar fast, no distant chunks are loaded for scans.
- Vanilla deserts/dry biomes can still create abrupt local border effects even though the storm-field math is smooth.
- Sickness/heat are fictional game rules, not medical information; they can be disabled in config.
- In-game illness timers are session-local; they reset when hosting stops.
- Full packaging details in CHANGELOG.md; data and language files under `src/main/resources`.
