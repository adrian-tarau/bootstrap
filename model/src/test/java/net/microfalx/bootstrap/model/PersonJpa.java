package net.microfalx.bootstrap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import net.microfalx.lang.annotation.I18n;

@Entity
@Table(name = "person")
@I18n("person")
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Double getDummy() {
        return dummy;
    }

    public void setDummy(Double dummy) {
        this.dummy = dummy;
    }

    @Override
    public String toString() {
        return "PersonJpa{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", description='" + description + '\'' +
                ", age=" + age +
                ", dummy=" + dummy +
                '}';
    }
}