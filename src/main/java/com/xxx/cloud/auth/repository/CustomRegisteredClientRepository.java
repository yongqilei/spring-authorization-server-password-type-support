package com.xxx.cloud.auth.repository;

import com.xxx.cloud.auth.models.entity.CloudRegisteredClient;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Component
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final CloudRegisteredClientRepository cloudRegisteredClientRepository;

    public CustomRegisteredClientRepository(CloudRegisteredClientRepository cloudRegisteredClientRepository) {
        this.cloudRegisteredClientRepository = cloudRegisteredClientRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        // NOOP
    }

    @Override
    public RegisteredClient findById(String id) {
        return null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        CloudRegisteredClient cloudRegisteredClient = cloudRegisteredClientRepository.findFirstByClientId(clientId);
        RegisteredClient.Builder clientBuilder = RegisteredClient.withId(String.valueOf(cloudRegisteredClient.getId()))
                .clientId(cloudRegisteredClient.getClientId())
                .clientName(cloudRegisteredClient.getClientName())
                .clientSecret(cloudRegisteredClient.getClientSecret())
                .clientIdIssuedAt(cloudRegisteredClient.getClientIdIssuedAt());
        List<ClientAuthenticationMethod> methods = cloudRegisteredClient.getClientAuthenticationMethods();
        if (Objects.nonNull(methods) && methods.size() > 0) {
            methods.forEach(clientBuilder::clientAuthenticationMethod);
        }
        List<AuthorizationGrantType> grantTypes = cloudRegisteredClient.getAuthorizationGrantTypes();
        if (Objects.nonNull(grantTypes) && methods.size() > 0) {
            grantTypes.forEach(clientBuilder::authorizationGrantType);
        }
        List<String> redirectUris = cloudRegisteredClient.getRedirectUris();
        if (Objects.nonNull(redirectUris) && methods.size() > 0) {
            redirectUris.forEach(clientBuilder::redirectUri);
        }
        List<String> scopes = cloudRegisteredClient.getScopes();
        if (Objects.nonNull(scopes) && methods.size() > 0) {
            scopes.forEach(clientBuilder::scope);
        }
        clientBuilder.tokenSettings(defaultTokenSetting());
        return clientBuilder.build();
    }

    private TokenSettings defaultTokenSetting() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofDays(1))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(true)
                .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }
}
