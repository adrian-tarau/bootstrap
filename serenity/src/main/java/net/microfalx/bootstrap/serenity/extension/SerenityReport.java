package net.microfalx.bootstrap.serenity.extension;

import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.StringUtils;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.model.domain.CastMember;
import net.thucydides.model.domain.TestOutcome;
import net.thucydides.model.domain.TestStep;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import net.thucydides.model.reports.AsciiColors;

import java.util.stream.Collectors;

import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.lang.TextUtils.abbreviateMiddle;

/**
 * A class which creates a report based on a Serenity test outcome.
 */
class SerenityReport {

    public static final String FAIL_GLYPH = "❌";
    public static final String SUCCESS_GLYPH = "✅";
    public static final String CANCEL_GLYPH = "⊘";
    public static final String BULLET_GLYPH = "•";

    private static final int INDENT_STEP = 2;

    private final TestOutcome outcome;
    private final SystemEnvironmentVariables environmentVariables = new SystemEnvironmentVariables();
    private final AsciiColors colors = new AsciiColors(environmentVariables);
    private final StringBuilder logger = new StringBuilder();

    private int indent;

    public static SerenityReport create(TestOutcome outcome) {
        ArgumentUtils.requireNonNull(outcome);
        return new SerenityReport(outcome);
    }

    public static SerenityReport current() {
        TestOutcome outcome = StepEventBus.getParallelEventBus().getBaseStepListener().getCurrentTestOutcome();
        return new SerenityReport(outcome);
    }

    private SerenityReport(TestOutcome outcome) {
        this.outcome = outcome;
    }

    /**
     * Returns whether the Serenity test outcome is available.
     *
     * @return <code>true</code> if available, <code>false</code> otherwise
     */
    public boolean exists() {
        return outcome != null;
    }

    /**
     * Generates a textual report of the outcome.
     *
     * @return a non-null instance
     */
    public String generate() {
        if (outcome != null) doGenerate();
        return logger.toString();
    }

    private void doGenerate() {
        append("Name: " + outcome.getCompleteName() + ", actors: " + getActors());
        if (StringUtils.isNotEmpty(outcome.getDescription())) {
            append("Description: " + outcome.getDescription());
        }
        if (StringUtils.isNotEmpty(outcome.getConciseErrorMessage())) {
            append(FAIL_GLYPH+" failed, error description: " + outcome.getConciseErrorMessage());
        }
        append("Steps:");
        for (TestStep testStep : outcome.getTestSteps()) {
            appendStep(testStep);
        }
    }

    private void appendStep(TestStep testStep) {
        increaseIndent();
        String description = abbreviateMiddle(testStep.getDescription(), 140) + " (D=" + formatDuration(testStep.getDuration()) + ")";
        append(BULLET_GLYPH + " " + getStatusSymbol(testStep) + " " + description);
        increaseIndent();
        for (TestStep child : testStep.getChildren()) {
            appendStep(child);
        }
        decreaseIndent();
        decreaseIndent();
    }

    private String getStatusSymbol(TestStep testStep) {
        if (testStep.isFailure() || testStep.isError()) {
            return colors.red(FAIL_GLYPH);
        } else if (testStep.isSkipped()) {
            return colors.yellow(CANCEL_GLYPH);
        } else if (testStep.isSuccessful()) {
            return colors.green(SUCCESS_GLYPH);
        } else {
            return colors.magenta("?");
        }
    }

    private void append(String message) {
        logger.append(getIndentAsString()).append(message).append('\n');
    }

    private void increaseIndent() {
        indent += INDENT_STEP;
    }

    private void decreaseIndent() {
        indent -= INDENT_STEP;
    }

    private String getIndentAsString() {
        return indent == 0 ? StringUtils.EMPTY_STRING : org.apache.commons.lang3.StringUtils.repeat(' ', indent);
    }

    private String getActors() {
        return outcome.getActors().stream().map(CastMember::getName).collect(Collectors.joining(", "));
    }
}
