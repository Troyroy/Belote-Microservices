package belote.ex.business.imp;


import belote.ex.persistance.entity.LobbyEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LobbyStateService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOBBY_PREFIX = "lobby:";
    private static final String ACTIVE_LOBBIES = "active:lobbies";
    private static final Duration LOBBY_TTL = Duration.ofHours(2);

    /**
     * Create new lobby
     */
    public String createLobby(LobbyEntity lobby) {
        String lobbyId = UUID.randomUUID().toString();
        lobby.setId(lobbyId);
        lobby.setCreatedAt(System.currentTimeMillis());

        saveLobby(lobby);

        // Add to active lobbies set
        redisTemplate.opsForSet().add(ACTIVE_LOBBIES, lobbyId);

        log.info("Created new lobby: {}", lobbyId);
        return lobbyId;
    }

    /**
     * Save/Update lobby state
     */
    public void saveLobby(LobbyEntity lobby) {
        lobby.setUpdatedAt(System.currentTimeMillis());
        String key = LOBBY_PREFIX + lobby.getId();
        redisTemplate.opsForValue().set(key, lobby, LOBBY_TTL);
        log.debug("Saved lobby state: {}", lobby.getId());
    }

    /**
     * Get lobby by ID
     */
    public LobbyEntity getLobby(String lobbyId) {
        String key = LOBBY_PREFIX + lobbyId;
        Object result = redisTemplate.opsForValue().get(key);

        if (result instanceof LobbyEntity) {
            return (LobbyEntity) result;
        }

        log.warn("Lobby not found: {}", lobbyId);
        return null;
    }

    /**
     * Delete lobby
     */
    public void deleteLobby(String lobbyId) {
        String key = LOBBY_PREFIX + lobbyId;
        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove(ACTIVE_LOBBIES, lobbyId);
        log.info("Deleted lobby: {}", lobbyId);
    }

    /**
     * Get all active lobbies
     */
    public Set<LobbyEntity> getActiveLobbies() {
        Set<Object> lobbyIds = redisTemplate.opsForSet().members(ACTIVE_LOBBIES);

        if (lobbyIds == null || lobbyIds.isEmpty()) {
            return Set.of();
        }

        return lobbyIds.stream()
                .map(id -> getLobby(id.toString()))
                .filter(lobby -> lobby != null)
                .collect(Collectors.toSet());
    }

    /**
     * Check if lobby exists
     */
    public boolean lobbyExists(String lobbyId) {
        String key = LOBBY_PREFIX + lobbyId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    /**
     * Add player to lobby
     */
    public boolean addPlayer(String lobbyId, Integer playerId) {
        LobbyEntity lobby = getLobby(lobbyId);

        if (lobby == null) {
            log.warn("Cannot add player to non-existent lobby: {}", lobbyId);
            return false;
        }

        if (lobby.isFull()) {
            log.warn("Lobby {} is full", lobbyId);
            return false;
        }


        lobby.addPlayer(playerId);
        saveLobby(lobby);
        log.info("Player {} joined lobby {}", playerId, lobbyId);
        return true;
    }

    /**
     * Remove player from lobby
     */
    public boolean removePlayer(String lobbyId, Integer playerId) {
        LobbyEntity lobby = getLobby(lobbyId);

        if (lobby == null) {
            return false;
        }

        lobby.removePlayer(playerId);

        // If no players left, delete lobby
        if (lobby.getPlayerIds().isEmpty()) {
            deleteLobby(lobbyId);
            log.info("Deleted empty lobby: {}", lobbyId);
        } else {
            saveLobby(lobby);
            log.info("Player {} left lobby {}", playerId, lobbyId);
        }

        return true;
    }

    /**
     * Extend lobby TTL (for active lobbies)
     */
    public void extendLobbyTTL(String lobbyId) {
        String key = LOBBY_PREFIX + lobbyId;
        redisTemplate.expire(key, LOBBY_TTL);
    }
}
