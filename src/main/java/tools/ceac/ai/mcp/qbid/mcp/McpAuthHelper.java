package tools.ceac.ai.mcp.qbid.mcp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class McpAuthHelper {

    private static String apiUser;
    private static String apiPassword;

    public static void setCredentials(String user, String password) {
        apiUser = user;
        apiPassword = password;
    }

    public String buildBasicAuthHeader() {
        if ((apiUser == null || apiPassword == null)
                && StringUtils.hasText(System.getenv("QBID_RUNTIME_USERNAME"))
                && StringUtils.hasText(System.getenv("QBID_RUNTIME_PASSWORD"))) {
            apiUser = System.getenv("QBID_RUNTIME_USERNAME");
            apiPassword = System.getenv("QBID_RUNTIME_PASSWORD");
        }
        if ((apiUser == null || apiPassword == null)
                && StringUtils.hasText(System.getProperty("qbid.runtime.username"))
                && StringUtils.hasText(System.getProperty("qbid.runtime.password"))) {
            apiUser = System.getProperty("qbid.runtime.username");
            apiPassword = System.getProperty("qbid.runtime.password");
        }
        if (apiUser == null || apiPassword == null) {
            throw new IllegalStateException("Credenciales qBID no inicializadas.");
        }
        String encoded = Base64.getEncoder()
                .encodeToString((apiUser + ":" + apiPassword).getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}

