package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record ActivityResponse(
        String id,
        String name,
        String type,
        String url,
        boolean visible
) {
}


