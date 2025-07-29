package net.microfalx.bootstrap.ai.core.tools;

import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.lang.StringUtils;

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
        if (StringUtils.isNotEmpty(response)) {
            builder.append("Observation: The tool returned the data bellow. Answer the user's original question naturally, using this data.\n\n");
            builder.append(response);
        } else {
            builder.append("Observation: The tool returned not data, maybe use a different tool.");
        }
        return builder.toString();
    }
}
