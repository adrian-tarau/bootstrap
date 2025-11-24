package net.microfalx.bootstrap.security.user.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    /**
     * Locates a user by its username.
     *
     * @param userName the username
     * @return the user, null if it does not exist
     */
    User findByUserName(String userName);

    /**
     * Locates a user by its token.
     *
     * @param token the token
     * @return the user, null if it does not exist
     */
    User findByToken(String token);

    /**
     * Disables a user with a given id.
     *
     * @param id the primary key of the user
     */
    @Modifying
    @Transactional
    @Query("update User u set u.enabled = false where u.enabled = true and id = ?1")
    void disable(int id);

    @Modifying
    @Transactional
    @Query(value = "update security_users set token = :token where username = :username", nativeQuery = true)
    void updateToken(@Param("token") String token, @Param("username") String username);

    @Modifying
    @Transactional
    @Query(value = "update security_users set password = :password, reset_password = :reset where username = :username", nativeQuery = true)
    void updatePassword(@Param("password") String password, @Param("reset") boolean reset, @Param("username") String username);

    /**
     * Enables a user with a given id.
     *
     * @param id the primary key of the user
     */
    @Modifying
    @Transactional
    @Query("update User u set u.enabled = true where u.enabled = false and id = ?1")
    void activate(int id);

    @Modifying
    @Transactional
    @Query(value = "insert into security_group_members(username,group_id) values(:username,:groupId)", nativeQuery = true)
    void addUserToGroup(@Param("username") String username, @Param("groupId") int groupId);

    @Modifying
    @Transactional
    @Query(value = "delete from security_group_members where username=:username and group_id=:groupId", nativeQuery = true)
    void removeUserToGroup(@Param("username") String username, @Param("groupId") int groupId);
}
