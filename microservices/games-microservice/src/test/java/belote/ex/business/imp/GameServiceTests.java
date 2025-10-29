//package belote.ex.business.imp;
//import belote.ex.persistance.GameRepositoryInt;
//import belote.ex.persistance.entity.CardEntity;
//import belote.ex.persistance.entity.GameEntity;
//import belote.ex.persistance.entity.GameScoreEntity;
//import belote.ex.persistance.entity.UserEntity;
//import jakarta.persistence.criteria.CriteriaBuilder;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
// class GameServiceTests {
//
//
//    @Mock
//    private GameRepositoryInt repository;
//
//    @Mock
//    private SimpMessagingTemplate messagingTemplate;
//
//    @Mock
//    private GameScoresRepository scoresRepository;
//
//    @InjectMocks
//    private GameService service;
//
//    @BeforeEach
//    public void init() {
//        MockitoAnnotations.initMocks(this);
//    }
//
//
//
//    public GameEntity createGameObject(){
//        CardEntity[] deckOfCards = new CardEntity[32];
//
//        String[] suits = {"Diamonds", "Clubs", "Hearts", "Spades"};
//        String[] ranks = {"King", "Queen", "Jack", "Ten", "Nine", "Eight", "Seven", "Ace"};
//
//        deckOfCards[0] = (CardEntity.builder().id(0).points(11).rank(ranks[7]).suit(suits[0]).build());
//        deckOfCards[1] = (CardEntity.builder().id(1).points(4).rank(ranks[0]).suit(suits[0]).build());
//        deckOfCards[2] = (CardEntity.builder().id(2).points(3).rank(ranks[1]).suit(suits[0]).build());
//        deckOfCards[3] = (CardEntity.builder().id(3).points(20).rank(ranks[2]).suit(suits[0]).build());
//        deckOfCards[4] = (CardEntity.builder().id(4).points(10).rank(ranks[3]).suit(suits[0]).build());
//        deckOfCards[5] = (CardEntity.builder().id(5).points(14).rank(ranks[4]).suit(suits[0]).build());
//        deckOfCards[6] = (CardEntity.builder().id(6).points(0).rank(ranks[5]).suit(suits[0]).build());
//        deckOfCards[7] = (CardEntity.builder().id(7).points(-1).rank(ranks[6]).suit(suits[0]).build());
//
//        deckOfCards[8] = (CardEntity.builder().id(8).points(11).rank(ranks[7]).suit(suits[1]).build());
//        deckOfCards[9] = (CardEntity.builder().id(9).points(4).rank(ranks[0]).suit(suits[1]).build());
//        deckOfCards[10] = (CardEntity.builder().id(10).points(3).rank(ranks[1]).suit(suits[1]).build());
//        deckOfCards[11] = (CardEntity.builder().id(11).points(20).rank(ranks[2]).suit(suits[1]).build());
//        deckOfCards[12] = (CardEntity.builder().id(12).points(10).rank(ranks[3]).suit(suits[1]).build());
//        deckOfCards[13] = (CardEntity.builder().id(13).points(14).rank(ranks[4]).suit(suits[1]).build());
//        deckOfCards[14] = (CardEntity.builder().id(14).points(0).rank(ranks[5]).suit(suits[1]).build());
//        deckOfCards[15] = (CardEntity.builder().id(15).points(-1).rank(ranks[6]).suit(suits[1]).build());
//
//
//        deckOfCards[16] = (CardEntity.builder().id(16).points(11).rank(ranks[7]).suit(suits[2]).build());
//        deckOfCards[17] = (CardEntity.builder().id(17).points(4).rank(ranks[0]).suit(suits[2]).build());
//        deckOfCards[18] = (CardEntity.builder().id(18).points(3).rank(ranks[1]).suit(suits[2]).build());
//        deckOfCards[19] = (CardEntity.builder().id(19).points(20).rank(ranks[2]).suit(suits[2]).build());
//        deckOfCards[20] = (CardEntity.builder().id(20).points(10).rank(ranks[3]).suit(suits[2]).build());
//        deckOfCards[21] = (CardEntity.builder().id(21).points(14).rank(ranks[4]).suit(suits[2]).build());
//        deckOfCards[22] = (CardEntity.builder().id(22).points(0).rank(ranks[5]).suit(suits[2]).build());
//        deckOfCards[23] = (CardEntity.builder().id(23).points(-1).rank(ranks[6]).suit(suits[2]).build());
//
//
//        deckOfCards[24] = (CardEntity.builder().id(24).points(11).rank(ranks[7]).suit(suits[3]).build());
//        deckOfCards[25] = (CardEntity.builder().id(25).points(4).rank(ranks[0]).suit(suits[3]).build());
//        deckOfCards[26] = (CardEntity.builder().id(26).points(3).rank(ranks[1]).suit(suits[3]).build());
//        deckOfCards[27] = (CardEntity.builder().id(27).points(20).rank(ranks[2]).suit(suits[3]).build());
//        deckOfCards[28] = (CardEntity.builder().id(28).points(10).rank(ranks[3]).suit(suits[3]).build());
//        deckOfCards[29] = (CardEntity.builder().id(29).points(14).rank(ranks[4]).suit(suits[3]).build());
//        deckOfCards[30] = (CardEntity.builder().id(30).points(0).rank(ranks[5]).suit(suits[3]).build());
//        deckOfCards[31] = (CardEntity.builder().id(31).points(-1).rank(ranks[6]).suit(suits[3]).build());
//
//
//
//
//        HashMap<Integer,UserEntity> players = new HashMap<>();
//
//
//        int botID = -1;
//        for(int i=1; i<5; i++) {
//                players.put(i, UserEntity.builder().id(botID).build());
//                botID--;
//
//        }
//
//        HashMap<Integer,ArrayList<CardEntity>> hands = new HashMap<Integer,ArrayList<CardEntity>>();
//
//        hands.put(1,new ArrayList<>());
//        hands.put(2,new ArrayList<>());
//        hands.put(3,new ArrayList<>());
//        hands.put(4,new ArrayList<>());
//
//        Deque<Integer> order = new LinkedList<Integer>();
//
//        order.add(1);
//        order.add(2);
//        order.add(3);
//        order.add(4);
//
//        HashMap<Integer,ArrayList<CardEntity>> roundCards = new HashMap<>();
//        roundCards.put(0,new ArrayList<>());
//        roundCards.put(1,new ArrayList<>());
//
//        HashMap<Integer,Integer> scores = new HashMap<Integer, Integer>();
//        scores.put(0,0);
//        scores.put(1,0);
//
//        return  GameEntity.builder().id(1).players(players).thisRoundCards(roundCards).bucket(new ArrayList<>()).currentBucket(1).order(order).hands(hands).deck(deckOfCards).firstPlayerBucket(1).firstPlayerRound(1).scores(scores).build();
//    }
//
//    public GameEntity createOngoingGameObject(){
//        CardEntity[] deckOfCards = new CardEntity[32];
//
//        String[] suits = {"Diamonds", "Clubs", "Hearts", "Spades"};
//        String[] ranks = {"King", "Queen", "Jack", "Ten", "Nine", "Eight", "Seven", "Ace"};
//
//        deckOfCards[0] = (CardEntity.builder().id(0).points(11).rank(ranks[7]).suit(suits[0]).build());
//        deckOfCards[1] = (CardEntity.builder().id(1).points(4).rank(ranks[0]).suit(suits[0]).build());
//        deckOfCards[2] = (CardEntity.builder().id(2).points(3).rank(ranks[1]).suit(suits[0]).build());
//        deckOfCards[3] = (CardEntity.builder().id(3).points(20).rank(ranks[2]).suit(suits[0]).build());
//        deckOfCards[4] = (CardEntity.builder().id(4).points(10).rank(ranks[3]).suit(suits[0]).build());
//        deckOfCards[5] = (CardEntity.builder().id(5).points(14).rank(ranks[4]).suit(suits[0]).build());
//        deckOfCards[6] = (CardEntity.builder().id(6).points(0).rank(ranks[5]).suit(suits[0]).build());
//        deckOfCards[7] = (CardEntity.builder().id(7).points(-1).rank(ranks[6]).suit(suits[0]).build());
//
//        deckOfCards[8] = (CardEntity.builder().id(8).points(11).rank(ranks[7]).suit(suits[1]).build());
//        deckOfCards[9] = (CardEntity.builder().id(9).points(4).rank(ranks[0]).suit(suits[1]).build());
//        deckOfCards[10] = (CardEntity.builder().id(10).points(3).rank(ranks[1]).suit(suits[1]).build());
//        deckOfCards[11] = (CardEntity.builder().id(11).points(20).rank(ranks[2]).suit(suits[1]).build());
//        deckOfCards[12] = (CardEntity.builder().id(12).points(10).rank(ranks[3]).suit(suits[1]).build());
//        deckOfCards[13] = (CardEntity.builder().id(13).points(14).rank(ranks[4]).suit(suits[1]).build());
//        deckOfCards[14] = (CardEntity.builder().id(14).points(0).rank(ranks[5]).suit(suits[1]).build());
//        deckOfCards[15] = (CardEntity.builder().id(15).points(-1).rank(ranks[6]).suit(suits[1]).build());
//
//
//        deckOfCards[16] = (CardEntity.builder().id(16).points(11).rank(ranks[7]).suit(suits[2]).build());
//        deckOfCards[17] = (CardEntity.builder().id(17).points(4).rank(ranks[0]).suit(suits[2]).build());
//        deckOfCards[18] = (CardEntity.builder().id(18).points(3).rank(ranks[1]).suit(suits[2]).build());
//        deckOfCards[19] = (CardEntity.builder().id(19).points(20).rank(ranks[2]).suit(suits[2]).build());
//        deckOfCards[20] = (CardEntity.builder().id(20).points(10).rank(ranks[3]).suit(suits[2]).build());
//        deckOfCards[21] = (CardEntity.builder().id(21).points(14).rank(ranks[4]).suit(suits[2]).build());
//        deckOfCards[22] = (CardEntity.builder().id(22).points(0).rank(ranks[5]).suit(suits[2]).build());
//        deckOfCards[23] = (CardEntity.builder().id(23).points(-1).rank(ranks[6]).suit(suits[2]).build());
//
//
//        deckOfCards[24] = (CardEntity.builder().id(24).points(11).rank(ranks[7]).suit(suits[3]).build());
//        deckOfCards[25] = (CardEntity.builder().id(25).points(4).rank(ranks[0]).suit(suits[3]).build());
//        deckOfCards[26] = (CardEntity.builder().id(26).points(3).rank(ranks[1]).suit(suits[3]).build());
//        deckOfCards[27] = (CardEntity.builder().id(27).points(20).rank(ranks[2]).suit(suits[3]).build());
//        deckOfCards[28] = (CardEntity.builder().id(28).points(10).rank(ranks[3]).suit(suits[3]).build());
//        deckOfCards[29] = (CardEntity.builder().id(29).points(14).rank(ranks[4]).suit(suits[3]).build());
//        deckOfCards[30] = (CardEntity.builder().id(30).points(0).rank(ranks[5]).suit(suits[3]).build());
//        deckOfCards[31] = (CardEntity.builder().id(31).points(-1).rank(ranks[6]).suit(suits[3]).build());
//
//
//
//
//        HashMap<Integer,UserEntity> players = new HashMap<>();
//
//        players.put(1,UserEntity.builder().id(1).build());
//        int botID = -1;
//        for(int i=2; i<5; i++) {
//            players.put(i, UserEntity.builder().id(botID).build());
//            botID--;
//
//        }
//        HashMap<Integer,ArrayList<CardEntity>> hands = new HashMap<Integer,ArrayList<CardEntity>>();
//
//        hands.put(1,new ArrayList<>());
//        hands.get(1).add(deckOfCards[0]);
//        hands.get(1).add(deckOfCards[1]);
//        hands.get(1).add(deckOfCards[2]);
//        hands.get(1).add(deckOfCards[3]);
//        hands.get(1).add(deckOfCards[4]);
//        hands.get(1).add(deckOfCards[5]);
//        hands.get(1).add(deckOfCards[6]);
//        hands.get(1).add(deckOfCards[7]);
//
//        hands.put(2,new ArrayList<>());
//        hands.get(2).add(deckOfCards[8]);
//        hands.get(2).add(deckOfCards[9]);
//        hands.get(2).add(deckOfCards[10]);
//        hands.get(2).add(deckOfCards[11]);
//        hands.get(2).add(deckOfCards[12]);
//        hands.get(2).add(deckOfCards[13]);
//        hands.get(2).add(deckOfCards[14]);
//        hands.get(2).add(deckOfCards[15]);
//
//        hands.put(3,new ArrayList<>());
//        hands.get(3).add(deckOfCards[16]);
//        hands.get(3).add(deckOfCards[17]);
//        hands.get(3).add(deckOfCards[18]);
//        hands.get(3).add(deckOfCards[19]);
//        hands.get(3).add(deckOfCards[20]);
//        hands.get(3).add(deckOfCards[21]);
//        hands.get(3).add(deckOfCards[22]);
//        hands.get(3).add(deckOfCards[23]);
//
//        hands.put(4,new ArrayList<>());
//        hands.get(4).add(deckOfCards[24]);
//        hands.get(4).add(deckOfCards[25]);
//        hands.get(4).add(deckOfCards[26]);
//        hands.get(4).add(deckOfCards[27]);
//        hands.get(4).add(deckOfCards[28]);
//        hands.get(4).add(deckOfCards[29]);
//        hands.get(4).add(deckOfCards[30]);
//        hands.get(4).add(deckOfCards[31]);
//
//        Deque<Integer> order = new LinkedList<Integer>();
//
//        order.add(1);
//        order.add(2);
//        order.add(3);
//        order.add(4);
//
//        HashMap<Integer,ArrayList<CardEntity>> roundCards = new HashMap<>();
//        roundCards.put(0,new ArrayList<>());
//        roundCards.put(1,new ArrayList<>());
//
//        return  GameEntity.builder().id(1).players(players).thisRoundCards(roundCards).bucket(new ArrayList<>()).currentBucket(1).order(order).hands(hands).deck(deckOfCards).firstPlayerBucket(1).firstPlayerRound(1).build();
//    }
//
//     public GameEntity endingGame(){
//         CardEntity[] deckOfCards = new CardEntity[32];
//
//         String[] suits = {"Diamonds", "Clubs", "Hearts", "Spades"};
//         String[] ranks = {"King", "Queen", "Jack", "Ten", "Nine", "Eight", "Seven", "Ace"};
//
//         deckOfCards[0] = (CardEntity.builder().id(0).points(11).rank(ranks[7]).suit(suits[0]).build());
//         deckOfCards[1] = (CardEntity.builder().id(1).points(4).rank(ranks[0]).suit(suits[0]).build());
//         deckOfCards[2] = (CardEntity.builder().id(2).points(3).rank(ranks[1]).suit(suits[0]).build());
//         deckOfCards[3] = (CardEntity.builder().id(3).points(20).rank(ranks[2]).suit(suits[0]).build());
//         deckOfCards[4] = (CardEntity.builder().id(4).points(10).rank(ranks[3]).suit(suits[0]).build());
//         deckOfCards[5] = (CardEntity.builder().id(5).points(14).rank(ranks[4]).suit(suits[0]).build());
//         deckOfCards[6] = (CardEntity.builder().id(6).points(0).rank(ranks[5]).suit(suits[0]).build());
//         deckOfCards[7] = (CardEntity.builder().id(7).points(-1).rank(ranks[6]).suit(suits[0]).build());
//
//         deckOfCards[8] = (CardEntity.builder().id(8).points(11).rank(ranks[7]).suit(suits[1]).build());
//         deckOfCards[9] = (CardEntity.builder().id(9).points(4).rank(ranks[0]).suit(suits[1]).build());
//         deckOfCards[10] = (CardEntity.builder().id(10).points(3).rank(ranks[1]).suit(suits[1]).build());
//         deckOfCards[11] = (CardEntity.builder().id(11).points(20).rank(ranks[2]).suit(suits[1]).build());
//         deckOfCards[12] = (CardEntity.builder().id(12).points(10).rank(ranks[3]).suit(suits[1]).build());
//         deckOfCards[13] = (CardEntity.builder().id(13).points(14).rank(ranks[4]).suit(suits[1]).build());
//         deckOfCards[14] = (CardEntity.builder().id(14).points(0).rank(ranks[5]).suit(suits[1]).build());
//         deckOfCards[15] = (CardEntity.builder().id(15).points(-1).rank(ranks[6]).suit(suits[1]).build());
//
//
//         deckOfCards[16] = (CardEntity.builder().id(16).points(11).rank(ranks[7]).suit(suits[2]).build());
//         deckOfCards[17] = (CardEntity.builder().id(17).points(4).rank(ranks[0]).suit(suits[2]).build());
//         deckOfCards[18] = (CardEntity.builder().id(18).points(3).rank(ranks[1]).suit(suits[2]).build());
//         deckOfCards[19] = (CardEntity.builder().id(19).points(20).rank(ranks[2]).suit(suits[2]).build());
//         deckOfCards[20] = (CardEntity.builder().id(20).points(10).rank(ranks[3]).suit(suits[2]).build());
//         deckOfCards[21] = (CardEntity.builder().id(21).points(14).rank(ranks[4]).suit(suits[2]).build());
//         deckOfCards[22] = (CardEntity.builder().id(22).points(0).rank(ranks[5]).suit(suits[2]).build());
//         deckOfCards[23] = (CardEntity.builder().id(23).points(-1).rank(ranks[6]).suit(suits[2]).build());
//
//
//         deckOfCards[24] = (CardEntity.builder().id(24).points(11).rank(ranks[7]).suit(suits[3]).build());
//         deckOfCards[25] = (CardEntity.builder().id(25).points(4).rank(ranks[0]).suit(suits[3]).build());
//         deckOfCards[26] = (CardEntity.builder().id(26).points(3).rank(ranks[1]).suit(suits[3]).build());
//         deckOfCards[27] = (CardEntity.builder().id(27).points(20).rank(ranks[2]).suit(suits[3]).build());
//         deckOfCards[28] = (CardEntity.builder().id(28).points(10).rank(ranks[3]).suit(suits[3]).build());
//         deckOfCards[29] = (CardEntity.builder().id(29).points(14).rank(ranks[4]).suit(suits[3]).build());
//         deckOfCards[30] = (CardEntity.builder().id(30).points(0).rank(ranks[5]).suit(suits[3]).build());
//         deckOfCards[31] = (CardEntity.builder().id(31).points(-1).rank(ranks[6]).suit(suits[3]).build());
//
//
//
//
//         HashMap<Integer,UserEntity> players = new HashMap<>();
//
//         players.put(1,UserEntity.builder().id(1).build());
//         int botID = -1;
//         for(int i=2; i<5; i++) {
//             players.put(i, UserEntity.builder().id(botID).build());
//             botID--;
//
//         }
//
//         HashMap<Integer,ArrayList<CardEntity>> hands = new HashMap<Integer,ArrayList<CardEntity>>();
//
//         hands.put(1,new ArrayList<>());
//         hands.get(1).add(deckOfCards[0]);
//         hands.get(1).add(deckOfCards[1]);
//         hands.get(1).add(deckOfCards[2]);
//         hands.get(1).add(deckOfCards[3]);
//         hands.get(1).add(deckOfCards[4]);
//         hands.get(1).add(deckOfCards[5]);
//         hands.get(1).add(deckOfCards[6]);
//         hands.get(1).add(deckOfCards[7]);
//
//         hands.put(2,new ArrayList<>());
//         hands.get(2).add(deckOfCards[8]);
//         hands.get(2).add(deckOfCards[9]);
//         hands.get(2).add(deckOfCards[10]);
//         hands.get(2).add(deckOfCards[11]);
//         hands.get(2).add(deckOfCards[12]);
//         hands.get(2).add(deckOfCards[13]);
//         hands.get(2).add(deckOfCards[14]);
//         hands.get(2).add(deckOfCards[15]);
//
//         hands.put(3,new ArrayList<>());
//         hands.get(3).add(deckOfCards[16]);
//         hands.get(3).add(deckOfCards[17]);
//         hands.get(3).add(deckOfCards[18]);
//         hands.get(3).add(deckOfCards[19]);
//         hands.get(3).add(deckOfCards[20]);
//         hands.get(3).add(deckOfCards[21]);
//         hands.get(3).add(deckOfCards[22]);
//         hands.get(3).add(deckOfCards[23]);
//
//         hands.put(4,new ArrayList<>());
//         hands.get(4).add(deckOfCards[24]);
//         hands.get(4).add(deckOfCards[25]);
//         hands.get(4).add(deckOfCards[26]);
//         hands.get(4).add(deckOfCards[27]);
//         hands.get(4).add(deckOfCards[28]);
//         hands.get(4).add(deckOfCards[29]);
//         hands.get(4).add(deckOfCards[30]);
//         hands.get(4).add(deckOfCards[31]);
//
//         Deque<Integer> order = new LinkedList<Integer>();
//
//         order.add(1);
//         order.add(2);
//         order.add(3);
//         order.add(4);
//
//         HashMap<Integer,ArrayList<CardEntity>> roundCards = new HashMap<>();
//         roundCards.put(0,new ArrayList<>());
//         roundCards.put(1,new ArrayList<>());
//
//         roundCards.get(1).add(deckOfCards[4]);
//         roundCards.get(1).add(deckOfCards[11]);
//         roundCards.get(1).add(deckOfCards[18]);
//         roundCards.get(1).add(deckOfCards[25]);
//
//         HashMap<Integer,Integer> scores = new HashMap<Integer, Integer>();
//         scores.put(0,0);
//         scores.put(1,0);
//
//         return  GameEntity.builder().id(1).players(players).thisRoundCards(roundCards).bucket(new ArrayList<>()).currentBucket(1).order(order).hands(hands).deck(deckOfCards).firstPlayerBucket(1).firstPlayerRound(1).scores(scores).build();
//     }
//
//
//    @Test
//     void testShuffleDeck() {
//
//        GameEntity game = createGameObject();
//        CardEntity[] deck = game.getDeck().clone();
//
//        when(repository.getGame(1)).thenReturn(game);
//        System.out.println("Hello");
//        service.shuffleDeck(1);
//
//        assertNotEquals(deck,game.getDeck());
//
//    }
//
//    @Test
//     void testGetDeck() {
//        GameEntity game = createGameObject();
//        CardEntity[] deck = game.getDeck().clone();
//
//        when(repository.getGame(1)).thenReturn(game);
//        List<CardEntity> deck2 = service.getDeck("1");
//
//        assertArrayEquals(deck,deck2.toArray());
//    }
//
//
//    @Test
//     void testSetFirstPlayer2() {
//        int gameID = 1;
//        GameEntity game = createGameObject();
//        when(repository.getGame(gameID)).thenReturn(game);
//        service.setFirstPlayer2(2, gameID);
//
//        assertEquals(3, game.getOrder().getFirst());
//    }
//
//    @Test
//     void testNextPlayer() {
//        String gameID = "1";
//        GameEntity game = createGameObject();
//        when(repository.getGame(gameID)).thenReturn(game);
//        service.nextPlayer(gameID);
//        assertEquals(2, game.getOrder().getFirst());
//    }
//
//    @Test
//     void testDealCards() {
//        String gameID = "1";
//        AtomicInteger cardInDeckId = new AtomicInteger(0);
//        GameEntity game = createGameObject();
//
//        when(repository.getGame(gameID)).thenReturn(game);
//
//        service.dealCards(gameID, cardInDeckId, 3);
//
//        assertEquals(game.getHands().get(1).get(0),CardEntity.builder().id(0).points(11).rank("Ace").suit("Diamonds").build());
//        assertEquals(game.getHands().get(2).get(0),CardEntity.builder().id(3).points(20).rank("Jack").suit("Diamonds").build());
//        assertEquals(game.getHands().get(3).get(0),CardEntity.builder().id(6).points(0).rank("Eight").suit("Diamonds").build());
//        assertEquals(game.getHands().get(4).get(0),CardEntity.builder().id(9).points(4).rank("King").suit("Clubs").build());
//
//
//    }
//
//    @Test
//     void testPlayableCards1() {
//        ArrayList<CardEntity> hand = new ArrayList<>();
//        hand.add(CardEntity.builder().id(0).points(11).rank("Ace").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(1).points(4).rank("King").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(2).points(3).rank("Queen").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(3).points(20).rank("Jack").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(4).points(10).rank("Ten").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(5).points(14).rank("Nine").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(6).points(0).rank("Eight").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(7).points(-1).rank("Seven").suit("Clubs").build());
//
//        CardEntity cardToAnswer = CardEntity.builder().id(7).points(-1).rank("Seven").suit("Diamonds").build();
//
//        List<CardEntity> playableCards = service.playableCards(hand,cardToAnswer);
//
//        assertEquals(7,playableCards.size());
//
//    }
//
//    @Test
//     void testPlayableCards2() {
//        ArrayList<CardEntity> hand = new ArrayList<>();
//        hand.add(CardEntity.builder().id(0).points(11).rank("Ace").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(1).points(4).rank("King").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(2).points(3).rank("Queen").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(7).points(-1).rank("Seven").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(4).points(10).rank("Ten").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(5).points(14).rank("Nine").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(6).points(0).rank("Eight").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(7).points(-1).rank("Seven").suit("Clubs").build());
//
//        CardEntity cardToAnswer = CardEntity.builder().id(3).points(20).rank("Jack").suit("Diamonds").build();
//
//        List<CardEntity> playableCards = service.playableCards(hand,cardToAnswer);
//
//        assertEquals(7,playableCards.size());
//
//    }
//
//    @Test
//     void testPlayableCards3() {
//        ArrayList<CardEntity> hand = new ArrayList<>();
//        hand.add(CardEntity.builder().id(0).points(11).rank("Ace").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(1).points(4).rank("King").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(2).points(3).rank("Queen").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(7).points(-1).rank("Seven").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(4).points(10).rank("Ten").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(5).points(14).rank("Nine").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(6).points(0).rank("Eight").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(7).points(-1).rank("Seven").suit("Clubs").build());
//
//        CardEntity cardToAnswer = CardEntity.builder().id(3).points(20).rank("Jack").suit("Spades").build();
//
//        List<CardEntity> playableCards = service.playableCards(hand,cardToAnswer);
//
//        assertEquals(8,playableCards.size());
//
//    }
//    @Test
//     void testPlayableCards4() {
//        ArrayList<CardEntity> hand = new ArrayList<>();
//        hand.add(CardEntity.builder().id(0).points(11).rank("Ace").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(1).points(4).rank("King").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(2).points(3).rank("Queen").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(7).points(-1).rank("Seven").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(4).points(10).rank("Ten").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(5).points(14).rank("Nine").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(6).points(0).rank("Eight").suit("Diamonds").build());
//        hand.add(CardEntity.builder().id(7).points(-1).rank("Seven").suit("Clubs").build());
//
//
//
//        List<CardEntity> playableCards = service.playableCards(hand,null);
//
//        assertEquals(8,playableCards.size());
//
//    }
//
//    @Test
//     void testPlayCard() {
//        GameEntity game = createOngoingGameObject();
//
//        when(repository.getGame(1)).thenReturn(game);
//
//        service.playCard(1,4,1,0);
//
//        assertEquals(7,game.getHands().get(1).size());
//        assertEquals(0,game.getBucket().size());
//        assertEquals(2,game.getCurrentBucket());
//        assertEquals(4,game.getThisRoundCards().get(1).size());
//    }
//
//     @Test
//     void calculateScores() {
//         GameEntity game = endingGame();
//
//         when(repository.getGame(1)).thenReturn(game);
//
//         service.calculateScores("1");
//
//         assertEquals(4,game.getScores().get(1));
//     }
//
//     @Test
//     void endGame() {
//         GameEntity game = endingGame();
//
//
//         GameScoreEntity scores = GameScoreEntity.builder().id(0).player1(UserEntity.builder().id(1).build())
//                 .player2(null)
//                 .player3(null)
//                 .player4(null)
//                 .team1Score(0)
//                 .team2Score(0)
//                 .winnerTeam(2)
//                 .build();
//
//         when(repository.getGame(1)).thenReturn(game);
//
//         service.endGame("1");
//
//         verify(scoresRepository, times(1)).save(scores);
//
//     }
//
//     @Test
//     void endRound() {
//         GameEntity game = endingGame();
//
//
//         when(repository.getGame(1)).thenReturn(game);
//
//
//         service.getGame(1).setFirstPlayerRound(-3);
//         service.getGame(1).setDeck(new CardEntity[32]);
//         service.endRound(1);
//
//         assertEquals(-3, game.getFirstPlayerRound());
//
//     }
//}
