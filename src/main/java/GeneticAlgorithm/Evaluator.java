package GeneticAlgorithm;

import console.MetaStoneSim;
import console.PlayersGameStatistics;

import java.util.ArrayList;

public class Evaluator {

	public static void calculateFitness(ArrayList<GeneticDeck> offsprings) {
		offsprings.forEach(Evaluator::calculateFitness);
	}

	public static void calculateFitness(GeneticDeck child) {
		PlayersGameStatistics result = MetaStoneSim.simulate(child);

		child.setFitness(result.getPlayer1Statistics().getWinRate());
	}
}
