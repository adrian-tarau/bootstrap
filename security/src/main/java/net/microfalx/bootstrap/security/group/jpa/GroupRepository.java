package net.microfalx.bootstrap.security.group.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer>, JpaSpecificationExecutor<Group> {

    /**
     * Locates a group by its name.
     *
     * @param name the group name
     * @return the group, null if it does not exist
     */
    Group findByName(String name);
}
