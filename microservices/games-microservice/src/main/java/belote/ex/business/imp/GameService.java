package belote.ex.business.imp;

import belote.ex.business.GameServiceInt;
import belote.ex.business.exceptions.NotAvaibleException;
import belote.ex.events.LobbyReadyEvent;
import belote.ex.persistance.GameRepositoryInt;
import belote.ex.persistance.GameScoreRepository;
import belote.ex.persistance.entity.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@AllArgsConstructor
public class GameService implements GameServiceInt {
    private final GameStateService gameStateService;
    private GameScoreRepository gameScoresRepository;
    private final RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private static final String USER_SERVICE_URL = "http://users-microservice:8080/api/users";

    public String createGameFromLobby(LobbyReadyEvent event) {
        log.info("Creating game from lobby: {}", event.getLobbyId());

//        // Fetch user details from User Service
//        List<UserEntity> players = new ArrayList<>();
//        for (Integer playerId : event.getPlayerIds()) {
//            try {
//                UserEntity user = restTemplate.getForObject(
//                        USER_SERVICE_URL + "/" + playerId,
//                        UserEntity.class
//                );
//                if (user != null) {
//                    players.add(user);
//                }
//            } catch (Exception e) {
//                log.error("Error fetching user {}", playerId, e);
//            }
//        }

//        if (players.size() != event.getPlayerIds().size()) {
//            throw new IllegalStateException("Could not fetch all players");
//        }

        // Create game with lobby metadata
        GameEntity game = createGame(event.getPlayerIds());
        String gameId = gameStateService.createGame(game, event.getLobbyId());


        startRound(gameStateService.getGame(gameId));
        var mapper = new ObjectMapper();
        String lobbyTo = MessageFormat.format("/lobby/{0}", event.getLobbyId());
        String gameTO = MessageFormat.format("/game/{0}", event.getLobbyId());
        messagingTemplate.convertAndSend(lobbyTo, " \"id\":connect ");
        try {
            messagingTemplate.convertAndSend(gameTO, mapper.writeValueAsString(getGame(gameId)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //messagingTemplate.convertAndSend(gameTO, mapper.writeValueAsString(gameService.getGame(id)));
        log.info("Created game {} from lobby {}", gameId, event.getLobbyId());
        return gameId;
    }

    private GameEntity initializeGame(List<UserEntity> players, LobbyReadyEvent event) {
        // Your existing game initialization logic
        GameEntity game = new GameEntity();
        // ... initialization
        return game;
    }

    public void startRound(GameEntity game){
        AtomicInteger cardNumber = new AtomicInteger(0);


        this.shuffleDeck(game);
        log.info("Shuffled cards for game: {}", game.getId());
        this.dealCards(game,cardNumber,3);
        this.dealCards(game,cardNumber,2);
        this.dealCards(game,cardNumber,3);

        List<CardEntity> list = new ArrayList<>();
        game.setDeck(list);
        log.info("Finished setting game: {}", game.getId());
        gameStateService.saveGame(game);
        this.askPlayerForACard(game.getOrder().getFirst(),game);

    }

    public void shuffleDeck(GameEntity game) {

        if (game == null) {
            throw new IllegalArgumentException("Game not found: " + game);
        }

        List<CardEntity> deck = game.getDeck();

        if (deck == null || deck.isEmpty()) {
            log.warn("Cannot shuffle empty or null deck for game: {}", game);
            return;
        }

        // Use SecureRandom for better randomness (important for card games)
        Collections.shuffle(deck, new SecureRandom());

        gameStateService.saveGame(game);

        log.debug("Shuffled deck for game: {} using SecureRandom", game);
    }

    @Override
    public GameEntity getGame(String id){
        return gameStateService.getGame(id);
    }

    @Override
    public List<CardEntity> getDeck(GameEntity game) {

        List<CardEntity> deckOfCards =  game.getDeck();
        return deckOfCards;
    }



    public void setFirstPlayer2(int times, GameEntity game)
    {
        for(int x =0; x < times; x++) {
            game.getOrder().addLast(game.getOrder().getFirst());
            game.getOrder().removeFirst();
        }
    }

    public void nextPlayer(GameEntity game) {

        game.getOrder().addLast(game.getOrder().getFirst());
        game.getOrder().removeFirst();
    }



    public void dealCards(GameEntity game, AtomicInteger cardInDeckId, int numberOfCards) {


        List<CardEntity> deck = game.getDeck();

        for (int i=0; i < 4; i++) {
            List<CardEntity> hand = game.getHands().get(game.getOrder().getFirst());
            nextPlayer(game);
            for (int s=0; s < numberOfCards; s++) {
                hand.add(deck.get(cardInDeckId.get()));
                cardInDeckId.incrementAndGet();
            }
        }
        gameStateService.saveGame(game);

    }


    public List<CardEntity> playableCards(List<CardEntity> hand,CardEntity cardToAnswer){
        ArrayList<CardEntity> playableCards;
        if (cardToAnswer != null) {
            playableCards = hand.stream()
                    .filter(x -> x.getSuit().equals(cardToAnswer.getSuit()) && x.getPoints() > cardToAnswer.getPoints()).collect(Collectors.toCollection(ArrayList::new));

            if (playableCards.isEmpty()) {

                playableCards.addAll(hand.stream()
                        .filter(card -> card.getSuit().equals(cardToAnswer.getSuit()))
                        .toList());
            }

            return playableCards.isEmpty() ? hand : playableCards;
        } else {
            return hand;
        }

    }

    public void playCard(int playerID, int playedCard,GameEntity game,int seconds){


            List<CardEntity> hand = game.getHands().get(game.getOrder().getFirst());
            CardEntity card = hand.stream().filter(x -> playedCard == x.getId()).findFirst().orElse(null);


            List<CardEntity> cards = playableCards(game.getHands().get(game.getOrder().getFirst()),game.getCardToAnswer());
            if(!cards.contains(card))
            {
                throw new NotAvaibleException("Not avaible");
            }
            CardEntity card1ToAnswer = game.getCardToAnswer();
            game.getHands().get(game.getOrder().getFirst()).remove(card);
            if (game.getCardToAnswer() == null) {
                game.setCardToAnswer(card);
            } else {
                assert card != null;
                if (card1ToAnswer.getSuit().equals(card.getSuit()) && card1ToAnswer.getPoints() < card.getPoints()) {
                    game.setCardToAnswer(card);
                }
            }
            gameStateService.saveGame(game);

            List<CardEntity> bucket = game.getBucket();
            bucket.add(card);

            //String to = "/game/" + game.getId();
            notifyGameUpdate(game.getId(),game);
            //messagingTemplate.convertAndSend(to, game);

            try {
                Thread.sleep(seconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            nextPlayer(game);

            if (bucket.indexOf(card) == 3) {
                this.endBucket(game);

            } else {
                askPlayerForACard(game.getOrder().getFirst(), game);
            }
            gameStateService.saveGame(game);
    }

    @Async
    public void playCardAsync(int playerID, int playedCard,GameEntity game,int seconds){


        List<CardEntity> hand = game.getHands().get(game.getOrder().getFirst());
        CardEntity card = hand.stream().filter(x -> playedCard == x.getId()).findFirst().orElse(null);


        List<CardEntity> cards = playableCards(game.getHands().get(game.getOrder().getFirst()),game.getCardToAnswer());
        if(!cards.contains(card))
        {
            throw new NotAvaibleException("Not avaible");
        }
        CardEntity card1ToAnswer = game.getCardToAnswer();
        game.getHands().get(game.getOrder().getFirst()).remove(card);
        if (game.getCardToAnswer() == null) {
            game.setCardToAnswer(card);
        } else {
            assert card != null;
            if (card1ToAnswer.getSuit().equals(card.getSuit()) && card1ToAnswer.getPoints() < card.getPoints()) {
                game.setCardToAnswer(card);
            }
        }
        gameStateService.saveGame(game);

        List<CardEntity> bucket = game.getBucket();
        bucket.add(card);

        //String to = "/game/" + game.getId();
        notifyGameUpdate(game.getId(),game);
        //messagingTemplate.convertAndSend(to, game);

        try {
            Thread.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        nextPlayer(game);

        if (bucket.indexOf(card) == 3) {
            this.endBucket(game);

        } else {
            askPlayerForACard(game.getOrder().getFirst(), game);
        }
        gameStateService.saveGame(game);
    }
    public void endRound(GameEntity game){

        log.info("Ending round for game: {}", game.getId());
        this.calculateScores(game);

        if (game.getScores().get(1) > 9 || game.getScores().get(0) >9){
            endGame(game);
        }
        else {




        List<CardEntity> team1Cards = game.getThisRoundCards().get(1).stream().toList();
        List<CardEntity> team2Cards = game.getThisRoundCards().get(0).stream().toList();


        List<CardEntity> newDecK= Stream.concat(team1Cards.stream(), team2Cards.stream())
                .toList();

        game.setDeck(newDecK);

            game.getThisRoundCards().get(1).clear();
            game.getThisRoundCards().get(0).clear();


        this.setFirstPlayer2(game.getFirstPlayerRound(),game);
        this.startRound(game);
        gameStateService.saveGame(game);
        }
    }


    public void  endGame(GameEntity game)  {

        log.info("Ending game: {}", game.getId());
        int winner;
        Integer[] ids = new Integer[4];

        for(int i = 1; i <= 4; i++) {
            Integer newID = game.getPlayers().get(i);

            if (newID != null && newID > 0) {
                ids[i-1] = newID;
            } else {
                ids[i-1] = null;
            }
        }

        if(game.getScores().get(0) < game.getScores().get(1)) {
            winner = 1;
        } else {
            winner = 2;
        }

        GameScoreEntity scores = GameScoreEntity.builder()
                .id(game.getId())
                .player1(ids[0])
                .player2(ids[1])
                .player3(ids[2])
                .player4(ids[3])
                .team1Score(game.getScores().get(1))
                .team2Score(game.getScores().get(0))
                .winnerTeam(winner)
                .build();
        // Add permanent storage
        // scoresRepository.save(scores);

        gameScoresRepository.save(scores);
        gameStateService.deleteGame(game.getId());


        notifyGameEnd(game.getId(),game);
    }

    public void calculateScores(GameEntity game){
        List<CardEntity> team1 = game.getThisRoundCards().get(0);

        int team1Scores = 0;

        for(CardEntity card : team1) {
            team1Scores += card.getPoints();
        }
        team1Scores = Math.round((float) team1Scores / 10);

        List<CardEntity> team2 = game.getThisRoundCards().get(1);

        int team2Scores = 0;

        for(CardEntity card : team2) {
            team2Scores += card.getPoints();
        }

        team2Scores = Math.round((float) team2Scores / 10);

        int ds = game.getScores().get(0) + team1Scores;

        int ds2 = game.getScores().get(1) + team2Scores;


        game.getScores().clear();
        game.getScores().put(0,ds);
        game.getScores().put(1,ds2);


    }

    public void endBucket(GameEntity game){
        log.info("Ending bucket for game: {}", game.getId());

        List<CardEntity> bucket = game.getBucket();

        CardEntity card = bucket.stream().filter(x -> x.getSuit().equals(game.getCardToAnswer().getSuit()))
                .max(Comparator.comparing(CardEntity::getPoints)).orElseThrow(NoSuchElementException::new);



        int winnerPlayerIndex = bucket.indexOf(card);

        List<Integer> players = game.getOrder().stream().toList();
        int winnerID = players.get(winnerPlayerIndex);

        game.setFirstPlayerBucket(winnerID);

        for(CardEntity card1 : bucket) {
            game.getThisRoundCards().get((winnerID % 2)).add(card1);
        }


        game.setBucket(new ArrayList<>());
        game.setCardToAnswer(null);


        if (game.getCurrentBucket() != 8){

        this.setFirstPlayer2(winnerPlayerIndex,game);
            int bucket1 =  game.getCurrentBucket() + 1;
            game.setCurrentBucket(bucket1);
            gameStateService.saveGame(game);
        askPlayerForACard(game.getOrder().getFirst(),game);
        }
        else{
            game.setCurrentBucket(1);

            endRound(game);
            gameStateService.saveGame(game);
        }

    }




    public void askPlayerForACard(int playerIDinGame,GameEntity  game){

       int playerID = game.getPlayers().get(playerIDinGame);
       if (playerID <= 0) {
           List<CardEntity> cards;
           cards = playableCards(game.getHands().get(game.getOrder().getFirst()),game.getCardToAnswer());
           this.playCard(playerID,cards.get(0).getId(),game,1500);
       }
    }

    public GameEntity createGame(List<Integer> lobbyPlayerIDs) {

        List<CardEntity> deckOfCards = new ArrayList<>();
        String[] suits = {"Diamonds", "Clubs", "Hearts", "Spades"};
        String[] ranks = {"King", "Queen", "Jack", "Ten", "Nine", "Eight", "Seven", "Ace"};
        int[] points = {4, 3, 20, 10, 14, 0, -1, 11}; // Corresponding to ranks

        int cardId = 0;
        for (String suit : suits) {
            for (int i = 0; i < ranks.length; i++) {
                deckOfCards.add(CardEntity.builder()
                        .id(cardId++)
                        .points(points[i])
                        .rank(ranks[i])
                        .suit(suit)
                        .build());
            }
        }
        // ... rest of your code remains the same
        HashMap<Integer, Integer> playerIDs = new HashMap<>();


        int botID = -1;
        for(int i=1; i<5; i++) {
            if(i <= lobbyPlayerIDs.size()){
                playerIDs.put(i,lobbyPlayerIDs.get(i-1));
            }
            else{
                playerIDs.put(i,botID);
                botID--;
            }
        }

        HashMap<Integer,List<CardEntity>> hands = new HashMap<>();

        hands.put(1,new ArrayList<>());
        hands.put(2,new ArrayList<>());
        hands.put(3,new ArrayList<>());
        hands.put(4,new ArrayList<>());

        Deque<Integer> order = new LinkedList<>();

        order.add(1);
        order.add(2);
        order.add(3);
        order.add(4);

        HashMap<Integer, List<CardEntity>> roundCards = new HashMap<>();
        roundCards.put(0,new ArrayList<>());
        roundCards.put(1,new ArrayList<>());

        HashMap<Integer,Integer> scores = new HashMap<>();
        scores.put(0,0);
        scores.put(1,0);
        String gameId = UUID.randomUUID().toString();
        return  (GameEntity.builder().id(gameId).players(playerIDs).thisRoundCards(roundCards).bucket(new ArrayList<>()).currentBucket(1).order(order).hands(hands).deck(deckOfCards).firstPlayerBucket(1).firstPlayerRound(1).scores(scores).status("ACTIVE").build());
    }

    private void notifyGameUpdate(String gameId, GameEntity game) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                new GameUpdateMessage("GAME _UPDATE", game)
        );
    }

    /**
     * Notify lobby members that game is ready
     */
    private void notifyGameReady(String gameId, GameEntity game) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                new GameFinishedMessage("GAME_READY", gameId, game)
        );
    }

    private void notifyGameEnd(String gameId, GameEntity game) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                new GameFinishedMessage("GAME_END", gameId, game)
        );
    }

    record GameUpdateMessage(String type, GameEntity game) {}
    record GameFinishedMessage(String type, String gameId, GameEntity game) {}
}
