package GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class Population {
	// [2-4]
	private int tournamentRounds = 4;
	private Integer populationSize;
	private final ArrayList<Deck> members;

	// the probability to perform crossover = 0.8, mutation = 0.2
	private final Double operationProb = 0.8;
	private final Double eliteP = 0.8;
	private final Double randomP = 0.2;
	private final Integer K = 4;
	private final Random rand;

	public Population(Integer populationSize) {
		this.populationSize = populationSize;
		this.members = new ArrayList<>(populationSize);
		this.rand = new Random();
	}

	/**
	 * Add an organism to the population
	 *
	 * @param deck
	 */
	public void addOrganism(Deck deck) {
		members.add(deck);
	}

	public Population evolve(Evaluator evaluator) {
		Population newGeneration = new Population(this.populationSize);

		// select K individuals
		// get e * K individuals with the highest fitness
		members.sort(Comparator.comparing(Deck::getFitness).reversed());
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

		ArrayList<Deck> offsprings = new ArrayList<>();
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


		offsprings.sort(Comparator.comparing(Deck::getFitness).reversed());
		newGeneration.members.addAll(offsprings.subList(0, K));
		return newGeneration;
	}

	/**
	 * Swap a card with a random card or with one of +1/-1 mana cost from a random deck
	 *
	 * @param deck
	 * @return
	 */
	public void mutate(Deck deck) {
		// chose a random card from this deck
		int index1 = rand.nextInt(Deck.deckSize);
		Card toSwap = deck.cards.get(index1);
		// chose a random deck from the population and filter the cards with +1/-1 manaCost
		int index2 = rand.nextInt(populationSize);
		Deck randDeck = members.get(index2);

		Card replacement;
		// we chose +1/-1 cards so we avoid picking the same card
		replacement = randDeck.cards.stream()
				.filter(x -> Math.abs(toSwap.baseManaCost - x.baseManaCost) == 1)
				.collect(Collectors.toList())
				.get(0);

		//swap the cards if possible
		if (replacement != null) {
			deck.cards.set(index1, toSwap);
		}
	}

	/**
	 * Mix two decks
	 *
	 * @return
	 */
	public ArrayList<Deck> crossover() {
		// select both parents using tournament selection
		Deck parent1 = select();
		Deck parent2 = select();
		// make sure we don't use the same parent twice
		while (parent1.equals(parent2)) {
			parent2 = select();
		}

		// perform one point cross
		int crossPoint = rand.nextInt(Deck.deckSize);
		Deck offspring1 = new Deck();
		offspring1.cards.addAll(parent1.cards.subList(0, crossPoint));
		offspring1.cards.addAll(parent2.cards.subList(crossPoint + 1, Deck.deckSize));
		Deck offspring2 = new Deck();
		offspring2.cards.addAll(parent2.cards.subList(0, crossPoint));
		offspring2.cards.addAll(parent1.cards.subList(crossPoint + 1, Deck.deckSize));

		ArrayList<Deck> result = new ArrayList<>();
		result.add(offspring1);
		result.add(offspring2);
		return result;
	}

	/**
	 * Select a member of the population using tournament selection
	 *
	 * @return
	 */
	public Deck select() {
		ArrayList<Deck> tournament = new ArrayList<>(tournamentRounds);

		for (int i = 0; i < tournamentRounds; i++) {
			tournament.add(members.get((rand.nextInt(members.size()))));
		}

		tournament.sort(Comparator.comparing(Deck::getFitness).reversed());
		return tournament.get(0);
	}
}
