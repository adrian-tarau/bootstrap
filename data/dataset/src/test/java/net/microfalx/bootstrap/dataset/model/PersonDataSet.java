package net.microfalx.bootstrap.dataset.model;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.annotation.Provider;

import java.util.ArrayList;
import java.util.Collection;

@Provider
public class PersonDataSet extends MemoryDataSet<Person, Field<Person>, Integer> {

    public PersonDataSet(DataSetFactory<Person, Field<Person>, Integer> factory, Metadata<Person, Field<Person>, Integer> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Person> extractModels(Filter filterable) {
        Collection<Person> persons = new ArrayList<>();
        persons.add(new Person().setId(1).setFirstName("John").setLastName("Doe").setAge(25));
        persons.add(new Person().setId(1).setFirstName("Jane").setLastName("Doe").setAge(20));
        return persons;
    }
}
