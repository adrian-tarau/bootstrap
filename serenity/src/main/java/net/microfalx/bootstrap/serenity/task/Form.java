package net.microfalx.bootstrap.serenity.task;

import lombok.Getter;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.targets.TargetBuilder;
import org.openqa.selenium.By;

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
    public Interaction submit() {
        return null;
    }

    /**
     * Returns the interaction which fills the form fields with the correct values.
     *
     * @return a non-null interaction
     */
    public Interaction fill() {
        return null;
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
        return this;
    }

    /**
     * Returns whether the form is present and visible on the screen.
     *
     * @return a non-null instance
     */
    public Question<Boolean> isPresent() {
        return null;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(fill(), submit());
    }
}
