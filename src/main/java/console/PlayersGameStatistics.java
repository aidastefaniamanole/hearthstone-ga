package console;

import net.demilich.metastone.game.statistics.GameStatistics;

public class PlayersGameStatistics {
    private GameStatistics Player1;
    private GameStatistics Player2;

    PlayersGameStatistics(GameStatistics player1Statistics, GameStatistics player2Statistics) {
        Player1 = player1Statistics;
        Player2 = player2Statistics;
    }

    public GameStatistics getPlayer2Statistics() {
        return Player2;
    }

    public GameStatistics getPlayer1Statistics() {
        return Player1;
    }
}
