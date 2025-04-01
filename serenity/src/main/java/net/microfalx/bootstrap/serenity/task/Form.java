package net.microfalx.bootstrap.serenity.task;

import lombok.Getter;
import net.microfalx.lang.ObjectUtils;
import net.serenitybdd.screenplay.*;
import net.serenitybdd.screenplay.actions.*;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.questions.Attribute;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.targets.TargetBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A collections of interactions and questions related to forms.
 */
public class Form implements Interaction {

    /**
     * The target which identifies the form.
     */
    @Getter
    private final Target form;

    /**
     * The target which identifies the submit button (optional),
     */
    @Getter
    private Target button;

    private final Map<String, Object> fieldsByNames = new LinkedHashMap<>();
    private final Map<String, Object> fieldsByLabels = new LinkedHashMap<>();

    /**
     * Creates a form using a data set form.
     *
     * @return a non-null instance
     */
    public static Form create() {
        return create(new TargetBuilder<>("form").located(By.id("dataset-form")));
    }

    /**
     * Creates a form using a target.
     *
     * @param form the form
     * @return a non-null instance
     */
    public static Form create(Target form) {
        return create(form, null);
    }

    /**
     * Creates a form using a target for the form itself and the element (button) which triggers the submit action.
     *
     * @param form   the form
     * @param button the button
     * @return a non-null instance
     */
    public static Form create(Target form, Target button) {
        return new Form(form, button);
    }

    private Form(Target form, Target button) {
        requireNonNull(form);
        this.form = form;
        this.button = button;
    }

    /**
     * Returns the interaction which submits the form.
     *
     * @return a non-null interaction
     */
    public Task submit() {
        return Application.task(
                "{0} submits the form",
                checkPresent(),
                new SubmitInteraction()
        );
    }

    /**
     * Returns the interaction which fills the form fields with the correct values.
     *
     * @return a non-null interaction
     */
    public Task fill() {
        return Task.where("{0} fills in the form ",
                checkPresent(),
                new FillByNameInteraction(),
                new FillByLabelInteraction()
        );
    }

    /**
     * Populates a form field using the field name.
     *
     * @param name  the field name
     * @param value the field value
     * @return self
     */
    public Form fieldByName(String name, Object value) {
        requireNonNull(name);
        fieldsByNames.put(name, value);
        return this;
    }

    /**
     * Populates a form field using the field name.
     * <p>
     * The method presumes that the label has the attribute "for" which identifies the field associated with the label.
     *
     * @param label the field label
     * @param value the field value
     * @return self
     */
    public Form fieldByLabel(String label, Object value) {
        requireNonNull(label);
        fieldsByLabels.put(label, value);
        return this;
    }

    /**
     * Returns whether the form is present and visible on the screen.
     *
     * @return a non-null instance
     */
    public Question<Boolean> isPresent() {
        return form::isVisibleFor;
    }

    /**
     * Returns a performable which checks if the form is present.
     *
     * @return a non-null instance
     */
    public Performable checkPresent() {
        return Ensure.that("form is present", isPresent()).isTrue();
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(fill(), submit());
    }

    private <T extends Actor> void setValue(T actor, String name, Object value) {
        Target field = form.find(Target.the("the field")
                .located(By.name(name)));
        String type = actor.asksFor(Attribute.of(field, "type"));
        if (type == null) type = actor.asksFor(tagName(field));
        if ("checkbox".equals(type)) {
            SetCheckboxInteraction interaction = SetCheckbox.of(field);
            actor.attemptsTo(Boolean.TRUE.equals(value) ? interaction.toTrue() : interaction.toFalse());
        } else if ("select".equals(type) || "select-multiple".equals(type)) {
            actor.attemptsTo(SelectFromOptions.byVisibleText(ObjectUtils.toStringArray(value)).from(field));
        } else {
            actor.attemptsTo(Enter.theValue(ObjectUtils.toString(value))
                    .into(field));
        }
    }

    private static Question<String> tagName(Target target) {
        return Question.about(target.getName() + " name")
                .answeredBy(actor -> target.resolveAllFor(actor).stream().findFirst().map(WebElement::getTagName).orElse(null));
    }

    class FillByNameInteraction implements Interaction {

        @Override
        public <T extends Actor> void performAs(T actor) {
            for (Map.Entry<String, Object> entry : fieldsByNames.entrySet()) {
                setValue(actor, entry.getKey(), entry.getValue());
            }
        }
    }

    class FillByLabelInteraction implements Interaction {

        @Override
        public <T extends Actor> void performAs(T actor) {
            for (Map.Entry<String, Object> entry : fieldsByLabels.entrySet()) {
                Target label = form.find(Target.the("a label")
                        .located(By.tagName("label")).containingText(entry.getKey()));
                String fieldName = actor.asksFor(Attribute.of(label, "for"));
                setValue(actor, fieldName, entry.getValue());
            }
        }
    }

    class SubmitInteraction implements Interaction {

        @Override
        public <T extends Actor> void performAs(T actor) {
            if (button == null) {
                button = Target.the("a button")
                        .located(By.tagName("button")).containingText("Save");
            }
            actor.attemptsTo(Click.on(button));
        }
    }
}
