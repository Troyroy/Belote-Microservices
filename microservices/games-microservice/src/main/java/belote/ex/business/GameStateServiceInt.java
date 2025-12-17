package belote.ex.business;

import belote.ex.persistance.entity.GameEntity;
import java.util.Set;

public interface GameStateServiceInt {
    public String createGame(GameEntity game, String lobbyID);
    public void saveGame(GameEntity game) ;

    public GameEntity getGame(String gameId);
    public void deleteGame(String gameId);

    public long getActiveGameCount();

    public Set<String> getActiveGameIds();

    public boolean gameExists(String gameId);

    public void updateGameStatus(String gameId, String status) ;

    public void extendGameTTL(String gameId);
}
