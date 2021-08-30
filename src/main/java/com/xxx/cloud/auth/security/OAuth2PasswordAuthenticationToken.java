package com.xxx.cloud.auth.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class OAuth2PasswordAuthenticationToken extends AbstractAuthenticationToken implements Authentication {

    private final AuthorizationGrantType grantType;
    private Authentication clientPrincipal;
    private Object principal;
    private Object credentials;
    private Map<String, Object> additionalParameters;

    public OAuth2PasswordAuthenticationToken(AuthorizationGrantType grantType,
                                             Authentication clientPrincipal,
                                             Object principal, Object credentials,
                                             Map<String, Object> additionalParameters) {
        super(null);
        Assert.notNull(grantType, "Grant type cannot be null.");
        Assert.notNull(principal, "Principal cannot be null.");
        Assert.notNull(credentials, "Credentials cannot be null.");
        this.grantType = grantType;
        this.clientPrincipal = clientPrincipal;
        this.principal = principal;
        this.credentials = credentials;
        this.additionalParameters = Collections.unmodifiableMap(additionalParameters != null ? additionalParameters : Collections.emptyMap());
    }

    public OAuth2PasswordAuthenticationToken(AuthorizationGrantType grantType,
                                             Authentication clientPrincipal,
                                             Object principal, Object credentials,
                                             Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        Assert.notNull(grantType, "Grant type cannot be null.");
        Assert.notNull(principal, "Principal cannot be null.");
        Assert.notNull(credentials, "Credentials cannot be null.");
        this.grantType = grantType;
        this.clientPrincipal = clientPrincipal;
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    public AuthorizationGrantType getGrantType() {
        return grantType;
    }

    public Authentication getClientPrincipal() {
        return clientPrincipal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        Assert.isTrue(!authenticated,
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters;
    }
}
