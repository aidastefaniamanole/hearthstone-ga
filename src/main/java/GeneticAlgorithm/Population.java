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

	public Population evolve() {
		Population newGeneration = new Population(this.populationSize);

		// select K individuals
		// get e * K individuals with the highest fitness
		members.sort(Comparator.comparing(GeneticDeck::getFitness).reversed());
		int last = (int) (K * eliteP);
		newGeneration.members.addAll(members.subList(0, last));

		// get r * K random individuals to maintain diversity
		members.subList(0, last).clear();

		int noRandIndividuals = (int) (K * randomP);
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
		Evaluator.calculateFitness(offsprings);

		offsprings.sort(Comparator.comparing(GeneticDeck::getFitness).reversed());
		newGeneration.members.addAll(offsprings.subList(0, K));
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
		int index2 = rand.nextInt(populationSize);
		GeneticDeck randDeck = members.get(index2);

		// we chose +1/-1 cards so we avoid picking the same card
		List<GeneticCard> replacements = randDeck.getCards().stream()
				.filter(x -> Math.abs(toSwap.getBaseManaCost() - x.getBaseManaCost()) == 1)
				.collect(Collectors.toList());

		// swap the card if possible
		if (replacements.size() != 0) {
			GeneticCard toRemove = deck.cards.get(index1);
			deck.cards.remove(index1);
			Boolean swapped = false;
			for (int i = 0; i < replacements.size(); i++) {
				if (deck.canAddCardToDeck(replacements.get(i))) {
					deck.getCards().set(index1, toSwap);
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
		offspring1.getCards().addAll(parent1.getCards().subList(0, crossPoint));
		offspring1.getCards().addAll(parent2.getCards().subList(crossPoint + 1, GeneticDeck.deckSize));
		GeneticDeck offspring2 = new GeneticDeck(parent2.heroClass);
		offspring2.getCards().addAll(parent2.getCards().subList(0, crossPoint));
		offspring2.getCards().addAll(parent1.getCards().subList(crossPoint + 1, GeneticDeck.deckSize));

		ArrayList<GeneticDeck> result = new ArrayList<>();
		result.add(offspring1);
		result.add(offspring2);
		return result;
	}

	/**
	 * Select a member of the population using tournament selection
	 *
	 * @return
	 */
	public GeneticDeck select() {
		ArrayList<GeneticDeck> tournament = new ArrayList<>(tournamentRounds);

		for (int i = 0; i < tournamentRounds; i++) {
			tournament.add(members.get(rand.nextInt(members.size())));
		}

		tournament.sort(Comparator.comparing(GeneticDeck::getFitness).reversed());
		return tournament.get(0);
	}
}
