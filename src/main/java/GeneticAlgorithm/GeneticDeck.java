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

public class GeneticDeck {
	public static final int deckSize = 30;
	private static final Random rand = new Random();
	public List<GeneticCard> cards;
	public Double fitness;
	public String heroClass;

	public GeneticDeck(String heroClass) {
		this.cards = new ArrayList<>();
		this.heroClass = heroClass;
	}

	public static int getDeckSize() {
		return deckSize;
	}

	public List<GeneticCard> getCards() {
		return cards;
	}

	public void setCards(List<GeneticCard> cards) {
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

	public boolean checkCorrectnessAndFix() {
		Dataset data = HearthstoneSpark.dataset;
		Dataset dataset1 = data.filter(data.col("heroClass")
				.equalTo(heroClass).or(data.col("heroClass").equalTo("ANY")));

		HashMap<GeneticCard, Integer> toRemove = new HashMap<>();

		for (GeneticCard card : cards) {
			Integer frequency = Collections.frequency(cards, card);
			Boolean isCorrect = card.getRarity().equals("LEGENDARY") ? frequency < 2 : frequency < 3;
			// remove the card and find replacement
			if (!isCorrect) {
				toRemove.put(card, frequency - 1);
			}
		}

		try {
		for (Map.Entry<GeneticCard, Integer> entry : toRemove.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				cards.remove(entry.getKey());
			}

			List<GeneticCard> cardList = new LinkedList<GeneticCard>(dataset1.filter(data.col("baseManaCost")
					.equalTo(entry.getKey().getBaseManaCost())).as(HearthstoneSpark.geneticBean).collectAsList());

			for (int i = 0; (i < entry.getValue()) && (cards.size() != deckSize) && !cardList.isEmpty();) {
				int next = rand.nextInt(cardList.size());
				GeneticCard gCard = cardList.get(next);
				if (canAddCardToDeck(gCard)) {
					cards.add(gCard);
					i++;
				}
				cardList.remove(gCard);
			}
		} }
		catch (Exception e) {
			System.out.println("da");

		}

		return cards.size() == deckSize;
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
		StringBuilder deck = new StringBuilder("[ ");
		for (GeneticCard card : cards) {
			deck.append(card.toString()).append(" ");
		}
		deck.append("]");
		return deck.toString();
	}

	// TODO: mana curve

}
