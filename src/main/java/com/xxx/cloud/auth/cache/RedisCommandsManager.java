package com.xxx.cloud.auth.cache;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Component;

@Component
public class RedisCommandsManager {

    private final RedisClient redisClient;
    private volatile RedisReactiveCommands<String, String> reactiveCommands;
    private volatile RedisCommands<String, String> syncCommands;
    private volatile RedisAsyncCommands<String, String> asyncCommands;

    public RedisCommandsManager(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public RedisReactiveCommands<String, String> reactiveCommands() {
        if (reactiveCommands == null) {
            synchronized (redisClient) {
                if (reactiveCommands == null) {
                    reactiveCommands = redisClient.connect().reactive();
                }
            }
        }
        return  reactiveCommands;
    }

    public RedisCommands<String, String> syncCommands() {
        if (syncCommands == null) {
            synchronized (redisClient) {
                if (syncCommands == null) {
                    syncCommands = redisClient.connect().sync();
                }
            }
        }
        return  syncCommands;
    }

    public RedisAsyncCommands<String, String> asyncCommands() {
        if (asyncCommands == null) {
            synchronized (redisClient) {
                if (asyncCommands == null) {
                    asyncCommands = redisClient.connect().async();
                }
            }
        }
        return  asyncCommands;
    }
}
