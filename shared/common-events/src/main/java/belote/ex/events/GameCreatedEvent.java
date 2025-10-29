package belote.ex.events;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameCreatedEvent implements Serializable {
    private String gameId;
    private String lobbyId;
    private Long timestamp;
}