package belote.ex.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class PublicSecurityConfig {

    @Bean
    @Order(1) // Process public endpoints first
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        ("/users/login"),
                        ("/users/register"),
                        ("/actuator/**")
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}