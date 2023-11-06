package net.microfalx.bootstrap.security.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    /**
     * Locates a user by its username.
     *
     * @param userName the username
     * @return the user, null if it does not exist
     */
    User findByUserName(String userName);

    /**
     * Disables a user with a given id.
     *
     * @param id the primary key of the user
     */
    @Modifying
    @Query("update User u set u.enabled = false where u.enabled = true and id = ?1")
    void disable(int id);

    /**
     * Enables a user with a given id.
     *
     * @param id the primary key of the user
     */
    @Modifying
    @Query("update User u set u.enabled = true where u.enabled = false and id = ?1")
    void activate(int id);
}
