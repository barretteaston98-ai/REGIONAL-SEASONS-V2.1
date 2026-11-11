package dev.easton.regionalseasons.mixin;

import dev.easton.regionalseasons.ClimateMath;
import dev.easton.regionalseasons.SeasonConfig;
import dev.easton.regionalseasons.SeasonRuntime;
import dev.easton.regionalseasons.SeasonRules;
import dev.easton.regionalseasons.RadarMath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Changes the SAME vanilla precipitation result used by weather rendering and world logic.
 *  The world can still be internally raining; cold columns render falling SNOW, not rain streaks. */
@Mixin(Biome.class)
public abstract class BiomeMixin {
    @Shadow public abstract float getTemperature();
    @Shadow public abstract boolean hasPrecipitation();

    @Inject(method = "getPrecipitation", at = @At("HEAD"), cancellable = true)
    private void regionalseasons$realSnowInsteadOfRain(BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (!SeasonRuntime.synced || !SeasonConfig.INSTANCE.enableSeasonalPrecipitation) return;
        if (!hasPrecipitation()) return; // Dry biomes stay dry.
        if (RadarMath.intensity(pos.getX(),pos.getZ(),SeasonRuntime.radarTimeTicks,
                SeasonRuntime.radarSeed, SeasonRuntime.isRaining)==0) {
            cir.setReturnValue(Biome.Precipitation.NONE);
            return;
        }
        double airTemperature = ClimateMath.precipitationC(getTemperature(), pos);
        cir.setReturnValue(SeasonRules.snows(ClimateMath.cToF(airTemperature))
                ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN);
    }
}

