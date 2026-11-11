package dev.easton.regionalseasons;

import dev.easton.regionalseasons.network.RadarPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Accurate shared precipitation FIELD; distant temperatures are estimates, not remote block scans. */
public final class WeatherRadarItem extends Item {
    public WeatherRadarItem(Settings settings){super(settings);}
    @Override public TypedActionResult<ItemStack> use(World world,PlayerEntity player,Hand hand){
        ItemStack stack=player.getStackInHand(hand);
        if(!world.isClient()&&player instanceof ServerPlayerEntity sp&&SeasonConfig.INSTANCE.enableWeatherRadar
                &&ServerPlayNetworking.canSend(sp,RadarPayload.ID)){
            int cx=sp.getBlockX(),cz=sp.getBlockZ();
            int temp=(int)Math.round(ClimateMath.cToF(ClimateMath.temperatureC(world,sp.getBlockPos())));
            byte[] intensity=new byte[1024],offsets=new byte[1024];
            long ticks=world.getTime(), seed=WorldCalendar.radarSeed();
            boolean raining=world.isRaining();
            for(int z=0;z<32;z++)for(int x=0;x<32;x++){
                int px=cx+(x-16)*RadarMath.STEP_BLOCKS;
                int pz=cz+(z-16)*RadarMath.STEP_BLOCKS;
                int i=z*32+x;
                intensity[i]=(byte)RadarMath.intensity(px,pz,ticks,seed,raining);
                offsets[i]=(byte)Math.clamp((int)Math.round(RadarMath.estimatedTemperatureF(temp,px,pz,cx,cz,seed)-temp),-64,64);
            }
            ServerPlayNetworking.send(sp,new RadarPayload(cx,cz,temp,raining,intensity,offsets));
        }
        return TypedActionResult.success(stack,world.isClient());
    }
}
