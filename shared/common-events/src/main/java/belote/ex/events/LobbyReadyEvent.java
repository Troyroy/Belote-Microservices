package belote.ex.events;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LobbyReadyEvent implements Serializable {
    private String lobbyId;
    private List<Integer> playerIds;
    //private Long timestamp;
}
