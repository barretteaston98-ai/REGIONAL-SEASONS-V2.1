package dev.easton.regionalseasons.client;

import dev.easton.regionalseasons.network.RadarPayload;
import dev.easton.regionalseasons.RadarMath;
import dev.easton.regionalseasons.SeasonRuntime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** 32x32 cells, fixed 1024-block radius. Render ~1024 rectangles, not thousands of sampled chunks. */
public final class RadarScreen extends Screen {
    private final RadarPayload radar;
    private boolean temperatureMode=false;
    private byte[] displayed;
    private long lastSampleTick=Long.MIN_VALUE;
    public RadarScreen(RadarPayload radar){super(Text.literal("Regional Weather Radar"));this.radar=radar; this.displayed=radar.intensity().clone();}
    @Override public void render(DrawContext g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xBB02080F);
        // Refresh current radar field every 5 seconds while open; no expensive world/chunk scans.
        MinecraftClient client=MinecraftClient.getInstance();
        if(client.world!=null){
            long now=client.world.getTime();
            if(now<lastSampleTick || now-lastSampleTick>=100){
                long seed=SeasonRuntime.radarSeed;
                boolean raining=client.world.isRaining();
                for(int z=0;z<32;z++) for(int a=0;a<32;a++){
                    int px=radar.centerX()+(a-16)*RadarMath.STEP_BLOCKS;
                    int pz=radar.centerZ()+(z-16)*RadarMath.STEP_BLOCKS;
                    displayed[z*32+a]=(byte)RadarMath.intensity(px,pz,now,seed,raining);
                }
                lastSampleTick=now;
            }
        }
        int w=292,h=292,x=(width-w)/2,y=(height-h)/2;
        g.fill(x,y,x+w,y+h,0xFF10212C);
        g.fill(x+3,y+3,x+w-3,y+27,0xFF245369);
        g.drawCenteredTextWithShadow(textRenderer,"REGIONAL SEASONS · RADAR",width/2,y+10,0xFFE1F4FF);
        g.drawTextWithShadow(textRenderer,"PRECIP",x+12,y+35,temperatureMode?0xFF708894:0xFF64EFA6);
        g.drawTextWithShadow(textRenderer,"TEMP",x+86,y+35,temperatureMode?0xFFFFCA77:0xFF708894);
        g.drawTextWithShadow(textRenderer,"1,024 block radius",x+164,y+35,0xFFA0B8C9);
        int xx=x+34, yy=y+53, size=6;
        for(int z=0;z<32;z++)for(int a=0;a<32;a++){
            int index=z*32+a;
            int degree=radar.centerF()+radar.deltaF()[index];
            int code=displayed[index]&255;
            int color=temperatureMode?temperatureColor(degree):precipColor(code,degree);
            g.fill(xx+a*size,yy+z*size,xx+(a+1)*size,yy+(z+1)*size,color);
        }
        // Crosshair at player's position; surrounding rings show approx 512/1024 blocks.
        int cx=xx+96,cy=yy+96;
        g.fill(cx-4,cy,cx+5,cy+1,0xFFFFFFFF);g.fill(cx,cy-4,cx+1,cy+5,0xFFFFFFFF);
        g.drawTextWithShadow(textRenderer,"X: "+radar.centerX()+"  Z: "+radar.centerZ(),x+16,y+254,0xFFCEE4ED);
        g.drawTextWithShadow(textRenderer,temperatureMode?"Blue = cold     Red = hot":"Green/light  Yellow/mod  Red/heavy",x+16,y+269,0xFFB1C2CD);
        super.render(g,mx,my,delta);
    }
    private static int precipColor(int level,int f){
        if(level==0)return 0xFF142D39;
        if(f<35)return switch(level){case 1->0xFF396B97;case 2->0xFF65A7DC;default->0xFFC6E9FF;};
        return switch(level){case 1->0xFF2F9E65;case 2->0xFFF0C951;default->0xFFE95743;};
    }
    private static int temperatureColor(int f){
        if(f<20)return 0xFF2C4FAD;
        if(f<35)return 0xFF497EBE;
        if(f<55)return 0xFF76B7D2;
        if(f<75)return 0xFFB6C27B;
        if(f<95)return 0xFFFFAE55;
        return 0xFFF4614C;
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        int x=(width-292)/2,y=(height-292)/2;
        if(button==0&&my>=y+30&&my<y+52&&mx>=x&&mx<x+128){
            temperatureMode=mx>=x+70;return true;
        }
        return super.mouseClicked(mx,my,button);
    }
    @Override public boolean shouldPause(){return false;}
}
