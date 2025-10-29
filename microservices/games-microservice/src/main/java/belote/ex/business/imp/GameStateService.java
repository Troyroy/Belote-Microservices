package belote.ex.business.imp;


import belote.ex.persistance.entity.GameEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameStateService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GAME_PREFIX = "game:";
    private static final String ACTIVE_GAMES = "active:games";
    private static final Duration GAME_TTL = Duration.ofHours(6);

    // Create new game
    public String createGame(GameEntity game) {
        String gameId = UUID.randomUUID().toString();
        game.setId(gameId);
        game.setStatus("WAITING");
        game.setCreatedAt(System.currentTimeMillis());

        saveGame(game);

        // Add to active games set
        redisTemplate.opsForSet().add(ACTIVE_GAMES, gameId);

        log.info("Created new game: {}", gameId);
        return gameId;
    }

    // Save/Update game state
    public void saveGame(GameEntity game) {
        game.setUpdatedAt(System.currentTimeMillis());
        String key = GAME_PREFIX + game.getId();
        redisTemplate.opsForValue().set(key, game, GAME_TTL);
        log.debug("Saved game state: {}", game.getId());
    }

    // Get game by ID
    public GameEntity getGame(String gameId) {
        String key = GAME_PREFIX + gameId;
        Object result = redisTemplate.opsForValue().get(key);

        if (result instanceof GameEntity) {
            return (GameEntity) result;
        }

        log.warn("Game not found: {}", gameId);
        return null;
    }

    // Delete game
    public void deleteGame(String gameId) {
        String key = GAME_PREFIX + gameId;
        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove(ACTIVE_GAMES, gameId);
        log.info("Deleted game: {}", gameId);
    }

    // Get all active games
    public Set<Object> getActiveGames() {
        return redisTemplate.opsForSet().members(ACTIVE_GAMES);
    }

    // Check if game exists
    public boolean gameExists(String gameId) {
        String key = GAME_PREFIX + gameId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Update game status
    public void updateGameStatus(String gameId, String status) {
        GameEntity game = getGame(gameId);
        if (game != null) {
            game.setStatus(status);
            saveGame(game);

            if ("FINISHED".equals(status) || "ABANDONED".equals(status)) {
                // Remove from active games
                redisTemplate.opsForSet().remove(ACTIVE_GAMES, gameId);
            }
        }
    }

    // Extend game TTL (for long games)
    public void extendGameTTL(String gameId) {
        String key = GAME_PREFIX + gameId;
        redisTemplate.expire(key, GAME_TTL);
    }
}