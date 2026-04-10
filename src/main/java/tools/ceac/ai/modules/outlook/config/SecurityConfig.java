package tools.ceac.ai.modules.outlook.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Seguridad del recurso MCP local.
 *
 * <p>Cuando la autenticacion remota esta activada, esta configuracion convierte la app local en
 * un resource server JWT que:
 *
 * <ul>
 *   <li>anuncia metadata OAuth del recurso protegido</li>
 *   <li>valida issuer y audience</li>
 *   <li>exige el scope configurado para el endpoint MCP y la API Outlook</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la proteccion del endpoint MCP y de la API REST asociada.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            McpRemoteProperties properties,
                                            McpUrlService mcpUrlService,
                                            AuthenticationEntryPoint authenticationEntryPoint,
                                            AccessDeniedHandler accessDeniedHandler) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        String endpoint = mcpUrlService.resolveMcpEndpointPath();
        String scopeAuthority = "SCOPE_" + properties.getAuth().getRequiredScope();

        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers(
                    "/.well-known/oauth-protected-resource",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/health"
            ).permitAll();

            if (properties.getAuth().isEnabled()) {
                authorize.requestMatchers(endpoint, endpoint + "/**", "/api/outlook/**").hasAuthority(scopeAuthority);
            } else {
                authorize.requestMatchers(endpoint, endpoint + "/**", "/api/outlook/**").permitAll();
            }

            authorize.anyRequest().permitAll();
        });

        if (properties.getAuth().isEnabled()) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
                    .jwt(Customizer.withDefaults()));
        }

        return http.build();
    }

    /**
     * CORS orientado a clientes MCP web que consumen metadata y endpoint streamable.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(McpRemoteProperties properties, McpUrlService mcpUrlService) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(new ArrayList<>(properties.getAllowedOriginPatterns()));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(HttpHeaders.WWW_AUTHENTICATE, "mcp-session-id"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        String endpoint = mcpUrlService.resolveMcpEndpointPath();
        source.registerCorsConfiguration(endpoint, configuration);
        source.registerCorsConfiguration(endpoint + "/**", configuration);
        source.registerCorsConfiguration("/.well-known/oauth-protected-resource", configuration);
        source.registerCorsConfiguration("/v3/api-docs/**", configuration);
        source.registerCorsConfiguration("/swagger-ui/**", configuration);
        return source;
    }

    /**
     * Devuelve el challenge OAuth adecuado cuando falta token.
     */
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(McpUrlService mcpUrlService) {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, buildAuthenticateHeader(mcpUrlService, request, authException));
        };
    }

    /**
     * Devuelve el challenge OAuth cuando el token existe pero no tiene el scope requerido.
     */
    @Bean
    AccessDeniedHandler accessDeniedHandler(McpRemoteProperties properties, McpUrlService mcpUrlService) {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            String header = "Bearer error=\"insufficient_scope\", scope=\"" +
                    properties.getAuth().getRequiredScope() + "\", resource_metadata=\"" +
                    mcpUrlService.resolveProtectedResourceMetadataUrl(request) + "\"";
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, header);
        };
    }

    /**
     * Configura el decoder JWT del recurso protegido usando issuer y opcionalmente JWKS explicito.
     */
    @Bean
    @ConditionalOnProperty(prefix = "mcp.remote.auth", name = "enabled", havingValue = "true")
    JwtDecoder jwtDecoder(McpRemoteProperties properties, McpUrlService mcpUrlService) {
        String issuerUri = properties.getAuth().getIssuerUri();
        if (!StringUtils.hasText(issuerUri)) {
            throw new IllegalStateException("mcp.remote.auth.issuer-uri is required when auth is enabled");
        }

        NimbusJwtDecoder jwtDecoder = buildJwtDecoder(properties);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                audienceValidator(mcpUrlService.resolveRequiredAudience())
        );
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }

    private NimbusJwtDecoder buildJwtDecoder(McpRemoteProperties properties) {
        if (StringUtils.hasText(properties.getAuth().getJwkSetUri())) {
            return NimbusJwtDecoder.withJwkSetUri(properties.getAuth().getJwkSetUri()).build();
        }
        JwtDecoder decoder = JwtDecoders.fromIssuerLocation(properties.getAuth().getIssuerUri());
        if (decoder instanceof NimbusJwtDecoder nimbusJwtDecoder) {
            return nimbusJwtDecoder;
        }
        throw new IllegalStateException("Unable to configure NimbusJwtDecoder for the configured issuer");
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String requiredAudience) {
        return token -> {
            List<String> audience = token.getAudience();
            if (audience != null && audience.stream().anyMatch(requiredAudience::equals)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "The token audience does not include the required MCP resource: " + requiredAudience,
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        };
    }

    private String buildAuthenticateHeader(McpUrlService mcpUrlService,
                                           HttpServletRequest request,
                                           AuthenticationException authException) {
        StringBuilder header = new StringBuilder("Bearer resource_metadata=\"")
                .append(mcpUrlService.resolveProtectedResourceMetadataUrl(request))
                .append("\"");
        if (authException != null && StringUtils.hasText(authException.getMessage())) {
            header.append(", error=\"invalid_token\"");
        }
        return header.toString();
    }
}


