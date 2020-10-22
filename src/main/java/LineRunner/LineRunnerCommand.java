package LineRunner;

import GameResources.PlayersGameStatistics;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LineRunnerCommand {
	public static PlayersGameStatistics runMetastone(String[] args) throws IOException {
		int aiLevel = 2;
		int simulationsCount = 2;
		String deck1 = "Wild Pirate Warrior";
		String deck2 = "Midrange Shaman";

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("bash", "-c", "java -cp ~/Desktop/MetaStoneSim.jar net.demilich.metastone.console.MetaStoneSim" +
				" -ai " + aiLevel +
				" -s " + simulationsCount +
				" -d1 \"" + deck1 + "\"" +
				" -d2 \"" + deck2 + "\"");
		Process process = processBuilder.start();
		process.getErrorStream();

		try {
			process.waitFor();
			return printResults(process);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static PlayersGameStatistics printResults(Process process) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		return new Gson().fromJson(reader, PlayersGameStatistics.class);
	}
}
