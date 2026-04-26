package tools.ceac.ai.security;

import java.lang.reflect.Method;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Resolves whether a tool method can be exposed for a given access level.
 */
public final class ToolAccessSupport {

    private ToolAccessSupport() {
    }

    public static boolean isMethodAllowed(Method method, ClientAccessLevel accessLevel) {
        if (accessLevel == null || accessLevel.allowsWrites()) {
            return true;
        }
        return AnnotationUtils.findAnnotation(method, CeacWriteTool.class) == null;
    }
}
