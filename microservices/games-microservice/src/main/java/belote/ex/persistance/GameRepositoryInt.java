package belote.ex.persistance;

import belote.ex.persistance.entity.GameEntity;
import belote.ex.persistance.entity.LobbyEntity;

import java.util.List;

public interface GameRepositoryInt {

    List<GameEntity> getAllGames();
    GameEntity getGame(int id);

    int createGame(LobbyEntity lobby);

    void deleteGame(int id);
}
