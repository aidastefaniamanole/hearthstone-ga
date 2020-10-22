import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class Population {
    Integer populationSize;
    ArrayList<Deck> members;

    Double mutateP = 0.2;
    Double eliteP = 0.8;
    Double randomP = 0.2;
    Integer K;
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

        // select K individuals: e * K with the highest fitness + r * K random

        // perform crossover for N - K individuals

        // mutate p% individuals

        // recompute fitness for new generation

        return  newGeneration;
    }

    /**
     * Swap a card with a random card or with one of +1/-1 mana cost from a random deck
     * @param deck
     * @return
     */
    public void mutate(Deck deck) {
        // chose a random card from this deck
        int index1 = rand.nextInt(Deck.deckSize);
        Card card1 = deck.cards.get(index1);
        // chose a random deck from the population and filter the cards with +1/-1 manaCost
        int index2 = rand.nextInt(populationSize);
        Deck randDeck = members.get(index2);

        Card toSwap = deck.cards.get(index1);

        Card replacement = null;
        if (deck.heroClass == randDeck.heroClass) {
            // any card will work
            replacement = (Card) randDeck.cards.stream()
                    .filter(x -> Math.abs(toSwap.baseManaCost - x.baseManaCost) <= 1)
                    .collect(Collectors.toList())
                    .get(0);
        } else {
            // the card does not have to be class specific
            replacement = (Card) randDeck.cards.stream()
                    .filter(x -> (Math.abs(toSwap.baseManaCost - x.baseManaCost) <= 1) &&
                            ((x.heroClass == deck.heroClass) || (x.heroClass == Card.HeroClass.ANY)))
                    .collect(Collectors.toList())
                    .get(0);
        }
        //swap the cards if possible
        if (replacement != null) {
            deck.cards.set(index1, toSwap);
        }
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
     * @param evaluator
     * @return
     */
    public Deck select(Evaluator evaluator) {
        return null;
    }
}
