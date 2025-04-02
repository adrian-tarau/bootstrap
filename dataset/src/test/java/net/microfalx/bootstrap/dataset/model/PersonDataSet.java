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
public class PersonDataSet extends MemoryDataSet<PersonJpa, Field<PersonJpa>, Integer> {

    public PersonDataSet(DataSetFactory<PersonJpa, Field<PersonJpa>, Integer> factory, Metadata<PersonJpa, Field<PersonJpa>, Integer> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<PersonJpa> extractModels(Filter filterable) {
        Collection<PersonJpa> persons = new ArrayList<>();
        persons.add(new PersonJpa().setId(1).setFirstName("John").setLastName("Doe").setAge(25));
        persons.add(new PersonJpa().setId(1).setFirstName("Jane").setLastName("Doe").setAge(20));
        return persons;
    }
}
