package belote.ex.business;

import belote.ex.persistance.entity.GameEntity;
import java.util.Set;

public interface GameStateServiceInt {
    public String createGame(GameEntity game, String lobbyID);
    public void saveGame(GameEntity game) ;

    // Get game by ID
    public GameEntity getGame(String gameId);
    // Delete game (for finished/abandoned games)
    public void deleteGame(String gameId);

    // Get active game count - counts all game:* keys
    public long getActiveGameCount();

    // Get all active game IDs
    public Set<String> getActiveGameIds();

    // Check if game exists
    public boolean gameExists(String gameId);

    // Update game status
    public void updateGameStatus(String gameId, String status) ;

    // Extend game TTL (for long games)
    public void extendGameTTL(String gameId);
}
