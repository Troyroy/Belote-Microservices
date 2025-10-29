package belote.ex.persistance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameScoreEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name="player1")
    private int player1;

    @Column(name="player2")
    private int player2;

    @Column(name="player3")
    private int player3;

    @Column(name="player4")
    private int player4;

    @NotNull
    @Column(name = "team1Score")
    private int team1Score;

    @NotNull
    @Column(name = "team2Score")
    private int team2Score;

    @NotNull
    @Column(name = "winner")
    private int winnerTeam;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
