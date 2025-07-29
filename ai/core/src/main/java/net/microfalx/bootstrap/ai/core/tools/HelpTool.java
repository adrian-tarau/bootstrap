package net.microfalx.bootstrap.ai.core.tools;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.help.Toc;

import java.io.IOException;
import java.util.List;

import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;

@Slf4j
public class HelpTool extends AbstractToolExecutor {

    @Override
    public String execute(Tool.ExecutionRequest request) {
        List<Toc> tocs = search(request);
        return render(tocs);
    }

    private List<Toc> search(Tool.ExecutionRequest request) {
        HelpService helpService = getBean(HelpService.class);
        return helpService.search(request.getArgument("query"), 0, 10);
    }

    private String render(List<Toc> tocs) {
        StringBuilder sb = new StringBuilder();
        if (tocs.isEmpty()) {
            sb.append("No help content found.");
            return sb.toString();
        } else {
            for (Toc toc : tocs) {
                try {
                    sb.append(toc.getContent().loadAsString()).append("\n");
                } catch (IOException e) {
                    LOGGER.warn("Failed to load content for TOC '{}', root cause: {}", toc.getPath(), getRootCauseMessage(e));
                }
            }
        }
        return wrapResponse(sb.toString());
    }
}
