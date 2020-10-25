package GeneticAlgorithm;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.logic.GameLogic;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SQLContext;
import org.jcodings.util.Hash;

import java.io.Serializable;
import java.util.*;

public class GeneticDeck implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int deckSize = 30;
	public ArrayList<GeneticCard> cards;
	public Double fitness;
	public String heroClass;

	public GeneticDeck(String heroClass) {
		this.cards = new ArrayList<GeneticCard>();
		this.heroClass = heroClass;
	}

	public static int getDeckSize() {
		return deckSize;
	}

	public ArrayList<GeneticCard> getCards() {
		return cards;
	}

	public void setCards(ArrayList<GeneticCard> cards) {
		this.cards = cards;
	}

	public String getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(String heroClass) {
		this.heroClass = heroClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GeneticDeck deck = (GeneticDeck) o;
		return Objects.equals(cards, deck.cards) &&
				Objects.equals(fitness, deck.fitness) &&
				heroClass == deck.heroClass;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cards, fitness, heroClass);
	}
	
	public void checkCorrectnessAndFix() {
		Map<String, String> optionsMap = new HashMap<>();

		optionsMap.put("catalog", HearthstoneSpark.catalog);
		Dataset dataset = HearthstoneSpark.getSQLContext().read().options(optionsMap)
				.format("org.apache.spark.sql.execution.datasources.hbase").load();

		Dataset dataset1 = dataset.filter(dataset.col("heroClass")
				.equalTo(heroClass).or(dataset.col("heroClass").equalTo("ANY")));

		HashMap<GeneticCard, Integer> toRemove = new HashMap<>();

		for (GeneticCard card : cards) {
			Integer frequency = Collections.frequency(cards, card);
			Boolean isCorrect = card.getRarity().equals("LEGENDARY") ? frequency < 2 : frequency < 3;
			// remove the card and find replacement
			if (!isCorrect) {
				toRemove.put(card, frequency - 1);
			}
		}

		for (Map.Entry<GeneticCard, Integer> entry : toRemove.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				cards.remove(entry.getKey());
			}
			for (int i = 0; i < entry.getValue(); i++) {
				List<GeneticCard> cardList = dataset1.filter(dataset.col("baseManaCost")
						.equalTo(entry.getKey().getBaseManaCost())).as(Encoders.bean(GeneticCard.class)).collectAsList();
				System.out.println("Replacement cards " + cardList.size());
				for (GeneticCard gCard : cardList) {
					if (canAddCardToDeck(gCard)) {
						cards.add(gCard);
						break;
					}
				}
			}
		}
	}

	public int containsHowMany(GeneticCard card) {
		return Collections.frequency(cards, card);
	}

	public boolean canAddCardToDeck(GeneticCard card) {
		int cardInDeckCount = containsHowMany(card);
		return card.getRarity().equals("LEGENDARY") ? cardInDeckCount < 1 : cardInDeckCount < 2;
	}

	public Double getFitness() {
		return fitness;
	}

	public void setFitness(Double fitness) {
		this.fitness = fitness;
	}

	@Override
	public String toString() {
		String deck = "[ ";
		for (GeneticCard card : cards) {
			deck += card.toString() + " ";
		}
		deck += "]";
		return deck;
	}

	// TODO: mana curve

}
