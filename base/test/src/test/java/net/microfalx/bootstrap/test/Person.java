package net.microfalx.bootstrap.test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Person {

    private String firstName;
    private String lastName;
    private int age;
    private Gender gender;

    public enum Gender {
        MALE, FEMALE
    }

}
