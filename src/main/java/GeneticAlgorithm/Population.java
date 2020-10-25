package GeneticAlgorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Population implements Serializable {
	private static final long serialVersionUID = 1L;

	// [2-4]
	private int tournamentRounds = 4;
	private Integer populationSize;
	private final ArrayList<GeneticDeck> members;

	// the probability to perform crossover = 0.8, mutation = 0.2
	private final Double operationProb = 0.8;
	private final Double eliteP = 0.8;
	private final Double randomP = 0.2;
	private final Integer K = 4;
	private final Random rand;

	public Population(Integer populationSize) {
		this.populationSize = populationSize;
		this.members = new ArrayList<>();
		this.rand = new Random();
	}

	public ArrayList<GeneticDeck> getMembers() {
		return members;
	}

	/**
	 * Add an organism to the population
	 *
	 * @param deck
	 */
	public void addOrganism(GeneticDeck deck) {
		members.add(deck);
	}

	public Population evolve(Integer simulationsCount) {
		Population newGeneration = new Population(this.populationSize);

		// select K individuals
		// get e * K individuals with the highest fitness
		members.sort(Comparator.comparing(GeneticDeck::getFitness).reversed());
		int last = (int) (K * eliteP);
		for (int i = 0; i < last; i++) {
			newGeneration.members.add(members.get(i));
		}

		// get r * K random individuals to maintain diversity
		members.subList(0, last).clear();

		int noRandIndividuals = (int) Math.ceil((double)K * randomP);
		for (int i = 0; i < noRandIndividuals; i++) {
			int randI = rand.nextInt(members.size());
			newGeneration.addOrganism(members.get(randI));
			members.remove(randI);
		}

		ArrayList<GeneticDeck> offsprings = new ArrayList<>();
		// perform crossover for N - K individuals
		for (int i = 0; i < populationSize - K; i++) {
			// perform crossover or mutate a member of the population
			if (rand.nextDouble() < operationProb) {
				offsprings.addAll(crossover());
			} else {
				mutate(select());
			}
		}

		// compute fitness for new members and add the fittest K individuals to the new generation
		Evaluator.calculateFitness(offsprings, simulationsCount);

		offsprings.sort(Comparator.comparing(GeneticDeck::getFitness).reversed());
		for (int i = 0; i < Math.min(populationSize - K, offsprings.size()); i++) {
			newGeneration.members.add(offsprings.get(i));
		}
		return newGeneration;
	}

	/**
	 * Swap a card with a random card or with one of +1/-1 mana cost from a random deck
	 *
	 * @param deck
	 * @return
	 */
	public void mutate(GeneticDeck deck) {
		// chose a random card from this deck
		int index1 = rand.nextInt(GeneticDeck.deckSize);
		GeneticCard toSwap = deck.getCards().get(index1);
		// chose a random deck from the population and filter the cards with +1/-1 manaCost
		int index2 = rand.nextInt(populationSize - K);
		GeneticDeck randDeck = members.get(index2);

		// we chose +1/-1 cards so we avoid picking the same card
		List<GeneticCard> replacements = randDeck.getCards().stream()
				.filter(x -> Math.abs(toSwap.getBaseManaCost() - x.getBaseManaCost()) == 1)
				.collect(Collectors.toList());

		// swap the card if possible
		if (replacements.size() != 0) {
			GeneticCard toRemove = deck.cards.get(index1);
			deck.cards.remove(toSwap);
			boolean swapped = false;
			for (GeneticCard replacement : replacements) {
				if (deck.canAddCardToDeck(replacement)) {
					deck.getCards().add(replacement);
					swapped = true;
					break;
				}
			}
			if (!swapped) {
				deck.cards.add(toRemove);
			}
		}
	}

	/**
	 * Mix two decks
	 *
	 * @return
	 */
	public ArrayList<GeneticDeck> crossover() {
		// select both parents using tournament selection
		GeneticDeck parent1 = select();
		GeneticDeck parent2 = select();
		// make sure we don't use the same parent twice
		while (parent1.equals(parent2)) {
			parent2 = select();
		}

		// perform one point cross
		int crossPoint = rand.nextInt(GeneticDeck.deckSize);
		GeneticDeck offspring1 = new GeneticDeck(parent1.heroClass);
		for (int i = 0; i < crossPoint; i++) {
			offspring1.getCards().add(parent1.getCards().get(i));
		}
		for (int i = crossPoint; i < GeneticDeck.deckSize; i++) {
			offspring1.getCards().add(parent2.getCards().get(i));
		}
		GeneticDeck offspring2 = new GeneticDeck(parent2.heroClass);
		for (int i = 0; i < crossPoint; i++) {
			offspring2.getCards().add(parent2.getCards().get(i));
		}
		for (int i = crossPoint; i < GeneticDeck.deckSize; i++) {
			offspring2.getCards().add(parent1.getCards().get(i));
		}

		ArrayList<GeneticDeck> result = new ArrayList<>();
		//if (offspring1.checkCorrectnessAndFix()) {
			result.add(offspring1);
		//} else {
		//	result.add(HearthstoneSpark.generateDeck(parent1.heroClass));
		//}
		//if (offspring2.checkCorrectnessAndFix()) {
			result.add(offspring2);
		//} else {
		//	result.add(HearthstoneSpark.generateDeck(parent2.heroClass));
		//}

		return result;
	}

	/**
	 * Select a member of the population using tournament selection
	 *
	 * @return
	 */
	public GeneticDeck select() {
		List<GeneticDeck> tournament = new ArrayList<>();

		for (int i = 0; i < tournamentRounds; i++) {
			tournament.add(members.get(rand.nextInt(members.size())));
		}

		tournament.sort(Comparator.comparing(GeneticDeck::getFitness).reversed());
		return tournament.get(0);
	}
}
