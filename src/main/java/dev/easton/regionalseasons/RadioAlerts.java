package dev.easton.regionalseasons;

import dev.easton.regionalseasons.network.RadioPayload;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Five one-second beeps for severe SNOW only; no warning or false alarm for regular rain. */
public final class RadioAlerts {
    private RadioAlerts(){}
    private record Alarm(long nextAt,int remaining){}
    private static final Map<UUID,Alarm> active=new HashMap<>();
    private static final Map<UUID,Long> cooldown=new HashMap<>();
    public static RadioPayload current(ServerPlayerEntity player){
        int x=player.getBlockX(),z=player.getBlockZ();
        long tick=player.getServerWorld().getTime();
        long seed=WorldCalendar.radarSeed();
        int intensity=RadarMath.intensity(x,z,tick,seed,player.getServerWorld().isRaining());
        double airF=ClimateMath.cToF(ClimateMath.precipitationC(
            player.getServerWorld().getBiome(player.getBlockPos()).value().getTemperature(),player.getBlockPos()));
        if(intensity!=3||!SeasonRules.snows(airF))return new RadioPayload("~ static ~ No severe winter-weather alert.","CLEAR",0,false);
        return new RadioPayload("Severe snow band nearby. Estimated accumulation: 8–14 layers. Seek shelter.",
                "BLIZZARD",RadarMath.estimatedSnowLayers(intensity),true);
    }
    public static void tick(MinecraftServer server){
        if(!SeasonConfig.INSTANCE.enableStormRadio)return;
        long tick=server.getTicks();
        for(ServerPlayerEntity player:server.getPlayerManager().getPlayerList()){
            UUID id=player.getUuid();
            Alarm alarm=active.get(id);
            if(alarm!=null&&tick>=alarm.nextAt()){
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL,.95f,1.35f);
                if(alarm.remaining()<=1)active.remove(id);
                else active.put(id,new Alarm(tick+20,alarm.remaining()-1));
            }
            if(tick%100!=0||active.containsKey(id)||cooldown.getOrDefault(id,0L)>tick)continue;
            boolean loaded=false;
            for(int i=0;i<player.getInventory().size();i++){
                ItemStack stack=player.getInventory().getStack(i);
                if(stack.isOf(RegionalItems.STORM_RADIO)&&StormRadioItem.batteries(stack)>=2){loaded=true;break;}
            }
            if(!loaded)continue;
            if(current(player).active()){
                player.sendMessage(Text.literal("§b[REGIONAL SEASONS] RADIO ALERT: Blizzard detected! Right-click your radio."),false);
                active.put(id,new Alarm(tick,5));
                cooldown.put(id,tick+SeasonConfig.INSTANCE.radioAlertCooldownSeconds*20L);
            }
        }
    }
    public static void clear(){active.clear();cooldown.clear();}
}
