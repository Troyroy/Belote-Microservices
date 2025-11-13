package belote.ex;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter loginCounter(MeterRegistry registry) {
        return Counter.builder("users.logged.total")
                .description("Total number of users logged in")
                .tag("microservice", "users-service")
                .register(registry);
    }
}