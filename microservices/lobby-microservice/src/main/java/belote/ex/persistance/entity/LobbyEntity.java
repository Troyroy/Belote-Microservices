package belote.ex.persistance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.ArrayList;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LobbyEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                      // UUID
    private String hostId;                  // User ID who created lobby
    private List<Integer> playerIds;        // List of player user IDs
    private int maxPlayers;                 // Default 4 for Belote
    private Long createdAt;
    private Long updatedAt;

    public LobbyEntity(String id, String hostId) {
        this.id = id;
        this.hostId = hostId;
            this.playerIds = new ArrayList<>();
        this.maxPlayers = 4;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }


    public void addPlayer(Integer playerId) {
        if (!playerIds.contains(playerId) && playerIds.size() < 4) {
            playerIds.add(playerId);
            updatedAt = System.currentTimeMillis();
        }
    }

    public void removePlayer(Integer playerId) {
        playerIds.remove(playerId);
        updatedAt = System.currentTimeMillis();
    }
}