package net.microfalx.bootstrap.template;

import lombok.Data;

@Data
public class Person {

    private String firstName = "John";
    private String lastName = "Doe";
    private int age = 21;
    private Sex sex = Sex.MALE;

    public enum Sex {
        MALE,
        FEMALE
    }
}
