package dev.easton.regionalseasons.client;

import dev.easton.regionalseasons.network.ForecastPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Compact calendar/forecast screen. All values are estimates sent by the server. */
public final class CalendarScreen extends Screen {
    private final ForecastPayload data;
    public CalendarScreen(ForecastPayload data) {
        super(Text.literal("Regional Seasons · Forecast Calendar"));
        this.data=data;
    }
    @Override
    public void render(DrawContext draw,int mouseX,int mouseY,float delta) {
        draw.fill(0,0,width,height,0x8C07141C); // V2.1: translucent darkening, never blur the forecast text
        int w=286,h=186,x=(width-w)/2,y=(height-h)/2;
        draw.fill(x+3,y+4,x+w+3,y+h+4,0x84000000);
        draw.fill(x,y,x+w,y+h,0xF1192832);
        draw.fill(x,y,x+w,y+28,0xFF2B5265);
        draw.drawCenteredTextWithShadow(textRenderer,"REGIONAL SEASONS · FORECAST",width/2,y+10,0xFFE8F8FF);
        draw.drawTextWithShadow(textRenderer,"Year " + data.year() + " · " + data.season()
                + " · Day " + data.day(),x+15,y+38,0xFFECCAA2);
        draw.drawTextWithShadow(textRenderer,"Currently: " + data.current(),x+15,y+56,0xFFB1DDE8);
        draw.fill(x+13,y+75,x+w-13,y+76,0x6683B7CB);
        for(int i=0;i<3;i++) {
            int sy=y+83+i*27;
            int color=data.highF()[i]<35?0xFF81C9FF:data.highF()[i]>=85?0xFFFF8E65:0xFFE8EEC1;
            draw.drawTextWithShadow(textRenderer,data.dayNames()[i],x+15,sy,color);
            draw.drawTextWithShadow(textRenderer,data.lowF()[i]+"°–"+data.highF()[i]+"°F",x+172,sy,0xFFFFFFFF);
            draw.drawTextWithShadow(textRenderer,data.precipitationPct()[i]+"% precip.",x+172,sy+10,0xFF98B4C9);
        }
        draw.drawCenteredTextWithShadow(textRenderer,"Approximate outlook · Not guaranteed",width/2,y+h-15,0xFF829CAA);
        super.render(draw,mouseX,mouseY,delta);
    }
    @Override public boolean shouldPause() { return false; }
}

