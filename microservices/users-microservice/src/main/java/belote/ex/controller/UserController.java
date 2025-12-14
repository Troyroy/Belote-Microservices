package belote.ex.controller;

import Utils.MetricsMaker;
import belote.ex.business.imp.UserService;
import belote.ex.domain.CreateUserRequest;
import belote.ex.domain.LoginRequest;
import belote.ex.domain.LoginResponse;
import belote.ex.persistance.entity.UserEntity;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.jose.jwk.JWK;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users") //temp
@AllArgsConstructor
@Slf4j
public class UserController {

    private UserService userService;
    private final MeterRegistry meterRegistry;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateUserRequest request) {
        try {

            UserEntity user = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "User already exists"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        try {
            LoginResponse response = userService.login(request);
            MetricsMaker.MetricsCounter("user_count",meterRegistry);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for username: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Test endpoint works!");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        log.info("Token refresh request received");
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Refresh token is required"));
            }

            LoginResponse response = userService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        log.info("Get current user request received");
        try {
            String keycloakId = jwt.getSubject();
            UserEntity user = userService.getUserByKeycloakId(keycloakId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to get current user", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        log.info("Get user by ID request received: {}", id);
        try {
            UserEntity user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to get user by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
    }
}