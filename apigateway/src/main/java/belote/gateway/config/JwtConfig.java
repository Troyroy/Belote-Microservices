package belote.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import java.util.Arrays;
import java.util.List;

@Configuration
public class JwtConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();

        // Configure validators to accept multiple issuers
        OAuth2TokenValidator<Jwt> validators = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                multiIssuerValidator()
        );

        jwtDecoder.setJwtValidator(validators);
        return jwtDecoder;
    }

    /**
     * Allow both internal (Docker) and external (localhost) issuer URLs
     */
    private OAuth2TokenValidator<Jwt> multiIssuerValidator() {
        List<String> validIssuers = Arrays.asList(
                "http://keycloak:8080/realms/belote-game",      // Docker internal
                "http://localhost:8180/realms/belote-game"       // External
        );

        return new OAuth2TokenValidator<Jwt>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt jwt) {
                String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;

                if (issuer != null && validIssuers.contains(issuer)) {
                    return OAuth2TokenValidatorResult.success();
                }

                OAuth2Error error = new OAuth2Error(
                        "invalid_token",
                        "The iss claim is not valid. Expected one of: " + validIssuers + ", but got: " + issuer,
                        null
                );
                return OAuth2TokenValidatorResult.failure(error);
            }
        };
    }
}