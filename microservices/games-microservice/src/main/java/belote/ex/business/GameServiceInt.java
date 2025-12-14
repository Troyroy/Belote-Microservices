package belote.ex.business;

import belote.ex.events.LobbyReadyEvent;
import belote.ex.persistance.entity.CardEntity;
import belote.ex.persistance.entity.GameEntity;
import belote.ex.persistance.entity.LobbyEntity;

import java.util.List;

public interface GameServiceInt {

    public String createGameFromLobby(LobbyReadyEvent event);
    List<CardEntity> getDeck(GameEntity game);

     GameEntity getGame(String gameID);
     void playCard(int id, int cardID,GameEntity game,int milSeconds);

     void playCardAsync(int id, int cardID,GameEntity game,int milSeconds);

     void startRound(GameEntity game);
}
