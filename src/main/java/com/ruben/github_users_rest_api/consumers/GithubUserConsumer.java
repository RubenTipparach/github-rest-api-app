package com.ruben.github_users_rest_api.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruben.github_users_rest_api.AppConfiguration;
import com.ruben.github_users_rest_api.dto.GithubRepoDto;
import com.ruben.github_users_rest_api.dto.GithubUserDto;
import com.ruben.github_users_rest_api.exceptions.RateLimitException;
import com.ruben.github_users_rest_api.dto.GithubUserReplyDto;
import com.ruben.github_users_rest_api.dto.MetaData;
import com.ruben.github_users_rest_api.services.CacheService;
import com.ruben.github_users_rest_api.utilities.Checksum;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * This consumer handles the webclient call to github. If it has a rate limit status code returned,
 * it falls back on cache data.
 */
@Service
@Profile("service")
public class GithubUserConsumer {
    private final Logger logger = LoggerFactory.getLogger(GithubUserConsumer.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final HashMap<String, GithubUserReplyDto> userCache;
    private final CacheService cacheService;

    public GithubUserConsumer(WebClient gitHubWebClient,
                              ObjectMapper objectMapper,
                              CacheService cacheService) {
        this.objectMapper = objectMapper;

        this.webClient = gitHubWebClient;
        this.userCache = new HashMap<>();
        this.cacheService = cacheService;
    }

    @RabbitListener(queues = AppConfiguration.GET_USER_QUEUE)
    @SendTo(AppConfiguration.GET_USER_QUEUE)
    public GithubUserReplyDto getUser(String username) {
        val userDto = new GithubUserReplyDto();
        val metaData = new MetaData();
        userDto.setMetaData(metaData);
        try {
            getUserMono(username).onErrorResume(RateLimitException.class,
                            ex -> Mono.just(handleRateLimit(username, userDto, metaData)))
                    .doOnSuccess(user -> {
                        String useValue = null;
                        try {
                            useValue = objectMapper.writeValueAsString(user);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        val checksum = Checksum.getCRC32Checksum(useValue.getBytes());
                        userDto.setReply(user);

                        cacheService.getFromCache(
                                username,
                                userCache,
                                userDto,
                                checksum,
                                metaData
                        );

                        metaData.setCacheData(false);
                    }).block();
            return userDto;

        } catch (RateLimitException e) {
            handleRateLimit(username, userDto, metaData);
            return userDto;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            metaData.setError(true);
            return userDto;
        }

    }

    public Mono<GithubUserDto> getUserMono(String username) throws RateLimitException {
        return webClient.get()
                .uri("/users/{username}", username)
                .retrieve()
                .onStatus(status ->
                                status == HttpStatus.FORBIDDEN
                                        || status == HttpStatus.TOO_MANY_REQUESTS,
                        response -> {
                            logger.warn("hit rate limit");
                            return Mono.error(new RateLimitException());
                        })
                .bodyToMono(GithubUserDto.class);
    }

    private GithubUserDto handleRateLimit(String username, GithubUserReplyDto userDto, MetaData metaData) {
        logger.warn("Hit rate limit for GitHub.");
        if (userCache.containsKey(username)) {
            userDto.setReply(userCache.get(username).getReply());
            userDto.setMetaData(userCache.get(username).getMetaData());
            userDto.getMetaData().setCacheData(true);
            return userDto.getReply();
        } else {
            logger.error("No cache was found.", new RateLimitException());
            metaData.setError(true);
            return null;
        }
    }
}
