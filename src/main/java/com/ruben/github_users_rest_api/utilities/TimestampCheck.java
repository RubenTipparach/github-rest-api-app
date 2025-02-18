package com.ruben.github_users_rest_api.utilities;

import java.time.Duration;
import java.time.Instant;

/**
 * This utility checks if the two timestamps are greater duration.
 */
public class TimestampCheck {

    public static boolean HasTimeExpired(Instant lastTimestamp, Instant currentTimestamp, int timeLimitMinutes) {

        Duration duration = Duration.between(lastTimestamp, currentTimestamp);

        return duration.toMinutes() >= timeLimitMinutes;
    }
}
