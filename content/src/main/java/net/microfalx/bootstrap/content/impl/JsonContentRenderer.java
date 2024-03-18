package net.microfalx.bootstrap.content.impl;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentRenderer;
import net.microfalx.bootstrap.content.ContentUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

@Provider
public class JsonContentRenderer implements ContentRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonContentRenderer.class);

    @Override
    public Resource prettyPrint(Resource resource) throws IOException {
        ObjectMapper objectMapper = ContentUtils.createObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(resource.getInputStream());
            StringWriter writer = new StringWriter();
            objectMapper.writeValue(writer, jsonNode);
            writer.close();
            return MemoryResource.create(writer.toString(), resource.getFileName()).withName(resource.getName())
                    .withMimeType(resource.getMimeType());
        } catch (JsonParseException e) {
            // if it cannot be parsed
            return resource;
        } catch (JacksonException e) {
            // if it cannot be parsed or pretty print, return as is
            return resource;
        }
    }

    @Override
    public boolean supports(Content content) {
        return MimeType.APPLICATION_JSON.equals(content.getMimeType());
    }
}
