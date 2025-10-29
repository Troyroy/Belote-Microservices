package belote.ex.persistance;

import belote.ex.persistance.entity.GameScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface GameScoreRepository extends JpaRepository<GameScoreEntity, String> {

    // Find games by player
    @Query("SELECT g FROM GameScoreEntity g WHERE g.player1 = ?1 OR g.player2 = ?1 OR g.player3 = ?1 OR g.player4 = ?1")
    List<GameScoreEntity> findGamesByPlayerId(Integer playerId);

    // Find games won by team
    List<GameScoreEntity> findByWinnerTeam(Integer winnerTeam);

    // Count wins for a player
    @Query("SELECT COUNT(g) FROM GameScoreEntity g WHERE " +
            "(g.player1 = ?1 OR g.player2 = ?1) AND g.winnerTeam = 1 OR " +
            "(g.player3 = ?1 OR g.player4 = ?1) AND g.winnerTeam = 2")
    Long countWinsByPlayerId(Integer playerId);
}
