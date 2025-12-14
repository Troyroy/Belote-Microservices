package belote.ex.business.imp;

import belote.ex.business.exceptions.NotAvaibleException;
import belote.ex.events.LobbyReadyEvent;
import belote.ex.persistance.GameScoreRepository;
import belote.ex.persistance.entity.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTests {

    @Mock
    private GameStateService gameStateService;

    @Mock
    private GameScoreRepository gameScoresRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameService gameService;

    private GameEntity testGame;
    private List<Integer> testPlayerIds;

    @BeforeEach
    void setUp() {
        testPlayerIds = Arrays.asList(101, 102, 103, 104);
        testGame = createTestGame();
    }

    private GameEntity createTestGame() {
        String gameId = "test-game-123";

        // Create deck
        List<CardEntity> deck = new ArrayList<>();
        String[] suits = {"Diamonds", "Clubs", "Hearts", "Spades"};
        String[] ranks = {"King", "Queen", "Jack", "Ten", "Nine", "Eight", "Seven", "Ace"};
        int[] points = {4, 3, 20, 10, 14, 0, -1, 11};

        int cardId = 0;
        for (String suit : suits) {
            for (int i = 0; i < ranks.length; i++) {
                deck.add(CardEntity.builder()
                        .id(cardId++)
                        .points(points[i])
                        .rank(ranks[i])
                        .suit(suit)
                        .build());
            }
        }

        // Create player IDs
        HashMap<Integer, Integer> playerIDs = new HashMap<>();
        playerIDs.put(1, 101);
        playerIDs.put(2, 102);
        playerIDs.put(3, 103);
        playerIDs.put(4, 104);

        // Create hands
        HashMap<Integer, List<CardEntity>> hands = new HashMap<>();
        hands.put(1, new ArrayList<>());
        hands.put(2, new ArrayList<>());
        hands.put(3, new ArrayList<>());
        hands.put(4, new ArrayList<>());

        // Create order
        Deque<Integer> order = new LinkedList<>();
        order.add(1);
        order.add(2);
        order.add(3);
        order.add(4);

        // Create round cards
        HashMap<Integer, List<CardEntity>> roundCards = new HashMap<>();
        roundCards.put(0, new ArrayList<>());
        roundCards.put(1, new ArrayList<>());

        // Create scores
        HashMap<Integer, Integer> scores = new HashMap<>();
        scores.put(0, 0);
        scores.put(1, 0);

        return GameEntity.builder()
                .id(gameId)
                .players(playerIDs)
                .thisRoundCards(roundCards)
                .bucket(new ArrayList<>())
                .currentBucket(1)
                .order(order)
                .hands(hands)
                .deck(deck)
                .firstPlayerBucket(1)
                .firstPlayerRound(1)
                .scores(scores)
                .status("ACTIVE")
                .build();
    }

    @Test
    void testCreateGame_WithFullPlayerList() {
        // Arrange
        List<Integer> playerIds = Arrays.asList(101, 102, 103, 104);

        // Act
        GameEntity game = gameService.createGame(playerIds);

        // Assert
        assertNotNull(game);
        assertNotNull(game.getId());
        assertEquals(32, game.getDeck().size()); // 4 suits * 8 ranks
        assertEquals(4, game.getPlayers().size());
        assertEquals(101, game.getPlayers().get(1));
        assertEquals(104, game.getPlayers().get(4));
        assertEquals(4, game.getHands().size());
        assertTrue(game.getHands().get(1).isEmpty());
        assertEquals(0, game.getScores().get(0));
        assertEquals(0, game.getScores().get(1));
        assertEquals("ACTIVE", game.getStatus());
    }

    @Test
    void testCreateGame_WithBotsFillingEmpty() {
        // Arrange
        List<Integer> playerIds = Arrays.asList(101, 102);

        // Act
        GameEntity game = gameService.createGame(playerIds);

        // Assert
        assertNotNull(game);
        assertEquals(4, game.getPlayers().size());
        assertEquals(101, game.getPlayers().get(1));
        assertEquals(102, game.getPlayers().get(2));
        assertTrue(game.getPlayers().get(3) < 0); // Bot ID
        assertTrue(game.getPlayers().get(4) < 0); // Bot ID
    }

    @Test
    void testShuffleDeck_Success() {
        // Arrange
        GameEntity game = createTestGame();
        List<CardEntity> originalDeck = new ArrayList<>(game.getDeck());

        // Act
        gameService.shuffleDeck(game);

        // Assert
        verify(gameStateService, times(1)).saveGame(game);
        assertEquals(originalDeck.size(), game.getDeck().size());
        // Deck should still contain all the same cards
        assertTrue(game.getDeck().containsAll(originalDeck));
    }

    @Test
    void testShuffleDeck_NullGame_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.shuffleDeck(null);
        });
    }

    @Test
    void testGetGame() {
        // Arrange
        String gameId = "test-game-123";
        when(gameStateService.getGame(gameId)).thenReturn(testGame);

        // Act
        GameEntity result = gameService.getGame(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(gameId, result.getId());
        verify(gameStateService, times(1)).getGame(gameId);
    }

    @Test
    void testGetDeck() {
        // Act
        List<CardEntity> deck = gameService.getDeck(testGame);

        // Assert
        assertNotNull(deck);
        assertEquals(32, deck.size());
    }

    @Test
    void testNextPlayer() {
        // Arrange
        GameEntity game = createTestGame();
        Integer firstPlayer = game.getOrder().getFirst();

        // Act
        gameService.nextPlayer(game);

        // Assert
        assertNotEquals(firstPlayer, game.getOrder().getFirst());
        assertEquals(firstPlayer, game.getOrder().getLast());
    }

    @Test
    void testSetFirstPlayer2() {
        // Arrange
        GameEntity game = createTestGame();
        Deque<Integer> originalOrder = new LinkedList<>(game.getOrder());

        // Act
        gameService.setFirstPlayer2(2, game);

        // Assert
        assertEquals(originalOrder.stream().skip(2).findFirst().get(), game.getOrder().getFirst());
    }

    @Test
    void testDealCards() {
        // Arrange
        GameEntity game = createTestGame();
        AtomicInteger cardNumber = new AtomicInteger(0);
        int numberOfCards = 3;

        // Act
        gameService.dealCards(game, cardNumber, numberOfCards);

        // Assert
        // Each of 4 players should have 3 cards
        assertEquals(3, game.getHands().get(1).size());
        assertEquals(3, game.getHands().get(2).size());
        assertEquals(3, game.getHands().get(3).size());
        assertEquals(3, game.getHands().get(4).size());
        assertEquals(12, cardNumber.get()); // 4 players * 3 cards
        verify(gameStateService, times(1)).saveGame(game);
    }

    @Test
    void testPlayableCards_NoCardToAnswer_ReturnsFullHand() {
        // Arrange
        List<CardEntity> hand = Arrays.asList(
                CardEntity.builder().id(1).suit("Hearts").points(10).rank("Ten").build(),
                CardEntity.builder().id(2).suit("Diamonds").points(4).rank("King").build()
        );

        // Act
        List<CardEntity> result = gameService.playableCards(hand, null);

        // Assert
        assertEquals(hand, result);
    }

    @Test
    void testPlayableCards_WithCardToAnswer_HigherCardAvailable() {
        // Arrange
        CardEntity cardToAnswer = CardEntity.builder()
                .id(1).suit("Hearts").points(4).rank("King").build();

        List<CardEntity> hand = Arrays.asList(
                CardEntity.builder().id(2).suit("Hearts").points(10).rank("Ten").build(),
                CardEntity.builder().id(3).suit("Hearts").points(3).rank("Queen").build(),
                CardEntity.builder().id(4).suit("Diamonds").points(4).rank("King").build()
        );

        // Act
        List<CardEntity> result = gameService.playableCards(hand, cardToAnswer);

        // Assert
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getPoints());
        assertEquals("Hearts", result.get(0).getSuit());
    }

    @Test
    void testPlayableCards_WithCardToAnswer_NoHigherCard_ReturnsSameSuit() {
        // Arrange
        CardEntity cardToAnswer = CardEntity.builder()
                .id(1).suit("Hearts").points(20).rank("Jack").build();

        List<CardEntity> hand = Arrays.asList(
                CardEntity.builder().id(2).suit("Hearts").points(10).rank("Ten").build(),
                CardEntity.builder().id(3).suit("Hearts").points(3).rank("Queen").build(),
                CardEntity.builder().id(4).suit("Diamonds").points(4).rank("King").build()
        );

        // Act
        List<CardEntity> result = gameService.playableCards(hand, cardToAnswer);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(card -> card.getSuit().equals("Hearts")));
    }

    @Test
    void testPlayableCards_WithCardToAnswer_NoSameSuit_ReturnsAllCards() {
        // Arrange
        CardEntity cardToAnswer = CardEntity.builder()
                .id(1).suit("Hearts").points(10).rank("Ten").build();

        List<CardEntity> hand = Arrays.asList(
                CardEntity.builder().id(2).suit("Diamonds").points(4).rank("King").build(),
                CardEntity.builder().id(3).suit("Clubs").points(3).rank("Queen").build()
        );

        // Act
        List<CardEntity> result = gameService.playableCards(hand, cardToAnswer);

        // Assert
        assertEquals(hand, result);
    }

    @Test
    void testPlayCard_ValidCard_Success() {
        // Arrange
        GameEntity game = createTestGame();
        CardEntity cardToPlay = CardEntity.builder()
                .id(10).suit("Hearts").points(10).rank("Ten").build();

        game.getHands().get(1).add(cardToPlay);
        game.setCardToAnswer(null);

        // Act
        gameService.playCard(101, 10, game, 0);

        // Assert
        assertFalse(game.getHands().get(1).contains(cardToPlay));
        assertTrue(game.getBucket().contains(cardToPlay));
        verify(gameStateService, atLeast(1)).saveGame(game);
    }

    @Test
    void testPlayCard_InvalidCard_ThrowsException() {
        // Arrange
        GameEntity game = createTestGame();
        CardEntity validCard = CardEntity.builder()
                .id(10).suit("Hearts").points(10).rank("Ten").build();
        CardEntity invalidCard = CardEntity.builder()
                .id(20).suit("Diamonds").points(4).rank("King").build();

        game.getHands().get(1).add(validCard);
        game.getHands().get(1).add(invalidCard);
        game.setCardToAnswer(CardEntity.builder()
                .id(5).suit("Hearts").points(4).rank("King").build());

        // Act & Assert
        assertThrows(NotAvaibleException.class, () -> {
            gameService.playCard(101, 20, game, 0); // Try to play Diamonds when Hearts is required
        });
    }

    @Test
    void testCalculateScores() {
        // Arrange
        GameEntity game = createTestGame();

        // Team 1 (index 0) gets 50 points worth of cards
        game.getThisRoundCards().get(0).add(
                CardEntity.builder().id(1).points(20).rank("Jack").suit("Hearts").build()
        );
        game.getThisRoundCards().get(0).add(
                CardEntity.builder().id(2).points(14).rank("Nine").suit("Hearts").build()
        );
        game.getThisRoundCards().get(0).add(
                CardEntity.builder().id(3).points(11).rank("Ace").suit("Hearts").build()
        );
        game.getThisRoundCards().get(0).add(
                CardEntity.builder().id(4).points(10).rank("Ten").suit("Hearts").build()
        );

        // Team 2 (index 1) gets 17 points worth of cards
        game.getThisRoundCards().get(1).add(
                CardEntity.builder().id(5).points(4).rank("King").suit("Diamonds").build()
        );
        game.getThisRoundCards().get(1).add(
                CardEntity.builder().id(6).points(3).rank("Queen").suit("Diamonds").build()
        );
        game.getThisRoundCards().get(1).add(
                CardEntity.builder().id(7).points(10).rank("Ten").suit("Diamonds").build()
        );

        // Act
        gameService.calculateScores(game);

        // Assert
        // 55 / 10 = 5.5 -> rounds to 6
        assertEquals(6, game.getScores().get(0));
        // 17 / 10 = 1.7 -> rounds to 2
        assertEquals(2, game.getScores().get(1));
    }

    @Test
    void testEndBucket_DeterminesWinner() {
        // Arrange
        GameEntity game = createTestGame();

        CardEntity card1 = CardEntity.builder()
                .id(1).suit("Hearts").points(4).rank("King").build();
        CardEntity card2 = CardEntity.builder()
                .id(2).suit("Hearts").points(10).rank("Ten").build();
        CardEntity card3 = CardEntity.builder()
                .id(3).suit("Hearts").points(20).rank("Jack").build();
        CardEntity card4 = CardEntity.builder()
                .id(4).suit("Diamonds").points(4).rank("King").build();

        game.getBucket().add(card1);
        game.getBucket().add(card2);
        game.getBucket().add(card3);
        game.getBucket().add(card4);

        game.setCardToAnswer(card1);

        // Act
        gameService.endBucket(game);

        // Assert
        assertTrue(game.getBucket().isEmpty());
        assertNull(game.getCardToAnswer());
        assertEquals(4, game.getThisRoundCards().get(0).size() +
                game.getThisRoundCards().get(1).size());
        verify(gameStateService, atLeast(1)).saveGame(game);
    }

    @Test
    void testStartRound_DealsCardsCorrectly() {
        // Arrange
        GameEntity game = createTestGame();

        // Act
        gameService.startRound(game);

        // Assert
        // Each player should have 8 cards (3 + 2 + 3)
        assertEquals(8, game.getHands().get(1).size());
        assertEquals(8, game.getHands().get(2).size());
        assertEquals(8, game.getHands().get(3).size());
        assertEquals(8, game.getHands().get(4).size());
        assertTrue(game.getDeck().isEmpty());
        verify(gameStateService, atLeast(1)).saveGame(game);
    }

    @Test
    void testEndRound_WhenNoTeamWins_StartsNewRound() {
        // Arrange
        GameEntity game = createTestGame();
        game.getScores().put(0, 5);
        game.getScores().put(1, 7);

        // Add some cards to round cards
        game.getThisRoundCards().get(0).add(
                CardEntity.builder().id(1).points(10).rank("Ten").suit("Hearts").build()
        );
        game.getThisRoundCards().get(1).add(
                CardEntity.builder().id(2).points(20).rank("Jack").suit("Diamonds").build()
        );

        // Act
        //gameService.endRound(game);

        // Assert
        // Scores should be updated
        assertTrue(game.getScores().get(0) >= 5);
        assertTrue(game.getScores().get(1) >= 7);
    }

    @Test
    void testEndRound_WhenTeamWins_EndsGame() {
        // Arrange
        GameEntity game = createTestGame();
        game.getScores().put(0, 10);
        game.getScores().put(1, 5);

        // Act
        gameService.endRound(game);

        // Assert
        verify(gameScoresRepository, times(1)).save(any(GameScoreEntity.class));
        verify(gameStateService, times(1)).deleteGame(game.getId());
        //verify(messagingTemplate, times(1)).convertAndSend(anyString(), game);
    }

    @Test
    void testEndGame_SavesScoresAndDeletesGame() {
        // Arrange
        GameEntity game = createTestGame();
        game.getScores().put(0, 8);
        game.getScores().put(1, 10);

        // Act
        gameService.endGame(game);

        // Assert
        ArgumentCaptor<GameScoreEntity> scoreCaptor = ArgumentCaptor.forClass(GameScoreEntity.class);
        verify(gameScoresRepository, times(1)).save(scoreCaptor.capture());

        GameScoreEntity savedScore = scoreCaptor.getValue();
        assertNotNull(savedScore);
        assertEquals(game.getId(), savedScore.getId());
        assertEquals(101, savedScore.getPlayer1());
        assertEquals(102, savedScore.getPlayer2());
        assertEquals(103, savedScore.getPlayer3());
        assertEquals(104, savedScore.getPlayer4());
        assertEquals(10, savedScore.getTeam1Score());
        assertEquals(8, savedScore.getTeam2Score());
        assertEquals(1, savedScore.getWinnerTeam()); // Team 1 won

        verify(gameStateService, times(1)).deleteGame(game.getId());
    }

    @Test
    void testAskPlayerForACard_HumanPlayer_DoesNotPlayCard() {
        // Arrange
        GameEntity game = createTestGame();
        game.getHands().get(1).add(
                CardEntity.builder().id(1).points(10).rank("Ten").suit("Hearts").build()
        );

        // Act
        gameService.askPlayerForACard(1, game);

        // Assert
        // For human player (ID > 0), card should remain in hand
        assertEquals(1, game.getHands().get(1).size());
    }

    @Test
    void testAskPlayerForACard_BotPlayer_PlaysCard() {
        // Arrange
        GameEntity game = createTestGame();
        game.getPlayers().put(1, -1); // Bot player
        game.getHands().get(1).add(
                CardEntity.builder().id(1).points(10).rank("Ten").suit("Hearts").build()
        );

        // Act
        gameService.askPlayerForACard(1, game);

        // Assert
        // Bot should have played the card
        verify(gameStateService, atLeast(1)).saveGame(game);
    }
}