package com.xxx.cloud.auth.security.jose;

import com.xxx.cloud.auth.entity.CloudAuthAccount;
import com.xxx.cloud.auth.security.OAuth2PasswordAuthenticationProvider;
import com.xxx.cloud.auth.security.model.CloudUserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonWebTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        AbstractAuthenticationToken token = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2ClientAuthenticationToken) {
            token = (AbstractAuthenticationToken) authentication;
        }
        if (Objects.nonNull(token) && authentication.isAuthenticated() &&
                OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            Authentication usernamePasswordAuthentication = null;
            AuthorizationGrantType authorizationGrantType = context.getAuthorizationGrantType();
            if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationGrantType)) {
                usernamePasswordAuthentication = context.getPrincipal();
            }
            if (AuthorizationGrantType.PASSWORD.equals(context.getAuthorizationGrantType())) {
                usernamePasswordAuthentication = context.get(OAuth2PasswordAuthenticationProvider.PASSWORD_TYPE_AUTHENTICATION_KEY);
            }
            if (Objects.nonNull(usernamePasswordAuthentication) && usernamePasswordAuthentication
                    instanceof UsernamePasswordAuthenticationToken) {
                CloudUserDetails principal = (CloudUserDetails) usernamePasswordAuthentication.getPrincipal();
                Long userId = principal.getId();
                Set<String> authorities = principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                JwtClaimsSet.Builder builder = context.getClaims();
                builder.claim(OAuth2ParameterNames.SCOPE, authorities);
                builder.claim("user_id", userId);
                builder.claim(OAuth2ParameterNames.USERNAME, principal.getUsername());
            }
        }
    }
}
