package net.microfalx.bootstrap.security.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface UserSettingRepository extends JpaRepository<UserSetting, UserSetting.Id> {

    /**
     * Finds all the user settings for a given user.
     *
     * @param userName the user name
     * @return a non-null instance
     */
    List<UserSetting> findByUserName(String userName);
}
