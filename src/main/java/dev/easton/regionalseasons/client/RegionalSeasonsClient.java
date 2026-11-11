package dev.easton.regionalseasons.client;

import dev.easton.regionalseasons.ClimateMath;
import dev.easton.regionalseasons.Season;
import dev.easton.regionalseasons.SeasonConfig;
import dev.easton.regionalseasons.SeasonRuntime;
import dev.easton.regionalseasons.network.ForecastPayload;
import dev.easton.regionalseasons.network.RadioPayload;
import dev.easton.regionalseasons.network.RadarPayload;
import dev.easton.regionalseasons.network.SeasonSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

/** Animated color-changing Fahrenheit HUD; interpolated numbers never jump 4°F from one step. */
public final class RegionalSeasonsClient implements ClientModInitializer {
    private static double shownF=Double.NaN;
    private static double targetF=Double.NaN;
    private static long frames;

    @Override public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SeasonSyncPayload.ID,(p,context)-> context.client().execute(()->{
            SeasonRuntime.update(Season.fromIndex(p.seasonIndex()),p.day(),Math.max(1,p.seasonLength()),p.coldSnapDeciC()/10.0);
            SeasonRuntime.updateRadar(p.radarSeed(),context.client().world==null?0:context.client().world.getTime(),p.raining());
        }));
        ClientPlayNetworking.registerGlobalReceiver(RadioPayload.ID,(p,ctx)->ctx.client().execute(()->ctx.client().setScreen(new RadioScreen(p))));
        ClientPlayNetworking.registerGlobalReceiver(RadarPayload.ID,(p,ctx)->ctx.client().execute(()->ctx.client().setScreen(new RadarScreen(p))));
        ClientPlayNetworking.registerGlobalReceiver(ForecastPayload.ID,(p,context)-> context.client().execute(()->{
            context.client().setScreen(new CalendarScreen(p));
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{
            SeasonRuntime.synced=false;shownF=Double.NaN;targetF=Double.NaN;
        });
        // Sample every 3 seconds; HUD smoothly interpolates toward whole °F.
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if (client.world==null || client.player==null || !SeasonRuntime.synced) return;
            if (client.world.getRegistryKey()!=World.OVERWORLD) return;
            if (client.world.getTime()%60==0) {
                targetF=ClimateMath.cToF(ClimateMath.feelsLikeC(client.world,client.player.getBlockPos()));
                if (Double.isNaN(shownF)) shownF=targetF;
            }
            if (!Double.isNaN(targetF)) shownF+=(targetF-shownF)*0.055;
        });
        HudRenderCallback.EVENT.register((draw,tickCounter)->{
            MinecraftClient client=MinecraftClient.getInstance();
            if (client.player==null || client.world==null || client.options.hudHidden
                    || !SeasonRuntime.synced || !SeasonConfig.INSTANCE.showClimateHud
                    || Double.isNaN(shownF)) return;
            frames++;
            double tempF=shownF;
            int labelColor = tempF<35 ? 0xFF78C5FF : tempF<55 ? 0xFF91D5F5 : tempF>=85 ? 0xFFFF765F : 0xFFFFE09A;
            int x=SeasonConfig.INSTANCE.hudX, y=SeasonConfig.INSTANCE.hudY;
            int bounce=(int)Math.round(Math.sin(frames/13.0)*1.0);
            int panelW=169;
            draw.fill(x,y,x+panelW,y+34,0xA91A2934);
            draw.fill(x,y,x+3,y+34,labelColor);
            String season=SeasonRuntime.current.display.toUpperCase();
            draw.drawTextWithShadow(client.textRenderer,season+" · DAY "+SeasonRuntime.dayOfSeason,x+9,y+5,0xFFC3D6DE);
            String number=Math.round(tempF)+"°F";
            draw.drawTextWithShadow(client.textRenderer,number,x+9,y+18+bounce,labelColor);
            if (tempF<35) {
                draw.drawTextWithShadow(client.textRenderer,"❄ COLD",x+104,y+18,0xFF96DAFF);
            } else if (tempF>=85) {
                // Three animated pixel flames above HOT; Minecraft emoji-font support not required.
                int fx=x+108,fy=y+12+(int)Math.round(Math.sin(frames/5.0));
                draw.fill(fx+3,fy-5,fx+6,fy,0xFFFFA23B);
                draw.fill(fx+1,fy-2,fx+8,fy+5,0xFFFF553A);
                draw.fill(fx+3,fy,fx+6,fy+5,0xFFFFD27A);
                draw.drawTextWithShadow(client.textRenderer,"HOT",x+126,y+18,0xFFFF9E76);
            } else {
                draw.drawTextWithShadow(client.textRenderer,"● LOCAL",x+104,y+18,0xFF92C2A4);
            }
        });
    }
}

