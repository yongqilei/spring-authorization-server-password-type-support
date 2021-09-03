package com.xxx.cloud.auth.config;

import com.xxx.cloud.auth.exception.SystemException;
import io.lettuce.core.*;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.DecodeBufferPolicies;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
public class RedisCacheConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheConfiguration.class);
    public static final String ADAPTIVE_REFRESH_TRIGGER_TIMEOUT = "redis.adaptiveRefreshTriggersTimeout";
    public static final String RECONNECT_ATTEMPT = "redis.refreshTriggersReconnectAttempts";
    public static final String RECONNECT_DELAY = "redis.reconnect.delay";

    private static final int DEFAULT_ADAPTIVE_REFRESH_TRIGGER_TIMEOUT = 500;
    private static final int DEFAULT_RECONNECT_ATTEMPT = 2;

    private static final String INVALID_CLUSTER_CONFIGURATION =
            "There is no configuration for Redis Cluster, please add one for it.";

//    @Bean
//    public RedisClusterClient redisClusterClient(RedisConnectionFactory factory, Environment env) {
//        LettuceClientConfiguration lettuceClientConfiguration =
//                ((LettuceConnectionFactory) factory).getClientConfiguration();
//        Duration timeout = lettuceClientConfiguration.getCommandTimeout();
//        List<RedisURI> initUris = new ArrayList<>();
//        RedisClusterConfiguration clusterConfiguration =
//                ((LettuceConnectionFactory) factory).getClusterConfiguration();
//        if (Objects.isNull(clusterConfiguration)) {
//            LOGGER.error(INVALID_CLUSTER_CONFIGURATION);
//            throw SystemException.create("Cluster configuration for Redis is required!");
//        }
//        clusterConfiguration.getClusterNodes().forEach(node -> initUris.add(generateUri(node, timeout)));
//
//        RedisClusterClient clusterClient = RedisClusterClient.create(buildClientResources(env), initUris);
//        clusterClient.setOptions(ClusterClientOptions.builder()
//                        .topologyRefreshOptions(getClusterTopologyRefreshOptions(env))
//                        .autoReconnect(true)
//                        .protocolVersion(ProtocolVersion.newestSupported())
//                .build());
//        return clusterClient;
//    }

    @Bean
    public RedisClient redisClient(RedisConnectionFactory redisConnectionFactory, Environment env) {
        LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactory;
        LettuceClientConfiguration lettuceClientConfiguration = lettuceConnectionFactory.getClientConfiguration();
        Duration timeout = lettuceClientConfiguration.getCommandTimeout();
        RedisClient client = RedisClient.create(buildClientResources(env), buildRedisURI(lettuceConnectionFactory, timeout));
        client.setOptions(ClientOptions.builder()
                        .autoReconnect(true)
                .build());
        return client;
    }

    private RedisURI buildRedisURI(LettuceConnectionFactory lettuceConnectionFactory, Duration timeout) {
        RedisURI redisURI = RedisURI.create(lettuceConnectionFactory.getHostName(), lettuceConnectionFactory.getPort());
        redisURI.setTimeout(timeout);
        return redisURI;
    }


    private ClusterTopologyRefreshOptions getClusterTopologyRefreshOptions(Environment env) {
        ClusterTopologyRefreshOptions.Builder builder = ClusterTopologyRefreshOptions.builder()
                .enableAllAdaptiveRefreshTriggers()
                .adaptiveRefreshTriggersTimeout(Duration.ofMillis(DEFAULT_ADAPTIVE_REFRESH_TRIGGER_TIMEOUT))
                .refreshTriggersReconnectAttempts(DEFAULT_RECONNECT_ATTEMPT);

        String adaptiveRefreshTriggersTimeout = env.getProperty(ADAPTIVE_REFRESH_TRIGGER_TIMEOUT);
        if (StringUtils.isNotBlank(adaptiveRefreshTriggersTimeout)) {
            ApplicationConversionService applicationConversionService = new ApplicationConversionService();
            try {
                Duration duration = applicationConversionService.convert(adaptiveRefreshTriggersTimeout,
                        Duration.class);
                builder.adaptiveRefreshTriggersTimeout(duration);
            } catch (ConversionFailedException exception) {
                LOGGER.error(
                        "Invalid data for 'redis.adaptiveRefreshTriggersTimeout', starting with the default value: {}",
                        DEFAULT_ADAPTIVE_REFRESH_TRIGGER_TIMEOUT, exception);
            }
        }

        String refreshTriggersReconnectAttempts = env.getProperty(RECONNECT_ATTEMPT);
        if (StringUtils.isNotBlank(refreshTriggersReconnectAttempts)) {
            try {
                builder.refreshTriggersReconnectAttempts(Integer.parseInt(refreshTriggersReconnectAttempts));
            } catch (NumberFormatException exception) {
                LOGGER.error(
                        "Invalid data for 'redis.refreshTriggersReconnectAttempts', starting with the default value: {}",
                        DEFAULT_RECONNECT_ATTEMPT, exception);
            }
        }

        return builder.build();
    }

    private ClientResources buildClientResources(Environment env) {
        ClientResources.Builder builder = ClientResources.builder();
        String reconnectDelay = env.getProperty("redis.reconnect.delay");
        List<Duration> reconnectDelayList;
        try {
            reconnectDelayList = ReconnectDelay.getReconnectDelay(reconnectDelay);
        } catch (Exception e) {
            LOGGER.error("Invalid data for 'redis.reconnect.delay', starting with the default value: {}",
                    ReconnectDelay.DEFAULT_DURATION_LIST_STR);
            reconnectDelayList = getDefaultReconnectDelayList();
        }
        if (!CollectionUtils.isEmpty(reconnectDelayList)) {
            builder.reconnectDelay(new ReconnectDelay(reconnectDelayList));
        }
        return builder.build();
    }

    private List<Duration> getDefaultReconnectDelayList() {
        return ReconnectDelay.getReconnectDelay(ReconnectDelay.DEFAULT_DURATION_LIST_STR);
    }

    private RedisURI generateUri(RedisNode node, Duration timeout) {
        RedisURI redisURI = RedisURI.create(node.getHost(), node.getPort());
        redisURI.setTimeout(timeout);
        return redisURI;
    }
}
