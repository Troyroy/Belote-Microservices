package belote.ex.business.imp;

import belote.ex.config.LobbyRabbitMQConfig;
import belote.ex.events.LobbyReadyEvent;
import belote.ex.persistance.entity.LobbyEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTests {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private LobbyStateService lobbyStateService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private LobbyService lobbyService;

    private LobbyEntity testLobby;
    private String testLobbyId;
    private String testHostId;

    @BeforeEach
    void setUp() {
        testLobbyId = "lobby-123";
        testHostId = "101";
        testLobby = createTestLobby();
    }

    private LobbyEntity createTestLobby() {
        LobbyEntity lobby = new LobbyEntity(testLobbyId, testHostId);
        lobby.addPlayer(101);
        return lobby;
    }

    // ============= CREATE LOBBY TESTS =============

    @Test
    void testCreateLobby_Success() {
        // Arrange
        String lobbyName = "Test Lobby";
        String gameMode = "CLASSIC";
        String expectedLobbyId = "lobby-new-123";

        when(lobbyStateService.createLobby(any(LobbyEntity.class))).thenReturn(expectedLobbyId);

        // Act
        String result = lobbyService.createLobby(lobbyName, testHostId, gameMode);

        // Assert
        assertNotNull(result);
        assertEquals(expectedLobbyId, result);
        verify(lobbyStateService, times(1)).createLobby(any(LobbyEntity.class));
    }

    @Test
    void testCreateLobby_HostIsAddedAsPlayer() {
        // Arrange
        String lobbyName = "Test Lobby";
        String gameMode = "CLASSIC";
        ArgumentCaptor<LobbyEntity> lobbyCaptor = ArgumentCaptor.forClass(LobbyEntity.class);

        when(lobbyStateService.createLobby(any(LobbyEntity.class))).thenReturn(testLobbyId);

        // Act
        lobbyService.createLobby(lobbyName, testHostId, gameMode);

        // Assert
        verify(lobbyStateService).createLobby(lobbyCaptor.capture());
        LobbyEntity capturedLobby = lobbyCaptor.getValue();
        assertTrue(capturedLobby.getPlayerIds().contains(Integer.parseInt(testHostId)));
    }

    @Test
    void testCreateLobby_WithDifferentHostIds() {
        // Arrange
        String[] hostIds = {"101", "202", "303", "404"};

        when(lobbyStateService.createLobby(any(LobbyEntity.class)))
                .thenReturn("lobby-1", "lobby-2", "lobby-3", "lobby-4");

        // Act & Assert
        for (String hostId : hostIds) {
            String result = lobbyService.createLobby("Lobby", hostId, "CLASSIC");
            assertNotNull(result);
        }

        verify(lobbyStateService, times(4)).createLobby(any(LobbyEntity.class));
    }

    @Test
    void testCreateLobby_WithNullLobbyName() {
        // Arrange
        when(lobbyStateService.createLobby(any(LobbyEntity.class))).thenReturn(testLobbyId);

        // Act
        String result = lobbyService.createLobby(null, testHostId, "CLASSIC");

        // Assert
        assertNotNull(result);
        verify(lobbyStateService, times(1)).createLobby(any(LobbyEntity.class));
    }

    @Test
    void testCreateLobby_WithNullGameMode() {
        // Arrange
        when(lobbyStateService.createLobby(any(LobbyEntity.class))).thenReturn(testLobbyId);

        // Act
        String result = lobbyService.createLobby("Test Lobby", testHostId, null);

        // Assert
        assertNotNull(result);
        verify(lobbyStateService, times(1)).createLobby(any(LobbyEntity.class));
    }

    // ============= JOIN LOBBY TESTS =============

    @Test
    void testJoinLobby_Success() {
        // Arrange
        Integer playerId = 102;
        when(lobbyStateService.addPlayer(testLobbyId, playerId)).thenReturn(true);
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);

        // Act
        boolean result = lobbyService.joinLobby(testLobbyId, playerId);

        // Assert
        assertTrue(result);
        verify(lobbyStateService, times(1)).addPlayer(testLobbyId, playerId);
        verify(lobbyStateService, times(1)).getLobby(testLobbyId);
       // verify(messagingTemplate, times(1)).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testJoinLobby_Failed() {
        // Arrange
        Integer playerId = 102;
        when(lobbyStateService.addPlayer(testLobbyId, playerId)).thenReturn(false);

        // Act
        boolean result = lobbyService.joinLobby(testLobbyId, playerId);

        // Assert
        assertFalse(result);
        verify(lobbyStateService, times(1)).addPlayer(testLobbyId, playerId);
        verify(lobbyStateService, never()).getLobby(anyString());
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testJoinLobby_MultiplePlayersJoining() {
        // Arrange
        Integer[] playerIds = {102, 103, 104};

        when(lobbyStateService.addPlayer(eq(testLobbyId), anyInt())).thenReturn(true);
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);

        // Act
        for (Integer playerId : playerIds) {
            boolean result = lobbyService.joinLobby(testLobbyId, playerId);
            assertTrue(result);
        }

        // Assert
        verify(lobbyStateService, times(3)).addPlayer(eq(testLobbyId), anyInt());
        //verify(messagingTemplate, times(3)).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testJoinLobby_WithNullPlayerId() {
        // Arrange
        when(lobbyStateService.addPlayer(testLobbyId, null)).thenReturn(false);

        // Act
        boolean result = lobbyService.joinLobby(testLobbyId, null);

        // Assert
        assertFalse(result);
    }

    // ============= LEAVE LOBBY TESTS =============

    @Test
    void testLeaveLobby_Success() {
        // Arrange
        Integer playerId = 102;
        when(lobbyStateService.removePlayer(testLobbyId, playerId)).thenReturn(true);
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);

        // Act
        boolean result = lobbyService.leaveLobby(testLobbyId, playerId);

        // Assert
        assertTrue(result);
        verify(lobbyStateService, times(1)).removePlayer(testLobbyId, playerId);
        verify(lobbyStateService, times(1)).getLobby(testLobbyId);
        //verify(messagingTemplate, times(1)).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testLeaveLobby_Failed() {
        // Arrange
        Integer playerId = 102;
        when(lobbyStateService.removePlayer(testLobbyId, playerId)).thenReturn(false);

        // Act
        boolean result = lobbyService.leaveLobby(testLobbyId, playerId);

        // Assert
        assertFalse(result);
        verify(lobbyStateService, times(1)).removePlayer(testLobbyId, playerId);
        verify(lobbyStateService, never()).getLobby(anyString());
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testLeaveLobby_LobbyIsNull_NoNotification() {
        // Arrange
        Integer playerId = 102;
        when(lobbyStateService.removePlayer(testLobbyId, playerId)).thenReturn(true);
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(null);

        // Act
        boolean result = lobbyService.leaveLobby(testLobbyId, playerId);

        // Assert
        assertTrue(result);
        verify(lobbyStateService, times(1)).getLobby(testLobbyId);
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testLeaveLobby_MultiplePlayersLeaving() {
        // Arrange
        Integer[] playerIds = {102, 103, 104};

        when(lobbyStateService.removePlayer(eq(testLobbyId), anyInt())).thenReturn(true);
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);

        // Act
        for (Integer playerId : playerIds) {
            boolean result = lobbyService.leaveLobby(testLobbyId, playerId);
            assertTrue(result);
        }

        // Assert
        verify(lobbyStateService, times(3)).removePlayer(eq(testLobbyId), anyInt());
    }

    // ============= START GAME TESTS =============

    @Test
    void testStartGame_Success() throws JsonProcessingException {
        // Arrange
        testLobby.addPlayer(102);
        testLobby.addPlayer(103);
        testLobby.addPlayer(104);

        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"lobbyId\":\"lobby-123\"}");

        // Act
        lobbyService.startGame(testLobbyId);

        // Assert
        verify(lobbyStateService, times(1)).getLobby(testLobbyId);
        verify(objectMapper, times(1)).writeValueAsString(any(LobbyReadyEvent.class));
        verify(redisTemplate, times(1)).convertAndSend(eq("lobby:events"), anyString());
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(LobbyRabbitMQConfig.GAME_EXCHANGE),
                eq(LobbyRabbitMQConfig.START_GAME_ROUTING_KEY),
                any(LobbyReadyEvent.class)
        );
        verify(lobbyStateService, times(1)).deleteLobby(testLobbyId);
    }

    @Test
    void testStartGame_LobbyNotFound_ThrowsException() {
        // Arrange
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lobbyService.startGame(testLobbyId)
        );

        assertTrue(exception.getMessage().contains("Lobby not found"));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
        verify(lobbyStateService, never()).deleteLobby(anyString());
    }

    @Test
    void testStartGame_PublishesRabbitMQEvent() throws JsonProcessingException {
        // Arrange
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LobbyReadyEvent> eventCaptor = ArgumentCaptor.forClass(LobbyReadyEvent.class);

        // Act
        lobbyService.startGame(testLobbyId);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                eventCaptor.capture()
        );

        assertEquals(LobbyRabbitMQConfig.GAME_EXCHANGE, exchangeCaptor.getValue());
        assertEquals(LobbyRabbitMQConfig.START_GAME_ROUTING_KEY, routingKeyCaptor.getValue());

        LobbyReadyEvent event = eventCaptor.getValue();
        assertEquals(testLobbyId, event.getLobbyId());
        assertNotNull(event.getPlayerIds());
    }

    @Test
    void testStartGame_PublishesRedisEvent() throws JsonProcessingException {
        // Arrange
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);
        String expectedJson = "{\"lobbyId\":\"lobby-123\",\"playerIds\":[101]}";
        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);

        ArgumentCaptor<String> channelCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        lobbyService.startGame(testLobbyId);

        // Assert
        verify(redisTemplate, times(1)).convertAndSend(
                channelCaptor.capture(),
                messageCaptor.capture()
        );

        assertEquals("lobby:events", channelCaptor.getValue());
        assertEquals(expectedJson, messageCaptor.getValue());
    }

    @Test
    void testStartGame_JsonProcessingException_ThrowsRuntimeException() throws JsonProcessingException {
        // Arrange
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("JSON error") {});

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> lobbyService.startGame(testLobbyId)
        );

        assertTrue(exception.getMessage().contains("Failed to start game"));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testStartGame_WithMultiplePlayers() throws JsonProcessingException {
        // Arrange
        testLobby.addPlayer(102);
        testLobby.addPlayer(103);
        testLobby.addPlayer(104);

        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ArgumentCaptor<LobbyReadyEvent> eventCaptor = ArgumentCaptor.forClass(LobbyReadyEvent.class);

        // Act
        lobbyService.startGame(testLobbyId);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture()
        );

        LobbyReadyEvent event = eventCaptor.getValue();
        assertEquals(4, event.getPlayerIds().size());
    }

    // ============= GET LOBBY TESTS =============

    @Test
    void testGetLobby_Success() {
        // Arrange
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);

        // Act
        LobbyEntity result = lobbyService.getLobby(testLobbyId);

        // Assert
        assertNotNull(result);
        assertEquals(testLobby, result);
        verify(lobbyStateService, times(1)).getLobby(testLobbyId);
    }

    @Test
    void testGetLobby_NotFound_ReturnsNull() {
        // Arrange
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(null);

        // Act
        LobbyEntity result = lobbyService.getLobby(testLobbyId);

        // Assert
        assertNull(result);
        verify(lobbyStateService, times(1)).getLobby(testLobbyId);
    }

    @Test
    void testGetLobby_WithDifferentLobbyIds() {
        // Arrange
        String[] lobbyIds = {"lobby-1", "lobby-2", "lobby-3"};

        when(lobbyStateService.getLobby(anyString()))
                .thenReturn(testLobby, null, testLobby);

        // Act & Assert
        assertNotNull(lobbyService.getLobby(lobbyIds[0]));
        assertNull(lobbyService.getLobby(lobbyIds[1]));
        assertNotNull(lobbyService.getLobby(lobbyIds[2]));

        verify(lobbyStateService, times(3)).getLobby(anyString());
    }

    // ============= GET ACTIVE LOBBIES TESTS =============

    @Test
    void testGetActiveLobbies_ReturnsMultipleLobbies() {
        // Arrange
        LobbyEntity lobby1 = new LobbyEntity("lobby-1", "101");
        LobbyEntity lobby2 = new LobbyEntity("lobby-2", "102");
        LobbyEntity lobby3 = new LobbyEntity("lobby-3", "103");

        Set<LobbyEntity> activeLobbies = Set.of(lobby1, lobby2, lobby3);
        when(lobbyStateService.getActiveLobbies()).thenReturn((Set<LobbyEntity>) activeLobbies);

        // Act
        List<LobbyEntity> result = lobbyService.getActiveLobbies();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(lobbyStateService, times(1)).getActiveLobbies();
    }

    @Test
    void testGetActiveLobbies_ReturnsEmptyList() {
        // Arrange
        Set<LobbyEntity> emptyLobbies = Collections.emptySet();
        when(lobbyStateService.getActiveLobbies()).thenReturn(emptyLobbies);

        // Act
        List<LobbyEntity> result = lobbyService.getActiveLobbies();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(lobbyStateService, times(1)).getActiveLobbies();
    }

    @Test
    void testGetActiveLobbies_ReturnsSingleLobby() {
        // Arrange
        Set<LobbyEntity> singleLobby = Set.of(testLobby);
        when(lobbyStateService.getActiveLobbies()).thenReturn((Set<LobbyEntity>) singleLobby);

        // Act
        List<LobbyEntity> result = lobbyService.getActiveLobbies();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLobby, result.get(0));
        verify(lobbyStateService, times(1)).getActiveLobbies();
    }

    // ============= INTEGRATION TESTS =============

    @Test
    void testLobbyLifecycle_CreateJoinLeave() throws JsonProcessingException {
        // Arrange
        Integer playerId = 102;

        when(lobbyStateService.createLobby(any(LobbyEntity.class))).thenReturn(testLobbyId);
        when(lobbyStateService.addPlayer(testLobbyId, playerId)).thenReturn(true);
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);
        when(lobbyStateService.removePlayer(testLobbyId, playerId)).thenReturn(true);

        // Act - Create
        String lobbyId = lobbyService.createLobby("Test", testHostId, "CLASSIC");

        // Act - Join
        boolean joined = lobbyService.joinLobby(lobbyId, playerId);

        // Act - Leave
        boolean left = lobbyService.leaveLobby(lobbyId, playerId);

        // Assert
        assertNotNull(lobbyId);
        assertTrue(joined);
        assertTrue(left);

        verify(lobbyStateService, times(1)).createLobby(any());
        verify(lobbyStateService, times(1)).addPlayer(testLobbyId, playerId);
        verify(lobbyStateService, times(1)).removePlayer(testLobbyId, playerId);
    }

    @Test
    void testLobbyLifecycle_CreateJoinStartGame() throws JsonProcessingException {
        // Arrange
        Integer playerId = 102;

        when(lobbyStateService.createLobby(any(LobbyEntity.class))).thenReturn(testLobbyId);
        when(lobbyStateService.addPlayer(testLobbyId, playerId)).thenReturn(true);
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        String lobbyId = lobbyService.createLobby("Test", testHostId, "CLASSIC");
        lobbyService.joinLobby(lobbyId, playerId);
        lobbyService.startGame(lobbyId);

        // Assert
        verify(lobbyStateService, times(1)).createLobby(any());
        verify(lobbyStateService, times(1)).addPlayer(testLobbyId, playerId);
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
        verify(lobbyStateService, times(1)).deleteLobby(testLobbyId);
    }

    @Test
    void testMultiplePlayersJoinAndStart() throws JsonProcessingException {
        // Arrange
        Integer[] playerIds = {102, 103, 104};

        when(lobbyStateService.createLobby(any())).thenReturn(testLobbyId);
        when(lobbyStateService.addPlayer(eq(testLobbyId), anyInt())).thenReturn(true);
        when(lobbyStateService.getLobby(testLobbyId)).thenReturn(testLobby);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        lobbyService.createLobby("Test", testHostId, "CLASSIC");

        for (Integer playerId : playerIds) {
            lobbyService.joinLobby(testLobbyId, playerId);
        }

        lobbyService.startGame(testLobbyId);

        // Assert
        verify(lobbyStateService, times(3)).addPlayer(eq(testLobbyId), anyInt());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
      //  verify(messagingTemplate, times(4)).convertAndSend(Optional.of(anyString()), any()); // 3 joins + 1 game ready
    }
}