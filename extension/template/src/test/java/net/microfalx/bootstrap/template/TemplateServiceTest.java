package net.microfalx.bootstrap.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TemplateServiceTest extends AbstractTemplateTest {

    @Test
    void createMvelTemplate() {
        Template template = templateService.getTemplate(Template.Type.MVEL, "firstName +' '+lastName + '->'+ sex");
        assertEquals(Template.Type.MVEL, template.getType());
        assertNotNull(template.getResource());
    }

    @Test
    void createThymeleafMvelTemplate() {
        Template template = templateService.getTemplate(Template.Type.THYMELEAF, "${firstName} +' '+${lastName} + '->'+ ${sex}");
        assertEquals(Template.Type.THYMELEAF, template.getType());
        assertNotNull(template.getResource());
    }

}