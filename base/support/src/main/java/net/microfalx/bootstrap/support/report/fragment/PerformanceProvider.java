package net.microfalx.bootstrap.support.report.fragment;

import net.microfalx.bootstrap.support.report.AbstractFragmentProvider;
import net.microfalx.bootstrap.support.report.Chart;
import net.microfalx.bootstrap.support.report.Fragment;
import net.microfalx.bootstrap.support.report.Template;
import net.microfalx.jvm.ServerMetrics;
import net.microfalx.jvm.VirtualMachineMetrics;
import net.microfalx.metrics.SeriesStore;

@net.microfalx.lang.annotation.Provider
public class PerformanceProvider extends AbstractFragmentProvider {

    @Override
    public Fragment create() {
        return Fragment.builder("Performance").template("performance")
                .icon("fa-solid fa-flag-checkered")
                .order(800)
                .build();
    }

    @Override
    public void update(Template template) {
        super.update(template);
        template.addVariable("performance", this);
    }

    public Chart.AreaChart<Long, Float> getServerCpu(String id) {
        SeriesStore store = getServerMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "CPU");
        chart.add(Chart.convert("System", store.get(ServerMetrics.CPU_SYSTEM)));
        chart.add(Chart.convert("User", store.get(ServerMetrics.CPU_USER)));
        chart.add(Chart.convert("Nice", store.get(ServerMetrics.CPU_NICE)));
        chart.add(Chart.convert("I/O Wait", store.get(ServerMetrics.CPU_IO_WAIT)));
        chart.setStacked(true);
        chart.getYaxis().setUnit(Chart.Unit.PERCENT);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getServerLoad(String id) {
        SeriesStore store = getServerMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "Load");
        chart.add(Chart.convert("Load", store.get(ServerMetrics.LOAD_1)));
        chart.setStacked(true);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getServerKernel(String id) {
        SeriesStore store = getServerMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "Kernel");
        chart.add(Chart.convert("Context Switches", store.get(ServerMetrics.CONTEXT_SWITCHES)));
        chart.add(Chart.convert("Interrupts", store.get(ServerMetrics.INTERRUPTS)));
        return chart;
    }

    public Chart.AreaChart<Long, Float> getServerIOCounts(String id) {
        SeriesStore store = getServerMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "IO / Activity");
        chart.add(Chart.convert("Reads", store.get(ServerMetrics.IO_READS)));
        chart.add(Chart.convert("Writes", store.get(ServerMetrics.IO_WRITES)));
        return chart;
    }

    public Chart.AreaChart<Long, Float> getServerIOBytes(String id) {
        SeriesStore store = getServerMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "IO / Bytes");
        chart.add(Chart.convert("Read Bytes", store.get(ServerMetrics.IO_READ_BYTES)));
        chart.add(Chart.convert("Write Bytes", store.get(ServerMetrics.IO_WRITE_BYTES)));
        chart.getYaxis().setUnit(Chart.Unit.BYTE);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getServerMemory(String id) {
        SeriesStore store = getServerMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "Memory");
        chart.add(Chart.convert("Maximum", store.get(ServerMetrics.MEMORY_MAX)));
        chart.add(Chart.convert("Used", store.get(ServerMetrics.MEMORY_USED)));
        chart.getYaxis().setUnit(Chart.Unit.BYTE);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getProcessCpu(String id) {
        SeriesStore store = getVirtualMachineMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "CPU");
        chart.add(Chart.convert("System", store.get(VirtualMachineMetrics.CPU_SYSTEM)));
        chart.add(Chart.convert("User", store.get(VirtualMachineMetrics.CPU_USER)));
        chart.setStacked(true);
        chart.getYaxis().setUnit(Chart.Unit.PERCENT);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getProcessMemory(String id) {
        SeriesStore store = getVirtualMachineMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "Memory");
        chart.add(Chart.convert("Heap", store.get(VirtualMachineMetrics.MEMORY_HEAP_USED)));
        chart.add(Chart.convert("Non-Heap", store.get(VirtualMachineMetrics.MEMORY_NON_HEAP_USED)));
        chart.setStacked(true);
        chart.getYaxis().setUnit(Chart.Unit.BYTE);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getProcessThreads(String id) {
        SeriesStore store = getVirtualMachineMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "Threads");
        chart.add(Chart.convert("Daemon", store.get(VirtualMachineMetrics.THREAD_DAEMON)));
        chart.add(Chart.convert("Non-Daemon", store.get(VirtualMachineMetrics.THREAD_NON_DAEMON)));
        chart.setStacked(true);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getProcessIO(String id) {
        SeriesStore store = getVirtualMachineMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "IO");
        chart.add(Chart.convert("Read Bytes", store.get(VirtualMachineMetrics.IO_READ_BYTES)));
        chart.add(Chart.convert("Write Bytes", store.get(VirtualMachineMetrics.IO_WRITE_BYTES)));
        chart.setStacked(true);
        chart.getYaxis().setUnit(Chart.Unit.BYTE);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getProcessGcCounts(String id) {
        SeriesStore store = getVirtualMachineMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "GC / Collections");
        chart.add(Chart.convert("Eden", store.get(VirtualMachineMetrics.GC_EDEN_COUNT)));
        chart.add(Chart.convert("Tenured", store.get(VirtualMachineMetrics.GC_TENURED_COUNT)));
        chart.setStacked(true);
        return chart;
    }

    public Chart.AreaChart<Long, Float> getProcessGcDuration(String id) {
        SeriesStore store = getVirtualMachineMetrics();
        Chart.AreaChart<Long, Float> chart = new Chart.AreaChart<>(id, "GC / Durations");
        chart.add(Chart.convert("Eden", store.get(VirtualMachineMetrics.GC_EDEN_DURATION)));
        chart.add(Chart.convert("Tenured", store.get(VirtualMachineMetrics.GC_TENURED_DURATION)));
        chart.getYaxis().setUnit(Chart.Unit.DURATION);
        return chart;
    }

    private SeriesStore getVirtualMachineMetrics() {
        return VirtualMachineMetrics.get().getStore();
    }

    private SeriesStore getServerMetrics() {
        return ServerMetrics.get().getStore();
    }
}
