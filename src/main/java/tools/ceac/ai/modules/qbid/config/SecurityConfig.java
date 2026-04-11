package tools.ceac.ai.modules.qbid.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.ceac.ai.desktop.launcher.LocalApiAccessFilter;
import tools.ceac.ai.desktop.launcher.LauncherJwtSupport;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
                authorize.requestMatchers(endpoint, endpoint + "/**")
                        .access((authentication, context) -> new org.springframework.security.authorization.AuthorizationDecision(
                                LauncherJwtSupport.hasResourceAccess(authentication.get(), scopeAuthority)
                        ));
                authorize.anyRequest()
                        .access((authentication, context) -> new org.springframework.security.authorization.AuthorizationDecision(
                                LauncherJwtSupport.hasResourceAccess(authentication.get(), scopeAuthority)
                        ));
            } else {
                authorize.anyRequest().permitAll();
            }
        });

        if (properties.getAuth().isEnabled()) {
            JwtDecoder jwtDecoder = buildJwtDecoder(properties, mcpUrlService);
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
                    .jwt(jwt -> jwt.decoder(jwtDecoder)));
        }

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(McpRemoteProperties properties, McpUrlService mcpUrlService) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(properties.getAllowedOriginPatterns());
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

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(McpUrlService mcpUrlService) {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, buildAuthenticateHeader(mcpUrlService, request));
        };
    }

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

    private JwtDecoder buildJwtDecoder(McpRemoteProperties properties, McpUrlService mcpUrlService) {
        return LauncherJwtSupport.buildCompositeJwtDecoder(
                properties.getAuth().getIssuerUri(),
                properties.getAuth().getJwkSetUri(),
                mcpUrlService.resolveRequiredAudience(),
                properties.getLauncher().getIssuerUri(),
                properties.getLauncher().getSharedSecret()
        );
    }

    private String buildAuthenticateHeader(McpUrlService mcpUrlService, HttpServletRequest request) {
        return "Bearer resource_metadata=\"" + mcpUrlService.resolveProtectedResourceMetadataUrl(request) + "\"";
    }
}



