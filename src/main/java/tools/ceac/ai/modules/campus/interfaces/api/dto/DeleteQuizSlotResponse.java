package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record DeleteQuizSlotResponse(
        String newsummarks,
        boolean deleted,
        int newnumquestions
) {}


