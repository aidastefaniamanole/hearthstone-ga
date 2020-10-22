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

    public enum CardType {
        MINION,
        SPELL
    }

    String name;
    Integer baseManaCost;
    HeroClass heroClass;
    CardType cardType;

    public Card(JSONObject cardInfo) {
        this.name = (String) cardInfo.get("name");
        this.baseManaCost = (Integer) cardInfo.get("baseManaCost");
        this.heroClass = (HeroClass) cardInfo.get("heroClass");
        this.cardType = (CardType) cardInfo.get("type");
    }
}
