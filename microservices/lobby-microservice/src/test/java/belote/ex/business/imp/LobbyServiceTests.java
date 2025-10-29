//package belote.ex.business.imp;
//
//import belote.ex.persistance.LobbyRepositoryInt;
//import belote.ex.persistance.entity.LobbyEntity;
//import belote.ex.persistance.entity.UserEntity;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.util.ArrayList;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//@DataJpaTest
// class LobbyServiceTests {
//
//    @Mock
//    private LobbyRepositoryInt repository;
//
//    @InjectMocks
//    private LobbyService service;
//
//
//
//    @Test
//     void testCreateLobby() {
//        UserEntity user;
//        user = new UserEntity();
//        when(repository.createLobby(any())).thenReturn(1);
//        int lobbyId = service.createLobby(user);
//        assertEquals(1, lobbyId);
//    }
//
//    @Test
//     void testJoinLobby() {
//        UserEntity user = new UserEntity();
//        LobbyEntity lobby = LobbyEntity.builder().id(1).players(new ArrayList<>()).build();
//        lobby.getPlayers().add(new UserEntity());
//        when(repository.getLobby(1)).thenReturn(lobby);
//        service.joinLobby(user,1);
//        assertEquals(1,lobby.getPlayers().size());
//    }
//
//    @Test
//     void testGetLobby() {
//        LobbyEntity lobby = LobbyEntity.builder().id(1).players(new ArrayList<>()).build();
//        when(repository.getLobby(1)).thenReturn(lobby);
//        LobbyEntity result = service.getLobby(1);
//        assertEquals(lobby, result);
//    }
//
//    @Test
//     void testDeleteLobby() {
//        service.deleteLobby(1);
//        when(repository.getLobby(1)).thenReturn(null);
//        LobbyEntity result = service.getLobby(1);
//        assertEquals(null, result);
//    }
//}
