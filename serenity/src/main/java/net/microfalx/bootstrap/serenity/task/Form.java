package net.microfalx.bootstrap.serenity.task;

import lombok.Getter;
import net.microfalx.lang.ObjectUtils;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.questions.Attribute;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.targets.TargetBuilder;
import org.openqa.selenium.By;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A collections of interactions and questions related to forms.
 */
public class Form implements Interaction {

    /**
     * The target which identifies the for,
     */
    @Getter
    private final Target form;

    /**
     * The target which identifies the submit button (optional),
     */
    @Getter
    private final Target button;

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
                Ensure.that("form is present", isPresent()).isTrue(),
                //Ensure.that("submit button", Enabled.of(button)).isTrue(),
                Click.on(button)
        );
    }

    /**
     * Returns the interaction which fills the form fields with the correct values.
     *
     * @return a non-null interaction
     */
    public Task fill() {
        return Task.where("{0} fills in the form ",
                Ensure.that("form is present", isPresent()).isTrue(),
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

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(fill(), submit());
    }

    class FillByNameInteraction implements Interaction {

        @Override
        public <T extends Actor> void performAs(T actor) {
            for (Map.Entry<String, Object> entry : fieldsByNames.entrySet()) {
                actor.attemptsTo(Enter.theValue(ObjectUtils.toString(entry.getValue()))
                        .into(By.name(entry.getKey())));
            }
        }
    }

    class FillByLabelInteraction implements Interaction {

        @Override
        public <T extends Actor> void performAs(T actor) {
            for (Map.Entry<String, Object> entry : fieldsByLabels.entrySet()) {
                Target field = form.find(Target.the("the label")
                        .located(By.xpath("label[text()='" + entry.getKey() + "']")));
                String fieldName = actor.asksFor(Attribute.of(field, "for"));
                actor.attemptsTo(Enter.theValue(ObjectUtils.toString(entry.getValue()))
                        .into(By.name(fieldName)));
            }
        }
    }
}
