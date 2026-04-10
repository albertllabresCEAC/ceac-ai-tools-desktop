package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record DeleteQuizSlotResponse(
        String newsummarks,
        boolean deleted,
        int newnumquestions
) {}
