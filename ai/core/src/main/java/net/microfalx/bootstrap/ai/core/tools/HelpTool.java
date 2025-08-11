package net.microfalx.bootstrap.ai.core.tools;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiService;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.help.RenderingOptions;
import net.microfalx.bootstrap.help.Toc;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.List;

import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;

@Slf4j
public class HelpTool extends AbstractToolExecutor {

    private static final String TOC_ATTR_PREFIX = "toc.";

    @Override
    public Tool.ExecutionResponse execute(Tool.ExecutionRequest request) {
        List<Toc> tocs = search(request);
        return render(request.getChat(), tocs);
    }

    private List<Toc> search(Tool.ExecutionRequest request) {
        HelpService helpService = getBean(HelpService.class);
        String query = normalizeQuery(request.getArgument("query"));
        return helpService.search(query, 0, 10);
    }

    private String normalizeQuery(String query) {
        AiService aiService = getBean(AiService.class);
        if (StringUtils.isEmpty(query)) return EMPTY_STRING;
        query = StringUtils.replaceAll(query, aiService.getName(), EMPTY_STRING);
        return query.trim();
    }

    private Tool.ExecutionResponse render(Chat chat, List<Toc> tocs) {
        HelpService helpService = getBean(HelpService.class);
        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder nameBuilder = new StringBuilder();
        tocs = tocs.stream().
                filter(toc -> !chat.hasAttribute(TOC_ATTR_PREFIX + toc.getId()))
                .toList();
        if (tocs.isEmpty()) {
            contentBuilder.append("No help content found or help was already provided before.");
        } else {
            for (Toc toc : tocs) {
                try {
                    Resource resource = helpService.transform(toc, RenderingOptions.builder().navigation(true).build());
                    contentBuilder.append(resource.loadAsString()).append("\n");
                    chat.addAttribute(TOC_ATTR_PREFIX + toc.getId(), Boolean.TRUE);
                    StringUtils.append(nameBuilder, toc.getName());
                } catch (IOException e) {
                    LOGGER.warn("Failed to load content for TOC '{}', root cause: {}", toc.getPath(), getRootCauseMessage(e));
                }
            }
        }
        return createResponse(contentBuilder.toString(), tocs.size(), nameBuilder.toString());
    }
}
