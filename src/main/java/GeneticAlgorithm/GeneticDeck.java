package GeneticAlgorithm;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.logic.GameLogic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
<<<<<<< HEAD
=======
import java.util.HashMap;
>>>>>>> 23547221fb188e5a377c01c2f56393d8ac94cdb7
import java.util.Objects;

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

	public boolean checkCorrectness() {
		for (GeneticCard card : cards) {
			Integer frequency = Collections.frequency(cards, card);
<<<<<<< HEAD
			Boolean isCorrect = card.getRarity().equals("LEGENDARY") ? frequency < 2 : frequency < 3;
=======
			Boolean isCorrect = card.getRarity().equals("LEGENDARY") ? frequency < 1 : frequency < 2;
>>>>>>> 23547221fb188e5a377c01c2f56393d8ac94cdb7
			if (!isCorrect) {
				return false;
			}
		}
		return true;
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
