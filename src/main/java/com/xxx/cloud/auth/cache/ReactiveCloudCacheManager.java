package com.xxx.cloud.auth.cache;

import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

@Service
public final class ReactiveCloudCacheManager extends AbstractCloudCacheManager {

    private final RedisReactiveCommands<String, String> reactiveCommands;
    private final RedisAsyncCommands<String, String> asyncCommands;

    public ReactiveCloudCacheManager(RedisCommandsManager redisCommandsManager) {
        super(redisCommandsManager);
        this.reactiveCommands = redisCommandsManager.reactiveCommands();
        this.asyncCommands = redisCommandsManager.asyncCommands();
    }

    @Override
    protected CompletionStage<Boolean> setCacheAsync(String key, Object value, Duration timeout) {
        return null;
    }

    @Override
    protected CompletionStage<Boolean> setCacheAsync(String key, Object value, Instant expiresAt) {
        return null;
    }

    @Override
    protected Mono<Boolean> setCacheRx(String key, Object value, Duration timeout) {
        return null;
    }

    @Override
    protected Mono<Boolean> setCacheRx(String key, Object value, Instant expiresAt) {
        return null;
    }
}
