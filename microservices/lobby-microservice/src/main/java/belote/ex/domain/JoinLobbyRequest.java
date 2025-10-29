package belote.ex.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinLobbyRequest {
    @NotNull
    private Integer playerId;
}
