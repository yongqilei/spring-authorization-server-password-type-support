package com.xxx.cloud.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Objects;

@Component
public class OAuth2PasswordAuthenticationProvider implements AuthenticationProvider {

    public static final String PASSWORD_TYPE_AUTHENTICATION_KEY = UsernamePasswordAuthenticationToken.class.getName().concat(".PRINCIPAL");

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer = context -> {};
    private ProviderSettings providerSettings;

    public OAuth2PasswordAuthenticationProvider(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null.");
        Assert.notNull(jwtEncoder, "jwtEncoder cannot be null.");
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
    }

    public void setTokenCustomizer(OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer) {
        Assert.notNull(tokenCustomizer, "tokenCustomizer cannot be null.");
        this.tokenCustomizer = tokenCustomizer;
    }

    @Autowired
    public void setProviderSettings(ProviderSettings providerSettings) {
        this.providerSettings = providerSettings;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2PasswordAuthenticationToken token = (OAuth2PasswordAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientAuthenticationToken = getAuthenticatedClientOrElseThrowError(token);
        RegisteredClient registeredClient = clientAuthenticationToken.getRegisteredClient();

        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.PASSWORD)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private OAuth2ClientAuthenticationToken getAuthenticatedClientOrElseThrowError(OAuth2PasswordAuthenticationToken token) {
        OAuth2ClientAuthenticationToken clientAuthenticationToken = null;

        if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(token.getClientPrincipal().getClass())) {
            clientAuthenticationToken = (OAuth2ClientAuthenticationToken) token.getClientPrincipal();
        }

        if (Objects.nonNull(clientAuthenticationToken) && clientAuthenticationToken.isAuthenticated()) {
            return clientAuthenticationToken;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }
}
