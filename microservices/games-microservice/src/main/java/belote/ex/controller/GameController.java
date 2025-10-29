package belote.ex.controller;

import belote.ex.business.GameServiceInt;
import belote.ex.persistance.entity.GameEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;

@RestController
@RequestMapping( value = "/game")
@AllArgsConstructor

public class GameController {
    GameServiceInt gameService;

    private final SimpMessagingTemplate messagingTemplate;


    /**
     * Client subscribes to: /topic/game/{gameId}
     * Client sends to: /app/game/{gameId}/join
     */
//    @MessageMapping("/game/{gameId}/join")
//    public void joinGame(
//            @DestinationVariable String gameId,
//            JoinGameRequest request) {
//
//        log.info("Player {} joining game {}", request.getPlayerId(), gameId);
//
//        try {
//            GameEntity game = gameStateService.getGame(gameId);
//
//            if (game == null) {
//                sendError(gameId, "Game not found");
//                return;
//            }
//
//            // Notify all players that someone joined
//            messagingTemplate.convertAndSend(
//                    "/topic/game/" + gameId,
//                    game
//            );
//
//            log.info("Player {} joined game {} successfully", request.getPlayerId(), gameId);
//
//        } catch (Exception e) {
//            log.error("Error joining game", e);
//            sendError(gameId, "Failed to join game");
//        }
//    }
    @GetMapping("{id}")
    public ResponseEntity<GameEntity>getGame(@PathVariable String id) {
        return ResponseEntity.ok(gameService.getGame(id));
    }


   @PostMapping("{id}/{cardID}")
    public ResponseEntity<GameEntity> playCard(@PathVariable String id, @PathVariable int cardID/*@PathVariable int playerID*/) throws JsonProcessingException {
        gameService.playCard(1, cardID,id,1500);
       var mapper = new ObjectMapper();
       String lobbyTo = MessageFormat.format("/game/{0}", id);
       messagingTemplate.convertAndSend(lobbyTo, mapper.writeValueAsString(gameService.getGame(id)));
        return ResponseEntity.ok(gameService.getGame(id));
    }


    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ResponseEntity<String> sendPlayCard(@Payload ResponseEntity<String> message, @DestinationVariable String roomId) {

        return message;
    }
}
