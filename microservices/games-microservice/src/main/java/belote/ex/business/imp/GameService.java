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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
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

        // Fetch user details from User Service
        List<UserEntity> players = new ArrayList<>();
        for (Integer playerId : event.getPlayerIds()) {
            try {
                UserEntity user = restTemplate.getForObject(
                        USER_SERVICE_URL + "/" + playerId,
                        UserEntity.class
                );
                if (user != null) {
                    players.add(user);
                }
            } catch (Exception e) {
                log.error("Error fetching user {}", playerId, e);
            }
        }

        if (players.size() != event.getPlayerIds().size()) {
            throw new IllegalStateException("Could not fetch all players");
        }

        // Create game with lobby metadata
        GameEntity game = createGame(event.getPlayerIds());
        String gameId = gameStateService.createGame(game);

        log.info("Created game {} from lobby {}", gameId, event.getLobbyId());
        return gameId;
    }

    private GameEntity initializeGame(List<UserEntity> players, LobbyReadyEvent event) {
        // Your existing game initialization logic
        GameEntity game = new GameEntity();
        // ... initialization
        return game;
    }

    public void startRound(String gameID){
        AtomicInteger cardNumber = new AtomicInteger(0);

        GameEntity game = gameStateService.getGame(gameID);
        this.shuffleDeck(gameID);

        this.dealCards(gameID,cardNumber,3);
        this.dealCards(gameID,cardNumber,2);
        this.dealCards(gameID,cardNumber,3);

        List<CardEntity> list = new ArrayList<>();
        game.setDeck(list);
        this.askPlayerForACard(game.getOrder().getFirst(),gameID);

    }

    public void shuffleDeck(String gameId) {
        GameEntity game = gameStateService.getGame(gameId);

        if (game == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        List<CardEntity> deck = game.getDeck();

        if (deck == null || deck.isEmpty()) {
            log.warn("Cannot shuffle empty or null deck for game: {}", gameId);
            return;
        }

        // Use SecureRandom for better randomness (important for card games)
        Collections.shuffle(deck, new SecureRandom());

        gameStateService.saveGame(game);

        log.debug("Shuffled deck for game: {} using SecureRandom", gameId);
    }

    @Override
    public GameEntity getGame(String id){
        return gameStateService.getGame(id);
    }

    @Override
    public List<CardEntity> getDeck(String id) {
        GameEntity game = getGame(id);
        List<CardEntity> deckOfCards =  game.getDeck();
        return deckOfCards;
    }



    public void setFirstPlayer2(int times, String gameID)
    {
        GameEntity game = gameStateService.getGame(gameID);
        for(int x =0; x < times; x++) {
            game.getOrder().addLast(game.getOrder().getFirst());
            game.getOrder().removeFirst();
        }
    }

    public void nextPlayer(String gameID) {
        GameEntity game =gameStateService.getGame(gameID);
        game.getOrder().addLast(game.getOrder().getFirst());
        game.getOrder().removeFirst();
    }



    public void dealCards(String gameID, AtomicInteger cardInDeckId, int numberOfCards) {

        GameEntity game =gameStateService.getGame(gameID);
        List<CardEntity> deck = game.getDeck();

        for (int i=0; i < 4; i++) {
            List<CardEntity> hand = game.getHands().get(game.getOrder().getFirst());
            nextPlayer(gameID);
            for (int s=0; s < numberOfCards; s++) {
                hand.add(deck.get(cardInDeckId.get()));
                cardInDeckId.incrementAndGet();
            }
        }

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

    public void playCard(int playerID, int playedCard,String gameID,int seconds){
        GameEntity game =gameStateService.getGame(gameID);


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


            List<CardEntity> bucket = game.getBucket();
            bucket.add(card);

            String to = "/game/" + gameID;
            messagingTemplate.convertAndSend(to, game);

            try {
                Thread.sleep(seconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            nextPlayer(gameID);

            if (bucket.indexOf(card) == 3) {
                this.endBucket(gameID);
            } else {
                askPlayerForACard(game.getOrder().getFirst(), gameID);
            }
    }
    public void endRound(String gameID){
        GameEntity game =gameStateService.getGame(gameID);

        this.calculateScores(gameID);

        if (game.getScores().get(1) > 9 || game.getScores().get(0) >9){
            endGame(gameID);
        }
        else {




        List<CardEntity> team1Cards = game.getThisRoundCards().get(1).stream().toList();
        List<CardEntity> team2Cards = game.getThisRoundCards().get(0).stream().toList();


        List<CardEntity> newDecK= Stream.concat(team1Cards.stream(), team2Cards.stream())
                .toList();

        game.setDeck(newDecK);

            game.getThisRoundCards().get(1).clear();
            game.getThisRoundCards().get(0).clear();


        this.setFirstPlayer2(game.getFirstPlayerRound(),gameID);
        this.startRound(gameID);
        }
    }


    public void  endGame(String gameID)  {
        GameEntity game =gameStateService.getGame(gameID);


        int winner;
        Integer[] ids = new Integer[4];
        for(int i = 1; i <= 4; i++) {
            int newID = game.getPlayers().get(i);
            if (newID > 0) {
                ids[i-1] = (game.getPlayers().get(i));
            }
            else {
                ids[i-1] = null;
            }

        }


        if(game.getScores().get(0) < game.getScores().get(1)) {
            winner = 1;
        }
        else {
            winner = 2;
        }


        GameScoreEntity scores = GameScoreEntity.builder()
                .id(gameID)
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
        gameStateService.deleteGame(gameID);

        String to = "/game/" + gameID;
        messagingTemplate.convertAndSend(to, scores);
    }

    public void calculateScores(String gameID){
        GameEntity game =gameStateService.getGame(gameID);
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

    public void endBucket(String gameID){


        GameEntity game = gameStateService.getGame(gameID);
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

        this.setFirstPlayer2(winnerPlayerIndex,gameID);
            int bucket1 =  game.getCurrentBucket() + 1;
            game.setCurrentBucket(bucket1);

        askPlayerForACard(game.getOrder().getFirst(),gameID);
        }
        else{
            game.setCurrentBucket(1);
            endRound(gameID);
        }

    }




    public void askPlayerForACard(int playerIDinGame,String  gameID){
        GameEntity game =gameStateService.getGame(gameID);
       int playerID = game.getPlayers().get(playerIDinGame);
       if (playerID <= 0) {
           List<CardEntity> cards;
           cards = playableCards(game.getHands().get(game.getOrder().getFirst()),game.getCardToAnswer());
           this.playCard(playerID,cards.get(0).getId(),gameID,1500);
       }
    }

    public GameEntity createGame(List<Integer> lobbyPlayerIDs) {

        List<CardEntity> deckOfCards = new ArrayList<>();

        String[] suits = {"Diamonds", "Clubs", "Hearts", "Spades"};
        String[] ranks = {"King", "Queen", "Jack", "Ten", "Nine", "Eight", "Seven", "Ace"};

        deckOfCards.set(0, (CardEntity.builder().id(0).points(11).rank(ranks[7]).suit(suits[0]).build()));
        deckOfCards.set(1, (CardEntity.builder().id(1).points(4).rank(ranks[0]).suit(suits[0]).build()));
        deckOfCards.set(2, (CardEntity.builder().id(2).points(3).rank(ranks[1]).suit(suits[0]).build()));
        deckOfCards.set(3, (CardEntity.builder().id(3).points(20).rank(ranks[2]).suit(suits[0]).build()));
        deckOfCards.set(4, (CardEntity.builder().id(4).points(10).rank(ranks[3]).suit(suits[0]).build()));
        deckOfCards.set(5, (CardEntity.builder().id(5).points(14).rank(ranks[4]).suit(suits[0]).build()));
        deckOfCards.set(6, (CardEntity.builder().id(6).points(0).rank(ranks[5]).suit(suits[0]).build()));
        deckOfCards.set(7, (CardEntity.builder().id(7).points(-1).rank(ranks[6]).suit(suits[0]).build()));

        deckOfCards.set(8, (CardEntity.builder().id(8).points(11).rank(ranks[7]).suit(suits[1]).build()));
        deckOfCards.set(9, (CardEntity.builder().id(9).points(4).rank(ranks[0]).suit(suits[1]).build()));
        deckOfCards.set(10, (CardEntity.builder().id(10).points(3).rank(ranks[1]).suit(suits[1]).build()));
        deckOfCards.set(11, (CardEntity.builder().id(11).points(20).rank(ranks[2]).suit(suits[1]).build()));
        deckOfCards.set(12, (CardEntity.builder().id(12).points(10).rank(ranks[3]).suit(suits[1]).build()));
        deckOfCards.set(13, (CardEntity.builder().id(13).points(14).rank(ranks[4]).suit(suits[1]).build()));
        deckOfCards.set(14, (CardEntity.builder().id(14).points(0).rank(ranks[5]).suit(suits[1]).build()));
        deckOfCards.set(15, (CardEntity.builder().id(15).points(-1).rank(ranks[6]).suit(suits[1]).build()));


        deckOfCards.set(16, (CardEntity.builder().id(16).points(11).rank(ranks[7]).suit(suits[2]).build()));
        deckOfCards.set(17, (CardEntity.builder().id(17).points(4).rank(ranks[0]).suit(suits[2]).build()));
        deckOfCards.set(18, (CardEntity.builder().id(18).points(3).rank(ranks[1]).suit(suits[2]).build()));
        deckOfCards.set(19, (CardEntity.builder().id(19).points(20).rank(ranks[2]).suit(suits[2]).build()));
        deckOfCards.set(20, (CardEntity.builder().id(20).points(10).rank(ranks[3]).suit(suits[2]).build()));
        deckOfCards.set(21, (CardEntity.builder().id(21).points(14).rank(ranks[4]).suit(suits[2]).build()));
        deckOfCards.set(22, (CardEntity.builder().id(22).points(0).rank(ranks[5]).suit(suits[2]).build()));
        deckOfCards.set(23, (CardEntity.builder().id(23).points(-1).rank(ranks[6]).suit(suits[2]).build()));


        deckOfCards.set(24, (CardEntity.builder().id(24).points(11).rank(ranks[7]).suit(suits[3]).build()));
        deckOfCards.set(25, (CardEntity.builder().id(25).points(4).rank(ranks[0]).suit(suits[3]).build()));
        deckOfCards.set(26, (CardEntity.builder().id(26).points(3).rank(ranks[1]).suit(suits[3]).build()));
        deckOfCards.set(27, (CardEntity.builder().id(27).points(20).rank(ranks[2]).suit(suits[3]).build()));
        deckOfCards.set(28, (CardEntity.builder().id(28).points(10).rank(ranks[3]).suit(suits[3]).build()));
        deckOfCards.set(29, (CardEntity.builder().id(29).points(14).rank(ranks[4]).suit(suits[3]).build()));
        deckOfCards.set(30, (CardEntity.builder().id(30).points(0).rank(ranks[5]).suit(suits[3]).build()));
        deckOfCards.set(31, (CardEntity.builder().id(31).points(-1).rank(ranks[6]).suit(suits[3]).build()));




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
        return  (GameEntity.builder().id(gameId).players(playerIDs).thisRoundCards(roundCards).bucket(new ArrayList<>()).currentBucket(1).order(order).hands(hands).deck(deckOfCards).firstPlayerBucket(1).firstPlayerRound(1).scores(scores).build());
    }
}
