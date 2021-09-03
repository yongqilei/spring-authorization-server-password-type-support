//package com.xxx.cloud.auth.cache;
//
//import io.lettuce.core.cluster.RedisClusterClient;
//import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ClusterReactiveCommandsManager {
//
//    private final RedisClusterClient redisClusterClient;
//    private volatile RedisAdvancedClusterReactiveCommands<String, String> reactiveCommands;
//
//    public ClusterReactiveCommandsManager(RedisClusterClient redisClusterClient) {
//        this.redisClusterClient = redisClusterClient;
//    }
//
//    public RedisAdvancedClusterReactiveCommands<String, String> reactiveCommands() {
//        if (reactiveCommands == null) {
//            synchronized (redisClusterClient) {
//                if (reactiveCommands == null) {
//                    reactiveCommands = redisClusterClient.connect().reactive();
//                }
//            }
//        }
//        return reactiveCommands;
//    }
//}
