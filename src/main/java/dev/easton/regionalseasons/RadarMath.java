package dev.easton.regionalseasons;

/** Pure math: deterministic, slowly drifting spatial precipitation bands. NO world block scans. */
public final class RadarMath {
    private RadarMath() {}
    public static final int RADIUS_BLOCKS = 1024;
    public static final int PIXELS = 32;
    public static final int STEP_BLOCKS = 64;
    public static final double RAIN_THRESHOLD = .52;
    private static double hash(int x,int z,long seed) {
        long h=seed + x*0x9E3779B97F4A7C15L + z*0xC2B2AE3D27D4EB4FL;
        h^=h>>>33; h*=0xff51afd7ed558ccdL; h^=h>>>33; h*=0xc4ceb9fe1a85ec53L; h^=h>>>33;
        return (h>>>11)/(double)(1L<<53);
    }
    private static double smooth(double x) { return x*x*(3-2*x); }
    private static double noise(double x,double z,int scale,long seed) {
        double a=x/scale,b=z/scale; int ix=(int)Math.floor(a), iz=(int)Math.floor(b);
        double u=smooth(a-ix),v=smooth(b-iz);
        double n00=hash(ix,iz,seed), n10=hash(ix+1,iz,seed);
        double n01=hash(ix,iz+1,seed), n11=hash(ix+1,iz+1,seed);
        return (n00+(n10-n00)*u)*(1-v)+(n01+(n11-n01)*u)*v;
    }
    /** 0..1; position covers land and sea; moving cloudy fronts (~64 blocks per in-game day). */
    public static double moisture(int x,int z,long worldTicks,long radarSeed) {
        double drift=worldTicks / 380.0;
        double large=noise(x+drift,z-drift*.52,500,radarSeed);
        double small=noise(x+drift*1.5,z+drift*.2,220,radarSeed+104729);
        return Math.clamp(large*.67+small*.33,0,1);
    }
    /** NONE=0; LIGHT=1; MEDIUM=2; HEAVY=3; only active when the global weather is raining. */
    public static int intensity(int x,int z,long ticks,long seed,boolean globalRain) {
        if (!globalRain) return 0;
        double n=moisture(x,z,ticks,seed);
        return n<RAIN_THRESHOLD?0:n<.64?1:n<.76?2:3;
    }
    public static int estimatedSnowLayers(int intensity) {
        return switch(intensity) {case 1->2;case 2->7;case 3->14;default->0;};
    }
    public static double estimatedTemperatureF(double centerF,int x,int z,int cx,int cz,long seed) {
        // Extrapolated from the player's air temperature, NOT a claim to sample distant biomes.
        return centerF + 8*(noise(x,z,600,seed+881)-noise(cx,cz,600,seed+881));
    }
}
