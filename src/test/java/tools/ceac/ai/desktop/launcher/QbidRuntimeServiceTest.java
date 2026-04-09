package tools.ceac.ai.desktop.launcher;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QbidRuntimeServiceTest {

    @Test
    void commandLineArgsKeepServerPortOverride() {
        QbidRuntimeService service = new QbidRuntimeService();
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("server.port", 8082);
        properties.put("spring.application.name", "ceac-ia-tools-qbid-mcp");
        properties.put("mcp.remote.auth.required-audience", "qbid-mcp");

        assertArrayEquals(
                new String[] {
                        "--server.port=8082",
                        "--spring.application.name=ceac-ia-tools-qbid-mcp",
                        "--mcp.remote.auth.required-audience=qbid-mcp"
                },
                service.toCommandLineArgs(properties)
        );
    }
}
