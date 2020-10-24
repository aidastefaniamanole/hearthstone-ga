package GeneticAlgorithm;

import console.MetaStoneSim;
import console.PlayersGameStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Evaluator {

	private static final Logger logger = LoggerFactory.getLogger(Evaluator.class);

	public static void calculateFitness(ArrayList<GeneticDeck> offsprings) {
		offsprings.forEach(Evaluator::calculateFitness);
	}

	public static void calculateFitness(GeneticDeck child) {
		logger.info("Start simulation child");
		PlayersGameStatistics result = MetaStoneSim.simulate(child);
		logger.info("Complete simulation child winRate: {}", result.getPlayer1Statistics().getWinRate());

		child.setFitness(result.getPlayer1Statistics().getWinRate());
	}
}
