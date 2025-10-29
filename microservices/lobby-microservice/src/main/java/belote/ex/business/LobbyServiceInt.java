package belote.ex.business;

import belote.ex.persistance.entity.LobbyEntity;

import java.util.List;

public interface LobbyServiceInt {

     public String createLobby(String lobbyName, String hostId, String gameMode);
     boolean joinLobby(String lobbyId, Integer playerId);
     boolean leaveLobby(String lobbyId, Integer playerId);
     void startGame(String lobbyId);

     LobbyEntity getLobby(String lobbyId);

     List<LobbyEntity> getActiveLobbies();

}
