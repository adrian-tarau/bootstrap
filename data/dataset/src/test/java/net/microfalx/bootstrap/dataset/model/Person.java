package net.microfalx.bootstrap.dataset.model;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.lang.annotation.Id;

@Getter
@Setter
public class Person {

    @Id
    private int id;

    private String firstName;

    private String lastName;

    private String description;

    private int age;

    private Double dummy;

    private String nonExportable;

    private String renamed;
}
