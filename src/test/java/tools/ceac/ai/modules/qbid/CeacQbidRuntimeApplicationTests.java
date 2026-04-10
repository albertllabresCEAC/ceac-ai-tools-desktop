package tools.ceac.ai.modules.qbid;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(
        classes = CeacQbidRuntimeApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.application.name=ceac-ia-tools-qbid-mcp-test",
                "spring.main.headless=true",
                "server.port=0",
                "spring.ai.mcp.server.name=ceac-qbid-mcp-test",
                "spring.ai.mcp.server.version=1.0.0",
                "spring.ai.mcp.server.type=SYNC",
                "spring.ai.mcp.server.protocol=STREAMABLE",
                "spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp",
                "mcp.remote.public-base-url=http://localhost:8082",
                "mcp.remote.mcp-endpoint=/mcp",
                "mcp.remote.auth.enabled=false"
        }
)
class CeacQbidRuntimeApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}

