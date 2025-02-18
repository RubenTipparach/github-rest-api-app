package com.ruben.github_users_rest_api;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This is the configuration class. It defines the queues and reply routing.
 * In addition, it also defines the open api spec, jackson serializer and the webclient for calling github.
 */
@Configuration
@EnableAutoConfiguration
public class AppConfiguration {

    public static final String GET_USER_QUEUE = "get.user.queue";
    public static final String GET_REPOS_QUEUE = "get.repos.queue";
    public static final String GET_USER_REPOS_QUEUE = "get.user-repos.queue";

    public static final String ROUTING_KEY_USER = "request.user";
    public static final String ROUTING_KEY_REPOS = "request.repos.routing";
    public static final String ROUTING_KEY_USER_REPOS = "request.user-repos.routing";

    public static final String EXCHANGE = "message.exchange";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("My API")
                        .version("1.0")
                        .description("API documentation for the GitHub Rest API app"));
    }

    @Bean
    public Queue getUserQueue() {
        return new Queue(GET_USER_QUEUE, false);
    }

    @Bean
    public Queue getReposQueue() {
        return new Queue(GET_REPOS_QUEUE, false);
    }

    @Bean
    public Queue getUserReposQueue() {
        return new Queue(GET_USER_REPOS_QUEUE, false);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingUserReposQueue(Queue getUserQueue, DirectExchange exchange) {
        return BindingBuilder.bind(getUserQueue).to(exchange).with(ROUTING_KEY_USER_REPOS);
    }

    @Bean
    public Binding bindingUserQueue(Queue getUserQueue, DirectExchange exchange) {
        return BindingBuilder.bind(getUserQueue).to(exchange).with(ROUTING_KEY_USER);
    }

    @Bean
    public Binding bindingReposQueue(Queue getReposQueue, DirectExchange exchange) {
        return BindingBuilder.bind(getReposQueue).to(exchange).with(ROUTING_KEY_REPOS);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Use ISO-8601 format
        return mapper;
    }

    @Bean
    WebClient gitHubWebClient(@Value("${app.github.api-url}") String apiUrl) {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }
}