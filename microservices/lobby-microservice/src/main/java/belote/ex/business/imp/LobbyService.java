package belote.ex.business.imp;

import belote.ex.business.LobbyServiceInt;
import belote.ex.persistance.LobbyRepositoryInt;
import belote.ex.persistance.entity.LobbyEntity;
import belote.ex.persistance.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LobbyService implements LobbyServiceInt {

    private LobbyRepositoryInt repository;

    @Override
    public int createLobby(UserEntity user) {
        return  repository.createLobby(user);
    }

    @Override
    public void joinLobby(UserEntity user, int lobbyID) {
       if(repository.getLobby(lobbyID).getPlayers().size() < 4) {
       repository.joinLobby(user,lobbyID);
       }
    }

    @Override
    public LobbyEntity getLobby(int id) {
       return repository.getLobby(id);
    }

    @Override
    public void deleteLobby(int id) {
        repository.deleteLobby(id);
    }
}
