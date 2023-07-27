package net.microfalx.bootstrap.security.group;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface GroupRepository extends JpaRepository<Group, Integer> {

    /**
     * Locates a group by its name.
     *
     * @param name the group name
     * @return the group, null if it does not exist
     */
    Group findByName(String name);
}
