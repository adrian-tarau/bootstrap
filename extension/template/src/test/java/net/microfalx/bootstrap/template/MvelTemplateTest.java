package net.microfalx.bootstrap.template;

import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MvelTemplateTest extends AbstractTemplateTest {

    @Test
    public void evaluateWithModel() {
        Template template = templateService.getTemplate(Template.Type.MVEL, "firstName +' '+lastName + ' -> '+ sex");
        TemplateContext templateContext = templateService.createContext(new Person());
        assertEquals("John Doe -> MALE", template.evaluate(templateContext));
    }

    @Test
    public void evaluateWithAttributes() {
        Template template = templateService.getTemplate(Template.Type.MVEL, "person.firstName +' '+person.lastName + ' -> '+ person.sex");
        Attributes<Attribute> attributes = Attributes.create();
        TemplateContext templateContext = templateService.createContext(attributes);
        attributes.add("person", new Person());
        assertEquals("John Doe -> MALE", template.evaluate(templateContext));
    }

    @Test
    public void evaluateSimpleExpression() {
        Template template = templateService.getTemplate(Template.Type.MVEL, "name");
        TemplateContext templateContext = templateService.createContext();
        templateContext.set("name", "John");
        assertEquals("John", template.evaluate(templateContext));
    }

    @Test
    public void evaluateTemplate() throws IOException {
        Template template = templateService.getTemplate(Template.Type.MVEL, "@{person.firstName} @{person.lastName} -> @{person.sex}");
        TemplateContext templateContext = templateService.createContext();
        templateContext.set("person", new Person());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        template.evaluate(templateContext, buffer);
        assertEquals("John Doe -> MALE", buffer.toString());
    }

    @Test
    public void evaluateNoCache() throws Exception {
        templateProperties.setCached(false);
        templateService.afterPropertiesSet();
        evaluateWithModel();
    }

}