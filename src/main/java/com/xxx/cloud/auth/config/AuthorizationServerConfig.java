package com.xxx.cloud.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.xxx.cloud.auth.security.OAuth2PasswordAuthenticationConverter;
import com.xxx.cloud.auth.security.OAuth2PasswordAuthenticationProvider;
import com.xxx.cloud.auth.security.jose.JsonWebTokenCustomizer;
import com.xxx.cloud.auth.security.jose.Jwks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.Objects;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    private final RegisteredClientRepository registeredClientRepository;

    public AuthorizationServerConfig(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer<>();

        http.apply(authorizationServerConfigurer.tokenEndpoint(tokenEndpoint -> {
            tokenEndpoint.accessTokenRequestConverter(
                    new DelegatingAuthenticationConverter(Arrays.asList(
                            new OAuth2AuthorizationCodeAuthenticationConverter(),
                            new OAuth2RefreshTokenAuthenticationConverter(),
                            new OAuth2ClientCredentialsAuthenticationConverter(),
                            new OAuth2PasswordAuthenticationConverter()
                    ))
            );
        }));
        authorizationServerConfigurer.authorizationEndpoint(endpointConfigurer -> endpointConfigurer.consentPage("/oauth/consent"));
        authorizationServerConfigurer.registeredClientRepository(registeredClientRepository);

        RequestMatcher endpointMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http.requestMatcher(endpointMatcher)
                .authorizeRequests(request -> request.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointMatcher))
                .apply(authorizationServerConfigurer);

        SecurityFilterChain securityFilterChain = http.formLogin(Customizer.withDefaults()).build();
        addCustomAuthenticationProvider(http);

        return securityFilterChain;
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    ProviderSettings providerSettings() {
        return ProviderSettings.builder()
                .issuer("http://localhost:9000/engine")
                .build();
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return new JsonWebTokenCustomizer();
    }

    private void addCustomAuthenticationProvider(HttpSecurity http) {
        ProviderSettings providerSettings = http.getSharedObject(ProviderSettings.class);
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        JwtEncoder jwtEncoder = http.getSharedObject(JwtEncoder.class);
        OAuth2PasswordAuthenticationProvider authenticationProvider = new OAuth2PasswordAuthenticationProvider(authenticationManager, jwtEncoder);

        OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer = jwtTokenCustomizer();
        if (Objects.nonNull(jwtCustomizer)) {
            authenticationProvider.setTokenCustomizer(jwtCustomizer);
        }

        authenticationProvider.setProviderSettings(providerSettings);

        http.authenticationProvider(authenticationProvider);
    }

}
