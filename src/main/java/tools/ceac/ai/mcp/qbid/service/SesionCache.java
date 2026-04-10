package tools.ceac.ai.mcp.qbid.service;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caché en memoria de sesiones qBID por usuario.
 * Evita hacer login en qBID en cada request — reutiliza el jsessionid
 * mientras no hayan pasado 14 minutos desde la última interacción
 * (qBID expira la sesión por inactividad a los 15 minutos).
 */
@Component
public class SesionCache {

    private final QbidHttpService http;
    private final ConcurrentHashMap<String, CachedSession> cache = new ConcurrentHashMap<>();

    public SesionCache(QbidHttpService http) {
        this.http = http;
    }

    /**
     * Dado el header Authorization: Basic <base64>,
     * devuelve un jsessionid válido — del caché o haciendo login.
     */
    public String resolveSession(String authorizationHeader) throws Exception {
        String[] credentials = decodeBasicAuth(authorizationHeader);
        String username = credentials[0];
        String password = credentials[1];

        CachedSession cached = cache.get(username);
        if (cached != null && !cached.isExpired()) {
            cache.put(username, cached.touch());
            return cached.jsessionid();
        }

        // Login en qBID y guardar en caché
        String jsessionid = http.login(username, password);
        cache.put(username, new CachedSession(jsessionid, System.currentTimeMillis()));
        return jsessionid;
    }

    /**
     * Fuerza renovación de sesión para un usuario.
     * Se llama cuando qBID devuelve SessionExpiredException.
     */
    public String renewSession(String authorizationHeader) throws Exception {
        String[] credentials = decodeBasicAuth(authorizationHeader);
        cache.remove(credentials[0]);
        return resolveSession(authorizationHeader);
    }

    /**
     * Elimina la sesión del caché (logout).
     */
    public void invalidate(String authorizationHeader) {
        try {
            String[] credentials = decodeBasicAuth(authorizationHeader);
            cache.remove(credentials[0]);
        } catch (Exception ignored) {}
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String[] decodeBasicAuth(String header) {
        if (header == null || !header.startsWith("Basic ")) {
            throw new IllegalArgumentException("Header Authorization inválido. Usa Basic Auth.");
        }
        String base64  = header.substring("Basic ".length()).trim();
        String decoded = new String(Base64.getDecoder().decode(base64));
        String[] parts = decoded.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de credenciales inválido.");
        }
        return parts;
    }

    // ── Record interno ────────────────────────────────────────────────────────

    private record CachedSession(String jsessionid, long lastUsed) {
        /**
         * La sesión expira si han pasado más de 14 minutos desde la última
         * interacción (qBID expira por inactividad a los 15 minutos).
         */
        boolean isExpired() {
            return System.currentTimeMillis() - lastUsed > (14 * 60_000);
        }

        CachedSession touch() {
            return new CachedSession(jsessionid, System.currentTimeMillis());
        }
    }
}

