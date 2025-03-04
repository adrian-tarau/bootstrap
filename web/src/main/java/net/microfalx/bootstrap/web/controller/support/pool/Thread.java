package net.microfalx.bootstrap.web.controller.support.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.TextUtils;
import net.microfalx.lang.ThreadUtils;
import net.microfalx.lang.annotation.*;
import net.microfalx.threadpool.ThreadPoolUtils;

@Name("Threads")
@Getter
@Setter
@ToString
@ReadOnly
public class Thread extends NamedIdentityAware<Long> {

    @Position(6)
    @Label(value = "Virtual")
    @Description("Indicates whether the thread is virtual")
    private boolean virtual;

    @Position(7)
    @Label(value = "State")
    @Description("The state of the thread")
    private java.lang.Thread.State state;

    @Position(8)
    @Label(value = "Carrier")
    @Description("The thread which carries a virtual thread")
    private String carrier;

    @Position(20)
    @Label(value = "Call Stack")
    @Description("The top of the call stack (what is executed right now)")
    private String executing;

    @Position(50)
    @Label(value = "Group")
    @Description("The group owning the tread")
    private String threadGroup;

    @Position(51)
    @Label(value = "Class Name")
    @Description("The name of the thread class (implementation)")
    @Visible(value = false)
    private String className;

    public static Thread from(java.lang.Thread thread) {
        Thread model = new Thread();
        model.setId(thread.threadId());
        model.setName(thread.getName());
        model.setCarrier(getCarrierThreadName(thread));
        model.setVirtual(thread.isVirtual());
        model.setState(thread.getState());
        model.setThreadGroup(thread.getThreadGroup() != null ? thread.getThreadGroup().getName() : null);
        model.setClassName(ClassUtils.getCompactName(thread));
        model.setExecuting(Thread.getTopStack(thread));
        return model;
    }

    public static String getThreadName(java.lang.Thread thread) {
        String name = thread.getName();
        String carrierName = getCarrierThreadName(thread);
        if (carrierName != null) name += " [" + carrierName + "]";
        return name;
    }

    public static String getTopStack(java.lang.Thread thread) {
        return TextUtils.abbreviateMiddle(ThreadUtils.getTopStack(thread), 50);
    }

    public static String getCarrierThreadName(java.lang.Thread thread) {
        String name = null;
        if (thread.isVirtual()) {
            java.lang.Thread carrier = ThreadPoolUtils.getCarrier(thread);
            if (carrier != null) name = carrier.getName();
        }
        return name;
    }
}
