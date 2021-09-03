package com.xxx.cloud.auth.cache;

import java.time.Duration;
import java.time.Instant;

public interface CloudCacheManager {

    boolean setCache(String key, Object value, Duration timeout);

    boolean setCache(String key, Object value, Instant expiresAt);

    Object getCache(String key);

    boolean removeCache(String key);

}
