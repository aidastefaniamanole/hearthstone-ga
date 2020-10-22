package GameResources;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class GameStatistics implements Cloneable {

	private final Map<Statistic, Object> stats = new EnumMap<>(Statistic.class);
	private final Map<String, Map<Integer, Integer>> cardsPlayed = new HashMap<>();
	private final Map<String, Map<Integer, Integer>> minionsSummoned = new HashMap<>();
	private final Map<String, Map<Integer, Integer>> permanentsSummoned = new HashMap<>();

	private void add(Statistic key, long value) {
		if (!stats.containsKey(key)) {
			stats.put(key, 0L);
		}
		long newValue = getLong(key) + value;
		stats.put(key, newValue);
	}

	@Override
	public GameStatistics clone() {
		GameStatistics clone = new GameStatistics();
		clone.stats.putAll(stats);
		clone.getCardsPlayed().putAll(getCardsPlayed());
		clone.getMinionsSummoned().putAll(getMinionsSummoned());
		clone.getPermanentsSummoned().putAll(getPermanentsSummoned());
		return clone;
	}

	public boolean contains(Statistic key) {
		return stats.containsKey(key);
	}

	public Object get(Statistic key) {
		return stats.get(key);
	}

	public Map<String, Map<Integer, Integer>> getCardsPlayed() {
		return cardsPlayed;
	}
	
	public Map<String, Map<Integer, Integer>> getMinionsSummoned() {
		return minionsSummoned;
	}
	
	public Map<String, Map<Integer, Integer>> getPermanentsSummoned() {
		return permanentsSummoned;
	}

	public double getDouble(Statistic key) {
		return stats.containsKey(key) ? (double) stats.get(key) : 0.0;
	}

	public long getLong(Statistic key) {
		return stats.containsKey(key) ? (long) stats.get(key) : 0L;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[GameStatistics]\n");
		for (Statistic stat : stats.keySet()) {
			builder.append(stat);
			builder.append(": ");
			builder.append(stats.get(stat));
			builder.append("\n");
		}
		return builder.toString();
	}
}
