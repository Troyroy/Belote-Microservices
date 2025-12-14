package belote.ex.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyCloakAdminConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }
}