package net.microfalx.bootstrap.security.user;

import net.microfalx.bootstrap.security.audit.Audit;
import net.microfalx.bootstrap.security.audit.AuditContext;
import net.microfalx.bootstrap.security.audit.AuditRepository;
import net.microfalx.bootstrap.security.provisioning.SecuritySettings;
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

import static net.microfalx.bootstrap.security.SecurityConstants.ANONYMOUS_USER;
import static net.microfalx.bootstrap.security.SecurityUtils.getRandomPassword;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.capitalizeWords;

/**
 * A service around user management.
 */
@Service
public class UserService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final int DEFAULT_USER_COUNT = 2;

    @Autowired
    private UserRepository userRepository;

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
        if (user == null) throw new SecurityException("A user with user name '" + getUserName()
                + "' could not be located");
        return user;
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
            LOGGER.error("Failed to audit action '" + context.getAction() + "' for user '" + getUserName() + "', details: " + context.getDescription(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        createDefaultUsers();
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
        String userName = getUserName();
        User user = userRepository.findByUserName(userName);
        if (user == null && create) {
            user = createUser(userName, getRandomPassword(), capitalizeWords(userName),
                    "A generated user with no permissions for auditing purposes");
        }
        return user;
    }

    private String getUserName() {
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
        user.setUserName(userName);
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
}
