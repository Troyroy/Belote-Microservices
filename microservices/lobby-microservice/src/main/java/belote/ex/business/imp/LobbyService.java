package belote.ex.business.imp;


import belote.ex.business.LobbyServiceInt;
import belote.ex.events.LobbyReadyEvent;
import belote.ex.persistance.entity.LobbyEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class LobbyService implements LobbyServiceInt {

    private final LobbyStateService lobbyStateService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private static final String LOBBY_EVENTS_CHANNEL = "lobby:events";

    /**
     * Create a new lobby
     */
    public String createLobby(String lobbyName, String hostId, String gameMode) {
        LobbyEntity lobby = new LobbyEntity(null, hostId);
        lobby.addPlayer(Integer.parseInt(hostId)); // Host joins automatically

        String lobbyId = lobbyStateService.createLobby(lobby);

        log.info("Created lobby {} by host {}", lobbyId, hostId);
        return lobbyId;
    }

    /**
     * Join an existing lobby
     */
    public boolean joinLobby(String lobbyId, Integer playerId) {
        boolean success = lobbyStateService.addPlayer(lobbyId, playerId);

        if (success) {
            // Notify all players in lobby via WebSocket
            LobbyEntity lobby = lobbyStateService.getLobby(lobbyId);
            notifyLobbyUpdate(lobbyId, lobby);
        }

        return success;
    }

    /**
     * Leave a lobby
     */
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

    /**
     * Start the game from lobby
     */
    public void startGame(String lobbyId) {
        LobbyEntity lobby = lobbyStateService.getLobby(lobbyId);

        if (lobby == null) {
            throw new IllegalArgumentException("Lobby not found: " + lobbyId);
        }


        // Create and publish event
        LobbyReadyEvent event = new LobbyReadyEvent(
                lobbyId,
                lobby.getPlayerIds(),
                System.currentTimeMillis()
        );

        // Publish event to Redis for game service
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(LOBBY_EVENTS_CHANNEL, eventJson);
            log.info("Published LobbyReadyEvent for lobby: {}", lobbyId);
        } catch (Exception e) {
            log.error("Error publishing lobby ready event", e);
            throw new RuntimeException("Failed to start game", e);
        }
    }



    /**
     * Get lobby details
     */
    public LobbyEntity getLobby(String lobbyId) {
        return lobbyStateService.getLobby(lobbyId);
    }

    /**
     * Get all active lobbies
     */
    public List<LobbyEntity> getActiveLobbies() {
        return lobbyStateService.getActiveLobbies().stream().toList();
    }

    /**
     * Notify lobby members of updates via WebSocket
     */
    private void notifyLobbyUpdate(String lobbyId, LobbyEntity lobby) {
        messagingTemplate.convertAndSend(
                "/topic/lobby/" + lobbyId,
                new LobbyUpdateMessage("LOBBY_UPDATE", lobby)
        );
    }

    /**
     * Notify lobby members that game is ready
     */
    private void notifyGameReady(String lobbyId, String gameId, LobbyEntity lobby) {
        messagingTemplate.convertAndSend(
                "/topic/lobby/" + lobbyId,
                new GameReadyMessage("GAME_READY", gameId, lobby)
        );
    }

    record LobbyUpdateMessage(String type, LobbyEntity lobby) {}
    record GameReadyMessage(String type, String gameId, LobbyEntity lobby) {}
}
