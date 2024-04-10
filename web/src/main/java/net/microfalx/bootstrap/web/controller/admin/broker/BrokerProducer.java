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

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Name("Consumers")
@ReadOnly
public class BrokerProducer {

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

    @Position(8)
    @Description("The age of the consumer")
    private Duration age;

    @Position(9)
    @Description("Indicates whether the producer has auto-commit turned on")
    private boolean autoCommit;

    @Position(10)
    @Description("The number of executed polls")
    @Label(value = "Consumed", group = "Events")
    private int eventCount;

    @Position(11)
    @Description("The throughput of the produced events")
    @Label(value = "Throughput", group = "Events")
    @Formattable(unit = Formattable.Unit.THROUGHPUT_REQUESTS)
    private int eventThroughput;

    @Position(21)
    @Description("The number of commits")
    @Label(value = "Commit", group = "Counts")
    private int commitCount;

    @Position(22)
    @Description("The number of rollbacks")
    @Label(value = "Rollback", group = "Counts")
    private int rollbackCount;

    @Position(50)
    @Description("The status of the producer")
    private net.microfalx.bootstrap.broker.BrokerProducer.Status status;

    @Position(101)
    @Description("The timestamp when the consumer was created")
    private LocalDateTime createdAt;

    /**
     * Creates a model from a {@link net.microfalx.bootstrap.broker.BrokerProducer}.
     *
     * @param producer the producer
     * @return a non-null instance
     */
    public static BrokerProducer from(net.microfalx.bootstrap.broker.BrokerProducer<?, ?> producer) {
        if (producer == null) return null;
        Topic topic = producer.getTopic();
        BrokerProducer model = new BrokerProducer();
        model.setId(producer.getId());
        model.setName(topic.getBroker().getName());
        model.setType(topic.getBroker().getType());
        model.setTopic(topic.getName());
        model.setAge(Duration.between(producer.getCreatedAt(), LocalDateTime.now()));
        model.setAutoCommit(topic.isAutoCommit());
        model.setEventCount(producer.getEventCount());
        model.setCommitCount(producer.getCommitCount());
        model.setRollbackCount(producer.getRollbackCount());
        model.setStatus(producer.getStatus());
        model.setCreatedAt(producer.getCreatedAt());
        return model;
    }
}
