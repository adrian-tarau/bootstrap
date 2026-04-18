package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.extension.Session;
import net.microfalx.lang.Initializable;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

/**
 * Base classes for all answers.
 */
public abstract class AbstractAnswer implements org.mockito.stubbing.Answer<Object>, Initializable {

    private Session session;

    public final Session getSession() {
        if (session == null) throw new IllegalStateException("Session has not been created yet");
        return session;
    }

    @Override
    public void initialize(Object... context) {
        this.session = (Session) context[0];
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        return Mockito.RETURNS_SMART_NULLS.answer(invocation);
    }
}
