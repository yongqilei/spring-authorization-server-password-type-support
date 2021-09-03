package com.xxx.cloud.auth.cache;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

import static com.xxx.cloud.auth.models.constant.AuthorizationServerConstant.REDIS_SET_RESULT_OK;


@Service
public abstract class AbstractCloudCacheManager implements CloudCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCloudCacheManager.class);

    protected final Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer;
    protected final RedisCommandsManager redisCommandsManager;
    protected final RedisCommands<String, String> syncCommands;

    public AbstractCloudCacheManager(RedisCommandsManager redisCommandsManager) {
        this.redisCommandsManager = redisCommandsManager;
        this.jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        this.syncCommands = redisCommandsManager.syncCommands();
    }

    @Override
    public boolean setCache(String key, Object value, Duration timeout) {
        SetArgs setArgs = SetArgs.Builder.px(timeout);
        String data = serializeDefault(value);
        return REDIS_SET_RESULT_OK.equalsIgnoreCase(syncCommands.set(key, data, setArgs));
    }

    @Override
    public boolean setCache(String key, Object value, Instant expiresAt) {
        SetArgs setArgs = SetArgs.Builder.pxAt(expiresAt);
        String data = serializeDefault(value);
        return REDIS_SET_RESULT_OK.equalsIgnoreCase(syncCommands.set(key, data, setArgs));
    }

    @Override
    public Object getCache(String key) {
        return jackson2JsonRedisSerializer.deserialize(syncCommands.get(key).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean removeCache(String key) {
        String value = syncCommands.getdel(key);
        LOGGER.debug("Removed cache, Key[{}], Value[{}]", key, value);
        return true;
    }

    protected String serializeDefault(Object val) {
        return new String(jackson2JsonRedisSerializer.serialize(val));
    }

    protected abstract CompletionStage<Boolean> setCacheAsync(String key, Object value, Duration timeout);

    protected abstract CompletionStage<Boolean> setCacheAsync(String key, Object value, Instant expiresAt);

    protected abstract Mono<Boolean> setCacheRx(String key, Object value, Duration timeout);

    protected abstract Mono<Boolean> setCacheRx(String key, Object value, Instant expiresAt);
}
