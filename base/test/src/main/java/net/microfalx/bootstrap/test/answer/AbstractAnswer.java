package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.TestContext;
import net.microfalx.lang.Initializable;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

/**
 * Base classes for all answers.
 */
public abstract class AbstractAnswer implements org.mockito.stubbing.Answer<Object>, Initializable {

    private TestContext testContext;

    protected final TestContext getContext() {
        if (testContext == null) throw new IllegalStateException("Session has not been created yet");
        return testContext;
    }

    protected final <T> T lookup(Class<T> type) {
        return getContext().lookup(type);
    }

    protected final <T> T resolve(Class<T> type) {
        return getContext().resolve(type);
    }

    @Override
    public void initialize(Object... context) {
        this.testContext = (TestContext) context[0];
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        return Mockito.RETURNS_SMART_NULLS.answer(invocation);
    }
}
