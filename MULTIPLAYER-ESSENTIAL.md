# Exact Essential two-player steps (Fabric 1.21.1)

1. Build `regional-seasons-2.1.0.jar` with GitHub Actions. **The source ZIP cannot be dropped into `mods`.** The new build must show a green check and still needs an in-game test.
2. On your PC: Modrinth App → Create Instance → Minecraft 1.21.1 → Fabric.
3. Open Add Content → add **Fabric API** and **Essential Mod** compatible with Fabric 1.21.1.
4. Go to the instance three-dot menu → Open Folder → `mods` → drop in the built V2.1 JAR. Remove older Regional Seasons JARs.
5. Send your friend the **exact same built JAR**, or share the Modrinth instance. Your friend does steps 2–4 with the same Fabric API + Essential versions.
6. Both launch Minecraft, sign in, and open Essential Social. Add/accept the other person's Minecraft username as an Essential friend.
7. Host opens the world via Essential **Host** (from main menu or pause screen), clicks **Start Hosting**, selects friend, and **Add Members**.
8. Friend opens **Social → Chat → host → Join Game**, or **Multiplayer → Friends** to join.
9. You are playing in the same world; the host controls time/weather/season settings. The host must stay in Minecraft for the friend to stay connected.
10. Test with `/seasons`, `/give @s regionalseasons:weather_radar`, `/give @s regionalseasons:storm_radio`, `/give @s regionalseasons:aa_battery 2`, `/seasons set winter`, and `/weather rain`.
11. Trouble: if a player disconnects, compare logs and each `mods` directory for identical mod versions, confirm Fabric, Java 21, and test without shaders first. Always back up a world before a new mod version.

Official Essential guide: https://essential.gg/wiki/start-hosting
Joining guide: https://essential.gg/wiki/play-together
