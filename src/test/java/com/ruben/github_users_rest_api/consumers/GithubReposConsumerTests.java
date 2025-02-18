package com.ruben.github_users_rest_api.consumers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruben.github_users_rest_api.dto.GithubRepoDto;
import com.ruben.github_users_rest_api.exceptions.RateLimitException;
import com.ruben.github_users_rest_api.services.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GithubReposConsumerTests {

    GithubReposConsumer consumer;

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

        var octoRepo = new GithubRepoDto();
        octoRepo.setName("octocat repo");
        var repos = new GithubRepoDto[]{
                octoRepo
        };
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GithubRepoDto[].class))
                .thenReturn(Mono.just(repos));

        CacheService cacheService = new CacheService(1, maxCacheSize);
        consumer = new GithubReposConsumer(webClient, new ObjectMapper(), cacheService);
    }

    // happy path
    @Test
    public void testGettingRepoData(){

        var response = consumer.getRepos(username);
        assert response != null;

        assert response.getRepos().length > 0;
        assert response.getRepos()[0].getName().equals("octocat repo");
    }

    // Test caching mechanism
    @Test
    public void testFailedToGetFreshData(){
        // build up cache
        consumer.getRepos(username);

        // modfify some response
        when(responseSpec.bodyToMono(GithubRepoDto[].class))
                .thenReturn(Mono.error(new RateLimitException()));
        var response = consumer.getRepos(username);

        assert response.getRepos().length > 0;
        var metaData = response.getMetaData();
        assert metaData != null;
        assert metaData.isCacheData();
    }

    @Test
    public void testFailedToCacheData(){
        when(responseSpec.bodyToMono(GithubRepoDto[].class))
                .thenReturn(Mono.error(new RateLimitException()));
        var response = consumer.getRepos(username);

        assert response.getRepos() == null;
        var metaData = response.getMetaData();
        assert metaData != null;
        assert metaData.isError();
    }
}
