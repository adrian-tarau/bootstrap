package net.microfalx.bootstrap.store;

import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Timer;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class StoreUtils {

    static Metrics METRICS = Metrics.of("Store");
    static Metrics METRICS_FAILURES = METRICS.withGroup("Failure");

    public static String ADD_ACTION = "Add";
    public static String REMOVE_ACTION = "Remove";
    public static String FIND_ACTION = "Find";
    public static String WALK_ACTION = "Walk";

    /**
     * Returns the timer which tracks a given action.
     *
     * @param action the action
     * @param store  the store
     * @return the timer
     */
    public static Timer getTimer(String action, Store<?, ?> store) {
        requireNonNull(action);
        requireNonNull(store);
        return METRICS.withGroup(action).getTimer(store.getName());
    }
}
