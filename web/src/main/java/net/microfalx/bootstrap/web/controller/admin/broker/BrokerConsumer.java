package net.microfalx.bootstrap.web.controller.admin.broker;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.broker.Broker;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.microfalx.lang.NumberUtils.throughput;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Name("Consumers")
@ReadOnly
public class BrokerConsumer {

    @Id
    @Visible(value = false)
    private String id;

    @Position(1)
    @Description("The name of the broker (cluster)")
    @Label(value = "Name", group = "Broker")
    private String name;

    @Position(2)
    @Description("The type of the broker (cluster)")
    @Label(value = "Type", group = "Broker")
    private Broker.Type type;

    @Position(5)
    @Description("The type of the topic")
    @Name
    private String topic;

    @Position(6)
    @Description("The number of partitions assigned to the consumer")
    private int partitions;

    @Position(7)
    @Description("The subscription associated with the consumer")
    private String subscription;

    @Position(8)
    @Description("The age of the consumer")
    private Duration age;

    @Position(9)
    @Description("Indicates whether the consumer has auto-commit turned on")
    private boolean autoCommit;

    @Position(10)
    @Description("The number of executed polls")
    @Label(value = "Consumed", group = "Events")
    private int eventCount;

    @Position(11)
    @Description("The throughput of the consumed events")
    @Label(value = "Throughput", group = "Events")
    @Formattable(unit = Formattable.Unit.THROUGHPUT_REQUESTS)
    private float eventThroughput;

    @Position(12)
    @Description("The number of pending events (lag)")
    @Label(value = "Pending", group = "Counts")
    private int lagCount;

    @Position(20)
    @Description("The number of executed polls")
    @Label(value = "Poll", group = "Counts")
    private int pollCount;

    @Position(21)
    @Description("The number of commits")
    @Label(value = "Commit", group = "Counts")
    private int commitCount;

    @Position(22)
    @Description("The number of rollbacks")
    @Label(value = "Rollback", group = "Counts")
    private int rollbackCount;

    @Position(50)
    @Description("The status of the consumer")
    private net.microfalx.bootstrap.broker.BrokerConsumer.Status status;

    @Position(101)
    @Description("The timestamp when the consumer was created")
    @CreatedAt
    private LocalDateTime createdAt;

    /**
     * Creates a model from a {@link net.microfalx.bootstrap.broker.BrokerConsumer}.
     *
     * @param consumer the consumer
     * @return a non-null instance
     */
    public static BrokerConsumer from(net.microfalx.bootstrap.broker.BrokerConsumer<?, ?> consumer) {
        if (consumer == null) return null;
        Topic topic = consumer.getTopic();
        BrokerConsumer model = new BrokerConsumer();
        model.setId(consumer.getId());
        model.setName(topic.getBroker().getName());
        model.setType(topic.getBroker().getType());
        model.setTopic(topic.getName());
        model.setPartitions(consumer.getPartitions().size());
        model.setAge(Duration.between(consumer.getCreatedAt(), LocalDateTime.now()));
        model.setAutoCommit(topic.isAutoCommit());
        model.setEventThroughput(throughput(consumer.getEventCount(), consumer.getCreatedAt()).floatValue());
        model.setSubscription(topic.getSubscription());
        model.setPollCount(consumer.getPollCount());
        model.setEventCount(consumer.getEventCount());
        model.setCommitCount(consumer.getCommitCount());
        model.setRollbackCount(consumer.getRollbackCount());
        model.setStatus(consumer.getStatus());
        model.setCreatedAt(consumer.getCreatedAt());
        return model;
    }
}
