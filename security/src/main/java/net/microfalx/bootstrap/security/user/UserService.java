package net.microfalx.bootstrap.security.user;

import jakarta.annotation.PostConstruct;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.security.audit.Audit;
import net.microfalx.bootstrap.security.audit.AuditContext;
import net.microfalx.bootstrap.security.audit.AuditRepository;
import net.microfalx.bootstrap.security.group.GroupService;
import net.microfalx.bootstrap.security.provisioning.SecurityProperties;
import net.microfalx.bootstrap.web.preference.PreferenceService;
import net.microfalx.bootstrap.web.preference.PreferenceStorage;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.bootstrap.security.SecurityConstants.ANONYMOUS_USER;
import static net.microfalx.bootstrap.security.SecurityUtils.getRandomPassword;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.StringUtils.capitalizeWords;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A service around user management.
 */
@Service
public class UserService extends ApplicationContextSupport implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final int DEFAULT_USER_COUNT = 2;
    private static final String LOGIN_ACTION = "Login";
    private static final String LOGOUT_ACTION = "Logout";
    private static final String SECURITY_MODULE = "Security";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingRepository userSettingRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private SecurityProperties settings;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private JdbcClient jdbcClient;

    private final Map<String, Role> roles = new ConcurrentHashMap<>();

    /**
     * Returns the roles registered with the application.
     *
     * @return a non-null instance
     */
    public Set<Role> getRoles() {
        return Set.copyOf(roles.values());
    }

    /**
     * Returns a role by its identifier.
     *
     * @param id the role identifier
     * @return the role
     * @throws SecurityException if the role does not exist
     */
    public Role getRole(String id) {
        requireNotEmpty(id);
        Role role = roles.get(toIdentifier(id));
        if (role == null) throw new SecurityException(" A role with identifier '" + id + " is not registered");
        return role;
    }

    /**
     * Registers a new role.
     *
     * @param role
     */
    public void registerRole(Role role) {
        requireNonNull(role);
        LOGGER.info("Register role '" + role.getId() + "', name " + role.getName());
        this.roles.put(StringUtils.toIdentifier(role.getId()), role);
    }

    /**
     * Authenticate a user.
     *
     * @param userName the user name
     * @param password the password
     * @return the user information
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the user does not exists
     * @throws org.springframework.security.authentication.BadCredentialsException     if the credentials are wrong
     * @throws org.springframework.security.core.AuthenticationException               if any other authentication related issues
     */
    public UserDetails authenticate(String userName, String password) {
        requireNotEmpty(userName);
        requireNotEmpty(password);
        UserDetails userDetails = userDetailsManager.loadUserByUsername(userName);
        return userDetails;
    }

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
        try {
            Audit audit = createAudit(context);
            auditRepository.saveAndFlush(audit);
        } catch (Exception e) {
            LOGGER.error("Failed to audit action '" + context.getAction() + "' for user '" + getCurrentUserName() + "', details: " + context.getDescription(), e);
        }
    }

    /**
     * Registers a new user.
     * <p>
     * The user will have no permissions. An administrator will have to assign appropiate permissions to users.
     *
     * @param name     the full name of the user
     * @param userName the user name
     * @param email    the email address
     * @throws SecurityException if the user cannot be registered
     */
    public void register(String name, String userName, String password, String email) {
        requireNotEmpty(name);
        requireNotEmpty(userName);
        createUser(userName, password, name, email, null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        createDefaultUsers();
        createDefaultRoles();
    }

    @PostConstruct
    protected void afterStartup() {
        PreferenceService preferenceService = getBean(PreferenceService.class);
        preferenceService.setStorage(new UserPreferenceListener());
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        AuditContext context = AuditContext.get().setAction(LOGIN_ACTION)
                .setModule(SECURITY_MODULE).setErrorCode("200")
                .setDescription("User '" + success.getAuthentication().getName() + "' was authenticated successfully");
        context = updateAuditContext(context, success.getAuthentication());
        audit(context);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        AuditContext context = AuditContext.get().setAction(LOGIN_ACTION)
                .setModule(SECURITY_MODULE).setErrorCode("403")
                .setDescription("User '" + failures.getAuthentication().getName() + "' failed to authenticate, root cause: " + getRootCauseMessage(failures.getException()));
        context = updateAuditContext(context, failures.getAuthentication());
        audit(context);
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
                    null, "A generated user with no permissions for auditing purposes");
        }
        return user;
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) return ((UserDetails) principal).getUsername();
        }
        return ANONYMOUS_USER;
    }

    private UserDetails getUserDetails(Authentication authentication) {
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            }
        }
        org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(ANONYMOUS_USER, ANONYMOUS_USER, Collections.emptyList());
        return user;
    }

    private User createUser(String userName, String password, String name, String email, String description) {
        User user = new User();
        user.setUserName(userName.toLowerCase());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setDescription(description);
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);
        userRepository.saveAndFlush(user);
        return user;
    }

    private void updateRoles(User user, GrantedAuthority... authorities) {

    }

    private AuditContext updateAuditContext(AuditContext context, Authentication authentication) {
        UserDetails userDetails = getUserDetails(authentication);
        context.setReference(userDetails.getUsername());
        return context;
    }

    private void createDefaultUsers() {
        try {
            if (userRepository.count() >= DEFAULT_USER_COUNT) return;
            createUser(settings.getAdminUserName(), settings.getAdminPassword(), capitalizeWords(settings.getAdminUserName()),
                    null, "A default administrator");
            createUser(settings.getGuestUserName(), settings.getGuestPassword(), capitalizeWords(settings.getGuestUserName()),
                    null, "A user with no permissions to access public resources");
        } catch (Exception e) {
            LOGGER.error("Failed to create default users", e);
        }
    }

    private void createDefaultRoles() {
        registerRole(Role.ADMIN);
        registerRole(Role.GUEST);
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
