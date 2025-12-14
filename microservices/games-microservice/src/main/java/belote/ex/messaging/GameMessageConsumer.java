package belote.ex.messaging;

import belote.ex.business.GameServiceInt;
import belote.ex.business.imp.GameService;


import belote.ex.config.GameRabbitMQConfig;
import belote.ex.events.LobbyReadyEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GameMessageConsumer {
    private final GameServiceInt gameService;

    public GameMessageConsumer(GameServiceInt gameService) {
        this.gameService = gameService;
    }
    @RabbitListener(queues = GameRabbitMQConfig.START_GAME_QUEUE)
    public void handleStartGame(LobbyReadyEvent event) {
        System.out.println("Received start game event for lobby: " + event.getLobbyId());
        gameService.createGameFromLobby(event);
    }
}