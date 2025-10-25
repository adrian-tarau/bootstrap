package net.microfalx.bootstrap.security.group.jpa;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Modifying
    @Transactional
    @Query(value = "insert into security_group_members(username,group_id) values(:username,:groupId)", nativeQuery = true)
    void addUser(@Param("groupId") int groupId,@Param("username") String username);

    @Modifying
    @Transactional
    @Query(value = "delete from security_group_members where group_id=:groupId and username=:username", nativeQuery = true)
    void removeUser(@Param("groupId") int groupId,@Param("username") String username);
}
