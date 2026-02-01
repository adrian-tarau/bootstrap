package net.microfalx.bootstrap.dos;

import net.microfalx.bootstrap.dos.jpa.AuditRepository;
import net.microfalx.bootstrap.dos.jpa.RuleRepository;
import net.microfalx.threadpool.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractDosTestCase {

    @Mock protected AuditRepository auditRepository;
    @Mock protected RuleRepository ruleRepository;
    @Mock protected ThreadPool threadPool;

    @Spy protected DosProperties properties = new DosProperties();
    @Spy protected ApplicationContext applicationContext = new GenericApplicationContext();

    protected DosPersistence persistence;
    @InjectMocks protected DosService dosService;

    @BeforeEach
    void setup() throws Exception {
        registerBeans();
        persistence = new DosPersistence();
        persistence.initialize(applicationContext);
        dosService.afterPropertiesSet();
    }

    private void registerBeans() {
        ((GenericApplicationContext) applicationContext).registerBean(AuditRepository.class, () -> auditRepository);
        ((GenericApplicationContext) applicationContext).registerBean(RuleRepository.class, () -> ruleRepository);
        ((GenericApplicationContext) applicationContext).registerBean(ThreadPool.class, () -> threadPool);
        ((GenericApplicationContext) applicationContext).refresh();
    }
}
