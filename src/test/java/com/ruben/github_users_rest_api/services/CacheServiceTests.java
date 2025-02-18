package com.ruben.github_users_rest_api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruben.github_users_rest_api.dto.GithubUserDto;
import com.ruben.github_users_rest_api.dto.GithubUserReplyDto;
import com.ruben.github_users_rest_api.dto.MetaData;
import com.ruben.github_users_rest_api.utilities.Checksum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Objects;

public class CacheServiceTests {
    CacheService cacheService;
    String username = "octocat";
    ObjectMapper objectMapper = new ObjectMapper();
    int maxCacheSize = 100;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService(1, 100);
    }

    @Test
    void testPutCacheData(){
        // cache is empty
        var cache = new HashMap<String, GithubUserReplyDto>();
        var user = new GithubUserDto();

        var userReply = new GithubUserReplyDto();
        var metaData = new MetaData();

        userReply.setMetaData(metaData);
        userReply.setReply(user);
        user.setName("octocat guy");

        String data = null;
        try {
            data = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var checksum = Checksum.getCRC32Checksum(data.getBytes());
        cacheService.getFromCache(username, cache, userReply, checksum, metaData);

        assert Objects.equals(cache.get(username).getReply().getName(), "octocat guy");
        assert !userReply.getMetaData().isCacheData();
        // there should be stuff in the cache
        assert cache.containsKey(username);
        assert cache.get(username).getReply().getName().equals("octocat guy");
    }

    @Test
    void testGetCacheData(){
        var cache = new HashMap<String, GithubUserReplyDto>();
        var user = new GithubUserDto();

        var userReply = new GithubUserReplyDto();
        var metaData = new MetaData();

        userReply.setMetaData(metaData);
        userReply.setReply(user);
        user.setName("octocat guy");
        // precache data
        cache.put(username, userReply);
        String data = null;
        try {
            data = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var checksum = Checksum.getCRC32Checksum(data.getBytes());
        cacheService.getFromCache(username, cache, userReply, checksum, metaData);

        assert Objects.equals(userReply.getReply().getName(), "octocat guy");
        assert !userReply.getMetaData().isCacheData();
    }

    @Test
    void testUpdateCacheData(){
        var cache = new HashMap<String, GithubUserReplyDto>();
        var user = new GithubUserDto();

        var userReply = new GithubUserReplyDto();
        var metaData = new MetaData();

        userReply.setMetaData(metaData);
        userReply.setReply(user);
        user.setName("octocat guy");

        cache.put(username, userReply);

        // set new user data invalidating cache!
        var newUser = new GithubUserDto();
        newUser.setName("super cat guy");
        var newUserReply = new GithubUserReplyDto();
        newUserReply.setMetaData(metaData);
        newUserReply.setReply(newUser);

        String data = null;
        try {
            data = objectMapper.writeValueAsString(newUser);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var checksum = Checksum.getCRC32Checksum(data.getBytes());
        cacheService.getFromCache(username, cache, newUserReply, checksum, metaData);

        assert Objects.equals(cache.get(username).getReply().getName(), "super cat guy");
        assert !userReply.getMetaData().isCacheData();
    }

    @Test
    void testMaxCacheSize(){
        int maxCacheSize = 10;
        cacheService = new CacheService(1, 10);

        var cache = new HashMap<String, GithubUserReplyDto>();
        var user = new GithubUserDto();

        var userReply = new GithubUserReplyDto();
        var metaData = new MetaData();

        userReply.setMetaData(metaData);
        userReply.setReply(user);
        user.setName("octocat guy");

        for(int i = 0; i < maxCacheSize; i++) {
            // precache data
            cache.put(username + i, userReply);
        }
        String data = null;
        try {
            data = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var checksum = Checksum.getCRC32Checksum(data.getBytes());
        cacheService.getFromCache(username, cache, userReply, checksum, metaData);
        assert cache.size() == maxCacheSize + 1;
        cache.put(username + (maxCacheSize + 1), userReply);
        cache.put(username + (maxCacheSize + 2), userReply);

        cacheService.getFromCache(username, cache, userReply, checksum, metaData);
        assert cache.size() == 1;

        assert Objects.equals(userReply.getReply().getName(), "octocat guy");
        assert !userReply.getMetaData().isCacheData();
    }
}
