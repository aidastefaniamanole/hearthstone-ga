package GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Objects;

public class GeneticDeck {
	public static final int deckSize = 30;
	public ArrayList<GeneticCard> cards;
	public Double fitness;
	public GeneticCard.HeroClass heroClass;

	public static int getDeckSize() {
		return deckSize;
	}

	public ArrayList<GeneticCard> getCards() {
		return cards;
	}

	public void setCards(ArrayList<GeneticCard> cards) {
		this.cards = cards;
	}

	public GeneticCard.HeroClass getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(GeneticCard.HeroClass heroClass) {
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

	public Integer checkCorrectness() {
		// extract cards that appear more than twice or if the card is a legendary

		return 0;
	}

	public Double getFitness() {
		return fitness;
	}

	public void setFitness(Double fitness) {
		this.fitness = fitness;
	}

	// TODO: mana curve

}
