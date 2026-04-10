package tools.ceac.ai.mcp.campus.infrastructure.campus;

import tools.ceac.ai.mcp.campus.infrastructure.browser.BrowserCookieBridge;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Stateful HTTP client that keeps synchronized Moodle session cookies.
 */
@Component
public class CampusSessionHttpClient {
    private final CampusProperties properties;
    private final BrowserCookieBridge cookieBridge;
    private final ApplicationEventPublisher eventPublisher;
    private final CookieManager cookieManager;
    private final HttpClient httpClient;

    public CampusSessionHttpClient(CampusProperties properties, BrowserCookieBridge cookieBridge,
                                   ApplicationEventPublisher eventPublisher) {
        this.properties = properties;
        this.cookieBridge = cookieBridge;
        this.eventPublisher = eventPublisher;
        this.cookieManager = new CookieManager();
        this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(properties.httpTimeoutSeconds()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void syncFromEmbeddedBrowser() {
        cookieBridge.flushBrowserCookies();
        cookieBridge.copyCookiesToJavaNet(properties.baseUrl(), cookieManager);
    }

    public boolean hasMoodleSessionCookie() {
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if ("MoodleSession".equalsIgnoreCase(cookie.getName()) && !cookie.hasExpired()) {
                return true;
            }
        }
        return false;
    }

    public HttpResponse<String> post(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(properties.httpTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Cookie", buildCookieHeaderForUrl(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        eventPublisher.publishEvent(new HttpResponseEvent(this, "POST", url, body, response.body()));
        return response;
    }

    public void clearCookies() {
        cookieManager.getCookieStore().removeAll();
    }

    public String debugCookies() {
        StringBuilder sb = new StringBuilder();
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            sb.append(cookie.getName())
                    .append("=")
                    .append(cookie.getValue())
                    .append(" ; domain=")
                    .append(cookie.getDomain())
                    .append(" ; path=")
                    .append(cookie.getPath())
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    public HttpResponse<String> postForm(String url, String formBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(properties.httpTimeoutSeconds()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", buildCookieHeaderForUrl(url))
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        eventPublisher.publishEvent(new HttpResponseEvent(this, "POST", url, formBody, response.body()));
        return response;
    }

    public HttpResponse<byte[]> getBytes(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(properties.httpTimeoutSeconds()))
                .header("Cookie", buildCookieHeaderForUrl(url))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    public HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(properties.httpTimeoutSeconds()))
                .header("Accept", "*/*")
                .header("Cookie", buildCookieHeaderForUrl(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        eventPublisher.publishEvent(new HttpResponseEvent(this, "GET", url, null, response.body()));
        return response;
    }

    private String buildCookieHeaderForUrl(String url) {
        URI uri = URI.create(url);
        List<String> pairs = new ArrayList<>();
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.hasExpired()) {
                continue;
            }
            if (!matchesCookieDomain(cookie, uri.getHost())) {
                continue;
            }
            if (!matchesCookiePath(cookie, uri.getPath())) {
                continue;
            }
            if (cookie.getSecure() && !"https".equalsIgnoreCase(uri.getScheme())) {
                continue;
            }
            pairs.add(cookie.getName() + "=" + cookie.getValue());
        }
        return pairs.stream().distinct().collect(Collectors.joining("; "));
    }

    private boolean matchesCookieDomain(HttpCookie cookie, String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        String cookieDomain = cookie.getDomain();
        if (cookieDomain == null || cookieDomain.isBlank()) {
            return true;
        }
        String normalizedCookieDomain = normalizeDomain(cookieDomain);
        String normalizedHost = normalizeDomain(host);
        return normalizedHost.equals(normalizedCookieDomain)
                || normalizedHost.endsWith("." + normalizedCookieDomain);
    }

    private boolean matchesCookiePath(HttpCookie cookie, String requestPath) {
        String cookiePath = cookie.getPath();
        String normalizedCookiePath = (cookiePath == null || cookiePath.isBlank()) ? "/" : cookiePath;
        String normalizedRequestPath = (requestPath == null || requestPath.isBlank()) ? "/" : requestPath;
        return normalizedRequestPath.startsWith(normalizedCookiePath);
    }

    private String normalizeDomain(String value) {
        String normalized = value.startsWith(".") ? value.substring(1) : value;
        return normalized.toLowerCase(Locale.ROOT);
    }
}
