package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.bootstrap.test.annotation.AnswerFor;
import org.mockito.invocation.InvocationOnMock;

@AnswerFor(Registry.class)
public class RegistryAnswer extends AbstractAnswer {

    private Registry registry;

    @Override
    public void initialize(Object... context) {
        super.initialize(context);
        registry = getSession().lookup(RegistryService.class).getRegistry();
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        return invocation.getMethod().invoke(registry, invocation.getArguments());
    }
}
