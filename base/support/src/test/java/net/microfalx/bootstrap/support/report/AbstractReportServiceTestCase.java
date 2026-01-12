package net.microfalx.bootstrap.support.report;

import net.microfalx.bootstrap.logger.LoggerService;
import net.microfalx.jvm.ServerMetrics;
import net.microfalx.jvm.VirtualMachineMetrics;
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
public abstract class AbstractReportServiceTestCase {

    @Spy protected ApplicationContext applicationContext = new GenericApplicationContext();
    @Mock protected LoggerService loggerService;
    @Mock protected ThreadPool threadPool;

    @InjectMocks protected ReportService reportService;

    @BeforeEach
    void setup() throws Exception {
        registerBeans();
        reportService.afterPropertiesSet();
        //collectVMStats();
    }

    void collectVMStats() {
        VirtualMachineMetrics vmm = VirtualMachineMetrics.get();
        ServerMetrics sm = ServerMetrics.get();
        for (int i = 0; i < 5; i++) {
            vmm.scrape();
            sm.scrape();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void registerBeans() {
        ((GenericApplicationContext) applicationContext).registerBean(LoggerService.class, () -> loggerService);
        ((GenericApplicationContext) applicationContext).registerBean(ThreadPool.class, () -> threadPool);
        ((GenericApplicationContext) applicationContext).refresh();
    }
}
