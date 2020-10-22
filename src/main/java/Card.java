import org.json.simple.JSONObject;

public class Card {
    public enum HeroClass {
        DRUID,
        PALADIN,
        PRIEST,
        WARLOCK,
        WARRIOR,
        MAGE,
        ROGUE,
        SHAMAN,
        ANY
    }

    String name;
    Integer baseManaCost;
    HeroClass heroClass;
    JSONObject cardInfo;

    public Card(JSONObject cardInfo) {
        this.cardInfo = cardInfo;
        this.name = (String) cardInfo.get("name");
        this.baseManaCost = (Integer) cardInfo.get("baseManaCost");
        this.heroClass = (HeroClass) cardInfo.get("heroClass");
    }
}
