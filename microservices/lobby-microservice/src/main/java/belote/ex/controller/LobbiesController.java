package belote.ex.controller;

import Utils.MetricsMaker;
import belote.ex.business.imp.LobbyService;
import belote.ex.domain.CreateLobbyRequest;
import belote.ex.domain.JoinLobbyRequest;
import belote.ex.persistance.entity.LobbyEntity;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lobbies")
@RequiredArgsConstructor
@Slf4j
public class LobbiesController {
    private final LobbyService lobbyService;

    private final MeterRegistry meterRegistry;

    @PostMapping
    public ResponseEntity<LobbyCreationResponse> createLobby(
            @Valid @RequestBody CreateLobbyRequest request) {

        MetricsMaker.MetricsCounter("lobby_count",meterRegistry);
        String lobbyId = lobbyService.createLobby(
                request.getLobbyName(),
                request.getHostId(),
                request.getGameMode()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LobbyCreationResponse(lobbyId, "Lobby created"));
    }

    @GetMapping("/{lobbyId}")
    public ResponseEntity<LobbyEntity> getLobby(@PathVariable String lobbyId) {
        LobbyEntity lobby = lobbyService.getLobby(lobbyId);

        if (lobby == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(lobby);
    }

    @PostMapping("/{lobbyId}/join")
    public ResponseEntity<String> joinLobby(
            @PathVariable String lobbyId,
            @Valid @RequestBody JoinLobbyRequest request) {

        boolean success = lobbyService.joinLobby(lobbyId, request.getPlayerId());

        if (!success) {
            return ResponseEntity.badRequest().body("Cannot join lobby");
        }

        return ResponseEntity.ok("Joined lobby successfully");
    }

    @PostMapping("/{lobbyId}/leave")
    public ResponseEntity<String> leaveLobby(
            @PathVariable String lobbyId,
            @RequestParam Integer playerId) {

        lobbyService.leaveLobby(lobbyId, playerId);
        return ResponseEntity.ok("Left lobby successfully");
    }

    @PostMapping("/{lobbyId}/start")
    public ResponseEntity<String> startGame(@PathVariable String lobbyId) {
        try {
            lobbyService.startGame(lobbyId);
            return ResponseEntity.ok("Game is starting...");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<LobbyEntity>> getActiveLobbies() {
        return ResponseEntity.ok(lobbyService.getActiveLobbies());
    }

    record LobbyCreationResponse(String lobbyId, String message) {}
}
