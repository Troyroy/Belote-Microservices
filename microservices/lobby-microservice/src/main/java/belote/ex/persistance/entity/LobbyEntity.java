package belote.ex.persistance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.ArrayList;

@AllArgsConstructor
@Builder
@Data
public class LobbyEntity {
    private int id;
    private ArrayList<UserEntity> players;
}
