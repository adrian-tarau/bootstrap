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

    protected final Session getSession() {
        if (session == null) throw new IllegalStateException("Session has not been created yet");
        return session;
    }

    protected final <T> T lookup(Class<T> type) {
        return getSession().lookup(type);
    }

    protected final <T> T resolve(Class<T> type) {
        return getSession().resolve(type);
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
