import dev.easton.regionalseasons.RadarMath;
import dev.easton.regionalseasons.ExposureRules;
import java.util.Random;
public class RadarAndExposureTest {
    private static void require(boolean condition,String why){if(!condition)throw new AssertionError(why);}
    public static void main(String[] args){
        long seed=814L;
        double a=RadarMath.moisture(100,140,1000,seed);
        require(a>=0 && a<=1,"bounded radar data");
        require(a==RadarMath.moisture(100,140,1000,seed),"deterministic across clients");
        require(Math.abs(a-RadarMath.moisture(101,140,1000,seed))<.02,"no abrupt one-block weather discontinuity");
        require(RadarMath.intensity(100,140,1000,seed,false)==0,"no precipitation in clear weather");
        require(RadarMath.RADIUS_BLOCKS==1024&&RadarMath.PIXELS*RadarMath.PIXELS==1024,"radar sized correctly");
        for(int i=0;i<1000;i++){
            int v=RadarMath.intensity(i*17,i*7,i*127,seed,true);
            require(v>=0&&v<=3,"storm intensity bounds");
        }
        require(RadarMath.estimatedSnowLayers(3)>RadarMath.estimatedSnowLayers(1),"blizzard deeper forecast");
        require(ExposureRules.atRisk(27.99)&&!ExposureRules.atRisk(28),"cold threshold");
        require(ExposureRules.COLD_MIN_TICKS==8400&&ExposureRules.COLD_MAX_TICKS==12000,"7-10 minute cold duration");
        Random r=new Random(5);
        for(int i=0;i<100;i++){
            int length=ExposureRules.coldDurationTicks(r);
            require(length>=8400&&length<=12000,"duration bounded");
        }
        require(ExposureRules.FULL_ARMOR_CHANCE<ExposureRules.HELMET_CHANCE,"armor more protective");
        System.out.println("PASS: deterministic radar, gradual cell transitions, cold/exposure limits and 1000 sample intensity bounds");
    }
}
