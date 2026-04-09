package tools.ceac.ai.mcp.qbid.service;

import tools.ceac.ai.mcp.qbid.exception.SessionExpiredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ãšnica clase que habla con qBID por HTTP.
 * Todas las demÃ¡s clases pasan por aquÃ­ â€” nunca usan HttpClient directamente.
 *
 * Stateless: cada llamada recibe el jsessionid del cliente.
 * No guarda estado entre requests.
 */
@Service
public class QbidHttpService {

    private static final Logger log = LoggerFactory.getLogger(QbidHttpService.class);

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36";

    @Value("${qbid.base-url:https://www.empresaiformacio.org/sBid}")
    private String baseUrl;

    @Value("${qbid.http.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${qbid.http.read-timeout:30000}")
    private int readTimeout;

    // â”€â”€ Login â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Realiza el login en dos pasos (GET pÃ¡gina + POST credenciales).
     * Devuelve el JSESSIONID autenticado.
     */
    public String login(String username, String password) throws Exception {

        // Cada login usa su propio CookieManager â€” aislado
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();

        log("LOGIN", baseUrl + "/modules/Login");
        // Paso 1 â€” GET para obtener JSESSIONID anÃ³nimo
        HttpRequest getPage = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/modules/Login?initial=yes"))
                .GET()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .build();

        client.send(getPage, HttpResponse.BodyHandlers.ofString());

        // Paso 2 â€” POST credenciales
        String body = buildFormBody(Map.of(
                "idTask",      "",
                "hashTask",    "",
                "moduleaction","doLogin",
                "fwBrowser",   "Chrome",
                "username",    username,
                "password",    password
        ));

        HttpRequest postLogin = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/modules/Login"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", USER_AGENT)
                .header("Referer", baseUrl + "/modules/Login?initial=yes")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .build();

        HttpResponse<String> loginResponse = client.send(
                postLogin, HttpResponse.BodyHandlers.ofString());

        // Verificar login exitoso por contenido
        if (loginResponse.body().contains("doLogin")) {
            throw new SessionExpiredException("Credenciales incorrectas.");
        }

        // Extraer JSESSIONID y sessionExpiry
        return cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> "JSESSIONID".equals(c.getName()))
                .findFirst()
                .map(HttpCookie::getValue)
                .orElseThrow(() -> new SessionExpiredException("No se recibiÃ³ JSESSIONID."));
    }

    // â”€â”€ GET autenticado â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Hace un GET a qBID usando el jsessionid proporcionado.
     * Lanza SessionExpiredException si la sesiÃ³n ha expirado.
     */
    public String get(String url, String jsessionid) throws Exception {
        log("GET ", url);
        HttpClient client = buildAuthenticatedClient(jsessionid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        checkSessionValid(response);
        return response.body();
    }

    // â”€â”€ GET binario autenticado â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Hace un GET a qBID y devuelve la respuesta como bytes (para PDFs).
     * Lanza SessionExpiredException si la sesiÃ³n ha expirado.
     */
    public byte[] getBytes(String url, String jsessionid) throws Exception {
        log("GET ", url);
        HttpClient client = buildAuthenticatedClient(jsessionid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/pdf,*/*")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.uri().toString().contains("Login?initial=yes")) {
            throw new SessionExpiredException();
        }
        return response.body();
    }

    // â”€â”€ POST autenticado â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Hace un POST a qBID usando el jsessionid proporcionado.
     */
    public String post(String url, Map<String, String> params, String jsessionid) throws Exception {
        String body = buildFormBody(params);
        log("POST", url, body);
        HttpClient client = buildAuthenticatedClient(jsessionid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "es-ES,es;q=0.9")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        checkSessionValid(response);
        return response.body();
    }

    /**
     * POST con body ya construido como String.
     * Ãštil para formularios con parÃ¡metros multi-valor (e.g. activitatFormativa[]).
     */
    public String postRaw(String url, String formBody, String jsessionid) throws Exception {
        log("POST", url, formBody);
        HttpClient client = buildAuthenticatedClient(jsessionid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "es-ES,es;q=0.9")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkSessionValid(response);
        return response.body();
    }

    // â”€â”€ Helpers privados â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private HttpClient buildAuthenticatedClient(String jsessionid) {
        CookieManager cm = new CookieManager();
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        // Inyectar el JSESSIONID manualmente
        HttpCookie cookie = new HttpCookie("JSESSIONID", jsessionid);
        cookie.setPath("/sBid");
        cookie.setVersion(0);
        cm.getCookieStore().add(
                URI.create("https://www.empresaiformacio.org"),
                cookie
        );

        return HttpClient.newBuilder()
                .cookieHandler(cm)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();
    }

    private void checkSessionValid(HttpResponse<String> response) {
        String finalUrl = response.uri().toString();
        String body = response.body();

        // Detectar redirecciÃ³n a login o contenido de login
        if (finalUrl.contains("Login?initial=yes") || body.contains("doLogin")) {
            throw new SessionExpiredException();
        }
    }

    private void log(String method, String url) {
        log.info("[HTTP] {}  {}", method, url);
    }

    private void log(String method, String url, String body) {
        log.info("[HTTP] {}  {}  body: {}", method, url, body);
    }

    private String buildFormBody(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}

