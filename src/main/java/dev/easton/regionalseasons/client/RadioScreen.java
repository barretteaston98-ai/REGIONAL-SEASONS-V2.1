package dev.easton.regionalseasons.client;

import dev.easton.regionalseasons.network.RadioPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Sharp UI: intentionally no post-process blur. */
public final class RadioScreen extends Screen {
    private final RadioPayload data;
    public RadioScreen(RadioPayload data){super(Text.literal("Storm Radio"));this.data=data;}
    @Override public void render(DrawContext g,int mx,int my,float dt){
        g.fill(0,0,width,height,0x95050C10);
        int w=296,h=146,x=(width-w)/2,y=(height-h)/2;
        g.fill(x,y,x+w,y+h,0xFF18272D);
        g.fill(x+5,y+5,x+w-5,y+30,0xFF254451);
        g.drawCenteredTextWithShadow(textRenderer,"REGIONAL SEASONS · STORM RADIO",width/2,y+13,0xFFEAF5FF);
        int color=data.active()?0xFFFFC18A:0xFFAAB7BE;
        g.drawTextWithShadow(textRenderer,"SIGNAL: "+data.level(),x+15,y+48,color);
        if(data.active()){
            g.drawTextWithShadow(textRenderer,"SEVERE WINTER WEATHER",x+15,y+70,0xFFFF9288);
            g.drawTextWithShadow(textRenderer,"Estimated snow: "+data.estimatedSnowLayers()+" layers",x+15,y+89,0xFFB2E3FF);
        } else {
            g.drawTextWithShadow(textRenderer,"-- shhhhhhhh -- [static]",x+15,y+70,0xFF8C9CA2);
            g.drawTextWithShadow(textRenderer,"No active blizzard alert",x+15,y+89,0xFFB2B9C0);
        }
        g.drawCenteredTextWithShadow(textRenderer,"Escape to close",width/2,y+123,0xFFB0BABE);
        super.render(g,mx,my,dt);
    }
    @Override public boolean shouldPause(){return false;}
}
