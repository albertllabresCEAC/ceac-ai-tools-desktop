package tools.ceac.ai.modules.campus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(
        classes = CeacCampusRuntimeApplication.class,
        properties = {
                "server.port=0",
                "spring.main.headless=true",
                "campus.base-url=https://campus.ceacfp.es",
                "campus.login-url=https://campus.ceacfp.es/login/index.php",
                "campus.dashboard-path=/my/",
                "campus.http-timeout-seconds=30",
                "campus.jcef-install-dir=target/test-jcef-install",
                "campus.jcef-cache-dir=target/test-jcef-cache",
                "campus.ui.enabled=false",
                "campus.ui.show-logout=true",
                "campus.ui.show-cookies=true",
                "campus.ui.show-current-url=true",
                "campus.ui.show-browser-view=true",
                "mcp.remote.public-base-url=http://localhost",
                "mcp.remote.mcp-endpoint=/mcp",
                "mcp.remote.auth.enabled=false",
                "spring.ai.mcp.server.protocol=STREAMABLE",
                "spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp"
        }
)
class CeacCampusRuntimeApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoadsWithoutEmbeddedUi() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.containsBean("campusMcpTools")).isTrue();
    }
}

