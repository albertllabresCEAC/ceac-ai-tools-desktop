package tools.ceac.ai.modules.trello;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(
        classes = CeacTrelloRuntimeApplication.class,
        properties = {
                "spring.application.name=ceac-ai-tools-trello-mcp-test",
                "server.port=0",
                "mcp.remote.public-base-url=http://localhost:8083",
                "mcp.remote.auth.enabled=false",
                "mcp.remote.auth.required-scope=trello:tools",
                "mcp.remote.auth.resource-name=Trello MCP",
                "trello.api-base-url=https://api.trello.com/1",
                "trello.api-key=test-api-key",
                "trello.access-token=test-access-token"
        }
)
class CeacTrelloRuntimeApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext.containsBean("trelloToolCallbacks")).isTrue();
        assertThat(applicationContext.containsBean("trelloController")).isTrue();
    }
}
