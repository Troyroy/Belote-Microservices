package belote.ex.controller;

import Utils.MetricsMaker;
import belote.ex.business.GameServiceInt;
import belote.ex.business.GameStateServiceInt;
import belote.ex.events.LobbyReadyEvent;
import belote.ex.persistance.entity.GameEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;

@Slf4j
@RestController
@RequestMapping( value = "/games")


public class GameController {
    private final GameServiceInt gameServiceInt;
    private final GameStateServiceInt gameStateServiceInt;
    private final SimpMessagingTemplate messagingTemplate;
    private final MeterRegistry meterRegistry;

    public GameController(
            GameServiceInt gameServiceInt,
            GameStateServiceInt gameStateServiceInt,
            SimpMessagingTemplate messagingTemplate,
            MeterRegistry meterRegistry) {

        this.gameServiceInt = gameServiceInt;
        this.gameStateServiceInt = gameStateServiceInt;
        this.messagingTemplate = messagingTemplate;
        this.meterRegistry = meterRegistry;
    }
    @GetMapping("/{id}")
    public ResponseEntity<GameEntity> getGame(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("Get game request - ID: {}, User: {}", id, jwt.getSubject());
        return ResponseEntity.ok(gameServiceInt.getGame(id));
    }

    @PostMapping
    public ResponseEntity<GameEntity> createGame(
            @RequestBody @Valid LobbyReadyEvent lobby,
            @AuthenticationPrincipal Jwt jwt) throws JsonProcessingException {

        log.info("Create game request - User: {}, Lobby: {}", jwt.getSubject(), lobby);

        String id = gameServiceInt.createGameFromLobby(lobby);
        MetricsMaker.MetricsCounter("game_count", meterRegistry);
        GameEntity game = gameStateServiceInt.getGame(id);

        //gameServiceInt.startRound(game);

        var mapper = new ObjectMapper();
        String lobbyTo = MessageFormat.format("/lobby/{0}", id);
        String gameTO = MessageFormat.format("/game/{0}", id);
        messagingTemplate.convertAndSend(lobbyTo, " \"id\":connect ");
        messagingTemplate.convertAndSend(gameTO, mapper.writeValueAsString(gameServiceInt.getGame(id)));

        log.info("Game created successfully - ID: {}", id);
        return ResponseEntity.ok(gameServiceInt.getGame(id));
    }


    @PostMapping("/{id}/{cardID}")
    public ResponseEntity<GameEntity> playCard(
            @PathVariable String id,
            @PathVariable int cardID,
            @AuthenticationPrincipal Jwt jwt) throws JsonProcessingException {

        log.info("Play card request - Game: {}, Card: {}, User: {}", id, cardID, jwt.getSubject());

        GameEntity game = gameStateServiceInt.getGame(id);
        gameServiceInt.playCardAsync(1, cardID, game, 1500);


        return ResponseEntity.ok(gameServiceInt.getGame(id));
    }

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ResponseEntity<String> sendPlayCard(
            @Payload ResponseEntity<String> message,
            @DestinationVariable String roomId) {
        return message;
    }
}
