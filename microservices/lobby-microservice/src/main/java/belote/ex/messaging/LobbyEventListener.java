package belote.ex.messaging;


import belote.ex.business.imp.LobbyService;
import belote.ex.events.GameCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LobbyEventListener {

    private final LobbyService lobbyService;
    private final ObjectMapper objectMapper;

    /**
     * Listen for GameCreatedEvent from game service
     */
    public void onGameCreated(String message) {
        try {
            GameCreatedEvent event = objectMapper.readValue(message, GameCreatedEvent.class);
            log.info("Received GameCreatedEvent for lobby: {}", event.getLobbyId());

        } catch (Exception e) {
            log.error("Error processing game created event", e);
        }
    }
}