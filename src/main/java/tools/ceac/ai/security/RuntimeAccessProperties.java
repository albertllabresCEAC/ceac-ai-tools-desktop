package tools.ceac.ai.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Runtime access profile resolved during launcher login and injected into each local MCP runtime.
 */
@ConfigurationProperties(prefix = "ceac.security")
public class RuntimeAccessProperties {

    private ClientAccessLevel accessLevel = ClientAccessLevel.READ_WRITE;

    public ClientAccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = ClientAccessLevel.fromValue(accessLevel);
    }
}
