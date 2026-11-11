import dev.easton.regionalseasons.SeasonRules;
public class SeasonRulesTest {
  private static void check(boolean actual, String message) { if (!actual) throw new AssertionError(message); }
  public static void main(String[] args) {
    check(SeasonRules.snows(-1), "-1°F should snow");
    check(SeasonRules.snows(34.999), "34.999°F should snow");
    check(!SeasonRules.snows(35), "35°F should rain");
    check(!SeasonRules.snows(83), "83°F should rain");
    check(SeasonRules.addLayer(0)==1,"snow begins at 1");
    check(SeasonRules.addLayer(17)==18,"snow 18 cap");
    check(SeasonRules.addLayer(18)==18,"snow does not exceed 18");
    check(SeasonRules.addLayer(25)==18,"preexisting snow capped");
    check(SeasonRules.accumulationChance(1)<SeasonRules.accumulationChance(2),"light < moderate");
    check(SeasonRules.accumulationChance(2)<SeasonRules.accumulationChance(3),"moderate < heavy");
    check(SeasonRules.meltChance(20)==0,"no thaw below freezing snow threshold");
    check(SeasonRules.meltChance(55)>SeasonRules.meltChance(38),"warm thaws faster");
    System.out.println("PASS: 12 isolated seasonal logic checks");
  }
}
