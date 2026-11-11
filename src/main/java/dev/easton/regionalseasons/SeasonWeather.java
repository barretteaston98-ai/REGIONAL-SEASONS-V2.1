package dev.easton.regionalseasons;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/** Surface snow, shaded snow and canopy dusting, hard cap exactly 18 stacked vanilla layers. */
public final class SeasonWeather {
    public static final int MAX_SNOW_LAYERS = SeasonRules.MAX_SNOW_LAYERS;  // NEVER a config option
    private SeasonWeather() {}

    public static void tick(MinecraftServer server) {
        SeasonConfig cfg = SeasonConfig.INSTANCE;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getServerWorld();
            if (!world.getRegistryKey().equals(World.OVERWORLD) || player.isSpectator()) continue;
            for (int i=0; i<cfg.samplesPerPlayer; i++) {
                int r=cfg.updateRadiusBlocks;
                int x=player.getBlockX()+world.random.nextBetween(-r,r);
                int z=player.getBlockZ()+world.random.nextBetween(-r,r);
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                boolean canopyTop=world.random.nextDouble()<cfg.snowOnLeavesChance;
                Heightmap.Type heightmap=canopyTop ? Heightmap.Type.WORLD_SURFACE : Heightmap.Type.MOTION_BLOCKING_NO_LEAVES;
                BlockPos top=world.getTopPosition(heightmap, new BlockPos(x,0,z));
                if (!world.isInBuildLimit(top) || !world.getBlockState(top).isAir()) continue;
                boolean visible=world.isSkyVisible(top);
                boolean underLeaves=!visible && cfg.allowSnowUnderLeaves && hasCanopy(world,top);
                if (!visible && !underLeaves) continue; // Roofed houses don't get snow.
                if (underLeaves && world.random.nextDouble()>cfg.snowUnderLeavesChance) continue;
                double c=ClimateMath.precipitationC(world.getBiome(top).value().getTemperature(),top);
                BlockState topSolid=world.getBlockState(top.down());
                if (cfg.enableSeasonalThaw && c>=ClimateMath.fToC(cfg.snowThresholdFahrenheit)) {
                    if (topSolid.isOf(Blocks.SNOW)) {
                        double chance=Math.min(cfg.maxSnowMeltingMultiplier, SeasonRules.meltChance(ClimateMath.cToF(c)));
                        if (underLeaves) chance*=0.35;
                        if (world.random.nextDouble()<chance) meltOne(world,top);
                    }
                    if (cfg.enableSeasonalIce && c>=5.0 && topSolid.isOf(Blocks.FROSTED_ICE)) {
                        world.setBlockState(top.down(),Blocks.WATER.getDefaultState(),3);
                    }
                    continue;
                }
                if (cfg.enableSeasonalIce && c<=-4 && topSolid.isOf(Blocks.WATER)
                        && world.getFluidState(top.down()).isStill()
                        && world.random.nextDouble()<cfg.freezeChancePerSample) {
                    world.setBlockState(top.down(),Blocks.FROSTED_ICE.getDefaultState(),3);
                }
                // This is the EXACT same precipitation decision vanilla rendering now uses.
                int intensity=RadarMath.intensity(x,z,world.getTime(),WorldCalendar.radarSeed(),world.isRaining());
                if (!cfg.enableSurfaceSnow || intensity==0 || !SeasonRules.snows(ClimateMath.cToF(c))
                        || world.getBiome(top).value().getPrecipitation(top)!=Biome.Precipitation.SNOW) continue;
                double chance=SeasonRules.accumulationChance(intensity);
                if (underLeaves) chance*=0.6;
                if (world.random.nextDouble()<chance) accumulateOne(world,top);
            }
        }
    }

    private static boolean hasCanopy(ServerWorld world, BlockPos pos) {
        for (int dy=1;dy<=11;dy++) {
            var check=pos.up(dy);
            if (!world.isInBuildLimit(check)) return false;
            if (world.getBlockState(check).isIn(BlockTags.LEAVES)) return true;
        }
        return false;
    }

    /** Counts contiguous vanilla snow blocks from the highest surface downward (8+8+2 maximum). */
    static int snowDepth(ServerWorld world, BlockPos top) {
        int depth=0;
        for(int dy=1;dy<=3;dy++) {
            BlockState state=world.getBlockState(top.down(dy));
            if (!state.isOf(Blocks.SNOW)) break;
            depth+=state.get(SnowBlock.LAYERS);
        }
        return depth;
    }

    static void accumulateOne(ServerWorld world, BlockPos top) {
        if (snowDepth(world,top)>=MAX_SNOW_LAYERS) return;
        BlockState below=world.getBlockState(top.down());
        if (below.isOf(Blocks.SNOW)) {
            int layers=below.get(SnowBlock.LAYERS);
            if (layers<8) {
                world.setBlockState(top.down(),below.with(SnowBlock.LAYERS,layers+1),3);
            } else if (Blocks.SNOW.getDefaultState().canPlaceAt(world,top)) {
                world.setBlockState(top,Blocks.SNOW.getDefaultState(),3);
            }
        } else if (below.isSolidBlock(world,top.down()) && Blocks.SNOW.getDefaultState().canPlaceAt(world,top)) {
            world.setBlockState(top,Blocks.SNOW.getDefaultState(),3);
        }
    }

    static void meltOne(ServerWorld world, BlockPos top) {
        BlockState below=world.getBlockState(top.down());
        if (!below.isOf(Blocks.SNOW)) return;
        int layers=below.get(SnowBlock.LAYERS);
        if (layers==1) world.removeBlock(top.down(),false);
        else world.setBlockState(top.down(),below.with(SnowBlock.LAYERS,layers-1),3);
    }
}

