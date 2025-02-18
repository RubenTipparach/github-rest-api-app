package com.ruben.github_users_rest_api.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruben.github_users_rest_api.dto.GithubReposReplyDto;
import com.ruben.github_users_rest_api.dto.MetaData;
import com.ruben.github_users_rest_api.dto.GithubRepoDto;
import com.ruben.github_users_rest_api.AppConfiguration;
import com.ruben.github_users_rest_api.exceptions.RateLimitException;
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
import java.util.Objects;

/**
 * This consumer handles the request for getting repos for a certain user.
 * It handles messaging via Rabbit MQ.
 */
@Service
@Profile("service")
public class GithubReposConsumer {
    private final Logger logger = LoggerFactory.getLogger(GithubReposConsumer.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final HashMap<String, GithubReposReplyDto> reposCache;
    private final CacheService cacheService;

    public GithubReposConsumer(WebClient gitHubWebClient,
                               ObjectMapper objectMapper,
                               CacheService cacheService) {
        this.objectMapper = objectMapper;

        this.webClient = gitHubWebClient;
        this.reposCache = new HashMap<>();
        this.cacheService = cacheService;
    }


    // Consumer for get repositories request
    @RabbitListener(queues = AppConfiguration.GET_REPOS_QUEUE)
    @SendTo(AppConfiguration.GET_REPOS_QUEUE)
    public GithubReposReplyDto getRepos(String username) {
        val reposDto = new GithubReposReplyDto();
        val metaData = new MetaData();
        reposDto.setMetaData(metaData);

        try {
            getRepoMono(username)
                    .onErrorResume(RateLimitException.class,
                            ex -> Mono.just(handleRateLimit(username, reposDto, metaData)))
                    .doOnSuccess(repos -> {
                        String data = null;
                        try {
                            data = objectMapper.writeValueAsString(repos);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        val checksum = Checksum.getCRC32Checksum(data.getBytes());
                        reposDto.setRepos(repos);

                        cacheService.getFromCache(
                                username,
                                reposCache,
                                reposDto,
                                checksum,
                                metaData
                        );

                        metaData.setCacheData(false);

                    }).block();

            return reposDto;
        } catch (RateLimitException e) {
            handleRateLimit(username, reposDto, metaData);
            return reposDto;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            metaData.setError(true);
            return reposDto;
        }
    }

    // The web client for making the http request
    private Mono<GithubRepoDto[]> getRepoMono(String username) throws RateLimitException {
        return webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(status ->
                                status == HttpStatus.FORBIDDEN
                                        || status == HttpStatus.TOO_MANY_REQUESTS,
                        response -> {
                            logger.warn("hit rate limit");
                            return Mono.error(new RateLimitException());
                        })
                .bodyToMono(GithubRepoDto[].class);
    }

    // Sets the meta data and grabs cache data as backup if exists.
    private GithubRepoDto[] handleRateLimit(String username, GithubReposReplyDto reposDto, MetaData metaData) {
        logger.warn("Hit rate limit for GitHub.");
        if (reposCache.containsKey(username)) {
            reposDto.setRepos(reposCache.get(username).getRepos());
            reposDto.setMetaData(reposCache.get(username).getMetaData());
            reposDto.getMetaData().setCacheData(true);
            return reposDto.getRepos();
        } else {
            logger.error("No cache was found.", new RateLimitException());
            metaData.setError(true);
            return null;
        }
    }
}
