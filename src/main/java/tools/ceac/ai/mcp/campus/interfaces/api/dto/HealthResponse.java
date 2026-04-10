package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record HealthResponse(
        String status,
        boolean authenticated
) {
}
