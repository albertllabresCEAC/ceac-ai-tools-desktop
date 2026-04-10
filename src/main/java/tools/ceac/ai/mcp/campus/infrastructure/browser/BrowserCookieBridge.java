package tools.ceac.ai.mcp.campus.infrastructure.browser;

import org.cef.callback.CefCompletionCallback;
import org.cef.callback.CefCookieVisitor;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookie;
import org.cef.network.CefCookieManager;
import org.springframework.stereotype.Component;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Bridges session cookies from JCEF to java.net CookieManager.
 */
@Component
public class BrowserCookieBridge {

    public List<HttpCookie> extractCookiesForUrl(String url) {
        List<HttpCookie> cookies = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        CefCookieManager manager = CefCookieManager.getGlobalManager();

        boolean started = manager.visitUrlCookies(url, true, new CefCookieVisitor() {
            private int seen = 0;

            @Override
            public boolean visit(CefCookie cefCookie, int count, int total, BoolRef deleteCookie) {
                seen++;
                cookies.add(toHttpCookie(cefCookie));
                if (seen >= total) {
                    latch.countDown();
                }
                return true;
            }
        });

        if (!started) {
            return cookies;
        }
        await(latch, 1500);
        return cookies;
    }

    public List<HttpCookie> extractCookiesForDomain(String host) {
        List<HttpCookie> cookies = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        CefCookieManager manager = CefCookieManager.getGlobalManager();

        boolean started = manager.visitAllCookies((cefCookie, count, total, deleteCookie) -> {
            if (matchesDomain(cefCookie.domain, host)) {
                cookies.add(toHttpCookie(cefCookie));
            }
            if (count >= (total - 1)) {
                latch.countDown();
            }
            return true;
        });

        if (!started) {
            return cookies;
        }
        await(latch, 2000);
        return dedupe(cookies);
    }

    public void copyCookiesToJavaNet(String url, CookieManager targetCookieManager) {
        URI uri = URI.create(url);
        List<HttpCookie> cookies = extractCookiesForUrl(uri.toString());
        if (cookies.isEmpty()) {
            cookies = extractCookiesForDomain(uri.getHost());
        }

        targetCookieManager.getCookieStore().removeAll();
        for (HttpCookie cookie : cookies) {
            targetCookieManager.getCookieStore().add(uri, cookie);
        }
    }

    public void flushBrowserCookies() {
        CefCookieManager manager = CefCookieManager.getGlobalManager();
        CountDownLatch latch = new CountDownLatch(1);
        manager.flushStore(new CefCompletionCallback() {
            @Override
            public void onComplete() {
                latch.countDown();
            }
        });
        await(latch, 2000);
    }

    private HttpCookie toHttpCookie(CefCookie cefCookie) {
        HttpCookie cookie = new HttpCookie(cefCookie.name, cefCookie.value);
        if (cefCookie.domain != null && !cefCookie.domain.isBlank()) {
            cookie.setDomain(cefCookie.domain);
        }
        if (cefCookie.path != null && !cefCookie.path.isBlank()) {
            cookie.setPath(cefCookie.path);
        } else {
            cookie.setPath("/");
        }
        cookie.setSecure(cefCookie.secure);
        cookie.setHttpOnly(cefCookie.httponly);
        return cookie;
    }

    private boolean matchesDomain(String cookieDomain, String host) {
        if (cookieDomain == null || cookieDomain.isBlank() || host == null || host.isBlank()) {
            return false;
        }
        String cd = cookieDomain.startsWith(".") ? cookieDomain.substring(1) : cookieDomain;
        String h = host.startsWith(".") ? host.substring(1) : host;
        return h.equalsIgnoreCase(cd) || h.endsWith("." + cd);
    }

    private List<HttpCookie> dedupe(List<HttpCookie> cookies) {
        Map<String, HttpCookie> unique = new LinkedHashMap<>();
        for (HttpCookie cookie : cookies) {
            unique.put(cookie.getName() + "|" + cookie.getDomain() + "|" + cookie.getPath(), cookie);
        }
        return new ArrayList<>(unique.values());
    }

    private void await(CountDownLatch latch, long timeoutMs) {
        try {
            latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
