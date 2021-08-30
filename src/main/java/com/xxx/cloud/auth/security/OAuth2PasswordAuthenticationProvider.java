package com.xxx.cloud.auth.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OAuth2PasswordAuthenticationProvider implements AuthenticationProvider {

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
