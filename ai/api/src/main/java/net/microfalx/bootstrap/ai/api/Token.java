package net.microfalx.bootstrap.ai.api;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.ArgumentUtils;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Getter
@ToString
public class Token {

    /**
     * The type of message token
     */
    private final Type type;

    /**
     * The text of the token, which can be null or empty.
     */
    private final String text;

    /**
     * Creates a new token with the specified type and text.
     *
     * @param type the type of the token, which can be THINKING, QUESTION, or ANSWER
     * @param text the text of the token, which can be null or empty
     * @return a non-null instance
     */
    public static Token create(Type type, String text) {
        return new Token(type, text);
    }

    private Token(Type type, String text) {
        requireNonNull(type);
        this.type = type;
        this.text = text;
    }

    public enum Type {
        THINKING,
        QUESTION,
        ANSWER
    }

}
