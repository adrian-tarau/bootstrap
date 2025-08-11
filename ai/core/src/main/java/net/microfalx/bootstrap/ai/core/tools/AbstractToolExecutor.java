package net.microfalx.bootstrap.ai.core.tools;

import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.microfalx.bootstrap.ai.api.Content;
import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.ai.core.ContentImpl;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.lang.StringUtils;

import java.io.IOException;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Base class for tool executors that provides common functionality and integrates with the application context.
 */
public abstract class AbstractToolExecutor extends ApplicationContextSupport implements Tool.Executor {

    /**
     * Wraps the response from a tool execution in a standardized format.
     *
     * @param response the response from the tool execution
     * @return the final response
     */
    protected final String wrapResponse(String response) {
        StringBuilder builder = new StringBuilder();
        if (isNotEmpty(response)) {
            builder.append("Observation: The tool returned the data bellow. Answer the user's original question naturally, using this data.\n\n");
            builder.append(response);
        } else {
            builder.append("Observation: The tool returned not data, maybe use a different tool.");
        }
        return builder.toString();
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
     * @param content   the context
     * @param itemCount the items returned by the tool
     * @return a non-null instance
     */
    protected final Tool.ExecutionResponse createResponse(String content, int itemCount, String name) {
        return new ToolExecutionResponse(name, ContentImpl.from(wrapResponse(content)), itemCount);
    }

    /**
     * Creates an empty tool execution response with the given content and count.
     *
     * @return a non-null instance
     */
    protected final Tool.ExecutionResponse createEmptyResponse() {
        return createResponse(StringUtils.EMPTY_STRING, 0, "Empty");
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
            HuggingFaceTokenCountEstimator estimator = new HuggingFaceTokenCountEstimator();
            try {
                return estimator.estimateTokenCountInText(content.getResource().loadAsString());
            } catch (IOException e) {
                return -1;
            }
        }
    }
}
