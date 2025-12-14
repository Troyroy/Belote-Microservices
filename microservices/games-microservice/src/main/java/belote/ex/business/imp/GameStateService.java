package belote.ex.business.imp;

import belote.ex.business.GameStateServiceInt;
import belote.ex.persistance.entity.GameEntity;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class GameStateService implements GameStateServiceInt {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GAME_PREFIX = "game:";
    private static final Duration GAME_TTL = Duration.ofMinutes(40); //Temporary

    public GameStateService(RedisTemplate<String, Object> redisTemplate,
                            MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;

        // Register gauge that counts game:* keys directly
        Gauge.builder("belote.games.active.count", this,
                        service -> service.getActiveGameCount())
                .description("Current number of active games in Redis")
                .register(meterRegistry);
    }

    public String createGame(GameEntity game, String lobbyID) {
        String gameId = lobbyID;
        game.setId(gameId);
        game.setStatus("WAITING");
        game.setCreatedAt(System.currentTimeMillis());

        saveGame(game);

        log.info("Created new game: {}", gameId);
        return gameId;
    }

    public void saveGame(GameEntity game) {
        game.setUpdatedAt(System.currentTimeMillis());
        String key = GAME_PREFIX + game.getId();
        redisTemplate.opsForValue().set(key, game, GAME_TTL);
        log.debug("Saved game state: {}", game.getId());
    }

    public GameEntity getGame(String gameId) {
        String key = GAME_PREFIX + gameId;
        Object result = redisTemplate.opsForValue().get(key);

        if (result instanceof GameEntity) {
            return (GameEntity) result;
        }

        log.warn("Game not found: {}", gameId);
        return null;
    }

    public void deleteGame(String gameId) {
        String key = GAME_PREFIX + gameId;
        redisTemplate.delete(key);
        log.info("Deleted game: {}", gameId);
    }

    public long getActiveGameCount() {
        AtomicLong count = new AtomicLong(0);

        redisTemplate.execute((RedisCallback<Object>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(GAME_PREFIX + "*")
                    .count(100)
                    .build();

            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                cursor.next();
                count.incrementAndGet();
            }
            cursor.close();
            return null;
        });

        return count.get();
    }


    public Set<String> getActiveGameIds() {
        return redisTemplate.keys(GAME_PREFIX + "*");
    }

    public boolean gameExists(String gameId) {
        String key = GAME_PREFIX + gameId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void updateGameStatus(String gameId, String status) {
        GameEntity game = getGame(gameId);
        if (game != null) {
            game.setStatus(status);

            if ("FINISHED".equals(status) || "ABANDONED".equals(status)) {
                deleteGame(gameId);
                log.info("Game finished/abandoned and removed: {}", gameId);
            } else {
                saveGame(game);
            }
        }
    }

    // Extend game TTL (for long games)
    public void extendGameTTL(String gameId) {
        String key = GAME_PREFIX + gameId;
        redisTemplate.expire(key, GAME_TTL);
        log.debug("Extended TTL for game: {}", gameId);
    }
}