package dev.easton.regionalseasons;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative fictional illness / temperature gameplay. 20 TPS, 7–10 minute cold. */
public final class ExposureSystem {
    private ExposureSystem() {}
    private static final Map<UUID, Illness> illnesses = new HashMap<>();
    private static final Map<UUID, PendingInfection> exposures = new HashMap<>();
    private static final Map<UUID, Long> recoveredUntil = new HashMap<>();
    private static final Map<UUID, Long> nextExposureRoll = new HashMap<>();
    private static long tick;

    private record Illness(long endsAt, long nextCoughCheck) {}
    private record PendingInfection(long triggersAt) {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (!world.isClient && hand == Hand.MAIN_HAND && player instanceof ServerPlayerEntity source
                    && target instanceof ServerPlayerEntity recipient && sick(source)) {
                expose(recipient, source);
            }
            return ActionResult.PASS;
        });
    }

    public static boolean sick(ServerPlayerEntity player) {
        Illness state = illnesses.get(player.getUuid());
        return state != null && state.endsAt > tick;
    }

    private static void startCold(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        if (sick(player) || recoveredUntil.getOrDefault(uuid, 0L) > tick) return;
        int duration = ExposureRules.coldDurationTicks(new java.util.Random(player.getRandom().nextLong()));
        illnesses.put(uuid, new Illness(tick + duration, tick + 20 * 60));
        exposures.remove(uuid);
        player.sendMessage(Text.literal("§dRegional Seasons: You caught a cold. Rest up (7–10 minutes)."), false);
    }

    private static void expose(ServerPlayerEntity recipient, ServerPlayerEntity source) {
        if (recipient == source || recipient.getServerWorld() != source.getServerWorld()
                || sick(recipient) || recoveredUntil.getOrDefault(recipient.getUuid(), 0L) > tick) return;
        // Transmission roll first; symptoms start after 1–60 seconds, not immediately.
        if (recipient.getRandom().nextDouble() < ExposureRules.TRANSMISSION_CHANCE) {
            exposures.putIfAbsent(recipient.getUuid(), new PendingInfection(tick + 20 + recipient.getRandom().nextInt(1200)));
        }
    }

    public static void tick(MinecraftServer server) {
        tick++;
        if (tick % 20 != 0) return;
        boolean coldEnabled=SeasonConfig.INSTANCE.enableExposureEffects;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isSpectator() || !player.isAlive()) continue;
            UUID uuid = player.getUuid();
            PendingInfection pending = exposures.get(uuid);
            if (coldEnabled && pending != null && tick >= pending.triggersAt()) startCold(player);
            Illness illness = illnesses.get(uuid);
            if (illness != null && tick >= illness.endsAt()) {
                illnesses.remove(uuid);
                recoveredUntil.put(uuid, tick + ExposureRules.IMMUNITY_TICKS);
                player.sendMessage(Text.literal("§aRegional Seasons: Your cold has cleared."), false);
                illness = null;
            }
            if (coldEnabled && illness != null) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 45, 0, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 45, 0, true, false, true));
                ServerWorld world = player.getServerWorld();
                if (tick % 80 == 0) world.spawnParticles(ParticleTypes.WITCH,
                        player.getX(), player.getY()+1.3, player.getZ(), 3, .25, .35, .25, .02);
                if (tick >= illness.nextCoughCheck()) {
                    illnesses.put(uuid, new Illness(illness.endsAt(), tick + 20 * 60));
                    if (player.getRandom().nextDouble() < 0.30) cough(player);
                }
            }
            // Temperature exposure is a game mechanic, not medical advice. One roll per 30s.
            if (coldEnabled && tick >= nextExposureRoll.getOrDefault(uuid, 0L) && illness == null
                    && player.getServerWorld().getRegistryKey() == net.minecraft.world.World.OVERWORLD
                    && ExposureRules.atRisk(ClimateMath.cToF(ClimateMath.feelsLikeC(player.getServerWorld(), player.getBlockPos())))) {
                boolean helmet = !player.getEquippedStack(EquipmentSlot.HEAD).isEmpty();
                boolean fullyArmored=helmet && !player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()
                        && !player.getEquippedStack(EquipmentSlot.LEGS).isEmpty()
                        && !player.getEquippedStack(EquipmentSlot.FEET).isEmpty();
                int seconds=helmet?30+player.getRandom().nextInt(16):30;
                nextExposureRoll.put(uuid,tick+seconds*20L);
                double chance=fullyArmored?ExposureRules.FULL_ARMOR_CHANCE:
                        ExposureRules.chancePerExposureRoll(helmet);
                if (player.getRandom().nextDouble()<chance) startCold(player);
            }
            double feelsF = ClimateMath.cToF(ClimateMath.feelsLikeC(player.getServerWorld(), player.getBlockPos()));
            if (coldEnabled && feelsF < 12 && tick % 100 == 0) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 110, 0, true, false, false));
            }
            // Heat-stroke possibility in game: small health loss on rare exposure roll; water/shade/fan mitigate.
            if (SeasonConfig.INSTANCE.enableHeatEffects && feelsF >= 100 && tick % 600 == 0 && player.getRandom().nextDouble() < 0.08) {
                player.damage(player.getDamageSources().generic(), 1.0f);
                player.sendMessage(Text.literal("§cRegional Seasons: Extreme heat! Seek water or shade."), true);
            }
            if (coldEnabled && illness != null && tick % 100 == 0) {
                for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                    if (other != player && other.getServerWorld() == player.getServerWorld()
                            && other.squaredDistanceTo(player) <= 4.0 && other.getRandom().nextDouble() < 0.04) expose(other, player);
                }
            }
        }
    }

    private static void cough(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PANDA_SNEEZE, SoundCategory.PLAYERS, .6f, .85f);
        player.swingHand(Hand.MAIN_HAND, true);
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0f);
        for (int i=1; i<=10; i++) {
            Vec3d point = start.add(direction.multiply(i*.5));
            world.spawnParticles(ParticleTypes.WITCH, point.x,point.y,point.z,2,.12,.12,.12,0.003);
        }
        for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
            if (other == player || other.getServerWorld() != world) continue;
            Vec3d relative = other.getEyePos().subtract(start);
            double along = relative.dotProduct(direction);
            if (along < 0 || along > 5) continue;
            Vec3d closest = start.add(direction.multiply(along));
            if (other.getEyePos().squaredDistanceTo(closest) <= 2.25) expose(other,player);
        }
    }

    public static void clear() {
        illnesses.clear(); exposures.clear(); recoveredUntil.clear(); nextExposureRoll.clear(); tick=0;
    }
}
