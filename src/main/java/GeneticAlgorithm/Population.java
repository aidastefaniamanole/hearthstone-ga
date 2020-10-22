package GeneticAlgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class Population {
    Integer populationSize = 10;
    // [2-4]
    public static final int tournamentSize = 4;
    ArrayList<Deck> members;

    Double mutateP = 0.1;
    Double eliteP = 0.8;
    Double randomP = 0.2;
    Integer K = 4;
    Random rand;

    public Population(Integer populationSize) {
        this.populationSize = populationSize;
        this.members = new ArrayList<>(populationSize);
        this.rand = new Random();
    }

    /**
     * Add an organism to the population
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



        // mutate mutateP individuals
        int toMutate = (int) (members.size() * mutateP);
        for (int i = 0; i < toMutate; i++) {
            int randI = rand.nextInt(members.size());
            offsprings.add(mutate(members.get(randI)));
        }

        // recompute fitness for new members and add them to the new generation

        newGeneration.members.addAll(offsprings);
        return newGeneration;
    }

    /**
     * Swap a card with a random card or with one of +1/-1 mana cost from a random deck
     * @param deck
     * @return
     */
    public Deck mutate(Deck deck) {
        // chose a random card from this deck
        int index1 = rand.nextInt(Deck.deckSize);
        Card card1 = deck.cards.get(index1);
        // chose a random deck from the population and filter the cards with +1/-1 manaCost
        int index2 = rand.nextInt(populationSize);
        Deck randDeck = members.get(index2);

        Card toSwap = deck.cards.get(index1);

        Card replacement = null;
        // we chose +1/-1 cards so we avoid picking the same card
        if (deck.heroClass == randDeck.heroClass) {
            // any card will work
            replacement = (Card) randDeck.cards.stream()
                    .filter(x -> Math.abs(toSwap.baseManaCost - x.baseManaCost) == 1)
                    .collect(Collectors.toList())
                    .get(0);
        } else {
            // the card does not have to be class specific
            replacement = (Card) randDeck.cards.stream()
                    .filter(x -> (Math.abs(toSwap.baseManaCost - x.baseManaCost) == 1) &&
                            ((x.heroClass == deck.heroClass) || (x.heroClass == Card.HeroClass.ANY)))
                    .collect(Collectors.toList())
                    .get(0);
        }
        //swap the cards if possible
        if (replacement != null) {
            deck.cards.set(index1, toSwap);
        }

        return deck;
    }

    /**
     * Mix two decks
     * @param parent1
     * @param parent2
     * @return
     */
    public Deck crossover(Deck parent1, Deck parent2) {
        // select both parents using tournament selection

        return null;
    }

    /**
     * Select a member of the population using tournament selection
     * @return
     */
    public Deck select() {


        return null;
    }
}
