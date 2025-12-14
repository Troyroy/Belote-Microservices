package belote.ex.business.imp;


import belote.ex.business.LobbyServiceInt;
import belote.ex.config.LobbyRabbitMQConfig;
import belote.ex.events.LobbyReadyEvent;
import belote.ex.persistance.entity.LobbyEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class LobbyService implements LobbyServiceInt {



    private RabbitTemplate rabbitTemplate;
    private final LobbyStateService lobbyStateService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private static final String LOBBY_EVENTS_CHANNEL = "lobby:events";

    public String createLobby(String lobbyName, String hostId, String gameMode) {
        LobbyEntity lobby = new LobbyEntity(null, hostId);
        lobby.addPlayer(Integer.parseInt(hostId)); // Host joins automatically

        String lobbyId = lobbyStateService.createLobby(lobby);

        log.info("Created lobby {} by host {}", lobbyId, hostId);
        return lobbyId;
    }

    public boolean joinLobby(String lobbyId, Integer playerId) {
        boolean success = lobbyStateService.addPlayer(lobbyId, playerId);

        if (success) {
            // Notify all players in lobby via WebSocket
            LobbyEntity lobby = lobbyStateService.getLobby(lobbyId);
            notifyLobbyUpdate(lobbyId, lobby);
        }

        return success;
    }

    public boolean leaveLobby(String lobbyId, Integer playerId) {
        boolean success = lobbyStateService.removePlayer(lobbyId, playerId);

        if (success) {
            LobbyEntity lobby = lobbyStateService.getLobby(lobbyId);
            if (lobby != null) {
                notifyLobbyUpdate(lobbyId, lobby);
            }
        }

        return success;
    }

    public void startGame(String lobbyId) {
        LobbyEntity lobby = lobbyStateService.getLobby(lobbyId);

        if (lobby == null) {
            throw new IllegalArgumentException("Lobby not found: " + lobbyId);
        }

        LobbyReadyEvent event = new LobbyReadyEvent(
                lobbyId,
                lobby.getPlayerIds()
        );

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(LOBBY_EVENTS_CHANNEL, eventJson);
            log.info("Published LobbyReadyEvent for lobby: {}", lobbyId);
        } catch (Exception e) {
            log.error("Error publishing lobby ready event", e);
            throw new RuntimeException("Failed to start game", e);
        }

        rabbitTemplate.convertAndSend(
                LobbyRabbitMQConfig.GAME_EXCHANGE,
                LobbyRabbitMQConfig.START_GAME_ROUTING_KEY,
                event
        );

        System.out.println("Game start event published for lobby: " + lobby.getId());

        notifyGameReady(lobbyId,lobbyId,lobby);
        lobbyStateService.deleteLobby(lobbyId);
    }

    public LobbyEntity getLobby(String lobbyId) {
        return lobbyStateService.getLobby(lobbyId);
    }

    public List<LobbyEntity> getActiveLobbies() {
        return lobbyStateService.getActiveLobbies().stream().toList();
    }

    private void notifyLobbyUpdate(String lobbyId, LobbyEntity lobby) {
        messagingTemplate.convertAndSend(
                "/topic/lobby/" + lobbyId,
                new LobbyUpdateMessage("LOBBY_UPDATE", lobby)
        );
    }

    private void notifyGameReady(String lobbyId, String gameId, LobbyEntity lobby) {
        messagingTemplate.convertAndSend(
                "/topic/lobby/" + lobbyId,
                new GameReadyMessage("GAME_READY", gameId, lobby)
        );
    }

    record LobbyUpdateMessage(String type, LobbyEntity lobby) {}
    record GameReadyMessage(String type, String gameId, LobbyEntity lobby) {}
}
