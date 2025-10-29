package belote.ex.persistance.entity;


import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
public class WonGames {


   private String username;
   private Long gamesWon;
}
