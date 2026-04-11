package tools.ceac.ai.desktop.launcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Keeps the local REST API and Swagger reachable only from the local machine. MCP endpoints remain
 * available through the tunnel because they use a different path.
 *
 * <p>This filter is the boundary that lets the product expose public MCP endpoints while keeping
 * manual REST checks and Swagger strictly local to the operator machine.
 */
public class LocalApiAccessFilter extends OncePerRequestFilter {

    private static final List<String> BLOCKED_REMOTE_HEADERS = List.of(
            "Forwarded",
            "X-Forwarded-For",
            "X-Forwarded-Proto",
            "CF-Connecting-IP",
            "CF-Ray",
            "Fly-Client-IP"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/api/")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isStrictlyLocal(request) || hasRemoteForwardingHeaders(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "This API is only available locally.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isStrictlyLocal(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddress)
                || "::1".equals(remoteAddress)
                || "0:0:0:0:0:0:0:1".equals(remoteAddress);
    }

    private boolean hasRemoteForwardingHeaders(HttpServletRequest request) {
        return BLOCKED_REMOTE_HEADERS.stream().anyMatch(header -> {
            String value = request.getHeader(header);
            return value != null && !value.isBlank();
        });
    }
}
