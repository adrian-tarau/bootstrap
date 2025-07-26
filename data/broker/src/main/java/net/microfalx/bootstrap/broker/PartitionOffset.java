package net.microfalx.bootstrap.broker;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An offset inside a partition.
 */
public final class PartitionOffset implements Identifiable<String>, Nameable, Comparable<PartitionOffset> {

    private final Partition partition;
    private final Object value;

    /**
     * Creates a partition offset.
     *
     * @param partition the partition
     * @param value     the offset identifier/value
     * @return a non-null instance
     */
    public static PartitionOffset create(Partition partition, Object value) {
        return new PartitionOffset(partition, value);
    }

    PartitionOffset(Partition partition, Object value) {
        requireNonNull(partition);
        requireNonNull(value);
        this.partition = partition;
        this.value = value;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int compareTo(PartitionOffset o) {
        return 0;
    }

    public Partition getPartition() {
        return partition;
    }

    public Object getValue() {
        return value;
    }
}
