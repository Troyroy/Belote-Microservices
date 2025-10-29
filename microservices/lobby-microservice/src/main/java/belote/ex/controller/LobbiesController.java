package belote.ex.controller;

import belote.ex.business.LobbyServiceInt;
import belote.ex.business.UserServiceInt;
import belote.ex.domain.GetUserResponse;
import belote.ex.persistance.entity.LobbyEntity;
import belote.ex.persistance.entity.UserEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;

@RestController
@RequestMapping("/lobby")
@AllArgsConstructor

public class LobbiesController {
    LobbyServiceInt lobbyService;
    UserServiceInt userService;
    private final SimpMessagingTemplate messagingTemplate;


    @PostMapping("{userID}")
    public int createLobby(@PathVariable int userID) {

        GetUserResponse response = userService.getUser(userID);

        UserEntity user = UserEntity.builder()
                .id(response.getId())
                .username(response.getUsername())
                .email(response.getEmail())
                .password(response.getPassword())
                .build();

        return  lobbyService.createLobby(user);
    }

    @PostMapping("{userID}/{lobbyID}")
    public void joinLobby(@PathVariable int userID, @PathVariable int lobbyID) throws JsonProcessingException {
        GetUserResponse response = userService.getUser(userID);

        UserEntity user = UserEntity.builder()
                .id(response.getId())
                .username(response.getUsername())
                .email(response.getEmail())
                .password(response.getPassword())
                .build();

        var mapper = new ObjectMapper();
        String lobbyTo = MessageFormat.format("/lobby/{0}", lobbyID);

        lobbyService.joinLobby(user,lobbyID);
        messagingTemplate.convertAndSend(lobbyTo, mapper.writeValueAsString(lobbyService.getLobby(lobbyID)));

    }

    @GetMapping("{id}")
    public LobbyEntity getLobby(@PathVariable int id) {
        return lobbyService.getLobby(id);
    }

    @DeleteMapping("{id}")
    public void deleteLobby(@PathVariable int id) {
        lobbyService.deleteLobby(id);
    }
}
