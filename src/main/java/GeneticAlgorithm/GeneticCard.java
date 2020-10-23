package GeneticAlgorithm;

import org.json.simple.JSONObject;

import java.util.Objects;

public class GeneticCard {
	private String name;
	private Long baseManaCost;
	private String heroClass;
	private String cardType;
	private String id;

	public GeneticCard(JSONObject cardInfo, String id) {
		this.name = (String) cardInfo.get("name");
		this.baseManaCost = (Long) cardInfo.get("baseManaCost");
		this.heroClass = (String) cardInfo.get("heroClass");
		this.cardType = (String) cardInfo.get("type");
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public Long getBaseManaCost() {
		return baseManaCost;
	}

	public String getHeroClass() {
		return heroClass;
	}

	public String getCardType() {
		return cardType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GeneticCard card = (GeneticCard) o;
		return Objects.equals(name, card.name) &&
				Objects.equals(baseManaCost, card.baseManaCost) &&
				Objects.equals(heroClass, card.heroClass) &&
				Objects.equals(cardType, card.cardType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, baseManaCost, heroClass, cardType);
	}

	public String getName() {
		return name;
	}

	public enum HeroClass {
		DRUID,
		HUNTER,
		MAGE,
		PALADIN,
		PRIEST,
		ROGUE,
		SHAMAN,
		WARLOCK,
		WARRIOR,
		ANY
	}

	public enum CardType {
		MINION,
		SPELL,
		WEAPON
	}
}
