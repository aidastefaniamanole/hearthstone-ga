package GeneticAlgorithm;

import org.json.simple.JSONObject;

public class Card {
	String name;
	Long baseManaCost;
	String heroClass;
	String cardType;
	public Card(JSONObject cardInfo) {
		this.name = (String) cardInfo.get("name");
		this.baseManaCost = (Long) cardInfo.get("baseManaCost");
		this.heroClass = (String) cardInfo.get("heroClass");
		this.cardType = (String) cardInfo.get("type");
	}

	public String getName() {
		return name;
	}

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
		SPELL,
		WEAPON
	}
}
