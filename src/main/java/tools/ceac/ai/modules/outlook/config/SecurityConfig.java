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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.ceac.ai.desktop.launcher.LocalApiAccessFilter;
import tools.ceac.ai.desktop.launcher.LauncherJwtSupport;

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
        http.addFilterBefore(new LocalApiAccessFilter(), SecurityContextHolderFilter.class);

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
                authorize.requestMatchers(endpoint, endpoint + "/**", "/api/outlook/**")
                        .access((authentication, context) -> new org.springframework.security.authorization.AuthorizationDecision(
                                LauncherJwtSupport.hasResourceAccess(authentication.get(), scopeAuthority)
                        ));
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
        return LauncherJwtSupport.buildCompositeJwtDecoder(
                properties.getAuth().getIssuerUri(),
                properties.getAuth().getJwkSetUri(),
                mcpUrlService.resolveRequiredAudience(),
                properties.getLauncher().getIssuerUri(),
                properties.getLauncher().getSharedSecret()
        );
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


