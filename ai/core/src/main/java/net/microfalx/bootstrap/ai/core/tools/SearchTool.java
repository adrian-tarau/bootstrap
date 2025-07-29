package net.microfalx.bootstrap.ai.core.tools;

import net.microfalx.bootstrap.ai.api.Tool;

public class SearchTool extends AbstractToolExecutor {

    @Override
    public String execute(Tool.ExecutionRequest request) {
        return wrapResponse(null);
    }
}
