package GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class Deck {
	public static final int deckSize = 30;
	public ArrayList<Card> cards;
	public Integer fitness;
	public Card.HeroClass heroClass;

	@Override
	public boolean equals(Object o) {
		Deck deck = (Deck) o;

		cards.sort(Comparator.comparing(Card::getName));
		deck.cards.sort(Comparator.comparing(Card::getName));

		Iterator<Card> i1 = cards.iterator();
		Iterator<Card> i2 = deck.cards.iterator();

		while (i1.hasNext()) {
			if (!i1.next().name.equals(i2.next().name)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cards, fitness, heroClass);
	}

	public Integer checkCorrectness() {
		// extract cards that appear more than twice or if the card is a legendary

		return 0;
	}

	public Integer getFitness() {
		return fitness;
	}

	// TODO: mana curve

}
