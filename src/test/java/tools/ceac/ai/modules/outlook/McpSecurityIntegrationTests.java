package tools.ceac.ai.modules.outlook;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "mcp.remote.public-base-url=https://mcp.example.com",
        "mcp.remote.auth.enabled=true",
        "mcp.remote.auth.issuer-uri=https://auth.example.com/realms/outlookdesktop-mcp",
        "mcp.remote.auth.jwk-set-uri=https://auth.example.com/realms/outlookdesktop-mcp/protocol/openid-connect/certs"
})
@AutoConfigureMockMvc
class McpSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void exposesProtectedResourceMetadata() throws Exception {
        mockMvc.perform(get("/.well-known/oauth-protected-resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resource").value("https://mcp.example.com/mcp"))
                .andExpect(jsonPath("$.authorization_servers", hasItem("https://auth.example.com/realms/outlookdesktop-mcp")))
                .andExpect(jsonPath("$.scopes_supported", hasItem("mcp:tools")));
    }

    @Test
    void challengesMcpEndpointWithProtectedResourceMetadata() throws Exception {
        mockMvc.perform(get("/mcp"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate",
                        containsString("resource_metadata=\"https://mcp.example.com/.well-known/oauth-protected-resource\"")));
    }

    @Test
    void allowsCorsPreflightForInspector() throws Exception {
        mockMvc.perform(options("/mcp")
                        .header("Origin", "http://localhost:6274")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:6274"))
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("mcp-session-id")));
    }
}

