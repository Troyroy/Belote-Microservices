package belote.ex.messaging;


import belote.ex.business.GameServiceInt;
import belote.ex.business.GameStateServiceInt;
import belote.ex.business.imp.GameService;
import belote.ex.business.imp.GameStateService;
import belote.ex.events.GameCreatedEvent;
import belote.ex.events.LobbyReadyEvent;
import belote.ex.persistance.entity.GameEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GameEventListener {

    private final GameServiceInt gameService;
    private final GameStateServiceInt gameStateService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public GameEventListener(
            GameServiceInt gameService,
            GameStateServiceInt gameStateService,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            SimpMessagingTemplate messagingTemplate) {

        this.gameService = gameService;
        this.gameStateService = gameStateService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    private static final String GAME_EVENTS_CHANNEL = "game:events";
    private static final String LOBBY_WS_PREFIX = "/topic/lobby/";

    public void onMessage(String message) {
        try {
            LobbyReadyEvent event = objectMapper.readValue(message, LobbyReadyEvent.class);
            log.info("Received LobbyReadyEvent for lobby: {}", event.getLobbyId());

            // Create game from lobby event
            String gameId = gameService.createGameFromLobby(event);

            // Get the created game
            GameEntity game = gameStateService.getGame(gameId);

            // Publish GameCreatedEvent to Redis (for lobby service)
            publishGameCreatedEvent(gameId, event.getLobbyId());

            // Notify lobby players via WebSocket that game is ready
            notifyLobbyPlayersGameReady(event.getLobbyId(), gameId, game);

        } catch (Exception e) {
            log.error("Error processing lobby ready event", e);
        }
    }

    private void publishGameCreatedEvent(String gameId, String lobbyId) {
        try {
            GameCreatedEvent event = new GameCreatedEvent(
                    gameId,
                    lobbyId,
                    System.currentTimeMillis()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(GAME_EVENTS_CHANNEL, eventJson);
            log.info("Published GameCreatedEvent: {}", gameId);
        } catch (Exception e) {
            log.error("Error publishing game created event", e);
        }
    }

    private void notifyLobbyPlayersGameReady(String lobbyId, String gameId, GameEntity game) {
        try {
            GameReadyNotification notification = new GameReadyNotification(
                    gameId,
                    lobbyId,
                    "Game is ready! Redirecting...",
                    game
            );

            // Send to lobby WebSocket topic
            messagingTemplate.convertAndSend(
                    LOBBY_WS_PREFIX + lobbyId,
                    notification
            );

            log.info("Notified lobby {} that game {} is ready", lobbyId, gameId);
        } catch (Exception e) {
            log.error("Error notifying lobby players", e);
        }
    }

    record GameReadyNotification(
            String gameId,
            String lobbyId,
            String message,
            GameEntity game
    ) {}
}
