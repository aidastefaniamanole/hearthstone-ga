package console;

import GeneticAlgorithm.GeneticCard;
import GeneticAlgorithm.GeneticDeck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.GreedyOptimizeMove;
import net.demilich.metastone.game.behaviour.GreedyOptimizeTurn;
import net.demilich.metastone.game.behaviour.IBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.WeightedHeuristic;
import net.demilich.metastone.game.behaviour.threat.FeatureVector;
import net.demilich.metastone.game.behaviour.threat.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.statistics.GameStatistics;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MetaStoneSim {

    private static final Logger logger = LoggerFactory.getLogger(MetaStoneSim.class);

    private static final DeckFormat deckFormat;
    private static final List<Deck> decks;
    private static final Random rand = new Random();

    static {
        //Define deck format
        deckFormat = new DeckFormat();
        for (CardSet set : new CardSet[]{CardSet.ANY, CardSet.BASIC, CardSet.CLASSIC, CardSet.REWARD, CardSet.PROMO, CardSet.HALL_OF_FAME}) {
            deckFormat.addSet(set);
        }

        //Load cards
        try {
            //CardCatalogue.copyCardsFromResources();
            CardCatalogue.loadLocalCards();
        } catch (Exception e) {
            logger.info("Fail loading cards metastone", e);
        }
        //Load decks
        DeckProxy dp = new DeckProxy();
        try {
            dp.loadDecks();
        } catch (Exception e) {
            logger.info("Fail loading decks metastone", e);
        }

        decks = dp.getDecks();
    }

    public static void main(String[] args) {
        //Save json
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(simulate(args));
        PrintWriter writer = new PrintWriter(System.out);
        writer.println(json);
        writer.flush();
        writer.close();
    }

    public static PlayersGameStatistics simulate(String[] args) {
        Options options = new Options();

        Option arg = new Option("d1", "deckD1", true, "name deck for player 1");
        arg.setRequired(true);
        options.addOption(arg);

        arg = new Option("d2", "deckD2", true, "name deck for player 2");
        arg.setRequired(true);
        options.addOption(arg);

        arg = new Option("ai", "aiLevel", true, "AI level");
        arg.setRequired(true);
        options.addOption(arg);

        arg = new Option("s", "simCount", true, "Simulations Count");
        arg.setRequired(true);
        options.addOption(arg);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.info("Fail argument parsing", e);
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String d1Name = cmd.getOptionValue("deckD1");
        String d2Name = cmd.getOptionValue("deckD2");
        int simulationsCount = Integer.parseInt(cmd.getOptionValue("simCount"));
        int aiLevel = Integer.parseInt(cmd.getOptionValue("aiLevel"));

        //Simulate
        Deck d1 = decks.stream().filter(d -> d.getName().equals(d1Name)).findFirst().get();
        Deck d2 = decks.stream().filter(d -> d.getName().equals(d2Name)).findFirst().get();
        GameConfig gc = GetGameConfig(d1, d2, deckFormat, aiLevel, simulationsCount);

        return Simulate(gc);
    }

    private static Deck adaptToMetaStone(GeneticDeck deck) {
        List<String> deckIds = deck.getCards().stream().map(GeneticCard::getRowkey).collect(Collectors.toList());
        return DeckProxy.parseStandardDeck("CustomDeck", HeroClass.getEnumFromValue(deck.getHeroClass()), deckIds);

    }

    public static PlayersGameStatistics simulate(GeneticDeck deck) {
        Deck d1 = adaptToMetaStone(deck);
        d1.setName("custom");
        //Deck d2 = decks.stream().filter(d -> d.getName().equals("Burgle Rogue")).findFirst().get();
        Deck d2 = decks.get(new Random().nextInt(decks.size()));

        GameConfig gc = GetGameConfig(d1, d2, deckFormat, 1, 20);

        return Simulate(gc);
    }

    public static GameStatistics simulateAllDecksSpark(GeneticDeck deck, Integer simulationsCount) {
        Deck d1 = adaptToMetaStone(deck);
        d1.setName("custom");
        //Deck d2 = decks.stream().filter(d -> d.getName().equals("Burgle Rogue")).findFirst().get();
        //Deck d2 = decks.get(rand.nextInt(decks.size()));
        GameStatistics custom = new GameStatistics();

        decks.forEach(x -> {
            GameConfig gc = GetGameConfig(d1, x, deckFormat, 1, simulationsCount);
            custom.merge(Simulate(gc).getPlayer1Statistics());
        });

        return custom;
    }

    public static PlayersGameStatistics simulate(GeneticDeck deck, int aiLevel, int simulationsCount) {
        Deck d1 = adaptToMetaStone(deck);
        Deck d2 = decks.get(rand.nextInt(decks.size()));

        GameConfig gc = GetGameConfig(d1, d2, deckFormat, aiLevel, simulationsCount);

        return Simulate(gc);
    }

    public static List<PlayersGameStatistics> simulateAll(GeneticDeck deck, int aiLevel, int simulationsCount) {
        Deck d1 = adaptToMetaStone(deck);
        List<PlayersGameStatistics> results = new ArrayList<>();

        decks.forEach(x -> {
            GameConfig gc = GetGameConfig(d1, x, deckFormat, aiLevel, simulationsCount);
            results.add(Simulate(gc));
        });

        return results;
    }

    protected static HeroCard getHeroCardForClass(HeroClass heroClass) {
        for (Card card : CardCatalogue.getHeroes()) {
            HeroCard heroCard = (HeroCard) card;
            if (heroCard.getHeroClass().equals(heroClass)) {
                return heroCard;
            }
        }
        return null;
    }

    private static GameConfig GetGameConfig(Deck deck1, Deck deck2, DeckFormat format, int aiLevel, int simulationsCount) {
        PlayerConfig player1Config = new PlayerConfig(deck1, GetAIBehaviour(aiLevel));// new GameStateValueBehaviour(FeatureVector.getFittest(), "a"));
        player1Config.setName("Player 1");
        player1Config.setHeroCard(getHeroCardForClass(deck1.getHeroClass()));

        PlayerConfig player2Config = new PlayerConfig(deck2, GetAIBehaviour(aiLevel));// new GameStateValueBehaviour(FeatureVector.getFittest(), "b"));
        player2Config.setName("Player 2");
        player2Config.setHeroCard(getHeroCardForClass(deck2.getHeroClass()));

        GameConfig gc = new GameConfig();
        gc.setPlayerConfig1(player1Config);
        gc.setPlayerConfig2(player2Config);
        gc.setNumberOfGames(simulationsCount);
        gc.setDeckFormat(format);

        return gc;
    }

    private static IBehaviour GetAIBehaviour(int aiLevel) {
        switch (aiLevel) {
            case 0:
                return new PlayRandomBehaviour();
            case 1:
                return new GreedyOptimizeMove(new WeightedHeuristic());
            case 2:
                return new GreedyOptimizeTurn(new WeightedHeuristic());
            case 3:
                return new GameStateValueBehaviour(FeatureVector.getFittest(), "(fittest)");
            default:
                throw new ArrayIndexOutOfBoundsException(aiLevel);
        }
    }

    public static PlayersGameStatistics Simulate(GameConfig gameConfig) {
        GameStatistics p1stats = new GameStatistics(), p2stats = new GameStatistics();

        for (int i = 0; i < gameConfig.getNumberOfGames(); i++) {
            Player player1 = new Player(gameConfig.getPlayerConfig1());
            Player player2 = new Player(gameConfig.getPlayerConfig2());

            GameContext context = new GameContext(player1, player2, new GameLogic(), gameConfig.getDeckFormat());

            context.play();

            p1stats.merge(context.getPlayer1().getStatistics());
            p2stats.merge(context.getPlayer2().getStatistics());

            context.dispose();
        }

        return new PlayersGameStatistics(p1stats, p2stats);
    }

    private static GameStatistics[] Simulate(Deck deck1, Deck deck2, DeckFormat format, int simulationsCount) {
        GameStatistics p1stats = new GameStatistics(), p2stats = new GameStatistics();

        PlayerConfig player1Config = new PlayerConfig(deck1, new PlayRandomBehaviour());
        player1Config.setName("Player 1");
        player1Config.setHeroCard(getHeroCardForClass(deck1.getHeroClass()));

        PlayerConfig player2Config = new PlayerConfig(deck2, new PlayRandomBehaviour());
        player2Config.setName("Player 2");
        player2Config.setHeroCard(getHeroCardForClass(deck2.getHeroClass()));

        for (int i = 0; i < simulationsCount; i++) {
            Player player1 = new Player(player1Config);
            Player player2 = new Player(player2Config);

            GameContext context = new GameContext(player1, player2, new GameLogic(), format);

            context.play();

            p1stats.merge(context.getPlayer1().getStatistics());
            p2stats.merge(context.getPlayer2().getStatistics());

            context.dispose();
        }

        return new GameStatistics[]{p1stats, p2stats};
    }
}