# Regional Seasons — 2.1.0 source update

**Status:** SOURCE CANDIDATE. Pure Java math tests passed; Fabric compilation, Minecraft launch, multiplayer, and actual rendered screenshots have NOT been verified here. Build using the included GitHub Actions workflow before installing. No working 2.1 JAR is included.

## New — regional Doppler-style radar
- Craftable `regionalseasons:weather_radar`. Right-click for a 32×32 pixelated weather map, covering approximately **1,024 blocks in each direction** (2,048 blocks across).
- PRECIP / TEMP tabs. Green/yellow/red show light/medium/heavy rain; blues/whites show snow. Temperature colors are approximations extrapolated from the local reading — **not measured temperatures in distant unloaded biomes**.
- Drifting, deterministic, smooth spatial storm bands shared between the local world and connected clients; local weather intensity also feeds snow accumulation.
- Fixed-size, low-cost ~2 KB radar payload generated on item use. No 2,000-block chunk scans or always-on video-style map rendering.

## New — survival temperature and sickness
- Whole-number Fahrenheit HUD samples conditions every 3 seconds instead of showing fast-changing decimal temperatures; existing animation eases between changes.
- Altitude cooling, water/icy-water cooling, near-ice cooling, tree shade, indoor temperature moderation, and nearby lava/fire/campfires/torches warming; craftable AC cooling fan reduces feels-like temperature.
- Cold risk below 28°F: 4% per ~30 sec without a helmet; 1% per 30–45 sec with a helmet; 0.05% per 30–45 sec with full armor. (Configurable enable/disable as a set.)
- Fictional colds last **7–10 real-time minutes of active gameplay**, inflicting Slowness / Mining Fatigue and some purple particles. 2-minute post-recovery cooldown; timers are session-local, not saved across server restarts.
- Sick players may transmit colds by close proximity, hits, or occasional coughing. Cough particles travel forward up to ~5 blocks; the exposure roll is 75%, with symptoms starting 1–60 seconds after a successful roll. Sound plus simple arm-swing animation, not a custom player animation model.
- 100°F+ feels-like temperatures can very occasionally cause half a heart of damage; water, cooling fan and indoor conditions can help. Very low temperatures can induce Slowness.

## New — battery-powered secret storm radio
- Secret shaped crafting recipe for `regionalseasons:storm_radio`, plus craftable `regionalseasons:aa_battery` (recipe outputs two).
- Hold radio in main hand, **two AA batteries in offhand**, sneak + right-click to load them; power is stored on the actual radio stack.
- During severe local snow bands, radio plays five one-second bell beeps and shows a message. Right-click for estimated snow depth and alert details. No blizzard = static.
- Requires an installed, loaded radio in your inventory; no radio alerts for regular rain. Alarm cooldown 90 seconds by default.

## Fixes / changes
- Weather is based on coherent regional cloud/precipitation fields on land **and** sea, not checks for water under the player.
- Under 35°F AIR temperature, active local precipitation selects **vanilla SNOW** (not rain particles plus snow overlays); 35°F+ selects rain. Above threshold no new snow accumulation from this system.
- Existing 18-layer hard cap (8+8+2 vanilla snow), light/medium/heavy accumulation and thawing preserved.
- Forecast calendar background now darkens without explicitly applying background blur; calendar text and panel drawn afterward.
- Leaf recoloring and crop-speed modifications remain intentionally out of scope.

## Known limitations
- No complete biome-transition smoothing on every Minecraft desert boundary; vanilla dry-biome eligibility remains a hard edge. Regional cloud bands are smooth, but vanilla biome blending can still cause abrupt snow/rain/dry boundaries.
- Regional radar remote temperatures are estimates, not chunk-accurate observations. Radar refreshes its precipitation field every 5 seconds while open, while remote temperature shading remains a rough static estimate until reopened.
- Severe storm warnings reflect the local 32×32 radar field and ambient air temperature, not a full meteorological simulation.
- The 18-layer system uses stacked vanilla blocks, not a 3D custom 18-layer block.
- Source compiles, game rendering, exact sound registration, performance and Essential compatibility still need an actual build and live test. Do not call previews playtest screenshots.
