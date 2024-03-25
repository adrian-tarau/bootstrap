package net.microfalx.bootstrap.template;

import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MvelTemplateTest extends AbstractTemplateTest {

    @Test
    public void evaluateWithModel() {
        Template template = templateService.getTemplate(Template.Type.MVEL, "@{person.firstName} @{person.lastName} -> @{person.sex}");
        TemplateContext templateContext = templateService.createContext(new Person());
        templateContext.set("person", new Person());
        assertEquals("John Doe -> MALE", template.evaluate(templateContext));
    }

    @Test
    public void evaluateWithAttributes() {
        Template template = templateService.getTemplate(Template.Type.MVEL, "@{person.firstName} @{person.lastName} -> @{person.sex}");
        Attributes<Attribute> attributes = Attributes.create();
        TemplateContext templateContext = templateService.createContext(attributes);
        attributes.add("person", new Person());
        assertEquals("John Doe -> MALE", template.evaluate(templateContext));
    }

}