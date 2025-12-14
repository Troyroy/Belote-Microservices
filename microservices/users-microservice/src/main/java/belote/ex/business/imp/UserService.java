package belote.ex.business.imp;

import belote.ex.domain.*;
import belote.ex.persistance.entity.UserEntity;
import belote.ex.persistance.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Keycloak keycloakAdminClient;

    private String realm = "belote-game";

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;

    public UserEntity getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserEntity getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserEntity registerUser(CreateUserRequest request) {
        log.info("=== Starting user registration for username: {} ===", request.getUsername());

        try {
            // Check if user already exists in database
            log.debug("Checking if username exists: {}", request.getUsername());
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                log.warn("Registration failed: Username already exists: {}", request.getUsername());
                throw new RuntimeException("Username already exists");
            }

            log.debug("Checking if email exists: {}", request.getEmail());
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                log.warn("Registration failed: Email already exists: {}", request.getEmail());
                throw new RuntimeException("Email already exists");
            }

            // Create user in Keycloak
            log.info("Creating user in Keycloak for username: {}", request.getUsername());
            String keycloakId = createUserInKeycloak(request);
            log.info("User created in Keycloak successfully. Keycloak ID: {}", keycloakId);

            // Create user in database
            log.info("Saving user to database. Keycloak ID: {}", keycloakId);
            UserEntity user = UserEntity.builder()
                    .keycloakId(keycloakId)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .role("USER")  // ← Added default role
                    .createdAt(LocalDateTime.now())
                    .build();

            UserEntity savedUser = userRepository.save(user);
            log.info("User saved to database successfully. User ID: {}, Username: {}",
                    savedUser.getId(), savedUser.getUsername());

            log.info("=== User registration completed successfully for username: {} ===", request.getUsername());
            return savedUser;

        } catch (Exception e) {
            log.error("=== User registration FAILED for username: {} ===", request.getUsername());
            log.error("Error type: {}", e.getClass().getSimpleName());
            log.error("Error message: {}", e.getMessage());
            log.error("Full stack trace:", e);
            throw new RuntimeException("User registration failed: " + e.getMessage(), e);
        }
    }

    private String createUserInKeycloak(CreateUserRequest request) {
        // Get realm resource
        RealmResource realmResource = keycloakAdminClient.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Create user representation
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(request.getUsername());
        keycloakUser.setEmail(request.getEmail());
        keycloakUser.setFirstName(request.getUsername());
        keycloakUser.setLastName(request.getUsername());
        keycloakUser.setEnabled(true);
        keycloakUser.setEmailVerified(true);  // Set to false if you want email verification

        // Create user
        Response response = usersResource.create(keycloakUser);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }

        // Get user ID from location header
        String locationHeader = response.getHeaderString("Location");
        String keycloakId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        usersResource.get(keycloakId).resetPassword(credential);

        // Assign default role (USER)
        assignRoleToUser(keycloakId, "USER");

        return keycloakId;
    }

    private void assignRoleToUser(String userId, String roleName) {
        RealmResource realmResource = keycloakAdminClient.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Get role
        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

        // Assign role to user
        usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(role));
    }

    public LoginResponse login(LoginRequest request) {
        log.info("=== Starting login for username: {} ===", request.getUsername());

        try {
            // Get token from Keycloak
            log.debug("Requesting token from Keycloak for username: {}", request.getUsername());
            KeycloakTokenResponse tokenResponse = getTokenFromKeycloak(request.getUsername(), request.getPassword());
            log.info("Token received from Keycloak successfully");

            // Get user info from database
            log.debug("Fetching user from database: {}", request.getUsername());
            UserEntity user = userRepository.findByUsername(request.getUsername())
                    .orElseGet(() -> {
                        log.warn("User not found in database, syncing from Keycloak: {}", request.getUsername());
                        return syncUserFromKeycloak(request.getUsername());
                    });

            log.info("User found in database. User ID: {}", user.getId());

            // Update last login time
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.debug("Updated last login time for user: {}", user.getId());

            // Build response
            LoginResponse response = LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenType(tokenResponse.getTokenType())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .refreshExpiresIn(tokenResponse.getRefreshExpiresIn())
                    .userId(Long.valueOf(user.getId()))
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();

            log.info("=== Login completed successfully for username: {} ===", request.getUsername());
            return response;

        } catch (RestClientException e) {
            log.error("Failed to authenticate with Keycloak for username: {}", request.getUsername());
            log.error("Error type: {}", e.getClass().getSimpleName());
            log.error("Error message: {}", e.getMessage());
            throw new RuntimeException("Invalid username or password", e);
        } catch (Exception e) {
            log.error("=== Login FAILED for username: {} ===", request.getUsername());
            log.error("Error type: {}", e.getClass().getSimpleName());
            log.error("Error message: {}", e.getMessage());
            log.error("Full stack trace:", e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get JWT token from Keycloak using username and password
     */
    private KeycloakTokenResponse getTokenFromKeycloak(String username, String password) {
        log.debug("Building token request for Keycloak");

        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        log.debug("Token URL: {}", tokenUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isEmpty()) {
            map.add("client_secret", clientSecret);
        }
        map.add("username", username);
        map.add("password", password);
        map.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        log.debug("Sending token request to Keycloak...");
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                tokenUrl,
                request,
                KeycloakTokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            log.info("Token retrieved successfully from Keycloak");
            return response.getBody();
        } else {
            log.error("Failed to get token from Keycloak. Status: {}", response.getStatusCode());
            throw new RuntimeException("Failed to authenticate with Keycloak");
        }
    }

    /**
     * Sync user from Keycloak to local database (for first-time login)
     */
    private UserEntity syncUserFromKeycloak(String username) {
        log.info("Syncing user from Keycloak: {}", username);

        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Search for user in Keycloak
            List<UserRepresentation> keycloakUsers = usersResource.search(username, true);

            if (keycloakUsers.isEmpty()) {
                log.error("User not found in Keycloak: {}", username);
                throw new RuntimeException("User not found");
            }

            UserRepresentation keycloakUser = keycloakUsers.get(0);
            log.debug("Found user in Keycloak. Keycloak ID: {}", keycloakUser.getId());

            // Create user in database
            UserEntity user = UserEntity.builder()
                    .keycloakId(keycloakUser.getId())
                    .username(keycloakUser.getUsername())
                    .email(keycloakUser.getEmail())
                    .role("USER")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            UserEntity savedUser = userRepository.save(user);
            log.info("User synced from Keycloak to database. User ID: {}", savedUser.getId());

            return savedUser;

        } catch (Exception e) {
            log.error("Failed to sync user from Keycloak: {}", username, e);
            throw new RuntimeException("Failed to sync user from Keycloak", e);
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public LoginResponse refreshToken(String refreshToken) {
        log.info("=== Refreshing access token ===");

        try {
            String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", clientId);
            if (clientSecret != null && !clientSecret.isEmpty()) {
                map.add("client_secret", clientSecret);
            }
            map.add("refresh_token", refreshToken);
            map.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    request,
                    KeycloakTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                KeycloakTokenResponse tokenResponse = response.getBody();

                LoginResponse loginResponse = LoginResponse.builder()
                        .accessToken(tokenResponse.getAccessToken())
                        .refreshToken(tokenResponse.getRefreshToken())
                        .tokenType(tokenResponse.getTokenType())
                        .expiresIn(tokenResponse.getExpiresIn())
                        .refreshExpiresIn(tokenResponse.getRefreshExpiresIn())
                        .build();

                log.info("=== Token refreshed successfully ===");
                return loginResponse;
            } else {
                log.error("Failed to refresh token. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to refresh token");
            }

        } catch (Exception e) {
            log.error("=== Token refresh FAILED ===");
            log.error("Error: {}", e.getMessage(), e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage(), e);
        }
    }
}
