package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.ai.api.Token;

import java.util.Iterator;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class TokenStreamImpl extends AbstractTokenStream {

    private final Iterator<Token> streams;

    public TokenStreamImpl(Iterator<Token> streams) {
        requireNonNull(streams);
        this.streams = streams;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = streams.hasNext();
        completed.set(!hasNext);
        return hasNext;
    }

    @Override
    public Token next() {
        Token token = streams.next();
        answerBuilder.append(token);
        return token;
    }
}
