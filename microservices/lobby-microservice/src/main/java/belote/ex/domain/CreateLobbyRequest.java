package belote.ex.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLobbyRequest {
    @NotBlank
    private String lobbyName;

    @NotBlank
    private String hostId;

    private String gameMode = "classic";
}
