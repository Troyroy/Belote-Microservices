package belote.ex.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameRabbitMQConfig {

    public static final String GAME_EXCHANGE = "game.exchange";
    public static final String START_GAME_QUEUE = "start.game.queue";
    public static final String GAME_RESULT_QUEUE = "game.result.queue";
    public static final String START_GAME_ROUTING_KEY = "game.start";
    public static final String GAME_RESULT_ROUTING_KEY = "game.result";

    @Bean
    public TopicExchange gameExchange() {
        return new TopicExchange(GAME_EXCHANGE);
    }

    @Bean
    public Queue startGameQueue() {
        return new Queue(START_GAME_QUEUE, true);
    }

    @Bean
    public Queue gameResultQueue() {
        return new Queue(GAME_RESULT_QUEUE, true);
    }

    @Bean
    public Binding startGameBinding() {
        return BindingBuilder
                .bind(startGameQueue())
                .to(gameExchange())
                .with(START_GAME_ROUTING_KEY);
    }

    @Bean
    public Binding gameResultBinding() {
        return BindingBuilder
                .bind(gameResultQueue())
                .to(gameExchange())
                .with(GAME_RESULT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}