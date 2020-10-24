package GeneticAlgorithm;

import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Objects;

public class GeneticCard implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private Long baseManaCost;
	private String heroClass;
	private String cardType;
	private String rowkey;
	private String rarity;

	public GeneticCard() {}

	public GeneticCard(JSONObject cardInfo, String rowkey) {
		this.name = (String) cardInfo.get("name");
		this.baseManaCost = (Long) cardInfo.get("baseManaCost");
		this.heroClass = (String) cardInfo.get("heroClass");
		this.cardType = (String) cardInfo.get("type");
		this.rarity = (String) cardInfo.get("rarity");
		this.rowkey = rowkey;
	}

	public String getRowkey() {
		return rowkey;
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

	public String getRarity() { return  rarity; }

	public void setName(String name) {
		this.name = name;
	}

	public void setBaseManaCost(Long baseManaCost) {
		this.baseManaCost = baseManaCost;
	}

	public void setHeroClass(String heroClass) {
		this.heroClass = heroClass;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public void setRowkey(String rowKey) {
		this.rowkey = rowKey;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GeneticCard card = (GeneticCard) o;
		return Objects.equals(name, card.name) &&
				Objects.equals(baseManaCost, card.baseManaCost) &&
				Objects.equals(heroClass, card.heroClass) &&
				Objects.equals(cardType, card.cardType) &&
				Objects.equals(rarity, card.rarity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, baseManaCost, heroClass, cardType);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return rowkey;
	}
}
