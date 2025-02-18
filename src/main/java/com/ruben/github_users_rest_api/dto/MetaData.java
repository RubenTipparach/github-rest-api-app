package com.ruben.github_users_rest_api.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MetaData {
    private boolean error;
    private Instant timestamp;

    private boolean isCacheData;
    private boolean cacheExpired;

    private long checksum;
}
