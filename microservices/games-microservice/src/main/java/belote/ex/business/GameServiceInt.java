package belote.ex.business;

import belote.ex.persistance.entity.CardEntity;
import belote.ex.persistance.entity.GameEntity;
import belote.ex.persistance.entity.LobbyEntity;

import java.util.List;

public interface GameServiceInt {

    List<CardEntity> getDeck(String id);

     GameEntity getGame(String id);

     void playCard(int id, int cardID,String gameID,int milSeconds);

     void startRound(String gameID);
}
