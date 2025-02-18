package com.ruben.github_users_rest_api.services;

import com.ruben.github_users_rest_api.dto.MetaData;
import com.ruben.github_users_rest_api.utilities.TimestampCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;

/**
 * This service manages the caching data stored in memory using hashmaps.
 * A checksum is provided from the response data to validate if the data cache has changed.
 * Normally we won't use the cache unless github has rate limited us.
 */
@Service
public class CacheService {
    private final int cacheExpirationInMinutes; // not currently used but could be useful.
    private final int maxCacheSize;

    public CacheService(@Value("${app.cache.timeout}") int cacheTimeout, @Value("${app.cache.maxCache}")int maxCacheSize) {
        this.cacheExpirationInMinutes = cacheTimeout;
        this.maxCacheSize = maxCacheSize;
    }

    public <T> T getFromCache(String key, HashMap<String, T> cache, T newValue, long checksum, MetaData metaData) {
        if(cache.size() > maxCacheSize) {
            cache.clear();
        }

        T cached = cache.get(key);

        if (cached != null) {

            if (metaData.getChecksum() != checksum) {
                metaData.setChecksum(checksum);
                metaData.setTimestamp(Instant.now());
                cache.replace(key, newValue);
            }
        } else {
            metaData.setChecksum(checksum);
            metaData.setTimestamp(Instant.now());
            cache.put(key, newValue);
        }

        return cached != null ? cached : newValue;
    }
}