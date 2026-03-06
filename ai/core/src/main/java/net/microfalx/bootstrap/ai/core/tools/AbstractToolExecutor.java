package net.microfalx.bootstrap.ai.core.tools;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.microfalx.bootstrap.ai.api.Content;
import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.ai.core.ContentImpl;
import net.microfalx.bootstrap.ai.core.ToolExecutors;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;

import java.io.IOException;

import static net.microfalx.lang.StringUtils.EMPTY_STRING;

/**
 * Base class for tool executors that provides common functionality and integrates with the application context.
 */
public abstract class AbstractToolExecutor extends ApplicationContextSupport implements Tool.Executor {

    /**
     * Wraps the response from a tool execution in a standardized format.
     *
     * @param tool the tool who produced the response
     * @param response the response from the tool execution
     * @return the final response
     */
    protected final String wrapResponse(Tool tool, String response) {
        return ToolExecutors.wrapResponse(tool, null, response, null);
    }

    /**
     * Creates a tool execution response with the given content and count.
     *
     * @param content   the context
     * @param itemCount the number of items returned by the tool
     * @return a itemCount-null instance
     */
    protected final Tool.ExecutionResponse createResponse(Content content, int itemCount, String name) {
        return new ToolExecutionResponse(name, content, itemCount);
    }

    /**
     * Creates a tool execution response with the given content and count.
     *
     * @param tool the tool who produced the response
     * @param content   the context
     * @param itemCount the items returned by the tool
     * @return a non-null instance
     */
    protected final Tool.ExecutionResponse createResponse(Tool tool, String content, int itemCount, String name) {
        return new ToolExecutionResponse(name, ContentImpl.from(wrapResponse(tool, content)), itemCount);
    }

    /**
     * Creates an empty tool execution response with the given content and count.
     *
     * @param tool the tool who produced the response
     * @return a non-null instance
     */
    protected final Tool.ExecutionResponse createEmptyResponse(Tool tool) {
        return createResponse(tool, EMPTY_STRING, 0, "Empty");
    }

    /**
     * Converts the result of the tool execution to a string format that can be used in the response.
     *
     * @param result the result of the tool execution
     * @return the string representation of the result
     */
    protected final String convert(Object result) {
        if (result == null) return EMPTY_STRING;
        DefaultToolCallResultConverter converter = new DefaultToolCallResultConverter();
        return converter.convert(converter, result.getClass());
    }

    @RequiredArgsConstructor
    @Getter
    @ToString
    private static class ToolExecutionResponse implements Tool.ExecutionResponse {

        private final String name;
        private final Content content;
        private final int itemCount;

        @Override
        public int getTokenCount() {
            TokenCountEstimator estimator = new JTokkitTokenCountEstimator();
            try {
                return estimator.estimate(content.getResource().loadAsString());
            } catch (IOException e) {
                return -1;
            }
        }
    }
}
