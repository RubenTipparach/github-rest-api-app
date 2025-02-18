package com.ruben.github_users_rest_api.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruben.github_users_rest_api.dto.GithubUserDto;
import com.ruben.github_users_rest_api.exceptions.RateLimitException;
import com.ruben.github_users_rest_api.services.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GithubUserConsumerTests {

    GithubUserConsumer consumer;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    String username = "octocat";
    int maxCacheSize = 100;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        var octoUser = new GithubUserDto();
        octoUser.setName("octocat user");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}", username)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GithubUserDto.class))
                .thenReturn(Mono.just(octoUser));

        CacheService cacheService = new CacheService(1, maxCacheSize);
        consumer = new GithubUserConsumer(webClient, new ObjectMapper(), cacheService);
    }

    // happy path
    @Test
    public void testGettingRepoData(){

        var response = consumer.getUser(username);
        assert response != null;

        assert response.getReply() != null;
        assert response.getReply().getName().equals("octocat user");
    }

    // Test caching mechanism
    @Test
    public void testFailedToGetFreshData(){
        // build up cache
        consumer.getUser(username);

        // modfify some response
        when(responseSpec.bodyToMono(GithubUserDto.class))
                .thenReturn(Mono.error(new RateLimitException()));
        var response = consumer.getUser(username);

        assert response.getReply() != null;
        var metaData = response.getMetaData();
        assert metaData != null;
        assert metaData.isCacheData();
    }


    @Test
    public void testFailedToCacheData(){
        when(responseSpec.bodyToMono(GithubUserDto.class))
                .thenReturn(Mono.error(new RateLimitException()));
        var response = consumer.getUser(username);

        assert response.getReply() == null;
        var metaData = response.getMetaData();
        assert metaData != null;
        assert metaData.isError();
    }
}
