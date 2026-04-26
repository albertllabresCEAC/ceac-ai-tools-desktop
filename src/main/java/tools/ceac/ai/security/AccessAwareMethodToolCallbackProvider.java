package tools.ceac.ai.security;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Variant of Spring AI's method-based provider with an explicit method filter.
 */
public final class AccessAwareMethodToolCallbackProvider implements ToolCallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(AccessAwareMethodToolCallbackProvider.class);

    private final List<Object> toolObjects;
    private final Predicate<Method> methodFilter;

    private AccessAwareMethodToolCallbackProvider(List<Object> toolObjects, Predicate<Method> methodFilter) {
        Assert.notNull(toolObjects, "toolObjects cannot be null");
        Assert.noNullElements(toolObjects, "toolObjects cannot contain null elements");
        this.toolObjects = toolObjects;
        this.methodFilter = methodFilter == null ? method -> true : methodFilter;
        assertToolAnnotatedMethodsPresent(toolObjects);
        validateToolCallbacks(getToolCallbacks());
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        ToolCallback[] toolCallbacks = this.toolObjects.stream()
                .map(toolObject -> toolMethodsFor(toolObject)
                        .map(toolMethod -> MethodToolCallback.builder()
                                .toolDefinition(ToolDefinitions.from(toolMethod))
                                .toolMetadata(ToolMetadata.from(toolMethod))
                                .toolMethod(toolMethod)
                                .toolObject(toolObject)
                                .toolCallResultConverter(ToolUtils.getToolCallResultConverter(toolMethod))
                                .build())
                        .toArray(ToolCallback[]::new))
                .flatMap(Stream::of)
                .toArray(ToolCallback[]::new);

        validateToolCallbacks(toolCallbacks);
        return toolCallbacks;
    }

    private void assertToolAnnotatedMethodsPresent(List<Object> toolObjects) {
        for (Object toolObject : toolObjects) {
            List<Method> toolMethods = toolMethodsFor(toolObject).toList();
            if (toolMethods.isEmpty()) {
                throw new IllegalStateException("No eligible @Tool annotated methods found in " + toolObject + ".");
            }
        }
    }

    private Stream<Method> toolMethodsFor(Object toolObject) {
        Class<?> targetClass = AopUtils.isAopProxy(toolObject) ? AopUtils.getTargetClass(toolObject) : toolObject.getClass();
        return Stream.of(ReflectionUtils.getDeclaredMethods(targetClass))
                .filter(this::isToolAnnotatedMethod)
                .filter(toolMethod -> !isFunctionalType(toolMethod))
                .filter(ReflectionUtils.USER_DECLARED_METHODS::matches)
                .filter(methodFilter);
    }

    private boolean isFunctionalType(Method toolMethod) {
        boolean isFunction = ClassUtils.isAssignable(Function.class, toolMethod.getReturnType())
                || ClassUtils.isAssignable(Supplier.class, toolMethod.getReturnType())
                || ClassUtils.isAssignable(Consumer.class, toolMethod.getReturnType());
        if (isFunction) {
            logger.warn("Method {} is annotated with @Tool but returns a functional type. This is not supported and the method will be ignored.",
                    toolMethod.getName());
        }
        return isFunction;
    }

    private boolean isToolAnnotatedMethod(Method method) {
        Tool annotation = AnnotationUtils.findAnnotation(method, Tool.class);
        return Objects.nonNull(annotation);
    }

    private void validateToolCallbacks(ToolCallback[] toolCallbacks) {
        List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
        if (!duplicateToolNames.isEmpty()) {
            throw new IllegalStateException("Multiple tools with the same name (%s) found in sources: %s".formatted(
                    String.join(", ", duplicateToolNames),
                    this.toolObjects.stream().map(o -> o.getClass().getName()).collect(Collectors.joining(", "))));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private List<Object> toolObjects;
        private Predicate<Method> methodFilter = method -> true;

        private Builder() {
        }

        public Builder toolObjects(Object... toolObjects) {
            Assert.notNull(toolObjects, "toolObjects cannot be null");
            this.toolObjects = Arrays.asList(toolObjects);
            return this;
        }

        public Builder methodFilter(Predicate<Method> methodFilter) {
            this.methodFilter = methodFilter;
            return this;
        }

        public AccessAwareMethodToolCallbackProvider build() {
            return new AccessAwareMethodToolCallbackProvider(this.toolObjects, this.methodFilter);
        }
    }
}
