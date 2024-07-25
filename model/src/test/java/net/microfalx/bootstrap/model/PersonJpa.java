package net.microfalx.bootstrap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.ToString;
import net.microfalx.lang.annotation.I18n;

@Entity
@Table(name = "person")
@I18n("person")
@Data
@ToString
class PersonJpa {

    private int id;

    @jakarta.persistence.Id
    @Column(name = "first_name")
    private String firstName;

    @jakarta.persistence.Id
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "description")
    private String description;

    @Column(name = "age")
    private int age;

    @Transient
    private Double dummy;
}