package net.microfalx.bootstrap.security.user;

import net.microfalx.bootstrap.security.SecurityUtils;
import net.microfalx.bootstrap.security.audit.Audit;
import net.microfalx.bootstrap.security.audit.AuditRepository;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static net.microfalx.bootstrap.security.SecurityConstants.ANONYMOUS_USER;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A service around user management.
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditRepository auditRepository;

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
     *
     * @param action      the action
     * @param description the description
     */
    public void audit(String action, String description) {
        requireNonNull(action);
        requireNotEmpty(description);
        Audit audit = createAudit(action, description);
        try {
            auditRepository.saveAndFlush(audit);
        } catch (Exception e) {
            LOGGER.error("Failed to audit action '" + action + "' for user '" + getUserName() + "', details: " + description, e);
        }
    }

    private Audit createAudit(String action, String description) {
        User user = findUser(true);
        Audit audit = new Audit();
        audit.setUser(user);
        audit.setAction(action);
        audit.setDescription(description);
        audit.setCreatedAt(LocalDateTime.now());
        return audit;
    }

    private User findUser(boolean create) {
        String userName = getUserName();
        User user = userRepository.findByUserName(userName);
        if (user == null && create) {
            user = new User();
            user.setUserName(userName);
            user.setName(StringUtils.capitalizeWords(userName));
            user.setPassword(SecurityUtils.getRandomPassword());
            user.setDescription("A user with no permissions to access public resources");
            user.setCreatedAt(LocalDateTime.now());
            user.setEnabled(false);
            userRepository.saveAndFlush(user);
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
}
