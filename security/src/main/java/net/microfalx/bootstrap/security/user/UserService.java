package net.microfalx.bootstrap.security.user;

import jakarta.annotation.PostConstruct;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.security.audit.Audit;
import net.microfalx.bootstrap.security.audit.AuditContext;
import net.microfalx.bootstrap.security.audit.AuditRepository;
import net.microfalx.bootstrap.security.provisioning.SecuritySettings;
import net.microfalx.bootstrap.web.preference.PreferenceService;
import net.microfalx.bootstrap.web.preference.PreferenceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static net.microfalx.bootstrap.security.SecurityConstants.ANONYMOUS_USER;
import static net.microfalx.bootstrap.security.SecurityUtils.getRandomPassword;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.capitalizeWords;

/**
 * A service around user management.
 */
@Service
public class UserService extends ApplicationContextSupport implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final int DEFAULT_USER_COUNT = 2;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingRepository userSettingRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private SecuritySettings settings;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Returns the entity which contains the user information for the user attached to the web session.
     *
     * @return a non-null instance
     * @throws SecurityException if such a user does not exist
     */
    public User getCurrentUser() {
        User user = findUser(false);
        if (user == null) throw new SecurityException("A user with user name '" + getCurrentUserName()
                + "' could not be located");
        return user;
    }

    /**
     * Returns a setting for the current user.
     *
     * @param name the setting name
     * @return the setting, null if it does not exist
     */
    public byte[] getSetting(String name) {
        return getSetting(getCurrentUser(), name);
    }

    /**
     * Returns a setting for a given user.
     *
     * @param user the user
     * @param name the setting name
     * @return the setting, null if it does not exist
     */
    public byte[] getSetting(User user, String name) {
        requireNonNull(user);
        requireNotEmpty(name);
        Optional<UserSetting> setting = userSettingRepository.findById(new UserSetting.Id(user.getUserName().toLowerCase(), name.toLowerCase()));
        return setting.map(UserSetting::getValue).orElseGet(() -> null);
    }

    /**
     * Changes a setting for the current user.
     *
     * @param name  the setting name
     * @param value the setting value
     */
    public void setSetting(String name, byte[] value) {
        setSetting(getCurrentUser(), name, value);
    }

    /**
     * Changes a setting for a given user
     *
     * @param user  the user
     * @param name  the setting name
     * @param value the setting value
     */
    public void setSetting(User user, String name, byte[] value) {
        requireNonNull(user);
        requireNotEmpty(name);
        UserSetting setting = new UserSetting();
        setting.setUserName(user.getUserName().toLowerCase());
        setting.setName(name.toLowerCase());
        setting.setValue(value);
        setting.setCreatedAt(LocalDateTime.now());
        setting.setModifiedAt(LocalDateTime.now());
        userSettingRepository.saveAndFlush(setting);

    }

    /**
     * Registers an audit action.
     * <p>
     * Information about user is provided by the service.
     *
     * @param context the audit context information
     */
    public void audit(AuditContext context) {
        requireNonNull(context);
        Audit audit = createAudit(context);
        try {
            auditRepository.saveAndFlush(audit);
        } catch (Exception e) {
            LOGGER.error("Failed to audit action '" + context.getAction() + "' for user '" + getCurrentUserName() + "', details: " + context.getDescription(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        createDefaultUsers();
    }

    @PostConstruct
    protected void afterStartup() {
        PreferenceService preferenceService = getBean(PreferenceService.class);
        preferenceService.setStorage(new UserPreferenceListener());
    }

    private Audit createAudit(AuditContext context) {
        User user = findUser(true);
        Audit audit = new Audit();
        audit.setUser(user);
        audit.setAction(context.getAction());
        audit.setModule(context.getModule());
        audit.setCategory(context.getCategory());
        audit.setDescription(context.getDescription());
        audit.setErrorCode(context.getErrorCode());
        audit.setReference(context.getReference());
        audit.setClientInfo(context.getClientInfo());
        audit.setCreatedAt(LocalDateTime.now());
        return audit;
    }

    private User findUser(boolean create) {
        return findUser(create, getCurrentUserName());
    }

    private User findUser(boolean create, String userName) {
        User user = userRepository.findByUserName(userName.toLowerCase());
        if (user == null && create) {
            user = createUser(userName, getRandomPassword(), capitalizeWords(userName),
                    "A generated user with no permissions for auditing purposes");
        }
        return user;
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return ANONYMOUS_USER;
        }
    }

    private UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        } else {
            return null;
        }
    }

    private User createUser(String userName, String password, String name, String description) {
        User user = new User();
        user.setUserName(userName.toLowerCase());
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setDescription(description);
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);
        userRepository.saveAndFlush(user);
        return user;
    }

    private void updateRoles(User user, GrantedAuthority... authorities) {

    }

    private void createDefaultUsers() {
        try {
            if (userRepository.count() >= DEFAULT_USER_COUNT) return;
            createUser(settings.getAdminUserName(), settings.getAdminPassword(), capitalizeWords(settings.getAdminUserName()),
                    "A default administrator");
            createUser(settings.getGuestUserName(), settings.getGuestPassword(), capitalizeWords(settings.getGuestUserName()),
                    "A user with no permissions to access public resources");
        } catch (Exception e) {
            LOGGER.error("Failed to create default users", e);
        }
    }

    private class UserPreferenceListener implements PreferenceStorage {

        @Override
        public void store(String userName, String name, byte[] value) {
            User user = findUser(true, userName);
            setSetting(user, name, value);
        }

        @Override
        public byte[] load(String userName, String name) {
            User user = findUser(true, userName);
            return getSetting(user, name);
        }
    }
}
