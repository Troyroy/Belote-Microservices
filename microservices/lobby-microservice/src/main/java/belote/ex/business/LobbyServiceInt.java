package belote.ex.business;

import belote.ex.persistance.entity.LobbyEntity;
import belote.ex.persistance.entity.UserEntity;

public interface LobbyServiceInt {
     int createLobby(UserEntity user);

     void joinLobby(UserEntity user, int lobbyID);
     LobbyEntity getLobby(int id);

     void deleteLobby(int id);
}
