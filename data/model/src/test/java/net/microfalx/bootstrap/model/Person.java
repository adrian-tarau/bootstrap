package net.microfalx.bootstrap.model;

import lombok.Data;
import lombok.ToString;
import net.microfalx.lang.annotation.Id;

import java.util.Collection;

@Data
@ToString
class Person {

    @Id
    private int id;
    private String firstName;
    private String lastName;
    private String description;
    private int age;
    private Collection<Order> orders;
}