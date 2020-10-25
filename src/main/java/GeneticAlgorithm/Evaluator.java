package GeneticAlgorithm;

import console.MetaStoneSim;
import console.PlayersGameStatistics;
import net.demilich.metastone.game.statistics.GameStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Evaluator {

	private static final Logger logger = LoggerFactory.getLogger(Evaluator.class);

	public static void calculateFitness(ArrayList<GeneticDeck> offsprings, Integer simulationsCount) {
		offsprings.forEach(x -> calculateFitness(x, simulationsCount));
	}

	public static void calculateFitness(GeneticDeck child, Integer simulationsCount) {
		logger.info("Start simulation child");
		GameStatistics result = MetaStoneSim.simulateAllDecksSpark(child, simulationsCount);
		logger.info("Complete simulation child winRate: {}", result.getWinRate());

		child.setFitness(result.getWinRate());
	}
}
