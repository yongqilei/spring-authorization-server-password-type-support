package com.xxx.cloud.auth.service;

import com.xxx.cloud.auth.cache.AbstractCloudCacheManager;
import com.xxx.cloud.auth.cache.ReactiveCloudCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

import static com.xxx.cloud.auth.models.constant.AuthorizationServerConstant.*;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.USERNAME;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.EXP;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.JTI;

@Component
public class CustomOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomOAuth2AuthorizationService.class);

    private final ReactiveCloudCacheManager cloudCacheManager;
    private final JwtDecoder jwtDecoder;

    public CustomOAuth2AuthorizationService(ReactiveCloudCacheManager cloudCacheManager, JwtDecoder jwtDecoder) {
        this.cloudCacheManager = cloudCacheManager;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Save {@link OAuth2Authorization} for reuse purpose.
     * We can reuse the oauth2 info even if user login multiple times.
     * <b>cacheKey</b>: oauth2:authorization:{client_id}:{username}
     * @param authorization {@link OAuth2Authorization}
     */
    @Override
    public void save(OAuth2Authorization authorization) {
        String clientId = authorization.getId();
        String username = authorization.getPrincipalName();
        Instant expiresAt = authorization.getAttribute(EXP);

        boolean res = cloudCacheManager.setCache(PREFIX_AUTHORIZATION + clientId + COLON + username,
                authorization, expiresAt);
        if (!res) {
            LOGGER.debug("OAuth2Authorization saved failed...");
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        String username = authorization.getPrincipalName();
        String clientId = authorization.getRegisteredClientId();
        cloudCacheManager.removeCache(PREFIX_AUTHORIZATION + clientId + COLON + username);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        throw new UnsupportedOperationException("Find by ID is not supported...");
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Jwt jwt = jwtDecoder.decode(token);
        String username = jwt.getSubject();
        String client = jwt.getAudience().get(0);
        return (OAuth2Authorization) cloudCacheManager.getCache(PREFIX_AUTHORIZATION + client + COLON + username);
    }
}
