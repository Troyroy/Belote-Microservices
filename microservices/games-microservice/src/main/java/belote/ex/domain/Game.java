package belote.ex.domain;

import belote.ex.persistance.entity.CardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    private int id;
    private int testVal;

    private ArrayList<CardEntity> deck;

    private CardEntity cardToAnswer;

    private Queue<Integer> order;

    private HashMap<Integer,Integer> playerID;

    private HashMap<Integer,CardEntity[]> hands;

}
