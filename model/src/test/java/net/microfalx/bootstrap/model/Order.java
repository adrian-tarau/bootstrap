package net.microfalx.bootstrap.model;

import lombok.Data;
import lombok.ToString;
import net.microfalx.lang.annotation.Id;

import java.time.LocalDateTime;

@Data
@ToString
public class Order {

    @Id
    private int id;
    private Person person;
    private LocalDateTime orderedAt;
    private int quantity;
}
