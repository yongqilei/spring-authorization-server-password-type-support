package com.xxx.cloud.auth.security;

import com.xxx.cloud.auth.security.model.CloudUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.JoseHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class OAuth2PasswordAuthenticationProvider implements AuthenticationProvider {

    public static final String PASSWORD_TYPE_AUTHENTICATION_KEY = UsernamePasswordAuthenticationToken.class.getName().concat(".PRINCIPAL");
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2PasswordAuthenticationProvider.class);
    private static final StringKeyGenerator DEFAULT_REFRESH_TOKEN_GENERATOR =
            new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer = context -> {};
    private Supplier<String> refreshTokenGenerator = DEFAULT_REFRESH_TOKEN_GENERATOR::generateKey;
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

    public void setRefreshTokenGenerator(Supplier<String> refreshTokenGenerator) {
        this.refreshTokenGenerator = refreshTokenGenerator;
    }

    @Autowired
    public void setProviderSettings(ProviderSettings providerSettings) {
        this.providerSettings = providerSettings;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2PasswordAuthenticationToken passwordAuthenticationToken = (OAuth2PasswordAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientAuthenticationToken = getAuthenticatedClientOrElseThrowError(passwordAuthenticationToken);
        RegisteredClient registeredClient = clientAuthenticationToken.getRegisteredClient();

        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.PASSWORD)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        Map<String, Object> requestParameters = passwordAuthenticationToken.getAdditionalParameters();
        String username = (String) requestParameters.get(OAuth2ParameterNames.USERNAME);
        String password = (String) requestParameters.get(OAuth2ParameterNames.PASSWORD);
        UsernamePasswordAuthenticationToken usernamePasswordAuthentication =
                new UsernamePasswordAuthenticationToken(username, password);

        Authentication passwordAuthentication = authenticationManager.authenticate(usernamePasswordAuthentication);
        Set<String> scopes = registeredClient.getScopes();
        Set<String> requestedScopes = passwordAuthenticationToken.getScopes();

        for (String requestScope : requestedScopes) {
            if (!scopes.contains(requestScope)) {
                OAuth2Error oAuth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                        String.format("Unsupported scope: %s", requestScope), OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
                throw new OAuth2AuthenticationException(oAuth2Error, String.format("Unsupported scope: %s", requestScope));
            }
        }

        String issuer = this.providerSettings != null ? this.providerSettings.getIssuer() : null;

        JoseHeader.Builder headerBuilder = JwtUtils.headers();
        JwtClaimsSet.Builder claimBuilder = JwtUtils.accessTokenClaims(registeredClient, issuer,
                ((CloudUserDetails) passwordAuthentication.getPrincipal()).getUsername(), requestedScopes);
        JwtEncodingContext jwtEncodingContext = JwtEncodingContext.with(headerBuilder, claimBuilder)
                .registeredClient(registeredClient)
                .principal(clientAuthenticationToken)
                .authorizedScopes(requestedScopes)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                .put(PASSWORD_TYPE_AUTHENTICATION_KEY, passwordAuthentication)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrant(passwordAuthenticationToken)
                .build();

        this.tokenCustomizer.customize(jwtEncodingContext);

        JoseHeader header = jwtEncodingContext.getHeaders().build();
        JwtClaimsSet claims = jwtEncodingContext.getClaims().build();

        Jwt jwt = jwtEncoder.encode(header, claims);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(),
                jwt.getIssuedAt(), jwt.getExpiresAt(), requestedScopes);

        OAuth2RefreshToken refreshToken = null;

        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN) &&
                !clientAuthenticationToken.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {
            refreshToken = generateRefreshToken(registeredClient.getTokenSettings().getRefreshTokenTimeToLive());
        }

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientAuthenticationToken, accessToken, refreshToken);
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

    private OAuth2RefreshToken generateRefreshToken(Duration tokenTimeToLive) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(tokenTimeToLive);
        return new OAuth2RefreshToken(this.refreshTokenGenerator.get(), issuedAt, expiresAt);
    }
}
