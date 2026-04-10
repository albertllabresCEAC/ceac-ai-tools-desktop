package tools.ceac.ai.modules.qbid.application.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Provides the local qBid operator credentials injected into the embedded runtime.
 *
 * <p>The credentials are local-only launcher inputs. They are passed to the qBid Spring context as
 * startup properties and never leave the desktop machine.
 */
@Component
public class QbidRuntimeCredentials {

    private final String username;
    private final String password;

    public QbidRuntimeCredentials(
            @Value("${qbid.runtime.username:}") String username,
            @Value("${qbid.runtime.password:}") String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Builds the Basic authorization header expected by the local qBid session cache.
     */
    public String buildBasicAuthHeader() {
        String effectiveUsername = firstNonBlank(
                username,
                System.getenv("QBID_RUNTIME_USERNAME"),
                System.getProperty("qbid.runtime.username")
        );
        String effectivePassword = firstNonBlank(
                password,
                System.getenv("QBID_RUNTIME_PASSWORD"),
                System.getProperty("qbid.runtime.password")
        );
        if (!StringUtils.hasText(effectiveUsername) || !StringUtils.hasText(effectivePassword)) {
            throw new IllegalStateException("Credenciales qBid no inicializadas.");
        }
        String encoded = Base64.getEncoder()
                .encodeToString((effectiveUsername + ":" + effectivePassword).getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}

