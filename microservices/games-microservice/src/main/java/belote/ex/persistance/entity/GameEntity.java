package belote.ex.persistance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;  // Use String UUID for Redis
    private int testVal;

    private List<CardEntity> deck;  // Use List instead of array for better serialization
    private CardEntity cardToAnswer;

    private int currentBucket;
    private int firstPlayerRound;
    private int firstPlayerBucket;

    private List<CardEntity> bucket;
    private Map<Integer, Integer> scores;
    private Map<Integer, List<CardEntity>> thisRoundCards;

    private Deque<Integer> order;
    private Map<Integer, Integer> players;
    private Map<Integer, List<CardEntity>> hands;

    // Game metadata
    private String status;  // WAITING, IN_PROGRESS, FINISHED
    private Long createdAt;
    private Long updatedAt;
}